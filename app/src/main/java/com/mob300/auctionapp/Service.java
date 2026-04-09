package com.mob300.auctionapp;
import java.io.Serializable;

public class Service extends Offer implements Serializable {
    private String serviceId;
    private String serviceName;
    private String serviceDescription;
    private String contactInfo;
    private String imageUrl;
    private String userId;

    public Service() {
        // Default constructor required for calls to DataSnapshot.getValue(Service.class)
    }

    public Service(String serviceId, String serviceName, String serviceDescription, String contactInfo, String imageUrl, String userId) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.contactInfo = contactInfo;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public String getDescription() {
        return serviceDescription;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
