package com.mob300.auctionapp;

import java.io.Serializable;
import java.util.List;

public abstract class Offer implements Serializable {
    private String id;
    private String name;
    private List<String> imageUrls;
    private String userId;
    public Offer() {
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Offer(String id, String name, List<String> imageUrl, String userId) {
        this.id = id;
        this.name = name;
        this.imageUrls = imageUrl;
        this.userId = userId;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getDescription();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
