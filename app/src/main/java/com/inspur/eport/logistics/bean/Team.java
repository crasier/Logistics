package com.inspur.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Team implements Serializable{
    @JSONField(name = "MOTORCADE_TYPE")
    private String type;
    @JSONField(name = "C_DATE")
    private String createTime;
    @JSONField(name = "MOTORCADE_NAME")
    private String typeName;
    @JSONField(name = "MOTORCADE_STAFF_ID")
    private String staffId;
    @JSONField(name = "MOTORCADE_ID")
    private String id;
    @JSONField(name = "TRUCK_NO")
    private String truckNo;
    @JSONField(name = "FK_MOTORCADE_ID")
    private String fkId;
    @JSONField(name = "IN_USE")
    private String inuse;
    @JSONField(name = "NICK_NAME")
    private String nick;
    @JSONField(name = "PHONE")
    private String phone;
    @JSONField(name = "MOTORCADE_CODE")
    private String code;
    @JSONField(name = "ACCOUNT_C_NAME")
    private String account;
    @JSONField(name = "FK_ORG_9")
    private String fkOg9;
    @JSONField(name = "ID_CARD_NO")
    private String cardId;
    @JSONField(name = "ROW_ID")
    private String rowId;
    @JSONField(name = "CONTACT_PERSON")
    private String contacter;
    @JSONField(name = "CHI_ACCOUNT")
    private String chiAccount;

    private boolean spread;

    @Override
    public String toString() {
        return "Team{" +
                "type='" + type + '\'' +
                ", createTime='" + createTime + '\'' +
                ", typeName='" + typeName + '\'' +
                ", staffId='" + staffId + '\'' +
                ", id='" + id + '\'' +
                ", truckNo='" + truckNo + '\'' +
                ", fkId='" + fkId + '\'' +
                ", inuse='" + inuse + '\'' +
                ", nick='" + nick + '\'' +
                ", phone='" + phone + '\'' +
                ", code='" + code + '\'' +
                ", account='" + account + '\'' +
                ", fkOg9='" + fkOg9 + '\'' +
                ", cardId='" + cardId + '\'' +
                ", rowId='" + rowId + '\'' +
                ", contacter='" + contacter + '\'' +
                ", chiAccount='" + chiAccount + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }

    public String getFkId() {
        return fkId;
    }

    public void setFkId(String fkId) {
        this.fkId = fkId;
    }

    public String getInuse() {
        return inuse;
    }

    public void setInuse(String inuse) {
        this.inuse = inuse;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getFkOg9() {
        return fkOg9;
    }

    public void setFkOg9(String fkOg9) {
        this.fkOg9 = fkOg9;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getContacter() {
        return contacter;
    }

    public void setContacter(String contacter) {
        this.contacter = contacter;
    }

    public String getChiAccount() {
        return chiAccount;
    }

    public void setChiAccount(String chiAccount) {
        this.chiAccount = chiAccount;
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }
}
