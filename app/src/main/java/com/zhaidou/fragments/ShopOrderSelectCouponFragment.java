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
import com.zhaidou.model.Coupon;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShopOrderSelectCouponFragment extends BaseFragment implements View.OnClickListener
{

    private String index;
    private int mStatus;
    private View rootView;
    private LinearLayout exchangeBtn,noCouponBtn;
    private ImageView noCouponIv;
    private int userId;
    private String token;
    private OnCouponListener onCouponListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private ListView mListview;
    private LinearLayout loadingView;
    private CouponAdapter couponAdapter;

    private final int UPDATE_ADDRESS_LIST = 0;
    private int mCheckedPosition = 0;

    private List<Coupon> items = new ArrayList<Coupon>();
    private Coupon mCoupon;
    private String mDatas;



    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_ADDRESS_LIST:
                    if (mDialog != null)
                        mDialog.dismiss();
                    if (mStatus==0)
                    {
                        noCouponIv.setSelected(true);
                    }else
                    {
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
                    }
                    loadingView.setVisibility(View.GONE);
                    couponAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_no_coupon:
                    mCheckedPosition = -1;
                    mStatus=1;
                    couponAdapter.notifyDataSetChanged();
                    ((MainActivity) getActivity()).popToStack(ShopOrderSelectCouponFragment.this);
                    onCouponListener.onDefaultCouponChange(null);
                    break;
                case R.id.bt_add_coupon:
                break;

            }
        }
    };

    public static ShopOrderSelectCouponFragment newInstance(String index, int mpage, Coupon coupon,String dataJsonObject)
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
            mDatas=getArguments().getString("dataJsonObject");
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

        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
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
                ((MainActivity) getActivity()).popToStack(ShopOrderSelectCouponFragment.this);
                onCouponListener.onDefaultCouponChange(items.get(position));
            }
        });

        exchangeBtn=(LinearLayout)rootView.findViewById(R.id.bt_add_coupon);
        exchangeBtn.setOnClickListener(onClickListener);

        noCouponBtn=(LinearLayout)rootView.findViewById(R.id.btn_no_coupon);
        noCouponBtn.setOnClickListener(onClickListener);

        mRequestQueue = Volley.newRequestQueue(getActivity());
        token = (String)SharedPreferencesUtil.getData(mContext,"token", "");
        userId = (Integer)SharedPreferencesUtil.getData(mContext,"userId", -1);
        FetchData();



    }

    public void setOnCouponListener(OnCouponListener onCouponListener)
    {
        this.onCouponListener = onCouponListener;
    }

    public interface OnCouponListener
    {
        public void onDefaultCouponChange(Coupon coupon);

    }

    private void FetchData()
    {
        JSONObject json=new JSONObject();
        try
        {
            json.put("userId",userId);
            json.put("skuAndNumLists",mDatas);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,ZhaiDou.GetOrderCouponUrl,json, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                mDialog.dismiss();
                ToolUtils.setLog(jsonObject.toString());
                int status=jsonObject.optInt("status");
                if (status!=200)
                {
                    ToolUtils.setToast(mContext,R.string.loading_fail_txt);
                }
                JSONObject object=jsonObject.optJSONObject("data");
                JSONArray datasObject = object.optJSONArray("data");
                if (datasObject != null && datasObject.length() > 0)
                {
                    for (int i = 0; i <datasObject.length() ; i++)
                    {
                        JSONObject couponObject=datasObject.optJSONObject(i);
                        int id=couponObject.optInt("id");
                        int couponId=couponObject.optInt("couponId");
                        int couponRuleId=couponObject.optInt("couponRuleId");
                        String couponCode=couponObject.optString("couponCode");
                        double enoughValue=couponObject.optDouble("enoughValue");
                        double money=couponObject.optDouble("bookValue");
                        String info=couponObject.optString("couponName");
                        String startTime=couponObject.optString("startTime");
                        String endTime=couponObject.optString("endTime");
                        int days=0;
                        try
                        {
                            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date endDate=format.parse(endTime);
                            long diff=endDate.getTime()-System.currentTimeMillis();
                            days=(int) (diff / (1000 * 60 * 60 * 24))+diff %(1000 * 60 * 60 * 24)>0?1:0;
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                        String statu=couponObject.optString("status");
                        String property=couponObject.optString("property");
                        String goodsType=couponObject.optString("goodsType");

                        Coupon coupon=new Coupon();
                        coupon.id=id;
                        coupon.couponId=couponId;
                        coupon.couponRuleId=couponRuleId;
                        coupon.couponCode=couponCode;
                        coupon.enoughMoney=enoughValue;
                        coupon.money=money;
                        coupon.info=info;
                        coupon.startDate=startTime;
                        coupon.endDate=endTime;
                        coupon.time=days;
                        coupon.status=statu;
                        coupon.property=property;
                        coupon.type=goodsType;
                        coupon.isDefault=false;

                        items.add(coupon);
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
                ToolUtils.setToast(mContext,R.string.loading_fail_txt);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
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
            TextView money = ViewHolder.get(convertView, R.id.money);
            TextView time = ViewHolder.get(convertView, R.id.time);
            ImageView mDefalueIcon = ViewHolder.get(convertView, R.id.iv_coupon_defalue);
            View view = ViewHolder.get(convertView, R.id.lineBg);
            if (position == 0)
            {
                view.setVisibility(View.GONE);
            } else
            {
                view.setVisibility(View.VISIBLE);
            }

            Coupon coupon = getList().get(position);
            info.setText(coupon.info);
            date.setText(coupon.endDate);
            money.setText(ToolUtils.isIntPrice(coupon.money+""));
            time.setText("("+coupon.time+")");
            if (coupon.time>3)
            {
                time.setVisibility(View.GONE);
            }
            else
            {
                time.setVisibility(View.VISIBLE);
            }
            mDefalueIcon.setImageResource(mCheckedPosition == position ? R.drawable.icon_address_checked : R.drawable.icon_address_normal);
            return convertView;
        }
    }

    @Override
    public void onDestroyView()
    {
//        if (items != null && items.size() > 0)
//        {
//            if (items.size() > mCheckedPosition)
//            {
//                if (onCouponListener != null)
//                    onCouponListener.onDefaultCouponChange(items.get(mCheckedPosition));
//            }
//        }
        super.onDestroyView();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("可用优惠卷");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("可用优惠卷");
    }
}
