package com.eport.logistics.utils;

import android.util.Log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class EncryptUtil {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	private static final String KEY_ALGORITHM = "RSA";

	public static boolean flagShowCertificateInfo = false;

	private static PrivateKey privateKey = null;
	private static PublicKey publicKey = null;
	private static String alias = null;//别名，证书id

	public static byte[] digestSHA256(byte[] data) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(data);
		return md.digest();
	}
	

	/**
	 * 私钥加密
	 * 
	 * @param data 待加密数据
//	 * @param publicKey 公钥
	 * @return byte[] 加密数据
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// 对数据加密
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}

	public synchronized static void getCertificateInfo(InputStream pfxInputStream, String strPassword) {
		try {
			// 实例化KeyStore对象
			KeyStore ks = KeyStore.getInstance("PKCS12");
//			FileInputStream fis = new FileInputStream(strPfxPath);
			// If the keystore password is empty(""), then we have to set
			// to null, otherwise it won't work!!!
			char[] nPassword = null;
			if ((strPassword == null) || strPassword.trim().equals("")) {
				nPassword = null;
			} else {
				nPassword = strPassword.toCharArray();
			}
			// 加载密钥库,使用密码"password"
			ks.load(pfxInputStream, nPassword);
            pfxInputStream.close();
			// System.out.println("keystore type=" + ks.getType());
			// Now we loop all the aliases, we need the alias to get keys.
			// It seems that this value is the "Friendly name" field in the
			// detals tab <-- Certificate window <-- view <-- Certificate
			// Button <-- Content tab <-- Internet Options <-- Tools menu
			// In MS IE 6.
			// 列出此密钥库的所有别名
			Enumeration<String> enumas = ks.aliases();
			String keyAlias = null;
			if (enumas.hasMoreElements())// we are readin just one certificate.
			{
				keyAlias = (String) enumas.nextElement();
			}
		
			// 获得别名为xxx所对应的私钥
			PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
			// 返回与给定别名关联的证书
			Certificate cert = ks.getCertificate(keyAlias);
			PublicKey pubkey = cert.getPublicKey();
			privateKey = prikey;
			publicKey = pubkey;
			alias = keyAlias;
		} catch (Exception e) {
			System.out.println("根据路径和密码获取证书信息出错,pfxPath:[]");
			e.printStackTrace();
		}
	}
	
	public static String getEncryptInfo(String sso_username,String sso_password,String current_datetime,InputStream pfxInputStream,String certPassword){
		EncryptUtil.flagShowCertificateInfo = false;
		String ssoInfo="";
		if(!StringUtils.isBlank(sso_username)&&!StringUtils.isBlank(sso_password)){
			if(StringUtils.isBlank(current_datetime)){
		    Date sysdate=new Date();
		    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
		    current_datetime=simpleDateFormat.format(sysdate);
		}
		ssoInfo ="&sso_username="+sso_username+"&sso_password="+sso_password+"&current_datetime="+current_datetime;
            Log.e("getEncryptInfo", "before encrypt: ssoInfo = "+ssoInfo);
		EncryptUtil.getCertificateInfo(pfxInputStream, certPassword);
		byte[] data = ssoInfo.getBytes();
		try {
			byte[] encBytes = EncryptUtil.encryptByPrivateKey(data, EncryptUtil.privateKey);
//			String strEncBase64 = Base64.encodeBase64String(encBytes);
			String strEncBase64 = android.util.Base64.encodeToString(encBytes, android.util.Base64.DEFAULT);
			ssoInfo= URLEncoder.encode(strEncBase64, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	  return ssoInfo;
	}
}
