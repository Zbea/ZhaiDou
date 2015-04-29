package com.zhaidou.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HtmlFetcher {
	public static String fetch(URL url) throws IOException {
		URLConnection connect = url.openConnection();
		DataInputStream dis = new DataInputStream(connect.getInputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(dis, "UTF-8"));// Ŀ��ҳ�����ΪUTF-8
		String html = "";
		String readLine = null;
		while ((readLine = in.readLine()) != null) {
			html = html + readLine;
		}
		in.close();
		return html;
	}
	
	public static String fetch(String url) throws MalformedURLException, IOException {
		return fetch(new URL(url));
	}
}
