package eu.einfracentral.service;

import eu.einfracentral.domain.Catalogue;
import eu.einfracentral.domain.Provider;
import eu.einfracentral.domain.ResourceBundle;
import eu.einfracentral.domain.Service;

public interface IdCreator {

    /**
     * Creates id for {@link Provider}
     *
     * @param provider
     * @return
     */
    String createProviderId(Provider provider);

    /**
     * Creates id for {@link Service}
     *
     * @param service
     * @return
     */
    String createResourceId(Service service);

    /**
     * Creates id for {@link Catalogue}
     *
     * @param catalogue
     * @return
     */
    String createCatalogueId(Catalogue catalogue);

    /**
     * *
     * @param providerName
     * @return
     */
    String createHostingLegalEntityId(String providerName);
}
