package com.zhaidou.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.CallbackContext;
import com.alibaba.sdk.android.login.LoginService;
import com.alibaba.sdk.android.login.callback.LoginCallback;
import com.alibaba.sdk.android.session.model.Session;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.AccountManage;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.NativeHttpUtil;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by wangclark on 15/7/16.
 */
public class LoginActivity extends FragmentActivity implements View.OnClickListener, PlatformActionListener, RegisterFragment.RegisterOrLoginListener {

    private TextView mRegisterView, mResetView;
    private CustomEditText mEmailView;
    private CustomEditText mPswView;
    private String strEmail;
    private TextView headTitle;
    private TextView mLoginView;
    private Dialog mDialog;
    protected InputMethodManager inputMethodManager;
    private RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener;

    //dialog相关
    private CustomEditText mCodeView, mPhoneView;
    private TextView mGetCode;
    private int initTime = 0;

    private static final int SHOW_DIALOG = 1;
    private static final int CLOSE_DIALOG = 2;
    private int flags;
    public int index;
    RequestQueue requestQueue;
    private DialogUtils mDialogUtils;
    private boolean validate_phone = false;
    private String token;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    AccountManage.getInstance().setConflict(false);
                    User u = (User) msg.obj;//id,email,token,nick,null
                    SharedPreferencesUtil.saveUser(getApplicationContext(), u);
                    ToolUtils.setLog("要刷新登录了");

                    Intent intent1 = new Intent(ZhaiDou.IntentRefreshLoginTag);
                    sendBroadcast(intent1);

                    if (flags == 3) {
                        Intent intent = new Intent();
                        setResult(5001, intent);
                        finish();//此处一定要调用finish()方法
                        return;
                    }

