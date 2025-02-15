package eu.einfracentral.service;

import eu.einfracentral.domain.*;
import eu.einfracentral.exception.ResourceException;
import eu.einfracentral.exception.ResourceNotFoundException;
import eu.einfracentral.exception.ValidationException;
import eu.einfracentral.registry.manager.CatalogueManager;
import eu.einfracentral.registry.manager.PendingProviderManager;
import eu.einfracentral.registry.manager.ProviderManager;
import eu.einfracentral.registry.service.InfraServiceService;
import eu.einfracentral.registry.service.PendingResourceService;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.service.ServiceException;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service("securityService")
public class OIDCSecurityService implements SecurityService {

    private final ProviderManager providerManager;
    private final CatalogueManager catalogueManager;
    private final PendingProviderManager pendingProviderManager;
    private final InfraServiceService<InfraService, InfraService> infraServiceService;
    private final PendingResourceService<InfraService> pendingServiceManager;
    private OIDCAuthenticationToken adminAccess;

    @Value("${project.name:}")
    private String projectName;

    @Value("${mail.smtp.from:}")
    private String projectEmail;

    @Autowired
    OIDCSecurityService(ProviderManager providerManager, CatalogueManager catalogueManager,
                        InfraServiceService<InfraService, InfraService> infraServiceService,
                        PendingProviderManager pendingProviderManager, PendingResourceService<InfraService> pendingServiceManager) {
        this.providerManager = providerManager;
        this.catalogueManager = catalogueManager;
        this.infraServiceService = infraServiceService;
        this.pendingProviderManager = pendingProviderManager;
        this.pendingServiceManager = pendingServiceManager;

        // create admin access
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        DefaultUserInfo userInfo = new DefaultUserInfo();
        userInfo.setEmail(projectEmail);
        userInfo.setId(1L);
        userInfo.setGivenName(projectName);
        userInfo.setFamilyName("");
        adminAccess = new OIDCAuthenticationToken("", "", userInfo, roles, null, "", "");
    }

    public Authentication getAdminAccess() {
        return adminAccess;
    }

    @Override
    public String getRoleName(Authentication authentication) {
        String role;
        if (hasRole(authentication, "ROLE_ADMIN")) {
            role = "admin";
        } else if (hasRole(authentication, "ROLE_EPOT")) {
            role = "EPOT";
        } else if (hasRole(authentication, "ROLE_PROVIDER")) {
            role = "provider";
        } else {
            role = "user";
        }
        return role;
    }

    public boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    public boolean isProviderAdmin(Authentication auth, @NotNull String providerId) {
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsProviderAdmin(user, providerId);
    }

    public boolean isProviderAdmin(Authentication auth, @NotNull String providerId, boolean noThrow) {
        if (auth == null && noThrow) {
            return false;
        }
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsProviderAdmin(user, providerId);
    }

    public boolean userIsProviderAdmin(User user, @NotNull String providerId) {
        ProviderBundle registeredProvider;
        try {
            registeredProvider = providerManager.get(providerId);
        } catch (ResourceException e) {
            try {
                registeredProvider = pendingProviderManager.get(providerId);
            } catch (RuntimeException re) {
                return false;
            }
        } catch (RuntimeException e) {
            return false;
        }
        if (registeredProvider == null) {
            throw new ResourceNotFoundException("Provider with id '" + providerId + "' does not exist.");
        }
        if (registeredProvider.getProvider().getUsers() == null) {
            return false;
        }
        return registeredProvider.getProvider().getUsers()
                .parallelStream()
                .filter(Objects::nonNull)
                .anyMatch(u -> {
                    if (u.getId() != null) {
                        if (u.getEmail() != null) {
                            return u.getId().equals(user.getId())
                                    || u.getEmail().equalsIgnoreCase(user.getEmail());
                        }
                        return u.getId().equals(user.getId());
                    }
                    return u.getEmail().equalsIgnoreCase(user.getEmail());
                });
    }

    public boolean isCatalogueAdmin(Authentication auth, @NotNull String catalogueId) {
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsCatalogueAdmin(user, catalogueId);
    }

    public boolean isCatalogueAdmin(Authentication auth, @NotNull String catalogueId, boolean noThrow) {
        if (auth == null && noThrow) {
            return false;
        }
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsCatalogueAdmin(user, catalogueId);
    }

