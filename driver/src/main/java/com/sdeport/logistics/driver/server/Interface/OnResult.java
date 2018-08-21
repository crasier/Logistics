package com.sdeport.logistics.driver.server.Interface;

public interface OnResult<T> {
    public void onSuccess(T t);
    public void onFailed(Error error);
}
