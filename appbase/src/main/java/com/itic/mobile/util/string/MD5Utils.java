package com.itic.mobile.util.string;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密相关方法类.
 * <p>
 * Company: itic
 * </p>
 *
 * @author: idong
 * @date: 2015年5月8日 上午9:44:53
 * @version: V1.0
 */
public abstract class MD5Utils {
	// private salt hold by server inside
	private static final String privateSalt = "88d404ae06a980d9";

	/**
	 * 生成字符串对应的MD5值并转化32位定长HEX.
	 *
	 * @param str
	 *            需要加密的字符串
	 * @param charset
	 *            指定的字符集
	 * @return String 32位定长HEX串
	 */
	public static String md5(String str, String charset) {
		try {
			return bytes2Hex(md5(str.getBytes(charset)));
		} catch (UnsupportedEncodingException e) {
			//logger.error("MD5 ERROR", e);
		}
		return null;
	}

	/**
	 * 使用平台字符集生成MD5值并转化16位定长HEX.
	 *
	 * @param str
	 *            需要加密的字符串
	 * @return String 16位定长HEX串
	 * @throws UnsupportedEncodingException
	 */
	public static String md5_16(String str) {
		String md5_32;
		try {
			md5_32 = bytes2Hex(md5(str.getBytes(Charset.defaultCharset()
					.displayName())));
			if (TextUtils.isEmpty(md5_32)){
				return null;
			}
			return md5_32.substring(8, 24);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * 使用平台字符集生成MD5值并转化32位定长HEX.
	 *
	 * @param str
	 *            需要加密的字符串
	 * @return String 32位定长HEX串
	 */
	public static String md5(String str) {
		try {
			return bytes2Hex(md5(str.getBytes(Charset.defaultCharset()
					.displayName())));
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * 使用双盐混淆MD5加密字符串，返回32位定长HEX.
	 *
	 * @param message
	 *            需要加密的字符串
	 * @param pubSalt
	 *            公共盐，如：用户名
	 * @return String 32位定长HEX串
	 */
	public static String md5DoubleSalt(String message, String pubSalt) {
		try {
			String encryStr = message + privateSalt + pubSalt;
			return bytes2Hex(md5(encryStr.getBytes(Charset.defaultCharset()
					.displayName())));
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * 对文件进行MD5加密并返回32位定长HEX.
	 *
	 * @param file
	 *            需要加密的文件
	 * @return String 32位HEX串
	 */
	public static String fileMd5(File file) {
		if (file == null || !file.exists() || !file.isFile())
			return null;
		FileInputStream fis = null;
		byte[] buffer = new byte[1024];
		int numRead = 0;
		try {
			fis = new FileInputStream(file);
			MessageDigest md5;
			md5 = MessageDigest.getInstance("md5");
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			return bytes2Hex(md5.digest());
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} catch (NoSuchAlgorithmException e) {

		} finally {
			if (null != fis) {
				try {
					fis.close();
				} catch (IOException e) {

				}
			}
		}
		return null;
	}

	/**
	 * Java Security MessageDigest:MD5加密.
	 *
	 * @param bytes
	 *            需要加密的字节数组
	 * @return 128位MD5值字节数组
	 */
	private static byte[] md5(byte[] bytes) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(bytes);
			return md5.digest();
		} catch (NoSuchAlgorithmException ex) {
		}
		return null;
	}

	/**
	 * 将字节数组转换为16进制字符串.
	 *
	 * @param bytes
	 *            需要转换的字节数组
	 * @return String 32位HEX串
	 */
	private static String bytes2Hex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		String hex = null;
		for (int i = 0; i < bytes.length; i++) {
			hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}