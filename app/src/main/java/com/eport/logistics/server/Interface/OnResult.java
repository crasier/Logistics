package com.eport.logistics.server.Interface;

import com.eport.logistics.server.Entity.Error;

public interface OnResult<T> {
    public void onSuccess(T t);
    public void onFailed(Error error);
}
