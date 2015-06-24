package com.zhaidou.utils;

import android.os.Environment;

public class SDcardUtils {
	public static final String APP_NAME = "zhaidou";
	/** SD卡路径 */
	public final static String SDCARD_PATH = format(Environment
			.getExternalStorageDirectory().getAbsolutePath(), true);
	/** 客户端本地存放路径 */
	public static final String CLIENT_PATH = format(SDCARD_PATH + APP_NAME,
			true);
	/**图片缓存*/
	public static final String CACHE_IAMGE = format(CLIENT_PATH + "cacheimage",
			true);
	/** 小图片缓存 */
	public static final String CACHE_SMALL_IMAGE = format(CLIENT_PATH
			+ "cachesmallimage", true);
	/** 图片下载保存路径 */
	public static final String IMAGE_SAVE_PATH = format(Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
			.getAbsolutePath(), true)
			+ APP_NAME + "/";

	/**
	 * @description 格式化路径，添加或删除"/"
	 * @param path
	 * @param needFolder
	 *            true表示需要一个文件夹，false表示需要一个文件
	 * @return
	 */
	public static String format(String path, boolean needFolder) {
		StringBuilder sb = new StringBuilder(path);
		if (needFolder) {
			if (!path.endsWith("/")) {
				sb.append("/");
			}
		} else {
			if (path.endsWith("/")) {
				sb.deleteCharAt(path.length() - 1);
			}
		}
		return sb.toString();
	}

	/**
	 * @description 检查SD卡是否存在
	 * @return
	 */
	public static boolean checkSdCardExists() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
			return true;

		return false;
	}
}
