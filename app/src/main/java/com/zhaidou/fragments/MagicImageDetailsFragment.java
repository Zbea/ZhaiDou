package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.zhaidou.view.TypeFaceTextView;
import com.zhaidou.view.stretch.PicGallery;
import com.zhaidou.view.stretch.StretchImageView;

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
public class MagicImageDetailsFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private int mId;
    private View view;

    private RelativeLayout headerLine, bottomLine;
    private TypeFaceTextView titleTv;
    private LinearLayout backBtn;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private LinearLayout dotsLine;
    private PicGallery gallery;
    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;

    private static final int UPDATE_HOMELIST = 1;
    private RequestQueue mRequestQueue;
    private List<Article> articleList = new ArrayList<Article>();
    private List<ImageView> dots = new ArrayList<ImageView>();
    private GalleryAdapter adapter;
    private boolean isClick = true;


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_HOMELIST:
                    setDotsView();
                    adapter.setData(articleList);
                    break;
            }
        }
    };

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

    public static MagicImageDetailsFragment newInstance(String param, int string)
    {
        MagicImageDetailsFragment fragment = new MagicImageDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putInt(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicImageDetailsFragment()
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
            view = inflater.inflate(R.layout.fragment_magic_image_details, container, false);
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

        backBtn = (LinearLayout) view.findViewById(R.id.ll_back);
        backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity) getActivity()).popToStack(MagicImageDetailsFragment.this);
            }
        });

        headerLine = (RelativeLayout) view.findViewById(R.id.headerLine);
        bottomLine = (RelativeLayout) view.findViewById(R.id.bottomLine);
        bottomLine.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MagicDesignFragment magicDesignFragment = MagicDesignFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(magicDesignFragment);
            }
        });

        gallery = (PicGallery) view.findViewById(R.id.picgallery);
        gallery.setVerticalFadingEdgeEnabled(false);// 取消竖直渐变边框
        gallery.setHorizontalFadingEdgeEnabled(false);// 取消水平渐变边框
        gallery.setDetector(new GestureDetector(getActivity(), new MySimpleGesture()));
        adapter = new GalleryAdapter(getActivity());
        gallery.setAdapter(adapter);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (isClick)
                {
                    disappearView();
                } else
                {
                    StretchImageView imageView = (StretchImageView) view;
                    imageView.zoomTo(imageView.getMiniZoom());//图片复原
                    showView();
                }
            }
        });
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
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
            public void onNothingSelected(AdapterView<?> parent)
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

    /**
     * 点击消失
     */
    private void disappearView()
    {
        Animation bottomAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bottom_down);
        bottomLine.startAnimation(bottomAnimation);
        bottomLine.setVisibility(View.GONE);
        Animation topAnimation = AnimationUtils.loadAnimation(mContext, R.anim.top_up);
        headerLine.startAnimation(topAnimation);
        headerLine.setVisibility(View.GONE);
        isClick = false;

    }

    /**
     * 点击显示
     */
    private void showView()
    {
        Animation translateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bottom_up);
        bottomLine.startAnimation(translateAnimation);
        bottomLine.setVisibility(View.VISIBLE);
        Animation topAnimation = AnimationUtils.loadAnimation(mContext, R.anim.top_dowm);
        headerLine.startAnimation(topAnimation);
        headerLine.setVisibility(View.VISIBLE);
        isClick = true;
    }


    private void FetchData()
    {
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.MagicCImageDetailsUrl + mId, new Response.Listener<JSONObject>()
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
                    JSONArray articles = response.optJSONArray("data");
                    if (articles != null)
                    {
                        for (int i = 0; i < articles.length(); i++)
                        {
                            JSONObject article = articles.optJSONObject(i);
                            int id = article.optInt("id");
                            String title = article.optString("title");
                            String img_url = article.optString("mainPic");
                            String articleUrl = article.optString("articleUrl");
                            String date = article.optString("createTime").split(" ")[0];
                            Article item = new Article(id, title, img_url, articleUrl, 0);
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

    private class MySimpleGesture extends GestureDetector.SimpleOnGestureListener
    {
        // 按两下的第二下Touch down时触发
        public boolean onDoubleTap(MotionEvent e)
        {

            View view = gallery.getSelectedView();
            if (view instanceof StretchImageView)
            {
                StretchImageView imageView = (StretchImageView) view;
                if (imageView.getScale() > imageView.getMiniZoom())
                {
                    imageView.zoomTo(imageView.getMiniZoom());
                } else
                {
                    imageView.zoomTo(imageView.getMaxZoom());
                }

            } else
            {
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            return true;
        }
    }


    public class GalleryAdapter extends BaseAdapter
    {

        private Context context;

        private ArrayList<StretchImageView> imageViews = new ArrayList<StretchImageView>();
        private List<Article> mItems;

        public void setData(List<Article> data)
        {
            this.mItems = data;
            notifyDataSetChanged();
        }

        public GalleryAdapter(Context context)
        {
            this.context = context;
        }

        @Override
        public int getCount()
        {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public Object getItem(int position)
        {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            StretchImageView view = new StretchImageView(context);
            view.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT,
                    Gallery.LayoutParams.MATCH_PARENT));
            Article item = mItems.get(position);
            if (item != null)
            {
                ToolUtils.setImageCacheUrl(item.getImg_url(), view);
                if (!this.imageViews.contains(view))
                {
                    imageViews.add(view);
                }
            }
            return view;
        }


    }


}
