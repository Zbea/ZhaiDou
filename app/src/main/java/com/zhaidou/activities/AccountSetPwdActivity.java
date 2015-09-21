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
import java.util.Timer;
import java.util.TimerTask;

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

    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_ok:
                    String pwd = mPwdView.getText().toString();
                    if (TextUtils.isEmpty(pwd)) {
                        mPwdView.setShakeAnimation();
                        return;
                    }
                    else if (pwd.length()>16)
                    {
                        ToolUtils.setToast(getApplicationContext(), "抱歉,设置的密码过长");
                        mPwdView.setShakeAnimation();
                        return;
                    }
                    else if (pwd.length()<6)
                    {
                        ToolUtils.setToast(getApplicationContext(),"抱歉,设置的密码过短");
                        mPwdView.setShakeAnimation();
                    }
                    doRegister();
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

        headTitle=(TextView)findViewById(R.id.title_tv);
        headTitle.setText(R.string.title_register_set);
        mPwdView=(CustomEditText)findViewById(R.id.tv_psd);
        mOk=(TextView)findViewById(R.id.bt_ok);
        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

        mRequestQueue= Volley.newRequestQueue(this);
        mOk.setOnClickListener(onClickListener);
        findViewById(R.id.back_btn).setOnClickListener(onClickListener);

    }

    private void doRegister(){

        mDialog= CustomLoadingDialog.setLoadingDialog(AccountSetPwdActivity.this, "注册中");
        Map<String, String> valueParams = new HashMap<String,String>();
        ZhaiDouRequest request=new ZhaiDouRequest(Request.Method.POST,ZhaiDou.USER_REGISTER_URL,valueParams,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog!=null)
                    mDialog.dismiss();
                Object obj = jsonObject.opt("message");
                if (obj!=null){
                    JSONArray errMsg =  jsonObject.optJSONArray("message");
                    Toast.makeText(AccountSetPwdActivity.this,errMsg.optString(0),Toast.LENGTH_LONG).show();
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
