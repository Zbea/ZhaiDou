package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
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
import com.zhaidou.view.CustomCircleView;
import com.zhaidou.view.RoundImageView;
import com.zhaidou.view.TypeFaceTextView;

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
public class MagicImageCalssFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private int mId;
    private View view;

    private TypeFaceTextView titleTv;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private PullToRefreshScrollView scrollView;
    private LinearLayout fallLine1, fallLine2;
    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST = 1;
    private RequestQueue mRequestQueue;
    private List<ImageItem> imageItems = new ArrayList<ImageItem>();

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_HOMELIST:
                    if (pageCount > imageItems.size())
                    {
                        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
                    }
                    else
                    {
                        scrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    fallLine1.removeAllViews();
                    fallLine2.removeAllViews();
                    int j = 0;
                    if (imageItems!=null & imageItems.size()>0)
                    for (int i = 0; i < imageItems.size(); i++)
                    {
                        addView(imageItems.get(i), j, i);
                        j++;
                        if (j > 1)
                        {
                            j = 0;
                        }
                    }
                    break;
            }
        }
    };

    private void addView(ImageItem article, final int j, final int i)
    {
        View convertView=mHashMap.get(i);
        if (convertView==null)
        {
            convertView = mInflater.inflate(R.layout.item_magic_image_class, null);
            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    MagicImageDetailsFragment magicImageDetailsFragment = MagicImageDetailsFragment.newInstance(imageItems.get(i).name, imageItems.get(i).id);
                    ((MainActivity) getActivity()).navigationToFragment(magicImageDetailsFragment);
                }
            });
            TextView title = (TextView) convertView.findViewById(R.id.titleTv);
            RoundImageView cover = (RoundImageView) convertView.findViewById(R.id.imageIv);
            cover.setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams layoutParams;
            if (article.imageHeight!=0&&article.imageWidth!=0)
            {
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((screenWidth-75)/2)*article.imageHeight/article.imageWidth);
            }
            else
            {
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200);
            }
            if (i > 1)
            {
                layoutParams.topMargin = 25;
            }
            cover.setLayoutParams(layoutParams);
            title.setText(article.name);
            ToolUtils.setImageCacheUrl(article.imageUrl, cover, R.drawable.icon_loading_defalut);
            cover.setRadius(13);
            mHashMap.put(i,convertView);
        }
        if (j == 0)
        {
            fallLine1.addView(convertView);
        } else if (j == 1)
        {
            fallLine2.addView(convertView);
        }

    }

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2()
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

    public static MagicImageCalssFragment newInstance(String param, int string)
    {
        MagicImageCalssFragment fragment = new MagicImageCalssFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putInt(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicImageCalssFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam = getArguments().getString(ARG_PARAM);
            mId = getArguments().getInt(ARG_STRING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_magic_image_case_class, container, false);
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

        titleTv = (TypeFaceTextView) view.findViewById(R.id.tv_title);
        titleTv.setText(mParam);

        scrollView = (PullToRefreshScrollView) view.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
//        listView=(StaggeredGridView)view.findViewById(R.id.grid);
//        mHomeAdapter = new HomeAdapter(mContext,imageItems);
//        listView.setAdapter(mHomeAdapter);

        fallLine1 = (LinearLayout) view.findViewById(R.id.fallLine1);
        fallLine2 = (LinearLayout) view.findViewById(R.id.fallLine2);

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

    private void FetchData()
    {
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.MagicImageClassUrl +mId+"&pageSize=10&pageNo="+currentPage, new Response.Listener<JSONObject>()
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
                    JSONObject dataObject = response.optJSONObject("data");
                    if (dataObject != null)
                    {
                        pageSize = dataObject.optInt("pageSize");
                        pageCount = dataObject.optInt("totalCount");
                        JSONArray articles = dataObject.optJSONArray("softDecorationCasePOs");
                        if (articles != null)
                        {
                            for (int i = 0; i < articles.length(); i++)
                            {
                                JSONObject article = articles.optJSONObject(i);
                                int id = article.optInt("id");
                                String title = article.optString("caseName");
                                String img_url = article.optString("mainPic");
                                String enName = article.optString("style");
                                String date = article.optString("createTime").split(" ")[0];
                                int imageWidth = article.optInt("imageWidth");
                                int imageHeight = article.optInt("imageHeight");
                                ImageItem item = new ImageItem();
                                item.name=title;
                                item.id=id;
                                item.englishName=enName;
                                item.time=date;
                                item.imageUrl=img_url;
                                item.imageWidth=imageWidth;
                                item.imageHeight=imageHeight;
                                imageItems.add(item);
                            }
                            handler.sendEmptyMessage(UPDATE_HOMELIST);
                        }
                    } else
                    {
                        if (currentPage > 1)
                            currentPage--;
                        ToolUtils.setToast(mContext, R.string.loading_fail_txt);
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
                convertView = mInflater.inflate(R.layout.item_magic_image_class, null);
            TextView title = ViewHolder.get(convertView, R.id.titleTv);
            CustomCircleView cover = ViewHolder.get(convertView, R.id.imageIv);

            ImageItem article = getList().get(position);
            title.setText(article.name);
            DisplayImageOptions options = new DisplayImageOptions.Builder()
//                    .displayer(new RoundedBitmapDisplayer(13))//设置圆角半径
                    .showImageOnLoading(R.drawable.icon_loading_item)
                    .showImageForEmptyUri(R.drawable.icon_loading_item)
                    .showImageOnFail(R.drawable.icon_loading_item)
                    .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                    .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                    .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .build();

            ImageLoader.getInstance().displayImage(article.imageUrl, cover, options, new ImageLoadingListener()
            {
                @Override
                public void onLoadingStarted(String s, View view)
                {
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason)
                {
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap)
                {
                    CustomCircleView imageView = (CustomCircleView) view;
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - 100) / 2, (bitmap.getHeight() * (screenWidth - 100) / 2) / bitmap.getWidth()));
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onLoadingCancelled(String s, View view)
                {
                }
            });

            mHashMap.put(position, convertView);
            return convertView;
        }
    }

}
