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

import java.util.ArrayList;
import java.util.List;

public class PhotoViewActivity extends BaseActivity {

    private String[] images;

    List<ImageView> imageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
        findViewById(R.id.title_tv).setVisibility(View.GONE);
        findViewById(R.id.rl_back).setBackgroundColor(Color.parseColor("#000000"));
        CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        images = getIntent().getStringArrayExtra("images");
        imageViews = new ArrayList<ImageView>();
        for (int i = 0; i < images.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setId(i);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            String image = images[i];
            ToolUtils.setImageCacheUrl(image, imageView);
            imageViews.add(imageView);
        }
        mViewPager.setAdapter(new PhotoViewAdapter());
        mIndicator.setViewPager(mViewPager);
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
            return images.length;
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
            container.addView(imageViews.get(position),0);
            return imageViews.get(position);
        }
    }
}
