package com.zhaidou.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

	public static String getMD5Encoding(String s) {
		byte[] input = s.getBytes();
		String output = null;
		char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input);
			byte[] tmp = md.digest();
			char[] str = new char[32];
			byte b = 0;
			for (int i = 0; i < 16; i++) {
				b = tmp[i];
				str[2 * i] = hexChar[b >>> 4 & 0xf];
				str[2 * i + 1] = hexChar[b & 0xf];
			}
			output = new String(str);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * MD5-Hex加密
	 * 
	 * @param origin
	 * @return
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(origin.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	}

	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param bytes[]
	 *            字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte bytes[]) {
		// int pos = 0;
		// char[] c = new char[b.length * 2];
		// for (int i = 0; i < b.length; i++) {
		// c[pos++] = hexDigits[(b[i] >> 4) & 0x0F];
		// c[pos++] = hexDigits[b[i] & 0x0f];
		// }
		// return new String(c);

		StringBuffer buf = new StringBuffer(bytes.length * 2);
		int i;

		for (i = 0; i < bytes.length; i++) {
			if (((int) bytes[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) bytes[i] & 0xff, 16));
		}
		return buf.toString();

	}
}
