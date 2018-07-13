package com.inspur.eport.logistics.server;

import android.net.LocalServerSocket;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inspur.eport.logistics.Constants;
import com.inspur.eport.logistics.bean.EventBean;
import com.inspur.eport.logistics.server.retrofit.ApiManager;
import com.inspur.eport.logistics.server.retrofit.TokenLoader;
import com.inspur.eport.logistics.server.retrofit.Urls;
import com.inspur.eport.logistics.utils.EncryptUtil;


import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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

    private void withToken(final String url, final Observer observer, final RType type, final RMethod method, final RequestBody... body) {
        final Observable<?> observable = Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                Log.e(TAG, "111111111   ");
                String token = TokenLoader.getInstance().getCacheToken();
                Log.e(TAG, "111111111   token = "+token);
                return Observable.just(token);
            }
        }).flatMap(new Function<String, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(String s) {
                Log.e(TAG, "222222222   "+s);
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
                Log.e(TAG, "333333333   "+object);

                try {
                    JSONObject jo = JSON.parseObject(String.valueOf(object));
                    if (jo.getIntValue("code") == 401) {

                        for (Observable ob : observables) {
                            ob.unsubscribeOn(Schedulers.io());
                        }
                        observables.clear();
                        EventBus.getDefault().post(new EventBean(EventBean.TAG_SESSION_INVALID));
                        return Observable.error(new Throwable("登录过期，正在重新登录..."));
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
                        Log.e(TAG, "44444444   "+throwable);

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

    /**
     * 测试用登录
     * */
    public void login(String name, String pwd, String token, InputStream pfxInputStream, Observer observer) {
//        String url = Urls.URL_LOGIN;
//        ApiManager.getInstance().getRetrofitService().login(url, name, pwd)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer);

        String ssoInfo = EncryptUtil.getEncryptInfo(
                name,
                pwd,
                null,
                pfxInputStream,
                "62236644");

        Log.e("getEncryptInfo", "after encrypt: ssoInfo = "+ssoInfo);
        String url = String.format(Locale.CHINA, Constants.URL_LOGIN_CAS,
                "sso",
                "kmtPNcJqdsMUtBevloOLU6LsjR8l7h8T3TjcQJxOCqAm49HYy2pEX3KilIrv7EoGov047u3j5cVMJhgvCD1RxSGlQRHcuxzN3bnfm6bmfoZe%2BNtY1q0r%2B77Us2M7jW0P4497IGqzon%2B2UNgqxdwS%2BQR%2FfAviK7Sz8bUWcf9m%2BgwzSjLOG9Fu7MKuImxQrmkndSz2vqrjoVlYtzE5VZE43f%2BdRxliV3sLItmPLeOof%2FEEliWuJAGAwvgRX0YhszcsPaLpsC0cqtfGhhJTTV%2Bd%2FUHNpFSixaF1vR3TAwBhovQzYneIe50uBx3P%2FjLC6vwrzHG6yPHSzeoZ%2FH7uY%2B6X3Q%3D%3D");

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
     * 获取主界面功能菜单
     * */
    public void getMenu(Observer observer) {
        String url = "http://test.sditds.gov.cn:81/logistics/menu/getMenu";
        withToken(url, observer, RType.String, RMethod.GET);
    }

    /**
     * 获取状态码和状态信息
     * */
    public void getDicts(String dictCode, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/dict/getDicts?" +
                "dictCode=%s" +
                "&_=%s",
                Urls.URL_SERVER_BASE,
                dictCode,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取运输委托单列表信息
     * */
    public void getOrderList(int pageNum, int pageSize, String billNo, String forwardName,
                             String consigneeCName, String delivTimeStart, String delivTimeEnd,
                             String flowStatus, Observer observer) {
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
    public void getOrderDetail(String id, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/forwarder/motorcade/detail?" +
                        "forwardingId=%s",
                Urls.URL_SERVER_BASE,
                id);

        withToken(url, observer, RType.String, RMethod.GET);
    }

    /**
     * 拒绝/接受订单,5510 拒绝, 5520 接受, 5530 撤销
     * */
    public void modifyOrderStatus(String id, String status, Observer observer) {
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
                              String forwarderName, String fkForwardingId, Observer observer) {
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
    public void getDriverList(Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/getDriverList",
                Urls.URL_SERVER_BASE);

        withToken(url, observer, RType.JSONArray, RMethod.GET);
    }

    /**
     * 获取卡车列表
     * */
    public void getTruckList(Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/getTruckList",
                Urls.URL_SERVER_BASE);

        withToken(url, observer, RType.JSONArray, RMethod.GET);
    }

    /**
     * 执行派车操作
     * @param fkReceiptId 多个以“,”分隔
     * @param id 多个以“,”分隔
     * @param fkReceiptId 多个以“,”分隔
     * @param delivTimeStart 多个以“,”分隔
     * @param appointTimes 多个以“,”分隔
     * @param rtnAppointTimes 多个以“,”分隔
     * @param oriBack 多个以“,”分隔
     * */

    public void dispatch(String fkForwardingId, String driverId, String truckNo, String getCtn,
                         String id, String fkReceiptId, String delivTimeStart, String appointTimes,
                         String rtnAppointTimes, String oriBack, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/forwardingOrder/dispatch/dispatchTruck?" +
                        "fkForwardingId=%s" +
                        "&driverId=%s" +
                        "&truckNo=%s" +
                        "&getCtn=%s" +
                        "&fkReceiptId=%s" +
                        "&id=%s" +
                        "&delivTimeStart=%s" +
                        "&appointTimes=%s" +
                        "&rtnAppointTimes=%s" +
                        "&oriBack=%s",
                Urls.URL_SERVER_BASE,
                fkForwardingId,
                driverId,
                truckNo,
                getCtn,
                fkReceiptId,
                id,
                delivTimeStart,
                appointTimes,
                rtnAppointTimes,
                oriBack);

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取派车单列表信息
     */
    public void getDispatchList(int pageNum, int pageSize, String billNo,
                                String forwardName, String consigneeCName, String delivTimeStart,
                                String delivTimeEnd, String flowStatus, String oriBack, Observer observer) {
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
                            Observer observer) {
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
    public void modifyTeamUsable(String motorcadeStaffId, int status, Observer observer) {
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
     * 修改车队账号信息
     * @param add true:执行添加,false:执行修改
     * */
    public void modifyTeamInfo(String staffId, String motorcadeCode, String motorcadeName, String contacter, String truckNo,
                               String chiAccount, String password, String cardId, String accountCName, String nick,
                               String phone, boolean add, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/%s?" +
                        "staffId=%s" +
                        "&motorcadeCode=%s" +
                        "&motorcadeName=%s" +
                        "&contactPerson=%s" +
                        "&truckNo=%s" +
                        "&chiAccount=%s" +
                        "&password=%s" +
                        "&idCardNo=%s" +
                        "&accountCName=%s" +
                        "&nickName=%s" +
                        "&phone=%s",
                Urls.URL_SERVER_BASE,
                add ? "saveMotorcadeAccount" : "modifyMotorcadeAccount",
                staffId,
                motorcadeCode,
                motorcadeName,
                contacter,
                truckNo,
                chiAccount,
                password,
                cardId,
                accountCName,
                nick,
                phone);

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取车辆信息列表
     * */
    public void getTruckList(int pageNum, int pageSize, String truckNo, String truckType,
                             String drivingLicNo, String carryCap, Observer observer) {
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
     * 设置车辆是否可用
     * */
    public void modifyTruckUsable(String id, String status, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/truck/changeInUseStatus?" +
                        "id=%s" +
                        "&truckStatus=%s",
                Urls.URL_SERVER_BASE,
                id,
                status);

        withToken(url, observer, RType.String, RMethod.GET);
    }

    /**
     * 修改车辆信息
     *
     * @http method post
     * */
    public void modifyTruckInfo(String id, String fkMotorcadeId, String motorcadeCode,
                                String truckNo, String drivingLicNo, String truckType,
                                String carryCap, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/truck/save?" +
                "id=%s" +
                "&fkMotorcadeId=%s" +
                "&motorcadeCode=%s" +
                "&truckNo=%s" +
                "&drivingLicNo=%s" +
                "&truckType=%s" +
                "&carryCap=%s",
                Urls.URL_SERVER_BASE,
                id,
                fkMotorcadeId,
                motorcadeCode,
                truckNo,
                drivingLicNo,
                truckType,
                carryCap
        );
        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 获取司机信息列表
     * */
    public void getDriverList(int pageNum, int pageSize,
                              String chiAccount, String accountCName, String idCardNo,
                              String phone, String nickName, String truckNo,
                              String fkMotorcadeId, String staffType, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/staff/listData?" +
                        "pageNumber=%s" +
                        "&pageSize=%s" +
                        "&chiAccount=%s" +
                        "&accountCName=%s" +
                        "&idCardNo=%s" +
                        "&phone=%s" +
                        "&nickName=%s" +
                        "&truckNo=%s" +
                        "&fkMotorcadeId=%s" +
                        "&staffType=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                pageNum,
                pageSize,
                chiAccount,
                accountCName,
                idCardNo,
                phone,
                nickName,
                truckNo,
                fkMotorcadeId,
                staffType,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 修改司机是否可用状态
     * */
    public void modifyDriverUsable(String id, String fkUserId, String status, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/motorcade/staff/changeInUseStatus?" +
                        "id=%s" +
                        "&fkUserId=%s" +
                        "&staffStatus=%s",
                Urls.URL_SERVER_BASE,
                id,
                fkUserId,
                status);

        withToken(url, observer, RType.String, RMethod.GET);
    }

    /**
     * 修改司机信息
     *
     * @http method get
     * */
    public void modifyDriverInfo(String id, String fkMotorcadeId, String chiAccount, String chiPassword,
                                 String accountCName, String cardNo, String phone, String nick, String truckNo,
                                 Observer observer) {
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
     * 获取预约
     * */

    /**
     * 物流状态跟踪
     * */
    public void getStatusRecord(String billNo, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/logisticsState/getStatusRecord?" +
                        "billNo=%s" +
                        "&_=%s",
                Urls.URL_SERVER_BASE,
                billNo,
                Calendar.getInstance().getTimeInMillis());

        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }
}
