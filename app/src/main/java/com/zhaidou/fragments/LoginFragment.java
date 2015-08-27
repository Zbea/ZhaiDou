package com.zhaidou.fragments;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.Environment;
import com.alibaba.sdk.android.callback.CallbackContext;
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
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
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
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

public class LoginFragment extends BaseFragment implements View.OnClickListener, PlatformActionListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private TextView mRegisterView, mResetView;
    private EditText mEmailView, mPswView;
    private ImageView emailDelete;
    private String strEmail;
    private Context mContext;

    private TextView mLoginView;
    public static final String TAG = LoginFragment.class.getSimpleName();

    private RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener;
    private BackClickListener backClickListener;

    public int index;
    RequestQueue requestQueue;

    private Dialog mDialog;

    private static final String SHARED_PRE = "_tae_sdk_demo";

    private static final String KEY_ENV_INDEX = "envIndex";

    private static final String FORMAT_STRING = "{\"version\":\"1.0.0.daily\",\"target\":\"thirdpartlogin\",\"params\":{\"loginInfo\":{\"loginId\":\"%s\",\"password\":\"%s\"}}}";


    /**
     * 输入邮箱改变事件
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            strEmail = charSequence.toString();
            if (charSequence.toString().length() > 0) {
                emailDelete.setVisibility(View.VISIBLE);
            } else {
                emailDelete.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mContext = getActivity();
        strEmail = getEmail();

        mEmailView = (EditText) view.findViewById(R.id.tv_email);
        mEmailView.setText(strEmail);
        mEmailView.addTextChangedListener(textWatcher);
        emailDelete = (ImageView) findViewById(R.id.emailDelete);
        emailDelete.setOnClickListener(this);
        if (strEmail != null)
            if (strEmail.length() > 0) {
                emailDelete.setVisibility(View.VISIBLE);
            }
        mPswView = (EditText) view.findViewById(R.id.tv_password);
        mLoginView = (TextView) view.findViewById(R.id.bt_login);
        mRegisterView = (TextView) view.findViewById(R.id.tv_register);
        mResetView = (TextView) view.findViewById(R.id.tv_reset_psw);

        requestQueue = Volley.newRequestQueue(getActivity());
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

    /**
     * 记住邮箱帐号
     */
    private void saveEmail() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("email", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", strEmail);
        editor.commit();
    }

    /**
     * 获得保存的邮箱帐号
     *
     * @return
     */
    private String getEmail() {
        return mContext.getSharedPreferences("email", 0).getString("email", "");
    }

    @Override
    public void onClick(View view) {
        ShareSDK.initSDK(getActivity());
        switch (view.getId()) {
            case R.id.bt_login:
                hideInputMethod();
                String password = mPswView.getText().toString();
                if (TextUtils.isEmpty(strEmail)) {
                    Toast.makeText(getActivity(), "邮箱不能为空哦！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity(), "密码不能为空哦!", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveEmail();
                new MyTask().execute();
                break;
            case R.id.tv_register:
                RegisterFragment fragment = RegisterFragment.newInstance(mParam1, "");
                ((BaseActivity) getActivity()).navigationToFragment(fragment);
                break;
            case R.id.tv_reset_psw:
                break;
            case R.id.ll_back:
                Log.i("ll_back--->", "ll_back");
                ((BaseActivity) getActivity()).popToStack(this);
                break;
            case R.id.ll_weixin:
                Log.i("ll_weixin--->", "ll_weixin");
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                if (!wechat.isClientValid()) {
                    Toast.makeText(getActivity(), "没有安装微信客户端哦！", Toast.LENGTH_SHORT).show();
                    return;
                }
                authorize(wechat);
                break;
            case R.id.ll_qq:
                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                authorize(qq);
                break;
            case R.id.ll_weibo:
                //新浪微博
                Platform sina = ShareSDK.getPlatform(SinaWeibo.NAME);
                sina.removeAccount(true);
                authorize(sina);
                break;
            case R.id.ll_taobao:

                AlibabaSDK.getService(LoginService.class).showLogin(getActivity(), new LoginCallback() {
                    @Override
                    public void onSuccess(final Session session) {
                        Log.i("onSuccess-----", "onSuccess");
                        Log.i("getUserId", session.getUserId());
                        Log.i("getUserIcon", session.getUser().avatarUrl);
                        Log.i("getUserName", session.getUser().nick);
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("uid", session.getUserId());
                        params.put("provider", "taobao");
                        params.put("nick_name", session.getUser().nick);

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_THIRD_VERIFY_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Log.i("jsonObject--->", jsonObject.toString());
                                int flag = jsonObject.optInt("flag");

                                if (0 == flag) {
                                    JSONObject login_user = jsonObject.optJSONObject("user").optJSONObject("login_user");
                                    String email = login_user.optString("s_email");
                                    String nick = login_user.optString("s_nick_name");
                                    Log.i("0==flag", "0==flag");
                                    Map<String, String> registers = new HashMap<String, String>();
                                    registers.put("user[email]", email);
                                    registers.put("user[nick_name]", session.getUser().nick);
                                    registers.put("user[uid]", session.getUserId());
                                    registers.put("user[provider]", "taobao");
                                    registers.put("profile_image", session.getUser().avatarUrl);

                                    new RegisterTask().execute(registers);
                                } else {
                                    Log.i("flag==1", "flag==1");
                                    JSONObject userJson = jsonObject.optJSONObject("user");
                                    String token = userJson.optJSONObject("user_tokens").optString("token");
                                    JSONArray userArray = userJson.optJSONArray("users");
                                    if (userArray != null && userArray.length() > 0) {
                                        JSONObject user = userArray.optJSONObject(0);
                                        String nick = user.optString("nick_name");
                                        int id = user.optInt("id");
                                        String email = user.optString("email");
                                        User u = new User(id, email, token, nick, null);
                                        Log.i("LoginFragment----onRegisterOrLoginSuccess---->", "onRegisterOrLoginSuccess");
                                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(u, LoginFragment.this);
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.i("volleyError--->", volleyError.getMessage());
                            }
                        });
                        requestQueue.add(request);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.i("onFailure---->", "onFailure");
                    }
                });
                break;
            case R.id.emailDelete:
                mEmailView.setText("");
                break;
            default:
                break;
        }
    }

    private class MyTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "登陆中");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String str = null;
            try {
                String email = mEmailView.getText().toString();
                String password = mPswView.getText().toString();
                str = executeHttpPost(email, password);
            } catch (Exception e) {
            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mDialog != null)
                mDialog.dismiss();
            try {
                JSONObject json = new JSONObject(s);
                String msg = json.optString("message");
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    return;
                }
                JSONArray userArr = json.optJSONArray("users");
                for (int i = 0; i < userArr.length(); i++) {
                    JSONObject userObj = userArr.optJSONObject(i);
                    int id = userObj.optInt("id");
                    String email = userObj.optString("email");
                    String nick = userObj.optString("nick_name");
                    String token = json.optJSONObject("user_tokens").optString("token");
                    User user = new User(id, email, token, nick, null);

                    ToolUtils.setLog("要刷新登录了");
                    SharedPreferencesUtil.saveUser(getActivity(), user);
                    Intent intent = new Intent(ZhaiDou.IntentRefreshLoginTag);
                    getActivity().sendBroadcast(intent);

                    mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, LoginFragment.this);
                }

            } catch (Exception e) {

            }

        }
    }

    public String executeHttpPost(String email, String psw) throws Exception {
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_LOGIN_URL);

            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();


            parameters.add(new BasicNameValuePair("user_token[email]", email));
            parameters.add(new BasicNameValuePair("user_token[password]", psw));

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
        if (plat == null) {
            return;
        }
