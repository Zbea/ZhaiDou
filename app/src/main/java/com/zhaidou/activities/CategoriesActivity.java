package com.zhaidou.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.zhaidou.ZhaiDou;
import com.zhaidou.R;

public class CategoriesActivity extends Activity {

    private WebView webView;

    private long lastClickTime = 0L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
    }



    public void onClick_Event(View view) {
        long thisClickTime = SystemClock.elapsedRealtime();
        if ((thisClickTime - lastClickTime) < 1000) {
            return;
        }

        lastClickTime = thisClickTime;
        String tag = (String) view.getTag();

        String targetUrl = String.format(ZhaiDou.TAG_BASE_URL, tag);
        Log.v("Verbose", "------------->Target Url: " + targetUrl);

        Intent tagsIntent = new Intent(this, HomeActivity.class);
        tagsIntent.putExtra("targetUrl", targetUrl);
        tagsIntent.putExtra("type", ZhaiDou.ListType.TAG);

        startActivity(tagsIntent);

        Log.v("Verbose", "View clicked with tag: " + tag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
