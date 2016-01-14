package eionet.cr.dto;

import eionet.cr.dto.enums.ExternalServiceType;

/**
 * External service for push services.
 */
public class ExternalServiceDTO {
    private Integer serviceId;
    private String serviceUrl;
    private String secureToken;
    
    private ExternalServiceType serviceType;
    private String userId;


    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getSecureToken() {
        return secureToken;
    }

    public void setSecureToken(String secureToken) {
        this.secureToken = secureToken;
    }

    public ExternalServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ExternalServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
