package com.zhaidou.fragments;



import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshGridView;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ProductAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.HtmlFetcher;
import com.zhaidou.utils.NativeHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SingleFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SingleFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "categoryId";
    private static final String ARG_FROM = "from";
    private static final String ID = "id";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int id=-1;

    private int currentpage=1;
    private int count=-1;
    private int sort=0;

    private ImageView iv_heart;
    private TextView tv_money,tv_count,tv_detail;
    private PullToRefreshGridView gv_single;

    private List<Product> products = new ArrayList<Product>();
    private RequestQueue mRequestQueue;
    private AsyncImageLoader1 imageLoader;
    private ProductAdapter productAdapter;

    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();
    private Dialog mDialog;
//    private SingleAdapter mSingleAdapter;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mDialog!=null)mDialog.dismiss();
          productAdapter.setList(products);
          gv_single.onRefreshComplete();
        }
    };


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param from Parameter 2.
     * @return A new instance of fragment SingleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SingleFragment newInstance(String categoryId, String from) {
        SingleFragment fragment = new SingleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, categoryId);
        args.putString(ARG_FROM, from);
        fragment.setArguments(args);
        return fragment;
    }
    public SingleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_FROM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_single, container, false);
        initView(view);
//        FetchData(mParam1,sort,currentpage=1);
        return view;
    }

    private void initView(View view){
        tv_count=(TextView)view.findViewById(R.id.tv_count);
//        tv_detail=(TextView)view.findViewById(R.id.tv_detail);
        tv_money=(TextView)view.findViewById(R.id.tv_money);
        gv_single=(PullToRefreshGridView)view.findViewById(R.id.gv_single);
        productAdapter = new ProductAdapter(getActivity(),products);
        gv_single.setAdapter(productAdapter);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        if ("category".equalsIgnoreCase(mParam2)){
            FetchCategoryData(mParam1, sort, currentpage=1);
        }else {
            FetchData(mParam1, sort, currentpage=1);
        }

        gv_single.setMode(PullToRefreshBase.Mode.BOTH);
        gv_single.setOnRefreshListener(this);
        productAdapter.setOnInViewClickListener(R.id.ll_single_layout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Product product=(Product)values;
                Intent detailIntent = new Intent(getActivity(), WebViewActivity.class);;
                detailIntent.putExtra("id", product.getId()+"");
                detailIntent.putExtra("title", product.getTitle());
//                detailIntent.putExtra("cover_url", product.getImg_url());
                detailIntent.putExtra("url",product.getUrl());
                startActivity(detailIntent);
            }
        });
    }

    /**
     *开始加载进度
     */
    private void setStartLoading()
    {
        mDialog= CustomLoadingDialog.setLoadingDialog(getActivity(),"loading");
    }

    /**
     * 结束加载进度
     */
    private void setEndLoading()
    {
        if (mDialog!=null)
        {
            mDialog.dismiss();
        }
    }

    public void FetchData(String msg,int sort,int page){
        mParam1=msg;
        setStartLoading();
        Log.i("FetchData------>","FetchData");
        this.sort=sort;
        currentpage=page;
        if (page==1) products.clear();
        Map<String, String> params = new HashMap<String, String>();
        params.put("search", msg);
        params.put("page",page+"");
        if (sort==1){
            params.put("hot_d","desc");
        }else if (sort==2){
            params.put("price","asc");
        }else if (sort==3){
            params.put("price","desc");
        }
//        new ProductTask().execute(params);
        JsonObjectRequest newMissRequest = new JsonObjectRequest(
                Request.Method.POST, ZhaiDou.SEARCH_PRODUCT_URL,
                new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {

                mDialog.hide();
                JSONArray items = json.optJSONArray("article_items");
                JSONObject meta = json.optJSONObject("meta");

                count=meta==null?0:meta.optInt("count");
                if (items==null)
                {
                    gv_single.onRefreshComplete();
                    Toast.makeText(getActivity(),"抱歉，未找到商品",Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i=0;i<items.length();i++){

                    JSONObject item = items.optJSONObject(i);
                    int id=item.optInt("id");
                    String title =item.optString("title");
                    double price=item.optDouble("price");
                    Log.i("zhaidou-------->","价格：￥"+price);
                    String url=item.optString("url");
                    int bean_like_count=item.optInt("bean_likes_count");
                    JSONArray array = item.optJSONArray("asset_imgs");
                    String image=null;
                    if (array.length()>0){
                        JSONArray array1 = array.optJSONArray(0);
                        JSONObject object =  array1.optJSONObject(1);
                        JSONObject picObj= object.optJSONObject("picture");
                        JSONObject thumbObj =picObj.optJSONObject("thumb");
                        image =thumbObj.optString("url");
                        Log.i("image",image);
                    }

                    Product product = new Product(id,title,price,url,bean_like_count,null,image);
                    products.add(product);
                }
                handler.sendEmptyMessage(0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setEndLoading();
                Toast.makeText(getActivity(),"抱歉,加载失败",Toast.LENGTH_SHORT).show();
                Log.i("onErrorResponse",error.toString());
            }
        });
        if (mRequestQueue==null) mRequestQueue=Volley.newRequestQueue(getActivity());
        mRequestQueue.add(newMissRequest);
    }

    private class ProductTask extends AsyncTask<Map<String,String>,Void,String>{
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            String result=null;
            try {
                result= NativeHttpUtil.post(ZhaiDou.SEARCH_PRODUCT_URL,null,maps[0]);
            }catch (Exception e){

            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("onPostExecute---->ProductTask--->",s);
        }
    }

    public void FetchCategoryData(String id,int sort,int page){
        setStartLoading();
        String url=ZhaiDou.ARTICLE_ITEM_WITH_CATEGORY+id+"&page="+page;
        JsonObjectRequest fetchCategoryTask = new JsonObjectRequest(url,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                mDialog.hide();
                JSONArray items = jsonObject.optJSONArray("article_items");
                if (items==null)
                {
                    gv_single.onRefreshComplete();
                    Toast.makeText(getActivity(),"抱歉，未找到商品",Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i=0;i<items.length();i++){

                    JSONObject item = items.optJSONObject(i);
                    int id=item.optInt("id");
                    String title =item.optString("title");
                    Double price=item.optDouble("price");
                    String url=item.optString("url");
                    int bean_like_count=item.optInt("bean_likes_count");
                    JSONArray array = item.optJSONArray("asset_imgs");
                    JSONArray array1 = array.optJSONArray(0);
                    JSONObject object =  array1.optJSONObject(1);
                    JSONObject picObj= object.optJSONObject("picture");
                    JSONObject thumbObj =picObj.optJSONObject("thumb");
                    String image =thumbObj.optString("url");

                    Product product = new Product(id,title,price,url,bean_like_count,null,image);
                    products.add(product);
                }
                handler.sendEmptyMessage(0);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                setEndLoading();
                Toast.makeText(getActivity(),"抱歉，加载失败",Toast.LENGTH_SHORT).show();
            }
        });
        mRequestQueue.add(fetchCategoryTask);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        Log.i("onPullDownToRefresh--->","onPullDownToRefresh");
//        FetchData(mParam1,sort,currentpage=1);
        products.clear();
        if ("category".equalsIgnoreCase(mParam2)){
            FetchCategoryData(mParam1, sort, currentpage=1);
        }else {
            FetchData(mParam1, sort, currentpage=1);
        }
        gv_single.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
//        FetchData(mParam1,sort,++currentpage);
        if (count!=-1&&productAdapter.getCount()==count){
            Toast.makeText(getActivity(),"已经加载完毕",Toast.LENGTH_SHORT).show();
            gv_single.onRefreshComplete();
            gv_single.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
        if ("category".equalsIgnoreCase(mParam2)){
            FetchCategoryData(mParam1, sort,++currentpage);
        }else {
            FetchData(mParam1, sort, ++currentpage);
        }
    }
}
