package eionet.cr.harvest.service;

import eionet.cr.dto.enums.ExternalServiceType;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.service.impl.DataDictService;

/**
 * Factory for returning a module for an external service.
 */
public class ExternalServiceFactory {

    /**
     * Prevent direct initialization.
     */
    private ExternalServiceFactory() {
        throw new UnsupportedOperationException();
    }
    /**
     * Returning correct service module for the service type.
     * @param type Service type
     * @return Service module
     * @throws HarvestException if unknown type (module not found)
     */
    public static ExternalService getService(ExternalServiceType type) throws HarvestException {
        if (ExternalServiceType.DATADICT.equals(type)) {
            return new DataDictService();
        }
        
        throw new HarvestException("Unknown service type " + type.toString());
    }
}
