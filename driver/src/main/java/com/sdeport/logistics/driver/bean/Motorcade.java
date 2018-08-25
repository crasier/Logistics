package com.sdeport.logistics.driver.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 车队信息
 * */
public class Motorcade {

    @JSONField(name = "MOTORCADE_NAME")
    private String name;
    @JSONField(name = "ID")
    private String id;
    @JSONField(name = "CONTACT_PERSON")
    private String person;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return "Motorcade{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", person='" + person + '\'' +
                '}';
    }
}
