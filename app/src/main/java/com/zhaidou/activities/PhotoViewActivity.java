package com.zhaidou.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.viewpagerindicator.CirclePageIndicator;
import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoViewActivity extends BaseActivity {

    private String[] images;

    List<ScaleImageView> imageViews;
    private int mCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
        findViewById(R.id.title_tv).setVisibility(View.GONE);
        findViewById(R.id.rl_back).setBackgroundColor(Color.parseColor("#000000"));
        CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        images = getIntent().getStringArrayExtra("images");
        mCurrentPosition = getIntent().getIntExtra("position", 0);
        imageViews = new ArrayList<ScaleImageView>();
        for (int i = 0; i < images.length; i++) {
            ScaleImageView imageView = new ScaleImageView(this);
            imageView.setId(i);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ToolUtils.setImageCacheUrl(images[i], imageView);
            imageViews.add(imageView);
        }
        mViewPager.setAdapter(new PhotoViewAdapter());
        mIndicator.setViewPager(mViewPager, mCurrentPosition);
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private class PhotoViewAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViews.get(position), 0);
            return imageViews.get(position);
        }
    }
}
