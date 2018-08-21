package com.sdeport.logistics.driver.server;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.driver.bean.EventBean;
import com.sdeport.logistics.driver.server.retrofit.ApiManager;
import com.sdeport.logistics.driver.server.retrofit.TokenLoader;
import com.sdeport.logistics.driver.server.retrofit.Urls;


import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class WebRequest {

    private static final String TAG = "WebRequest";

    private static WebRequest instance;

    public enum RType {
        JSONObject,
        JSONArray,
        String,
        Boolean,
        NoBody,
        Void,
        Body
    }

    public enum RMethod {
        GET,
        POST
    }

    public static WebRequest getInstance() {
        if (instance == null) {
            synchronized (WebRequest.class) {
                if (instance == null) {
                    instance = new WebRequest();
                }
            }
        }

        return instance;
    }

    private List<Observable> observables = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public void withToken(final String url, final Observer observer, final RType type, final RMethod method, final RequestBody... body) {
        final Observable<?> observable = Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                String token = TokenLoader.getInstance().getCacheToken();
                Log.d(TAG, "111111111   token = "+token);
                if (TextUtils.isEmpty(token)) {
                    try {
                        return (ObservableSource<String>) reLogin(url, observer, type, method, body);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return Observable.just(token);
            }
        }).flatMap(new Function<String, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(String s) {
                Log.d(TAG, "222222222   "+s);
                switch (type) {
                    case JSONObject:
                        if (method == RMethod.GET)
                            return ApiManager.getInstance().getRetrofitService().getObj(formatToken(s), url);
                        else
                            return ApiManager.getInstance().getRetrofitService().postObj(formatToken(s), url, body == null ? null : body[0]);
                    case JSONArray:
                        if (method == RMethod.GET)
                            return ApiManager.getInstance().getRetrofitService().getArr(formatToken(s), url);
                        else
                            return ApiManager.getInstance().getRetrofitService().postArr(formatToken(s), url, body == null ? null : body[0]);
                    case String:
                        if (method == RMethod.GET)
                            return ApiManager.getInstance().getRetrofitService().getStr(formatToken(s), url);
                        else
                            return ApiManager.getInstance().getRetrofitService().postStr(formatToken(s), url, body == null ? null : body[0]);
                    case Void:
                        if (method == RMethod.GET)
                            return ApiManager.getInstance().getRetrofitService().getVoid(formatToken(s), url);
                        else
                            return ApiManager.getInstance().getRetrofitService().postVoid(formatToken(s), url, body == null ? null : body[0]);
                    default:
                        if (method == RMethod.GET)
                            return ApiManager.getInstance().getRetrofitService().getBody(formatToken(s), url);
                        else
                            return ApiManager.getInstance().getRetrofitService().postBody(formatToken(s), url, body == null ? null : body[0]);
                }
            }
        }).flatMap(new Function<Object, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Object object) throws Exception {
                try {
                    JSONObject jo = JSON.parseObject(String.valueOf(object));
                    if (jo.getIntValue("code") == 401) {
                        return reLogin(url, observer, type, method, body);
                    }
                }catch (Exception e) {

                }

                observables.clear();
                return Observable.just(object);
            }
        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {

            private int mRetryCount = 0;
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        Log.d(TAG, "44444444   "+throwable);

                        if (mRetryCount > 1) {
                            return Observable.error(new Throwable("重登陆失败"));
                        }else if (throwable != null && throwable.getMessage().equals("401")) {
                            mRetryCount++;
                            return TokenLoader.getInstance().getNetTokenLocked();
                        }else {
                            return Observable.error(throwable);
                        }
                    }
                });
            }
        });

        observables.add(observable);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Observable<?> reLogin(String url, Observer<?> observer, RType type, RMethod method, RequestBody... body) {
        for (Observable ob : observables) {
            ob.unsubscribeOn(Schedulers.io());
        }
        observables.clear();
        EventBean bean = new EventBean(EventBean.TAG_SESSION_INVALID);
        bean.setUrl(url);
        bean.setObserver(observer);
        bean.setType(type);
        bean.setMethod(method);
        bean.setBody(body);
        EventBus.getDefault().post(bean);
        return Observable.error(new Throwable("登录过期，正在重新登录..."));
    }

    /**
     * 测试用登录
     * */
    public void login(String name, String pwd, String token, InputStream pfxInputStream, Observer observer) {
//        String url = Urls.URL_LOGIN;
//        ApiManager.getInstance().getRetrofitService().login(url, name, pwd)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer);


        ApiManager.getInstance().getRetrofitService().login("https://test.sditds.gov.cn:5565/cas/login?service=http://test.sditds.gov.cn:81/cargo/security/login&theme=sso_cargo&cert_alias=sso&encrypt_info=kmtPNcJqdsMUtBevloOLU6LsjR8l7h8T3TjcQJxOCqAm49HYy2pEX3KilIrv7EoGov047u3j5cVMJhgvCD1RxSGlQRHcuxzN3bnfm6bmfoZe%2BNtY1q0r%2B77Us2M7jW0P4497IGqzon%2B2UNgqxdwS%2BQR%2FfAviK7Sz8bUWcf9m%2BgwzSjLOG9Fu7MKuImxQrmkndSz2vqrjoVlYtzE5VZE43f%2BdRxliV3sLItmPLeOof%2FEEliWuJAGAwvgRX0YhszcsPaLpsC0cqtfGhhJTTV%2Bd%2FUHNpFSixaF1vR3TAwBhovQzYneIe50uBx3P%2FjLC6vwrzHG6yPHSzeoZ%2FH7uY%2B6X3Q%3D%3D%2BNtY1q0r%2B77Us2M7jW0P4497IGqzon%2B2UNgqxdwS%2BQR%2FfAviK7Sz8bUWcf9m%2BgwzSjLOG9Fu7MKuImxQrmkndSz2vqrjoVlYtzE5VZE43f%2BdRxliV3sLItmPLeOof%2FEEliWuJAGAwvgRX0YhszcsPaLpsC0cqtfGhhJTTV%2Bd%2FUHNpFSixaF1vR3TAwBhovQzYneIe50uBx3P%2FjLC6vwrzHG6yPHSzeoZ%2FH7uY%2B6X3Q%3D%3D")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private String formatToken(String token) {
//        return String.format(Locale.CHINA, "sdeport.session.id=%s", token);
        return token;
    }

    /**
     * 获取运输委托单列表信息
     * */
    public void getOrderList(int pageNum, int pageSize, String billNo, String forwardName,
                             String consigneeCName, String delivTimeStart, String delivTimeEnd,
                             String flowStatus, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/forwarder/listMotoForOrdData?" +
                        "pageNum=%s" +
                        "&pageSize=%s" +
                        "&billNo=%s" +
                        "&forwarderName=%s" +
                        "&consigneeCName=%s" +
                        "&delivTimeStart=%s" +
                        "&delivTimeEnd=%s" +
                        "&flowStatus=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                pageNum,
                pageSize,
                billNo,
                forwardName,
                consigneeCName,
                delivTimeStart,
                delivTimeEnd,
                flowStatus,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取订单详细信息
     * */
    public void getOrderDetail(String id, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/forwarder/motorcade/detail?" +
                        "forwardingId=%s",
                Urls.URL_SERVER_BASE,
                id);

        withToken(url, observer, RType.String, RMethod.GET);
    }

    /**
     * 拒绝/接受订单,5510 拒绝, 5520 接受, 5530 撤销
     * */
    public void modifyOrderStatus(String id, String status, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/forwarder/changeMotoForOrdStatus?" +
                        "id=%s" +
                        "&flowStatus=%s",
                Urls.URL_SERVER_BASE,
                id,
                status);

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 提箱派车列表获取
     */
    public void getOrderDispatchList(int pageNum, int pageSize, String billNo, String delivPlaceName,
                              String rtnPlaceName, String flowStatus, String consigneeCName,
                              String forwarderName, String fkForwardingId, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/getDetailsList?" +
                        "pageNum=%s" +
                        "&pageSize=%s" +
                        "&billNo=%s" +
                        "&delivPlaceName=%s" +
                        "&rtnPlaceName=%s" +
                        "&flowStatus=%s" +
                        "&consigneeCName=%s" +
                        "&forwarderName=%s" +
                        "&fkForwardingId=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                pageNum,
                pageSize,
                billNo,
                delivPlaceName,
                rtnPlaceName,
                flowStatus,
                consigneeCName,
                forwarderName,
                fkForwardingId,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取司机列表
     * */
    public void getDriverList(Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/getDriverList",
                Urls.URL_SERVER_BASE);

        withToken(url, observer, RType.JSONArray, RMethod.GET);
    }

    /**
     * 获取卡车列表
     * */
    public void getTruckList(Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/getTruckList",
                Urls.URL_SERVER_BASE);

        withToken(url, observer, RType.JSONArray, RMethod.GET);
    }

    /**
     * 获取派车单列表信息
     */
    public void getDispatchList(int pageNum, int pageSize, String billNo,
                                String forwardName, String consigneeCName, String delivTimeStart,
                                String delivTimeEnd, String flowStatus, String oriBack, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/listData?" +
                        "pageNum=%s" +
                        "&pageSize=%s" +
                        "&billNo=%s" +
                        "&forwarderName=%s" +
                        "&consigneeCName=%s" +
                        "&delivTimeStart=%s" +
                        "&delivTimeEnd=%s" +
                        "&flowStatus=%s" +
                        "&oriBack=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                pageNum,
                pageSize,
                billNo,
                forwardName,
                consigneeCName,
                delivTimeStart,
                delivTimeEnd,
                flowStatus,
                oriBack,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取车队信息列表
     * */
    public void getTeamList(int pageNum, int pageSize, String contact,
                            String motorcadeCode, String motorcadeName, String chiAccount,
                            String accountName, String cardId, String nick,
                            Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/listData?" +
                        "pageNum=%s" +
                        "&pageSize=%s" +
                        "&motorcadeName=%s" +
                        "&motorcadeCode=%s" +
                        "&contactPerson=%s" +
                        "&chiAccount=%s" +
                        "&accountCName=%s" +
                        "&idCardNo=%s" +
                        "&nickName=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                pageNum,
                pageSize,
                contact,
                motorcadeCode,
                motorcadeName,
                chiAccount,
                accountName,
                cardId,
                nick,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 修改车队账号启用状态
     * @param status 状态：0 停用，1 启用
     * */
    public void modifyTeamUsable(String motorcadeStaffId, int status, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/changeInUseStatus?" +
                        "enteMotorcadeStaffId=%s" +
                        "&inUse=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                motorcadeStaffId,
                status,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取车辆信息列表
     * */
    public void getTruckList(int pageNum, int pageSize, String truckNo, String truckType,
                             String drivingLicNo, String carryCap, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/truck/listData?" +
                        "pageNumber=%s" +
                        "&pageSize=%s" +
                        "&truckNo=%s" +
                        "&truckType=%s" +
                        "&drivingLicNo=%s" +
                        "&carryCap=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                pageNum,
                pageSize,
                truckNo,
                truckType,
                drivingLicNo,
                carryCap,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 修改司机信息
     *
     * @http method get
     * */
    public void modifyDriverInfo(String id, String fkMotorcadeId, String chiAccount, String chiPassword,
                                 String accountCName, String cardNo, String phone, String nick, String truckNo,
                                 Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/staff/save?" +
                        "id=%s" +
                        "&fkMotorcadeId=%s" +
                        (TextUtils.isEmpty(chiAccount) ? "%s" : "&chiAccount=%s") +
                        (TextUtils.isEmpty(chiPassword) ? "%s" : "&chiPassword=%s") +
                        "&accountCName=%s" +
                        "&idCardNo=%s" +
                        "&phone=%s" +
                        "&nickName=%s" +
                        "&truckNo=%s",
                Urls.URL_SERVER_BASE,
                id,
                fkMotorcadeId,
                (TextUtils.isEmpty(chiAccount) ? "" : chiAccount),
                (TextUtils.isEmpty(chiPassword) ? "" : chiPassword),
                accountCName,
                cardNo,
                phone,
                nick,
                truckNo
        );
        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 物流状态跟踪
     * */
    public void getStatusRecord(String billNo, Observer<?> observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/logisticsState/getStatusRecord?" +
                        "billNo=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                billNo,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }
}
