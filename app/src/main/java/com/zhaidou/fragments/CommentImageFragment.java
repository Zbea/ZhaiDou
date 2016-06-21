package com.zhaidou.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roy on 15/8/28.
 */
public class CommentImageFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private ArrayList<String> images;
    private int mIndex;

    private ViewPager viewPager;
    private LinearLayout viewGroupe;

    private List<ImageView> dots = new ArrayList<ImageView>();
    public List<ImageView> mImageViews = new ArrayList<ImageView>();

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int i, float v, int i2)
        {

        }

        @Override
        public void onPageSelected(int i)
        {
            setImageBackground(i);
        }

        @Override
        public void onPageScrollStateChanged(int i)
        {

        }
    };


    public static CommentImageFragment newInstance(ArrayList<String> images, int index) {
        CommentImageFragment fragment = new CommentImageFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, images);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentImageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            images = (ArrayList<String>)getArguments().getSerializable(DATA);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_comment_image, container, false);
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

        viewPager = (ViewPager) mView.findViewById(R.id.viewPager);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        viewGroupe = (LinearLayout) mView.findViewById(R.id.dotsLine);

        for (int i = 0; i < images.size(); i++)
        {
            ImageView imageView = new ImageView(mContext);
            setAddImage(imageView,images.get(i));
//            ToolUtils.setImageCacheUrl(images.get(i), imageView, R.drawable.icon_loading_goods_details);
            mImageViews.add(imageView);
        }

        if (images != null)
        for (int i = 0; i < images.size(); i++)
        {
            ImageView dot_iv = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0)
            {
                params.leftMargin = 0;
            } else
            {
                params.leftMargin = 20;
            }
            dot_iv.setLayoutParams(params);
            if (i == mIndex)
            {
                dot_iv.setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else
            {
                dot_iv.setBackgroundResource(R.drawable.home_tips_icon);
            }
            viewGroupe.addView(dot_iv);
            dots.add(dot_iv);
        }

        ImageAdapter imageAdapter=new ImageAdapter();
        viewPager.setAdapter(imageAdapter);
        viewPager.setCurrentItem(mIndex);

    }

    private void setAddImage(ImageView imgView,String url)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_loading_item)
                .showImageForEmptyUri(R.drawable.icon_loading_item)
                .showImageOnFail(R.drawable.icon_loading_item)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(url, imgView, options, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String s, View view)
            {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason)
            {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap)
            {
                ImageView imageView1 = (ImageView) view;
                RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(screenWidth, bitmap.getHeight() * screenWidth / bitmap.getWidth());
                layoutParams.topMargin=(screenHeight-bitmap.getHeight() * screenWidth / bitmap.getWidth())/2;
                imageView1.setLayoutParams(layoutParams);
                imageView1.setImageBitmap(bitmap);

            }
            @Override
            public void onLoadingCancelled(String s, View view)
            {
            }
        });
    }

    /**
     * 设置指示器
     */
    private void setImageBackground(int position)
    {
        for (int i = 0; i < dots.size(); i++)
        {
            if (i == position)
            {
                dots.get(position).setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else
            {
                dots.get(i).setBackgroundResource(R.drawable.home_tips_icon);
            }
        }
    }


    public class ImageAdapter extends PagerAdapter
    {

        @Override
        public int getCount()
        {
            return mImageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o)
        {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView(mImageViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(mImageViews.get(position));
            return mImageViews.get(position);
        }
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("评论图片");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("评论图片");
    }

}
