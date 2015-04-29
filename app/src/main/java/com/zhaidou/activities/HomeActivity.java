package com.zhaidou.activities;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.fragments.ElementListFragment;
import com.zhaidou.utils.AsyncImageLoader;
import com.zhaidou.utils.HtmlFetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity implements AbsListView.OnScrollListener {

    private ProgressDialog loading;
    private ListView listView;

    /* pagination */
    private String targetUrl;
    private int currentPage;
    private boolean loadedAll;

    /* Data Definition*/
    List<JSONObject> listItem;

    /* Adapter */
    private HomeItemsAdapter homeItemsAdapter;

    private int lastVisibleIndex;

    private ZhaiDou.ListType listType;

    /* Log cat */
    public static final String ERROR_CAT = "ERROR";
    public static final String DEBUG_CAT = "DEBUG";

    private final int LOADED = 1;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == LOADED) {
                loading.dismiss();
                homeItemsAdapter.notifyDataSetChanged();
            }
        }
    };

    private AdapterView.OnItemClickListener itemSelectListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {

                JSONObject item = listItem.get(i);
                Intent detailIntent = new Intent(HomeActivity.this, ItemDetailActivity.class);
                detailIntent.putExtra("id", item.get("id").toString());
                startActivity(detailIntent);

//                ElementListFragment detailFragment = new ElementListFragment();
//                getFragmentManager().beginTransaction().replace(R.id.home_layout, detailFragment).commit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        listView = (ListView) findViewById(R.id.homeItemList);

        String url = getIntent().getStringExtra("targetUrl");
        String type = getIntent().getStringExtra("type");

        if (url == null) {
            this.targetUrl = ZhaiDou.HOME_PAGE_URL;
        } else {
            this.targetUrl = url;
        }

        /* TODO 渣，要改 */
        if (type != null) {
            if (type.equals("1")) {
                listType = ZhaiDou.ListType.HOME;
                targetUrl = targetUrl + "?page={0}";
            } else if (type.equals("2")) {
                listType = ZhaiDou.ListType.TAG;
                targetUrl = targetUrl + "&page={0}";
            }
        } else {
            listType = ZhaiDou.ListType.HOME;
            targetUrl = targetUrl + "&page={0}";
        }
        currentPage = 1;

        loadedAll = false;

        listItem = new ArrayList<JSONObject>();
        homeItemsAdapter = new HomeItemsAdapter(this);
        listView.setAdapter(homeItemsAdapter);
        listView.setOnItemClickListener(itemSelectListener);
        listView.setOnScrollListener(this);
        loadMoreData();
        loading = ProgressDialog.show(this, "", "正在努力加载中...", true);
    }

    private void loadMoreData() {
        new Thread() {
            public void run() {
                if (loadedAll) {
                    loading.dismiss();
                    return;
                }
                try {
                    String requestUrl = MessageFormat.format(targetUrl, currentPage);
                    Log.d(DEBUG_CAT, "-------> 加载url: " + requestUrl);
                    URL url = new URL(requestUrl);
                    String jsonContent = HtmlFetcher.fetch(url);
                    try {
                        JSONObject root = new JSONObject(jsonContent);
                        JSONArray items = root.getJSONArray("posts");
                        for (int i = 0; i < items.length(); i++) {
                            listItem.add(items.getJSONObject(i));
                        }
                        Message msg = new Message();
                        msg.what = LOADED;
                        handler.sendMessage(msg);
                        currentPage++;
                        int count = Integer.valueOf(root.get("count").toString());
                        int pages = Integer.valueOf(root.get("pages").toString());
                        if (listItem.size() >= count*pages) {
                            loadedAll = true;
                        }
                    } catch (Exception ex) {
                        Log.e("Debug Info", ex.getMessage());
                    }
                } catch (Exception ex) {
                    Log.e(ERROR_CAT, "不能加载数据: " + ex);
                }
            }
        }.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
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

    @Override
    public void onScrollStateChanged(AbsListView absListView, int state) {
        Log.d(DEBUG_CAT, "--------------> lastVisibleIndex: " + lastVisibleIndex);
        Log.d(DEBUG_CAT, "--------------> homeItemsAdapter.getCount: " +  homeItemsAdapter.getCount());
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastVisibleIndex == homeItemsAdapter.getCount()) {
            loading.show();
            loadMoreData();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastVisibleIndex = firstVisibleItem + visibleItemCount;
        Log.d(DEBUG_CAT, "滚动到这里了");
    }

    public final class ViewHolder {
        public TextView title;
        public TextView views;
        public ImageView cover;
    }

    public class HomeItemsAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public HomeItemsAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        // TODO
        public HomeItemsAdapter(List<JSONObject> data, Context context) {

        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public Object getItem(int position) {
            return listItem.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.home_item_list, null);

                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.views = (TextView) convertView.findViewById(R.id.views);
                holder.cover = (ImageView) convertView.findViewById(R.id.cover);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            JSONObject item = listItem.get(i);

            try {
                String coverUrl = item.get("thumbnail").toString();
                ImageView coverView = holder.cover;
                coverView.setTag(coverUrl);

                if (!listItem.get(i).has("coverImage")) {
                    AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
                    asyncImageLoader.setObject(item);
				    asyncImageLoader.loadDrawable(coverUrl, new AsyncImageLoader.ImageLoadCallback() {

                        @Override
                        public void imageLoaded(Drawable drawable, String imageUrl) {
                            ImageView imageViewByTag = (ImageView)listView.findViewWithTag(imageUrl);
                            if (imageViewByTag != null) {
                                imageViewByTag.setImageDrawable(drawable);
                                notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    coverView.setImageDrawable((Drawable)listItem.get(i).get("coverImage"));
                }

                holder.title.setText(item.get("title").toString());
                JSONObject customFields = item.getJSONObject("custom_fields");
                holder.views.setText(customFields.getJSONArray("views").get(0).toString());
            } catch (JSONException ex) {
                Log.e(ERROR_CAT, "Json解析错误: " + ex.getMessage());
            }
            return convertView;
        }
    }
}

