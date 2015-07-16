package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.User;
import com.zhaidou.utils.SharedPreferencesUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    setResult(RESULT_OK, intent);
                    finish();//此处一定要调用finish()方法
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

        new MyTask().execute();
    }


    private class MyTask extends AsyncTask<Void,Void,String> {
        @Override
        protected void onPreExecute() {
            mDialog= CustomLoadingDialog.setLoadingDialog(RegisterActivity.this, "注册中");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String str=null;
            try {
                String email = mEmailView.getText().toString();
                String password =mPswView.getText().toString();
                String psw_confirm=mConfirmPsw.getText().toString();
                String nick =mNickView.getText().toString();

                Map<String, String> valueParams = new HashMap<String,String>();
                valueParams.put("user[email]", email);
                valueParams.put("user[password]", password);
                valueParams.put("user[password_confirmations]",psw_confirm);
                valueParams.put("user[nick_name]", nick);
                str = executeHttpPost(email,password,psw_confirm,nick);
            }catch (Exception e){

            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mDialog!=null)
                mDialog.dismiss();
            try {
                JSONObject json = new JSONObject(s);
                Object obj = json.opt("message");
                if (obj!=null){
                    JSONArray errMsg =  json.optJSONArray("message");
                    Toast.makeText(RegisterActivity.this,errMsg.optString(0),Toast.LENGTH_LONG).show();
                    return;
                }

                Log.i("before--->","before");
                JSONObject userObj = json.optJSONObject("user");
                Log.i("userObj--->","userObj");
                int id = userObj.optInt("id");
                Log.i("id--->","id");
                String email = userObj.optString("email");
                Log.i("email--->","email");
                String token = userObj.optString("authentication_token");
                Log.i("token--->","token");
                String state =userObj.optString("state");
                Log.i("state--->","state");
                String avatar = userObj.optJSONObject("avatar").optJSONObject("mobile_icon").optString("url");
                Log.i("avatar--->","avatar");
                String nickname=userObj.optString("nick_name");
                Log.i("nickname--->","nickname");
                User user=new User(id,email,token,nickname,avatar);
                Log.i("user------------>",user.toString());
                Log.i("onRegisterOrLoginSuccess---->","onRegisterOrLoginSuccess");
                Message message=new Message();
                message.what=0;
                message.obj=user;
                handler.sendMessage(message);
            }catch (Exception e){

            }

        }
    }

    public String executeHttpPost(String email,String psw,String psw2,String nick) throws Exception {
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_REGISTER_URL);

            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();


            parameters.add(new BasicNameValuePair("user[email]",email));
            parameters.add(new BasicNameValuePair("user[password]",psw));
            parameters.add(new BasicNameValuePair("user[password_confirmations]",psw2));
            parameters.add(new BasicNameValuePair("user[nick_name]",nick));

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

}
