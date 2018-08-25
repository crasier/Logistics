package com.sdeport.logistics.driver.server;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.driver.bean.EventBean;
import com.sdeport.logistics.driver.constant.Urls;
import com.sdeport.logistics.driver.server.retrofit.ApiManager;
import com.sdeport.logistics.driver.server.retrofit.TokenLoader;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
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

    private String formatToken(String token) {
//        return String.format(Locale.CHINA, "sdeport.session.id=%s", token);
        return token;
    }

    /**
     * 获取验证码
     * @param type 1 注册， 2 重置密码
     * */
    public void getValidateCode(String phone, String type, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/getSmsCode?" +
                        "phoneNumber=%s" +
                        "&reFlag=%s",
                Urls.URL_SERVER_BASE,
                phone,
                type);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 注册用户
     * */
    public void register(String phone, String pwd, String code, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/register?" +
                "phoneNumber=%s" +
                "&password=%s" +
                "&smsCode=%s",
                Urls.URL_SERVER_BASE,
                phone,
                pwd,
                code);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 重置密码
     * */
    public void retrieve(String phone, String pwd, String code, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/resetPassword?" +
                        "phoneNumber=%s" +
                        "&password=%s" +
                        "&smsCode=%s",
                Urls.URL_SERVER_BASE,
                phone,
                pwd,
                code);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 修改密码
     * */
    public void resetPassword(String phone, String pwdOld, String pwdNew, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/modifyPassword?" +
                        "phoneNumber=%s" +
                        "&oldPassword=%s" +
                        "&newPassword=%s",
                Urls.URL_SERVER_BASE,
                phone,
                pwdOld,
                pwdNew);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }


    /**
     * 获取用户基本信息
     * */
    public void getRoleInfo(String acc, Observer observer) {
        Observable.create(new ObservableOnSubscribe<JSONObject>() {
            @Override
            public void subscribe(ObservableEmitter<JSONObject> emitter) throws Exception {
                emitter.onNext(JSON.parseObject("{\n" +
                        "\t\"success\": \"true\",\n" +
                        "\t\"failReason\": \"\",\n" +
                        "\t\"data\": \"{\\\"userName\\\":\\\"cargo\\\",\\\"cnName\\\": \\\"张三\\\",\\\"phoneNumber\\\":\\\"15012341234\\\",\\\"roleInfo\\\": \\\"11111||22222||33333||44444||55555\\\",\\\"driverInfo\\\": \\\"{\\\\\\\"nickName\\\\\\\":\\\\\\\"小张\\\\\\\",\\\\\\\"driveLicenseType\\\\\\\":\\\\\\\"B2\\\\\\\",\\\\\\\"driveLicenseNo\\\\\\\":\\\\\\\"6351263416\\\\\\\",\\\\\\\"idCardNo\\\\\\\":\\\\\\\"3713241231413\\\\\\\",\\\\\\\"motorcadeName\\\\\\\":\\\\\\\"第一车队\\\\\\\",\\\\\\\"contactPerson\\\\\\\":\\\\\\\"李四\\\\\\\",\\\\\\\"attachStatus\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"truckNo\\\\\\\":\\\\\\\"鲁A26533\\\\\\\"}\\\"}\"\n" +
                        "}"));
            }
        })
                .delay(1500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

//        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/getDriverInfo?",
//                Urls.URL_SERVER_BASE);
//        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 修改用户基本信息
     * */
    public void modifyRoleInfo(String name, String id, String license, String licenseType, String truck, Observer observer) {
        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/updateDriverInfo?" +
                        "userName=%s" +
                        "&cnName=%s" +
                        "&idCardNo=%s" +
                        "&nickName=%s" +
                        "&truckNo=%s" +
                        "&driveLicenseType=%s" +
                        "&driveLicenseNo=%s",
                Urls.URL_SERVER_BASE,
                name,
                name,
                id,
                "",
                truck,
                licenseType,
                license);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 获取车队列表
     * @param keyWord 模糊查询关键字
     * */
    public Observable getMotorcadeList(String keyWord) {

        if (true) {
            return Observable.create(new ObservableOnSubscribe() {
                @Override
                public void subscribe(ObservableEmitter emitter) throws Exception {
                    emitter.onNext(JSON.parseObject("{\n" +
                            "    \"success\": true,\n" +
                            "    \"failReason\": \"\",\n" +
                            "    \"data\": [\n" +
                            "        {\n" +
                            "            \"id\": \"376e83da-9a7c-4e4d-8521-00c54e0fbd79\",\n" +
                            "            \"motorcadeName\": \"主车队\",\n" +
                            "            \"contactPerson\": null\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": \"8425d366-4f23-451a-9e12-2eb1d721cb9a\",\n" +
                            "            \"motorcadeName\": \"次车队\",\n" +
                            "            \"contactPerson\": \"小明\"\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": \"40f8deab-87bc-41b4-adb6-a4f09ad98cf3\",\n" +
                            "            \"motorcadeName\": \"次车队2\",\n" +
                            "            \"contactPerson\": null\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}"));
                }
            })
                    .delay(2000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/getMotorcadeList?" +
                        "keyword=%s",
                Urls.URL_SERVER_BASE,
                keyWord);

        return ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 申请挂靠车队
     * */
    public void applyAttach(String user, String motorcadeId, Observer observer) {

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/applyAttach?" +
                        "userName=%s" +
                        "&motorcadeId=%s",
                Urls.URL_SERVER_BASE,
                user,
                motorcadeId);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 获取派车单列表
     * */
    public void getOrderList(int page, int pageSize, String status, Observer observer) {

        if (true) {
            Observable.create(new ObservableOnSubscribe<JSONObject>() {
                @Override
                public void subscribe(ObservableEmitter<JSONObject> emitter) throws Exception {
                    emitter.onNext(JSON.parseObject("{\n" +
                            "    \"success\": \"true\",\n" +
                            "    \"failReason\": \"\",\n" +
                            "    \"data\": \"{\\\"total\\\":\\\"3\\\",\\\"list\\\":[{\\\"id\\\":\\\"2b09d34f14664625bbcd3d875d24a118\\\",\\\"billNo\\\":\\\"提单号\\\",\\\"consigneeCName\\\":\\\"收货人\\\",\\\"forwarderName\\\":\\\"货代\\\",\\\"addr\\\":\\\"送货地址\\\",\\\"delivTime\\\":\\\"送货时间\\\",\\\"cntrNo\\\":\\\"箱号\\\",\\\"cntrSizeCode\\\":\\\"集装箱尺寸\\\",\\\"cntrTypeCode\\\":\\\"箱型\\\",\\\"driverName\\\":\\\"提箱司机\\\",\\\"truckNo\\\":\\\"提箱车牌号\\\",\\\"delivPlaceName\\\":\\\"提箱地点\\\",\\\"tStatus\\\":\\\"提箱状态\\\",\\\"rtnDriverName\\\":\\\"还箱司机\\\",\\\"rtnTruckNo\\\":\\\"还箱车牌号\\\",\\\"rtnPlaceName\\\":\\\"还箱地点\\\",\\\"rStatus\\\":\\\"还箱状态\\\"},{\\\"id\\\":\\\"2b09d34f14664625bbcd3d875d24a118\\\",\\\"billNo\\\":\\\"提单号\\\",\\\"consigneeCName\\\":\\\"收货人\\\",\\\"forwarderName\\\":\\\"货代\\\",\\\"addr\\\":\\\"送货地址\\\",\\\"delivTime\\\":\\\"送货时间\\\",\\\"cntrNo\\\":\\\"箱号\\\",\\\"cntrSizeCode\\\":\\\"集装箱尺寸\\\",\\\"cntrTypeCode\\\":\\\"箱型\\\",\\\"driverName\\\":\\\"提箱司机\\\",\\\"truckNo\\\":\\\"提箱车牌号\\\",\\\"delivPlaceName\\\":\\\"提箱地点\\\",\\\"tStatus\\\":\\\"提箱状态\\\",\\\"rtnDriverName\\\":\\\"还箱司机\\\",\\\"rtnTruckNo\\\":\\\"还箱车牌号\\\",\\\"rtnPlaceName\\\":\\\"还箱地点\\\",\\\"rStatus\\\":\\\"还箱状态\\\"},{\\\"id\\\":\\\"2b09d34f14664625bbcd3d875d24a118\\\",\\\"billNo\\\":\\\"提单号\\\",\\\"consigneeCName\\\":\\\"收货人\\\",\\\"forwarderName\\\":\\\"货代\\\",\\\"addr\\\":\\\"送货地址\\\",\\\"delivTime\\\":\\\"送货时间\\\",\\\"cntrNo\\\":\\\"箱号\\\",\\\"cntrSizeCode\\\":\\\"集装箱尺寸\\\",\\\"cntrTypeCode\\\":\\\"箱型\\\",\\\"driverName\\\":\\\"提箱司机\\\",\\\"truckNo\\\":\\\"提箱车牌号\\\",\\\"delivPlaceName\\\":\\\"提箱地点\\\",\\\"tStatus\\\":\\\"提箱状态\\\",\\\"rtnDriverName\\\":\\\"还箱司机\\\",\\\"rtnTruckNo\\\":\\\"还箱车牌号\\\",\\\"rtnPlaceName\\\":\\\"还箱地点\\\",\\\"rStatus\\\":\\\"还箱状态\\\"}]}\"\n" +
                            "}"));
                }
            })
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, "refreshList: doOnError = "+throwable);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);

            return;
        }

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/applyAttach?" +
                        "userName=%s" +
                        "&motorcadeId=%s",
                Urls.URL_SERVER_BASE);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取派车单列表
     * */
    public void getOrderDetail(String id, Observer observer) {

        if (true) {
            Observable.create(new ObservableOnSubscribe<JSONObject>() {
                @Override
                public void subscribe(ObservableEmitter<JSONObject> emitter) throws Exception {
                    emitter.onNext(JSON.parseObject("{\n" +
                            "\t\"success\": \"true\",\n" +
                            "\t\"failReason\": \"\",\n" +
                            "\t\"data\": \"{\\\"ID\\\":\\\"2b09d34f14664625bbcd3d875d24a118\\\",\\\"BILL_NO\\\":\\\"提单号\\\",\\\"CONSIGNEE_C_NAME\\\":\\\"收货人\\\",\\\"FORWARDER_NAME\\\":\\\"货代\\\",\\\"ADDR\\\":\\\"送货地址\\\",\\\"DELIV_TIME\\\":\\\"送货时间\\\",\\\"CNTR_NO\\\":\\\"箱号\\\",\\\"CNTR_SIZE_CODE\\\":\\\"集装箱尺寸\\\",\\\"CNTR_TYPE_CODE\\\":\\\"箱型\\\",\\\"DRIVER_NAME\\\":\\\"提箱司机\\\",\\\"TRUCK_NO\\\":\\\"提箱车牌号\\\",\\\"DELIV_PLACE_NAME\\\":\\\"提箱地点\\\",\\\"T_STATUS\\\":\\\"提箱状态\\\",\\\"RTN_DRIVER_NAME\\\":\\\"还箱司机\\\",\\\"RTN_TRUCK_NO\\\":\\\"还箱车牌号\\\",\\\"RTN_PLACE_NAME\\\":\\\"还箱地点\\\",\\\"R_STATUS\\\":\\\"还箱状态\\\"}\"\n" +
                            "}"));
                }
            })
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, "refreshList: doOnError = "+throwable);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);

            return;
        }

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/applyAttach?" +
                        "userName=%s" +
                        "&motorcadeId=%s",
                        Urls.URL_SERVER_BASE);

        ApiManager.getInstance().getRetrofitService().getObj("", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 接单
     * @param id 派车单ID
     * @param type T-提箱状态，R-还箱状态，A-同时
     * */
    public void acceptOrder(String id, String type, Observer observer) {

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/accept?" +
                        "dispatchingId=%s" +
                        "&type=%s",
                        Urls.URL_SERVER_BASE,
                        id,
                        type);
        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }

    /**
     * 拒绝、撤销
     * @param id 派车单ID
     * @param type T-提箱状态，R-还箱状态，A -同时
     * */
    public void RefuseOrCancelOrder(String id, String type, Observer observer) {

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/reject?" +
                        "dispatchingId=%s" +
                        "&type=%s",
                Urls.URL_SERVER_BASE,
                id,
                type);
        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }


    /**
     * 确认提箱、还箱（司机端自行操作）
     * @param id 派车单ID
     * @param type T-提箱状态，R-还箱状态
     * */
    public void confirmOrder(String id, String type, Observer observer) {

        String url = String.format(Locale.CHINA, "%s/logistics/driverApp/confirm?" +
                        "dispatchingId=%s" +
                        "&type=%s",
                Urls.URL_SERVER_BASE,
                id,
                type);
        withToken(url, observer, RType.JSONObject, RMethod.GET);
    }
}
