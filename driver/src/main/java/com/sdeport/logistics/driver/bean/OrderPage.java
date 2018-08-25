package com.sdeport.logistics.driver.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 订单页数辅助
 * */
public class OrderPage implements Serializable {

    private int currentPage = 1;
    private int total;
    private int countPerPage = 10;
    private String status = "0";//0 全部，1 待接单， 2 已接单， 3 已完成
    private boolean isLoading;
    private ArrayList<Order> datas = new ArrayList<>();

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCountPerPage() {
        return countPerPage;
    }

    public void setCountPerPage(int countPerPage) {
        this.countPerPage = countPerPage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public ArrayList<Order> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<Order> datas) {
        this.datas = datas;
    }
}
