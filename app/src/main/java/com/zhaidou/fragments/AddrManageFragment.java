package com.zhaidou.fragments;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddrManageFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddrManageFragment extends BaseFragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NickName = "param1";
    private static final String ARG_MOBILE = "param2";
    private static final String ARG_ADDRESS = "param3";
    private static final String ARG_PROFILE_ID = "param4";
    private static final String ARG_STATUS = "param5";

    // TODO: Rename and change types of parameters
    private String mNickName;
    private String mMobile;
    private String mAddress;
    private String mProfileId;
    private int mStatus;

    private LinearLayout ll_edit_addr;
    private LinearLayout ll_manage_address;
    private EditText et_mobile,et_addr,et_name;
    private TextView tv_save,tv_edit,tv_addr_username,tv_addr_mobile,tv_addr,tv_delete;
    private String token;
    private SharedPreferences mSharedPreferences;

    private AddressListener addressListener;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddrManageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddrManageFragment newInstance(String nickname,String mobile,String address, String profileId,int status) {
        AddrManageFragment fragment = new AddrManageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NickName, nickname);
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_PROFILE_ID, profileId);
        args.putInt(ARG_STATUS,status);
        fragment.setArguments(args);
        return fragment;
    }
    public AddrManageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNickName = getArguments().getString(ARG_NickName);
            mMobile = getArguments().getString(ARG_MOBILE);
            mAddress = getArguments().getString(ARG_ADDRESS);
            mProfileId = getArguments().getString(ARG_PROFILE_ID);
            mStatus=getArguments().getInt(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_addr_manage, container, false);
        Log.i("onCreateView---->",mNickName+"---"+mMobile+"---"+mAddress);
        ll_manage_address=(LinearLayout)view.findViewById(R.id.ll_manage_address);
        ll_edit_addr=(LinearLayout)view.findViewById(R.id.ll_edit_addr);
        et_name=(EditText)view.findViewById(R.id.et_addr_name);
        et_addr=(EditText)view.findViewById(R.id.et_addr);
        et_mobile=(EditText)view.findViewById(R.id.et_mobile);
        tv_addr=(TextView)view.findViewById(R.id.tv_addr);
        tv_delete=(TextView)view.findViewById(R.id.tv_delete);
        tv_addr_mobile=(TextView)view.findViewById(R.id.tv_addr_mobile);
        tv_addr_username=(TextView)view.findViewById(R.id.tv_addr_username);
        tv_save=(TextView)view.findViewById(R.id.tv_save);
        tv_edit=(TextView)view.findViewById(R.id.tv_edit);
        tv_save.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        if (TextUtils.isEmpty(mNickName)||TextUtils.isEmpty(mMobile)||TextUtils.isEmpty(mAddress)){
            ll_edit_addr.setVisibility(View.VISIBLE);
            ll_manage_address.setVisibility(View.GONE);
        }else {
            ll_edit_addr.setVisibility(View.GONE);
            ll_manage_address.setVisibility(View.VISIBLE);
            tv_addr_username.setText(mNickName);
            tv_addr_mobile.setText(mMobile);
            tv_addr.setText(mAddress);
        }

        if (mStatus==1){
            ll_edit_addr.setVisibility(View.VISIBLE);
            ll_manage_address.setVisibility(View.GONE);
            et_addr.setHint(mAddress);
            et_mobile.setHint(mMobile);
            et_name.setHint(mNickName);
        }

        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token=mSharedPreferences.getString("token", null);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_save:
                hideInputMethod();
                String name=et_name.getText().toString();
                String mobile=et_mobile.getText().toString();
                String address=et_addr.getText().toString();
                if (TextUtils.isEmpty(name)){
                    Toast.makeText(getActivity(),"收货人信息不能为空",Toast.LENGTH_SHORT).show();
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
                Log.i("hhh","dada");
                new MyTask().execute(name,mobile,address,mProfileId);
                break;
            case R.id.tv_edit:
                ll_edit_addr.setVisibility(View.VISIBLE);
                ll_manage_address.setVisibility(View.GONE);
                et_addr.setHint(mAddress);
                et_mobile.setHint(mMobile);
                et_name.setHint(mNickName);
                break;
            case R.id.tv_delete:
                Log.i("tv_addr_username.getText().toString()--->",tv_addr_username.getText().toString());
                Log.i("tv_addr_mobile.getText().toString()--->",tv_addr_mobile.getText().toString());
                new MyTask().execute(tv_addr_username.getText().toString(),tv_addr_mobile.getText().toString(),"",mProfileId);
                break;
        }
    }


    private class MyTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {

            String s = null;
            try {
                s =executeHttpPost(strings[0],strings[1],strings[2],strings[3]);
            }catch (Exception e){

            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("AddrManageFragment--onPostExecute-->",s);
            try {
                JSONObject json =new JSONObject(s);
                JSONObject profile =json.optJSONObject("profile");
                String mobile=profile.optString("mobile");
                String address=profile.optString("address2");
                addressListener.onAddressDataChange(mNickName,mMobile,address);
            }catch (Exception e){

            }
        }
    }
    public String executeHttpPost(String name,String mobile,String addr,String id) throws Exception {
        Log.i("name--->",name==null?"":name);
        Log.i("mobile--->",mobile==null?"":mobile);
        Log.i("addr--->",addr==null?"":addr);
        Log.i("id--->",id==null?"":id);
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_EDIT_PROFILE_URL+id);
            request.addHeader("SECAuthorization", token);


            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("_method","PUT"));
//            String newStr = new String(old.getBytes("UTF-8"));
            parameters.add(new BasicNameValuePair("profile[first_name]",name));
            parameters.add(new BasicNameValuePair("profile[mobile]",mobile));
            parameters.add(new BasicNameValuePair("profile[address2]",addr));
            parameters.add(new BasicNameValuePair("profile[id]",id));

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    parameters, HTTP.UTF_8);//这里要设置，不然回来乱码
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
            Log.i("EditProfileFragment--------->",result);
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

    public void setAddressListener(AddressListener addressListener) {
        this.addressListener = addressListener;
    }

    public interface AddressListener{
        public void onAddressDataChange(String name,String mobile,String address);
    }
}
