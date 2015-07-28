package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.SearchActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.HtmlFetcher;
import com.zhaidou.utils.ImageDownloader;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.HeaderLayout;
import com.zhaidou.view.XListView;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ElementListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ElementListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ElementListFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView>,
        HeaderLayout.onLeftImageButtonClickListener,
        HeaderLayout.onRightImageButtonClickListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String URL = "targetUrl";
    private static final String TYPE = "type";

    private Dialog loading;
    private PullToRefreshListView listView;
    private ZhaiDou.ListType listType;

    /* pagination */
    private String targetUrl;
    private int currentPage;
    private boolean loadedAll;
    private final int LOADED = 1;
    private AsyncImageLoader1 imageLoader;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    /* Data Definition*/
    List<JSONObject> listItem;
    private static final int STATUS_REFRESH = 0;
    private static final int STATUS_LOAD_MORE = 1;
    private static final int UPDATE_CATEGORY = 2;

    private PopupWindow mPopupWindow = null;
    private LinearLayout ll_poplayout;
    private GridView gv_category;
    private CategoryAdapter mCategoryAdapter;
    private List<String> categoryList;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();
    private AdapterView.OnItemClickListener itemSelectListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            Log.i("onItemClick--->", "onItemClick");
            try
            {
                JSONObject item = listItem.get(i-1);
                Log.i("item---------------->",item.toString());
                Intent detailIntent = new Intent(getActivity(), ItemDetailActivity.class);
                detailIntent.putExtra("id", item.get("id").toString());
                detailIntent.putExtra("title", item.get("title").toString());
                detailIntent.putExtra("cover_url", URLDecoder.decode(item.get("thumbnail").toString()));
                detailIntent.putExtra("from","beauty1");
                detailIntent.putExtra("url", item.get("url").toString());
                detailIntent.putExtra("show_header", false);
                startActivity(detailIntent);

//                ElementListFragment detailFragment = new ElementListFragment();
//                getFragmentManager().beginTransaction().replace(R.id.home_layout, detailFragment).commit();
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };
    /* Log cat */
    public static final String ERROR_CAT = "ERROR";
    public static final String DEBUG_CAT = "DEBUG";

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {

            if (msg.what == LOADED)
            {

                if (loading.isShowing())
                {
                    loading.dismiss();
                }
                homeItemsAdapter.notifyDataSetChanged();
            } else if (msg.what == UPDATE_CATEGORY)
            {
                mCategoryAdapter.setList(categoryList);
            }
            listView.onRefreshComplete();
            homeItemsAdapter.notifyDataSetChanged();
        }
    };

    /* Adapter */
    private ImageAdapter homeItemsAdapter;

    private int lastVisibleIndex;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static ElementListFragment newInstance(String url, String type)
    {
        Log.i("ElementListFragment---->","ElementListFragment");
        ElementListFragment fragment = new ElementListFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public ElementListFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            targetUrl = getArguments().getString(URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.element_list_fragment, container, false);
        listView = (PullToRefreshListView) view.findViewById(R.id.homeItemList);

        String url = getArguments().getString("targetUrl");
        String type = getArguments().getString("type");

        if (url == null)
        {
            this.targetUrl = ZhaiDou.HOME_PAGE_URL;
        } else
        {
            this.targetUrl = url;
        }

        /* TODO 渣，要改 */
        if (type != null)
        {
            if (type.equals(ZhaiDou.ListType.HOME + ""))
            {
                listType = ZhaiDou.ListType.HOME;
                targetUrl = targetUrl + "&page={0}";
            } else if (type.equals(ZhaiDou.ListType.TAG + ""))
            {
                listType = ZhaiDou.ListType.TAG;
                targetUrl = targetUrl + "&page={0}";
            }
        } else
        {
            listType = ZhaiDou.ListType.HOME;
            targetUrl = targetUrl + "&page={0}";
        }
        currentPage = 1;

        loadedAll = false;

        mRequestQueue = Volley.newRequestQueue(getActivity());
        listItem = new ArrayList<JSONObject>();
        homeItemsAdapter = new ImageAdapter(getActivity());
        listView.setAdapter(homeItemsAdapter);
        listView.setOnItemClickListener(itemSelectListener);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(this);

        loadMoreData(STATUS_REFRESH);
        loading = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        setUpPopView();
//        FetchData();
        return view;
    }

    private void setUpPopView()
    {
        mPopupWindow = new PopupWindow(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.item_popupwindows, null);

        ll_poplayout = (LinearLayout) view.findViewById(R.id.ll_popup);

        gv_category = (GridView) view.findViewById(R.id.gv_category);
        categoryList = new ArrayList<String>();
        mCategoryAdapter = new CategoryAdapter(getActivity(), categoryList);
        gv_category.setAdapter(mCategoryAdapter);
        mPopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setContentView(view);
        gv_category.setOnItemClickListener(new GridView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                mPopupWindow.dismiss();
            }
        });

        mCategoryAdapter.setOnInViewClickListener(R.id.tv_category_item, new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void loadMoreData(final int status)
    {
        new Thread()
        {
            public void run()
            {
                if (loadedAll)
                {
                    return;
                }
                try
                {
                    String requestUrl = MessageFormat.format(targetUrl, currentPage);
                    java.net.URL url = new URL(requestUrl);
                    String jsonContent = HtmlFetcher.fetch(url);
                    System.out.print("-------> 加载jsonContent: " + jsonContent);
                    try
                    {
                        JSONObject root = new JSONObject(jsonContent);
                        JSONArray items = root.getJSONArray("posts");
                        Log.i("items", items.length() + "");
                        if (currentPage == 1) listItem.clear();
                        for (int i = 0; i < items.length(); i++)
                        {
                            listItem.add(items.getJSONObject(i));
                        }
                        Message msg = new Message();
                        msg.what = LOADED;
                        msg.arg1 = status;
                        handler.sendMessage(msg);
                        currentPage++;
                        int count = Integer.valueOf(root.get("count").toString());
                        int pages = Integer.valueOf(root.get("pages").toString());
                        if (listItem.size() >= count * pages)
                        {
                            loadedAll = true;
                        }
                    } catch (Exception ex)
                    {
                        Log.e("Debug Info", ex.getMessage());
                    }
                } catch (Exception ex)
                {
                    Log.e(ERROR_CAT, "不能加载数据: " + ex);
                }
            }
        }.start();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class ImageAdapter extends BaseAdapter
    {

        private LayoutInflater inflater;

        private final ImageDownloader imageDownloader = new ImageDownloader();

        public ImageAdapter(Context context)
        {
            imageDownloader.setMode(ImageDownloader.Mode.CORRECT);
            this.inflater = LayoutInflater.from(context);
            ;
        }

        public void clear()
        {
            listItem.clear();
        }

        public int getCount()
        {
            return listItem.size();
        }

        public Object getItem(int position)
        {
            return listItem.get(position);
        }

        public long getItemId(int position)
        {
            return listItem.get(position).hashCode();
        }

        public View getView(int position, View view, ViewGroup parent)
        {
            view = mHashMap.get(position);
            if (view == null)
            {
                view = inflater.inflate(R.layout.home_item_list, null);//new ImageView(parent.getContext());
            }

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView articleViews = (TextView) view.findViewById(R.id.views);
            ImageView cover = (ImageView) view.findViewById(R.id.cover);

            final JSONObject item = listItem.get(position);
            try
            {
                title.setText(item.get("title").toString());
                JSONObject customFields = item.getJSONObject("custom_fields");
                articleViews.setText(customFields.getJSONArray("views").get(0).toString());
//                imageDownloader.download(item.get("thumbnail").toString(), cover);
                Log.i("item.get(\"thumbnail\")----->", item.get("thumbnail").toString());
                ToolUtils.setImageCacheUrl(URLDecoder.decode(item.get("thumbnail").toString(), "utf-8"), cover);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
            mHashMap.put(position, view);
            return view;
        }

        public ImageDownloader getImageDownloader()
        {
            return imageDownloader;
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.header_ib_rightbutton:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.header_ib_leftbutton:
                mPopupWindow.showAtLocation(getView(), Gravity.TOP, 0, 220);
                mPopupWindow.setFocusable(true);
                gv_category.setFocusable(true);
                runOnWorkThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        try
                        {
                            java.net.URL url = new URL(ZhaiDou.INDEX_CATEGORY_FILTER);
                            String json = HtmlFetcher.fetch(url);
                            Log.d(DEBUG_CAT, "-------> INDEX_CATEGORY_FILTER: " + json);
                            JSONObject root = new JSONObject(json);
                            JSONArray categories = root.optJSONArray("article_categories");
                            categoryList.clear();
                            for (int i = 0; i < categories.length(); i++)
                            {
                                JSONObject item = categories.optJSONObject(i);
                                ShowLog(item.optString("name"));
                                categoryList.add(item.optString("name"));
                            }
                            handler.sendEmptyMessage(UPDATE_CATEGORY);
                        } catch (Exception ex)
                        {
                            Log.e("Debug Info", ex.getMessage());
                        }
                    }
                });
                break;
        }
    }

    public class CategoryAdapter extends BaseListAdapter<String>
    {
        public CategoryAdapter(Context context, List<String> list)
        {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.category_item_gv, null);
            TextView tv_item = ViewHolder.get(convertView, R.id.tv_category_item);
            String item = getList().get(position);
            tv_item.setText(item);

            return convertView;
        }
    }

    private void FetchData()
    {

        String url = "http://192.168.1.45/article/api/articles";
        JsonObjectRequest jr = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.i("FetchData", response.toString());
                JSONArray articles = response.optJSONArray("articles");
                for (int i = 0; i < articles.length(); i++)
                {
                    JSONObject article = articles.optJSONObject(i);
                    int id = article.optInt("id");
                    String title = article.optString("title");
                    String img_url = article.optString("img_url");
                    String is_new = article.optString("is_new");
                    int reviews = article.optInt("reviews");
                    Article item = new Article(id, title, img_url, is_new, reviews);
                    articleList.add(item);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.i("FetchData", error.getMessage());
            }
        });
        mRequestQueue.add(jr);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
    {
        currentPage = 1;
//        homeItemsAdapter.clear();
        loadMoreData(STATUS_REFRESH);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
    {
        loadMoreData(STATUS_LOAD_MORE);
    }
}
