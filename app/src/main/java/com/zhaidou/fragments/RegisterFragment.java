package com.zhaidou.fragments;

import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
 import android.widget.Toast;
 import com.android.volley.RequestQueue;
 import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
 import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.AccountRegisterSetPwdActivity;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseActivity;
 import com.zhaidou.base.BaseFragment;
 import com.zhaidou.dialog.CustomLoadingDialog;
 import com.zhaidou.model.User;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;

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


 public class RegisterFragment extends BaseFragment implements View.OnClickListener {

     private static final String ARG_PARAM1 = "param1";
     private static final String ARG_PARAM2 = "param2";
     public static final String TAG = RegisterFragment.class.getSimpleName();

     private String mParam1;
     private String mParam2;
     private TextView headTitle;

     private CustomEditText mEmailView;
     private LinearLayout mLogin;
     private TextView mRegister;
     private RequestQueue mRequestQueue;
     SharedPreferences mSharedPreferences;
     static RegisterOrLoginListener mRegisterListener;

     private Dialog mDialog;
     private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             String s = (String) msg.obj;
         }
     };

     public static RegisterFragment newInstance(String param1, String param2) {
         RegisterFragment fragment = new RegisterFragment();
         Bundle args = new Bundle();
         args.putString(ARG_PARAM1, param1);
         args.putString(ARG_PARAM2, param2);
         fragment.setArguments(args);
         return fragment;
     }

     public RegisterFragment() {
     }

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (getArguments() != null) {
             mParam1 = getArguments().getString(ARG_PARAM1);
             mParam2 = getArguments().getString(ARG_PARAM2);
         }
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_register, container, false);

         headTitle=(TextView)findViewById(R.id.title_tv);
         headTitle.setText(R.string.title_register);

         mEmailView = (CustomEditText) view.findViewById(R.id.tv_email);
         mLogin = (LinearLayout) view.findViewById(R.id.tv_login);
         mRegister = (TextView) view.findViewById(R.id.bt_register);

         mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);

         mRequestQueue = Volley.newRequestQueue(getActivity());
         mRegister.setOnClickListener(this);
         mLogin.setOnClickListener(this);
         view.findViewById(R.id.back_btn).setOnClickListener(this);

         return view;
     }

     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.bt_register:
                 hideInputMethod();
                 String email = mEmailView.getText().toString();
                 if (TextUtils.isEmpty(email)) {
                     mEmailView.setShakeAnimation();
                     return;
                 }
                 if (ToolUtils.isPhoneOk(email) && email.length() > 0)
                 {
                     Intent intent=new Intent(mContext,AccountRegisterSetPwdActivity.class);
                     startActivity(intent);
//                     doRegister();
                 }
                 else
                 {
                     mEmailView.setShakeAnimation();
                     ToolUtils.setToast(getActivity(),"抱歉,无效手机号码");
                 }
                 break;
             case R.id.tv_login:
                 ((BaseActivity) getActivity()).popToStack(this);
                 break;
             case R.id.back_btn:
                 ((BaseActivity) getActivity()).popToStack(this);
                 break;
             default:
                 break;
         }
     }

     private void doRegister() {
         new MyTask().execute();
     }


     private class MyTask extends AsyncTask<Void, Void, String> {
         @Override
         protected void onPreExecute() {
             mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "注册中");
             super.onPreExecute();
         }

         @Override
         protected String doInBackground(Void... voids) {
             String str = null;
             try {
                 String email = mEmailView.getText().toString();

                 Map<String, String> valueParams = new HashMap<String, String>();
                 valueParams.put("user[email]", email);
                 str = executeHttpPost(email);
             } catch (Exception e) {

             }
             return str;
         }

         @Override
         protected void onPostExecute(String s) {
             if (mDialog != null)
                 mDialog.dismiss();
             Log.i("onPostExecute------------>", s);
             Log.i("setRegisterOrLoginListener-------->", mRegisterListener.toString());
             try {
                 JSONObject json = new JSONObject(s);
                 Object obj = json.opt("message");
                 if (obj != null) {
 //                    mRegisterListener.onRegisterOrLoginSuccess(null);
                     JSONArray errMsg = json.optJSONArray("message");
                     Toast.makeText(getActivity(), errMsg.optString(0), Toast.LENGTH_LONG).show();
                     return;
                 }

                 JSONObject userObj = json.optJSONObject("user");
                 int id = userObj.optInt("id");
                 String email = userObj.optString("email");
                 String token = userObj.optString("authentication_token");
                 String state = userObj.optString("state");
                 String avatar = userObj.optJSONObject("avatar").optJSONObject("mobile_icon").optString("url");
                 String nickname = userObj.optString("nick_name");
                 User user = new User(id, email, token, nickname, avatar);

                 Intent intent = new Intent(ZhaiDou.IntentRefreshLoginTag);
                 getActivity().sendBroadcast(intent);

                 if (getActivity() != null && getActivity() instanceof ItemDetailActivity) {
                     ((BaseActivity) getActivity()).onRegisterOrLoginSuccess(user, RegisterFragment.this);
                 } else {
                     mRegisterListener.onRegisterOrLoginSuccess(user, RegisterFragment.this);
                 }
             } catch (Exception e) {

             }

         }
     }

     public String executeHttpPost(String email) throws Exception {
         BufferedReader in = null;
         try {
             // 定义HttpClient
             HttpClient client = new DefaultHttpClient();


             // 实例化HTTP方法
             HttpPost request = new HttpPost(ZhaiDou.USER_REGISTER_URL);

             // 创建名/值组列表
             List<NameValuePair> parameters = new ArrayList<NameValuePair>();


             parameters.add(new BasicNameValuePair("user[email]", email));

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

     public void setRegisterOrLoginListener(RegisterOrLoginListener mRegisterListerner) {
         Log.i("setRegisterOrLoginListener-------->", mRegisterListerner.toString());
         this.mRegisterListener = mRegisterListerner;
     }

     public interface RegisterOrLoginListener {
         public void onRegisterOrLoginSuccess(User user, Fragment fragment);
     }

     public void onResume() {
         super.onResume();
         MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_register));
     }
     public void onPause() {
         super.onPause();
         MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_register));
     }

 }
