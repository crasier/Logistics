package com.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 物流状态
 * */
public class TransStatus implements Serializable {
    @JSONField(name = "C_ORG_NAME")
    private String cOrgName;
    @JSONField(name = "DATASOURCE")
    private String source;
    @JSONField(name = "FLOW_DATE")
    private Long date;
    @JSONField(name = "FLOW_STATUS")
    private String status;
    @JSONField(name = "R_ORG_NAME")
    private String pOrgName;

    @Override
    public String toString() {
        return "TransStatus{" +
                "cOrgName='" + cOrgName + '\'' +
                ", source='" + source + '\'' +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                ", pOrgName='" + pOrgName + '\'' +
                '}';
    }

    public String getcOrgName() {
        return cOrgName;
    }

    public void setcOrgName(String cOrgName) {
        this.cOrgName = cOrgName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getpOrgName() {
        return pOrgName;
    }

    public void setpOrgName(String pOrgName) {
        this.pOrgName = pOrgName;
    }
}
