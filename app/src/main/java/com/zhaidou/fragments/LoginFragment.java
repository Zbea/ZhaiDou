package com.zhaidou.fragments;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.Environment;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.alibaba.sdk.android.session.model.Session;
import com.alibaba.sdk.android.util.JSONUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.model.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.login.LoginService;
import com.alibaba.sdk.android.login.callback.LoginCallback;
import com.zhaidou.utils.NativeHttpUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LoginFragment extends Fragment implements View.OnClickListener,PlatformActionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView mEmailView,mPswView,mRegisterView,mResetView;

    private TextView mLoginView;
    public static final String TAG=LoginFragment.class.getSimpleName();


    private RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener;

    public int index;
    RequestQueue requestQueue;

    private static final String SHARED_PRE = "_tae_sdk_demo";

    private static final String KEY_ENV_INDEX = "envIndex";

    private static final String FORMAT_STRING = "{\"version\":\"1.0.0.daily\",\"target\":\"thirdpartlogin\",\"params\":{\"loginInfo\":{\"loginId\":\"%s\",\"password\":\"%s\"}}}";
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LoginFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_login, container, false);
        mEmailView=(TextView)view.findViewById(R.id.tv_email);
        mPswView=(TextView)view.findViewById(R.id.tv_password);
        mLoginView=(TextView)view.findViewById(R.id.bt_login);
        mRegisterView=(TextView)view.findViewById(R.id.tv_register);
        mResetView=(TextView)view.findViewById(R.id.tv_reset_psw);

        requestQueue=Volley.newRequestQueue(getActivity());
        mLoginView.setOnClickListener(this);
        mRegisterView.setOnClickListener(this);
        mResetView.setOnClickListener(this);
        view.findViewById(R.id.ll_back).setOnClickListener(this);
        view.findViewById(R.id.ll_qq).setOnClickListener(this);
        view.findViewById(R.id.ll_weixin).setOnClickListener(this);
        view.findViewById(R.id.ll_weibo).setOnClickListener(this);
        view.findViewById(R.id.ll_taobao).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        ShareSDK.initSDK(getActivity());
        switch (view.getId()){
            case R.id.bt_login:
                new MyTask().execute();
                break;
            case R.id.tv_register:
                RegisterFragment fragment = RegisterFragment.newInstance("","");
                if (getActivity()!=null&&getActivity() instanceof ItemDetailActivity){
                    Log.i("R.id.ll_back:","getActivity()!=null&&getActivity() instanceof ItemDetailActivity");
                    ((ItemDetailActivity)getActivity()).navigationToFragment(fragment);
                    return;
                }else if (getActivity()!=null&&getActivity() instanceof MainActivity){
                    ((MainActivity)getActivity()).navigationToFragment(fragment);
                    return;
                }
                ((PersonalMainFragment)getParentFragment()).addToStack(fragment);
                break;
            case R.id.tv_reset_psw:
                break;
            case R.id.ll_back:
                Log.i("ll_back--->","ll_back");
                if (getActivity()!=null&&getActivity() instanceof ItemDetailActivity){
                    Log.i("R.id.ll_back:","getActivity()!=null&&getActivity() instanceof ItemDetailActivity");
                    ((ItemDetailActivity)getActivity()).popToStack();
                    return;
                }else if (getActivity()!=null&&getActivity() instanceof MainActivity){
                    Log.i("R.id.ll_back:","getActivity()!=null&&getActivity() instanceof MainActivity");
                    ((MainActivity)getActivity()).popToStack(this);
                    return;
                }
                ((PersonalMainFragment)getParentFragment()).popToStack();
                ((PersonalMainFragment)getParentFragment()).toggleTabContainer();
                break;
            case R.id.ll_weixin:
                Log.i("ll_weixin--->","ll_weixin");
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                authorize(wechat);
                break;
            case R.id.ll_qq:
                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                authorize(qq);
                break;
            case R.id.ll_weibo:
                //新浪微博
                Platform sina = ShareSDK.getPlatform(SinaWeibo.NAME);
                authorize(sina);
                break;
            case R.id.ll_taobao:
                AlibabaSDK.getService(LoginService.class).showLogin(getActivity(),new LoginCallback() {
                    @Override
                    public void onSuccess(final Session session) {
                        Log.i("onSuccess-----","onSuccess");
                        Toast.makeText(getActivity(),"onSuccess",Toast.LENGTH_LONG).show();
                        Log.i("getUserId", session.getUserId());
                        Log.i("getUserIcon",session.getUser().avatarUrl);
                        Log.i("getUserName",session.getUser().nick);
                        Map<String,String> params =new HashMap<String, String>();
                        params.put("uid",session.getUserId());
                        params.put("provider","taobao");
                        params.put("nick_name",session.getUser().nick);

                        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,"http://192.168.199.171/api/v1/users/verification_other",new JSONObject(params),new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Log.i("jsonObject--->",jsonObject.toString());
                                Toast.makeText(getActivity(),jsonObject.toString(),Toast.LENGTH_LONG).show();
                                int flag=jsonObject.optInt("flag");

                                if (0==flag){
                                    JSONObject login_user=jsonObject.optJSONObject("user").optJSONObject("login_user");
                                    String email = login_user.optString("s_email");
                                    String nick=login_user.optString("s_nick_name");
                                    Log.i("0==flag","0==flag");
                                    Map<String,String> registers = new HashMap<String, String>();
                                    registers.put("user[email]",email);
                                    registers.put("user[nick_name]",session.getUser().nick);
                                    registers.put("user[uid]",session.getUserId());
                                    registers.put("user[provider]","taobao");
                                    registers.put("profile_image",session.getUser().avatarUrl);

                                    new RegisterTask().execute(registers);
                                }else {
                                    Log.i("flag==1","flag==1");
                                    JSONObject userJson = jsonObject.optJSONObject("user");
                                    String token =userJson.optJSONObject("user_tokens").optString("token");
                                    JSONArray userArray = userJson.optJSONArray("users");
                                    if (userArray!=null&&userArray.length()>0){
                                        JSONObject user = userArray.optJSONObject(0);
                                        String nick = user.optString("nick_name");
                                        int id = user.optInt("id");
                                        String email = user.optString("email");
                                        User u = new User(id,email,token,nick,null);
                                        Log.i("LoginFragment----onRegisterOrLoginSuccess---->","onRegisterOrLoginSuccess");
                                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(u,LoginFragment.this);
                                    }
                                }
                            }
                        },new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.i("volleyError--->",volleyError.getMessage());
                                Toast.makeText(getActivity(),volleyError.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                        requestQueue.add(request);
                    }
                    @Override
                    public void onFailure(int i, String s) {
                        Log.i("onFailure---->","onFailure");
                        Toast.makeText(getActivity(),"onFailure",Toast.LENGTH_LONG).show();
                    }
                });
                break;
            default:
                break;
        }
    }

    private class MyTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... voids) {
            String str=null;
            try {
                String email = mEmailView.getText().toString();
                String password =mPswView.getText().toString();

                str = executeHttpPost(email,password);
            }catch (Exception e){

            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("onPostExecute------------>", s);
            try {
                JSONObject json = new JSONObject(s);
                Object obj = json.opt("message");
                if (obj!=null){
                    JSONArray errMsg =  json.optJSONArray("message");
                    Toast.makeText(getActivity(),errMsg.optString(0),Toast.LENGTH_LONG).show();
                    return;
                }

                Log.i("before--->","before");
                JSONArray userArr = json.optJSONArray("users");
                for (int i=0;i<userArr.length();i++){
                    JSONObject userObj = userArr.optJSONObject(i);
                    int id = userObj.optInt("id");
                    Log.i("id--->",id+"");
                    String email=userObj.optString("email");
                    Log.i("email--->",email);
                    String nick = userObj.optString("nick_name");
                    Log.i("nickname--->",nick);
                    String token=json.optJSONObject("user_tokens").optString("token");
                    Log.i("token--->",token);

                    User user = new User(id,email,token,nick,null);
                    Log.i("LoginFragment----onRegisterOrLoginSuccess---->","onRegisterOrLoginSuccess");
                    mRegisterOrLoginListener.onRegisterOrLoginSuccess(user,LoginFragment.this);
                }

            }catch (Exception e){

            }

        }
    }
    public String executeHttpPost(String email,String psw) throws Exception {
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost("http://192.168.199.171/api/v1/user_tokens");

            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();


            parameters.add(new BasicNameValuePair("user_token[email]",email));
            parameters.add(new BasicNameValuePair("user_token[password]",psw));
//            parameters.add(new BasicNameValuePair("user[nick_name]",nick));

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    parameters);
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
    private void authorize(Platform plat) {
        Log.i("Platform----->",plat.getName());
        if (plat == null) {
            return;
        }

        plat.setPlatformActionListener(this);
        //关闭SSO授权
        plat.SSOSetting(true);
        plat.showUser(null);
    }

    @Override
    public void onComplete(final Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
        Log.i("onComplete----->",platform.getName()+"---"+i);
//        Log.i("stringObjectHashMap",stringObjectHashMap.toString());
        String plat =platform.getName();
        final String provider=plat.equals("QQ")?"tqq":plat.equals("SinaWeibo")?"weibo":"weixin";
        Log.i("getUserId", platform.getDb().getUserId());
        Log.i("getUserIcon",platform.getDb().getUserIcon());
        Log.i("getUserName",platform.getDb().getUserName());
        Map<String,String> params =new HashMap<String, String>();
        params.put("uid",platform.getDb().getUserId());
        params.put("provider",provider);
        params.put("nick_name",platform.getDb().getUserName());

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,"http://192.168.199.171/api/v1/users/verification_other",new JSONObject(params),new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("jsonObject--->",jsonObject.toString());
                int flag=jsonObject.optInt("flag");

                if (0==flag){
                    JSONObject login_user=jsonObject.optJSONObject("user").optJSONObject("login_user");
                    String email = login_user.optString("s_email");
                    String nick=login_user.optString("s_nick_name");
                    Log.i("0==flag","0==flag");
                    Map<String,String> registers = new HashMap<String, String>();
                    registers.put("user[email]",email);
                    registers.put("user[nick_name]",platform.getDb().getUserName());
                    registers.put("user[uid]",platform.getDb().getUserId());
                    registers.put("user[provider]",provider);
                    registers.put("user[agreed]",true+"");
                    registers.put("profile_image",platform.getDb().getUserIcon());

                    new RegisterTask().execute(registers);
                }else {
                    Log.i("flag==1","flag==1");
                    JSONObject userJson = jsonObject.optJSONObject("user");
                    String token =userJson.optJSONObject("user_tokens").optString("token");
                    JSONArray userArray = userJson.optJSONArray("users");
                    if (userArray!=null&&userArray.length()>0){
                        JSONObject user = userArray.optJSONObject(0);
                        String nick = user.optString("nick_name");
                        int id = user.optInt("id");
                        String email = user.optString("email");
                        User u = new User(id,email,token,nick,null);
                        Log.i("LoginFragment----onRegisterOrLoginSuccess---->","onRegisterOrLoginSuccess");
                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(u,LoginFragment.this);
                    }
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("volleyError--->",volleyError.getMessage());
            }
        });
        requestQueue.add(request);
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Log.i("platform----->",platform.getName()+"---"+i+throwable.getMessage().toString());
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Log.i("onCancel----->",platform.getName()+"---"+i);
    }

    public void setRegisterOrLoginListener(RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener) {
        this.mRegisterOrLoginListener = mRegisterOrLoginListener;
    }

    private class RegisterTask extends AsyncTask<Map<String,String>,Void,String>{
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            Log.i("doInBackground--------------->",maps[0].toString());
            String s=null;
            try {
                s=NativeHttpUtil.post("http://192.168.199.171/api/v1/users",null,maps[0]);
            }catch (Exception e){
                Log.i("e--->",e.getMessage());
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("RegisterTask-->onPostExecute-->s--->",s);
            try{
                JSONObject json = new JSONObject(s);
                JSONObject userJson = json.optJSONObject("user");
                int id = userJson.optInt("id");
                String email =userJson.optString("email");
                String token =userJson.optString("authentication_token");
                String avatar =userJson.optJSONObject("avatar").optString("url");
                String nick=userJson.optString("nick_name");
                Log.i("LoginFragment----onRegisterOrLoginSuccess---->","onRegisterOrLoginSuccess");
                User user=new User(id,email,token,nick,avatar);
                mRegisterOrLoginListener.onRegisterOrLoginSuccess(user,LoginFragment.this);
            }catch (Exception e){
//                Log.i("e--------->",e.getMessage());
            }
        }
    }

    private class RegisterThirdTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }
}
