package com.zhaidou.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.zhaidou.R;


/**
 * Created by Zbea on 16/1/27.
 */
public class CustomCircleView extends ImageView
{

    private Bitmap mBitmap;
    private int mRadius;//圆角
    private int borderWidth;//边框宽度
    private int borderColor;//边框颜色
    private int mWidth,mHeight;//图像宽高
    private int type;//图片类型
    private static final int Type_Circle=0;
    private static final int Type_Round=1;
    private Paint borderPaint=new Paint();
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private ScaleType mScaleType;

    public CustomCircleView(Context context)
    {
        this(context, null);
    }

    public CustomCircleView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CustomCircleView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        setScaleType(ScaleType.FIT_XY);

        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.CustomCircleView,defStyle,0);
        mRadius=array.getDimensionPixelOffset(R.styleable.CustomCircleView_radiuss, 0);
        type=array.getInt(R.styleable.CustomCircleView_type,0);
        borderWidth=array.getDimensionPixelOffset(R.styleable.CustomCircleView_borderWidth,0);
        borderColor=array.getColor(R.styleable.CustomCircleView_borderColor, Color.WHITE);

        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthSpec=MeasureSpec.getMode(widthMeasureSpec);

        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightSpec=MeasureSpec.getMode(heightMeasureSpec);

        if (widthSpec==MeasureSpec.EXACTLY)
        {
            mWidth=widthSize;
        }
        else
        {
            mWidth=Math.min((mBitmap.getWidth()+getPaddingLeft()+getPaddingRight()),widthSize);
        }

        if (heightSpec==MeasureSpec.EXACTLY)
        {
            mHeight=heightSize;
        }
        else
        {
            mHeight=Math.min((mBitmap.getHeight()+getPaddingTop()+getPaddingBottom()),heightSize);
        }

        setMeasuredDimension(mWidth,mHeight);

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
//        super.onDraw(canvas);
        if (mBitmap==null)
        {
            return;
        }
        borderPaint.setColor(borderColor);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setStyle(Paint.Style.STROKE);

        switch (type)
        {
            case Type_Circle:
                int min=Math.min(mWidth,mHeight);
                //按最小缩放图片
                mBitmap=Bitmap.createScaledBitmap(mBitmap,min,min,false);
                Bitmap circleBitmap=setCircleBitmap(mBitmap,min);
                canvas.drawBitmap(circleBitmap,0,0,null);
                if (borderWidth>0)
                {
                    int borderwidth=min-borderWidth;
                    canvas.drawCircle(min/2,min/2,borderwidth/2,borderPaint);
                }
                break;
            case Type_Round:

                Bitmap roundBitmap=setRoundBitmap(mBitmap);
                canvas.drawBitmap(roundBitmap,0,0,null);
                if (borderWidth>0)
                {
                    int borderwidth=mWidth-borderWidth/2;
                    int borderheight=mHeight-borderWidth/2;
                    canvas.drawRoundRect(new RectF(borderWidth/2,borderWidth/2,borderwidth,borderheight),mRadius,mRadius,borderPaint);
                }
                break;
        }
    }



    /**
     * 设置圆形
     * @param mBitmap
     * @param min
     * @return
     */
    private Bitmap setCircleBitmap(Bitmap mBitmap, int min)
    {
        Bitmap bitmap=null;
        Paint mPaint=new Paint();//设置画笔
        mPaint.setAntiAlias(true);

        bitmap=Bitmap.createBitmap(min,min, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);//画该bitmap 大小的画布
        canvas.drawCircle(min/2,min/2,min/2,mPaint);//画圆

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//设置画笔模式
        canvas.drawBitmap(mBitmap,0,0,mPaint);

        return bitmap;
    }

    /**
     *
     * @param mBitmap
     * @return
     */
    private Bitmap setRoundBitmap(Bitmap mBitmap)
    {
        Bitmap bitmap=null;
        Paint mPaint=new Paint();//设置画笔
        mPaint.setAntiAlias(true);

        bitmap=Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);//画该bitmap 大小的画布
        canvas.drawRoundRect(new RectF(0,0,mWidth,mHeight),mRadius,mRadius,mPaint);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//设置画笔模式
        canvas.drawBitmap(mBitmap,0,0,mPaint);

        return bitmap;
    }


    @Override
    public void setScaleType(ScaleType scaleType)
    {
        if (mScaleType != scaleType)
        {
            mScaleType = scaleType;
            setScaleType(mScaleType);
            switch (scaleType)
            {
                case CENTER:
                    super.setScaleType(ScaleType.CENTER);
                case CENTER_CROP:
                    super.setScaleType(ScaleType.CENTER_CROP);
                case CENTER_INSIDE:
                    super.setScaleType(ScaleType.CENTER_INSIDE);
                case FIT_CENTER:
                    super.setScaleType(ScaleType.FIT_CENTER);
                case FIT_START:
                    super.setScaleType(ScaleType.FIT_START);
                case FIT_END:
                    super.setScaleType(ScaleType.FIT_END);
                case FIT_XY:
                    super.setScaleType(ScaleType.FIT_XY);
                    break;
                default:
                    super.setScaleType(scaleType);

                    break;
            }
        }
    }

    @Override
    public ScaleType getScaleType()
    {
        return mScaleType;
    }




    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        invalidate();
    }


        @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        invalidate();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = getBitmapFromDrawable(getDrawable());
        invalidate();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

}
