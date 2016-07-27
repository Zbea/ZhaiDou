package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Coupons;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DateUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
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

public class CouponsFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView> {
    private static final String ARG_PARAM1 = "status";
    private static final String ARG_PARAM2 = "param2";

    private String mStatus;
    private String mTag;

    private PullToRefreshListView mListView;
    private List<Coupons> mCouponsList;
    private CouponAdapter mCouponAdapter;

    private DialogUtils mDialogUtils;
    private int currentPage;
    private Dialog dialog;
    private long mServerTime;
    private View mRootView;
    private View mEmptyView;
    private TextView mEmptyText;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    public static CouponsFragment newInstance(String param1, String tag) {
        CouponsFragment fragment = new CouponsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, tag);
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
            mTag = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (null != mRootView) {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (null != parent) {
                parent.removeView(mRootView);
            }
        } else {
            mRootView = inflater.inflate(R.layout.fragment_coupons, null);
            mEmptyView = mRootView.findViewById(R.id.ll_empty);
            mEmptyText = (TextView) mRootView.findViewById(R.id.emptyText);
            mListView = (PullToRefreshListView) mRootView.findViewById(R.id.listView);
            mListView.setMode(PullToRefreshBase.Mode.BOTH);
            mListView.setOnRefreshListener(this);
            mCouponsList = new ArrayList<Coupons>();
            mCouponAdapter = new CouponAdapter(getActivity(), mCouponsList);
            mListView.setAdapter(mCouponAdapter);
            mDialogUtils = new DialogUtils(getActivity());
            FetchData(currentPage = 1);
            mCouponAdapter.setOnInViewClickListener(R.id.categoryLayout, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    if(mCouponsList.get(position).goodsType.equalsIgnoreCase("C"))
                    {
                        ImageView mArrowView = (ImageView) v.findViewById(R.id.arrow);
                        mArrowView.setSelected(!mArrowView.isSelected());
                        mCouponAdapter.notifyDataSetChanged();
                    }
                }
            });
            mCouponAdapter.setOnInViewClickListener(R.id.ll_coupons, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    String categoryStr=null;
                    String categoryId=null;
                    Coupons coupons=mCouponsList.get(position);
                    if(coupons.goodsType.equalsIgnoreCase("T"))
                    {
                        if (coupons.couponGoodsTypeNames.size()>0&coupons.couponGoodsTypeNamesCategeryID.size()>0)
                        {
                            categoryStr = coupons.couponGoodsTypeNames.get(0);
                            categoryId=coupons.couponGoodsTypeNamesCategeryID.get(0);
                            GoodsBrandListFragment goodsBrandListFragment = GoodsBrandListFragment.newInstance(categoryStr, categoryId);
                            ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsBrandListFragment);
                        }
                    }
                }
            });

            dialog = mDialogUtils.showLoadingDialog();
        }
        return mRootView;
    }


    private void FetchData(final int page) {
        System.out.println("page = " + page);
        String mUserId = SharedPreferencesUtil.getData(getActivity(), "userId", -1) + "";
        Map<String, String> mParams = new HashMap<String, String>();
        mParams.put("user_id", mUserId);
        mParams.put("page", "" + page);
        mParams.put("pageSize", "15");
        mParams.put("status", mStatus);

        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.COUPONS_MINE_URL, mParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                ToolUtils.setLog(jsonObject.toString());
                int status = jsonObject.optInt("status");
                mServerTime = jsonObject.optLong("timestamp");
                if (status == 200) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    JSONArray couponUseInfoDTOs = data.optJSONArray("couponUseInfoDTOs");
                    int pageSize = data.optInt("pageSize");
                    if (couponUseInfoDTOs != null && couponUseInfoDTOs.length() > 0) {
                        List<Coupons> couponses = JSON.parseArray(couponUseInfoDTOs.toString(), Coupons.class);
                        System.out.println("CouponsFragment.onResponse---->" + couponses.size() + "----" + pageSize);
                        if (page==1) mCouponAdapter.clear();
                        mCouponAdapter.addAll(couponses);
                        mListView.onRefreshComplete();
                        if (couponses.size() < pageSize) {
                            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            if (mCouponAdapter.getCount() > pageSize)
                                Toast.makeText(mContext, "加载完毕", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                mEmptyView.setVisibility(mCouponAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
                mEmptyText.setText(String.format("还没有%s的优惠券哦~", mTag));
                mDialogUtils.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialogUtils.dismiss();
                mListView.onRefreshComplete();
            }
        });
        ZDApplication.newRequestQueue().add(request);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        System.out.println("CouponsFragment.onPullDownToRefresh");
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
            TextView tv_type= ViewHolder.get(convertView, R.id.tv_typeName);
            TextView mTitle = ViewHolder.get(convertView, R.id.title);
            TextView mMoney = ViewHolder.get(convertView, R.id.money);
            TextView mDetail = ViewHolder.get(convertView, R.id.detail);
            ImageView mImageView = ViewHolder.get(convertView, R.id.arrow);
            ClickableTextView mCategory = ViewHolder.get(convertView, R.id.category);
            ImageView mTipView = ViewHolder.get(convertView, R.id.mTipView);
            Coupons coupons = getList().get(position);
            // A全场通用，C品类专用，T品牌专用
            tv_type.setText(coupons.goodsType.equalsIgnoreCase("A")?"全场通用":coupons.goodsType.equalsIgnoreCase("C")?"品类专用":"品牌专用");
            mTitle.setText(coupons.couponName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            String startTimeStr = null,endTimeStr = null,timeStr=null;
            Date endTime = null;
            try {
                timeStr= DateUtils.getCouponDateDiff(coupons.endTime);
                endTime = simpleDateFormat.parse(coupons.endTime);
                endTimeStr = dateFormat.format(endTime);
                startTimeStr= dateFormat.format(simpleDateFormat.parse(coupons.startTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long diff=endTime.getTime() - mServerTime;
            int days=diff %(1000 * 60 * 60 * 24)>0?1:0;
            long day = diff / (1000 * 60 * 60 * 24)+days;
            endTimeStr=day>3|!"N".equalsIgnoreCase(mStatus)?startTimeStr+"-"+endTimeStr:endTimeStr+"到期";
            mMoney.setText(Html.fromHtml("<big><big>￥<big><big><big>" + coupons.bookValue + "</big></big></big></big></big>"));
            mDetail.setText(Html.fromHtml(String.format("%s<font color=red>%s</font><br><br>满%s使用", endTimeStr, day <= 3 ? "(" + timeStr + ")" : "", coupons.enoughValue)));
            String categoryStr = "";
            List<String> ids = new ArrayList<String>();
            List<String> couponGoodsTypeNamesCategeryID = coupons.couponGoodsTypeNamesCategeryID;
            for (int i = 0; i < coupons.couponGoodsTypeNames.size(); i++) {
                String category = coupons.couponGoodsTypeNames.get(i);
                categoryStr += (category + "、");
                if (i < couponGoodsTypeNamesCategeryID.size())
                    ids.add(coupons.couponGoodsTypeNamesCategeryID.get(i));
            }
            mCategory.setClickText(categoryStr.length() > 0 ? categoryStr.substring(0, categoryStr.length() - 1) : "", ids, this,position);
            mCategory.setVisibility((mImageView.isSelected() ? View.VISIBLE : View.GONE));
            mTipView.setVisibility(View.GONE);
            convertView.setBackgroundResource(R.drawable.coupons_bg);
            if ("U".equalsIgnoreCase(mStatus)) {
                mTipView.setVisibility(View.VISIBLE);
                mTipView.setImageResource(R.drawable.coupon_used);
                convertView.setBackgroundResource(R.drawable.coupon_bg_used);
            } else if ("O".equalsIgnoreCase(mStatus)) {
                convertView.setBackgroundResource(R.drawable.coupon_bg_used);
                mTipView.setVisibility(View.VISIBLE);
                mTipView.setImageResource(R.drawable.coupon_overtime);
            }
            if (!"N".equalsIgnoreCase(mStatus)) {
                mMoney.setTextColor(getResources().getColor(R.color.gray_9));
                mDetail.setTextColor(getResources().getColor(R.color.gray_9));
                mDetail.setText(Html.fromHtml(String.format("%s<br><br>满%s使用", endTimeStr, coupons.enoughValue)));
            }
            mImageView.setVisibility("C".equalsIgnoreCase(coupons.goodsType) ? View.VISIBLE : View.GONE);
            return convertView;
        }

        @Override
        public void onTextClick(String categoryStr, String id,int position) {
            System.out.println("categoryStr = [" + categoryStr + "], id = [" + id + "]");
            Coupons coupons=getList().get(position);
            if(coupons.goodsType.equalsIgnoreCase("C"))
            {
                SearchFragment searchFragment = SearchFragment.newInstance(categoryStr,id, 2);
                ((BaseActivity) getActivity()).navigationToFragmentWithAnim(searchFragment);
            }
        }
    }

    public PullToRefreshListView getListView() {
        return mListView;
    }

}
