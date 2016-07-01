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
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.RecommendAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.RecommendItem;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roy on 15/8/28.
 */
public class SettingRecommendFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private String mIndex;
    private Context mContext;
    private Dialog mDialog;
    private ListView mListView;
    private TextView backBtn,headTitle;
    private List<RecommendItem> lists=new ArrayList<RecommendItem>();
    private RequestQueue mRequestQueue;
    private RecommendAdapter recommendAdapter;

    private Handler mHandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:
                    recommendAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent intent = new Intent();
            intent.putExtra("url", lists.get(position).appUrl);
            intent.setClass(getActivity(), WebViewActivity.class);
            getActivity().startActivity(intent);
        }
    };


    public static SettingRecommendFragment newInstance(String page, String index) {
        SettingRecommendFragment fragment = new SettingRecommendFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingRecommendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(DATA);
            mIndex = getArguments().getString(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.setting_recommend_page, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }

    /**
     * 初始化
     */
    private void initView() {
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"loading");

        headTitle = (TextView) mView.findViewById(R.id.title_tv);
        headTitle.setText(R.string.setting_recommend_txt);

        mListView=(ListView)mView.findViewById(R.id.recommendList);
        recommendAdapter=new RecommendAdapter(mContext,lists);
        mListView.setAdapter(recommendAdapter);
        mListView.setOnItemClickListener(onItemClickListener);
        mRequestQueue= Volley.newRequestQueue(mContext);

        FetchData();
    }

    /**
     * 加载列表数据
     */
    private void FetchData()
    {
        lists.clear();
        String url=ZhaiDou.settingRecommendAppUrl;
        ToolUtils.setLog(url);
        JsonObjectRequest jr = new JsonObjectRequest(url, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog!=null)
                    mDialog.dismiss();
                if (response!=null)
                {
                    int stutus=response.optInt("status");
                    if (stutus!=200)
                    {
                        return;
                    }
                    JSONObject jsonObject=response.optJSONObject("data");
                    if (jsonObject!=null)
                    {
                        JSONArray array=jsonObject.optJSONArray("app_exchanges");
                        for (int i = 0; i < array.length(); i++)
                        {
                            JSONObject obj=array.optJSONObject(i);
                            String title=obj.optString("title");
                            String desc=obj.optString("desc");
                            String android_url=obj.optString("android");
                            String logo=obj.optString("logo");
                            RecommendItem recommendItem=new RecommendItem();
                            recommendItem.title=title;
                            recommendItem.info=desc;
                            recommendItem.appUrl=android_url;
                            recommendItem.imageUrl=logo;
                            lists.add(recommendItem);

                        }
                        mHandler.sendEmptyMessage(1);
                    }

                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (mDialog!=null)
                    mDialog.dismiss();
                ToolUtils.setToast(mContext,"加载失败");
            }
        })        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(jr);
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.setting_recommend_txt));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.setting_recommend_txt));
    }

}
