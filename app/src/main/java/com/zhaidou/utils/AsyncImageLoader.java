package com.zhaidou.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AsyncImageLoader {
	private Map<String, SoftReference<Drawable>> imageCache;
	private JSONObject object;

	public AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}
	
	public Drawable loadDrawable(final String imageUrl, final ImageLoadCallback callback) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		
		final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    object.put("coverImage", (Drawable) msg.obj);
                    callback.imageLoaded((Drawable) msg.obj, imageUrl);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        };
		
		new Thread() {
			@Override
			public void run() {
				URL url;
				InputStream in = null;
                try {
                    URI uri = new URI(imageUrl);
                    url = new URL(uri.toASCIIString());
                    in = (InputStream) url.getContent();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException ex1) {
                    ex1.printStackTrace();
                }

                Drawable drawable = Drawable.createFromStream(in, "src");
				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				Message msg = handler.obtainMessage(0, drawable);
				handler.sendMessage(msg);
			}
		}.start();
		return null;
	}
	
	public interface ImageLoadCallback {
		public void imageLoaded(Drawable drawable, String imageUrl);
	}

	public JSONObject getObject() {
		return object;
	}

	public void setObject(JSONObject object) {
		this.object = object;
	}
}
