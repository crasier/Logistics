package com.inspur.eport.logistics.server.retrofit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RetrofitService {

    /**
     * 登录
     * */
//    @GET("account/login")
//    public Observable<JSONObject> login(@Query("name") String name, @Query("pwd") String password);

    @GET
    public Observable<String> login(@Url String url);

//    @FormUrlEncoded
    @POST
    @Headers({"Content-Type: application/json","Accept: application/json"})
    /**
     * 请求的返回结果是jsonobject
     * */
    public Observable<JSONObject> requestObj(@Header("Cookie") String token, @Url String url, @Body RequestBody body);

//    @FormUrlEncoded
    @POST
    @Headers({"Content-Type: application/json","Accept: application/json"})
    /**
     * 请求的返回结果是jsonarray
     * */
    public Observable<JSONArray> requestArr(@Header("Cookie") String token, @Url String url, @Body RequestBody body);


//    @FormUrlEncoded
    @POST
    @Headers({"Content-Type: application/json","Accept: application/json"})
    /**
     * 请求的返回结果是String
     * */
    public Observable<String> requestStr(@Header("Cookie") String token, @Url String url, @Body RequestBody body);
//    @FormUrlEncoded
    @POST
    @Headers({"Content-Type: application/json","Accept: application/json"})
    /**
     * 请求的返回结果是String
     * */
    public Observable<Boolean> requestBool(@Header("Cookie") String token, @Url String url, @Body RequestBody body);

    @POST
    @Headers({"Content-Type: application/json","Accept: application/json"})
    /**
     * 请求的返回结果没有响应body，只有header
     * */
    public Observable<Response<Void>> requestNoBody(@Header("Cookie") String token, @Url String url, @Body RequestBody body);



    /**
     * 测试用登录
     * */
    @GET()
    public Observable<JSONObject> login(@Url String url, @Query("name") String name, @Query("pwd") String pwd);

    @GET
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    public Observable<JSONArray> getArr(@Header("Cookie") String token, @Url String url);

    @GET()
    @Headers({"Content-Type: application/json","Accept: application/json"})
    public Observable<JSONObject> getObj(@Header("Cookie") String token, @Url String url);

    @GET
    @Headers({"Content-Type:text/plain;charset=UTF-8","Accept:text/plain"})
    public Observable<String> getStr(@Header("Cookie") String token, @Url String url);

    @GET
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    public Observable<Void> getVoid(@Header("Cookie") String token, @Url String url);

    @GET
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    public Observable<ResponseBody> getBody(@Header("Cookie") String token, @Url String url);

    @POST
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    public Observable<JSONArray> postArr(@Header("Cookie") String token, @Url String url, @Body RequestBody body);


    @POST
    @Headers({"Content-Type: application/json","Accept: application/json"})
    public Observable<JSONObject> postObj(@Header("Cookie") String token, @Url String url, @Body RequestBody body);

    @POST
    @Headers({"Content-Type:text/plain;charset=UTF-8","Accept:text/plain"})
    public Observable<String> postStr(@Header("Cookie") String token, @Url String url, @Body RequestBody body);

    @POST
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    public Observable<Void> postVoid(@Header("Cookie") String token, @Url String url, @Body RequestBody body);

    @POST
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    public Observable<ResponseBody> postBody(@Header("Cookie") String token, @Url String url, @Body RequestBody body);
}
