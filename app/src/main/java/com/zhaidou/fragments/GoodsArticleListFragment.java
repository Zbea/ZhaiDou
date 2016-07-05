package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ArticleGoodsAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 软装清单
 */
public class GoodsArticleListFragment extends BaseFragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CATEGORY = "category";

    private View view;
    private int mParam;
    private String mString;

    private TextView titleTv;
    private PullToRefreshListView listView;
    private TextView subtotalTv;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST = 3;
    private List<CartGoodsItem> articleList = new ArrayList<CartGoodsItem>();

    private ArticleGoodsAdapter mGoodsAdapter;
    private int page = 1;
    private int pageSize;
    private int pageCount;
    private String totalPrice;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (mDialog != null)
            {
                mDialog.dismiss();
            }
            subtotalTv.setText("￥"+ToolUtils.isIntPrice(totalPrice));
            mGoodsAdapter.notifyDataSetChanged();
            listView.onRefreshComplete();
            if (articleList.size()< pageCount)
            {
                listView.setMode(PullToRefreshBase.Mode.BOTH);
            } else
            {
                listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            }

        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            articleList.clear();
            page=1;
            FetchData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            ++page;
            FetchData();
        }
    };

    public static GoodsArticleListFragment newInstance(int param, String string)
    {
        GoodsArticleListFragment fragment = new GoodsArticleListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param);
        args.putString(ARG_CATEGORY, string);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsArticleListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam = getArguments().getInt(ARG_PARAM1);
            mString = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_home_article_list, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null)
        {
            parent.removeView(view);
        }
        return view;
    }

    private void initView()
    {
        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText("软装清单");

        subtotalTv=(TextView) view.findViewById(R.id.detailsSubtotalTv);

        listView = (PullToRefreshListView) view.findViewById(R.id.lv_special_list);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(onRefreshListener);
        mGoodsAdapter = new ArticleGoodsAdapter(getActivity(), articleList);
        listView.setAdapter(mGoodsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (articleList.get(position).storeId.equals("S"))
                {
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(articleList.get(position).name, articleList.get(position).goodsId);
                    Bundle bundle = new Bundle();
                    bundle.putString("index", articleList.get(position).goodsId);
                    bundle.putString("page", articleList.get(position).name);
                    bundle.putString("sizeId", articleList.get(position).sizeId);
                    goodsDetailsFragment.setArguments(bundle);
                    ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);

                } else
                {
                    Intent intent = new Intent();
                    intent.putExtra("url", articleList.get(position).userId);
                    intent.setClass(mContext, WebViewActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });
        initData();
    }

    private void initData()
    {
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void FetchData()
    {
        String url;
        if (mParam==1)
        {
            url = ZhaiDou.HomeArticleGoodsDetailsUrl + mString+"&pageNo="+page+"&pageSize=10";
        }
        else
        {
            url = ZhaiDou.HomeSofeListDetailUrl + mString + "&pageNo=" + page + "&pageSize=10";
        }
        ZhaiDouRequest request = new ZhaiDouRequest(mContext,url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        if (response == null)
                        {
                            ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null)
                        {
                            pageCount = jsonObject1.optInt("totalCount");
                            pageSize = jsonObject1.optInt("pageSize");
                            Double aDouble= jsonObject1.optDouble("totalPrice");
                            DecimalFormat df=new DecimalFormat("#.00");
                            totalPrice=df.format(aDouble);
                            JSONArray jsonArray = jsonObject1.optJSONArray(mParam==1?"changeCaseProductPOs":"designerListProductPOs");
                            if (jsonArray != null)
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    obj = jsonArray.optJSONObject(i);
                                    int baseid = obj.optInt("id");
                                    int caseId = obj.optInt("caseId");
                                    String type = obj.optString("goodsType");
                                    String title = obj.optString("goodsTitle");
                                    String productCode = obj.optString("productCode");
                                    String productSkuCode = obj.optString("productSkuCode");
                                    String productSku = obj.optString("goodsAttr");
                                    double price = Double.parseDouble(df.format(obj.optDouble("price")));
                                    String imageUrl = obj.optString("mainPic");
                                    String url =obj.optString("aUrl");
                                    int num = obj.optInt("quantity");
                                    CartGoodsItem cartGoodsItem = new CartGoodsItem();
                                    cartGoodsItem.id = baseid;
                                    cartGoodsItem.id = caseId;//商品id
                                    cartGoodsItem.goodsId = productCode;
                                    cartGoodsItem.num = num;
                                    cartGoodsItem.imageUrl = imageUrl;
                                    cartGoodsItem.size = productSku;
                                    cartGoodsItem.sizeId = productSkuCode;
                                    cartGoodsItem.currentPrice = price;
                                    cartGoodsItem.name = title;
                                    cartGoodsItem.storeId = type;//商品类型
                                    cartGoodsItem.userId = url;//跳转地址
                                    articleList.add(cartGoodsItem);
                                }
                            Message message = new Message();
                            message.what = UPDATE_HOMELIST;
                            handler.sendMessage(message);
                        } else
                        {
                            ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            return;
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                if (page > 1)
                {
                    page--;
                }
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
            }
        });
        ZDApplication.mRequestQueue.add(request);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("软装清单");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("软装清单");
    }
}
