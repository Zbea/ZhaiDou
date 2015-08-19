package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Address;
import com.zhaidou.model.CartItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceEditText;
import com.zhaidou.view.TypeFaceTextView;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by roy on 15/7/24.
 */
public class ShopOrderOkFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private String bzInfo_Str;

    private TypeFaceTextView backBtn, titleTv;
    private Button okBtn;
    private LinearLayout orderAddressInfoLine, orderAddressNullLine, orderAddressEditLine;
    private LinearLayout orderGoodsListLine;
    private TypeFaceEditText bzInfo;
    private TypeFaceTextView moneyTv, moneyYfTv, moneyTotalTv, moneyNumTv;
    private TextView addressNameTv, addressPhoneTv, addressinfoTv;
    private ArrayList<CartItem> items;
    private List<CartItem> erroritems=new ArrayList<CartItem>();
    private List<Address> addressList = new ArrayList<Address>();
    private String Str_token;

    private int num = 0;
    private double money = 0;
    private int moneyYF = 0;
    private double totalMoney = 0;

    private RequestQueue mRequestQueue;
    private int STATUS_FROM_ORDER = 3;
    private final int UPDATE_DEFALUE_ADDRESS_INFO = 0;
    private boolean isOSaleBuy;//是否已经购买过零元特卖
    private boolean isOSale;//是否含有零元特卖
    private boolean isljOsale;//是否是来自立即购买的零元特卖

    private Address address;
    private CreatCartDB creatCartDB;
    private String token;
    private int userId;
    private int flags=0;//1代表零元特卖的立即购买

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_DEFALUE_ADDRESS_INFO:
                    mDialog.dismiss();

                    orderAddressInfoLine.setVisibility(View.VISIBLE);
                    orderAddressEditLine.setVisibility(View.VISIBLE);
                    orderAddressNullLine.setVisibility(View.GONE);

                    address = addressList.get(0);
                    setYFMoney(address);
                    addressPhoneTv.setText("收件人：" + address.getPhone());
                    addressNameTv.setText("电话：" + address.getName());
                    addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
                    break;
                case 2:
                    mDialog.dismiss();
                    orderAddressInfoLine.setVisibility(View.GONE);
                    orderAddressEditLine.setVisibility(View.GONE);
                    orderAddressNullLine.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    mDialog.dismiss();
                    Toast.makeText(mContext, "抱歉,提交订单失败", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    mDialog.dismiss();

                    for (int i = 0; i < items.size(); i++) {
                        CreatCartTools.deleteByData(creatCartDB, items.get(i));
                    }

                    if(isljOsale)//清除购物车中的零元特卖
                    {
                        List<CartItem> itemsAll=CreatCartTools.selectByAll(creatCartDB,userId);
                        for (int i = 0; i < itemsAll.size(); i++)
                        {
                            if (itemsAll.get(i).isOSale.equals("true"))
                            {
                                CreatCartTools.deleteByData(creatCartDB, itemsAll.get(i));
                            }
                        }
                    }

                    //发送刷新购物车广播
                    Intent intent = new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
                    mContext.sendBroadcast(intent);
                    //关闭本页面
                    ((MainActivity) getActivity()).popToStack(ShopOrderOkFragment.this);

                    String result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject orderObj = jsonObject.optJSONObject("order");
                        int orderId = orderObj.optInt("id");
                        int amount = orderObj.optInt("amount");
                        int fare = moneyYF;
                        ShopPaymentFragment shopPaymentFragment = ShopPaymentFragment.newInstance(orderId, amount, fare,15*60,null,1);
                        ((MainActivity) getActivity()).navigationToFragment(shopPaymentFragment);
//                        Intent intent1=new Intent(getActivity(), PayDemoActivity.class);
//                        intent1.putExtra("id",orderId);
//                        intent1.putExtra("amount",amount);
//                        startAnimActivity(intent1);
                    } catch (Exception e) {

                    }
                    break;
                case 5:
                    if (isOSaleBuy)
                    {
                        mDialog.dismiss();
                        Toast.makeText(mContext, "抱歉,您已经购买过零元特卖商品,今天已经不能购买", Toast.LENGTH_LONG).show();
                    }
                    else {
                        commit();
                    }
                    break;
                case 6:
                    mDialog.dismiss();
                    String json=msg.obj.toString();
                    String[] arrayStr =new String[]{};
                    arrayStr=json.split(",");

                    for (int i = 0; i <items.size() ; i++)
                    {
                        if (items.get(i).sizeId==Integer.valueOf(arrayStr[1]))
                        {
                            ToolUtils.setToast(mContext,items.get(i).name+"的"+items.get(i).size+"规格库存不足");
                        }
                    }
                    break;
            }
        }
    };


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            bzInfo_Str = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(ShopOrderOkFragment.this);
                    break;

                case R.id.jsOkBtn:
                    if (address!=null)
                    {
                        mDialog=CustomLoadingDialog.setLoadingDialog(mContext,"提交中");
                        for (int i = 0; i <items.size(); i++)
                        {
                            if (items.get(i).isOSale.equals("true"))
                            {
                                isOSale=true;
                            }
                        }
                        if (isOSale)
                        {
                            FetchOSaleData(5);//判断零元特卖是否已经当天购买过
                        }
                        else
                        {
                            commit();
                        }
                    } else {
                        ToolUtils.setToast(mContext, "抱歉,您未填写收货地址");
                    }

                    break;
                case R.id.jsEditAddressBtn:
                    AddrSelectFragment addrManageFragment = AddrSelectFragment.newInstance("", STATUS_FROM_ORDER, address);
                    ((MainActivity) getActivity()).navigationToFragment(addrManageFragment);
                    addrManageFragment.setAddressListener(new AddrSelectFragment.AddressListener() {
                        @Override
                        public void onDefalueAddressChange(Address maddress) {
                            ToolUtils.setLog(maddress.toString());
                            address = maddress;
                            setYFMoney(address);
                            addressPhoneTv.setText("收件人：" + address.getPhone());
                            addressNameTv.setText("电话：" + address.getName());
                            addressinfoTv.setText(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
                        }

                        @Override
                        public void onDeleteFinishAddress() {
                            orderAddressInfoLine.setVisibility(View.GONE);
                            orderAddressNullLine.setVisibility(View.VISIBLE);
                            orderAddressEditLine.setVisibility(View.GONE);
                        }
                    });
                    break;
                case R.id.jsAddressNullLine:

                    final NewAddrFragment newAddrFragment = NewAddrFragment.newInstance(0, "", "", "", "", 0, 2);
                    ((MainActivity) getActivity()).navigationToFragment(newAddrFragment);
                    newAddrFragment.setAddrSaveSuccessListener(new NewAddrFragment.AddrSaveSuccessListener() {
                        @Override
                        public void onSaveListener(JSONObject receiver, int status,int yfprice,String province, String city, String area)
                        {
                            int id = receiver.optInt("id");
                            int user_id = receiver.optInt("user_id");
                            String name = receiver.optString("name");
                            String phone = receiver.optString("phone");
                            int provider_id = receiver.optInt("provider_id");
                            String addresss = receiver.optString("address");
                            boolean is_default = receiver.optBoolean("is_default");
                            Address addr = new Address(id, name, is_default, phone, user_id, addresss, provider_id,yfprice);
                            addr.setProvince(province);
                            addr.setCity(city);
                            addr.setArea(area);
                            address=addr;
                            setYFMoney(addr);
                            orderAddressInfoLine.setVisibility(View.VISIBLE);
                            orderAddressNullLine.setVisibility(View.GONE);
                            orderAddressEditLine.setVisibility(View.VISIBLE);
                            addressPhoneTv.setText("收件人：" + addr.getPhone());
                            addressNameTv.setText("电话：" + addr.getName());
                            addressinfoTv.setText(address.getProvince()+address.getCity()+address.getArea()+addr.getAddress());
                            ((MainActivity) getActivity()).popToStack(newAddrFragment);
                        }
                    });
                    break;
            }
        }
    };


    public static ShopOrderOkFragment newInstance(String page, int index) {
        ShopOrderOkFragment fragment = new ShopOrderOkFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopOrderOkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
            flags = getArguments().getInt("flags");
            items = (ArrayList<CartItem>) getArguments().getSerializable("goodsList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.shop_settlement_page, container, false);
            mContext = getActivity();
            initView();
        }
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }

    /**
     * 初始化数据
     */
    private void initView() {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        mRequestQueue = Volley.newRequestQueue(getActivity());
        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_order_ok_text);

        okBtn = (Button) mView.findViewById(R.id.jsOkBtn);
        okBtn.setOnClickListener(onClickListener);

        if (flags==1)
        {
            isljOsale=true;
        }
        moneyTv = (TypeFaceTextView) mView.findViewById(R.id.jsPriceTotalTv);
        moneyYfTv = (TypeFaceTextView) mView.findViewById(R.id.jsPriceYfTv);
        moneyTotalTv = (TypeFaceTextView) mView.findViewById(R.id.jsTotalMoney);
        moneyNumTv = (TypeFaceTextView) mView.findViewById(R.id.jsTotalNum);

        addressinfoTv = (TextView) mView.findViewById(R.id.jsAddressinfoTv);
        addressNameTv = (TextView) mView.findViewById(R.id.jsAddressNameTv);
        addressPhoneTv = (TextView) mView.findViewById(R.id.jsAddressPhoneTv);

        bzInfo = (TypeFaceEditText) mView.findViewById(R.id.jsEditBzInfo);
        bzInfo.addTextChangedListener(textWatcher);

        orderAddressInfoLine = (LinearLayout) mView.findViewById(R.id.jsAddressInfoLine);
        orderAddressNullLine = (LinearLayout) mView.findViewById(R.id.jsAddressNullLine);
        orderAddressNullLine.setOnClickListener(onClickListener);
        orderAddressEditLine = (LinearLayout) mView.findViewById(R.id.jsEditAddressBtn);
        orderAddressEditLine.setOnClickListener(onClickListener);

        orderGoodsListLine = (LinearLayout) mView.findViewById(R.id.orderGoodsList);

        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);

        creatCartDB = new CreatCartDB(mContext);

        initData();

        FetchAddressData();
    }

    /**
     * 设置运费
     */
    private void setYFMoney(Address area) {
        moneyYF = area.getPrice();
        moneyYfTv.setText("￥" + moneyYF);

        ToolUtils.setLog("运费：" + area.getPrice());

        DecimalFormat df = new DecimalFormat("##.0");
        totalMoney = Double.parseDouble(df.format(money + moneyYF));
        moneyTotalTv.setText("￥" + totalMoney);
    }

    /**
     * 设置商品信息
     */
    private void initData() {
        if (items.size() > 0) {
            addGoodsView();
        }

        for (int i = 0; i < items.size(); i++) {
            CartItem cartItem = items.get(i);
            num = num + cartItem.num;
            money = money + cartItem.num * cartItem.currentPrice;
        }
        moneyNumTv.setText("" + num);

        DecimalFormat df = new DecimalFormat("##.0");
        money = Double.parseDouble(df.format(money));
        totalMoney = Double.parseDouble(df.format(money + 0));
        moneyTv.setText("￥" + money);
        moneyTotalTv.setText("￥" + totalMoney);
        moneyYfTv.setText("￥" + 0);

    }

    ;

    /**
     * 添加商品集合
     */
    private void addGoodsView() {
        orderGoodsListLine.removeAllViews();
        for (int position = 0; position < items.size(); position++) {
            View childeView = LayoutInflater.from(mContext).inflate(R.layout.shop_settlement_goods_item, null);
            TypeFaceTextView itemName = (TypeFaceTextView) childeView.findViewById(R.id.orderItemNameTv);
            TypeFaceTextView itemSize = (TypeFaceTextView) childeView.findViewById(R.id.orderItemSizeTv);
            TextView itemCurrentPrice = (TextView) childeView.findViewById(R.id.orderItemCurrentPrice);
            TextView itemFormalPrice = (TextView) childeView.findViewById(R.id.orderItemFormalPrice);
            final TypeFaceTextView itemNum = (TypeFaceTextView) childeView.findViewById(R.id.orderItemNum);
            ImageView itemImage = (ImageView) childeView.findViewById(R.id.orderImageItemTv);
            ImageView itemLine = (ImageView) childeView.findViewById(R.id.orderItemLine);

            if (items.size() > 1) {
                if (position == items.size() - 1) {
                    itemLine.setVisibility(View.GONE);
                }
                if (position == 0) {
                    itemLine.setVisibility(View.VISIBLE);
                }
            } else {
                itemLine.setVisibility(View.GONE);
            }

            final CartItem cartItem = items.get(position);

            itemName.setText(cartItem.name);
            itemSize.setText(cartItem.size);
            itemCurrentPrice.setText("￥ " + cartItem.currentPrice);
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥ " + cartItem.formalPrice);
            itemNum.setText("" + cartItem.num);
            ToolUtils.setImageCacheUrl(cartItem.imageUrl, itemImage);

            orderGoodsListLine.addView(childeView);
        }
    }

    /**
     * 提交订单接口
     */
    private void commit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = FetchRequset();
                if (result != null) {
                    try {
                        JSONObject jsonObject=new JSONObject(result);
                        int status=jsonObject.optInt("status");
                        if (status==201)
                        {
                            JSONObject orderObj=jsonObject.optJSONObject("order");
                            int id=orderObj.optInt("id");
                            String number=orderObj.optString("number");
                            int amount=orderObj.optInt("amount");
                            int count =orderObj.optInt("count");

                            Message message=new Message();
                            message.what=4;
                            message.obj=result;
                            handler.sendMessage(message);
                        }
                        if (status==400)
                        {
                            String errorArr=jsonObject.optString("order_items.count");
                            Message message=new Message();
                            message.what=6;
                            message.obj=errorArr;
                            handler.sendMessage(message);

                        }
                        if (status==401)
                        {

                        }

                    }catch (Exception e){
                    }


                    } else
                    {
                        handler.sendEmptyMessage(3);
                    }
            }
        }).start();

    }

    private String FetchRequset() {
        String result = null;
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();

            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.orderCommitUrl);
            request.addHeader("SECAuthorization", token);

            // 创建名/值组列表
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("sale_order[receiver_id]", "" + address.getId()));

            params.add(new BasicNameValuePair("sale_order[node]", bzInfo_Str));
            for (int i = 0; i < items.size(); i++) {
                params.add(new BasicNameValuePair("sale_order[order_items_attributes[" + i + "][merchandise_id]]", items.get(i).id + ""));
                params.add(new BasicNameValuePair("sale_order[order_items_attributes[" + i + "][specification_id]]", items.get(i).sizeId + ""));
                params.add(new BasicNameValuePair("sale_order[order_items_attributes[" + i + "][count]]", items.get(i).num + ""));
            }


            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    params, HTTP.UTF_8);
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
            result = sb.toString();
            Log.i("result------------>", result.toString());
            return result;

        } catch (Exception e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 请求收货地址信息
     */
    private void FetchAddressData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.ORDER_RECEIVER_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                ToolUtils.setLog(jsonObject.toString());
                if (jsonObject != null) {
                    JSONArray receivers = jsonObject.optJSONArray("receivers");
                    if (receivers != null && receivers.length() > 0) {
                        for (int i = 0; i < receivers.length(); i++) {
                            JSONObject receiver = receivers.optJSONObject(i);
                            int id = receiver.optInt("id");
                            int user_id = receiver.optInt("user_id");
                            String name = receiver.optString("name");
                            String phone = receiver.optString("phone");
                            int provider_id = receiver.optInt("provider_id");
                            String addr = receiver.optString("address");
                            boolean is_default = receiver.optBoolean("is_default");
                            String province = receiver.optString("parent_name");
                            String city = receiver.optString("city_name");
                            String area = receiver.optString("provider_name");
                            int price = receiver.optInt("price");
                            Address address = new Address(id, name, is_default, phone, user_id, addr, provider_id, price);
                            address.setProvince(province);
                            address.setCity(city);
                            address.setArea(area);
                            addressList.add(address);
                        }
                        Message message = new Message();
                        message.what = UPDATE_DEFALUE_ADDRESS_INFO;
                        message.obj = addressList;
                        handler.sendMessage(message);
                    } else {
                        handler.sendEmptyMessage(2);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
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

    /**
     * 零元特卖是否购买请求
     */
    public void FetchOSaleData(final int i) {
        String url = ZhaiDou.orderCheckOSaleUrl;
        Log.i("url---------------------->", url);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                if (jsonObject != null) {
                    isOSaleBuy = jsonObject.optBoolean("flag");
                    Log.i("isOSaleBuy---------------------->", "" + isOSaleBuy);
                }
                if (i == 5) {
                    handler.sendEmptyMessage(5);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null)
                    mDialog.dismiss();
                Toast.makeText(getActivity(), "抱歉,请求失败", Toast.LENGTH_SHORT).show();
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
}
