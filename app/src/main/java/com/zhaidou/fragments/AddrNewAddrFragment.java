package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Area;
import com.zhaidou.model.City;
import com.zhaidou.model.HttpPatch;
import com.zhaidou.model.Province;
import com.zhaidou.utils.ToolUtils;
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
import java.util.List;

public class AddrNewAddrFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_ID="id";
    private static final String ARG_NickName = "param1";
    private static final String ARG_MOBILE = "param2";
    private static final String ARG_ADDRESS = "param3";
    private static final String ARG_PROVIDER_ID = "param4";
    private static final String ARG_LOCATION="location";
    private static final String ARG_STATUS = "param5";

    private int mId;
    private String mNickName;
    private String mMobile;
    private String mAddress;
    private String mLocation;
    private int mProviderId;
    private int mStatus;

    private String token;
    private SharedPreferences mSharedPreferences;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private EditText et_name, et_mobile, et_address_detail;
    private TextView et_location;

    private List<Province> provinceList = new ArrayList<Province>();
    private Province selectedProvince=new Province();
    private City selectedCity=new City();
    private Area selectedArea=new Area();
    private AddrSaveSuccessListener addrSaveSuccessListener;
    private int UPDATE_ADDRESS_INFO=1;
    private int CREATE_NEW_ADDRESS=2;

    public static AddrNewAddrFragment newInstance(int id,String nickname, String mobile,String location,String address, int profileId, int status) {
        AddrNewAddrFragment fragment = new AddrNewAddrFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID,id);
        args.putString(ARG_NickName, nickname);
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_LOCATION,location);
        args.putInt(ARG_PROVIDER_ID, profileId);
        args.putInt(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    public AddrNewAddrFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId=getArguments().getInt(ARG_ID);
            mNickName = getArguments().getString(ARG_NickName);
            mMobile = getArguments().getString(ARG_MOBILE);
            mAddress = getArguments().getString(ARG_ADDRESS);
            mLocation=getArguments().getString(ARG_LOCATION);
            mProviderId = getArguments().getInt(ARG_PROVIDER_ID);
            mStatus = getArguments().getInt(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_addr, container, false);
        et_name = (EditText) view.findViewById(R.id.tv_addr_username);
        et_address_detail = (EditText) view.findViewById(R.id.tv_addr_detail);
        et_mobile = (EditText) view.findViewById(R.id.tv_addr_mobile);
        et_location = (TextView) view.findViewById(R.id.tv_addr_loc);

        et_name.setText(mNickName);
        et_mobile.setText(mMobile);
        et_location.setText(mLocation);

        if (mLocation!=null&&mLocation.length()>8)
        {
            String[] city=mLocation.split("-");
            selectedProvince.setName(city[0]);
            selectedCity.setName(city[1]) ;
            selectedArea.setName(city[2]);
        }

        et_address_detail.setText(mAddress);

        view.findViewById(R.id.tv_save).setOnClickListener(this);
        view.findViewById(R.id.ll_address).setOnClickListener(this);
        mRequestQueue = Volley.newRequestQueue(getActivity(), new HttpClientStack(new DefaultHttpClient()));
        mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token = mSharedPreferences.getString("token", null);

        if (MainActivity.provinceList!=null&&MainActivity.provinceList.size()>1)
        {
            ToolUtils.setLog("加载已经添加的");
            provinceList=MainActivity.provinceList;
        }
        else
        {
            ToolUtils.setLog("重新加载地址");
            FetchCityData();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                hideInputMethod();
                String name = et_name.getText().toString().trim();
                String mobile = et_mobile.getText().toString().trim();
                String location = et_location.getText().toString().trim();
                String address = et_address_detail.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "收货人信息不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(getActivity(), "联系方式不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(address)) {
                    Toast.makeText(getActivity(), "详细地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(location)) {
                    Toast.makeText(getActivity(), "省市区不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNickName = name;
                mMobile = mobile;
                mAddress = address;
//                mProviderId=selectedArea==null?mProviderId:selectedArea.getId();
                ToolUtils.setLog("selectedArea.getId():"+selectedArea.getId());
                ToolUtils.setLog("mProviderId:"+mProviderId);
                new MyTask().execute();
                break;
            case R.id.ll_address:
                final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

                Window dialogWindow = dialog.getWindow();
                dialogWindow.setGravity(Gravity.BOTTOM);
                dialogWindow.setWindowAnimations(R.style.pop_anim_style);

                View mDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_adddress, null, false);
                final WheelViewContainer wheelView= (WheelViewContainer) mDialogView.findViewById(R.id.wheel_view_wv);
                LinearLayout cityView= (LinearLayout) mDialogView.findViewById(R.id.cityView);
                if(provinceList != null && provinceList.size() >1)
                {
                    wheelView.setData(provinceList);
                    wheelView.setVisibility(View.VISIBLE);
                }
                else
                {
                    wheelView.setVisibility(View.GONE);
                }
                TextView cancelTv = (TextView) mDialogView.findViewById(R.id.bt_cancel);
                cancelTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                TextView okTv = (TextView) mDialogView.findViewById(R.id.bt_confirm);
                okTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        if(provinceList != null && provinceList.size() > 1)
                        {
                            selectedProvince = wheelView.getProvince();
                            selectedCity = wheelView.getCity();
                            selectedArea = wheelView.getArea();
                            mProviderId=selectedArea.getId();
                            ToolUtils.setLog("ok mProviderId:"+mProviderId);
                            et_location.setText(selectedProvince.getName() + "-" + selectedCity.getName() + "-" + selectedArea.getName());
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.addContentView(mDialogView, new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();
                break;
        }
    }


    private class MyTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        }

        @Override
        protected String doInBackground(Void... voids) {
            String s = null;
            try {
                s = executeHttpPost();
            } catch (Exception e) {

            }
            return s;
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                mDialog.dismiss();
                JSONObject json = new JSONObject(s);
                ToolUtils.setLog("s:"+s);
                String message=json.optString("message");
                int status=json.optInt("status");
                if (status==201)
                {
                    JSONObject receiver=json.optJSONObject("receiver");
                    double price=json.optDouble("price");
                    ToolUtils.setLog("yfPrice:"+price);
                    if (addrSaveSuccessListener!=null)
                    {
                        addrSaveSuccessListener.onSaveListener(receiver,mStatus,price,selectedProvince.getName(), selectedCity.getName(), selectedArea.getName());
                    }
                }else
                {
                    ShowToast("抱歉,保存失败");
                }
            } catch (Exception e) {

            }
        }
    }

    public String executeHttpPost() throws Exception {
        BufferedReader in = null;
        // 执行请求
        HttpResponse response=null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();

            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("receivers[name]", et_name.getText().toString().trim()));
            parameters.add(new BasicNameValuePair("receivers[phone]", et_mobile.getText().toString().trim()));
            parameters.add(new BasicNameValuePair("receivers[address]", et_address_detail.getText().toString().trim()));
            parameters.add(new BasicNameValuePair("receivers[provider_id]", mProviderId+""));

            if (mStatus==CREATE_NEW_ADDRESS){
                // 实例化HTTP方法
                HttpPost request = new HttpPost(ZhaiDou.ORDER_RECEIVER_URL);
                request.addHeader("SECAuthorization",token);

                // 创建UrlEncodedFormEntity对象
                UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                        parameters, HTTP.UTF_8);//这里要设置，不然回来乱码
                request.setEntity(formEntiry);
                // 执行请求
                response = client.execute(request);
            }else if (mStatus==UPDATE_ADDRESS_INFO){
                Log.i("mStatus==UPDATE_ADDRESS_INFO------------>","mStatus==UPDATE_ADDRESS_INFO");
                // 实例化HTTP方法
                HttpPatch request = new HttpPatch(ZhaiDou.ORDER_RECEIVER_URL+"/"+mId);
                request.addHeader("SECAuthorization",token);

                // 创建UrlEncodedFormEntity对象
                UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                        parameters, HTTP.UTF_8);//这里要设置，不然回来乱码
                request.setEntity(formEntiry);
                // 执行请求
                response = client.execute(request);
            }

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

    private void FetchCityData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.ORDER_ADDRESS_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchCityData---->", jsonObject.toString());
                if (jsonObject != null) {
                    JSONArray providerArr = jsonObject.optJSONArray("providers");
                    for (int i = 0; i < providerArr.length(); i++) {
                        JSONObject provinceObj = providerArr.optJSONObject(i);
                        int provinceId = provinceObj.optInt("id");
                        String provinceName = provinceObj.optString("name");
                        Province province = new Province();
                        province.setId(provinceId);
                        province.setName(provinceName);
                        List<City> cityList = new ArrayList<City>();
                        JSONArray cityArr = provinceObj.optJSONArray("cities");
                        if (cityArr != null && cityArr.length() > 0) {
                            for (int k = 0; k < cityArr.length(); k++) {
                                JSONObject cityObj = cityArr.optJSONObject(k);
                                int cityId = cityObj.optInt("id");
                                String cityName = cityObj.optString("name");
                                JSONArray areaArr = cityObj.optJSONArray("children");
                                City city = new City();
                                city.setId(cityId);
                                city.setName(cityName);
                                List<Area> areaList = new ArrayList<Area>();
                                if (areaArr != null && areaArr.length() > 0) {
                                    for (int j = 0; j < areaArr.length(); j++) {
                                        JSONObject areaObj = areaArr.optJSONObject(j);
                                        int areaId = areaObj.optInt("id");
                                        String areaName = areaObj.optString("name");
                                        double areaPrice = areaObj.optDouble("price");
                                        Area area = new Area();
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToolUtils.setToast(getActivity(),"抱歉,加载城市失败");
            }
        });
        mRequestQueue.add(request);
    }

    public void setAddrSaveSuccessListener(AddrSaveSuccessListener addrSaveSuccessListener) {
        this.addrSaveSuccessListener = addrSaveSuccessListener;
    }

    public interface AddrSaveSuccessListener{
        public void onSaveListener(JSONObject receiver,int status,double yfPrice,String province,String city,String area);
    }
}
