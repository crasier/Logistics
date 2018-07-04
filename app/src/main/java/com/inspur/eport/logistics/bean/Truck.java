package com.inspur.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Truck implements Serializable {
    @JSONField(name = "id")
    private String id;
    @JSONField(name = "fkOrgId")
    private String fkOrgId;
    @JSONField(name = "fkOrg9")
    private String fkOrg9;
    @JSONField(name = "fkMotorcadeId")
    private String motocadeId;
    @JSONField(name = "motorcadeCode")
    private String motocadeCode;
    @JSONField(name = "truckNo")
    private String truckNo;
    @JSONField(name = "drivingLicNo")
    private String licenseId;
    @JSONField(name = "truckType")
    private String truckType;
    @JSONField(name = "carryCap")
    private String carryCap;
    @JSONField(name = "truckStatus")
    private String truckStatus;
    @JSONField(name = "techRemark")
    private String remark;
    @JSONField(name = "cDate")
    private String cDate;
    @JSONField(name = "cOrgId")
    private String cOrgId;
    @JSONField(name = "cUserId")
    private String cUserId;
    @JSONField(name = "uDate")
    private String uDate;
    @JSONField(name = "uOrgId")
    private String uOrgId;
    @JSONField(name = "uUserId")
    private String uUserId;
    @JSONField(name = "flag")
    private String flag;
    @JSONField(name = "motorcadeName")
    private String motocadeName;
    @JSONField(name = "pageNumber")
    private String pageNumber;
    @JSONField(name = "pageSize")
    private String pageSize;

    private boolean spread;

    @Override
    public String toString() {
        return "Truck{" +
                "id='" + id + '\'' +
                ", fkOrgId='" + fkOrgId + '\'' +
                ", fkOrg9='" + fkOrg9 + '\'' +
                ", motocadeId='" + motocadeId + '\'' +
                ", motocadeCode='" + motocadeCode + '\'' +
                ", truckNo='" + truckNo + '\'' +
                ", licenseId='" + licenseId + '\'' +
                ", truckType='" + truckType + '\'' +
                ", carryCap='" + carryCap + '\'' +
                ", truckStatus='" + truckStatus + '\'' +
                ", remark='" + remark + '\'' +
                ", cDate='" + cDate + '\'' +
                ", cOrgId='" + cOrgId + '\'' +
                ", cUserId='" + cUserId + '\'' +
                ", uDate='" + uDate + '\'' +
                ", uOrgId='" + uOrgId + '\'' +
                ", uUserId='" + uUserId + '\'' +
                ", flag='" + flag + '\'' +
                ", motocadeName='" + motocadeName + '\'' +
                ", pageNumber='" + pageNumber + '\'' +
                ", pageSize='" + pageSize + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFkOrgId() {
        return fkOrgId;
    }

    public void setFkOrgId(String fkOrgId) {
        this.fkOrgId = fkOrgId;
    }

    public String getFkOrg9() {
        return fkOrg9;
    }

    public void setFkOrg9(String fkOrg9) {
        this.fkOrg9 = fkOrg9;
    }

    public String getMotocadeId() {
        return motocadeId;
    }

    public void setMotocadeId(String motocadeId) {
        this.motocadeId = motocadeId;
    }

    public String getMotocadeCode() {
        return motocadeCode;
    }

    public void setMotocadeCode(String motocadeCode) {
        this.motocadeCode = motocadeCode;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getTruckType() {
        return truckType;
    }

    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }

    public String getCarryCap() {
        return carryCap;
    }

    public void setCarryCap(String carryCap) {
        this.carryCap = carryCap;
    }

    public String getTruckStatus() {
        return truckStatus;
    }

    public void setTruckStatus(String truckStatus) {
        this.truckStatus = truckStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getcDate() {
        return cDate;
    }

    public void setcDate(String cDate) {
        this.cDate = cDate;
    }

    public String getcOrgId() {
        return cOrgId;
    }

    public void setcOrgId(String cOrgId) {
        this.cOrgId = cOrgId;
    }

    public String getcUserId() {
        return cUserId;
    }

    public void setcUserId(String cUserId) {
        this.cUserId = cUserId;
    }

    public String getuDate() {
        return uDate;
    }

    public void setuDate(String uDate) {
        this.uDate = uDate;
    }

    public String getuOrgId() {
        return uOrgId;
    }

    public void setuOrgId(String uOrgId) {
        this.uOrgId = uOrgId;
    }

    public String getuUserId() {
        return uUserId;
    }

    public void setuUserId(String uUserId) {
        this.uUserId = uUserId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getMotocadeName() {
        return motocadeName;
    }

    public void setMotocadeName(String motocadeName) {
        this.motocadeName = motocadeName;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }
}
