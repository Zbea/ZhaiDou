package com.zhaidou.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.zhaidou.R;
import com.zhaidou.ZDApplication;

/**
 * Created by roy on 15/7/22.
 */
public class CustomProgressWebview extends WebView
{
	public ProgressBar progressBar;

	public CustomProgressWebview(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		progressBar.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 5, 0, 0));
		progressBar.setMax(100);  
		addView(progressBar);
		setWebChromeClient(new WebChromeClient());

//        initTypeFace(context);

	}

    public class WebChromeClient extends android.webkit.WebChromeClient
	{
		@Override
		public void onProgressChanged(WebView view, int newProgress)
		{
			if (newProgress == 100)
			{
				progressBar.setVisibility(GONE);
			}
			else
			{
				if (progressBar.getVisibility() == GONE)
                    progressBar.setVisibility(VISIBLE);
				progressBar.setProgress(newProgress);
			}
            progressBar.setVisibility(GONE);
			super.onProgressChanged(view, newProgress);
		}
	}

    public class CustomWebViewClient extends WebViewClient
    {
        @Override
        public void onPageFinished(WebView view, String url)
        {
            progressBar.setVisibility(GONE);
            super.onPageFinished(view, url);
        }
    };

    public void initTypeFace(Context context){
        ZDApplication application =(ZDApplication)context.getApplicationContext();
        Typeface mTypeFace = application.getTypeFace();
        if (mTypeFace!=null)
        {
        }
    }
}
