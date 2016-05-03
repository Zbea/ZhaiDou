package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Coupons;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.ClickableTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouponsFragment extends Fragment implements PullToRefreshBase.OnRefreshListener2<ListView>{
    private static final String ARG_PARAM1 = "status";
    private static final String ARG_PARAM2 = "param2";

    private String mStatus;
    private String mParam2;

    private PullToRefreshListView mListView;
    private List<Coupons> mCouponsList;
    private CouponAdapter mCouponAdapter;

    private DialogUtils mDialogUtils;
    private int currentPage;
    private Dialog dialog;
    private long mServerTime;

    public static CouponsFragment newInstance(String param1, String param2) {
        CouponsFragment fragment = new CouponsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CouponsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatus = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_coupons,null);
        mListView = (PullToRefreshListView) mRootView.findViewById(R.id.listView);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(this);
        mCouponsList = new ArrayList<Coupons>();
        mCouponAdapter = new CouponAdapter(getActivity(), mCouponsList);
        mListView.setAdapter(mCouponAdapter);
        mDialogUtils=new DialogUtils(getActivity());
        FetchData(currentPage=1);
        mCouponAdapter.setOnInViewClickListener(R.id.categoryLayout, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                ImageView mArrowView = (ImageView) v.findViewById(R.id.arrow);
                mArrowView.setSelected(!mArrowView.isSelected());
                mCouponAdapter.notifyDataSetChanged();
            }
        });
        dialog = mDialogUtils.showLoadingDialog();
        return mRootView;
    }


    private void FetchData(int page) {
        String mUserId= SharedPreferencesUtil.getData(getActivity(),"userId",-1)+"";
        Map<String, String> mParams = new HashMap<String, String>();
        mParams.put("user_id", mUserId);
        mParams.put("pageNum", ""+page);
        mParams.put("pageSize", "20");
        mParams.put("status", mStatus);

        ZhaiDouRequest request = new ZhaiDouRequest(getActivity(), Request.Method.POST, ZhaiDou.COUPONS_MINE_URL, mParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                mServerTime=jsonObject.optLong("timestamp");
                if (status == 200) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    JSONArray couponUseInfoDTOs = data.optJSONArray("couponUseInfoDTOs");
                    int pageSize = data.optInt("pageSize");
                    if (couponUseInfoDTOs != null && couponUseInfoDTOs.length() > 0) {
                        List<Coupons> couponses = JSON.parseArray(couponUseInfoDTOs.toString(), Coupons.class);
                        mCouponAdapter.addAll(couponses);
                        if (couponses.size()<pageSize){
                            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            Toast.makeText(getActivity(),"加载完毕",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                mListView.onRefreshComplete();
                mDialogUtils.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialogUtils.dismiss();
            }
        });
        ((ZDApplication) getActivity().getApplicationContext()).mRequestQueue.add(request);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        mCouponAdapter.clear();
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        FetchData(currentPage = 1);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        FetchData(++currentPage);
    }

    public class CouponAdapter extends BaseListAdapter<Coupons> implements ClickableTextView.OnTextClickListener {

        public CouponAdapter(Context context, List<Coupons> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_coupons_adapter, null);
            TextView mTitle = ViewHolder.get(convertView, R.id.title);
            TextView mMoney = ViewHolder.get(convertView, R.id.money);
            TextView mDetail = ViewHolder.get(convertView, R.id.detail);
            ImageView mImageView = ViewHolder.get(convertView, R.id.arrow);
            ClickableTextView mCategory = ViewHolder.get(convertView, R.id.category);
            ImageView mTipView=ViewHolder.get(convertView,R.id.mTipView);
            Coupons coupons = getList().get(position);
            mTitle.setText(coupons.couponName);
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy.MM.dd");
            String endTimeStr = null;
            Date endTime = null;
            try {
                endTime = simpleDateFormat.parse(coupons.endTime);
                endTimeStr = dateFormat.format(endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String day=(endTime.getTime()-mServerTime)/(1000*60*60*24)+"";
            mMoney.setText(Html.fromHtml("<big><big>￥<big><big><big>" + coupons.bookValue + "</big></big></big></big></big>"));
            mDetail.setText(Html.fromHtml(String.format("%s到期<font color=red>(仅剩%s天)</font><br><br>满%s使用",endTimeStr,day,coupons.enoughValue)));
            String categoryStr = "";
            List<Integer> ids=new ArrayList<Integer>();
            for (String category : coupons.couponGoodsTypeNames) {
                categoryStr += (category + "、");
                ids.add(1);
            }
            mCategory.setClickText(categoryStr.length() > 0 ? categoryStr.substring(0, categoryStr.length() - 1) : "",ids,this);
            mCategory.setVisibility((mImageView.isSelected() ? View.VISIBLE : View.GONE));
            if (mServerTime<endTime.getTime()){
                mTipView.setVisibility("U".equalsIgnoreCase(mStatus)?View.VISIBLE:View.GONE);
                mTipView.setImageResource(R.drawable.coupon_used);
            }else {
                mTipView.setVisibility(View.VISIBLE);
                mTipView.setImageResource(R.drawable.coupon_overtime);
            }
            if (!"N".equalsIgnoreCase(mStatus)){
                mTitle.setTextColor(getResources().getColor(R.color.gray_9));
                mMoney.setTextColor(getResources().getColor(R.color.gray_9));
                mDetail.setTextColor(getResources().getColor(R.color.gray_9));
                mDetail.setText(Html.fromHtml(String.format("%s到期<br><br>满%s使用",endTimeStr,coupons.enoughValue)));
            }
            return convertView;
        }

        @Override
        public void onTextClick(String categoryStr, int id) {
            System.out.println("categoryStr = [" + categoryStr + "], id = [" + id + "]");
        }
    }

    public PullToRefreshListView getListView(){
        return mListView;
    }

}
