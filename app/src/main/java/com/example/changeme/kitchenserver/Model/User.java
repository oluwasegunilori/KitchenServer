package com.example.changeme.kitchenserver.Model;

/**
 * Created by SHEGZ on 1/3/2018.
 */
public class User {
    private String isStaff;
    private String name;
    private String password;
    private String phone;

    public User(String isStaff, String name, String password, String phone) {

        this.isStaff = isStaff;
        this.name = name;
        this.password = password;
        this.phone = phone;
    }

    public User() {

    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
