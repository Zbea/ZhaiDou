package com.zhaidou;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.zhaidou.utils.ToolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roy on 15/8/12.
 */
public class WelcomeGuidancePage extends Activity
{
    private ViewPager mViewPager;
    private List<ImageView> images=new ArrayList<ImageView>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.welcome_guidance_page);

        initView();

    }
    /**
     * 初始化
     */
    private void initView()
    {
        mViewPager=(ViewPager)findViewById(R.id.welcomeView);

        String imageUri = "drawable://" + R.drawable.icon_welcome_guidance1;
        ImageView img=new ImageView(this);
        ToolUtils.setImagePreventMemoryLeaksUrl(imageUri, img);
        images.add(img);

        String imageUri1 = "drawable://" + R.drawable.icon_welcome_guidance2;
        ImageView img1=new ImageView(this);
        ToolUtils.setImagePreventMemoryLeaksUrl(imageUri1, img1);
        images.add(img1);

        String imageUri2 = "drawable://" + R.drawable.icon_welcome_guidance3;
        ImageView img2=new ImageView(this);
        ToolUtils.setImagePreventMemoryLeaksUrl(imageUri2, img2);

        img2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent(WelcomeGuidancePage.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_into_the, R.anim.enter_out_the);
                finish();
            }
        });
        images.add(img2);

        MyAdapter adapter=new MyAdapter();
        mViewPager.setAdapter(adapter);

    }

    class MyAdapter extends PagerAdapter
    {
        @Override
        public int getCount()
        {
            return images.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView(images.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(images.get(position));
            return images.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object o)
        {
            return view==o;
        }

    }

    /**
     * 让返回键失效
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
