package com.sdeport.logistics.driver.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Driver implements Serializable{

    @JSONField(name = "nickName")
    private String nickname;
    @JSONField(name = "driveLicenseType")
    private String licenseType;
    @JSONField(name = "driveLicenseNo")
    private String licenseNo;
    @JSONField(name = "idCardNo")
    private String idCardNo;
    @JSONField(name = "motorcadeName")
    private String motorcadeName;
    @JSONField(name = "contactPerson")
    private String contactPerson;
    @JSONField(name = "attachStatus")
    private String attachStatus;
    @JSONField(name = "truckNo")
    private String truckNo;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getMotorcadeName() {
        return motorcadeName;
    }

    public void setMotorcadeName(String motorcadeName) {
        this.motorcadeName = motorcadeName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getAttachStatus() {
        return attachStatus;
    }

    public void setAttachStatus(String attachStatus) {
        this.attachStatus = attachStatus;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "nickname='" + nickname + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", licenseNo='" + licenseNo + '\'' +
                ", idCardNo='" + idCardNo + '\'' +
                ", motorcadeName='" + motorcadeName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", attachStatus='" + attachStatus + '\'' +
                ", truckNo='" + truckNo + '\'' +
                '}';
    }
}
