package com.zhaidou.fragments;



import android.content.Context;
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
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Article;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CollectFragment extends Fragment implements PullToRefreshBase.OnRefreshListener2<GridView>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<Product> products = new ArrayList<Product>();

    private PullToRefreshGridView mGridView;


    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;
    private AsyncImageLoader1 imageLoader;
    private SingleAdapter singleAdapter;

    private int count;
    private int currentpage=1;


    private Map<Integer,View> mHashMap=new HashMap<Integer, View>();
    private CollectCountChangeListener collectCountChangeListener;
    private Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            singleAdapter.notifyDataSetChanged();
            int num =msg.arg1;
            if (collectCountChangeListener!=null){
                collectCountChangeListener.onCountChange(count,CollectFragment.this);
                mGridView.onRefreshComplete();
            }
        }
    };
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CollectFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        singleAdapter=new SingleAdapter(getActivity(),products);
        mGridView.setAdapter(singleAdapter);

        singleAdapter.setOnInViewClickListener(R.id.ll_single_layout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Product product=(Product)values;
                Log.i("product",product.toString());
                Intent intent = new Intent();
                intent.putExtra("url", product.getUrl());
                intent.setClass(getActivity(), WebViewActivity.class);
                getActivity().startActivity(intent);
            }
        });
        singleAdapter.setOnInViewClickListener(R.id.ll_collect_heart,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Log.i("ll_collect_heart---->","ll_collect_heart");
                Product product=(Product)values;
                Log.i("product",product.toString());
                int userId= mSharedPreferences.getInt("userId", -1);
                String token=mSharedPreferences.getString("token","");
                int itemId=product.getId();
                new CancelTask().execute(userId+"",itemId+"",token,""+position);
            }
        });
        new MyTask().execute();
        return view;
    }

    private class MyTask extends AsyncTask<Void,Void,JSONObject>{
        @Override
        protected JSONObject doInBackground(Void... voids) {
            return getHttp("http://192.168.199.171/article/api/article_items/like_article_items?per_page=10&page="+currentpage, null);
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
//            Log.i("onPostExecute------>",jsonObject.toString());
            JSONArray article_items=jsonObject.optJSONArray("article_items");
            JSONObject meta = jsonObject.optJSONObject("meta");
            count=meta==null?0:meta.optInt("count");

//            Log.i("meta---->",meta.toString());
            if (article_items!=null&&article_items.length()>0){
                for (int i=0;i<article_items.length();i++){
                    JSONObject articleObj = article_items.optJSONObject(i);
                    int id = articleObj.optInt("id");
                    int bean_likes_count=articleObj.optInt("bean_likes_count");
                    String title=articleObj.optString("title");
                    int price = articleObj.optInt("price");
                    String url=articleObj.optString("url");
                    JSONArray asset_imgs=articleObj.optJSONArray("asset_imgs");
                    String thumb=null;
                    if (asset_imgs!=null&&asset_imgs.length()>0){
                        JSONObject picObj = asset_imgs.optJSONArray(0).optJSONObject(1);
                        thumb=picObj.optJSONObject("picture").optJSONObject("thumb").optString("url");
                    }
                    Product product=new Product(id,title,price,url,bean_likes_count,null,thumb);
                    products.add(product);
                }
                Message message=new Message();
                message.arg1=count;
                mHandler.sendMessage(message);
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

//        if (headers != null) {
//            Set<String> keys = headers.keySet();
//            for (Iterator<String> i = keys.iterator(); i.hasNext();) {
//                String key = (String) i.next();
//                httpGet.addHeader(key, headers.get(key));
//            }
//        }
        String token=mSharedPreferences.getString("token","");
        httpGet.addHeader("SECAuthorization",token);

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                5000);
        HttpConnectionParams.setSoTimeout(httpParameters, 5000);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            InputStream inStream =     httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream,"utf-8"));
            StringBuilder strber = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                strber.append(line + "\n");
            inStream.close();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.i("MobilpriseActivity", "success");
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

    public class SingleAdapter extends BaseListAdapter<Product> {
        public SingleAdapter(Context context, List<Product> list) {
            super(context, list);
            imageLoader = new AsyncImageLoader1(context);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView=mHashMap.get(position);
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_fragment_single,null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            ImageView image =ViewHolder.get(convertView,R.id.iv_single_item);
            TextView tv_money=ViewHolder.get(convertView,R.id.tv_money);
            ImageView iv_heart=ViewHolder.get(convertView,R.id.iv_heart);
            TextView tv_count=ViewHolder.get(convertView,R.id.tv_count);

            Product product = getList().get(position);
            tv_name.setText(product.getTitle());
            tv_money.setText("￥"+product.getPrice()+"元");
//            tv_count.setText(product.getBean_like_count());
            imageLoader.LoadImage("http://"+product.getImage(),image);
            iv_heart.setImageResource(R.drawable.heart_pressed);
            mHashMap.put(position,convertView);
            return convertView;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.i("hidden--------------->",hidden+"");
    }

    private class CancelTask extends AsyncTask<String,Void,String>{
        String position;
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
            Log.i("CancelTask------>",s);
            try {
                if (!TextUtils.isEmpty(s)){
                    JSONObject jsonObject = new JSONObject(s);
                    String like_state=jsonObject.optString("like_state");
                    String likes=jsonObject.optString("likes");
                    if (Integer.parseInt(position)<singleAdapter.getCount()){
                        singleAdapter.remove(Integer.parseInt(position));
                        singleAdapter.notifyDataSetChanged();
                        if (collectCountChangeListener!=null){
                            collectCountChangeListener.onCountChange(count-1,CollectFragment.this);
                        }
                    }
                }
            }catch (Exception e){

            }
        }
    }
    public String executeHttpPost(String liker_id,String article_item_id,String token) throws Exception {
        Log.i("liker_id--->",liker_id==null?"":liker_id);
        Log.i("article_item_id--->",article_item_id==null?"":article_item_id);
        Log.i("token--->",token==null?"":token);
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost("http://192.168.199.171/article/api/article_items/like");
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
            Log.i("EditProfileFragment--------->",result);
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
        Log.i("onPullDownToRefresh--->","onPullDownToRefresh");
//        FetchData(mParam1,sort,currentpage=1);
//        gv_single.setMode(PullToRefreshBase.Mode.BOTH);
        currentpage=1;
        products.clear();
        new MyTask().execute();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
        if (count!=-1&&singleAdapter.getCount()==count){
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            mGridView.onRefreshComplete();
            mGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        ++currentpage;
        new MyTask().execute();
    }
}
