package com.eport.logistics.bean;

public class EventBean {

    public static final int TAG_SESSION_INVALID = 1;//登录使用的session已经失效，需要重新登录

    private int tag;


    public EventBean(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
