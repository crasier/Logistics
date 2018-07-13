package com.inspur.eport.logistics;

public class Constants {

    public static boolean DEBUG = true;

    public static final int KEY_TOKEN = 0x0220;

    public static final int CODE_ACTIVITY_REGISTER = 10101;
    public static final int CODE_ACTIVITY_RETRIEVE = 10102;

    public static final String KEY_PREFER_USER = "login_user";
    public static final String KEY_PREFER_PWD = "login_pwd";
    public static final String KEY_PREFER_TOKEN = "prefer_token";

    public static final String URL_LOGIN = "http://test.sditds.gov.cn:81/logistics/";
    public static final String URL_LOGIN_CAS = "https://test.sditds.gov.cn:5565/cas/login?service=http://test.sditds.gov.cn:81/logistics/security/login&theme=sso_cargo&cert_alias=%s&encrypt_info=%s";
}
