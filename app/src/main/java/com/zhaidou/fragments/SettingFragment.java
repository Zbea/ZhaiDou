package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomVersionUpdateDialog;
import com.zhaidou.model.User;
import com.zhaidou.utils.NetService;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONObject;

public class SettingFragment extends BaseFragment implements View.OnClickListener,ProfileFragment.ProfileListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int CLEAR_USER_DATA=0;

    private String mParam1;
    private String mParam2;

    ProfileFragment mProfileFragment;

    SharedPreferences mSharedPreferences;
    RequestQueue requestQueue;
    private ProfileListener profileListener;
    private Dialog mDialog;
    private boolean isNetState;
    private Context mContext;
    private String serverName;
    private String serverInfo;
    private String serverUrl;
    private int serverCode;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CLEAR_USER_DATA:
                    SharedPreferencesUtil.clearUser(getActivity());

                    Intent intent=new Intent(ZhaiDou.IntentRefreshLoginExitTag);
                    getActivity().sendBroadcast(intent);

                    ((MainActivity)getActivity()).logout(SettingFragment.this);
                    ((MainActivity)getActivity()).CartTip(0);
                    break;
                case 1:
                    serverCode = parseJosn(msg.obj.toString());
                    ToolUtils.setLog(" ZDApplication.localVersionCode:" + ZDApplication.localVersionCode);
                    if (serverCode > ZDApplication.localVersionCode) {
                        CustomVersionUpdateDialog customVersionUpdateDialog = new CustomVersionUpdateDialog(mContext, serverName,serverUrl);
                        customVersionUpdateDialog.checkUpdateInfo();
                    }
                    else
                    {
                        ToolUtils.setToast(mContext,"当前版本为最新版本");
                    }
                    break;
            }
        }
    };

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SettingFragment() {
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
        View view=inflater.inflate(R.layout.fragment_setting, container, false);

        mContext=getActivity();

        LinearLayout versionBtn=(LinearLayout)view.findViewById(R.id.ll_version);
        versionBtn.setOnClickListener(this);

        view.findViewById(R.id.rl_back).setOnClickListener(this);
        view.findViewById(R.id.ll_recommend).setOnClickListener(this);
        view.findViewById(R.id.ll_profile).setOnClickListener(this);
        view.findViewById(R.id.ll_psw_change).setOnClickListener(this);
        view.findViewById(R.id.ll_competition).setOnClickListener(this);
        view.findViewById(R.id.ll_bbs_question).setOnClickListener(this);
        view.findViewById(R.id.ll_collocation).setOnClickListener(this);
        view.findViewById(R.id.ll_add_v).setOnClickListener(this);
        view.findViewById(R.id.ll_version).setOnClickListener(this);
        view.findViewById(R.id.ll_award_history).setOnClickListener(this);
        view.findViewById(R.id.ll_about).setOnClickListener(this);
        view.findViewById(R.id.bt_logout).setOnClickListener(this);
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(getActivity());
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_back:
                ((MainActivity)getActivity()).popToStack(SettingFragment.this);
                break;
            case R.id.ll_profile:
                mProfileFragment=ProfileFragment.newInstance("","");
                mProfileFragment.setProfileListener(this);
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(mProfileFragment);
                break;
            case R.id.ll_psw_change:
                ModifyPswFragment modifyPswFragment=new ModifyPswFragment();
                ((MainActivity)getActivity()).navigationToFragment(modifyPswFragment);
                break;
            case R.id.ll_competition:
                WebViewFragment webViewFragment=WebViewFragment.newInstance("http://www.zhaidou.com/competitions/current?zdclient=ios",true);
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(webViewFragment);
                break;
            case R.id.ll_bbs_question:
                break;
            case R.id.ll_collocation:
                ImageBgFragment fragment= ImageBgFragment.newInstance("豆搭教程");
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(fragment);
                break;
            case R.id.ll_recommend:
                SettingRecommendFragment settingRecommendFragment= SettingRecommendFragment.newInstance("","");
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(settingRecommendFragment);
                break;
            case R.id.ll_add_v:
                ImageBgFragment addVFragment= ImageBgFragment.newInstance("如何加V");
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(addVFragment);
                break;
            case R.id.ll_award_history:
                break;
            case R.id.ll_about:
                AboutFragment aboutFragment = AboutFragment.newInstance("","");
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(aboutFragment);
                break;
            case R.id.bt_logout:
//                mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"注销中");
                logout();
                break;
            case R.id.ll_version:
                if (NetworkUtils.isNetworkAvailable(mContext))
                {
                    getVersionServer();
                }
                else
                {
                    ToolUtils.setToast(mContext,"抱歉,网络连接失败");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取版本信息
     */
    private void getVersionServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = ZhaiDou.ApkUrl;
                String result = NetService.getHttpService(url);
                if (result != null) {
                    mHandler.obtainMessage(1, result).sendToTarget();
                }
            }
        }).start();
    }

    /**
     * 版本信息解析
     *
     * @param json
     * @return
     */
    private int parseJosn(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject!=null)
            {
                JSONObject object=jsonObject.optJSONObject("data");
                if(object!=null)
                serverName = object.optString("app_version");
                serverCode = object.optInt("code_version");
                serverUrl = object.optString("package_url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverCode;
    }

    public void logout(){
                mHandler.sendEmptyMessage(CLEAR_USER_DATA);

//        JsonObjectRequest request=new JsonObjectRequest(ZhaiDou.USER_LOGOUT_URL
//         ,new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject jsonObject) {
//                if (mDialog!=null) mDialog.dismiss();
//
//            }
//        },new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//
//            }
//        })        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError
//            {
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
//                return headers;
//            }
//        };
//        requestQueue.add(request);
    }

    @Override
    public void onProfileChange(User user) {
        profileListener.onProfileChange(user);
    }

    public void setProfileListener(ProfileListener profileListener) {
        this.profileListener = profileListener;
    }

    public interface ProfileListener{
        public void onProfileChange(User user);
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_setting));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_setting));
    }

}
