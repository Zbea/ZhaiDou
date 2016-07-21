package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.ProfileManage;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ProfileEditFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_TAG = "tag";
    private static final String PROFILE_ID = "profileId";
    private static final String ARG_TITLE = "title";
    private static final String ARG_PARAMS="map";

    private ProfileManage.TAG mTag;
    private String mProfileId;
    private String mTitle;
    private HashMap<String,String> mParams;

    private String token;

    private TextView tv_edit_msg, tv_done, tv_description, tv_length;
    private ImageView iv_cancel;
    private TextView mTitleView;

    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;

    private LinearLayout ll_input_msg;
    private RelativeLayout rl_description;

    private Dialog mDialog;

    public static ProfileEditFragment newInstance(ProfileManage.TAG tag, HashMap<String,String> map, String profileId, String title) {
        ProfileEditFragment fragment = new ProfileEditFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TAG, tag);
        args.putSerializable(ARG_PARAMS, map);
        args.putString(PROFILE_ID, profileId);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTag = (ProfileManage.TAG) getArguments().getSerializable(ARG_TAG);
            mParams = (HashMap<String, String>) getArguments().getSerializable(ARG_PARAMS);
            mProfileId = getArguments().getString(PROFILE_ID);
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        rl_description = (RelativeLayout) view.findViewById(R.id.rl_description);
        ll_input_msg = (LinearLayout) view.findViewById(R.id.ll_input_msg);
        mTitleView = (TextView) view.findViewById(R.id.tv_title);
        mTitleView.setText(mTitle);

        iv_cancel = (ImageView) view.findViewById(R.id.iv_cancel);
        tv_edit_msg = (EditText) view.findViewById(R.id.tv_edit_msg);
        if (mTitle.equals("手机号码")) {
            tv_edit_msg.setInputType(InputType.TYPE_CLASS_PHONE);
            tv_edit_msg.setMaxEms(11);
        }
        tv_edit_msg.setText(ProfileManage.TAG.NICK==mTag?mParams.get("nick_name"): mParams.get("mobile"));

        tv_description = (EditText) view.findViewById(R.id.tv_description);
        tv_length = (TextView) view.findViewById(R.id.tv_length);


        tv_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                tv_length.setText((75 - tv_description.getText().toString().length()) + "");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        if (ProfileManage.TAG.DESC==mTag) {
            ll_input_msg.setVisibility(View.GONE);
            rl_description.setVisibility(View.VISIBLE);
            tv_description.setText(mParams.get("description"));
            tv_length.setText((75 - (!TextUtils.isEmpty(tv_description.getText().toString()) ? tv_description.getText().toString().length() : 0)) + "");
        } else {
            ll_input_msg.setVisibility(View.VISIBLE);
            rl_description.setVisibility(View.GONE);
        }
        mRequestQueue = ZDApplication.newRequestQueue();
        mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token = mSharedPreferences.getString("token", null);
        view.findViewById(R.id.ll_back).setOnClickListener(this);
        view.findViewById(R.id.tv_done).setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ((ProfileFragment) getParentFragment()).popToStack();
                break;
            case R.id.iv_cancel:
                tv_edit_msg.setText("");
                break;
            case R.id.tv_done:
                if (ProfileManage.TAG.DESC==mTag) {//"description".equalsIgnoreCase(mParam1)
                    if (TextUtils.isEmpty(tv_description.getText().toString().trim())) {
                        ShowToast(mTitle + "不能为空");
                        return;
                    }
                    mParams.put("description",tv_description.getText().toString().trim());
                    UpdateUserInfo("description",mParams, mProfileId);
                } else if (TextUtils.isEmpty(tv_edit_msg.getText().toString().trim())) {
                    ShowToast(mTitle + "不能为空");
                    return;
                }
                if (ProfileManage.TAG.MOBILE==mTag) {
                    if (ToolUtils.isPhoneOk(tv_edit_msg.getText().toString())) {
                        hideInputMethod();
                        mParams.put("mobile",tv_edit_msg.getText().toString().trim());
                        UpdateUserInfo("mobile",mParams, mProfileId);
                    } else {
                        ToolUtils.setToast(getActivity(), "抱歉,手机号码格式输入不正确");
                    }
                } else if (ProfileManage.TAG.NICK==mTag){
                    if (tv_edit_msg.getText().toString().length() > 15) {
                        ShowToast("个人昵称不能超过15个字");
                        return;
                    }
                    hideInputMethod();
                    mParams.put("nick_name", tv_edit_msg.getText().toString().trim());
                    UpdateUserInfo("nick_name", mParams,mProfileId);
                }
                break;
        }
    }

    private void UpdateUserInfo(String type,HashMap<String,String> map, String id) {
        Object userId = SharedPreferencesUtil.getData(mContext, "userId", -1);
        System.out.println("id = " + id+"----------------"+userId);
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        Map<String, String> params = new HashMap<String, String>();
        params.put("_method","PUT");
        params.put("id", id);
        params.put("profile", new JSONObject(map).toString());
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,Request.Method.POST, ZhaiDou.USER_EDIT_PROFILE_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mDialog.dismiss();
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status == 200) {
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    if (dataObj.optJSONObject("profile") == null) {
                        Toast.makeText(getActivity(), mTitle+"已经被使用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ProfileManage.TAG.DESC==mTag) {
                        ProfileManage.getInstance().notify(ProfileManage.TAG.DESC,tv_description.getText().toString());

                    } else {
                        ProfileManage.getInstance().notify(mTag,tv_edit_msg.getText().toString());
                    }
                } else {
                    ShowToast(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mTitle);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mTitle);
    }
}
