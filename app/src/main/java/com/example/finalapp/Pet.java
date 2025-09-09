package com.example.finalapp;

public class Pet {
    private String id;
    private String name;
    private String gender;
    private String type;
    private String breed;
    private double price;
    private String dob;
    private String imageUrl;

    public Pet() {}

    public Pet(String id, String name, String gender, String type, String breed, double price, String dob, String imageUrl) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.type = type;
        this.breed = breed;
        this.price = price;
        this.dob = dob;
        this.imageUrl = imageUrl;
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

