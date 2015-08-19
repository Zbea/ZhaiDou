package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Address;
import com.zhaidou.utils.ToolUtils;

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
 * Use the {@link com.zhaidou.fragments.AddrSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddrSelectFragment extends BaseFragment implements View.OnClickListener {

    private String index;
    private int mStatus;
    private Address mAddress;

    private String token;
    private SharedPreferences mSharedPreferences;

    private AddressListener addressListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private ListView mListview;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<Address>();
    private final int UPDATE_ADDRESS_LIST = 0;

    private LinearLayout loadingView;

    private int UPDATE_ADDRESS_INFO=1;
    private int CREATE_NEW_ADDRESS=2;
    private int mCheckedPosition = 0;
    private View rootView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADDRESS_LIST:
                    if (mDialog!=null)
                    mDialog.dismiss();
                    ToolUtils.setLog("addressList：" + addressList.size());
                    if (mAddress!=null)
                        for (int i = 0; i < addressList.size(); i++)
                        {
                            if (addressList.get(i).getId()==mAddress.getId())
                            {
                                mCheckedPosition=i;
                            }
                        }
                    loadingView.setVisibility(View.GONE);
                    addressAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public static AddrSelectFragment newInstance(String index, int mpage,Address address) {
        AddrSelectFragment fragment = new AddrSelectFragment();
        Bundle args = new Bundle();
        args.putString("index", index);
        args.putInt("page", mpage);
        args.putSerializable("address", address);
        fragment.setArguments(args);
        return fragment;
    }

    public AddrSelectFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            index = getArguments().getString("index");
            mStatus = getArguments().getInt("page");
            mAddress=(Address)getArguments().getSerializable("address");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_addr_select, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view) {

        mDialog=CustomLoadingDialog.setLoadingDialog(getActivity(),"loading");

        loadingView=(LinearLayout)view.findViewById(R.id.loadingView);
        mListview = (ListView) view.findViewById(R.id.lv_addresses);
        addressAdapter = new AddressAdapter(getActivity(), addressList);
        mListview.setAdapter(addressAdapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                mCheckedPosition = position;
                addressAdapter.notifyDataSetChanged();
                ((MainActivity) getActivity()).popToStack(AddrSelectFragment.this);
                addressListener.onDefalueAddressChange(addressList.get(position));
            }
        });
        view.findViewById(R.id.bt_new_address).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final NewAddrFragment newAddrFragment = NewAddrFragment.newInstance(0,"", "","","",0,CREATE_NEW_ADDRESS);
                ((MainActivity) getActivity()).navigationToFragment(newAddrFragment);
                newAddrFragment.setAddrSaveSuccessListener(new NewAddrFragment.AddrSaveSuccessListener() {
                    @Override
                    public void onSaveListener(JSONObject receiverObj,int status,int yfprice,String province, String city, String area) {
                        if (receiverObj != null) {
                            int id = receiverObj.optInt("id");
                            int user_id = receiverObj.optInt("user_id");
                            String name = receiverObj.optString("name");
                            String phone = receiverObj.optString("phone");
                            int provider_id = receiverObj.optInt("provider_id");
                            String address = receiverObj.optString("address");
                            boolean is_default = receiverObj.optBoolean("is_default");
                            Address addr = new Address(id, name, is_default, phone, user_id, address, provider_id,yfprice);
                            addr.setProvince(province);
                            addr.setCity(city);
                            addr.setArea(area);
                            addressAdapter.add(addr);
                            ToolUtils.setLog(addr.toString());
                            ((MainActivity) getActivity()).popToStack(newAddrFragment);
                            ((MainActivity) getActivity()).popToStack(AddrSelectFragment.this);
                            addressListener.onDefalueAddressChange(addr);
                            mCheckedPosition=addressAdapter.getCount()-1;
                        }
                    }
                });
            }
        });
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token = mSharedPreferences.getString("token", null);
        FetchData();

        addressAdapter.setOnInViewClickListener(R.id.tv_delete, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Address address = (Address) values;
                int id = address.getId();
                mDialog=CustomLoadingDialog.setLoadingDialog(getActivity(),"删除中");
                String url =ZhaiDou.ORDER_RECEIVER_URL+ id;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (mDialog!=null)
                        mDialog.dismiss();
                        if (jsonObject!=null){
                            int status=jsonObject.optInt("status");
                            if (status==201)
                            {
                                addressList.remove(address);
                                addressAdapter.notifyDataSetChanged();
                                if (addressList.size() == 0)
                                {
                                    addressListener.onDeleteFinishAddress();
                                }
                            }
                            String message=jsonObject.optString("message");
                            ShowToast(message);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (mDialog!=null)
                            mDialog.dismiss();
                        ShowToast("加载失败");
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("SECAuthorization", token);
                        return headers;
                    }
                };
                mRequestQueue.add(request);
            }
        });
        addressAdapter.setOnInViewClickListener(R.id.tv_edit, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                Address address=(Address)values;
                int id=address.getId();
                String name=address.getName();
                String phone=address.getPhone();
                String addr=address.getAddress();
                String location=address.getProvince()+"-"+address.getCity()+"-"+address.getArea();
                int provider_id=address.getProvider_id();
                final NewAddrFragment newAddrFragment = NewAddrFragment.newInstance(id,name,phone,location,addr,provider_id,UPDATE_ADDRESS_INFO);
                ((MainActivity)getActivity()).navigationToFragment(newAddrFragment);

                newAddrFragment.setAddrSaveSuccessListener(new NewAddrFragment.AddrSaveSuccessListener() {

                    @Override
                    public void onSaveListener(JSONObject receiver, int status, int yfPrice, String province, String city, String area)
                    {
                        if (status==UPDATE_ADDRESS_INFO){
                            int id = receiver.optInt("id");
                            String phone = receiver.optString("phone");
                            String addr = receiver.optString("address");
                            int provider_id=receiver.optInt("provider_id");
                            int user_id = receiver.optInt("user_id");
                            String name = receiver.optString("name");
                            boolean is_default=receiver.optBoolean("is_default");
                            Address address1=new Address(id,name,is_default,phone,user_id,addr,provider_id,yfPrice);
                            address1.setProvince(province);
                            address1.setCity(city);
                            address1.setArea(area);
                            addressAdapter.remove(position);
                            addressAdapter.add(address1,position);
                            ((MainActivity)getActivity()).popToStack(newAddrFragment);
                        }
                    }
                });
            }
        });
    }

    public void setAddressListener(AddressListener addressListener) {
        this.addressListener = addressListener;
    }

    public interface AddressListener {
        public void onDefalueAddressChange(Address address);
        public void onDeleteFinishAddress();
    }

    private void FetchData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.ORDER_RECEIVER_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                mDialog.dismiss();
                JSONArray receiversArr = jsonObject.optJSONArray("receivers");
                ToolUtils.setLog(jsonObject.toString());
                if (receiversArr != null && receiversArr.length() > 0)
                {
                    for (int i = 0; i < receiversArr.length(); i++) {
                        JSONObject receiverObj = receiversArr.optJSONObject(i);
                        String phone = receiverObj.optString("phone");
                        int user_id = receiverObj.optInt("user_id");
                        String addr = receiverObj.optString("address");
                        String name = receiverObj.optString("name");
                        int id = receiverObj.optInt("id");
                        int provider_id=receiverObj.optInt("provider_id");
                        String province=receiverObj.optString("parent_name");
                        String city=receiverObj.optString("city_name");
                        String area=receiverObj.optString("provider_name");
                        boolean is_default=receiverObj.optBoolean("is_default");
                        int price=receiverObj.optInt("price");
                        if (is_default)
                        {
                            mCheckedPosition=i;
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
                }
                else
                {
                    loadingView.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                if (!TextUtils.isEmpty(token))
                    headers.put("SECAuthorization",token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public class AddressAdapter extends BaseListAdapter<Address> {
        public AddressAdapter(Context context, List<Address> list) {
            super(context, list);
        }
        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_addresses_select_list, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_addr_username);
            TextView tv_mobile = ViewHolder.get(convertView, R.id.tv_addr_mobile);
            TextView tv_addr = ViewHolder.get(convertView, R.id.tv_addr);
            TextView tv_defalue_hint=ViewHolder.get(convertView,R.id.tv_defalue_hint);
            ImageView mDefalueIcon=ViewHolder.get(convertView,R.id.iv_addr_defalue);
            View view=ViewHolder.get(convertView,R.id.lineBg);
            if (position==0)
            {
                view.setVisibility(View.GONE);
            }
            else
            {
                view.setVisibility(View.VISIBLE);
            }

            Address address = getList().get(position);
            tv_name.setText("收件人："+address.getName());
            tv_mobile.setText("电话："+address.getPhone());
            if (address.getProvince()==null)
            {
                tv_addr.setText("地址："+address.getAddress());
            }
            else
            {
                tv_addr.setText("地址："+address.getProvince()+address.getCity()+address.getArea()+address.getAddress());
            }
            if (address.isIs_default())
            {
                tv_defalue_hint.setVisibility(View.VISIBLE);
            }
            else
            {
                tv_defalue_hint.setVisibility(View.GONE);
            }
            mDefalueIcon.setImageResource(mCheckedPosition==position?R.drawable.icon_address_checked:R.drawable.icon_address_normal);
            return convertView;
        }
    }

    @Override
    public void onDestroyView()
    {
        if (addressList!=null&&addressList.size()>0)
        {
            if (addressList.size()>mCheckedPosition)
            {
                if (addressListener!=null)
                    addressListener.onDefalueAddressChange(addressList.get(mCheckedPosition));
            }
        }
        super.onDestroyView();
    }
}
