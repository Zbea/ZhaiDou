package com.zhaidou.fragments;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ProductAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView>{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<Product> products = new ArrayList<Product>();

    private PullToRefreshGridView mGridView;


    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;
    private ProductAdapter productAdapter;

    private int count;
    private int currentpage=1;

    private LinearLayout loadingView,nullLine;
    private Dialog mDialog;

    private long lastClickTime;
    private Map<Integer,View> mHashMap=new HashMap<Integer, View>();
    private CollectCountChangeListener collectCountChangeListener;
    private Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(mDialog!=null)
            {
                mDialog.dismiss();
            }
            loadingView.setVisibility(View.GONE);
            productAdapter.notifyDataSetChanged();
            if (products.size()==0)
            {
                loadingView.setVisibility(View.VISIBLE);
                nullLine.setVisibility(View.VISIBLE);
            }
            int num =msg.arg1;
            if (collectCountChangeListener!=null){
                collectCountChangeListener.onCountChange(count,CollectFragment.this);
                mGridView.onRefreshComplete();
            }
        }
    };

    public static CollectFragment newInstance(String param1, String param2) {
        CollectFragment fragment = new CollectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CollectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_collect, container, false);
        mGridView=(PullToRefreshGridView)view.findViewById(R.id.gv_collect);
        mGridView.setMode(PullToRefreshBase.Mode.BOTH);
        mGridView.setOnRefreshListener(this);

        mRequestQueue= Volley.newRequestQueue(getActivity());
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        productAdapter=new ProductAdapter(getActivity(),products,2,screenWidth);
        mGridView.setAdapter(productAdapter);

        productAdapter.setOnInViewClickListener(R.id.ll_single_layout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                if (System.currentTimeMillis()-lastClickTime<1000){
                    Toast.makeText(getActivity(),"豆豆，你点击太快啦。。。",Toast.LENGTH_SHORT).show();
                    lastClickTime=System.currentTimeMillis();
                    return;
                }
                Product product=(Product)values;
                Intent intent = new Intent();
                intent.putExtra("url", product.getUrl());
                intent.setClass(getActivity(), WebViewActivity.class);
                getActivity().startActivity(intent);
            }
        });
        productAdapter.setOnInViewClickListener(R.id.ll_collect_heart,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                 final Product product=(Product)values;
                final int index=position;

//                ImageView mHeartView=(ImageView)v.findViewById(R.id.iv_heart);
//                mHeartView.setSelected(!mHeartView.isSelected());
//                Log.i("mHeartView.isPressed()----->",(mHeartView.isSelected())+"");
//                new CancelTask().execute(userId+"",itemId+"",token,""+position);

                final Dialog dialog=new Dialog(getActivity(), R.style.custom_dialog);

                View view= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_collect_hint,null);
                TextView cancelTv=(TextView)view.findViewById(R.id.cancelTv);
                cancelTv.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.dismiss();
                    }
                });

                TextView okTv=(TextView)view.findViewById(R.id.okTv);
                okTv.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.dismiss();
                        int userId= mSharedPreferences.getInt("userId", -1);
                        String token=mSharedPreferences.getString("token","");
                        int itemId=product.getId();
                        new CancelTask().execute(userId+"",itemId+"",token,""+index);
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();
            }
        });

        loadingView=(LinearLayout)view.findViewById(R.id.loadingView);
        nullLine=(LinearLayout)view.findViewById(R.id.nullLine);

        if (NetworkUtils.isNetworkAvailable(getActivity()))
        {
            mDialog=CustomLoadingDialog.setLoadingDialog(getActivity(),"loading");
            new MyTask().execute();
        }
        else
        {
            ToolUtils.setToast(getActivity(), "抱歉,网络连接失败");
        }

        return view;
    }

    private class MyTask extends AsyncTask<Void,Void,JSONObject>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            return getHttp(ZhaiDou.USER_COLLECT_ITEM_URL+currentpage, null);
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(mDialog!=null)
            {
                mDialog.dismiss();
            }
            if (jsonObject!=null)
            {
                JSONArray article_items=jsonObject.optJSONArray("article_items");
                JSONObject meta = jsonObject.optJSONObject("meta");
                count=meta==null?0:meta.optInt("count");
                if (article_items!=null&&article_items.length()>0){
                    for (int i=0;i<article_items.length();i++){
                        JSONObject articleObj = article_items.optJSONObject(i);
                        int id = articleObj.optInt("id");
                        int bean_likes_count=articleObj.optInt("bean_likes_count");
                        String title=articleObj.optString("title");
                        double price = articleObj.optDouble("price");
                        String url=articleObj.optString("url");
                        JSONArray asset_imgs=articleObj.optJSONArray("asset_imgs");
                        String thumb=null;
                        if (asset_imgs!=null&&asset_imgs.length()>0){
                            JSONObject picObj = asset_imgs.optJSONArray(0).optJSONObject(1);
                            thumb=picObj.optJSONObject("picture").optJSONObject("thumb").optString("url");
                        }
                        Product product=new Product(id,title,price,url,bean_likes_count,null,thumb);
                        product.setCollect(true);
                        products.add(product);
                    }
                }
                Message message=new Message();
                message.arg1=count;
                mHandler.sendMessage(message);
            }
            else
            {
                nullLine.setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * get
     * @param url
     * @param headers
     */
    private JSONObject getHttp(String url,Map<String, String> headers){
        HttpGet httpGet = new HttpGet(url);

        String token=mSharedPreferences.getString("token","");
        httpGet.addHeader("SECAuthorization",token);

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                5000);
        HttpConnectionParams.setSoTimeout(httpParameters, 5000);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream,"utf-8"));
            StringBuilder strber = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                strber.append(line + "\n");
            inStream.close();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            }
            return new JSONObject(strber.toString());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private class CancelTask extends AsyncTask<String,Void,String>{
        String position;
        @Override
        protected void onPreExecute()
        {
            mDialog=CustomLoadingDialog.setLoadingDialog(getActivity(),"");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String s=null;
            try {
                position=strings[3];
                s=executeHttpPost(strings[0],strings[1],strings[2]);
            }catch (Exception e){
                Log.i("e------------>",e.getMessage());
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mDialog!=null)
                mDialog.dismiss();
            Log.i("CancelTask------>",s);
            try {
                if (!TextUtils.isEmpty(s))
                {
                    JSONObject jsonObject = new JSONObject(s);
                    String like_state=jsonObject.optString("like_state");
                    String likes=jsonObject.optString("likes");
                    if (Integer.parseInt(position)<productAdapter.getCount())
                    {
                        productAdapter.remove(Integer.parseInt(position));
                        if (collectCountChangeListener!=null){
                            collectCountChangeListener.onCountChange(productAdapter.getCount(),CollectFragment.this);
                        }
                        if (productAdapter.getCount()==0)
                        {
                            loadingView.setVisibility(View.VISIBLE);
                            nullLine.setVisibility(View.VISIBLE);
                        }
                        productAdapter.notifyDataSetChanged();

                        Intent intent=new Intent(ZhaiDou.IntentRefreshCollectDesTag);
                        getActivity().sendBroadcast(intent);

                    }
                }
                else
                {
                    ToolUtils.setToast(getActivity(),"抱歉,取消失败");
                }
            }catch (Exception e){

            }
        }
    }
    public String executeHttpPost(String liker_id,String article_item_id,String token) throws Exception {
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_DELETE_COLLECT_ITEM_URL);
            request.addHeader("SECAuthorization", token);

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

    public void setCollectCountChangeListener(CollectCountChangeListener collectCountChangeListener) {
        this.collectCountChangeListener = collectCountChangeListener;
    }

    public interface CollectCountChangeListener{
        public void onCountChange(int count,Fragment fragment);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
//        FetchData(mParam1,sort,currentpage=1);
//        gv_single.setMode(PullToRefreshBase.Mode.BOTH);
        currentpage=1;
        products.clear();
        new MyTask().execute();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGridView.onRefreshComplete();
            }
        },3000);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
        if (count!=-1&&productAdapter.getCount()==count){
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            mGridView.onRefreshComplete();
            mGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        ++currentpage;
        new MyTask().execute();
    }

    public void refreshData(){
        currentpage=1;
        products.clear();
        productAdapter.notifyDataSetChanged();
        new MyTask().execute();
    }
}