//        if(plat.isValid()) {
//            String userId = plat.getDb().getUserId();
//            String username = plat.getDb().getUserName();
//            String token = plat.getDb().getToken();
//            if (userId != null) {
//                Log.i("userId---------->",userId+"");
//                Log.i("username---------->",username+"");
//                Log.i("token---------->",token+"");
////                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
////                login(plat.getName(), userId, null);
//                return;
//            }
//        }
        plat.setPlatformActionListener(this);
        //关闭SSO授权
        plat.SSOSetting(true);
        plat.showUser(null);
    }

    @Override
    public void onComplete(final Platform platform, int i, final HashMap<String, Object> stringObjectHashMap) {
        String plat = platform.getName();
        final String provider = plat.equals("QQ") ? "tqq" : plat.equals("SinaWeibo") ? "weibo" : "weixin";
        Log.i("getUserId", platform.getDb().getUserId());
        Log.i("getUserIcon", platform.getDb().getUserIcon());
        Log.i("getUserName", platform.getDb().getUserName());
        Log.i("platform.getId()---------------->", platform.getId() + "");
        Set<String> keys = stringObjectHashMap.keySet();
        for (String s : keys) {
            Log.i("key-------->" + s, "  value------------>" + stringObjectHashMap.get(s));
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", platform.getDb().getUserId());
        params.put("provider", provider);
        params.put("nick_name", platform.getDb().getUserName());


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_THIRD_VERIFY_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int flag = jsonObject.optInt("flag");

                if (0 == flag) {
                    JSONObject login_user = jsonObject.optJSONObject("user").optJSONObject("login_user");
                    String email = login_user.optString("s_email");
                    String nick = login_user.optString("s_nick_name");
                    Log.i("0==flag", "0==flag");
                    Map<String, String> registers = new HashMap<String, String>();
                    registers.put("user[email]", email);
                    registers.put("user[nick_name]", platform.getDb().getUserName());
                    if ("weixin".equalsIgnoreCase(provider)) {
                        registers.put("user[uid]", stringObjectHashMap.get("unionid") + "");
                    } else {
                        registers.put("user[uid]", platform.getDb().getUserId());
                    }
                    registers.put("user[provider]", provider);
                    registers.put("user[agreed]", true + "");
                    if ("tqq".equalsIgnoreCase(provider)) {//http://www.zhaidou.com/uploads/user/avatar/77069/thumb_f713f712d202b1ecab67497877401835.png
                        registers.put("profile_image", "http://www.zhaidou.com/uploads/user/avatar/77069/thumb_f713f712d202b1ecab67497877401835.png");
                    } else {
                        registers.put("profile_image", platform.getDb().getUserIcon());
                    }

                    new RegisterTask().execute(registers);
                } else {
                    Log.i("flag==1", "flag==1");
                    JSONObject userJson = jsonObject.optJSONObject("user");
                    String token = userJson.optJSONObject("user_tokens").optString("token");
                    JSONArray userArray = userJson.optJSONArray("users");
                    if (userArray != null && userArray.length() > 0) {
                        JSONObject user = userArray.optJSONObject(0);
                        String nick = user.optString("nick_name");
                        int id = user.optInt("id");
                        String email = user.optString("email");
                        User u = new User(id, email, token, nick, null);
                        Log.i("LoginFragment----onRegisterOrLoginSuccess---->", mRegisterOrLoginListener == null ? "null" : mRegisterOrLoginListener.toString());
                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(u, LoginFragment.this);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        requestQueue.add(request);
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Log.i("platform----->", platform.getName() + "---" + i + throwable.getMessage().toString());
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Log.i("onCancel----->", platform.getName() + "---" + i);
    }

    public void setRegisterOrLoginListener(RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener) {
        this.mRegisterOrLoginListener = mRegisterOrLoginListener;
    }

    public void setBackClickListener(BackClickListener backClickListener) {
        this.backClickListener = backClickListener;
    }

    public interface BackClickListener {
        public void onBackClick(Fragment fragment);
    }


    private class RegisterTask extends AsyncTask<Map<String, String>, Void, String> {
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            String s = null;
            try {
                s = NativeHttpUtil.post(ZhaiDou.USER_REGISTER_URL, null, maps[0]);
            } catch (Exception e) {
                Log.i("e--->", e.getMessage());
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject json = new JSONObject(s);
                JSONObject userJson = json.optJSONObject("user");
                int id = userJson.optInt("id");
                String email = userJson.optString("email");
                String token = userJson.optString("authentication_token");
                String avatar = userJson.optJSONObject("avatar").optString("url");
                String nick = userJson.optString("nick_name");
                User user = new User(id, email, token, nick, avatar);
                mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, LoginFragment.this);
            } catch (Exception e) {
            }
        }
    }

    private class RegisterThirdTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
