package com.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 委托单
 * */
public class Order implements Serializable{
    @JSONField(name = "CONSIGNEE_C_NAME")
    private String buyerCN;
    @JSONField(name = "CONSIGNEE_E_NAME")
    private String buyerEN;
    @JSONField(name = "FORWARDER_NAME")
    private String delegate;
    @JSONField(name = "FLOW_STATUS")
    private String status;
    @JSONField(name = "ID")
    private String id;
    @JSONField(name = "ROW_ID")
    private String rowId;
    @JSONField(name = "CONT_INFO")
    private ArrayList<Container> containers;
    @JSONField(name = "BILL_NO")
    private String billNo;
    @JSONField(name = "ADDR")
    private String address;

    private boolean spread;



    @Override
    public String toString() {
        return "Order{" +
                "buyerCN='" + buyerCN + '\'' +
                ", buyerEN='" + buyerEN + '\'' +
                ", delegate='" + delegate + '\'' +
                ", status='" + status + '\'' +
                ", id='" + id + '\'' +
                ", rowId='" + rowId + '\'' +
                ", containers=" + containers +
                ", billNo='" + billNo + '\'' +
                ", address='" + address + '\'' +
                ", spread=" + spread +
                '}';
    }

    public String getBuyerCN() {
        return buyerCN;
    }

    public void setBuyerCN(String buyerCN) {
        this.buyerCN = buyerCN;
    }

    public String getBuyerEN() {
        return buyerEN;
    }

    public void setBuyerEN(String buyerEN) {
        this.buyerEN = buyerEN;
    }

    public String getDelegate() {
        return delegate;
    }

    public void setDelegate(String delegate) {
        this.delegate = delegate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public ArrayList<Container> getContainers() {
        return containers;
    }

    public void setContainers(ArrayList<Container> containers) {
        this.containers = containers;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static class Container implements Serializable{
        @JSONField(name = "CNTR_TYPE")
        private String type;
        @JSONField(name = "CNTR_SIZE")
        private String size;
        @JSONField(name = "TOTAL")
        private Integer total;

        private String no;
        private String date;
        private String place;

        @Override
        public String toString() {
            return "Container{" +
                    "type='" + type + '\'' +
                    ", size='" + size + '\'' +
                    ", total='" + total + '\'' +
                    '}';
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

}
