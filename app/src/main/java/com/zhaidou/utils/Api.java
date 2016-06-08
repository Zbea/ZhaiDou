package com.zhaidou.utils;/**
 * Created by wangclark on 16/6/7.
 */

import android.text.TextUtils;

import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-06-07
 * Time: 17:08
 * Description:封装网络请求
 * FIXME
 */
public class Api {

    public static String comment(Map<String, Object> params, SuccessListener successListener, ErrorListener errorListener) {
        System.out.println("params = [" + params + "], successListener = [" + successListener + "], errorListener = [" + errorListener + "]");
        String userId = params.get("commentUserId").toString();
        String userName = params.get("commentUserName").toString();
        String content = params.get("content").toString();
        String articleId = params.get("articleId").toString();
        String articleTitle = params.get("articleTitle").toString();
        String commentType = params.get("commentType").toString();
        String commentId = params.get("commentId").toString();
        List<String> images= (List<String>) params.get("images");
        String BOUNDARY = UUID.randomUUID().toString();
        String result = null;
        BufferedReader in = null;
        try {
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, Charset.defaultCharset());
            multipartEntity.addPart("commentUserId", new StringBody(userId + ""));
            multipartEntity.addPart("commentUserName", new StringBody(userName, Charset.defaultCharset()));
            multipartEntity.addPart("content", new StringBody(content, Charset.defaultCharset()));
            multipartEntity.addPart("articleId", new StringBody(articleId + ""));
            multipartEntity.addPart("articleTitle", new StringBody(articleTitle + ""));
            multipartEntity.addPart("commentType", new StringBody(commentType));
            multipartEntity.addPart("commentId", new StringBody(commentId));
            for (int i = 0; images!=null&&i < images.size(); i++) {
                String image = images.get(i);
                if (!TextUtils.isEmpty(image)){
                    FileBody fileBody = new FileBody(new File(images.get(i)));
                    multipartEntity.addPart("files", fileBody);
                }
            }

            HttpPost request = new HttpPost(ZhaiDou.CommentAddUrl);
            request.addHeader("ZhaidouVesion", ZDApplication.getInstance().getResources().getString(R.string.app_versionName));
            request.addHeader("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            request.addHeader("Accept", "application/json");
            request.setEntity(multipartEntity);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            System.out.println("result = " + result);
            JSONObject jsonObject=new JSONObject(result);
            successListener.onSuccess(jsonObject);
            return result;

        } catch (Exception e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    errorListener.onError(e.getMessage());
                }
            }
        }
        return result;
    }

    public static void deleteComment(SuccessListener successListener, ErrorListener errorListener) {
        if (successListener != null) {
            successListener.onSuccess(new Object());
        }
    }


    public interface SuccessListener {
        public void onSuccess(Object object);
    }

    public interface ErrorListener {
        public void onError(Object object);
    }
}
