package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
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
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsSingleListFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView>
{
    private static final String ARG_PARAM1 = "categoryId";
    private static final String ARG_FROM = "from";
    private static final String ID = "id";

    private String mParam1;
    private String mParam2;
    private int mFlag;//当为1是特卖商城搜索的商品，当未2是分类 ID商城
    private int id = -1;
    private View mView;
    private Context mContext;

    private String token;
    private int userid;
    private boolean isLogin = false;

    private int currentpage = 1;
    private int pageTotal;
    private int pageSize;
    private int count = -1;
    private int sort = 0;

    private ImageView iv_heart;
    private TextView tv_money, tv_count, tv_detail;
    private PullToRefreshGridView gv_single;

    private LinearLayout nullLine;

    private List<Product> products = new ArrayList<Product>();
    private RequestQueue mRequestQueue;
    private ProductAdapter productAdapter;
    private Dialog mDialog;

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

    public static GoodsSingleListFragment newInstance(String categoryId, String from, int flag)
    {
        GoodsSingleListFragment fragment = new GoodsSingleListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, categoryId);
        args.putString(ARG_FROM, from);
        args.putInt("flag", flag);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsSingleListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_FROM);
            mFlag = getArguments().getInt("flag");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContext = getActivity();
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.fragment_single, container, false);
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
        isLogin = checkLogin();
        tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_money = (TextView) view.findViewById(R.id.tv_money);
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
        if (mFlag == 1)
        {
            FetchSpecialData(mParam1, sort, currentpage = 1);
        } else
        {
            FetchSpecialIdData(mParam1, sort, currentpage = 1);
        }
