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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangclark on 15/7/16.
 */
public class RegisterActivity extends FragmentActivity implements View.OnClickListener{
    private EditText mEmailView,mNickView,mPswView,mConfirmPsw;
    private TextView mLogin;
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
                    Log.i("handleMessage---------->",user.toString());
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
        mEmailView=(EditText)findViewById(R.id.tv_email);
        mNickView=(EditText)findViewById(R.id.tv_nick);
        mPswView=(EditText)findViewById(R.id.tv_password);
        mConfirmPsw=(EditText)findViewById(R.id.tv_password_confirm);
        mLogin=(TextView)findViewById(R.id.tv_login);
        mRegister=(TextView)findViewById(R.id.bt_register);

        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

        mRequestQueue= Volley.newRequestQueue(this);
        mRegister.setOnClickListener(this);
        mLogin.setOnClickListener(this);
        findViewById(R.id.ll_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_register:
                String email = mEmailView.getText().toString();
                String password =mPswView.getText().toString();
                String psw_confirm=mConfirmPsw.getText().toString();
                String nick =mNickView.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(this, "邮箱不能为空哦！", Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(nick)){
                    Toast.makeText(this,"昵称不能为空哦！",Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(password)){
                    Toast.makeText(this,"密码不能为空哦！",Toast.LENGTH_SHORT).show();
                    return;
                }else if (!password.equals(psw_confirm)){
                    Toast.makeText(this,"两次密码不一致哦！",Toast.LENGTH_SHORT).show();
                    return;
                }
                doRegister();
                break;
            case R.id.tv_login:
//                ((BaseActivity)getActivity()).popToStack(this);
                break;
            case R.id.ll_back:
//                ((BaseActivity)getActivity()).popToStack(this);
                finish();
                break;
            default:
                break;
        }
    }
    private void doRegister(){
        Log.i("doRegister------->", "doRegister");

        mDialog= CustomLoadingDialog.setLoadingDialog(RegisterActivity.this, "注册中");
        String email = mEmailView.getText().toString();
        String password =mPswView.getText().toString();
        String psw_confirm=mConfirmPsw.getText().toString();
        String nick =mNickView.getText().toString();
        Map<String, String> valueParams = new HashMap<String,String>();
        valueParams.put("user[email]", email);
        valueParams.put("user[password]", password);
        valueParams.put("user[password_confirmations]",psw_confirm);
        valueParams.put("user[nick_name]", nick);
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
