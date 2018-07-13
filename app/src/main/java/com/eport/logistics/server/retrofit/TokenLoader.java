package com.eport.logistics.server.retrofit;

import com.alibaba.fastjson.JSONObject;
import com.eport.logistics.bean.User;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class TokenLoader {

    private static final String TAG = "TokenLoader";
    private PublishSubject<JSONObject> mPublishSubject;
    private AtomicBoolean mRefreshing = new AtomicBoolean(false);
    private Observable<JSONObject> mTokenObervable;

    private static class LazyHolder {
        public static final TokenLoader INSTANCE = new TokenLoader();
    }

    private TokenLoader() {
        mPublishSubject = PublishSubject.create();
        mTokenObervable = ApiManager.getInstance().getRetrofitService().login(Urls.URL_LOGIN, User.getUser().getAccount(), User.getUser().getPassword())
            .doOnNext(new Consumer<JSONObject>() {
                @Override
                public void accept(JSONObject object) throws Exception {
                    if (object != null) {
                        User.getUser().setToken(object.getString("token"));
                    }
                    mRefreshing.set(false);
                }
            }).doOnError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    mRefreshing.set(false);
                }
            }).subscribeOn(Schedulers.io());
    }

    public static TokenLoader getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getCacheToken() {
        return User.getUser().getToken();
    }

    public Observable<JSONObject> getNetTokenLocked() {
        if (mRefreshing.compareAndSet(false, true)) {
            startTokenRequest();
        }else {

        }

        return mPublishSubject;
    }

    private void startTokenRequest() {
        mTokenObervable.subscribe(mPublishSubject);
    }
}
