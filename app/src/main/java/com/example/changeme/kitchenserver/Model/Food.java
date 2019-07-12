package com.example.changeme.kitchenserver.Model;

/**
 * Created by SHEGZ on 1/4/2018.
 */
public class Food {
    private String name;
    private String menuid;
    private String image;
    private String description;
    private String price;
    private String discount;


    public Food(String name, String menuid, String image, String description, String price, String discount) {

        this.name = name;
        this.menuid = menuid;
        this.image = image;
        this.description = description;
        this.price = price;
        this.discount = discount;
    }

    public Food() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMenuid() {
        return menuid;
    }

    public void setMenuid(String menuid) {
        this.menuid = menuid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
