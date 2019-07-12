package com.example.changeme.kitchenserver.Model;

/**
 * Created by SHEGZ on 1/4/2018.
 */
public class Order {
    private String productid;

    private String productname;
    private String quantity;
    private String price;
    private String discount;

    public Order() {

    }

    public Order(String productid, String productname, String quantity, String price, String discount) {
        this.productid = productid;
        this.productname = productname;
        this.quantity = quantity;
        this.price = price;
        this.discount = discount;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
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
