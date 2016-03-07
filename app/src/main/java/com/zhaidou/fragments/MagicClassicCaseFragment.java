package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;

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
public class MagicClassicCaseFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private String mString;
    private View view;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;

    private ViewPager viewPager;
    private LinearLayout dotsLine;

    private static final int UPDATE_HOMELIST = 1;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();
    private List<ImageView> dots = new ArrayList<ImageView>();
    private List<View> views = new ArrayList<View>();
    private ItemsAdapter itemsAdapter;


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_HOMELIST:

                    setAdapterView();
                    setDotsView();

                    break;
            }
        }
    };


    public static MagicClassicCaseFragment newInstance(String param, String string)
    {
        MagicClassicCaseFragment fragment = new MagicClassicCaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicClassicCaseFragment()
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
            view = inflater.inflate(R.layout.fragment_magic_classic_case, container, false);
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
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        itemsAdapter = new ItemsAdapter();
        viewPager.setAdapter(itemsAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                for (int j = 0; j < dots.size(); j++)
                {
                    if (j == position)
                    {
                        dots.get(j).setBackgroundResource(R.drawable.home_tips_foucs_icon);
                    } else
                    {
                        dots.get(j).setBackgroundResource(R.drawable.home_tips_icon);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {
            }
        });
        dotsLine = (LinearLayout) view.findViewById(R.id.dotsLine);

        mRequestQueue = Volley.newRequestQueue(mContext);

        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }


    }


    private void setAdapterView()
    {
        views.clear();
        for (int i = 0; i < articleList.size(); i++)
        {
            final int position = i;
            View contentView = LayoutInflater.from(mContext).inflate(R.layout.item_magic_classic_case, null);

            ImageView imageView = (ImageView) contentView.findViewById(R.id.imageIv);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (screenWidth - 75) * 3 / 4);
            imageView.setLayoutParams(param);
            TextView titleTv = (TextView) contentView.findViewById(R.id.titleTv);
            TextView infoTv = (TextView) contentView.findViewById(R.id.infoTv);
            LinearLayout detailsTv = (LinearLayout) contentView.findViewById(R.id.detailsTv);
            detailsTv.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HomeDesignCaseFragment homeDesignCaseFragment = HomeDesignCaseFragment.newInstance(articleList.get(position).getTitle(), articleList.get(position).getId() + "");
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(homeDesignCaseFragment);
                }
            });

            ToolUtils.setImageCacheRoundUrl(articleList.get(i).getImg_url(), imageView, 16, R.drawable.icon_loading_defalut);

            titleTv.setText(articleList.get(i).getTitle());

            infoTv.setText(articleList.get(i).getIs_new());

            views.add(contentView);

        }

        itemsAdapter.notifyDataSetChanged();
    }


    private void setDotsView()
    {
        dotsLine.removeAllViews();
        for (int i = 0; i < articleList.size(); i++)
        {
            ImageView dot_iv = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0)
            {
                params.leftMargin = 0;
            } else
            {
                params.leftMargin = 20;
            }
            dot_iv.setLayoutParams(params);
            dots.add(dot_iv);
            if (i == 0)
            {
                dots.get(i).setBackgroundResource(R.drawable.home_tips_foucs_icon);
            } else

            {
                dots.get(i).setBackgroundResource(R.drawable.home_tips_icon);
            }
            dotsLine.addView(dot_iv);
        }
    }

    private void FetchData()
    {
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.MagicClassicCaseUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                {
                    mDialog.dismiss();
                }
                if (currentPage == 1)
                    articleList.clear();
                if (response != null)
                {
                    ToolUtils.setLog(response.toString());
                    int code = response.optInt("code");
                    JSONObject dataObject = response.optJSONObject("data");
                    if (dataObject == null)
                    {
                        return;
                    }
                    pageSize = dataObject.optInt("pageSize");
                    pageCount = dataObject.optInt("totalCount");
                    JSONArray articles = dataObject.optJSONArray("freeClassicsCasePOs");
                    if (articles != null)
                    {
                        for (int i = 0; i < articles.length(); i++)
                        {
                            JSONObject article = articles.optJSONObject(i);
                            int id = article.optInt("id");
                            String title = article.optString("caseName");
                            String img_url = article.optString("mainPic");
                            String info = article.optString("caseDesc");
                            String date = article.optString("updateTime").split(" ")[0];
                            Article item = new Article(id, title, img_url, info, 0);
                            item.setDate(date);
                            articleList.add(item);
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


    /**
     * 单哥适配器
     */
    public class ItemsAdapter extends PagerAdapter
    {
        @Override
        public int getCount()
        {
            return views.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(views.get(position), 0);
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView(views.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object o)
        {
            return view == o;
        }
    }


}
