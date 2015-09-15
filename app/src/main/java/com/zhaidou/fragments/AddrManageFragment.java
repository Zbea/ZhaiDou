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
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Address;
import com.zhaidou.utils.SharedPreferencesUtil;
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

public class AddrManageFragment extends BaseFragment implements View.OnClickListener {
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
    private EditText et_mobile, et_addr, et_name;
    private TextView tv_save, tv_edit, tv_addr_username, tv_addr_mobile, tv_addr, tv_delete;
    private String token;
    private SharedPreferences mSharedPreferences;

    private AddressListener addressListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private ListView mListview;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<Address>();
    private final int UPDATE_ADDRESS_LIST = 0;

    private int UPDATE_ADDRESS_INFO=1;
    private int CREATE_NEW_ADDRESS=2;
    private int STATUS_FROM_ORDER=3;
    private int STATUS_FROM_PERSONAL=4;
    private int mCheckedPosition = 0;
    private View rootView;
    private LinearLayout loadingView;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADDRESS_LIST:
                    if (mDialog!=null)
                    mDialog.dismiss();
                    loadingView.setVisibility(View.GONE);
                    addressAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public static AddrManageFragment newInstance(String nickname, String mobile, String address, String profileId, int status) {
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

    public AddrManageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNickName = getArguments().getString(ARG_NickName);
            mMobile = getArguments().getString(ARG_MOBILE);
            mAddress = getArguments().getString(ARG_ADDRESS);
            mProfileId = getArguments().getString(ARG_PROFILE_ID);
            mStatus = getArguments().getInt(ARG_STATUS);
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
            rootView = inflater.inflate(R.layout.fragment_addr_manage, container, false);
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
        view.findViewById(R.id.bt_new_address).setOnClickListener(this);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        token = (String) SharedPreferencesUtil.getData(getActivity(),"token","");
        FetchData();
        addressAdapter.setOnInViewClickListener(R.id.ll_defalue, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {

                mCheckedPosition = position;
                addressAdapter.notifyDataSetChanged();
                if (mStatus==STATUS_FROM_ORDER)
                {
                    ((MainActivity) getActivity()).popToStack(AddrManageFragment.this);
                }
            }
        });
        addressAdapter.setOnInViewClickListener(R.id.tv_delete, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Address address = (Address) values;
                final int id = address.getId();
                final Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);

                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint, null);
                TextView textView = (TextView) view.findViewById(R.id.tv_msg);
                textView.setText("是否删除地址?");
                TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
                cancelTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                TextView okTv = (TextView) view.findViewById(R.id.okTv);
                okTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        mDialog.show();
                        String url =ZhaiDou.ORDER_RECEIVER_URL + id;
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                if (mDialog!=null)
                                    mDialog.dismiss();
                                if (jsonObject!=null){
                                    int status=jsonObject.optInt("status");
                                    if (status==201){
                                        addressList.remove(address);
                                        addressAdapter.notifyDataSetChanged();
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
                                ShowToast("网络异常");
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<String, String>();
                                headers.put("SECAuthorization",token);
                                return headers;
                            }
                        };
                        mRequestQueue.add(request);
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();

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
                final AddrNewAddrFragment newAddrFragment = AddrNewAddrFragment.newInstance(id, name, phone, location, addr, provider_id, UPDATE_ADDRESS_INFO);
                ((MainActivity)getActivity()).navigationToFragment(newAddrFragment);

                newAddrFragment.setAddrSaveSuccessListener(new AddrNewAddrFragment.AddrSaveSuccessListener() {
                    @Override
                    public void onSaveListener(JSONObject receiverObj, int status,double yfprice, String province, String city, String area) {
                        if (status==UPDATE_ADDRESS_INFO){
                            int id = receiverObj.optInt("id");
                            String phone = receiverObj.optString("phone");
                            String addr = receiverObj.optString("address");
                            int provider_id=receiverObj.optInt("provider_id");
                            int user_id = receiverObj.optInt("user_id");
                            String name = receiverObj.optString("name");
                            boolean is_default=receiverObj.optBoolean("is_default");
                            Address address1=new Address(id,name,is_default,phone,user_id,addr,provider_id,yfprice);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                hideInputMethod();
                String name = et_name.getText().toString();
                String mobile = et_mobile.getText().toString();
                String address = et_addr.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "收货人信息不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(getActivity(), "联系方式不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(address)) {
                    Toast.makeText(getActivity(), "收货地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNickName = name;
                mMobile = mobile;
                mAddress = address;
                new MyTask().execute(name, mobile, address, mProfileId);
                break;
            case R.id.tv_edit:
                ll_edit_addr.setVisibility(View.VISIBLE);
                ll_manage_address.setVisibility(View.GONE);
                et_addr.setHint(mAddress);
                et_mobile.setHint(mMobile);
                et_name.setHint(mNickName);
                break;
            case R.id.tv_delete:
                new MyTask().execute(tv_addr_username.getText().toString(), tv_addr_mobile.getText().toString(), "", mProfileId);
                break;
            case R.id.bt_new_address:
                final AddrNewAddrFragment newAddrFragment = AddrNewAddrFragment.newInstance(0, "", "", "", "", 0, CREATE_NEW_ADDRESS);
                ((MainActivity) getActivity()).navigationToFragment(newAddrFragment);
                newAddrFragment.setAddrSaveSuccessListener(new AddrNewAddrFragment.AddrSaveSuccessListener() {
                    @Override
                    public void onSaveListener(JSONObject receiverObj,int status,double yfprice, String province, String city, String area) {
                        if (receiverObj != null&&CREATE_NEW_ADDRESS==status) {
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
                            ((MainActivity) getActivity()).popToStack(newAddrFragment);
                        }

                    }
                });
                break;
            case R.id.rl_back:
                break;
        }
    }


    private class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        }

        @Override
        protected String doInBackground(String... strings) {

            String s = null;
            try {
                s = executeHttpPost(strings[0], strings[1], strings[2], strings[3]);
            } catch (Exception e) {

            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                mDialog.dismiss();
                JSONObject json = new JSONObject(s);
                JSONObject profile = json.optJSONObject("profile");
                String mobile = profile.optString("mobile");
                String address = profile.optString("address2");
//                addressListener.onAddressDataChange(mNickName, mMobile, address);
            } catch (Exception e) {

            }
        }
    }

    public String executeHttpPost(String name, String mobile, String addr, String id) throws Exception {
        Log.i("name--->", name == null ? "" : name);
        Log.i("mobile--->", mobile == null ? "" : mobile);
        Log.i("addr--->", addr == null ? "" : addr);
        Log.i("id--->", id == null ? "" : id);
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_EDIT_PROFILE_URL + id);
            request.addHeader("SECAuthorization", token);


            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("_method", "PUT"));
