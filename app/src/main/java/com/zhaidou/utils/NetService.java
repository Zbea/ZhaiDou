package com.zhaidou.utils;

import android.content.Context;
import android.util.Log;

import com.zhaidou.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class NetService
{
	private static String result;
    private static InputStream is;
	
	/**
	 * http get请求
	 * @param url
	 * @return
	 */
	public static String GETHttpService(String url, Context mContext)
	{
		try
		{
			HttpGet httpGet=new HttpGet(url);
            httpGet.addHeader("ZhaidouVesion",  mContext.getResources().getString(R.string.app_versionName));
			HttpClient httpClient=new DefaultHttpClient();
			HttpResponse httpResponse=httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK)
			{
				result=EntityUtils.toString(httpResponse.getEntity(), "utf-8");
				Log.i("zhaidou", "result:"+result);
			}
			else
			{
				result=null;
			}
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return result;
		
	}


    public static String GETHttpPostService(Context mContext,String url,Map<String,String> headers,List<NameValuePair> params)
    {
        BufferedReader in = null;
        try
        {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();
            // 实例化HTTP方法
            HttpPost request = new HttpPost(url);
            for (String key:headers.keySet())
            {
                request.addHeader(key, headers.get(key));
            }
            // 创建名/值组列表
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("userId", userId + ""));
//            params.add(new BasicNameValuePair("skuAndNumLists", new JSONArray(mDatas).toString()));
//            params.add(new BasicNameValuePair("nickName", userName));
//            params.add(new BasicNameValuePair("couponCode", couponCode));
            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    params, HTTP.UTF_8);
            request.setEntity(formEntiry);
            // 执行请求
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null)
            {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            return result;

        } catch (Exception e)
        {

        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


}
