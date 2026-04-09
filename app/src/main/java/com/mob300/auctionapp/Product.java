package com.mob300.auctionapp;
import java.io.Serializable;
import java.util.List;

public class Product extends Offer implements Serializable {
    private String productId;
    private String productName;
    private String Deadline;
    private float productPrice;
    private String productDescription;
    private String contactInfo;
    private List<String> imageUrls;
    private String selectedCurrency;
    private String countryAndCity;
    private String userId;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", Deadline='" + Deadline + '\'' +
                ", productPrice=" + productPrice +
                ", productDescription='" + productDescription + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", imageUrls=" + imageUrls +
                ", selectedCurrency='" + selectedCurrency + '\'' +
                ", countryAndCity='" + countryAndCity + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public Product(String productId, String productName, float productPriceFloat, String Deadline, String productDescription, String contactInfo,
                   List<String> imageUrl, String userId, String selectedCurrency, String countryAndCity) {
        this.productId = productId;
        this.productName = productName;
        this.selectedCurrency = selectedCurrency;
        this.productPrice = productPriceFloat;
        this.Deadline = Deadline;
        this.productDescription = productDescription;
        this.contactInfo = contactInfo;
        this.imageUrls = imageUrl;
        this.userId = userId;
        this.countryAndCity = countryAndCity;
    }


    public String getSelectedCurrency() {
        return selectedCurrency;
    }

    public void setSelectedCurrency(String selectedCurrency) {
        this.selectedCurrency = selectedCurrency;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getcountryAndCity() {
        return countryAndCity;
    }

    public void setcountryAndCity(String countryAndCity) {
        this.countryAndCity = countryAndCity;
    }

    public String getDeadline() {
        return Deadline;
    }

    public void setDeadline(String deadline) {
        Deadline = deadline;
    }

    @Override
    public String getName() {
        return productName;
    }

    public float getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(float productPrice) {
        this.productPrice = productPrice;
    }



    public String getDescription() {
        return productDescription;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }



    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
