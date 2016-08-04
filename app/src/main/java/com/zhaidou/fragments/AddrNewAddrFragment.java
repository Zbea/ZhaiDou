package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Area;
import com.zhaidou.model.City;
import com.zhaidou.model.Province;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetService;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CustomEditText;
import com.zhaidou.view.WheelViewContainer;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private CustomEditText et_name, et_mobile, et_address_detail;
    private TextView et_location;

    private List<Province> provinceList = new ArrayList<Province>();
    private Province selectedProvince=new Province();
    private City selectedCity=new City();
    private Area selectedArea=new Area();
    private AddrSaveSuccessListener addrSaveSuccessListener;
    private int UPDATE_ADDRESS_INFO=1;
    private int CREATE_NEW_ADDRESS=2;
    private DialogUtils mDialogUtils;
    private View mContainer;
    private TextView mTitle;


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
        et_name = (CustomEditText) view.findViewById(R.id.tv_addr_username);
        et_address_detail = (CustomEditText) view.findViewById(R.id.tv_addr_detail);
        et_mobile = (CustomEditText) view.findViewById(R.id.tv_addr_mobile);
        et_location = (TextView) view.findViewById(R.id.tv_addr_loc);
        mTitle=(TextView)view.findViewById(R.id.title);

        et_name.setText(mNickName);
        et_mobile.setText(mMobile);
        et_location.setText(mLocation);
        mTitle.setText(TextUtils.isEmpty(mNickName)?"新建地址":"编辑地址");

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
        mContainer=view.findViewById(R.id.container);
        mRequestQueue = Volley.newRequestQueue(getActivity(), new HttpClientStack(new DefaultHttpClient()));
        mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token = mSharedPreferences.getString("token", null);
        mDialogUtils=new DialogUtils(getActivity());

        provinceList=((MainActivity)mContext).getAddressCity();
        if (provinceList.size() ==0)
        {
            ToolUtils.setLog("重新加载地址");
            mContainer.setVisibility(View.GONE);
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
                    et_name.setShakeAnimation();
                    return;
                } else if (TextUtils.isEmpty(mobile)) {
                    et_mobile.setShakeAnimation();
                    Toast.makeText(getActivity(), "联系方式不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(address)) {
                    et_address_detail.setShakeAnimation();
                    Toast.makeText(getActivity(), "详细地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(location)) {
                    Toast.makeText(getActivity(), "省市区不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else if (mobile.length()!=11){
                    Toast.makeText(getActivity(), "手机号码格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNickName = name;
                mMobile = mobile;
                mAddress = address;

                new MyTask().execute();
                break;
            case R.id.ll_address:
                mDialogUtils.showCityDialog(provinceList,new DialogUtils.PositiveListener2()
                {
                    @Override
                    public void onPositive(Object o)
                    {
                        WheelViewContainer wheelView= (WheelViewContainer) o;
                        selectedProvince = wheelView.getProvince();
                        selectedCity = wheelView.getCity();
                        selectedArea = wheelView.getArea();
                        mProviderId=selectedArea.getId();
                        et_location.setText(selectedProvince.getName() + "-" + selectedCity.getName() + "-" + selectedArea.getName());
                    }
                },null);
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
            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("name", et_name.getText().toString().trim()));
            parameters.add(new BasicNameValuePair("phone", et_mobile.getText().toString().trim()));
            parameters.add(new BasicNameValuePair("address", et_address_detail.getText().toString().trim()));
            parameters.add(new BasicNameValuePair("provider_id", mProviderId+""));


            if (mStatus==CREATE_NEW_ADDRESS){
                s= NetService.GETHttpPostService(ZhaiDou.AddressNewUrl,null,parameters);
            }else if (mStatus==UPDATE_ADDRESS_INFO){
                parameters.add(new BasicNameValuePair("id", mId+""));
                // 实例化HTTP方法
                s= NetService.GETHttpPutService(ZhaiDou.AddressEditUrl,null,parameters);
            }
            return s;
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                mDialog.dismiss();
                JSONObject json = new JSONObject(s);
                ToolUtils.setLog("s:"+s);
                int status=json.optInt("status");
                if (status!=200)
                {
                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                }
                JSONObject dataObject=json.optJSONObject("data");
                int code=dataObject.optInt("status");
                if (code==201)
                {
                    JSONObject receiver=dataObject.optJSONObject("receiver");
                    double price=dataObject.optDouble("price");
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

    private void FetchCityData()
    {
        mDialog=mDialogUtils.showLoadingDialog();
        Api.getAddressCity(new Api.SuccessListener()
        {
            @Override
            public void onSuccess(Object object)
            {
                mDialog.dismiss();
                if (object != null)
                {
                    JSONObject jsonObject = (JSONObject) object;
                    if (jsonObject != null)
                    {
                        JSONArray providerArr = jsonObject.optJSONArray("providers");
                        for (int i = 0; i < providerArr.length(); i++)
                        {
                            JSONObject provinceObj = providerArr.optJSONObject(i);
                            int provinceId = provinceObj.optInt("id");
                            String provinceName = provinceObj.optString("name");
                            Province province = new Province();
                            province.setId(provinceId);
                            province.setName(provinceName);
                            List<City> cityList = new ArrayList<City>();
                            JSONArray cityArr = provinceObj.optJSONArray("cities");
                            if (cityArr != null && cityArr.length() > 0)
                            {
                                for (int k = 0; k < cityArr.length(); k++)
                                {
                                    JSONObject cityObj = cityArr.optJSONObject(k);
                                    int cityId = cityObj.optInt("id");
                                    String cityName = cityObj.optString("name");
                                    JSONArray areaArr = cityObj.optJSONArray("children");
                                    City city = new City();
                                    city.setId(cityId);
                                    city.setName(cityName);
                                    List<Area> areaList = new ArrayList<Area>();
                                    if (areaArr != null && areaArr.length() > 0)
                                    {
                                        for (int j = 0; j < areaArr.length(); j++)
                                        {
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
                        mContainer.setVisibility(View.VISIBLE);
                        ((MainActivity)mContext).setAddressCity(provinceList);
                    }
                }
            }
        }, new Api.ErrorListener()
        {
            @Override
            public void onError(Object object)
            {
                mDialog.dismiss();
                ToolUtils.setToast(mContext,"抱歉,城市加载失败");
            }
        });
    }

    public void setAddrSaveSuccessListener(AddrSaveSuccessListener addrSaveSuccessListener) {
        this.addrSaveSuccessListener = addrSaveSuccessListener;
    }

    public interface AddrSaveSuccessListener{
        public void onSaveListener(JSONObject receiver,int status,double yfPrice,String province,String city,String area);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_address_create));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_address_create));
    }
}
