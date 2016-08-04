package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Address;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddrManageFragment extends BaseFragment implements View.OnClickListener
{
    private static final String ARG_NickName = "param1";
    private static final String ARG_MOBILE = "param2";
    private static final String ARG_ADDRESS = "param3";
    private static final String ARG_PROFILE_ID = "param4";
    private static final String ARG_STATUS = "param5";

    private String mNickName;
    private String mMobile;
    private String mAddress;
    private String mProfileId;
    private int mStatus;


    private LinearLayout ll_edit_addr;
    private LinearLayout ll_manage_address;
    private TextView titleTv;
    private SharedPreferences mSharedPreferences;
    private String token;

    private AddressListener addressListener;
    private DialogUtils mDialogUtil;

    private Dialog mDialog;
    boolean isDialogFirstVisible = true;
    private RequestQueue mRequestQueue;
    private ListView mListview;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<Address>();
    private final int UPDATE_ADDRESS_LIST = 0;

    private int UPDATE_ADDRESS_INFO = 1;
    private int CREATE_NEW_ADDRESS = 2;
    private int STATUS_FROM_ORDER = 3;
    private int STATUS_FROM_PERSONAL = 4;
    private int mCheckedPosition = 0;
    private View rootView;
    private LinearLayout loadingView;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_ADDRESS_LIST:
                    loadingView.setVisibility(View.GONE);
                    addressAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public static AddrManageFragment newInstance(String nickname, String mobile, String address, String profileId, int status)
    {
        AddrManageFragment fragment = new AddrManageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NickName, nickname);
        args.putString(ARG_MOBILE, mobile);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_PROFILE_ID, profileId);
        args.putInt(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    public AddrManageFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mNickName = getArguments().getString(ARG_NickName);
            mMobile = getArguments().getString(ARG_MOBILE);
            mAddress = getArguments().getString(ARG_ADDRESS);
            mProfileId = getArguments().getString(ARG_PROFILE_ID);
            mStatus = getArguments().getInt(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (null != rootView)
        {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent)
            {
                parent.removeView(rootView);
            }
        } else
        {
            rootView = inflater.inflate(R.layout.fragment_addr_manage, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view)
    {
        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText(R.string.title_address_manage);

        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        isDialogFirstVisible = false;
        mDialogUtil = new DialogUtils(mContext);
        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        mListview = (ListView) view.findViewById(R.id.lv_addresses);
        addressAdapter = new AddressAdapter(getActivity(), addressList);
        mListview.setAdapter(addressAdapter);
        view.findViewById(R.id.bt_new_address).setOnClickListener(this);
        mRequestQueue = ZDApplication.newRequestQueue();
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        FetchData();
        addressAdapter.setOnInViewClickListener(R.id.ll_defalue, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {

                mCheckedPosition = position;
                addressAdapter.notifyDataSetChanged();
                if (mStatus == STATUS_FROM_ORDER)
                {
                    ((BaseActivity) getActivity()).popToStack(AddrManageFragment.this);
                }
            }
        });
        addressAdapter.setOnInViewClickListener(R.id.tv_delete, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values)
            {
                final Address address = (Address) values;
                final int id = address.getId();

                mDialogUtil.showDialog("是否删除地址?", new DialogUtils.PositiveListener()
                {
                    @Override
                    public void onPositive()
                    {
                        mDialog.show();
                        String url = ZhaiDou.AddressDeleteUrl + "?id=" + address.getId();
                        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.DELETE, url, new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject jsonObject)
                            {
                                if (mDialog != null)
                                    mDialog.dismiss();
                                int code = jsonObject.optInt("status");
                                if (code != 200)
                                {
                                    ToolUtils.setToast(mContext, "抱歉，删除失败");
                                    return;
                                }
                                JSONObject dataObject = jsonObject.optJSONObject("data");
                                if (dataObject != null)
                                {
                                    int status = dataObject.optInt("status");
                                    String msg = dataObject.optString("message");
                                    if (status == 201)
                                    {
                                        ToolUtils.setToast(mContext, msg);
                                        addressList.remove(address);
                                        addressAdapter.notifyDataSetChanged();
                                        if (addressList.size() == 0)
                                        {
                                            if (addressListener!=null)
                                            addressListener.onDeleteFinishAddress();
                                        }
                                    }else {

                                    String message = jsonObject.optString("message");
                                    ToolUtils.setToast(mContext, message);
                                    }
                                }
                            }
                        }, new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError volleyError)
                            {
                                if (mDialog != null)
                                    mDialog.dismiss();
                                ToolUtils.setToast(mContext, "抱歉，删除失败");
                            }
                        });
                        mRequestQueue.add(request);
                    }
                }, null);
            }
        });
        addressAdapter.setOnInViewClickListener(R.id.tv_edit, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values)
            {
                Address address = (Address) values;
                int id = address.getId();
                String name = address.getName();
                String phone = address.getPhone();
                String addr = address.getAddress();
                String location = address.getProvince() + "-" + address.getCity() + "-" + address.getArea();
                int provider_id = address.getProvider_id();
                final AddrNewAddrFragment newAddrFragment = AddrNewAddrFragment.newInstance(id, name, phone, location, addr, provider_id, UPDATE_ADDRESS_INFO);
                ((BaseActivity) getActivity()).navigationToFragment(newAddrFragment);

                newAddrFragment.setAddrSaveSuccessListener(new AddrNewAddrFragment.AddrSaveSuccessListener()
                {
                    @Override
                    public void onSaveListener(JSONObject receiverObj, int status, double yfprice, String province, String city, String area)
                    {
                        if (status == UPDATE_ADDRESS_INFO)
                        {
                            int id = receiverObj.optInt("id");
                            String phone = receiverObj.optString("phone");
                            String addr = receiverObj.optString("address");
                            int provider_id = receiverObj.optInt("provider_id");
                            int user_id = receiverObj.optInt("user_id");
                            String name = receiverObj.optString("name");
                            boolean is_default = receiverObj.optBoolean("is_default");
                            Address address1 = new Address(id, name, is_default, phone, user_id, addr, provider_id, yfprice);
                            address1.setProvince(province);
                            address1.setCity(city);
                            address1.setArea(area);
                            addressAdapter.remove(position);
                            addressAdapter.add(address1, position);
                            ((BaseActivity) getActivity()).popToStack(newAddrFragment);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.bt_new_address:
                final AddrNewAddrFragment newAddrFragment = AddrNewAddrFragment.newInstance(0, "", "", "", "", 0, CREATE_NEW_ADDRESS);
                ((BaseActivity) getActivity()).navigationToFragment(newAddrFragment);
                newAddrFragment.setAddrSaveSuccessListener(new AddrNewAddrFragment.AddrSaveSuccessListener()
                {
                    @Override
                    public void onSaveListener(JSONObject receiverObj, int status, double yfprice, String province, String city, String area)
                    {
                        if (receiverObj != null && CREATE_NEW_ADDRESS == status)
                        {
                            int id = receiverObj.optInt("id");
                            int user_id = receiverObj.optInt("user_id");
                            String name = receiverObj.optString("name");
                            String phone = receiverObj.optString("phone");
                            int provider_id = receiverObj.optInt("provider_id");
                            String address = receiverObj.optString("address");
                            boolean is_default = receiverObj.optBoolean("is_default");
                            Address addr = new Address(id, name, is_default, phone, user_id, address, provider_id, yfprice);
                            addr.setProvince(province);
                            addr.setCity(city);
                            addr.setArea(area);
                            addressAdapter.add(addr);
                            ((BaseActivity) getActivity()).popToStack(newAddrFragment);
                        }

                    }
                });
                break;
        }
    }


    public void setAddressListener(AddressListener addressListener)
    {
        this.addressListener = addressListener;
    }

    public interface AddressListener
    {
        public void onDefalueAddressChange(Address address);

        public void onDeleteFinishAddress();
    }

    private void FetchData()
    {
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,ZhaiDou.AddressListUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                mDialog.dismiss();
                ToolUtils.setLog(jsonObject.toString());
                int status = jsonObject.optInt("status");
                if (status != 200)
                {
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
                JSONObject dataObject = jsonObject.optJSONObject("data");
                JSONArray receiversArr = dataObject.optJSONArray("receivers");
                if (receiversArr != null && receiversArr.length() > 0)
                {
                    for (int i = 0; i < receiversArr.length(); i++)
                    {
                        JSONObject receiverObj = receiversArr.optJSONObject(i);
                        String phone = receiverObj.optString("phone");
                        int user_id = receiverObj.optInt("user_id");
                        String addr = receiverObj.optString("address");
                        String name = receiverObj.optString("name");
                        int id = receiverObj.optInt("id");
                        int provider_id = receiverObj.optInt("provider_id");
                        String province = receiverObj.optString("parent_name");
                        String city = receiverObj.optString("city_name");
                        String area = receiverObj.optString("provider_name");
                        boolean is_default = receiverObj.optBoolean("is_default");
                        double price = receiverObj.optDouble("price");
                        if (is_default)
                        {
                            mCheckedPosition = i;
                        }
                        Address address = new Address();
                        address.setAddress(addr);
                        address.setName(name);
                        address.setUser_id(user_id);
                        address.setPhone(phone);
                        address.setId(id);
                        address.setProvider_id(provider_id);
                        address.setProvince(province);
                        address.setCity(city);
                        address.setArea(area);
                        address.setIs_default(is_default);
                        address.setPrice(price);
                        addressList.add(address);
                    }
                    handler.sendEmptyMessage(UPDATE_ADDRESS_LIST);
                } else
                {
                    loadingView.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
            }
        });
        mRequestQueue.add(request);
    }

    public class AddressAdapter extends BaseListAdapter<Address>
    {
        public AddressAdapter(Context context, List<Address> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_addresses_list, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_addr_username);
            TextView tv_mobile = ViewHolder.get(convertView, R.id.tv_addr_mobile);
            TextView tv_addr = ViewHolder.get(convertView, R.id.tv_addr);
            TextView tv_defalue = ViewHolder.get(convertView, R.id.tv_defalue_addr);
            TextView tv_defalue_hint = ViewHolder.get(convertView, R.id.tv_defalue_hint);
            ImageView mDefalueIcon = ViewHolder.get(convertView, R.id.iv_addr_defalue);
            View view = ViewHolder.get(convertView, R.id.lineBg);
            if (position == 0)
            {
                view.setVisibility(View.GONE);
            } else
            {
                view.setVisibility(View.VISIBLE);
            }
            Address address = getList().get(position);
            tv_name.setText(address.getName());
            tv_mobile.setText(address.getPhone());
            if (address.getProvince() == null)
            {
                tv_addr.setText(address.getAddress());
            } else
            {
                tv_addr.setText(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
            }
            if (mStatus == STATUS_FROM_ORDER)
            {
                tv_defalue_hint.setVisibility(View.GONE);
                if (address.isIs_default())
                    tv_defalue_hint.setVisibility(View.VISIBLE);
                tv_defalue.setText("选择地址");
            }
            mDefalueIcon.setImageResource(mCheckedPosition == position ? R.drawable.icon_address_checked : R.drawable.icon_address_normal);
            return convertView;
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (addressAdapter.getCount() > mCheckedPosition)
        {
            Address address = addressAdapter.getItem(mCheckedPosition);
            JSONObject jsonObject = new JSONObject();
            try
            {
                jsonObject.put("id", address.getId());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.AddressIsDefultUrl, jsonObject, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject jsonObject)
                {
                    if (jsonObject != null)
                    {
                        JSONObject dataObject = jsonObject.optJSONObject("data");
                        if (dataObject == null)
                        {
                            return;
                        }
                        JSONObject receiver = dataObject.optJSONObject("receiver");
                        int id = receiver.optInt("id");
                        String phone = receiver.optString("phone");
                        String updated_at = receiver.optString("updated_at");
                        String addr = receiver.optString("address");
                        int provider_id = receiver.optInt("provider_id");
                        String name = receiver.optString("name");
                        String created_at = receiver.optString("created_at");
                        boolean is_default = receiver.optBoolean("is_default");
                        int user_id = receiver.optInt("user_id");
                        int price = receiver.optInt("price");
                        Address address1 = new Address(id, name, is_default, phone, user_id, addr, provider_id, price);
                        address1.setUpdated_at(updated_at);
                        address1.setCreated_at(created_at);
                    }
                }
            },null);
            mRequestQueue.add(request);
        }
    }

    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_address_manage));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_address_manage));
    }
}
