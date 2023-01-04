package eu.einfracentral.service;

import eu.einfracentral.domain.Catalogue;
import eu.einfracentral.domain.Provider;
import eu.einfracentral.domain.Vocabulary;
import eu.einfracentral.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SimpleIdCreator implements IdCreator {

    SimpleIdCreator() {
    }

    @Override
    public String createProviderId(Provider provider) {
        String providerId;
        if (provider.getAbbreviation() != null && !"".equals(provider.getAbbreviation()) && !"null".equals(provider.getAbbreviation())) {
            providerId = provider.getAbbreviation();
        } else {
            throw new ValidationException("Provider must have an abbreviation.");
        }
        return StringUtils
                .stripAccents(providerId)
                .replaceAll("[\\n\\t\\s]+", " ")
                .replaceAll("\\s+$", "")
                .replaceAll("[^a-zA-Z0-9\\s\\-\\_]+", "")
                .replace(" ", "_")
                .toLowerCase();

    }

    @Override
    public String createCatalogueId(Catalogue catalogue) {
        String catalogueId;
        if (catalogue.getId() == null || "".equals(catalogue.getId())) {
            if (catalogue.getAbbreviation() != null && !"".equals(catalogue.getAbbreviation()) && !"null".equals(catalogue.getAbbreviation())) {
                catalogueId = catalogue.getAbbreviation();
            } else {
                throw new ValidationException("Catalogue must have an abbreviation.");
            }
        } else {
            catalogueId = catalogue.getId();
        }
        return StringUtils
                .stripAccents(catalogueId)
                .replaceAll("[\\n\\t\\s]+", " ")
                .replaceAll("\\s+$", "")
                .replaceAll("[^a-zA-Z0-9\\s\\-\\_]+", "")
                .replace(" ", "_")
                .toLowerCase();

    }

    @Override
    public String createServiceId(eu.einfracentral.domain.Service service) {
        if (service.getResourceOrganisation() == null || service.getResourceOrganisation().equals("")) {
            throw new ValidationException("Resource must have a Resource Organisation.");
        }
        String serviceId;
        if (service.getAbbreviation() != null && !"".equals(service.getAbbreviation()) && !"null".equals(service.getAbbreviation())) {
            serviceId = service.getAbbreviation();
        } else {
            throw new ValidationException("Resource must have an abbreviation.");
        }
        String provider = service.getResourceOrganisation();
        return String.format("%s.%s", provider, StringUtils
                .stripAccents(serviceId)
                .replaceAll("[\n\t\\s]+", " ")
                .replaceAll("\\s+$", "")
                .replaceAll("[^a-zA-Z0-9\\s\\-\\_]+", "")
                .replace(" ", "_")
                .toLowerCase());
    }

    @Override
    public String createHostingLegalEntityId(String providerName) {
        return StringUtils
                .stripAccents(providerName)
                .replaceAll("[\\n\\t\\s]+", " ")
                .replaceAll("\\s+$", "")
                .replaceAll("[^a-zA-Z0-9\\s\\-\\_]+", "")
                .replace(" ", "_")
                .toLowerCase();

    }
}
