package com.zhaidou.model;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.zhaidou.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-09-09
 * Time: 14:27
 * Description:自定义request,方便请求复杂数据结构的参数
 * FIXME
 */
public class ZhaiDouRequest extends Request<JSONObject> {

    private final Response.Listener<JSONObject> mListener;
    private Map<String,String> params;
    private Map<String,String> mHeaders;
    protected Context mContext;

    public ZhaiDouRequest(Context context,int method, String url, Response.Listener<JSONObject> listener,
                      Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mContext=context;
        initHeader();
    }

    public ZhaiDouRequest(Context context,int method,String url,Map<String,String> params, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(context,Method.POST, url, listener, errorListener);
        this.params=params;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String xmlString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject object=new JSONObject(xmlString);
            return Response.success(object, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getParams() {
        return this.params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

    private void initHeader(){
        mHeaders=new HashMap<String, String>();
        mHeaders.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
    }

}