package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomeCompetitionActivity;
import com.zhaidou.adapter.RecommendAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.model.RecommendItem;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.PixelUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


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
            Intent detailIntent = new Intent(getActivity(), HomeCompetitionActivity.class);
            detailIntent.putExtra("url", lists.get(position).appUrl);
            detailIntent.putExtra("from", "app");
            detailIntent.putExtra("title", lists.get(position).title);
            getActivity().startActivity(detailIntent);
        }
    };


    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(SettingRecommendFragment.this);
                    break;
            }
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
        backBtn = (TextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
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
                JSONArray array=response.optJSONArray("app_exchanges");
                for (int i = 0; i < array.length(); i++)
                {
                    JSONObject obj=array.optJSONObject(i);
                    String title=obj.optString("title");
                    String desc=obj.optString("desc");
                    String android_url=obj.optString("android");
                    String logo="http://192.168.199.163:3000"+obj.optString("logo");
                    ToolUtils.setLog(android_url);
                    RecommendItem recommendItem=new RecommendItem();
                    recommendItem.title=title;
                    recommendItem.info=desc;
                    recommendItem.appUrl=android_url;
                    recommendItem.imageUrl=logo;
                    lists.add(recommendItem);

                }
                mHandler.obtainMessage(1).sendToTarget();
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
        });
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
