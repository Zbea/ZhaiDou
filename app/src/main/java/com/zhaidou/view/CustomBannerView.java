package com.zhaidou.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zhaidou.R;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.ToolUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by roy on 15/10/14.
 */
public class CustomBannerView extends FrameLayout
{
    //自动轮播的时间间隔
    private int TIME_INTERVAL = 5;
    private boolean isAutoPlay;
    private ScheduledExecutorService scheduledExecutorService;
    private List<SwitchImage> imgs;
    private Context mContext;
    private View view;
    private LinearLayout dotsLine;
    private ViewPager viewPager;
    private LinearLayout nullLine;
    private ImageLoopPagerAdapter adapter;
    private int currentPos = 0;
    private List<ImageView> dots = new ArrayList<ImageView>();
    private List<ImageView> banners = new ArrayList<ImageView>();
    private OnBannerClickListener onClickListener;
    private Bitmap norBitmap;
    private Bitmap selectBitmap;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentPos);
        }
    };

    public CustomBannerView(Context context, List<SwitchImage> imgs,boolean isPaly)
    {
        super(context);
        setNorBitmap();
        setSelectBitmap();
        this.imgs = imgs;
        mContext = context;
        if (isPaly)//设置是否自动开始
        {
            if(imgs.size()>1)
            {
                startPlay();
            }
        }
        initView();
    }


    /**
     * 开始轮播图切换
     */
    public void startPlay()
    {
        isAutoPlay=false;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 5, TIME_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 停止轮播图切换
     */
    public void stopPlay()
    {
        if (scheduledExecutorService!=null)
        scheduledExecutorService.shutdown();
        scheduledExecutorService=null;
    }

    /**
     * 执行轮播图切换任务
     *
     */
    private class SlideShowTask implements Runnable
    {
        @Override
        public void run()
        {
            synchronized (viewPager)
            {
                if (!isAutoPlay)
                {
                    currentPos = currentPos + 1;
                    handler.obtainMessage().sendToTarget();
                }
            }
        }

    }

    private void initView()
    {
        if (imgs.size()==0)
        {
            nullLine=new LinearLayout(mContext);
            nullLine.setBackgroundColor(Color.parseColor("#f1f1f1"));
            return;
        }

        banners.clear();
        dots.clear();
        if (view==null)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.custom_viewpager, this, true);
            dotsLine = (LinearLayout) view.findViewById(R.id.customLine);
        }
        dotsLine.removeAllViews();
        for (int i = 0; i < imgs.size(); i++)
        {
            final int postion=i;
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ToolUtils.setImageCacheUrl(imgs.get(i).imageUrl, imageView, R.drawable.icon_loading_item);
            imageView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onClickListener.onClick(postion);
                }
            });
            banners.add(imageView);
        }
        for (int i = 0; i < banners.size(); i++)
        {
            ImageView dot_iv = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0)
            {
                params.leftMargin = 0;
            } else
            {
                params.leftMargin = 15;
            }
            dot_iv.setLayoutParams(params);
            dots.add(dot_iv);
            if (i == 0)
            {
                dots.get(i).setImageBitmap(selectBitmap);
            } else

            {
                dots.get(i).setImageBitmap(norBitmap);
            }
            dotsLine.addView(dot_iv);
        }

        viewPager = (ViewPager)view.findViewById(R.id.customVp);

        if (imgs.size()>1)
        {
            dotsLine.setVisibility(View.VISIBLE);
            adapter=new ImageLoopPagerAdapter();
            viewPager.setOnPageChangeListener(new MyPageChangeListener());
            viewPager.setAdapter(adapter);
        }
        else
        {
            dotsLine.setVisibility(View.GONE);
            viewPager.setAdapter(new ImageAdapter());
        }

    }


    private void setNorBitmap()
    {
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#f1f1f1"));
        norBitmap=Bitmap.createBitmap(50,10, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(norBitmap);
        canvas.drawRect(0, 0, 50,4, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(norBitmap,0,0,paint);


    }

    private void setSelectBitmap()
    {
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#3fcccb"));
        selectBitmap=Bitmap.createBitmap(50,10, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(selectBitmap);
        canvas.drawRect(0, 0, 50,4, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(selectBitmap,0,0,paint);
    }

    /**
     * 左右循环适配器
     */
    private class ImageLoopPagerAdapter extends PagerAdapter
    {
        @Override
        public int getCount()
        {
            return Integer.MAX_VALUE / 2;
        }

        @Override
        public Object instantiateItem(View arg0, int position)
        {
            View view = null;
            if (position % banners.size() < 0)
            {
                view = banners.get(banners.size() + position);
            } else
            {
                view = banners.get(position % banners.size());
            }
            ViewParent vp = view.getParent();
            if (vp != null)
            {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            ((ViewPager) arg0).addView(view);

            return view;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2)
        {
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }
    }

    /**
     * 单哥适配器
     */
    public class ImageAdapter extends PagerAdapter
    {
        @Override
        public int getCount()
        {
            return banners.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(banners.get(position), 0);
            return banners.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView(banners.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object o)
        {
            return view == o;
        }
    }


    private class MyPageChangeListener implements ViewPager.OnPageChangeListener
    {
        @Override
        public void onPageScrolled(int i, float v, int i2)
        {
            viewPager.getParent().requestDisallowInterceptTouchEvent(true);
        }

        @Override
        public void onPageSelected(int i)
        {
            currentPos = i;
            for (int j = 0; j < dots.size(); j++)
            {
                if (j == i % banners.size())
                {
                    dots.get(i % banners.size()).setImageBitmap(selectBitmap);
                } else
                {
                    dots.get(j).setImageBitmap(norBitmap);
                }
            }
        }
        // 其中arg0这个参数
        // 有三种状态（0，1，2）。
        // arg0 == 1的时辰默示正在滑动，
        // arg0 == 2的时辰默示滑动完毕了，
        // arg0 == 0的时辰默示什么都没做。
        public void onPageScrollStateChanged(int arg0)
        {
            if (arg0 == 0)
            {
                isAutoPlay = false;
            }
            if (arg0 == 1)
            {
                isAutoPlay = true;
            }
            if (arg0 == 2)
            {
            }
        }
    }

    /**
     * 销毁ImageView资源，回收内存
     */
    public void destoryBitmaps()
    {

        for (int i = 0; i < banners.size(); i++)
        {
            ImageView imageView = banners.get(i);
            Drawable drawable = imageView.getDrawable();
            if (drawable != null)
            {
                //解除drawable对view的引用
                drawable.setCallback(null);
            }
        }
    }

    /**
     * 设置宽高
     * @param width
     * @param height
     */
    public void setLayoutParams(int width,int height)
    {
        if (imgs.size()==0)
        {
            nullLine.setLayoutParams(new LinearLayout.LayoutParams(width,height));
        }
        else
        {
            viewPager.setLayoutParams(new RelativeLayout.LayoutParams(width,height));
        }
    }


    /**
     * 刷新
     * @param imgs
     */
    public void setImages(List<SwitchImage> imgs)
    {
        this.imgs = imgs;
        isAutoPlay=true;
        destoryBitmaps();
        stopPlay();
        currentPos=0;
        if(imgs.size()>1)
        {
            startPlay();
        }
        initView();
    }

    /**
     * 设置自动滑动间隔时间
     * @param i
     */
    public void setInterval(int i)
    {
        TIME_INTERVAL=i;
    }


    public void setOnBannerClickListener(OnBannerClickListener onClickListener)
    {
        this.onClickListener=onClickListener;
    }

    public interface OnBannerClickListener
    {
        void onClick(int postion);
    }


}
