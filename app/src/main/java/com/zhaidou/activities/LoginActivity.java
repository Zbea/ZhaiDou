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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
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
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NativeHttpUtil;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    private RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener;

    //dialog相关
    private CustomEditText mCodeView, mPhoneView;
    private TextView mGetCode;
    private int initTime = 0;
    private Timer mTimer;

    private static final int SHOW_DIALOG = 1;
    private static final int CLOSE_DIALOG = 2;
    private int flags;
    public int index;
    RequestQueue requestQueue;
    private DialogUtils mDialogUtils;
    private boolean validate_phone = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    User u = (User) msg.obj;//id,email,token,nick,null
                    Log.i("handleMessage------------>", u.toString());
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
        findViewById(R.id.back_btn).setOnClickListener(this);
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
        SharedPreferences sharedPreferences = getSharedPreferences("email", 0);
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
        return getSharedPreferences("email", 0).getString("email", "");
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
                } else if (password.length() > 16) {
                    ToolUtils.setToast(getApplicationContext(), "抱歉,您输入的密码过长");
                    mPswView.setShakeAnimation();
                    return;
                } else if (password.length() < 6) {
                    ToolUtils.setToast(getApplicationContext(), "抱歉,您输入的密码过短");
                    mPswView.setShakeAnimation();
                }
                saveEmail();
                final Map<String, String> params = new HashMap<String, String>();
                params.put("user_token[email]", strEmail);
                params.put("user_token[password]", password);
                mDialog = CustomLoadingDialog.setLoadingDialog(LoginActivity.this, "登陆中");
                ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (mDialog != null)
                            mDialog.dismiss();
                        if (jsonObject != null) {
                            String token = jsonObject.optJSONObject("user_tokens").optString("token");
                            validate_phone = jsonObject.optJSONArray("users").optJSONObject(0).optBoolean("validate_phone");
                            if (!validate_phone) {
                                showVerifyDialog(token);
                            } else {
                                String msg = jsonObject.optString("message");
                                if (!TextUtils.isEmpty(msg)) {
                                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                JSONArray userArr = jsonObject.optJSONArray("users");
                                for (int i = 0; i < userArr.length(); i++) {
                                    JSONObject userObj = userArr.optJSONObject(i);
                                    int id = userObj.optInt("id");
                                    String email = userObj.optString("email");
                                    String nick = userObj.optString("nick_name");
                                    User user = new User(id, email, token, nick, null);
                                    mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, null);
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

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
            case R.id.back_btn:
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
                                    JSONObject userJson = jsonObject.optJSONObject("user");
                                    String token = userJson.optJSONObject("user_tokens").optString("token");
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
            default:
                break;
        }
    }

    private void authorize(Platform plat) {
        Log.i("Platform----->", plat.getName());
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
        if ("SinaWeibo".equalsIgnoreCase(plat.getName())) {
            plat.SSOSetting(true);
        } else {
            plat.SSOSetting(false);
        }
        plat.showUser(null);
    }

    /**
     * 验证手机号
     *
     * @param token
     */
    private void showVerifyDialog(final String token) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_phone_verify, null);
        final Dialog mDialog = new Dialog(this, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();

        mCodeView = (CustomEditText) view.findViewById(R.id.tv_code);
        mPhoneView = (CustomEditText) view.findViewById(R.id.tv_phone);
        mGetCode = (TextView) view.findViewById(R.id.bt_getCode);
        mGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVerifyCode(mPhoneView.getText().toString());
            }
        });
        TextView okTv = (TextView) view.findViewById(R.id.bt_ok);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = mCodeView.getText().toString();
                String phone = mPhoneView.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    mPhoneView.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(code)) {
                    mCodeView.setShakeAnimation();
                    return;
                }
                if (ToolUtils.isPhoneOk(phone)) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("phone", phone);
                    params.put("vcode", code);
                    ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_BINE_PHONE_URL, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            int status = jsonObject.optInt("status");
                            if (201 == status) {
                                mDialog.dismiss();
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                JSONObject userObj = jsonObject.optJSONObject("user");
                                int id = userObj.optInt("id");
                                String phone = userObj.optString("phone");
                                String nick = userObj.optString("nick_name");
                                String email = userObj.optString("email");
                                String avatar = userObj.optJSONObject("avatar").optString("mobile_icon");
                                validate_phone = true;
                                User user = new User(id, email, token, nick, avatar);
                                mRegisterOrLoginListener.onRegisterOrLoginSuccess(user, null);
                            } else {
                                String message = jsonObject.optString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
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

                } else {
                    ToolUtils.setToast(getApplicationContext(), "抱歉,无效手机号码");
                }
            }
        });
    }

    /**
     * 获得验证码
     *
     * @param phone 手机号码
     */
    private void getVerifyCode(String phone) {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_REGISTER_VERIFY_CODE_URL + phone + "&flag=1", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("LoginActivity.onResponse------->" + jsonObject.toString());
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status == 201) {
                    codeTimer();
                }
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication) getApplication()).mRequestQueue.add(request);
    }

    /**
     * 验证码倒计时事件处理
     */
    private void codeTimer() {
        initTime = ZhaiDou.VERFIRY_TIME;
        mGetCode.setBackgroundResource(R.drawable.btn_no_click_selector);
        mGetCode.setText("重新获取(" + initTime + ")");
        mGetCode.setClickable(false);
        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);
    }

    /**
     * 倒计时
     */
    class MyTimer extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initTime = initTime - 1;
                    mGetCode.setText("重新获取(" + initTime + ")");
                    if (initTime <= 0) {
                        if (mTimer != null)
                            mTimer.cancel();
                        mGetCode.setText("获取验证码");
                        mGetCode.setBackgroundResource(R.drawable.btn_green_click_bg);
                        mGetCode.setClickable(true);
                    }
                }
            });
        }
    }


    @Override
    public void onComplete(final Platform platform, int i, final HashMap<String, Object> stringObjectHashMap) {
        mHandler.sendEmptyMessage(SHOW_DIALOG);
        Log.i("onComplete----->", platform.getName() + "---" + i);
        Log.i("stringObjectHashMap", stringObjectHashMap.toString());
        String plat = platform.getName();
        final String provider = plat.equals("QQ") ? "tqq" : plat.equals("SinaWeibo") ? "weibo" : "weixin";
        Log.i("getUserId", platform.getDb().getUserId());
//        Log.i("getUserIcon","");//platform.getDb().getUserIcon()
        Log.i("getUserName", platform.getDb().getUserName());
        Map<String, String> params = new HashMap<String, String>();
        if ("weixin".equalsIgnoreCase(provider)) {
            params.put("uid", stringObjectHashMap.get("unionid") + "");
        } else {
            params.put("uid", platform.getDb().getUserId());
        }
        params.put("provider", provider);

        params.put("nick_name", platform.getDb().getUserName());

        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_THIRD_VERIFY_URL, params, new Response.Listener<JSONObject>() {
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
                    registers.put("user[nick_name]", nick);
                    if ("weixin".equalsIgnoreCase(provider)) {
                        registers.put("user[uid]", stringObjectHashMap.get("unionid") + "");
                    } else {
                        registers.put("user[uid]", platform.getDb().getUserId());
                    }
                    registers.put("user[provider]", provider);
                    Log.i("provider---------------->", provider);
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
                        validate_phone = user.optBoolean("validate_phone");
                        User u = new User(id, email, token, nick, null);
                        mRegisterOrLoginListener.onRegisterOrLoginSuccess(u, null);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(LoginActivity.this, "网络状况不太好哦", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRegisterOrLoginSuccess(final User user, Fragment fragment) {
        if (mDialog != null)
            mDialog.dismiss();
        if (!validate_phone) {
            mDialogUtils.showVerifyDialog(new DialogUtils.VerifyCodeListener() {
                @Override
                public void onVerify(String phone) {
                    System.out.println("LoginActivity.onVerify------------>" + phone);
                    getVerifyCode(phone);
                }
            }, new DialogUtils.BindPhoneListener() {
                @Override
                public void onBind(String phone, String verifyCode, final Dialog mDialog) {
                    System.out.println("LoginActivity.onPositive-------->");
                    if (ToolUtils.isPhoneOk(phone)) {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("phone", phone);
                        params.put("vcode", verifyCode);
                        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.USER_LOGIN_BINE_PHONE_URL, params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                int status = jsonObject.optInt("status");
                                String msg = jsonObject.optString("message");
                                if (201 == status) {
                                    mDialog.dismiss();
                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                    JSONObject userObj = jsonObject.optJSONObject("user");
                                    int id = userObj.optInt("id");
                                    String phone = userObj.optString("phone");
                                    String nick = userObj.optString("nick_name");
                                    String email = userObj.optString("email");
                                    String avatar = userObj.optJSONObject("avatar").optString("mobile_icon");
                                    String token = userObj.optString("authentication_token");
                                    User user1 = new User(id, email, token, nick, avatar);
                                    user1.setPhone(phone);
                                    Message message = new Message();
                                    message.obj = user1;
                                    message.what = 0;
                                    mHandler.sendMessage(message);
                                } else {
                                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                                headers.put("SECAuthorization", user.getAuthentication_token());
                                return headers;
                            }
                        };
                        ((ZDApplication) getApplication()).mRequestQueue.add(request);

                    } else {
                        ToolUtils.setToast(getApplicationContext(), "抱歉,无效手机号码");
                    }
                }
            });
        } else {
            System.out.println("LoginActivity.onRegisterOrLoginSuccess---else--->" + user.toString());
            Message message = new Message();
            message.obj = user;
            message.what = 0;
            mHandler.sendMessage(message);
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
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
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
}
