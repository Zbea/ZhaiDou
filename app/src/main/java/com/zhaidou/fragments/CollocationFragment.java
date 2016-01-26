package com.zhaidou.fragments;



import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshGridView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Collocation;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollocationFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<GridView>{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private SharedPreferences mSharedPreferences;

    private int count=-1;
    private int currentpage=1;

    private Map<Integer,View> mHashMap=new HashMap<Integer, View>();

    private List<Collocation> collocations=new ArrayList<Collocation>();
    private RequestQueue mRequestQueue;
    private PullToRefreshGridView mGridView;
    private CollocationAdapter mAdapter;

    private Activity mActivity;
    private CollocationCountChangeListener collocationCountChangeListener;

    private LinearLayout loadingView,nullLine;

    private Dialog mDialog;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    loadingView.setVisibility(View.GONE);
                    if (mDialog!=null&&mDialog.isShowing())
                        mDialog.dismiss();
                    mAdapter.notifyDataSetChanged();
                    int num = msg.arg1;
                    if (collocationCountChangeListener!=null){
                        collocationCountChangeListener.onCountChange(num,CollocationFragment.this);
                    }
                    mGridView.onRefreshComplete();
                    break;
            }
        }
    };

    public static CollocationFragment newInstance(String param1, String param2) {
        CollocationFragment fragment = new CollocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CollocationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity=activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_collocation, container, false);
        mGridView=(PullToRefreshGridView)view.findViewById(R.id.gv_collocation);
        mGridView.setMode(PullToRefreshBase.Mode.BOTH);
        mGridView.setOnRefreshListener(this);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mAdapter=new CollocationAdapter(getActivity(),collocations);
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        mGridView.setAdapter(mAdapter);

        loadingView=(LinearLayout)view.findViewById(R.id.loadingView);
        nullLine=(LinearLayout)view.findViewById(R.id.nullLine);

        if (NetworkUtils.isNetworkAvailable(getActivity()))
        {
            FetchCollocationData(currentpage);
        }
        else
        {
            ToolUtils.setToast(getActivity(),"抱歉,网络连接失败");
        }
        return view;
    }

    public void FetchCollocationData(int page){
        mDialog= CustomLoadingDialog.setLoadingDialog(mActivity,"loading",isDialogFirstVisible);
        isDialogFirstVisible=false;
        int userId=mSharedPreferences.getInt("userId", -1);
        Log.i("FetchCollocationData------->",userId+"");
        //"http://www.zhaidou.com/api/v1/users/77069/bean_collocations?page="+page
        JsonObjectRequest request =new JsonObjectRequest(ZhaiDou.USER_COLLOCATION_ITEM_URL+userId+"/bean_collocations?page="+page
            ,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchCollocationData----->",jsonObject.toString());
                if (mDialog!=null)
                {
                    mDialog.dismiss();
                }
                JSONArray collocationsArr=jsonObject.optJSONArray("bean_collocations");
                JSONObject meta = jsonObject.optJSONObject("meta");
                count=meta==null?0:meta.optInt("count");
                Collocation collocation= null;
                if (collocationsArr!=null&&collocationsArr.length()>0)
                {
                    for (int i=0;i<collocationsArr.length();i++){
                        JSONObject collocationObj=collocationsArr.optJSONObject(i);
                        int id=collocationObj.optInt("id");
                        String title=collocationObj.optString("title");
                        String thumb_pic=collocationObj.optString("thumb_pic");
                        String media_pic=collocationObj.optString("media_pic");
                        collocation=new Collocation();
                        collocation.setId(id);
                        collocation.setTitle(title);
                        collocation.setThumb_pic(thumb_pic);
                        collocation.setMedia_pic(media_pic);
                        collocations.add(collocation);
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
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog!=null)
                {
                    mDialog.dismiss();
                }
                nullLine.setVisibility(View.VISIBLE);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public class CollocationAdapter extends BaseListAdapter<Collocation> {
        public CollocationAdapter(Context context, List<Collocation> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView=mHashMap.get(position);
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.gv_collocation_item,null);
            TextView tv_title = ViewHolder.get(convertView, R.id.tv_collocation_title);
            ImageView iv_thumb=ViewHolder.get(convertView,R.id.iv_collocation_thumb);
            Collocation collocation=getList().get(position);
            tv_title.setText(collocation.getTitle());
            ToolUtils.setImageCacheUrl("http://"+collocation.getMedia_pic(),iv_thumb);
            mHashMap.put(position,convertView);
            return convertView;
        }
    }

    public void setCollocationCountChangeListener(CollocationCountChangeListener collocationCountChangeListener) {
        this.collocationCountChangeListener = collocationCountChangeListener;
    }

    public interface CollocationCountChangeListener{
        public void onCountChange(int count,Fragment fragment);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        collocations.clear();
        mAdapter.clear();
        FetchCollocationData(currentpage=1);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
        if (count!=-1&&mAdapter.getCount()==count){
            Toast.makeText(getActivity(), "已经加载完毕", Toast.LENGTH_SHORT).show();
            mGridView.onRefreshComplete();
            mGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            return;
        }
        FetchCollocationData(++currentpage);
    }

    public void refreshData(){
        collocations.clear();
        if (mAdapter!=null){
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        }
        FetchCollocationData(currentpage=1);
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_collocation));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_collocation));
    }
}
