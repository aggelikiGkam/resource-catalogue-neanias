package eu.einfracentral.registry.manager;

import eu.einfracentral.domain.*;
import eu.einfracentral.registry.service.ResourceService;
import eu.einfracentral.service.IdCreator;
import eu.openminted.registry.core.domain.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;

import java.security.NoSuchAlgorithmException;

@org.springframework.stereotype.Service("interoperabilityRecordManager")
public class InteroperabilityRecordManager extends ResourceManager<InteroperabilityRecord> implements ResourceService<InteroperabilityRecord, Authentication> {

    private static final Logger logger = LogManager.getLogger(InteroperabilityRecordManager.class);
    private final IdCreator idCreator;

    public InteroperabilityRecordManager(IdCreator idCreator) {
        super(InteroperabilityRecord.class);
        this.idCreator = idCreator;
    }

    @Override
    public String getResourceType() {
        return "interoperability_record";
    }

    @Override
    public InteroperabilityRecord add(InteroperabilityRecord interoperabilityRecord, Authentication auth) {
        validate(interoperabilityRecord);
        try {
            interoperabilityRecord.setId(idCreator.createInteroperabilityRecordId(interoperabilityRecord));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        interoperabilityRecord.setCreated(String.valueOf(System.currentTimeMillis()));
        interoperabilityRecord.setUpdated(String.valueOf(System.currentTimeMillis()));
        logger.trace("User '{}' is attempting to add a new Interoperability Record: {}", auth, interoperabilityRecord);
        logger.info("Adding Interoperability Record: {}", interoperabilityRecord);
        super.add(interoperabilityRecord, auth);

        return interoperabilityRecord;
    }

    @Override
    public InteroperabilityRecord update(InteroperabilityRecord interoperabilityRecord, Authentication auth) {

        logger.trace("User '{}' is attempting to update the Interoperability Record with id '{}'", auth, interoperabilityRecord.getId());
        validate(interoperabilityRecord);
        interoperabilityRecord.setUpdated(String.valueOf(System.currentTimeMillis()));
        Resource existing = whereID(interoperabilityRecord.getId(), true);
        existing.setPayload(serialize(interoperabilityRecord));
        existing.setResourceType(resourceType);

        resourceService.updateResource(existing);
        logger.debug("Updating Interoperability Record: {}", interoperabilityRecord);

        return interoperabilityRecord;
    }

}