    public boolean userIsCatalogueAdmin(User user, @NotNull String catalogueId) {
        CatalogueBundle registeredCatalogue;
        try {
            registeredCatalogue = catalogueManager.get(catalogueId);
        } catch (RuntimeException e) {
            return false;
        }
        if (registeredCatalogue == null) {
            throw new ResourceNotFoundException("Catalogue with id '" + catalogueId + "' does not exist.");
        }
        if (registeredCatalogue.getCatalogue().getUsers() == null) {
            return false;
        }
        return registeredCatalogue.getCatalogue().getUsers()
                .parallelStream()
                .filter(Objects::nonNull)
                .anyMatch(u -> {
                    if (u.getId() != null) {
                        if (u.getEmail() != null) {
                            return u.getId().equals(user.getId())
                                    || u.getEmail().equalsIgnoreCase(user.getEmail());
                        }
                        return u.getId().equals(user.getId());
                    }
                    return u.getEmail().equalsIgnoreCase(user.getEmail());
                });
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, String serviceId) {
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, serviceId);
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, String serviceId, String catalogueId) {
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, serviceId, catalogueId);
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, String serviceId, boolean noThrow) {
        if (auth == null && noThrow) {
            return false;
        }
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, serviceId);
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, eu.einfracentral.domain.Service service) {
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, service);
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, eu.einfracentral.domain.Service service, boolean noThrow) {
        if (auth == null && noThrow) {
            return false;
        }
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, service);
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, eu.einfracentral.domain.InfraService infraService) {
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, infraService);
    }

    @Override
    public boolean isServiceProviderAdmin(Authentication auth, eu.einfracentral.domain.InfraService infraService, boolean noThrow) {
        if (auth == null && noThrow) {
            return false;
        }
        if (hasRole(auth, "ROLE_ANONYMOUS")) {
            return false;
        }
        User user = User.of(auth);
        return userIsServiceProviderAdmin(user, infraService);
    }

    @Override
    public boolean userIsServiceProviderAdmin(User user, eu.einfracentral.domain.Service service) {
        if (service.getResourceOrganisation() == null || service.getResourceOrganisation().equals("")) {
            throw new ValidationException("Service has no Service Organisation");
        }
//        List<String> allProviders = service.getResourceProviders();
//        allProviders.add(service.getResourceOrganisation());
        List<String> allProviders = Collections.singletonList(service.getResourceOrganisation());
        Optional<List<String>> providers = Optional.of(allProviders);
        return providers
                .get()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(id -> userIsProviderAdmin(user, id));
    }

    @Override
    public boolean userIsServiceProviderAdmin(User user, InfraService infraService) {
        return userIsServiceProviderAdmin(user, infraService.getService());
    }

    @Override
    public boolean userIsServiceProviderAdmin(@NotNull User user, String serviceId) {
        InfraService service;
        try {
            service = infraServiceService.get(serviceId);
        } catch (ResourceException | ResourceNotFoundException e) {
            try {
                service = pendingServiceManager.get(serviceId);
            } catch (RuntimeException re) {
                return false;
            }
        } catch (RuntimeException e) {
            return false;
        }
        if (service.getService().getResourceOrganisation() == null || service.getService().getResourceOrganisation().equals("")) {
            throw new ValidationException("Service has no Service Organisation");
        }
//        List<String> allProviders = service.getService().getResourceProviders();
//        allProviders.add(service.getService().getResourceOrganisation());
        List<String> allProviders = Collections.singletonList(service.getService().getResourceOrganisation());
        Optional<List<String>> providers = Optional.of(allProviders);
        return providers
                .get()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(id -> userIsProviderAdmin(user, id));
    }

    @Override
    public boolean userIsServiceProviderAdmin(@NotNull User user, String serviceId, String catalogueId) {
        InfraService service;
        try {
            service = infraServiceService.get(serviceId, catalogueId);
        } catch (ResourceException | ResourceNotFoundException e) {
            try {
                service = pendingServiceManager.get(serviceId);
            } catch (RuntimeException re) {
                return false;
            }
        } catch (RuntimeException e) {
            return false;
        }
        if (service.getService().getResourceOrganisation() == null || service.getService().getResourceOrganisation().equals("")) {
            throw new ValidationException("Service has no Service Organisation");
        }

        List<String> allProviders = Collections.singletonList(service.getService().getResourceOrganisation());
        Optional<List<String>> providers = Optional.of(allProviders);
        return providers
                .get()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(id -> userIsProviderAdmin(user, id));
    }

    public boolean providerCanAddServices(Authentication auth, String serviceId) {
        return providerCanAddServices(auth, infraServiceService.get(serviceId));
    }

    public boolean providerCanAddServices(Authentication auth, InfraService infraService) {
        return providerCanAddServices(auth, infraService.getService());
    }

    public boolean providerCanAddServices(Authentication auth, eu.einfracentral.domain.Service service) {
//        List<String> providerIds = service.getService().getResourceProviders();
//        providerIds.add((service.getService().getResourceOrganisation()));
        List<String> providerIds = Collections.singletonList(service.getResourceOrganisation());
        for (String providerId : providerIds) {
            ProviderBundle provider = providerManager.get(providerId);
            if (isProviderAdmin(auth, provider.getId())) {
                if (provider.getStatus() == null) {
                    throw new ServiceException("Provider status field is null");
                }
                if (provider.isActive() && provider.getStatus().equals("approved provider")) {
                    return true;
                } else if (provider.getTemplateStatus().equals("no template status")) {
                    FacetFilter ff = new FacetFilter();
                    ff.addFilter("resource_organisation", provider.getId());
                    if (infraServiceService.getAll(ff, getAdminAccess()).getResults().isEmpty()) {
                        return true;
                    }
                    throw new ResourceException("You have already created a Service Template.", HttpStatus.CONFLICT);
                }
            }
        }
        return false;
    }

    public boolean providerIsActiveAndUserIsAdmin(Authentication auth, String serviceId) {
        InfraService service = infraServiceService.get(serviceId);
//        List<String> providerIds = service.getService().getResourceProviders();
//        providerIds.add(service.getService().getResourceOrganisation());
        List<String> providerIds = Collections.singletonList(service.getService().getResourceOrganisation());
        for (String providerId : providerIds) {
            ProviderBundle provider = providerManager.get(providerId);
            if (provider != null && provider.isActive()) {
                if (isProviderAdmin(auth, providerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean serviceIsActive(String serviceId) {
        InfraService service = infraServiceService.get(serviceId);
        return service.isActive();
    }

    public boolean serviceIsActive(String serviceId, String catalogueId) {
        InfraService service = infraServiceService.get(serviceId, catalogueId);
        return service.isActive();
    }

    public boolean serviceIsActive(String serviceId, String catalogueId, String version) {
        // FIXME: serviceId is equal to 'rich' and version holds the service ID
        //  when searching for a Rich Service without providing a version
        if ("rich".equals(serviceId)) {
            serviceId = version;
        }
        InfraService service = infraServiceService.get(serviceId, catalogueId, "latest");
        return service.isActive();
    }
}
