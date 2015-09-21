package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangclark on 15/7/16.
 */
public class RegisterActivity extends FragmentActivity implements View.OnClickListener{
    private CustomEditText mEmailView;
    private TextView headTitle;
    private LinearLayout mLogin;
    private TextView mRegister;
    private RequestQueue mRequestQueue;
    SharedPreferences mSharedPreferences;
    private Dialog mDialog;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    User user =(User)msg.obj;
                    SharedPreferencesUtil.saveUser(getApplicationContext(), user);
                    Intent intent=new Intent();
                    intent.putExtra("id",user.getId());
                    intent.putExtra("email",user.getEmail());
                    intent.putExtra("token",user.getAuthentication_token());
                    intent.putExtra("nick",user.getNickName());
                    setResult(2000, intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_register);

        headTitle=(TextView)findViewById(R.id.title_tv);
        headTitle.setText(R.string.title_register);

        mEmailView=(CustomEditText)findViewById(R.id.tv_email);
        mLogin=(LinearLayout)findViewById(R.id.tv_login);
        mRegister=(TextView)findViewById(R.id.bt_register);

        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

        mRequestQueue= Volley.newRequestQueue(this);
        mRegister.setOnClickListener(this);
        mLogin.setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_register:
                String email = mEmailView.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setShakeAnimation();
                    return;
                }
                if (ToolUtils.isPhoneOk(email) && email.length() > 0)
                {
                    Intent intent=new Intent(this,AccountRegisterSetPwdActivity.class);
                    startActivity(intent);
//                    doRegister();
                }
                else
                {
                    mEmailView.setShakeAnimation();
                    ToolUtils.setToast(this,"抱歉,无效手机号码");
                }
                break;
            case R.id.tv_login:
                finish();
                break;
            case R.id.back_btn:
                finish();
                break;
            default:
                break;
        }
    }
    private void doRegister(){

        mDialog= CustomLoadingDialog.setLoadingDialog(RegisterActivity.this, "注册中");
        String email = mEmailView.getText().toString();
        Map<String, String> valueParams = new HashMap<String,String>();
        valueParams.put("user[email]", email);
        ZhaiDouRequest request=new ZhaiDouRequest(Request.Method.POST,ZhaiDou.USER_REGISTER_URL,valueParams,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog!=null)
                    mDialog.dismiss();
                Object obj = jsonObject.opt("message");
                if (obj!=null){
                    JSONArray errMsg =  jsonObject.optJSONArray("message");
                    Toast.makeText(RegisterActivity.this,errMsg.optString(0),Toast.LENGTH_LONG).show();
                    return;
                }

                JSONObject userObj = jsonObject.optJSONObject("user");
                int id = userObj.optInt("id");
                String email = userObj.optString("email");
                String token = userObj.optString("authentication_token");
                String state =userObj.optString("state");
                String avatar = userObj.optJSONObject("avatar").optJSONObject("mobile_icon").optString("url");
                String nickname=userObj.optString("nick_name");
                User user=new User(id,email,token,nickname,avatar);
                Message message=new Message();
                message.what=0;
                message.obj=user;
                handler.sendMessage(message);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getResources().getString(R.string.title_register));
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getResources().getString(R.string.title_register));
        MobclickAgent.onPause(this);
    }
}
