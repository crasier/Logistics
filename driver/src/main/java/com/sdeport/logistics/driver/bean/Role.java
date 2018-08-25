package com.sdeport.logistics.driver.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Role implements Serializable {
    @JSONField(name = "userName")
    private String userName;
    @JSONField(name = "cnName")
    private String cnName;
    @JSONField(name = "phoneNumber")
    private String phoneNumber;
    @JSONField(name = "roleInfo")
    private String roleInfo;

    @JSONField(name = "driverInfo")
    private String driverInfo;

    private Driver driver;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRoleInfo() {
        return roleInfo;
    }

    public void setRoleInfo(String roleInfo) {
        this.roleInfo = roleInfo;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(String driverInfo) {
        this.driverInfo = driverInfo;
    }

    @Override
    public String toString() {
        return "Role{" +
                "userName='" + userName + '\'' +
                ", cnName='" + cnName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", roleInfo='" + roleInfo + '\'' +
                ", driverInfo=" + driverInfo +
                ", driver=" + driver +
                '}';
    }
}
