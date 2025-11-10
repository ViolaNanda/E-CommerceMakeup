package com.example.e_commerce_5.models;

public class Product {
    private String id;
    private String name;
    private String description;
    private String price;
    private String oldPrice;
    private String imageUrl;
    private String category;

    public Product(String id, String name, String description, String price, String oldPrice, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.oldPrice = oldPrice;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    // Setters (biasanya tidak diperlukan untuk Model sederhana, tapi disertakan)
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}