package com.inspur.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Driver implements Serializable {
    @JSONField(name = "id")
    private String id;
    @JSONField(name = "fkOrgId")
    private String fkOrgId;
    @JSONField(name = "fkOrg9")
    private String fkOrg9;
    @JSONField(name = "fkMotorcadeId")
    private String motocadeId;
    @JSONField(name = "staffType")
    private String staffType;
    @JSONField(name = "truckNo")
    private String truckNo;
    @JSONField(name = "fkUserId")
    private String fkUserId;
    @JSONField(name = "chiAccount")
    private String chiAccount;
    @JSONField(name = "idCardNo")
    private String idCardNo;
    @JSONField(name = "accountCName")
    private String accountName;
    @JSONField(name = "nickName")
    private String nick;
    @JSONField(name = "phone")
    private String phone;
    @JSONField(name = "inUse")
    private String inUse;
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
    @JSONField(name = "pageNum")
    private String pageNum;
    @JSONField(name = "pageSize")
    private String pageSize;

    private boolean spread;

    @Override
    public String toString() {
        return "Driver{" +
                "id='" + id + '\'' +
                ", fkOrgId='" + fkOrgId + '\'' +
                ", fkOrg9='" + fkOrg9 + '\'' +
                ", motocadeId='" + motocadeId + '\'' +
                ", staffType='" + staffType + '\'' +
                ", truckNo='" + truckNo + '\'' +
                ", fkUserId='" + fkUserId + '\'' +
                ", chiAccount='" + chiAccount + '\'' +
                ", idCardNo='" + idCardNo + '\'' +
                ", accountName='" + accountName + '\'' +
                ", nick='" + nick + '\'' +
                ", phone='" + phone + '\'' +
                ", inUse='" + inUse + '\'' +
                ", remark='" + remark + '\'' +
                ", cDate='" + cDate + '\'' +
                ", cOrgId='" + cOrgId + '\'' +
                ", cUserId='" + cUserId + '\'' +
                ", uDate='" + uDate + '\'' +
                ", uOrgId='" + uOrgId + '\'' +
                ", uUserId='" + uUserId + '\'' +
                ", flag='" + flag + '\'' +
                ", pageNum='" + pageNum + '\'' +
                ", pageSize='" + pageSize + '\'' +
                ", spread=" + spread +
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

    public String getStaffType() {
        return staffType;
    }

    public void setStaffType(String staffType) {
        this.staffType = staffType;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }

    public String getFkUserId() {
        return fkUserId;
    }

    public void setFkUserId(String fkUserId) {
        this.fkUserId = fkUserId;
    }

    public String getChiAccount() {
        return chiAccount;
    }

    public void setChiAccount(String chiAccount) {
        this.chiAccount = chiAccount;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getInUse() {
        return inUse;
    }

    public void setInUse(String inUse) {
        this.inUse = inUse;
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

    public String getPageNum() {
        return pageNum;
    }

    public void setPageNum(String pageNum) {
        this.pageNum = pageNum;
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
