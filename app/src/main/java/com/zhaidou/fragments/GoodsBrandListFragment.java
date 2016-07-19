package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshGridView;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Product;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
 * 品牌商品
 */
public class GoodsBrandListFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView>
{
    private static final String TITLE = "title";
    private static final String ID = "id";

    private String mTitle;
    private String mId;
    private View mView;
    private Context mContext;

    private String token;
    private int userid;

    private int currentpage = 1;
    private int pageTotal;
    private int pageSize;
    private TextView titleTv;
    private ImageView shareBtn;
    private PullToRefreshGridView gv_single;

    private LinearLayout nullLine;

    private List<Product> products = new ArrayList<Product>();
    private RequestQueue mRequestQueue;
    private ProductAdapter productAdapter;
    private Dialog mDialog;
    private DialogUtils mDialogUtil;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    productAdapter.notifyDataSetChanged();
                    if (products.size() > 0)
                    {
                        nullLine.setVisibility(View.GONE);
                        if (products.size() < pageTotal)
                        {
                            gv_single.setMode(PullToRefreshBase.Mode.BOTH);
                        } else
                        {
                            gv_single.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        }
                    }
                    break;
            }

        }
    };

    public static GoodsBrandListFragment newInstance(String title, String id)
    {
        GoodsBrandListFragment fragment = new GoodsBrandListFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsBrandListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mTitle = getArguments().getString(TITLE);
            mId = getArguments().getString(ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContext = getActivity();
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.fragment_goods_brand, container, false);
            initView(mView);
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }
        return mView;
    }

    private void initView(View view)
    {
        setStartLoading();
        mDialogUtil=new DialogUtils(mContext);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(mTitle);

        shareBtn = (ImageView) mView.findViewById(R.id.share_iv);
        shareBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                share();
            }
        });

        gv_single = (PullToRefreshGridView) view.findViewById(R.id.gv_single);
        gv_single.setOnRefreshListener(this);
        gv_single.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        nullLine = (LinearLayout) view.findViewById(R.id.nullline);
        productAdapter = new ProductAdapter(mContext, products);
        gv_single.setAdapter(productAdapter);
        productAdapter.setOnInViewClickListener(R.id.ll_single_layout, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
                Product product = (Product) values;
                GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(product.getTitle(), product.goodsId);
                ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
            }
        });
        mRequestQueue = ZDApplication.newRequestQueue();
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            FetchBrandIdData(mId, currentpage = 1);
        }
        else
        {
           ShowToast(R.string.net_fail_txt);
        }

    }
    /**
     * 开始加载进度
     */
    private void setStartLoading()
    {
        if (mDialog==null)
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        mDialog.show();
    }

    /**
     * 结束加载进度
     */
    private void setEndLoading()
    {
        if (mDialog != null)
        {
            mDialog.dismiss();
        }
    }


    /**
     * 分享
     */
    private void share()
    {
        String shareUrl=ZhaiDou.HOME_BASE_WAP_URL+"brand/"+mId+"?title="+mTitle;
        mDialogUtil = new DialogUtils(mContext);
        mDialogUtil.showShareDialog(mTitle, mTitle + "  " + shareUrl, null, shareUrl, new PlatformActionListener()
        {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap)
            {
                ShowToast(R.string.share_completed);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable)
            {
                ShowToast(R.string.share_error);
            }

            @Override
            public void onCancel(Platform platform, int i)
            {
                ShowToast(R.string.share_cancel);
            }
        });
    }



    /**
     * 品牌请求数据
     * @param brandId
     * @param page
     */
    public void FetchBrandIdData(String brandId, int page)
    {
        currentpage = page;
        if (page == 1)
            products.clear();
        String url = null;
        JSONObject json = new JSONObject();
        JSONArray jsonValue = new JSONArray();
        try
        {
            jsonValue.put(brandId);
            json.put("brandId", jsonValue);

            url = ZhaiDou.SearchGoodsBrandIdUrl + json + "&pageNo=" + currentpage;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        ToolUtils.setLog(url);
        ZhaiDouRequest newMissRequest = new ZhaiDouRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject json)
            {
                setEndLoading();
                gv_single.onRefreshComplete();
                if (json != null)
                {
                    JSONObject dataObject = json.optJSONObject("data");

                    if (dataObject == null)
                    {
                        if (currentpage == 1)
                        {
                            if (products.size() == 0)
                            {
                                nullLine.setVisibility(View.VISIBLE);
                            }
                        }
                        return;
                    }
                    JSONObject pageObject = dataObject.optJSONObject("pagePO");
                    if (pageObject == null)
                    {
                        if (currentpage == 1)
                        {
                            if (products.size() == 0)
                            {
                                nullLine.setVisibility(View.VISIBLE);
                            }
                        }
                        return;
                    }
                    pageTotal = pageObject.optInt("totalCount");
                    pageSize = pageObject.optInt("pageSize");
                    JSONArray jsonArray = pageObject.optJSONArray("items");
                    if (jsonArray == null || jsonArray.toString().length() < 5)
                    {
                        if (currentpage == 1)
                        {
                            if (products.size() == 0)
                            {
                                nullLine.setVisibility(View.VISIBLE);
                            }
                        }
                        return;
                    }
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject merchandise = jsonArray.optJSONObject(i);
                        int id = merchandise.optInt("id");
                        String productId = merchandise.optString("productId");
                        String title = merchandise.optString("productName");
                        double price = merchandise.optDouble("price");
                        String cost_price = merchandise.optString("marketingPrice") == "null" ? "0" : merchandise.optString("marketingPrice");
                        String imgUrl = merchandise.optString("productPicUrl");
                        JSONObject countObject = merchandise.optJSONObject("expandedResponse");
                        int remaining = countObject.optInt("stock");
                        Product product = new Product();
                        product.goodsId = productId;
                        product.setId(id);
                        product.setPrice(price);
                        product.setCost_price(Double.parseDouble(cost_price));
                        product.setTitle(title);
                        product.setImage(imgUrl);
                        product.setRemaining(remaining);
                        products.add(product);
                    }
                    handler.sendEmptyMessage(0);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                setEndLoading();
                gv_single.onRefreshComplete();
                if (products.size() == 0)
                {
                    nullLine.setVisibility(View.VISIBLE);
                }
                if (currentpage > 1)
                {
                    currentpage = currentpage - 1;
                }
            }
        });
        mRequestQueue.add(newMissRequest);
    }


    private Map<Integer, View> mHashMap = new HashMap<Integer, View>();

    public class ProductAdapter extends BaseListAdapter<Product>
    {
        public ProductAdapter(Context context, List<Product> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_goods_sale, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            ImageView image = ViewHolder.get(convertView, R.id.iv_single_item);
            image.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth-30 / 2, (screenWidth-30)/ 2 - 1));
            TextView tv_money = ViewHolder.get(convertView, R.id.tv_money);
            TextView tv_price = ViewHolder.get(convertView, R.id.tv_price);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView ll_sale_out = ViewHolder.get(convertView, R.id.ll_sale_out);
            ll_sale_out.setLayoutParams(new RelativeLayout.LayoutParams((screenWidth-30)/ 2, (screenWidth-30)/ 2));
            TextView shopSaleTv = ViewHolder.get(convertView, R.id.shopSaleTv);
            Product product = getList().get(position);
            tv_name.setText(product.getTitle());
            ToolUtils.setImageNoResetUrl(product.getImage(), image, R.drawable.icon_loading_defalut);
            tv_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv_money.setText("￥" + ToolUtils.isIntPrice("" + product.getPrice()));
            tv_price.setText("￥" + ToolUtils.isIntPrice("" + product.getCost_price()));

            ll_sale_out.setVisibility(product.getRemaining() == 0 ? View.VISIBLE : View.GONE);

            if (product.getPrice()==0||product.getCost_price()==0)
            {
                shopSaleTv.setVisibility(View.GONE);
            }
            else
            {
                DecimalFormat df = new DecimalFormat("##.0");
                String zk = df.format(product.getPrice() / product.getCost_price() * 10);
                if (zk.contains(".0")) {
                    int sales = (int) Double.parseDouble(zk);
                    shopSaleTv.setText(sales + "折");
                } else {
                    Double sales = Double.parseDouble(zk);
                    shopSaleTv.setText(sales + "折");
                }
                shopSaleTv.setVisibility(View.VISIBLE);
            }
            mHashMap.put(position, convertView);
            return convertView;
        }
    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView)
    {
        products.clear();
        FetchBrandIdData(mId, currentpage = 1);
    }


    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView)
    {
        FetchBrandIdData(mId, ++currentpage);
    }
}
