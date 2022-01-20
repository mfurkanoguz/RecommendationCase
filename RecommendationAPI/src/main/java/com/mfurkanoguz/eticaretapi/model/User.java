package com.mfurkanoguz.eticaretapi.model;


import java.util.Set;

public class User {
    String userid;
    String type;
    Set<String> products;

    public User() {
    }

    public User(String userid, String type, Set<String> products) {
        this.userid = userid;
        this.type = type;
        this.products = products;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getProducts() {
        return products;
    }

    public void setProducts(Set<String> products) {
        this.products = products;
    }
}

