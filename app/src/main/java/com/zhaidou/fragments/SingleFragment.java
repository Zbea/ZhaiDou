package com.zhaidou.fragments;



import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.HtmlFetcher;

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
public class SingleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_FROM = "from";
    private static final String ID = "id";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int id=-1;

    private ImageView iv_heart;
    private TextView tv_money,tv_count,tv_detail;
    private GridView gv_single;

    private List<Product> products = new ArrayList<Product>();
    private RequestQueue mRequestQueue;
    private AsyncImageLoader1 imageLoader;
    private SingleAdapter singleAdapter;

    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();
//    private SingleAdapter mSingleAdapter;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i("handleMessage----->","handleMessage");
          singleAdapter.setList(products);
        }
    };


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param from Parameter 2.
     * @return A new instance of fragment SingleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SingleFragment newInstance(String param1, String from) {
        Log.i("SingleFragment----------->","newInstance");
        SingleFragment fragment = new SingleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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
//        new FetchDataTask().execute();
        return view;
    }

    private void initView(View view){
        tv_count=(TextView)view.findViewById(R.id.tv_count);
//        tv_detail=(TextView)view.findViewById(R.id.tv_detail);
        tv_money=(TextView)view.findViewById(R.id.tv_money);
        gv_single=(GridView)view.findViewById(R.id.gv_single);
        singleAdapter = new SingleAdapter(getActivity(),products);
        gv_single.setAdapter(singleAdapter);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        Log.i("mParam2----------->",mParam2);
        if ("category".equalsIgnoreCase(mParam2)){

            FetchCategoryData(mParam1, 0, 1);
        }else {
            FetchData(mParam1,0,1);
        }
//        FetchData(mParam1,0,1);

        singleAdapter.setOnInViewClickListener(R.id.ll_single_layout,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Log.i("position-------------->",position+"");
                Log.i("values----------------->", values.toString());
                Product product=(Product)values;
                Log.i("url-------------->",product.getUrl());
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("id", product.getId()+"");
                detailIntent.putExtra("title", product.getTitle());
//                detailIntent.putExtra("cover_url", product.getImg_url());
                detailIntent.putExtra("url",product.getUrl());
                startActivity(detailIntent);
            }
        });

    }

    public void FetchData(String msg,int sort,int page){
        if (page==1) products.clear();
        Map<String, String> params = new HashMap<String, String>();
        params.put("price", "desc");
        params.put("hot_d", "desc");
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
                Request.Method.POST, "http://192.168.1.45/article/api/article_items/search",
                new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {
                Log.i("onResponse",json.toString());
                JSONArray items = json.optJSONArray("article_items");
                if (items==null) return;
                for (int i=0;i<items.length();i++){

                    JSONObject item = items.optJSONObject(i);
                    int id=item.optInt("id");
                    String title =item.optString("title");
                    int price=item.optInt("price");
                    String url=item.optString("url");
                    int bean_like_count=item.optInt("bean_likes_count");
                    JSONArray array = item.optJSONArray("asset_imgs");
                    JSONArray array1 = array.optJSONArray(0);
                    JSONObject object =  array1.optJSONObject(1);
                    JSONObject picObj= object.optJSONObject("picture");
                    JSONObject thumbObj =picObj.optJSONObject("thumb");
                    String image =thumbObj.optString("url");
                    Log.i("image",image);

                    Product product = new Product(id,title,price,url,bean_like_count,null,image);
                    products.add(product);
                }
                handler.sendEmptyMessage(0);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("onErrorResponse",error.toString());
            }
        });
        Log.i("mRequestQueue------>",mRequestQueue.toString());
        Log.i("getActivity------>",getActivity().toString());
        if (mRequestQueue==null) mRequestQueue=Volley.newRequestQueue(getActivity());
        mRequestQueue.add(newMissRequest);
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
            mHashMap.put(position,convertView);
            return convertView;
        }
    }
    public void FetchCategoryData(String id,int sort,int page){
        Log.i("FetchCategoryData--------->","FetchCategoryData");
//        if (page==1) products.clear();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("price", "desc");
//        params.put("hot_d", "desc");
//        params.put("search", msg);
//        params.put("page",page+"");
//        if (sort==1){
//            params.put("hot_d","desc");
//        }else if (sort==2){
//            params.put("price","asc");
//        }else if (sort==3){
//            params.put("price","desc");
//        }

        String url="http://192.168.1.45/article/api/article_items?item_catetory_id="+id;
        JsonObjectRequest fetchCategoryTask = new JsonObjectRequest(url,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchCategoryData->onResponse",jsonObject.toString());
                JSONArray items = jsonObject.optJSONArray("article_items");
                if (items==null) return;
                for (int i=0;i<items.length();i++){

                    JSONObject item = items.optJSONObject(i);
                    int id=item.optInt("id");
                    String title =item.optString("title");
                    int price=item.optInt("price");
                    String url=item.optString("url");
                    int bean_like_count=item.optInt("bean_likes_count");
                    JSONArray array = item.optJSONArray("asset_imgs");
                    JSONArray array1 = array.optJSONArray(0);
                    JSONObject object =  array1.optJSONObject(1);
                    JSONObject picObj= object.optJSONObject("picture");
                    JSONObject thumbObj =picObj.optJSONObject("thumb");
                    String image =thumbObj.optString("url");
                    Log.i("image",image);

                    Product product = new Product(id,title,price,url,bean_like_count,null,image);
                    products.add(product);
                }
                handler.sendEmptyMessage(0);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(fetchCategoryTask);
    }

}
