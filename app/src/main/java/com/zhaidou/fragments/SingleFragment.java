package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshGridView;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ProductAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Product;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SingleFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView>{
    private static final String ARG_PARAM1 = "categoryId";
    private static final String ARG_FROM = "from";
    private static final String ID = "id";

    private String mParam1;
    private String mParam2;
    private int id=-1;
    private View mView;
    private Context mContext;

    private String token;
    private int userid;
    private boolean isLogin=false;

    private int currentpage=1;
    private int count=-1;
    private int sort=0;

    private ImageView iv_heart;
    private TextView tv_money,tv_count,tv_detail;
    private PullToRefreshGridView gv_single;

    private LinearLayout nullLine;

    private List<Product> products = new ArrayList<Product>();
    private RequestQueue mRequestQueue;
    private ProductAdapter productAdapter;

    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();
    private Dialog mDialog;
    private DialogUtils mDialogUtils;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mDialog!=null)mDialog.dismiss();
            if (products.size()>0)
            {
                productAdapter.setList(products);
                nullLine.setVisibility(View.GONE);
            }
          gv_single.onRefreshComplete();
        }
    };

    public static SingleFragment newInstance(String categoryId, String from) {
        SingleFragment fragment = new SingleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, categoryId);
        args.putString(ARG_FROM, from);
        fragment.setArguments(args);
        return fragment;
    }
    public SingleFragment() {
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
        mContext=getActivity();
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
        isLogin=checkLogin();
        mDialogUtils=new DialogUtils(mContext);
        tv_count=(TextView)view.findViewById(R.id.tv_count);
        tv_money=(TextView)view.findViewById(R.id.tv_money);
        gv_single=(PullToRefreshGridView)view.findViewById(R.id.gv_single);
        nullLine=(LinearLayout)view.findViewById(R.id.nullline);
        productAdapter = new ProductAdapter(getActivity(),products,1,screenWidth);
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
        productAdapter.setOnInViewClickListener(R.id.iv_heart,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
                if (isLogin)
                {
                    setStartLoading();
                    new CollectTask().execute(userid+"",((Product)values).getId()+"",token,""+position);
                }
                else
                {
                    ToolUtils.setToast(getActivity(),"抱歉,尚未登录");
                }
            }
        });
    }

    public boolean checkLogin()
    {
        token=(String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        userid=(Integer)SharedPreferencesUtil.getData(getActivity(),"userId",-1);

        if (token!=null)
        {
            isLogin=false;
            if (token.length()>0&&userid>0)
            {
                isLogin=true;
            }
        }
        else
        {
            isLogin=false;
        }
        return isLogin;
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
                    nullLine.setVisibility(View.VISIBLE);
                    return;
                }
                for (int i=0;i<items.length();i++){

                    JSONObject item = items.optJSONObject(i);
                    int id=item.optInt("id");
                    String title =item.optString("title");
                    double price=item.optDouble("price");
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
                nullLine.setVisibility(View.GONE);
                Log.i("onErrorResponse",error.toString());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        if (mRequestQueue==null) mRequestQueue=Volley.newRequestQueue(getActivity());
        mRequestQueue.add(newMissRequest);
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
                    Toast.makeText(getActivity(),"抱歉,未找到商品",Toast.LENGTH_SHORT).show();
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
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(fetchCategoryTask);
    }

    /**
     * 收藏
     */
    private class CollectTask extends AsyncTask<String,Void,String>{
        String position;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            String s=null;
            try {
                position=strings[3];
                s=executeHttpPost(strings[0],strings[1],strings[2]);
            }catch (Exception e){
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mDialog!=null)
                mDialog.dismiss();
            try {
                if (!TextUtils.isEmpty(s))
                {
                    JSONObject jsonObject = new JSONObject(s);
                    ToolUtils.setLog(jsonObject.toString());
                    String like_state=jsonObject.optString("like_state");
                    String likes=jsonObject.optString("likes");

                    if(like_state.equals("true"))
                    {
                        productAdapter.setmCheckPosition(Integer.valueOf(position),Integer.valueOf(position));
                        productAdapter.notifyDataSetChanged();
                        mDialogUtils.showCollectDialog(mContext,R.drawable.dialog_loading_success_icon,R.string.collect_success);
                    }
                    else
                    {
                        productAdapter.setmCheckPosition(Integer.valueOf(position),-1);
                        productAdapter.notifyDataSetChanged();
                        mDialogUtils.showCollectDialog(mContext,R.drawable.dialog_loading_success_icon,R.string.collect_cancel);
                    }
                }
                else
                {
                    mDialogUtils.showCollectDialog(mContext,R.drawable.dialog_loading_success_icon,R.string.collect_fail);
                }
            }catch (Exception e){
            }
        }
    }

    /**
     * 收藏响应
     * @param liker_id
     * @param article_item_id
     * @param token
     * @return
     * @throws Exception
     */
    public String executeHttpPost(String liker_id,String article_item_id,String token) throws Exception {
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_DELETE_COLLECT_ITEM_URL);
            request.addHeader("SECAuthorization", token);
            request.addHeader("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("liker_id",liker_id));
            parameters.add(new BasicNameValuePair("article_item_id",article_item_id));

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    parameters);
            request.setEntity(formEntiry);
            // 执行请求
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String result = sb.toString();
            return result;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
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
