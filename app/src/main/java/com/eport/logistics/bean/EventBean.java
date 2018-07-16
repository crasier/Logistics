package com.eport.logistics.bean;

import com.eport.logistics.server.WebRequest;

import io.reactivex.Observer;
import okhttp3.RequestBody;

public class EventBean {

    public static final int TAG_SESSION_INVALID = 1;//登录使用的session已经失效，需要重新登录

    private int tag;
    private String url;
    private Observer observer;
    private WebRequest.RType type;
    private WebRequest.RMethod method;
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

    public WebRequest.RType getType() {
        return type;
    }

    public void setType(WebRequest.RType type) {
        this.type = type;
    }

    public WebRequest.RMethod getMethod() {
        return method;
    }

    public void setMethod(WebRequest.RMethod method) {
        this.method = method;
    }

    public RequestBody[] getBody() {
        return body;
    }

    public void setBody(RequestBody[] body) {
        this.body = body;
    }
}
