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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.HomeArticleAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Article;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 软装图例
 */
public class MagicClassicCaseFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";
    private static final String ARG_FLAGS = "flags";

    private String mParam;
    private String mString;
    private int mFlags;//1方案详情2软装方案详情
    private View view;

    private int currentPage = 1;
    private int pageSize;
    private int pageCount;

    private Dialog mDialog;
    private Context mContext;
    private PullToRefreshListView listView;
    private TextView titleTv,tv_nullContent;

    private static final int UPDATE_HOMELIST = 1;
    private List<Article> articleList = new ArrayList<Article>();
    private HomeArticleAdapter mHomeAdapter;


    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_HOMELIST:
                    tv_nullContent.setVisibility(articleList.size()>0?View.GONE:View.VISIBLE);
                    mHomeAdapter.setList(articleList);
                    mHomeAdapter.notifyDataSetChanged();
                    if (pageCount > articleList.size())
                    {
                        listView.setMode(PullToRefreshBase.Mode.BOTH);
                    } else
                    {
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
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
            currentPage=1;
            articleList.clear();
            FetchData();
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            currentPage++;
            FetchData();
        }
    };


    public static MagicClassicCaseFragment newInstance(String param, String string,int flags)
    {
        MagicClassicCaseFragment fragment = new MagicClassicCaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        args.putInt(ARG_FLAGS, flags);
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
            mFlags=getArguments().getInt(ARG_FLAGS);
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
        titleTv = (TypeFaceTextView) view.findViewById(R.id.title_tv);
        titleTv.setText(mFlags==1?"改造案例":"软装方案");

        tv_nullContent= (TypeFaceTextView) view.findViewById(R.id.tv_nullContent);
        tv_nullContent.setText(mFlags==1?"暂无改造案例":"暂无软装方案");

        listView=(PullToRefreshListView)view.findViewById(R.id.lv_special_list);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(onRefreshListener);
        mHomeAdapter = new HomeArticleAdapter(mContext,articleList,mFlags);
        listView.setAdapter(mHomeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                HomeArticleGoodsDetailsFragment homeArticleGoodsDetailsFragment = HomeArticleGoodsDetailsFragment.newInstance("", "" + articleList.get(position-1).getId(),mFlags);
                ((BaseActivity) mContext).navigationToFragment(homeArticleGoodsDetailsFragment);
                homeArticleGoodsDetailsFragment.setOnCommentListener(new HomeArticleGoodsDetailsFragment.OnCommentListener()
                {
                    @Override
                    public void setComment(int num)
                    {
                        if (num != 0)
                        {
                            articleList.get(position).setReviews(num);
                            mHomeAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
        mHomeAdapter.setOnInViewClickListener(R.id.title,new BaseListAdapter.onInternalClickListener()
        {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values)
            {
//                ClipboardManager clipboardManager= (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clipData=ClipData.newPlainText("text",articleList.get(position).getTitle());
//                clipboardManager.setPrimaryClip(clipData);
//                ToolUtils.setToast(mContext,"复制成功");
            }
        });

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
     * 加载列表数据
     */
    private void FetchData()
    {
        String url=null;
        if (mFlags==1)
        {
            url = ZhaiDou.HomeArticleGoodsUrl + currentPage;
        }
        else
        {
            String userId = SharedPreferencesUtil.getData(mContext, "userId", -1) + "";
            url = ZhaiDou.HomeSofeListUrl + "&userId=" + userId + "&pageNo=" + currentPage;
        }
        ToolUtils.setLog(url);
        ZhaiDouRequest jr = new ZhaiDouRequest(url ,new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                listView.onRefreshComplete();
                if (response == null)
                {
                    return;
                }
                ToolUtils.setLog(response.toString());
                int code = response.optInt("code");
                if (code == 500)
                {
                    listView.setMode(PullToRefreshBase.Mode.BOTH);
                    return;
                }
                JSONObject jsonObject = response.optJSONObject("data");
                if (jsonObject != null)
                {
                    pageCount = jsonObject.optInt("totalCount");
                    pageSize = jsonObject.optInt("pageSize");
                    JSONArray jsonArray = jsonObject.optJSONArray(mFlags==1?"freeClassicsCasePOs":"designerListPOs");

                    if (jsonArray != null)
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject obj = jsonArray.optJSONObject(i);
                            int id = obj.optInt("id");
                            String title = obj.optString("caseName");
                            String info = obj.optString("mainDesc");
                            String imageUrl = obj.optString("mainPic");
                            int num = obj.optInt("commentCount");
                            Article article=new Article(id,title,imageUrl,"",num,info);

                            articleList.add(article);
                        }
                    Message message = new Message();
                    message.what = UPDATE_HOMELIST;
                    handler.sendMessage(message);
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                listView.onRefreshComplete();
                if (mDialog != null)
                    mDialog.dismiss();
                if (articleList.size() != 0)
                {
                    currentPage--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
            }
        });
        ZDApplication.newRequestQueue().add(jr);
    }



    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_magic_class_case)); //统计页面
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_magic_class_case));
    }


}
