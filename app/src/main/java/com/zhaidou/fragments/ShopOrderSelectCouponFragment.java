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
import com.zhaidou.utils.NetworkUtils;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    private String token;
    private OnCouponListener onCouponListener;

    private Dialog mDialog;
    private RequestQueue mRequestQueue;
    private ListView mListview;
    private LinearLayout couponNullView;

    private LinearLayout loadingView, nullNetView, nullView;
    private TextView reloadBtn, reloadNetBtn;
    private CouponAdapter couponAdapter;

    private final int UPDATE_ADDRESS_LIST = 0;
    private final int UPDATE_RESULT = 1;
    private int mCheckedPosition = -1;

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
                    else
                    {
                        noCouponIv.setImageResource(R.drawable.icon_address_checked);
                    }
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

        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");
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
     * 提交订单接口
     */
    private void commit()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String result = FetchRequset();
                handler.obtainMessage(UPDATE_RESULT,result).sendToTarget();
            }
        }).start();

    }

    /**
     * 请求订单
     *
     * @return
     */
    private String FetchRequset()
    {
        String result = null;
        BufferedReader in = null;
        try
        {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();
            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.GetOrderCouponUrl);
            request.addHeader("SECAuthorization", token);
            request.addHeader("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
            // 创建名/值组列表
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userId", 1 + ""));
            params.add(new BasicNameValuePair("skuAndNumLists", new JSONArray(mDatas).toString()));
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
            while ((line = in.readLine()) != null)
            {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            return result;

        } catch (Exception e)
        {

        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result;
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
                    int days = 0;
                    try
                    {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date endDate = format.parse(endTime);
                        long diff = endDate.getTime() - System.currentTimeMillis();
                        days = (int) (diff / (1000 * 60 * 60 * 24)) + diff % (1000 * 60 * 60 * 24) > 0 ? 1 : 0;
                    } catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    endTime=endTime.split(" ")[0];

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
                    coupon.info = info;
                    coupon.startDate = startTime;
                    coupon.endDate = endTime;
                    coupon.time = days;
                    coupon.status = statu;
                    coupon.property = property;
                    coupon.type = goodsType;
                    coupon.isDefault = false;

                    items.add(coupon);
                }
                handler.sendEmptyMessage(UPDATE_ADDRESS_LIST);
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


    private void FetchData()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("userId", 1);
            json.put("skuAndNumLists", new JSONArray(mDatas));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        ToolUtils.setLog("json：" + json.toString());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.GetOrderCouponUrl, json.toString(), new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                mDialog.dismiss();
                ToolUtils.setLog("测试：" + jsonObject.toString());
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
                        int days = 0;
                        try
                        {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date endDate = format.parse(endTime);
                            long diff = endDate.getTime() - System.currentTimeMillis();
                            days = (int) (diff / (1000 * 60 * 60 * 24)) + diff % (1000 * 60 * 60 * 24) > 0 ? 1 : 0;
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

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
                        coupon.info = info;
                        coupon.startDate = startTime;
                        coupon.endDate = endTime;
                        coupon.time = days;
                        coupon.status = statu;
                        coupon.property = property;
                        coupon.type = goodsType;
                        coupon.isDefault = false;

                        items.add(coupon);
                    }
                    handler.sendEmptyMessage(UPDATE_ADDRESS_LIST);
                } else
                {
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                nullNetView.setVisibility(View.GONE);
                nullView.setVisibility(View.VISIBLE);
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
