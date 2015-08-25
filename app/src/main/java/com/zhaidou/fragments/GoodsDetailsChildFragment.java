package com.zhaidou.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.zhaidou.R;

import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.utils.PixelUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/23.
 */
public class GoodsDetailsChildFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private List<GoodInfo> datas = new ArrayList<GoodInfo>();
    private int mIndex;
    private Context mContext;
    private ListView mListView;
    private GoodInfoAdapter mAdapter;
    private List<GoodInfo> goodInfos;
    private RequestQueue mRequestQueue;
    private static final int UPDATE_GOOD_INFO = 0;
    private GoodDetail detail;
    private LinearLayout mImageContainer;
    private LinearLayout mDetailContainer;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_GOOD_INFO:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public static GoodsDetailsChildFragment newInstance(GoodDetail infos, ArrayList<GoodInfo> datas) {
        GoodsDetailsChildFragment fragment = new GoodsDetailsChildFragment();
        Bundle args = new Bundle();
        args.putSerializable("datas", datas);
        args.putSerializable("details", infos);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsDetailsChildFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            datas = (ArrayList<GoodInfo>) getArguments().getSerializable("datas");
            detail = (GoodDetail) getArguments().getSerializable("details");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.goods_details_info_page, container, false);
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


    private void initView() {
        mDetailContainer = (LinearLayout) mView.findViewById(R.id.ll_detail_container);
        mListView = (ListView) mView.findViewById(R.id.lv_good_info);
        mImageContainer = (LinearLayout) mView.findViewById(R.id.ll_img_container);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mAdapter = new GoodInfoAdapter(getActivity(), datas);
        mListView.setAdapter(mAdapter);
        if (detail != null) {
            if (detail.getImgs() != null)
                addImageToContainer(detail.getImgs());

        }
//        final ViewTreeObserver observer = mDetailContainer.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Log.i("mDetailContainer.getMeasuredWidth()-------------------->", mDetailContainer.getMeasuredWidth() + "");
//                Log.i("mDetailContainer.getMeasuredHeight()-------------------->", mDetailContainer.getMeasuredHeight() + "");
//                Log.i("mDetailContainer.getWidth()-------------------->", mDetailContainer.getWidth() + "");
//                Log.i("mDetailContainer.getHeight()-------------------->", mDetailContainer.getHeight() + "");
//                mDetailContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//            }
//        });
    }

    /**
     * Created by wangclark on 15/6/10.
     */
    public class GoodInfoAdapter extends BaseListAdapter<GoodInfo> {
        public GoodInfoAdapter(Context context, List<GoodInfo> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_goods_info, null);
            TextView tv_key = ViewHolder.get(convertView, R.id.tv_key);
            tv_key.setMaxWidth(screenWidth / 2 - 20);
            TextView tv_value = ViewHolder.get(convertView, R.id.tv_value);
            GoodInfo goodInfo = getList().get(position);
            tv_key.setText(goodInfo.getTitle());
            tv_value.setText(goodInfo.getValue());
            return convertView;
        }
    }

    private void addImageToContainer(final List<String> urls) {
        mImageContainer.removeAllViews();
        LinearLayout.LayoutParams containerParams = (LinearLayout.LayoutParams) mImageContainer.getLayoutParams();
        containerParams.height = containerParams.height + (urls.size()>5?250:urls.size() * 50);
        containerParams.setMargins(0, 0, 0, PixelUtil.dp2px(40, mContext));
        mImageContainer.setLayoutParams(containerParams);
        if (urls != null) {
            for (int i = 0; i < urls.size(); i++) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setImageResource(R.drawable.icon_loading_defalut);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundColor(Color.parseColor("#ffffff"));
                imageView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
                ImageLoader.getInstance().displayImage(urls.get(i), imageView, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        System.out.println("GoodsDetailsChildFragment.onLoadingComplete--------" + bitmap.getWidth() + "-----------" + screenWidth);
                        System.out.println("GoodsDetailsChildFragment.onLoadingComplete-------->" + bitmap.getHeight());
                        ImageView imageView = (ImageView) view;
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        layoutParams.width = screenWidth;
                        layoutParams.height = screenWidth * bitmap.getHeight() / bitmap.getWidth();
                        ViewGroup.LayoutParams containerParams = mImageContainer.getLayoutParams();
                        containerParams.height = containerParams.height + layoutParams.height;
                        mImageContainer.setLayoutParams(containerParams);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });

                mImageContainer.addView(imageView);
            }
        }

    }

}
