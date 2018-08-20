package com.sdeport.logistics.driver.bean;

import io.reactivex.Observer;
import okhttp3.RequestBody;

public class EventBean {

    public static final int TAG_SESSION_INVALID = 1;//登录使用的session已经失效，需要重新登录

    private int tag;
    private String url;
    private Observer observer;
    private RequestBody body[];

    public EventBean(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Observer getObserver() {
        return observer;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    public RequestBody[] getBody() {
        return body;
    }

    public void setBody(RequestBody[] body) {
        this.body = body;
    }
}
