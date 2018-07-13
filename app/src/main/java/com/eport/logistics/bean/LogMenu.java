package com.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Locale;

public class LogMenu implements Serializable{
    @JSONField(name = "createTime")
    private String createTime;
    @JSONField(name = "id")
    private String menuTag;
    @JSONField(name = "indx")
    private String index;
    @JSONField(name = "isEnabled")
    private String isEnabled;
    @JSONField(name = "menuName")
    private String menuName;
    @JSONField(name = "pid")
    private String pid;
    @JSONField(name = "url")
    private String url;

    private boolean isParent;//标记是否是一级菜单（父菜单）

    @Override
    public String toString() {
        return String.format(Locale.CHINA,
                "createTime:%s,menuTag:%s,index:%s,isEnabled:%s,menuName:%s,pid:%s,url:%s,isParent:%s",
                createTime,menuTag,index,isEnabled,menuName,pid,url,String.valueOf(isParent));
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getMenuTag() {
        return menuTag;
    }

    public void setMenuTag(String menuTag) {
        this.menuTag = menuTag;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }
}
