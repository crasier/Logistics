package com.sdeport.logistics.driver.bean;

import java.io.Serializable;

public class Order implements Serializable {

    private String id;
    private String status;

    private boolean spread;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", spread=" + spread +
                '}';
    }
}