//        if ("category".equalsIgnoreCase(mParam2))//全分类
//        {
//            FetchCategoryData(mParam1, sort, currentpage = 1);
//        }
//        else if ("goods".equalsIgnoreCase(mParam2))//搜索特卖商城
//        {
//            FetchSpecialData(mParam1, sort, currentpage=1);
//        }
//        else {
//            FetchData(mParam1, sort, currentpage=1);
//        }
    }

    public boolean checkLogin()
    {
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        userid = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);

        if (token != null)
        {
            isLogin = false;
            if (token.length() > 0 && userid > 0)
            {
                isLogin = true;
            }
        } else
        {
            isLogin = false;
        }
        return isLogin;
    }

    /**
     * 开始加载进度
     */
    private void setStartLoading()
    {
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
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

    public void setCurrentpage()
    {
        currentpage = 1;
    }

    /**
     * 特卖列表请求数据
     *
     * @param msg
     * @param sort
     * @param page
     */
    public void FetchSpecialData(String msg, int sort, int page)
    {
        mParam1 = msg;
        this.sort = sort;
        currentpage = page;
        mFlag=1;
        if (page == 1) products.clear();
        String url = null;

        try
        {
            if (sort == 1)//从低到高
            {
                url = ZhaiDou.SearchGoodsKeyWordUrl + URLEncoder.encode(msg, "UTF-8") + "&pageNo=" + currentpage + "&orderBy=price&order=asc";

            } else if (sort == 2)//从高到低
            {

                url = ZhaiDou.SearchGoodsKeyWordUrl + URLEncoder.encode(msg, "UTF-8") + "&pageNo=" + currentpage + "&orderBy=price&order=desc";

            } else
            {
                url = ZhaiDou.SearchGoodsKeyWordUrl + URLEncoder.encode(msg, "UTF-8") + "&pageNo=" + currentpage;
            }
        } catch (UnsupportedEncodingException e)
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

    /**
     * 特卖列表请求数据
     *
     * @param categoryId
     * @param sort
     * @param page
     */
    public void FetchSpecialIdData(String categoryId, int sort, int page)
    {
        mParam1 = categoryId;
        this.sort = sort;
        currentpage = page;
        mFlag=2;
        if (page == 1) products.clear();
        String url = null;
        JSONObject json = new JSONObject();
        try
        {
            json.put("categoryId", categoryId);
            json.put("brandId", null);
            json.put("storeId", null);
            json.put("regionId", null);

            if (sort == 1)//从低到高
            {
                url = ZhaiDou.SearchGoodsIdUrl + json + "&pageNo=" + currentpage + "&orderBy=price&order=asc";
            } else if (sort == 2)//从高到低
            {

                url = ZhaiDou.SearchGoodsIdUrl + json + "&pageNo=" + currentpage + "&orderBy=price&order=desc";

            } else
            {
                url = ZhaiDou.SearchGoodsIdUrl + json + "&pageNo=" + currentpage;
            }
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


    /**
     * Taobao商品请求数据
     *
     * @param msg
     * @param sort
     * @param page
     */

    public void FetchData(String msg, int sort, int page)
    {
        mParam1 = msg;
        this.sort = sort;
        currentpage = page;
        if (page == 1) products.clear();
        Map<String, String> params = new HashMap<String, String>();
        params.put("search", msg);
        params.put("page", page + "");
        if (sort == 1)
        {
            params.put("hot_d", "desc");
        } else if (sort == 2)
        {
            params.put("price", "asc");
        } else if (sort == 3)
        {
            params.put("price", "desc");
        }
        ZhaiDouRequest newMissRequest = new ZhaiDouRequest(
                Request.Method.POST, ZhaiDou.SEARCH_PRODUCT_URL,
                params, new Response.Listener<JSONObject>()
        {

            @Override
            public void onResponse(JSONObject json)
            {

                ToolUtils.setLog(json.toString());
                setEndLoading();
                if (json != null)
                {
                    JSONArray items = json.optJSONArray("article_items");
                    if (items == null)
                    {
                        gv_single.onRefreshComplete();
                        if (products.size() == 0)
                        {
                            nullLine.setVisibility(View.VISIBLE);
                        }
                        return;
                    }
                    JSONObject meta = json.optJSONObject("meta");
                    if (meta == null)
                    {
                        count = 0;
                    } else
                    {
                        count = meta.optInt("count");
                    }
                    for (int i = 0; i < items.length(); i++)
                    {

                        JSONObject item = items.optJSONObject(i);
                        int id = item.optInt("id");
                        String title = item.optString("title");
                        double price = item.optDouble("price");
                        String url = item.optString("url");
                        int bean_like_count = item.optInt("bean_likes_count");
                        JSONArray array = item.optJSONArray("asset_imgs");
                        String image = null;
                        if (array.length() > 0)
                        {
                            JSONArray array1 = array.optJSONArray(0);
                            JSONObject object = array1.optJSONObject(1);
                            JSONObject picObj = object.optJSONObject("picture");
                            JSONObject thumbObj = picObj.optJSONObject("thumb");
                            image = thumbObj.optString("url");
                        }

                        Product product = new Product(id, title, price, url, bean_like_count, null, image);
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


            }
        });
        mRequestQueue.add(newMissRequest);
    }

    public void FetchCategoryData(String id, int sort, int page)
    {
        String url = ZhaiDou.ARTICLE_ITEM_WITH_CATEGORY + id + "&page=" + page;
        ZhaiDouRequest fetchCategoryTask = new ZhaiDouRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject jsonObject)
            {
                setEndLoading();
                JSONArray items = jsonObject.optJSONArray("article_items");
                if (items == null)
                {
                    gv_single.onRefreshComplete();
                    return;
                }
                for (int i = 0; i < items.length(); i++)
                {

                    JSONObject item = items.optJSONObject(i);
                    int id = item.optInt("id");
                    String title = item.optString("title");
                    Double price = item.optDouble("price");
                    String url = item.optString("url");
                    int bean_like_count = item.optInt("bean_likes_count");
                    JSONArray array = item.optJSONArray("asset_imgs");
                    JSONArray array1 = array.optJSONArray(0);
                    JSONObject object = array1.optJSONObject(1);
                    JSONObject picObj = object.optJSONObject("picture");
                    JSONObject thumbObj = picObj.optJSONObject("thumb");
                    String image = thumbObj.optString("url");

                    Product product = new Product(id, title, price, url, bean_like_count, null, image);
                    products.add(product);
                }
                handler.sendEmptyMessage(0);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                setEndLoading();
                if (products.size() == 0)
                {
                    nullLine.setVisibility(View.VISIBLE);
                }
            }
        });
        mRequestQueue.add(fetchCategoryTask);
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
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        products.clear();
        if ("category".equalsIgnoreCase(mParam2))//全分类
        {
            FetchCategoryData(mParam1, sort, currentpage = 1);
        } else if ("goods".equalsIgnoreCase(mParam2))//搜索特卖商城
        {
            if (mFlag == 1)
            {
                FetchSpecialData(mParam1, sort, currentpage = 1);
            } else
            {
                FetchSpecialIdData(mParam1, sort, currentpage = 1);
            }
        } else
        {
            FetchData(mParam1, sort, currentpage = 1);
        }
    }


    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView)
    {
        if ("category".equalsIgnoreCase(mParam2))//全分类
        {
            FetchCategoryData(mParam1, sort, ++currentpage);
        } else if ("goods".equalsIgnoreCase(mParam2))//搜索特卖商城
        {
            if (mFlag == 1)
            {
                FetchSpecialData(mParam1, sort, ++currentpage);
            } else
            {
                FetchSpecialIdData(mParam1, sort, ++currentpage);
            }
        } else
        {
            FetchData(mParam1, sort, ++currentpage);
        }
    }
}
