package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Area;
import com.zhaidou.model.City;
import com.zhaidou.model.Province;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.WheelViewContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author Scoield
 * Created at 15/9/16 10:08
 * Description:个人资料里的地址管理
 * FIXME
 */
public class ProfileAddrFragment extends BaseFragment implements View.OnClickListener {

    private static final String ARG_NickName = "nickname";
    private static final String ARG_MOBILE = "mobile";
    private static final String ARG_ADDRESS = "address";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_PROFILE_ID = "profileId";
    private static final String ARG_PROVIDERID="providerId";

    private String mNickName;
    private String mMobile;
    private String mAddress;
    private String mLocation;
    private String mProfileId;
    private String mProviderId;

    private LinearLayout ll_edit_addr, tv_edit, tv_delete;
    private LinearLayout ll_manage_address;
    private EditText et_mobile, et_addr, et_name;
    private TextView tv_save, tv_addr_username, tv_addr_mobile, tv_addr;
    private String token;
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    private AddressListener addressListener;
    private Dialog mDialog;
    private List<Province> provinceList = new ArrayList<Province>();
    private Province selectedProvince = new Province();
    private City selectedCity = new City();
    private Area selectedArea = new Area();
    private TextView et_location;
    private DialogUtils mDialogUtils;

    private FrameLayout mContainer;
    private final int LOAD_ADDRESS_COMPLITED = 0;
    private final int UPDATE_USER_LOCATION = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_ADDRESS_COMPLITED:
                    mContainer.setVisibility(View.GONE);
                    break;
                case UPDATE_USER_LOCATION:
                    String loc = (String) msg.obj;
                    et_location.setText(loc);
                    break;
            }
        }
    };

    /**
     * @param nickname  姓名
     * @param mobile    联系电话
     * @param address   收货地址
     * @param profileId 用户的profileId
     * @return A new instance of fragment ProfileAddrFragment.
     */
    public static ProfileAddrFragment newInstance(String nickname, String mobile, String location, String address,String providerId, String profileId) {
        ProfileAddrFragment fragment = new ProfileAddrFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NickName, nickname);
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_LOCATION, location);
        args.putString(ARG_PROFILE_ID, profileId);
        args.putString(ARG_PROVIDERID,providerId);
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
            mAddress = getArguments().getString(ARG_ADDRESS);
            mLocation = getArguments().getString(ARG_LOCATION);
            mProfileId = getArguments().getString(ARG_PROFILE_ID);
            mProviderId=getArguments().getString(ARG_PROVIDERID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_addr, container, false);
        mContext = getActivity();
        ll_manage_address = (LinearLayout) view.findViewById(R.id.ll_manage_address);
        ll_edit_addr = (LinearLayout) view.findViewById(R.id.ll_edit_addr);
        et_name = (EditText) view.findViewById(R.id.et_addr_name);
        et_addr = (EditText) view.findViewById(R.id.et_addr);
        et_mobile = (EditText) view.findViewById(R.id.et_mobile);
        tv_addr = (TextView) view.findViewById(R.id.tv_addr);
        tv_delete = (LinearLayout) view.findViewById(R.id.tv_delete);
        tv_addr_mobile = (TextView) view.findViewById(R.id.tv_addr_mobile);
        tv_addr_username = (TextView) view.findViewById(R.id.tv_addr_username);
        tv_save = (TextView) view.findViewById(R.id.tv_save);
        tv_edit = (LinearLayout) view.findViewById(R.id.tv_edit);
        et_location = (TextView) view.findViewById(R.id.tv_addr_loc);
        mContainer = (FrameLayout) view.findViewById(R.id.fl_container);
        tv_save.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        view.findViewById(R.id.ll_address).setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        tv_delete.setVisibility(View.GONE);
        if (mNickName.length() > 0) {
            tv_save.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(mAddress)) {
            ll_edit_addr.setVisibility(View.VISIBLE);
            ll_manage_address.setVisibility(View.GONE);
        } else {
            ll_edit_addr.setVisibility(View.GONE);
            ll_manage_address.setVisibility(View.VISIBLE);
            tv_addr_username.setText(mNickName);
            tv_addr_mobile.setText(mMobile);
            String[] split = mLocation.split("-");
            String address="";
            for (String string:split) {
                address+=string;
            }
            tv_addr.setText(address+mAddress);
            et_location.setText(mLocation);

        }
        mDialogUtils = new DialogUtils(mContext);

        mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token = mSharedPreferences.getString("token", null);
        if (mContext instanceof MainActivity)
        provinceList=((MainActivity)mContext).getAddressCity();
        if (provinceList.size() ==0 ) {
            ToolUtils.setLog("重新加载地址");
            FetchCityData();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                String name = et_name.getText().toString().trim();
                String mobile = et_mobile.getText().toString().trim();
                String address = et_addr.getText().toString().trim();
                String loc = et_location.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "收货人信息不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(getActivity(), "联系方式不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(loc)) {
                    Toast.makeText(getActivity(), "省市区不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(address)) {
                    Toast.makeText(getActivity(), "收货地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else if (mobile.length()!=11){
                    Toast.makeText(getActivity(), "手机号码格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNickName = name;
                mMobile = mobile;
                mAddress = address;
                mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
                PostData(name, mobile, address, mProfileId);
                break;
            case R.id.tv_edit:
                ll_edit_addr.setVisibility(View.VISIBLE);
                ll_manage_address.setVisibility(View.GONE);
                tv_save.setVisibility(View.VISIBLE);
                et_addr.setText(mAddress);
                et_mobile.setText(mMobile);
                et_name.setText(mNickName);
                break;
            case R.id.tv_delete:
                mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
                PostData(tv_addr_username.getText().toString(), tv_addr_mobile.getText().toString(), "", mProfileId);
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
                        mProviderId = selectedArea.getId()+"";
                        et_location.setText(selectedProvince.getName() + "-" + selectedCity.getName() + "-" + selectedArea.getName());
                    }
                },null);

                break;
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
                        mContainer.setVisibility(TextUtils.isEmpty(mAddress)?View.VISIBLE:View.GONE);
                        if (mContext instanceof MainActivity)
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
                ToolUtils.setToast(mContext, "抱歉,城市加载失败");
            }
        });
    }

    private void PostData(String username, String mobile, final String address, String profileId) {
        final String token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        Map<String, String> params = new HashMap<String, String>();
        JSONObject jsonObject = new JSONObject();
        try {
            params.put("address1", mProviderId + "");
            params.put("first_name", username);
            params.put("mobile", mobile);
            params.put("address2", address);
            jsonObject.put("id", profileId + "");
            jsonObject.put("_method", "PUT");
            jsonObject.put("profile", new JSONObject(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.USER_EDIT_PROFILE_URL, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mDialog.dismiss();
                int status = jsonObject.optInt("status");
                if (status == 200) {
                    JSONObject profileObj = jsonObject.optJSONObject("data").optJSONObject("profile");
                    String first_name = profileObj.optString("first_name");
                    String address2 = profileObj.optString("address2");
                    String mobile = profileObj.optString("mobile");
                    if (addressListener != null)
                        addressListener.onAddressDataChange(first_name, mobile, et_location.getText().toString(), address2);
                    ((BaseActivity) getActivity()).popToStack(ProfileAddrFragment.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    public void setAddressListener(AddressListener addressListener) {
        this.addressListener = addressListener;
    }

    public interface AddressListener {
        public void onAddressDataChange(String name, String mobile, String locationStr, String address);
    }
}
