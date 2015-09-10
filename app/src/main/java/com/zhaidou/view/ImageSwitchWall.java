package com.zhaidou.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;
import com.zhaidou.R;
import com.zhaidou.model.SwitchImage;
import com.zhaidou.utils.AsyncImageLoader1;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangclark on 15/6/12.
 */
public class ImageSwitchWall extends RelativeLayout implements
        ViewPager.OnPageChangeListener, View.OnClickListener
{

    public static final int NEXT_DIRECTION = 5;
    public static final int PREV_DIRECTION = 6;
    public static final int WAIT_TIME = 5000;
    private boolean isAuto;
    private Context mContext;
    private TextView mTextView;
    private ScollThread mThread;
    private ISWHandler mHandler;
    private ISWAdapter mAdapter;
    private List<View> mViewList;
    private Resources mResources;
    private int current_direction = 5;
    private ViewPager mViewPager;
    private List<SwitchImage> mDataList;
    private AsyncImageLoader1 mImageLoader;
    private OnItemClickListener mListener;
    private CirclePageIndicator mIndicator;

    private List<SwitchImage> list;

    public ImageSwitchWall(Context context)
    {
        super(context);
        init(context);
    }

    public ImageSwitchWall(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public ImageSwitchWall(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener = listener;
    }

    public void setDatas(List<SwitchImage> list)
    {
        this.list = list;
        mViewList = new ArrayList<View>();
        mDataList = list;
        for (SwitchImage wi : list)
        {
            ImageView iv = new ImageView(mContext);
            ViewPager.LayoutParams params = new ViewPager.LayoutParams();
            params.width = ViewPager.LayoutParams.MATCH_PARENT;
            params.height = ViewPager.LayoutParams.MATCH_PARENT;
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setOnClickListener(this);
            mImageLoader.LoadImage(wi.typeValue, iv);
            mViewList.add(iv);
        }
        mAdapter = new ISWAdapter();
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setCurrentItem(0);
        mIndicator.setOnPageChangeListener(this);
        if (mThread != null)
        {
            mThread.setStop(true);
        }
        if (list.size() > 1)
        {
            mIndicator.setVisibility(View.VISIBLE);
            mTextView.setPadding(0, 0, 0, dp2px(11));
            mTextView.setText(mDataList.get(0).title);
            mThread = new ScollThread();
            mThread.start();
        } else if (list.size() == 1)
        {
            mTextView.setText(mDataList.get(0).title);
            mIndicator.setVisibility(View.GONE);
            mTextView.setPadding(0, 0, 0, 0);
        }
    }

    public List<SwitchImage> getData()
    {
        return list;
    }

    private void init(Context context)
    {
        mContext = context;
        mHandler = new ISWHandler(this);
        mResources = getResources();
        mImageLoader = new AsyncImageLoader1(context);

        // View v = new View(context);
        // RelativeLayout.LayoutParams params0 = new
        // RelativeLayout.LayoutParams(
        // RelativeLayout.LayoutParams.MATCH_PARENT, dp2px(4));
        // params0.addRule(ALIGN_PARENT_TOP);
        // v.setLayoutParams(params0);
        // v.setBackgroundResource(R.drawable.bg_images);

		/*
         * 初始化ViewPager
		 */
        mViewPager = new ViewPager(context);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        // params1.topMargin = dp2px(4);
        mViewPager.setLayoutParams(params1);
		/*
		 * 初始化TextView
		 */
        mTextView = new TextView(context);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(ALIGN_PARENT_BOTTOM);
        mTextView.setLayoutParams(params2);
        mTextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        mTextView.setPadding(0, 0, 0, dp2px(12));
        mTextView.setTextAppearance(context,
                android.R.style.TextAppearance_Small);
        mTextView.setTextColor(mResources.getColor(android.R.color.white));
        mTextView.setBackgroundColor(Color.argb(127, 0, 0, 0));
        mTextView.setTextSize(16);


        // mTextView.setClickable(true);
        // mTextView.setOnClickListener(this);
		/*
		 * 初始化指示器
		 */
        mIndicator = new CirclePageIndicator(context);
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, dp2px(10));
        params3.addRule(ALIGN_PARENT_BOTTOM);
        params3.bottomMargin = dp2px(2);
        mIndicator.setLayoutParams(params3);
        mIndicator.setBackgroundColor(mResources
                .getColor(android.R.color.transparent));
        mIndicator.setRadius(dp2px(4));
//        mIndicator.setPageColor(Color.argb(127, 245, 245, 245));
//        mIndicator.setFillColor(Color.argb(255, 0, 97, 161));
        mIndicator.setPageColor(getResources().getColor(R.color.gray_light));
        mIndicator.setFillColor(getResources().getColor(R.color.white));
        mIndicator.setStrokeWidth(0);

        // addView(v);
        addView(mViewPager);
//        addView(mTextView);
        addView(mIndicator);
    }

    private int dp2px(float dpValue)
    {
        final float scale = mResources.getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }

    @Override
    public void onPageSelected(int arg0)
    {
        if (!isAuto)
        {
            mThread.setStop(true);
            mThread = new ScollThread();
            mThread.start();
        }
        mTextView.setText(mDataList.get(arg0).title);
    }

    @Override
    public void onClick(View v)
    {
        if (mListener != null)
        {
            mListener.onItemClick(this, v, (Integer) v.getTag());
        }
    }

    public interface OnItemClickListener
    {
        public void onItemClick(ViewGroup vg, View v, int position);
    }

    private static class ISWHandler extends Handler
    {

        private WeakReference<ImageSwitchWall> mReference;

        public ISWHandler(ImageSwitchWall wall)
        {
            mReference = new WeakReference<ImageSwitchWall>(wall);
        }

        @Override
        public void handleMessage(Message msg)
        {
            ImageSwitchWall wall = mReference.get();
            if (wall == null)
            {
                return;
            }
            int item = wall.mViewPager.getCurrentItem();
            if (item == wall.mViewList.size() - 1)
            {
                wall.current_direction = PREV_DIRECTION;
            } else if (item == 0)
            {
                wall.current_direction = NEXT_DIRECTION;
            }
            if (wall.current_direction == NEXT_DIRECTION)
            {
                item++;
            } else
            {
                item--;
            }
            wall.isAuto = true;
            wall.mViewPager.setCurrentItem(item);
            wall.isAuto = false;
        }
    }


    private class ISWAdapter extends PagerAdapter
    {

        @Override
        public int getCount()
        {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            View v = mViewList.get(position);
            v.setTag(position);
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);
        }

    }

    @SuppressWarnings("unused")
    private class DownLoadImage extends AsyncTask<Object, Void, Void>
    {

        private ImageView mImageView;
        private String mImageUrl;
        private WeakReference<Bitmap> mReference;

        public DownLoadImage(ImageView imageView, String imageUrl)
        {
            super();
            this.mImageView = imageView;
            this.mImageUrl = imageUrl;
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            mReference = new WeakReference<Bitmap>(getBitmap(mImageUrl));
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            Bitmap b = mReference.get();
            if (b != null && !b.isRecycled())
            {
                mImageView.setImageBitmap(mReference.get());
            }
        }

        private Bitmap getBitmap(String imageUrl)
        {
            try
            {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(1000);
                conn.setRequestMethod("GET");
                conn.connect();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    InputStream is = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, null);
                    if (is != null)
                    {
                        is.close();
                        is = null;
                    }
                    if (conn != null)
                    {
                        conn.disconnect();
                        conn = null;
                    }
                    return bmp;
                }
            } catch (Exception e)
            {

            }
            return null;
        }
    }

    private class ScollThread extends Thread
    {

        private boolean isStop;

        public void setStop(boolean isStop)
        {
            this.isStop = isStop;
            mHandler.removeMessages(0);
        }

        @Override
        public void run()
        {
            super.run();
            try
            {
                do
                {
                    mHandler.sendEmptyMessageDelayed(0, WAIT_TIME);
                    sleep(WAIT_TIME);
                } while (!isStop);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

}