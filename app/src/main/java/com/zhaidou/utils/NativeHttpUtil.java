package com.zhaidou.utils;

import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NativeHttpUtil {

	//设置URLConnection的连接超时时间
	private final static int CONNET_TIMEOUT = 5 * 1000;
	//设置URLConnection的读取超时时间
	private final static int READ_TIMEOUT = 5 * 1000;
	//设置请求参数的字符编码格式
	private final static String QUERY_ENCODING = "UTF-8";
	//设置返回请求结果的字符编码格式
	private final static String ENCODING = "GBK";
	
	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @return
	 * 		HTTP GET请求结果
	 */
	public static String get(String url) {
		return get(url, null);
	} 
	
	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP GET请求的QueryString封装map集合
	 * @return
	 * 		HTTP GET请求结果
	 */
	public static String get(String url, Map<String, String> params) {
		InputStream is = null;
		try {
			StringBuffer queryString = null;
			if (params != null && params.size() > 0) {
				queryString = new StringBuffer("?");
				queryString = joinParam(params, queryString);
			}
			if (queryString != null) {
				url = url + URLEncoder.encode(queryString.toString(), QUERY_ENCODING);
			}
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNET_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == HttpStatus.SC_OK) {
				is = conn.getInputStream();
				return StreamUtil.readStreamToString(is, ENCODING);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP POST请求body的封装map集合
	 * @return
	 * 		
	 */
	public static String post(String url,String token, Map<String, String> params) throws Exception{
		if (params == null || params.size() == 0) {
			return null;
		}
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();

            // 实例化HTTP方法
            HttpPost request = new HttpPost(url);
            if (!TextUtils.isEmpty(token))
            request.addHeader("SECAuthorization", token);
            request.addHeader("ZhaidouVesion","V2.4.0");

            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            if (params!=null){
                Set<String> set =params.keySet();
                for (String key:set){
//                    Log.i("key--->"+key,"   value----->"+params.get(key));
                    parameters.add(new BasicNameValuePair(key,params.get(key)));
                }
            }

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    parameters, HTTP.UTF_8);
            request.setEntity(formEntiry);
            // 执行请求
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String result = sb.toString();
            return result;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP POST请求文本参数map集合
	 * @param files
	 * 		HTTP POST请求文件参数map集合
	 * @return
	 * 		HTTP POST请求结果
	 * @throws java.io.IOException
	 */
//	 public static String post(String url, Map<String, String> params, Map<String, File> files) throws IOException {
//	        String BOUNDARY = UUID.randomUUID().toString();
//	        String PREFIX = "--", LINEND = "\r\n";
//	        String MULTIPART_FROM_DATA = "multipart/form-data";
//
//	        URL uri = new URL(url);
//	        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
//	        // 缓存的最长时间
//	        conn.setReadTimeout(READ_TIMEOUT);
//	        // 允许输入
//	        conn.setDoInput(true);
//	        // 允许输出
//	        conn.setDoOutput(true);
//	        // 不允许使用缓存
//	        conn.setUseCaches(false);
//	        conn.setRequestMethod("POST");
//	        conn.setRequestProperty("connection", "keep-alive");
//	        conn.setRequestProperty("Charsert", "UTF-8");
//	        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
//
//	        // 首先组拼文本类型的参数
//	        StringBuilder sb = new StringBuilder();
//	        for (Entry<String, String> entry : params.entrySet()) {
//	            sb.append(PREFIX);
//	            sb.append(BOUNDARY);
//	            sb.append(LINEND);
//	            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
//	            sb.append("Content-Type: text/plain; charset=" + ENCODING + LINEND);
//	            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
//	            sb.append(LINEND);
//	            sb.append(entry.getValue());
//	            sb.append(LINEND);
//	        }
//
//	        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
//	        outStream.write(sb.toString().getBytes());
//	        // 发送文件数据
//	        if (files != null)
//	            for (Entry<String, File> file : files.entrySet()) {
//	                StringBuilder sb1 = new StringBuilder();
//	                sb1.append(PREFIX);
//	                sb1.append(BOUNDARY);
//	                sb1.append(LINEND);
//	                sb1.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
//	                        + file.getValue().getName() + "\"" + LINEND);
//	                sb1.append("Content-Type: application/octet-stream; charset=" + ENCODING + LINEND);
//	                sb1.append(LINEND);
//	                outStream.write(sb1.toString().getBytes());
//
//	                InputStream is = new FileInputStream(file.getValue());
//	                byte[] buffer = new byte[1024];
//	                int len = 0;
//	                while ((len = is.read(buffer)) != -1) {
//	                    outStream.write(buffer, 0, len);
//	                }
//	                is.close();
//	                outStream.write(LINEND.getBytes());
//	            }
//
//	        // 请求结束标志
//	        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
//	        outStream.write(end_data);
//	        outStream.flush();
//	        StringBuilder sb2 = new StringBuilder();
//	        if (conn.getResponseCode() == HttpStatus.SC_OK) {
//	        	InputStream in = conn.getInputStream();
//	            int ch;
//	            while ((ch = in.read()) != -1) {
//	                sb2.append((char) ch);
//	            }
//	        }
//	        outStream.close();
//	        conn.disconnect();
//	        return sb2.toString();
//    }

	 /**
	  * 
	  * @param params
	  * @param queryString
	  * @return
	  * 	返回拼接后的StringBuffer
	  */
	private static StringBuffer joinParam(Map<String, String> params, StringBuffer queryString) {
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			String key = param.getKey();
			String value = param.getValue();
			queryString.append(key).append('=').append(value);
			if (iterator.hasNext()) {
				queryString.append('&');
			}
		}
		return queryString;
	} 
}
