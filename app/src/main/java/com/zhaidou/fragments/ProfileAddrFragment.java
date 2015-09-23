package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.SharedPreferencesUtil;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
*
* Author Scoield
* Created at 15/9/16 10:08
* Description:个人资料里的地址管理
* FIXME
*/
public class ProfileAddrFragment extends BaseFragment implements View.OnClickListener {

    private static final String ARG_NickName = "nickname";
    private static final String ARG_MOBILE = "mobile";
    private static final String ARG_ADDRESS = "address";
    private static final String ARG_PROFILE_ID = "profileId";

    private String mNickName;
    private String mMobile;
    private String mAddress;
    private String mProfileId;

    private LinearLayout ll_edit_addr,tv_edit,tv_delete;
    private LinearLayout ll_manage_address;
    private EditText et_mobile,et_addr,et_name;
    private TextView tv_save,tv_addr_username,tv_addr_mobile,tv_addr;
    private String token;
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    private AddressListener addressListener;
    private Dialog mDialog;
    /**
     * @param nickname  姓名
     * @param mobile  联系电话
     *@param address 收货地址
     * @param profileId 用户的profileId
     * @return A new instance of fragment ProfileAddrFragment.
     */
    public static ProfileAddrFragment newInstance(String nickname, String mobile, String address, String profileId) {
        ProfileAddrFragment fragment = new ProfileAddrFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NickName, nickname);
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_PROFILE_ID, profileId);
        fragment.setArguments(args);
        return fragment;
    }
    public ProfileAddrFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNickName = getArguments().getString(ARG_NickName);
            mMobile = getArguments().getString(ARG_MOBILE);
            mAddress=getArguments().getString(ARG_ADDRESS);
            mProfileId=getArguments().getString(ARG_PROFILE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile_addr, container, false);
        mContext=getActivity();
        ll_manage_address=(LinearLayout)view.findViewById(R.id.ll_manage_address);
        ll_edit_addr=(LinearLayout)view.findViewById(R.id.ll_edit_addr);
        et_name=(EditText)view.findViewById(R.id.et_addr_name);
        et_addr=(EditText)view.findViewById(R.id.et_addr);
        et_mobile=(EditText)view.findViewById(R.id.et_mobile);
        tv_addr=(TextView)view.findViewById(R.id.tv_addr);
        tv_delete=(LinearLayout)view.findViewById(R.id.tv_delete);
        tv_addr_mobile=(TextView)view.findViewById(R.id.tv_addr_mobile);
        tv_addr_username=(TextView)view.findViewById(R.id.tv_addr_username);
        tv_save=(TextView)view.findViewById(R.id.tv_save);
        tv_edit=(LinearLayout)view.findViewById(R.id.tv_edit);
        tv_save.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        if (TextUtils.isEmpty(mAddress)){
            ll_edit_addr.setVisibility(View.VISIBLE);
            ll_manage_address.setVisibility(View.GONE);
        }else {
            ll_edit_addr.setVisibility(View.GONE);
            ll_manage_address.setVisibility(View.VISIBLE);
            tv_addr_username.setText(mNickName);
            tv_addr_mobile.setText(mMobile);
            tv_addr.setText(mAddress);
        }

        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token=mSharedPreferences.getString("token", null);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_save:
                String name=et_name.getText().toString().trim();
                String mobile=et_mobile.getText().toString().trim();
                String address=et_addr.getText().toString().trim();
                if (TextUtils.isEmpty(name)){
                    Toast.makeText(getActivity(), "收货人信息不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(mobile)){
                    Toast.makeText(getActivity(),"联系方式不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(address)){
                    Toast.makeText(getActivity(),"收货地址不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                mNickName=name;
                mMobile=mobile;
                mAddress=address;
//                new MyTask().execute(name,mobile,address,mProfileId);
                mDialog=CustomLoadingDialog.setLoadingDialog(mContext, "loading");;
                PostData(name,mobile,address,mProfileId);
                break;
            case R.id.tv_edit:
                ll_edit_addr.setVisibility(View.VISIBLE);
                ll_manage_address.setVisibility(View.GONE);
                et_addr.setText(mAddress);
                et_mobile.setText(mMobile);
                et_name.setText(mNickName);
                break;
            case R.id.tv_delete:
                Log.i("tv_addr_username.getText().toString()--->",tv_addr_username.getText().toString());
                Log.i("tv_addr_mobile.getText().toString()--->",tv_addr_mobile.getText().toString());
//                new MyTask().execute(tv_addr_username.getText().toString(),tv_addr_mobile.getText().toString(),"",mProfileId);
                mDialog=CustomLoadingDialog.setLoadingDialog(mContext, "loading");;
                PostData(tv_addr_username.getText().toString(), tv_addr_mobile.getText().toString(), "", mProfileId);
                break;
        }
    }


    private void PostData(String username,String mobile,String address,String profileId){
        final String token= (String)SharedPreferencesUtil.getData(mContext,"token","");
        Map<String,String> params=new HashMap<String, String>();
        params.put("_method", "PUT");
        params.put("profile[nick_name]", username);
        params.put("profile[mobile]", mobile);
        params.put("profile[address2]", address);
        params.put("profile[id]", profileId);
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST,ZhaiDou.USER_EDIT_PROFILE_URL+profileId,params,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mDialog.dismiss();
                System.out.println("ProfileAddrFragment.onResponse------->"+jsonObject);
                if (jsonObject!=null){
                    JSONObject profile = jsonObject.optJSONObject("profile");
                    String mobile = profile.optString("mobile");
                    String address = profile.optString("address2");
                    addressListener.onAddressDataChange(mNickName, mMobile, address);
                    ((MainActivity) getActivity()).popToStack(ProfileAddrFragment.this);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("SECAuthorization",token);
                return headers;
            }
        };
        ZDApplication.mRequestQueue.add(request);
    }

    public void setAddressListener(AddressListener addressListener) {
        this.addressListener = addressListener;
    }

    public interface AddressListener{
        public void onAddressDataChange(String name,String mobile,String address);
    }
}
