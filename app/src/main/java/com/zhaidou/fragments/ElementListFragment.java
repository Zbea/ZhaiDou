package com.zhaidou.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.utils.HtmlFetcher;
import com.zhaidou.utils.ImageDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ElementListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ElementListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ElementListFragment extends Fragment implements AbsListView.OnScrollListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String URL = "targetUrl";
    private static final String TYPE = "type";

    private ProgressDialog loading;
    private ListView listView;
    private ZhaiDou.ListType listType;

    /* pagination */
    private String targetUrl;
    private int currentPage;
    private boolean loadedAll;
    private final int LOADED = 1;
    /* Data Definition*/
    List<JSONObject> listItem;
    private AdapterView.OnItemClickListener itemSelectListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {

                JSONObject item = listItem.get(i);
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
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
    /* Log cat */
    public static final String ERROR_CAT = "ERROR";
    public static final String DEBUG_CAT = "DEBUG";

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

    /* Adapter */
    private ImageAdapter homeItemsAdapter;

    private int lastVisibleIndex;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static ElementListFragment newInstance(String url, String type) {
        ElementListFragment fragment = new ElementListFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }
    public ElementListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetUrl = getArguments().getString(URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.element_list_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.homeItemList);

        String url = getArguments().getString("targetUrl");
        String type = getArguments().getString("type");

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
        homeItemsAdapter = new ImageAdapter(getActivity());
        listView.setAdapter(homeItemsAdapter);
        listView.setOnItemClickListener(itemSelectListener);
        listView.setOnScrollListener(this);
        loadMoreData();
        loading = ProgressDialog.show(getActivity(), "", "正在努力加载中...", true);

        return view;
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
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
                    java.net.URL url = new URL(requestUrl);
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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
}
