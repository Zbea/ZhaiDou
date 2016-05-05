package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
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
import com.zhaidou.utils.NetService;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
    private LinearLayout exchangeBtn, noCouponBtn;
    private ImageView noCouponIv;
    private int userId;
    private String userName;
    private String token;
    private OnCouponListener onCouponListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private ListView mListview;
    private LinearLayout couponNullView;

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CouponAdapter couponAdapter;

    private final int UPDATE_COUPON_LIST = 0;
    private final int UPDATE_RESULT = 1;
    private final int UPDATE_REDEEM_COUPON_RESULT = 2;
    private final int UPDATE_PARSE_REDEEM_COUPON_RESULT = 3;

    private int mCheckedPosition = -1;

    private List<Coupon> items = new ArrayList<Coupon>();
    private Coupon mCoupon;
    private String mDatas;
    private String couponCode;
    private Coupon redeemCoupon;
    final Map<String,String> headers=new HashMap<String, String>();


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
//                    else
//                    {
//                        noCouponIv.setImageResource(R.drawable.icon_address_checked);
//                    }
                    loadingView.setVisibility(View.GONE);
                    couponNullView.setVisibility(View.GONE);
                    couponAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_RESULT:
                    if(mDialog!=null)
                    {
                        mDialog.dismiss();
                    }
                    if (msg.obj!=null)
                    {
                        JsonParse(msg.obj.toString());
                    }
                    break;
                case UPDATE_PARSE_REDEEM_COUPON_RESULT:
                    if(mDialog!=null)
                    {
                        mDialog.dismiss();
                    }
                    if (msg.obj!=null)
                    {
                        JsonRedeemParse(msg.obj.toString());
                    }

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
                    ((MainActivity) getActivity()).popToStack(ShopOrderSelectCouponFragment.this);
                    onCouponListener.onDefaultCouponChange(null);
                    break;
                case R.id.bt_add_coupon:
                    addCoupon();
                    break;
                case R.id.nullReload:
                    commit();
                    break;
                case R.id.netReload:
                    commit();
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
                ((MainActivity) getActivity()).popToStack(ShopOrderSelectCouponFragment.this);
                onCouponListener.onDefaultCouponChange(items.get(position));
            }
        });

        exchangeBtn = (LinearLayout) rootView.findViewById(R.id.bt_add_coupon);
        exchangeBtn.setOnClickListener(onClickListener);

        noCouponBtn = (LinearLayout) rootView.findViewById(R.id.btn_no_coupon);
        noCouponBtn.setOnClickListener(onClickListener);
        noCouponIv= (ImageView) rootView.findViewById(R.id.iv_addr_defalue);

        mRequestQueue = Volley.newRequestQueue(mContext);
        token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        userName = (String) SharedPreferencesUtil.getData(mContext, "nickName", "");
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");

        headers.put("SECAuthorization", token);
        headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));

        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            commit();
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
        final Dialog dialog = new Dialog(mContext, R.style.custom_dialog);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_redeem_coupon, null);
        final TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeInput(view);
                dialog.dismiss();
            }
        });

        TextView okTv = (TextView) view.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                couponCode=tv_msg.getText().toString();
                if (TextUtils.isEmpty(couponCode))
                {
                    ToolUtils.setToast(mContext,"抱歉，请先填写兑换码");
                    return;
                }
                dialog.dismiss();
                if (mDialog!=null)
                    mDialog.show();
                closeInput(view);
                FetchRedeem();

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();

    }

    private void closeInput(View dialog)
    {
        InputMethodManager inputMethodManagers=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManagers.isActive())
            inputMethodManagers.hideSoftInputFromWindow(dialog.getWindowToken(),0);
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

    /**
     * 获取优惠券列表
     */
    private void commit()
    {
        // 创建名/值组列表
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        try
        {
            params.add(new BasicNameValuePair("userId", userId + ""));
            params.add(new BasicNameValuePair("skuAndNumLists", new JSONArray(mDatas).toString()));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String result= NetService.GETHttpPostService(mContext, ZhaiDou.GetOrderCouponUrl, headers, params);
                handler.obtainMessage(UPDATE_RESULT,result).sendToTarget();
            }
        }).start();

    }

    /**
     * 提交兑换优惠券接口
     */
    private void FetchRedeem()
    {
        // 创建名/值组列表
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        try
        {
            params.add(new BasicNameValuePair("userId", userId + ""));
            params.add(new BasicNameValuePair("skuAndNumLists", new JSONArray(mDatas).toString()));
            params.add(new BasicNameValuePair("nickName", userName));
            params.add(new BasicNameValuePair("couponCode", couponCode));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String result= NetService.GETHttpPostService(mContext, ZhaiDou.GetRedeemAndCheckCouponUrl, headers, params);
                handler.obtainMessage(UPDATE_PARSE_REDEEM_COUPON_RESULT,result).sendToTarget();
            }
        }).start();

    }

    /**
     * json解析
     * @param json
     */
    private void JsonParse(String json)
    {
        ToolUtils.setLog(json);
        if (json != null && json.length() > 0)
        {
            JSONObject jsonObject = null;
            try
            {
                jsonObject = new JSONObject(json);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
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
                    int id = couponObject.optInt("id");
                    int couponId = couponObject.optInt("couponId");
                    int couponRuleId = couponObject.optInt("couponRuleId");
                    String couponCode = couponObject.optString("couponCode");
                    double enoughValue = couponObject.optDouble("enoughValue");
                    double money = couponObject.optDouble("bookValue");
                    String info = couponObject.optString("couponName");
                    String startTime = couponObject.optString("startTime");
                    String endTime = couponObject.optString("endTime");
                    int days=0;
                    try
                    {
                        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date endDate=format.parse(endTime);
                        long diff=endDate.getTime()-System.currentTimeMillis();
                        ToolUtils.setLog("diff："+diff);
                        days=(int) (diff / (1000 * 60 * 60 * 24)+((diff %(1000 * 60 * 60 * 24))>0?1:0));
                    } catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    endTime=endTime.split(" ")[0];
                    ToolUtils.setLog("days："+days);
                    String statu = couponObject.optString("status");
                    String property = couponObject.optString("property");
                    String goodsType = couponObject.optString("goodsType");
                    Coupon coupon = new Coupon();
                    coupon.id = id;
                    coupon.couponId = couponId;
                    coupon.couponRuleId = couponRuleId;
                    coupon.couponCode = couponCode;
                    coupon.enoughMoney = enoughValue;
                    coupon.money = money;
                    coupon.info = "满"+enoughValue+"减"+money;
                    coupon.startDate = startTime;
                    coupon.endDate = endTime;
                    coupon.time = days;
                    coupon.status = statu;
                    coupon.property = property;
                    coupon.type = goodsType;
                    coupon.isDefault = false;

                    items.add(coupon);
                }
                handler.sendEmptyMessage(UPDATE_COUPON_LIST);
            } else
            {
                couponNullView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
            }
        } else
        {
            nullNetView.setVisibility(View.GONE);
            nullView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 兑换优惠券接口
     * @param json
     */
    private void JsonRedeemParse(String json)
    {
        ToolUtils.setLog("json：" + json);
        if (json==null)
        {
            ToolUtils.setToast(mContext,R.string.loading_fail_txt);
            return;
        }
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject(json);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
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
                    try
                    {
                        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date endDate=format.parse(endTime);
                        long diff=endDate.getTime()-System.currentTimeMillis();
                        days=(int) (diff / (1000 * 60 * 60 * 24)+(diff %(1000 * 60 * 60 * 24)>0?1:0));
                    } catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    ToolUtils.setLog("days：" + days);
                    endTime=endTime.split(" ")[0];
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
            date.setText(coupon.endDate+"到期");
            money.setText(ToolUtils.isIntPrice(coupon.money + ""));
            time.setText("(仅剩" + coupon.time + "天)");
            if (coupon.time > 3)
            {
                time.setVisibility(View.GONE);
            } else
            {
                time.setVisibility(View.VISIBLE);
            }
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