//            String newStr = new String(old.getBytes("UTF-8"));
            parameters.add(new BasicNameValuePair("profile[first_name]", name));
            parameters.add(new BasicNameValuePair("profile[mobile]", mobile));
            parameters.add(new BasicNameValuePair("profile[address2]", addr));
            parameters.add(new BasicNameValuePair("profile[id]", id));

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
                    for (int i = 0; i < receiversArr.length(); i++)
                    {
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
                        double price=receiverObj.optDouble("price");
                        if (is_default)
                            mCheckedPosition=i;
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
                }else {
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
                convertView = mInflater.inflate(R.layout.item_addresses_list, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_addr_username);
            TextView tv_mobile = ViewHolder.get(convertView, R.id.tv_addr_mobile);
            TextView tv_addr = ViewHolder.get(convertView, R.id.tv_addr);
            TextView tv_defalue = ViewHolder.get(convertView, R.id.tv_defalue_addr);
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
            if (mStatus==STATUS_FROM_ORDER){
                tv_defalue_hint.setVisibility(View.GONE);
                if (address.isIs_default())
                   tv_defalue_hint.setVisibility(View.VISIBLE);
                tv_defalue.setText("选择地址");
            }
            mDefalueIcon.setImageResource(mCheckedPosition==position?R.drawable.icon_address_checked:R.drawable.icon_address_normal);
            return convertView;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (addressAdapter.getCount()>mCheckedPosition)
        {
            Address address=addressAdapter.getItem(mCheckedPosition);
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,ZhaiDou.ORDER_RECEIVER_URL+"/"+address.getId()+"/set_default",new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    if (jsonObject!=null){
                        JSONObject receiver=jsonObject.optJSONObject("receiver");
                        int id=receiver.optInt("id");
                        String phone=receiver.optString("phone");
                        String updated_at=receiver.optString("updated_at");
                        String addr=receiver.optString("address");
                        int provider_id=receiver.optInt("provider_id");
                        String name=receiver.optString("name");
                        String created_at=receiver.optString("created_at");
                        boolean is_default=receiver.optBoolean("is_default");
                        int user_id=receiver.optInt("user_id");
                        int price=receiver.optInt("price");
                        Address address1=new Address(id,name,is_default,phone,user_id,addr,provider_id,price);
                        address1.setUpdated_at(updated_at);
                        address1.setCreated_at(created_at);
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
            mRequestQueue.add(request);
        }
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_address_manage));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_address_manage));
    }
}
