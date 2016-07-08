package com.zhaidou.utils;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.CountManager;
import com.zhaidou.model.Comment;
import com.zhaidou.model.Coupons;
import com.zhaidou.model.ZhaiDouRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
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


    /**
     * banner点击统计
     * @param mContext
     * @param name
     * @param url
     * @param bannerType
     * @param bannerIndex
     * @param successListener
     * @param errorListener
     */
    public static void getBannerClick(Context mContext,String name,String url,int bannerType,int bannerIndex,final SuccessListener successListener, final ErrorListener errorListener) {
        int userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        String token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        String surl;
        if (!TextUtils.isEmpty(token))
        {
            surl=ZhaiDou.HomeClickStatisticalUrl+name+"&url="+url+"&userId="+userId+"&sourceCode=3&bannerType="+bannerType+"&bannerIndex="+bannerIndex;
        }
        else
        {
            surl=ZhaiDou.HomeClickStatisticalUrl+name+"&url="+url+"&sourceCode=3&bannerType="+bannerType+"&bannerIndex="+bannerIndex;
        }
        ZhaiDouRequest request = new ZhaiDouRequest(surl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                }
            }
        }, null);
        ZDApplication.newRequestQueue().add(request);
    }

    /**
     * 获取省市区
     * @param successListener
     * @param errorListener
     */
    public static void getAddressCity(final SuccessListener successListener, final ErrorListener errorListener) {
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.GET, ZhaiDou.ORDER_ADDRESS_URL, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null)
                    successListener.onSuccess(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }


    /**
     * 零元特卖购买请求
     * @param userId
     * @param successListener
     * @param errorListener
     */
    public static void getIsBuyOSale(int userId,final SuccessListener successListener, final ErrorListener errorListener) {
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.GET, ZhaiDou.IsBuyOSaleUrl + userId, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null)
                    successListener.onSuccess(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    /**
     * 自动更新管理
     * @param successListener
     * @param errorListener
     */
    public static void getApkManage(final SuccessListener successListener, final ErrorListener errorListener) {
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.GET, ZhaiDou.ApkUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null)
                    successListener.onSuccess(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    /**
     * 查看购物车数量
     * @param userId
     * @param successListener
     * @param errorListener
     */
    public static void getCartCount(int userId,final SuccessListener successListener, final ErrorListener errorListener) {
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.GET, ZhaiDou.CartGoodsCountUrl + userId, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null)
                    successListener.onSuccess(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }


    public static void getUserInfo(int userId,final SuccessListener successListener, final ErrorListener errorListener) {
        ZhaiDouRequest request = new ZhaiDouRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + "?id=" + userId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null)
                    successListener.onSuccess(jsonObject);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.newRequestQueue().add(request);
    }

    public static void getUserDetail(int userId,final SuccessListener successListener, final ErrorListener errorListener) {
        ZhaiDouRequest request = new ZhaiDouRequest(ZhaiDou.USER_DETAIL_PROFILE_URL + "?id=" + userId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null)
                    successListener.onSuccess(jsonObject);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.newRequestQueue().add(request);
    }

    public static String comment(Map<String, Object> params, SuccessListener successListener, ErrorListener errorListener) {
        String userId = params.get("commentUserId").toString();
        String userName = params.get("commentUserName").toString();
        String content = params.get("content").toString();
        String articleId = params.get("articleId").toString();
        String articleTitle = params.get("articleTitle").toString();
        String commentType = params.get("commentType").toString();
        String commentId = params.get("commentId").toString();
        List<String> images = (List<String>) params.get("images");
        String BOUNDARY = UUID.randomUUID().toString();
        String result = null;
        BufferedReader in = null;
        try
        {
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, Charset.defaultCharset());
            multipartEntity.addPart("commentUserId", new StringBody(userId + ""));
            multipartEntity.addPart("commentUserName", new StringBody(userName, Charset.defaultCharset()));
            multipartEntity.addPart("content", new StringBody(content, Charset.defaultCharset()));
            multipartEntity.addPart("articleId", new StringBody(articleId + ""));
            multipartEntity.addPart("articleTitle", new StringBody(articleTitle + "", Charset.defaultCharset()));
            multipartEntity.addPart("commentType", new StringBody(commentType));
        if (commentId!=null)
        {
            multipartEntity.addPart("commentId", new StringBody(commentId+""));
        }

            for (int i = 0; images != null && i < images.size(); i++)
            {
                String image = images.get(i);
                if (!TextUtils.isEmpty(image))
                {
                    FileBody fileBody = new FileBody(new File(image));
                    multipartEntity.addPart("files", fileBody);
                }
            }

            HttpPost request = new HttpPost(ZhaiDou.CommentAddUrl);
            request.addHeader("ZhaidouVesion", ZDApplication.getInstance().getResources().getString(R.string.app_versionName));
            request.addHeader("zd-client", "ANDROID");
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
            while ((line = in.readLine()) != null)
            {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            if (request != null)
            {
                JSONObject jsonObject = new JSONObject(result);
                successListener.onSuccess(jsonObject);
            } else
        {
            errorListener.onError("加载失败");
        }
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

    public static void getUnReadComment(int userId, final SuccessListener successListener, final ErrorListener errorListener) {
        System.out.println("Api.getUnReadComment");
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.GET, ZhaiDou.URL_GET_UNREAD_COMMETN + userId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                System.out.println("Api.onResponse");
                if (status == 200) {
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    int notReadNum = dataObj.optInt("NotReadNum");
                    int unReadComment = dataObj.optInt("UnReadComment");
                    int unReadDesigner = dataObj.optInt("UnReadDesigner");
                    JSONObject commentObj = dataObj.optJSONObject("comment");
                    Comment comment = JSON.parseObject(commentObj.toString(), Comment.class);
                    SharedPreferencesUtil.saveCommentData(comment,notReadNum,unReadComment,unReadDesigner);
                    System.out.println("comment = " + comment);
                    CountManager.getInstance().notifyCommentChange();
                    if (successListener != null)
                        successListener.onSuccess(jsonObject);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    //删除评论
    public static void deleteComment(int commentId, final SuccessListener successListener, final ErrorListener errorListener) {
        String token = (String) SharedPreferencesUtil.getData(ZDApplication.getInstance(), "token", "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", commentId + "");
        params.put("token", token);
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.URL_DELETE_COMMENT + commentId,params,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (successListener != null) {
                    successListener.onSuccess(new Object());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener != null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    //单个优惠劵领取接口
    public static void activateCoupons(String couponCode, final SuccessListener successListener, final ErrorListener errorListener){
        Map<String,String> params=new HashMap<String, String>();
        String userId=SharedPreferencesUtil.getData(ZDApplication.getInstance(),"userId",0)+"";
        String nickName= (String) SharedPreferencesUtil.getData(ZDApplication.getInstance(),"nickName","");
        params.put("userId",userId);
        params.put("couponCode",couponCode);
        params.put("nickName",nickName);
        ZhaiDouRequest request=new ZhaiDouRequest(Request.Method.POST,ZhaiDou.activateCoupons,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status==200){
                    JSONObject data = jsonObject.optJSONObject("data");
                    int code = jsonObject.optInt("code");
                    if (code==0&&data!=null){
                        JSONObject object = data.optJSONObject("data");
                        if (object!=null) {
                            Coupons coupons = JSON.parseObject(object.toString(), Coupons.class);
                            successListener.onSuccess(coupons);
                        }
                    }else if (code==-1){
                        String msg = data.optString("msg");
                        Toast.makeText(ZDApplication.getInstance(),msg,Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(ZDApplication.getInstance(),message,Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener!=null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    //一键领取优惠劵
    public static void activateAllCouponsByOneClick(String[] couponCodes, final SuccessListener successListener, final ErrorListener errorListener){
        Map<String,String> params=new HashMap<String, String>();
        String userId=SharedPreferencesUtil.getData(ZDApplication.getInstance(),"userId",0)+"";
        String nickName= (String) SharedPreferencesUtil.getData(ZDApplication.getInstance(),"nickName","");
        params.put("userId",userId);
        params.put("nickName",nickName);
        JSONArray jsonArray=new JSONArray();
        for (int i = 0; i < couponCodes.length; i++) {
            String couponCode=couponCodes[i];
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put("couponKey",couponCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        params.put("couponCodes",jsonArray.toString());
        ZhaiDouRequest request=new ZhaiDouRequest(Request.Method.POST,ZhaiDou.activateAllCouponsByOneClick,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status==200){
                    JSONObject data = jsonObject.optJSONObject("data");
                    int code = jsonObject.optInt("code");
                    if (code==0&&data!=null){
                        JSONObject object = data.optJSONObject("data");
                        if (object!=null) {
                            Coupons coupons = JSON.parseObject(object.toString(), Coupons.class);
                            successListener.onSuccess(coupons);
                        }
                    }else if (code==-1){
                        String msg = data.optString("msg");
                        Toast.makeText(ZDApplication.getInstance(),msg,Toast.LENGTH_SHORT).show();
                    }
                }else {
                    errorListener.onError(null);
                    Toast.makeText(ZDApplication.getInstance(),message,Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorListener!=null)
                    errorListener.onError(volleyError);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }


    public interface SuccessListener {
        public void onSuccess(Object object);
    }

    public interface ErrorListener {
        public void onError(Object object);
    }
}
