package com.zhaidou.fragments;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Area;
import com.zhaidou.model.City;
import com.zhaidou.model.PCityArea;
import com.zhaidou.model.Province;
import com.zhaidou.utils.CollectionUtils;
import com.zhaidou.view.WheelView;
import com.zhaidou.view.WheelViewContainer;

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
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link com.zhaidou.fragments.NewAddrFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class NewAddrFragment extends BaseFragment implements View.OnClickListener{
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

    private String token;
    private SharedPreferences mSharedPreferences;

    private AddressListener addressListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private EditText et_name,et_mobile,et_location,et_address_detail;

    private List<Province> provinceList=new ArrayList<Province>();
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddrManageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewAddrFragment newInstance(String nickname,String mobile,String address, String profileId,int status) {
        NewAddrFragment fragment = new NewAddrFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NickName, nickname);
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_PROFILE_ID, profileId);
        args.putInt(ARG_STATUS,status);
        fragment.setArguments(args);
        return fragment;
    }
    public NewAddrFragment() {
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
        View view=inflater.inflate(R.layout.fragment_new_addr, container, false);

        et_name=(EditText)view.findViewById(R.id.tv_addr_username);
        et_address_detail=(EditText)view.findViewById(R.id.tv_addr_detail);
        et_mobile=(EditText)view.findViewById(R.id.tv_addr_mobile);
        et_location=(EditText)view.findViewById(R.id.tv_addr_loc);

        view.findViewById(R.id.tv_save).setOnClickListener(this);
        view.findViewById(R.id.ll_address).setOnClickListener(this);
        mRequestQueue= Volley.newRequestQueue(getActivity(),new HttpClientStack(new DefaultHttpClient()));
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token=mSharedPreferences.getString("token", null);
        FetchCityData();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_save:
                hideInputMethod();
                String name=et_name.getText().toString();
                String mobile=et_mobile.getText().toString();
                String location=et_location.getText().toString();
                String address=et_address_detail.getText().toString();
                if (TextUtils.isEmpty(name)){
                    Toast.makeText(getActivity(),"收货人信息不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(mobile)){
                    Toast.makeText(getActivity(),"联系方式不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(address)){
                    Toast.makeText(getActivity(),"收货地址不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(location)){
                    Toast.makeText(getActivity(),"详细地址不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                mNickName=name;
                mMobile=mobile;
                mAddress=address;
                Log.i("hhh","dada");
                new MyTask().execute(name,mobile,address,mProfileId);
                break;
            case R.id.tv_edit:
//                ll_edit_addr.setVisibility(View.VISIBLE);
//                ll_manage_address.setVisibility(View.GONE);
//                et_addr.setHint(mAddress);
//                et_mobile.setHint(mMobile);
//                et_name.setHint(mNickName);
                break;
            case R.id.tv_delete:
//                new MyTask().execute(tv_addr_username.getText().toString(),tv_addr_mobile.getText().toString(),"",mProfileId);
                break;
            case R.id.ll_address:
                final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

                View mDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_adddress, null,false);
                WheelViewContainer wheelView=(WheelViewContainer)mDialogView.findViewById(R.id.wheel_view_wv);
//                WheelView wheelView1=(WheelView)mDialogView.findViewById(R.id.wheel_view_wv1);
//                WheelView areaView=(WheelView)mDialogView.findViewById(R.id.wheel_view_wv2);
                if (CollectionUtils.isNotNull(provinceList))
                wheelView.setData(provinceList);
//                wheelView1.setItems(cityList);
//                areaView.setItems(areaList);
//                TextView cancelTv = (TextView) view1.findViewById(R.id.cancelTv);
//                cancelTv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.dismiss();
//                    }
//                });
//
//                TextView okTv = (TextView) view1.findViewById(R.id.okTv);
//                okTv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.dismiss();
//                    }
//                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.addContentView(mDialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();
                break;
        }
    }


    private class MyTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute()
        {
            mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        }

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
            try {
                mDialog.dismiss();
                JSONObject json =new JSONObject(s);
                JSONObject profile =json.optJSONObject("profile");
                String mobile=profile.optString("mobile");
                String address=profile.optString("address2");
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
            HttpPost request = new HttpPost("http://192.168.199.173/special_mall/api/receivers");
            request.addHeader("SECAuthorization", "Yk77mfWaq_xYyeEibAxx");


            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("receivers[name]",name));
            parameters.add(new BasicNameValuePair("receivers[phone]",mobile));
            parameters.add(new BasicNameValuePair("receivers[address]",addr));
            parameters.add(new BasicNameValuePair("receivers[provider_id]",id));

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
        public void onAddressDataChange(String name, String mobile, String address);
    }

    private void FetchCityData(){
        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.173/special_mall/api/sales/provider",new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchCityData---->",jsonObject.toString());
                if (jsonObject!=null){
                    JSONArray providerArr=jsonObject.optJSONArray("providers");
                    for (int i=0;i<providerArr.length();i++){
                        JSONObject provinceObj=providerArr.optJSONObject(i);
                        int provinceId=provinceObj.optInt("id");
                        String provinceName=provinceObj.optString("name");
                        Province province=new Province();
                        province.setId(provinceId);
                        province.setName(provinceName);
                        List<City> cityList=new ArrayList<City>();
                        JSONArray cityArr=provinceObj.optJSONArray("cities");
                        if (cityArr!=null&&cityArr.length()>0){
                            for (int k=0;k<cityArr.length();k++){
                                JSONObject cityObj =cityArr.optJSONObject(k);
                                int cityId=cityObj.optInt("id");
                                String cityName=cityObj.optString("name");
                                JSONArray areaArr=cityObj.optJSONArray("children");
                                City city=new City();
                                city.setId(cityId);
                                city.setName(cityName);
                                List<Area> areaList=new ArrayList<Area>();
                                if (areaArr!=null&&areaArr.length()>0){
                                    for (int j=0;j<areaArr.length();j++){
                                        JSONObject areaObj=areaArr.optJSONObject(j);
                                        int areaId=areaObj.optInt("id");
                                        String areaName=areaObj.optString("name");
                                        int areaPrice=areaObj.optInt("price");
                                        Area area=new Area();
                                        area.setId(areaId);
                                        area.setName(areaName);
                                        area.setPrice(areaPrice);
                                        areaList.add(area);
                                    }
                                    city.setAreas(areaList);
                                    cityList.add(city);
                                }

                            }
                        }
                        province.setCityList(cityList);
                        provinceList.add(province);
                    }

                }
//                for(Province province:provinceList){
//                    Log.i("provinceList------>",province.getName());
//                }
//                for(City province:cityList){
//                    Log.i("cityList------>",province.getName());
//                }
//                for(Area province:areaList){
//                    Log.i("areaList------>",province.getName());
//                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }
}
