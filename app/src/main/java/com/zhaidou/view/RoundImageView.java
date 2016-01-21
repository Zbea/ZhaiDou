package com.zhaidou.view;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by roy on 16/1/21.
 */
public class RoundImageView extends ImageView
{

    private float mRadius=8;

    public RoundImageView(Context context)
    {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        BitmapShader shader;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
        shader = new BitmapShader(bitmapDrawable.getBitmap(),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);//设置映射否则图片显示不全
        RectF rect = new RectF(0.0f, 0.0f, getWidth(), getHeight());
        int width = bitmapDrawable.getBitmap().getWidth();
        int height = bitmapDrawable.getBitmap().getHeight();
        RectF src = new RectF(0.0f, 0.0f, width, height);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(src, rect, Matrix.ScaleToFit.CENTER);
        shader.setLocalMatrix(matrix);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);
    }


    public void setRadius(int radius)
    {
        mRadius=radius;
        invalidate();
    }


}
