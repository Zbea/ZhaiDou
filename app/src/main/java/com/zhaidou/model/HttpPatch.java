package com.zhaidou.model;

import org.apache.http.client.methods.HttpPut;

/**
 * Created by wangclark on 15/7/31.
 */
public class HttpPatch extends HttpPut{
    public HttpPatch(String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return "PATCH";
    }
}
