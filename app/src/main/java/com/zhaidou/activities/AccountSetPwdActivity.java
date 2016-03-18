package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.MD5Util;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roy on 15/9/16.
 */
public class AccountSetPwdActivity extends FragmentActivity {
    private CustomEditText mPwdView;
    private TextView headTitle;
    private TextView mOk;
    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;
    private Dialog mDialog;
    private String token;
    private String verifyCode;
    private String phone;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_ok:
                    String pwd = mPwdView.getText().toString();
                    if (TextUtils.isEmpty(pwd)) {
                        mPwdView.setShakeAnimation();
                        return;
                    } else if (pwd.length() > 16) {
                        ToolUtils.setToast(getApplicationContext(), "抱歉,设置的密码过长");
                        mPwdView.setShakeAnimation();
                        return;
                    } else if (pwd.length() < 6) {
                        ToolUtils.setToast(getApplicationContext(), "抱歉,设置的密码过短");
                        mPwdView.setShakeAnimation();
                        return;
                    }
                    doReset(pwd);
                    break;
                case R.id.back_btn:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_account_set_pwd_page);
        token = getIntent().getStringExtra("token");
        verifyCode = getIntent().getStringExtra("code");
        System.out.println("AccountSetPwdActivity.onCreate---->" + verifyCode);
        phone = getIntent().getStringExtra("phone");

        headTitle = (TextView) findViewById(R.id.title_tv);
        headTitle.setText(R.string.title_register_set);
        mPwdView = (CustomEditText) findViewById(R.id.tv_pwd);
        mOk = (TextView) findViewById(R.id.bt_ok);
        mSharedPreferences = getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

        mRequestQueue = Volley.newRequestQueue(this);
        mOk.setOnClickListener(onClickListener);
        findViewById(R.id.back_btn).setOnClickListener(onClickListener);

    }

    private void doReset(String password) {
        System.out.println("AccountSetPwdActivity.doReset------>" + phone + "---" + verifyCode);
        mDialog = CustomLoadingDialog.setLoadingDialog(AccountSetPwdActivity.this, "修改密码中");
        String md5str = MD5Util.getMD5Encoding(phone + verifyCode + "adminzhaidou888");
        Map<String, String> valueParams = new HashMap<String, String>();

        valueParams.put("password", password);
        valueParams.put("md5str", md5str);

        ZhaiDouRequest request = new ZhaiDouRequest(AccountSetPwdActivity.this,Request.Method.POST, ZhaiDou.USER_RESET_PSW_URL, valueParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();

                String message = jsonObject.optString("message");
                JSONObject dataObj = jsonObject.optJSONObject("data");
                if (jsonObject.isNull("code")) {
                    int status = dataObj.optInt("status");
                    String msg = dataObj.optString("message");
                    if (201 == status) {
                        setResult(1500);
                        finish();
                    } else {
                        Toast.makeText(AccountSetPwdActivity.this, TextUtils.isEmpty(msg) ? message : msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String msg = dataObj.optString("message");
                    Toast.makeText(AccountSetPwdActivity.this, TextUtils.isEmpty(msg) ? message : msg, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        ((ZDApplication) getApplication()).mRequestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getResources().getString(R.string.title_register_set));
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getResources().getString(R.string.title_register_set));
        MobclickAgent.onPause(this);
    }

}