                    if (flags != 1) {
                        Intent intent = new Intent();
                        intent.putExtra("id", u.getId());
                        intent.putExtra("email", u.getEmail());
                        intent.putExtra("token", u.getAuthentication_token());
                        intent.putExtra("nick", u.getNickName());
                        setResult(2000, intent);
                    }
                    finish();//此处一定要调用finish()方法
                    break;
                case SHOW_DIALOG:
                    if (mDialog == null)
                        mDialog = CustomLoadingDialog.setLoadingDialog(LoginActivity.this, "登陆中");
                    mDialog.show();
                    break;
                case CLOSE_DIALOG:
                    if (mDialog != null)
                        mDialog.dismiss();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_login);

        flags = getIntent().getFlags();

        strEmail = getEmail();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        headTitle = (TextView) findViewById(R.id.title_tv);
        headTitle.setText(R.string.title_login);

        mEmailView = (CustomEditText) findViewById(R.id.tv_email);
        mEmailView.setText(strEmail);
        mPswView = (CustomEditText) findViewById(R.id.tv_password);
        mLoginView = (TextView) findViewById(R.id.bt_login);
        mRegisterView = (TextView) findViewById(R.id.tv_register);
        mResetView = (TextView) findViewById(R.id.tv_reset_psw);

        requestQueue = Volley.newRequestQueue(this);
        mLoginView.setOnClickListener(this);
        mRegisterView.setOnClickListener(this);
        mResetView.setOnClickListener(this);
        findViewById(R.id.ll_back).setOnClickListener(this);
        findViewById(R.id.ll_qq).setOnClickListener(this);
        findViewById(R.id.ll_weixin).setOnClickListener(this);
        findViewById(R.id.ll_weibo).setOnClickListener(this);
        findViewById(R.id.ll_taobao).setOnClickListener(this);

        mDialogUtils = new DialogUtils(this);
        setRegisterOrLoginListener(this);
    }

    /**
     * 记住邮箱帐号
     */
    private void saveEmail() {
        SharedPreferencesUtil.saveData(this, "phone", strEmail);
    }

    /**
     * 获得保存的邮箱帐号
     *
     * @return
     */
    private String getEmail() {
        return (String) SharedPreferencesUtil.getData(this, "phone", "");
    }

    @Override
    public void onClick(View view) {
        ShareSDK.initSDK(this);
        switch (view.getId()) {
            case R.id.bt_login:
                strEmail = mEmailView.getText().toString();
                String password = mPswView.getText().toString();
                if (TextUtils.isEmpty(strEmail)) {
                    mEmailView.setShakeAnimation();
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    mPswView.setShakeAnimation();
                    return;
                }
//                else if (password.length() > 16) {
//                    ToolUtils.setToast(getApplicationContext(), "抱歉,您输入的密码过长");
//                    mPswView.setShakeAnimation();
//                    return;
//                } else if (password.length() < 6) {
//                    ToolUtils.setToast(getApplicationContext(), "抱歉,您输入的密码过短");
//                    mPswView.setShakeAnimation();
//                }
                saveEmail();
                final Map<String, String> params = new HashMap<String, String>();
                params.put("email", strEmail);
                params.put("password", password);

                mDialog = CustomLoadingDialog.setLoadingDialog(LoginActivity.this, "登陆中");
                final ZhaiDouRequest request = new ZhaiDouRequest(LoginActivity.this,Request.Method.POST, ZhaiDou.USER_LOGIN_URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (jsonObject != null) {
                            JSONObject dataObj = jsonObject.optJSONObject("data");
                            String message = jsonObject.optString("message");
                            if (jsonObject.isNull("code")) {
                                String token = dataObj.optJSONObject("user_tokens").optString("token");
                                validate_phone = dataObj.optJSONArray("users").optJSONObject(0).optBoolean("validate_phone");
                                JSONArray userArr = dataObj.optJSONArray("users");
                                for (int i = 0; i < userArr.length(); i++) {
                                    JSONObject userObj = userArr.optJSONObject(i);
                                    int id = userObj.optInt("id");
                                    String email = userObj.optString("email");
                                    String nick = userObj.optString("nick_name");
                                    User user = new User(id, email, token, nick, null);
//                                    loginToEaseServer(user);
                                    mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, null);
                                }
                            } else {
                                if (mDialog != null)
                                    mDialog.dismiss();
                                String msg = dataObj.optString("message");
                                Toast.makeText(LoginActivity.this, TextUtils.isEmpty(msg) ? message : msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (mDialog != null)
                            mDialog.dismiss();
                        if (volleyError.getMessage() != null && volleyError.getMessage().contains("authentication")) {
                            Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                requestQueue.add(request);
                break;
            case R.id.tv_register:
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), 200);
                break;
            case R.id.tv_reset_psw:
                Intent intent = new Intent(getApplicationContext(), AccountFindPwdActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_back:
                finish();
                break;
            case R.id.ll_weixin:
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                if (!wechat.isClientValid()) {
                    Toast.makeText(this, "没有安装微信客户端哦！", Toast.LENGTH_SHORT).show();
                    return;
                }
                authorize(wechat);
                break;
            case R.id.ll_qq:
                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                authorize(qq);
                ToolUtils.setLog("开始qq注册");
                break;
            case R.id.ll_weibo:
                //新浪微博
                Platform sina = ShareSDK.getPlatform(SinaWeibo.NAME);
                sina.removeAccount(true);
                authorize(sina);
                break;
            case R.id.ll_taobao:
                AlibabaSDK.getService(LoginService.class).showLogin(LoginActivity.this, new LoginCallback() {
                    @Override
                    public void onSuccess(final Session session) {
                        thirdPartyVerify("taobao", session.getUserId(), session.getUser().nick, session.getUser().avatarUrl);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.i("onFailure---->", "onFailure");
                    }
                });
                break;
            default:
                break;
        }
    }

    private void thirdPartyVerify(final String tag, final String userId, String nick, final String avatarUrl) {
        System.out.println("LoginActivity.thirdPartyVerify--------");
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", userId);
        params.put("provider", tag);
        params.put("nick_name", nick);
        ZhaiDouRequest request = new ZhaiDouRequest(LoginActivity.this,Request.Method.POST, ZhaiDou.USER_LOGIN_THIRD_VERIFY_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("LoginActivity.thirdPartyVerify----jsonObject--->", jsonObject.toString());
                JSONObject dataObj = jsonObject.optJSONObject("data");
                int flag = dataObj.optInt("flag");

                if (0 == flag) {
                    JSONObject login_user = dataObj.optJSONObject("user").optJSONObject("login_user");
                    String email = login_user.optString("s_email");
                    String nick1 = login_user.optString("s_nick_name");
                    Log.i("0==flag", "0==flag");
                    Map<String, String> registers = new HashMap<String, String>();
                    registers.put("email", email);
                    registers.put("nick_name", nick1);
                    registers.put("uid", userId);
                    registers.put("provider", tag);
                    registers.put("profile_image", avatarUrl);
                    thirdPartyRegisterTask(registers);
                } else {
                    JSONObject userJson = dataObj.optJSONObject("user");
                    token = userJson.optJSONObject("user_tokens").optString("token");
                    JSONArray userArray = userJson.optJSONArray("users");
                    if (userArray != null && userArray.length() > 0) {
                        JSONObject user = userArray.optJSONObject(0);
                        String nick = user.optString("nick_name");
                        int id = user.optInt("id");
                        String email = user.optString("email");
                        validate_phone = user.optBoolean("validate_phone");
                        User u = new User(id, email, token, nick, null);
                        Log.i("LoginFragment----onRegisterOrLoginSuccess---->", user.toString());
                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(u, null);
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

    private void bingPhoneTask(String phone, String verifyCode, final Dialog mDialog, final String token) {
        JSONObject params = new JSONObject();
        try
        {
            params.put("phone", phone);
            params.put("vcode", verifyCode);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_BINE_PHONE_URL, params, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                ToolUtils.setLog(jsonObject.toString());
                JSONObject dataObj = jsonObject.optJSONObject("data");
                if(dataObj!=null){
                    int status = dataObj.optInt("status");
                    if (201 == status) {
                        mDialog.dismiss();
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        JSONObject userObj = dataObj.optJSONObject("user");
                        int id = userObj.optInt("id");
                        String phone = userObj.optString("phone");
                        String nick = userObj.optString("nick_name");
                        String email = userObj.optString("email");
                        String avatar = userObj.optJSONObject("avatar").optString("mobile_icon");
                        validate_phone = true;
                        User user = new User(id, email, token, nick, avatar);
                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, null);
                    } else {
                        String message = dataObj.optString("message");
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError){
                if (mDialog != null)
                    mDialog.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", getApplicationContext().getResources().getString(R.string.app_versionName));
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    private void authorize(Platform plat) {
        Log.i("Platform----->", plat.getName());
        if (plat == null) {
            return;
        }
        if (plat.isValid()) {
            plat.removeAccount();
        }
        plat.setPlatformActionListener(this);
        //关闭SSO授权
        if ("SinaWeibo".equalsIgnoreCase(plat.getName())) {
            plat.SSOSetting(true);
        } else {
            plat.SSOSetting(false);
        }
        ToolUtils.setLog("注册");
        plat.showUser(null);
    }

    /**
     * 获得验证码
     *
     * @param phone   手机号码
     * @param mDialog
     */
    private void getVerifyCode(String phone, final Dialog mDialog) {
        if (TextUtils.isEmpty(phone)||phone.length()!=11){
            Toast.makeText(LoginActivity.this,"请输入正确的手机号码",Toast.LENGTH_SHORT).show();
        }
        ZhaiDouRequest request = new ZhaiDouRequest(LoginActivity.this,ZhaiDou.USER_REGISTER_VERIFY_CODE_URL + "?phone=" + phone + "&flag=1", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject dataObj = jsonObject.optJSONObject("data");
                String message = jsonObject.optString("message");
                if (jsonObject.isNull("code")) {
                    String msg = dataObj.optString("message");
                    int status = dataObj.optInt("status");
                    if (status == 201) {
                        mDialogUtils.codeTimer();
                        return;
                    } else {
                        mDialog.findViewById(R.id.iv_close).setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, TextUtils.isEmpty(msg) ? message : msg, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication) getApplication()).mRequestQueue.add(request);
    }

    @Override
    public void onComplete(final Platform platform, int i, final HashMap<String, Object> stringObjectHashMap) {
        mHandler.sendEmptyMessage(SHOW_DIALOG);
        ToolUtils.setLog("拿到第三方注册数据");
        Log.i("onComplete----->", platform.getName() + "---" + i);
        Log.i("stringObjectHashMap", stringObjectHashMap.toString());
        String plat = platform.getName();
        final String provider = plat.equals("QQ") ? "tqq" : plat.equals("SinaWeibo") ? "weibo" : "weixin";
        Log.i("getUserId", platform.getDb().getUserId());

        if ("weixin".equalsIgnoreCase(provider)) {
            thirdPartyVerify("weixin", stringObjectHashMap.get("unionid") + "", platform.getDb().getUserName(), String.valueOf(stringObjectHashMap.get("headimgurl")));
        } else if ("tqq".equalsIgnoreCase(provider)) {
            thirdPartyVerify(provider, platform.getDb().getUserId(), platform.getDb().getUserName(), String.valueOf(stringObjectHashMap.get("figureurl_qq_2")));
        } else if ("weibo".equalsIgnoreCase(provider)) {
            thirdPartyVerify(provider, platform.getDb().getUserId(), platform.getDb().getUserName(), String.valueOf(stringObjectHashMap.get("avatar_hd")));
        } else {
            thirdPartyVerify(provider, platform.getDb().getUserId(), platform.getDb().getUserName(), platform.getDb().getUserIcon());
        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Log.i("platform----->", platform.getName() + "---" + i );
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Log.i("onCancel----->", platform.getName() + "---" + i);
    }

    public void setRegisterOrLoginListener(RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener) {
        this.mRegisterOrLoginListener = mRegisterOrLoginListener;
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
                mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, null);
            } catch (Exception e) {
            }
        }
    }

    private void thirdPartyRegisterTask(Map<String, String> params) {
        System.out.println("LoginActivity.thirdPartyRegisterTask");
        ZhaiDouRequest request = new ZhaiDouRequest(LoginActivity.this,Request.Method.POST, ZhaiDou.USER_REGISTER_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("LoginActivity.onResponse-----》"+jsonObject.toString());
                if (jsonObject != null) {
                    System.out.println("jsonObject = " + jsonObject);
                    int status = jsonObject.optInt("status");
                    String message = jsonObject.optString("message");
                    if (status == 200) {
                        ToolUtils.setLog("注册成功了");
                        JSONObject dataObj = jsonObject.optJSONObject("data");
                        JSONArray errMsg = jsonObject.optJSONArray("message");
                        if (errMsg != null) {
                            Toast.makeText(LoginActivity.this, errMsg.optString(0), Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject userJson = dataObj.optJSONObject("user");
                        int id = userJson.optInt("id");
                        String email = userJson.optString("email");
                        String token = userJson.optString("authentication_token");
//                    String avatar = userJson.optJSONObject("avatar").optString("url","");
                        String nick = userJson.optString("nick_name");
                        User user = new User(id, email, token, nick, "");
//                        loginToEaseServer(user);
                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, null);
                    } else {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication) getApplication()).mRequestQueue.add(request);
    }

    @Override
    public void onRegisterOrLoginSuccess(final User user, Fragment fragment) {
        if (mDialog != null)
            mDialog.dismiss();
        if (!validate_phone) {
            mDialogUtils.showVerifyDialog(new DialogUtils.VerifyCodeListener() {
                @Override
                public void onVerify(String phone, Dialog mDialog) {
                    getVerifyCode(phone, mDialog);
                }
            }, new DialogUtils.BindPhoneListener() {
                @Override
                public void onBind(String phone, String verifyCode, final Dialog mDialog) {
                    bingPhoneTask(phone, verifyCode, mDialog, user.getAuthentication_token());
                }
            }, true);
        } else {
            loginToEaseServer(user);
//            Message message = new Message();
//            message.obj = user;
//            message.what = 0;
//            mHandler.sendMessage(message);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 2000:
                if (data != null) {
                    int id = data.getIntExtra("id", -1);
                    String email = data.getStringExtra("email");
                    String token = data.getStringExtra("token");
                    String nick = data.getStringExtra("nick");
                    User user = new User(id, email, token, nick, null);
                    Message message = new Message();
                    message.what = 0;
                    message.obj = user;
                    mHandler.sendMessage(message);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getResources().getString(R.string.title_login));
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getResources().getString(R.string.title_login));
        MobclickAgent.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);
    }

    private void loginToEaseServer(final User user){
        EaseUtils.login(user);
        Message message = new Message();
        message.obj = user;
        message.what = 0;
        mHandler.sendMessage(message);
    }
    public String string2MD5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }
}
