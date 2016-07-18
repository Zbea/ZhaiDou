package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.zhaidou.model.Coupon;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DateUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class ShopOrderSelectCouponFragment extends BaseFragment implements View.OnClickListener
{

    private String index;
    private int mStatus;
    private View rootView;
    private LinearLayout exchangeBtn, noCouponBtn;
    private ImageView noCouponIv;
    private int userId;
    private String userName;
    private String token;
    private OnCouponListener onCouponListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private TextView titleTv,noCouponTv;
    private ListView mListview,noCouponListView;
    private LinearLayout couponNullView;

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CouponAdapter couponAdapter;
    private CouponAdapter noCouponAdapter;

    private final int UPDATE_COUPON_LIST = 0;
    private final int UPDATE_REDEEM_COUPON_RESULT = 1;

    private int mCheckedPosition = -1;

    private List<Coupon> items = new ArrayList<Coupon>();
    private List<Coupon> itemsNo = new ArrayList<Coupon>();
    private Coupon mCoupon;
    private String mDatas;
    private String couponCode;
    private Coupon redeemCoupon;


    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_COUPON_LIST:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (mCoupon != null)
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (items.get(i).id == mCoupon.id)
                            {
                                mCheckedPosition = i;
                            }
                        }
                    }
                    loadingView.setVisibility(View.GONE);
                    couponNullView.setVisibility(View.GONE);
                    couponAdapter.setList(items);
                    noCouponAdapter.setList(itemsNo);
                    noCouponTv.setVisibility(itemsNo.size()>0?View.VISIBLE:View.GONE);
                    break;
                case UPDATE_REDEEM_COUPON_RESULT:

                    if (mDialog != null)
                        mDialog.dismiss();
                    ToolUtils.setToast(mContext,"兑换成功");
                    for (int i = 0; i < items.size(); i++)
                    {
                        if (items.get(i).id == mCoupon.id)
                        {
                            mCheckedPosition = i;
                        }
                    }
                    loadingView.setVisibility(View.GONE);
                    couponNullView.setVisibility(View.GONE);
                    couponAdapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_no_coupon:
                    mCheckedPosition = -1;
                    mStatus = 1;
                    couponAdapter.notifyDataSetChanged();
                    ((BaseActivity) getActivity()).popToStack(ShopOrderSelectCouponFragment.this);
                    onCouponListener.onDefaultCouponChange(null);
                    break;
                case R.id.bt_add_coupon:
                    addCoupon();
                    break;
                case R.id.nullReload:
                    FetchData();
                    break;
                case R.id.netReload:
                    FetchData();
                    break;

            }
        }
    };

    public static ShopOrderSelectCouponFragment newInstance(String index, int mpage, Coupon coupon, String dataJsonObject)
    {
        ShopOrderSelectCouponFragment fragment = new ShopOrderSelectCouponFragment();
        Bundle args = new Bundle();
        args.putString("index", index);
        args.putInt("page", mpage);
        args.putSerializable("coupon", coupon);
        args.putString("dataJsonObject", dataJsonObject);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopOrderSelectCouponFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            index = getArguments().getString("index");
            mStatus = getArguments().getInt("page");
            mCoupon = (Coupon) getArguments().getSerializable("coupon");
            mDatas = getArguments().getString("dataJsonObject");
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
            rootView = inflater.inflate(R.layout.shop_settlement_select_coupon_page, container, false);
            initView(rootView);
        }
        return rootView;
    }

    private void initView(View view)
    {
        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText("可用优惠卷");

        noCouponTv= (TypeFaceTextView) view.findViewById(R.id.tv_noCoupon);

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) view.findViewById(R.id.nullNetline);
        nullView = (LinearLayout) view.findViewById(R.id.nullline);

        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);

        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        couponNullView= (LinearLayout) view.findViewById(R.id.couponNullLine);
        mListview = (ListView) view.findViewById(R.id.lv_coupon);
        couponAdapter = new CouponAdapter(mContext, items);
        mListview.setAdapter(couponAdapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                mCheckedPosition = position;
                couponAdapter.notifyDataSetChanged();
                ((BaseActivity) getActivity()).popToStack(ShopOrderSelectCouponFragment.this);
                onCouponListener.onDefaultCouponChange(items.get(position));
            }
        });

        noCouponListView = (ListView) view.findViewById(R.id.lv_noCoupon);
        noCouponAdapter = new CouponAdapter(mContext, itemsNo);
        noCouponListView.setAdapter(noCouponAdapter);

        exchangeBtn = (LinearLayout) rootView.findViewById(R.id.bt_add_coupon);
        exchangeBtn.setOnClickListener(onClickListener);

        noCouponBtn = (LinearLayout) rootView.findViewById(R.id.btn_no_coupon);
        noCouponBtn.setOnClickListener(onClickListener);
        noCouponIv= (ImageView) rootView.findViewById(R.id.iv_addr_defalue);

        mRequestQueue = ZDApplication.newRequestQueue();
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        userName = (String) SharedPreferencesUtil.getData(mContext, "nickName", "");
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");

        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchData();
        } else
        {
            if (mDialog != null)
                mDialog.dismiss();
            nullView.setVisibility(View.GONE);
            nullNetView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 添加优惠券
     */
    private void addCoupon()
    {
        DialogUtils dialogUtils=new DialogUtils(mContext);
        dialogUtils.showCouponDialog(new DialogUtils.PositiveListener2()
        {
            @Override
            public void onPositive(Object o)
            {
                couponCode=o.toString().trim();
                if (mDialog!=null)
                    mDialog.show();
                FetchRedeem();
            }
        },null);

    }


    /**
     * 加载失败
     */
    private void loadingFail()
    {
        if (mDialog != null)
            mDialog.dismiss();
        ToolUtils.setToast(mContext,R.string.loading_fail_txt);
    }

    private void FetchData() {
        Map<String,String> mParams=new Hashtable<String, String>();
        try
        {
            mParams.put("userId", userId + "");
            mParams.put("skuAndNumLists", new JSONArray(mDatas).toString());
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.GetOrderCouponUrl, mParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if(mDialog!=null)
                {
                    mDialog.dismiss();
                }
                if (jsonObject!=null)
                ToolUtils.setLog(jsonObject.toString());
                int status = jsonObject.optInt("status");
                if (status != 200)
                {
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
                JSONObject object = jsonObject.optJSONObject("data");
                if (object == null)
                {
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                    return;
                }
                JSONArray datasObject = object.optJSONArray("data");
                if (datasObject != null && datasObject.length() > 0)
                {
                    for (int i = 0; i < datasObject.length(); i++)
                    {
                        JSONObject couponObject = datasObject.optJSONObject(i);
                        parseJson(couponObject,items,false);
                    }

                } else
                {
                    couponNullView.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.GONE);
                }
                JSONArray dataNoObject = object.optJSONArray("canNotUsedata");
                if (dataNoObject != null && dataNoObject.length() > 0)
                {
                    for (int i = 0; i < dataNoObject.length(); i++)
                    {
                        JSONObject couponObject = dataNoObject.optJSONObject(i);
                        parseJson(couponObject,itemsNo,true);
                    }
                }
                handler.sendEmptyMessage(UPDATE_COUPON_LIST);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                loadingFail();
            }
        });
        mRequestQueue.add(request);
    }


    private void parseJson(JSONObject jsonObject,List<Coupon> coupons,boolean isNoUser)
    {

        int id = jsonObject.optInt("id");
        int couponId = jsonObject.optInt("couponId");
        int couponRuleId = jsonObject.optInt("couponRuleId");
        String couponCode = jsonObject.optString("couponCode");
        double enoughValue = jsonObject.optDouble("enoughValue");
        double money = jsonObject.optDouble("bookValue");
        String info = jsonObject.optString("couponName");
        String startTime = jsonObject.optString("startTime");
        String endTime = jsonObject.optString("endTime");
        int days=0;
        String timeStr=null;
        try
        {
            timeStr= DateUtils.getCouponDateDiff(endTime);
            days=DateUtils.getDateDays(endTime);
            ToolUtils.setLog(days+"");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            endTime=dateFormat.format(DateUtils.formatDate(endTime));
            startTime=dateFormat.format(DateUtils.formatDate(startTime));
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        String statu = jsonObject.optString("status");
        String property = jsonObject.optString("property");
        String goodsType = jsonObject.optString("goodsType");
        Coupon coupon = new Coupon();
        coupon.id = id;
        coupon.couponId = couponId;
        coupon.couponRuleId = couponRuleId;
        coupon.couponCode = couponCode;
        coupon.enoughMoney = enoughValue;
        coupon.money = money;
        coupon.info="满"+ToolUtils.isIntPrice(enoughValue+"")+"减"+ToolUtils.isIntPrice(money+"");
        coupon.startDate = startTime;
        coupon.endDate = endTime;
        coupon.time = days;
        coupon.timeStr = timeStr;
        coupon.status = statu;
        coupon.property = property;
        coupon.type = goodsType;
        coupon.isNoUse = isNoUser;
        coupons.add(coupon);
    }

    /**
     * 提交兑换优惠券接口
     */
    private void FetchRedeem()
    {
        Map<String,String> mParams=new Hashtable<String, String>();
        try
        {
            mParams.put("userId", userId + "");
            mParams.put("skuAndNumLists", new JSONArray(mDatas).toString());
            mParams.put("nickName", userName);
            mParams.put("couponCode", couponCode);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.GetRedeemAndCheckCouponUrl, mParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if(mDialog!=null)
                {
                    mDialog.dismiss();
                }
                if (jsonObject!=null)
                ToolUtils.setLog(jsonObject.toString());
                int status = jsonObject.optInt("status");
                if (status != 200)
                {
                    loadingFail();
                }
                JSONObject datasObject = jsonObject.optJSONObject("data");
                if (datasObject != null && datasObject.length() > 0)
                {
                    int code=datasObject.optInt("code");
                    String message=datasObject.optString("msg");
                    JSONObject couponObject=datasObject.optJSONObject("data");
                    if (code==0)
                    {
                        if (couponObject != null && couponObject.length() > 0)
                        {
                            JSONObject couponUseInfoPO=couponObject.optJSONObject("couponUseInfoPO");

                            int id=couponUseInfoPO.optInt("id");
                            int couponId=couponUseInfoPO.optInt("couponId");
                            int couponRuleId=couponUseInfoPO.optInt("couponRuleId");
                            String couponCode=couponUseInfoPO.optString("couponCode");
                            double enoughValue=couponUseInfoPO.optDouble("enoughValue");
                            double money=couponUseInfoPO.optDouble("bookValue");
                            String info=couponUseInfoPO.optString("couponName");
                            String startTime=couponUseInfoPO.optString("startTime");
                            String endTime=couponUseInfoPO.optString("endTime");
                            int days=0;
                            String timeStr=null;
                            try
                            {
                                timeStr= DateUtils.getCouponDateDiff(endTime);
                                days=DateUtils.getDateDays(endTime);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                                endTime=dateFormat.format(DateUtils.formatDate(endTime));
                                startTime=dateFormat.format(DateUtils.formatDate(startTime));
                            } catch (ParseException e)
                            {
                                e.printStackTrace();
                            }
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                            endTime=dateFormat.format(endTime);
                            startTime=dateFormat.format(startTime);
                            String statu=couponUseInfoPO.optString("status");
                            String property=couponUseInfoPO.optString("property");
                            String goodsType=couponUseInfoPO.optString("goodsType");
                            Coupon coup=new Coupon();
                            coup.id=id;
                            coup.couponId=couponId;
                            coup.couponRuleId=couponRuleId;
                            coup.couponCode=couponCode;
                            coup.enoughMoney=enoughValue;
                            coup.money=money;
                            coup.info="满"+ToolUtils.isIntPrice(enoughValue+"")+"减"+ToolUtils.isIntPrice(money+"");
                            coup.startDate=startTime;
                            coup.endDate=endTime;
                            coup.time=days;
                            coup.timeStr = timeStr;
                            coup.status=statu;
                            coup.property=property;
                            coup.type=goodsType;
                            coup.isDefault=false;
                            items.add(coup);
                            handler.sendEmptyMessage(UPDATE_REDEEM_COUPON_RESULT);
                        } else
                        {
                            loadingFail();
                        }
                    }
                    else
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        ToolUtils.setToastLong(mContext,message);
                    }
                }
                else
                {
                    loadingFail();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                loadingFail();
            }
        });
        mRequestQueue.add(request);

    }

    public class CouponAdapter extends BaseListAdapter<Coupon>
    {
        public CouponAdapter(Context context, List<Coupon> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_shop_coupon_item, null);
            TextView info = ViewHolder.get(convertView, R.id.info);
            TextView date = ViewHolder.get(convertView, R.id.date);
            TextView moneySymbol = ViewHolder.get(convertView, R.id.moneySymbol);
            TextView money = ViewHolder.get(convertView, R.id.money);
            TextView time = ViewHolder.get(convertView, R.id.time);
            ImageView mDefalueIcon = ViewHolder.get(convertView, R.id.iv_coupon_defalue);
            ImageView view = ViewHolder.get(convertView, R.id.lineBg);
            if (position == (getCount()-1))
            {
                view.setVisibility(View.GONE);
            } else
            {
                view.setVisibility(View.VISIBLE);
            }

            Coupon coupon = getList().get(position);
            info.setText(coupon.info);
            money.setText(ToolUtils.isIntPrice(coupon.money + ""));
            date.setText(coupon.endDate+"到期");
            time.setText("(" + coupon.timeStr + ")");
            if (coupon.time > 3|coupon.time==0)
            {
                time.setVisibility(View.GONE);
                date.setText(coupon.startDate+"-"+coupon.endDate);
            } else
            {
                time.setVisibility(View.VISIBLE);
            }
            money.setTextColor(coupon.isNoUse ? getResources().getColor(R.color.text_gary_color) : getResources().getColor(R.color.green_color));
            moneySymbol.setTextColor(coupon.isNoUse?getResources().getColor(R.color.text_gary_color):getResources().getColor(R.color.green_color));
            mDefalueIcon.setVisibility(coupon.isNoUse?View.GONE:View.VISIBLE);
            mDefalueIcon.setImageResource(mCheckedPosition == position ? R.drawable.icon_address_checked : R.drawable.icon_address_normal);
            if (mCheckedPosition>=0)
            {
                noCouponIv.setImageResource(R.drawable.icon_address_normal);
            }
            return convertView;
        }
    }



    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    public void setOnCouponListener(OnCouponListener onCouponListener)
    {
        this.onCouponListener = onCouponListener;
    }

    public interface OnCouponListener
    {
        public void onDefaultCouponChange(Coupon coupon);

    }

    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("可用优惠卷");
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("可用优惠卷");
    }
}
