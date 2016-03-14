package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.ImageItem;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 软装图例
 */
public class MagicImageCaseFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private String mString;
    private View view;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private PullToRefreshScrollView scrollView;
    private ListViewForScrollView listView;
    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST = 1;
    private RequestQueue mRequestQueue;
    private List<ImageItem> imageItems = new ArrayList<ImageItem>();

    private HomeAdapter mHomeAdapter;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_HOMELIST:
                    mHomeAdapter.setList(imageItems);
                    mHomeAdapter.notifyDataSetChanged();
                    if (pageCount > imageItems.size())
                    {
                        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        scrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    break;
            }
        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {
            currentPage = 1;
            FetchData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            ++currentPage;
            FetchData();
        }
    };

    public static MagicImageCaseFragment newInstance(String param, String string)
    {
        MagicImageCaseFragment fragment = new MagicImageCaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicImageCaseFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam = getArguments().getString(ARG_PARAM);
            mString = getArguments().getString(ARG_STRING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (view == null)
        {
            view =  inflater.inflate(R.layout.fragment_magic_image_case, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null)
        {
            parent.removeView(view);
        }
        return view;
    }

    private void initView()
    {
        scrollView=(PullToRefreshScrollView)view.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView=(ListViewForScrollView)view.findViewById(R.id.lv_special_list);
        mHomeAdapter = new HomeAdapter(mContext,imageItems);
        listView.setAdapter(mHomeAdapter);

        mRequestQueue = Volley.newRequestQueue(mContext);

        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ImageItem imageItem = imageItems.get(position);
                MagicImageCalssFragment magicImageCaseFragment = MagicImageCalssFragment.newInstance(imageItem.name, imageItem.id);
                ((MainActivity) getActivity()).navigationToFragment(magicImageCaseFragment);
            }
        });

    }

    private void FetchData()
    {
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.MagicImageCaseUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                {
                    mDialog.dismiss();
                }
                scrollView.onRefreshComplete();
                if (currentPage == 1)
                    imageItems.clear();
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                    int code = response.optInt("code");
                    JSONArray articles = response.optJSONArray("data");
                    if (articles != null)
                    {
                        for (int i = 0; i < articles.length(); i++)
                        {
                            JSONObject article = articles.optJSONObject(i);
                            int id = article.optInt("id");
                            String title = article.optString("styleName");
                            String img_url = article.optString("mainPic");
                            String enName = article.optString("enName");
                            String date = article.optString("createTime").split(" ")[0];
                            ImageItem item = new ImageItem();
                            item.name=title;
                            item.id=id;
                            item.englishName=enName;
                            item.time=date;
                            item.imageUrl=img_url;
                            imageItems.add(item);
                        }
                        handler.sendEmptyMessage(UPDATE_HOMELIST);
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (mDialog != null)
                {
                    mDialog.dismiss();
                }
                scrollView.onRefreshComplete();
                if (currentPage > 1)
                {
                    currentPage--;
                }
                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(jr);
    }

    public class HomeAdapter extends BaseListAdapter<ImageItem>
    {
        Context context;

        public HomeAdapter(Context context, List<ImageItem> list)
        {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView = mHashMap.get(position);


            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_magic_image_case, null);
            TextView title = ViewHolder.get(convertView, R.id.titleTv);
            TextView english = ViewHolder.get(convertView, R.id.englishTv);
            ImageView cover = ViewHolder.get(convertView, R.id.imageIv);
            cover.setScaleType(ImageView.ScaleType.FIT_XY);
            View space = ViewHolder.get(convertView, R.id.spaceView);
            LinearLayout bottom = ViewHolder.get(convertView, R.id.floatLine);
            cover.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));
            bottom.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 316 / 722));
            if (position==0)
            {
                space.setVisibility(View.GONE);
            }
            else
            {
                space.setVisibility(View.VISIBLE);
            }

            ImageItem imageItem = getList().get(position);
            title.setText(imageItem.name);
            english.setText(imageItem.englishName);
            ToolUtils.setImageCacheRoundUrl(imageItem.imageUrl, cover, 13,R.drawable.icon_loading_item);

            mHashMap.put(position, convertView);
            return convertView;
        }
    }

}
