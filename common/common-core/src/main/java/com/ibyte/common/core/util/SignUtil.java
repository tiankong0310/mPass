package com.ibyte.common.core.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 签名工具类
 *
 * @author li.Shangzhi
 * @Date: 2019年10月25日 01:04:42
 */
public class SignUtil {
	
	/**
	 * 使用 HMAC-SHA1 签名方法对data进行签名
	 * @param data  被签名的字符串
	 * @param key  密钥
	 * @return 加密后的字符串
	 */
	public static String getHMAC(String data, String key) {
		final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
		byte[] result = null;
		try {
			byte[] bytekey = key.getBytes(StandardCharsets.UTF_8);
			// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
			SecretKeySpec signinKey = new SecretKeySpec(bytekey,HMAC_SHA1_ALGORITHM);
			// 生成一个指定 Mac 算法 的 Mac 对象
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			// 用给定密钥初始化 Mac 对象
			mac.init(signinKey);
			// 完成 Mac 操作
			byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			result = Base64.encodeBase64(rawHmac);

		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.err.println(e.getMessage());
		}

		if (null != result) {
			return new String(result);
		} else {
			return null;
		}
	}

}