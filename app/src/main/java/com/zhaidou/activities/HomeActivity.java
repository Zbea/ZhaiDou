package com.zhaidou.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.zhaidou.ZhaiDou;
import com.zhaidou.utils.AsyncImageLoader;
import com.zhaidou.utils.HtmlFetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.zhaidou.R;
import com.zhaidou.utils.ImageDownloader;
import com.zhaidou.utils.NetworkUtils;

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
    private ImageAdapter homeItemsAdapter;

    private int lastVisibleIndex;

    private ZhaiDou.ListType listType;

    /* Log cat */
    public static final String ERROR_CAT = "ERROR";
    public static final String DEBUG_CAT = "DEBUG";

    private final int LOADED = 1;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == LOADED) {
                if (loading.isShowing()) {
                    loading.dismiss();
                }
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
                detailIntent.putExtra("title", item.get("title").toString());
                detailIntent.putExtra("cover_url", item.get("thumbnail").toString());
                detailIntent.putExtra("url", item.get("url").toString());
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
        homeItemsAdapter = new ImageAdapter(this);
        listView.setAdapter(homeItemsAdapter);
        listView.setOnItemClickListener(itemSelectListener);
        listView.setOnScrollListener(this);

        if (NetworkUtils.isNetworkAvailable(this) == false) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("警告");
            alertDialog.setMessage("您还没有连接互联网");
            alertDialog.setPositiveButton("半闭", null);
            alertDialog.show();
            return;
        }


        loadMoreData();
        loading = ProgressDialog.show(this, "", "正在努力加载中...", true);
        setTitle("");

        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View customActionBar = LayoutInflater.from(this).inflate(R.layout.actionbar_with_backbutton, null);
            ImageView backView = (ImageView) customActionBar.findViewById(R.id.actionbar_back_button);
            backView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            getActionBar().setCustomView(customActionBar);
        }
    }

    private void loadMoreData() {
        new Thread() {
            public void run() {
                if (loadedAll) {
//                    loading.dismiss();
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
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastVisibleIndex == homeItemsAdapter.getCount()) {
//            loading.show();
            loadMoreData();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastVisibleIndex = firstVisibleItem + visibleItemCount;
    }

    public final class ViewHolder {
        public TextView title;
        public TextView views;
        public ImageView cover;
    }


    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private final ImageDownloader imageDownloader = new ImageDownloader();

        public ImageAdapter(Context context) {
            imageDownloader.setMode(ImageDownloader.Mode.CORRECT);
            this.inflater = LayoutInflater.from(context);
        }


        public int getCount() {
            return listItem.size();
        }

        public Object getItem(int position) {
            return listItem.get(position);
        }

        public long getItemId(int position) {
            return listItem.get(position).hashCode();
        }

        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.home_item_list, null);//new ImageView(parent.getContext());
            }

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView articleViews = (TextView) view.findViewById(R.id.views);
            ImageView cover = (ImageView) view.findViewById(R.id.cover);

            final JSONObject item = listItem.get(position);
            try {
                title.setText(item.get("title").toString());
                JSONObject customFields = item.getJSONObject("custom_fields");
                articleViews.setText(customFields.getJSONArray("views").get(0).toString());
                imageDownloader.download(item.get("thumbnail").toString(), cover);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }

        public ImageDownloader getImageDownloader() {
            return imageDownloader;
        }
    }



    /* Home items adapter */
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

            final JSONObject item = listItem.get(i);

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
//                                imageViewByTag.setImageDrawable(drawable);
//                                try {
//                                    item.put("coverImage", drawable);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                                imageViewByTag.setImageDrawable(drawable);
                                notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    Drawable drawable = (Drawable) listItem.get(i).get("coverImage");
                    coverView.setImageDrawable((Drawable) listItem.get(i).get("coverImage"));

//                    Draw bitmap = (Bitmap) listItem.get(i).get("coverImage");
//                    bitmap.recycle();
//                    coverView.setImageBitmap((Bitmap) listItem.get(i).get("coverImage"));
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

