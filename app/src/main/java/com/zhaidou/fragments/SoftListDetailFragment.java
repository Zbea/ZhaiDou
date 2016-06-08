package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pulltorefresh.PullToRefreshBase;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.Comment;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CircleImageView;
import com.zhaidou.view.CustomProgressWebview;
import com.zhaidou.view.CustomScrollView;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SoftListDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SoftListDetailFragment extends BaseFragment {
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";

    private String mParam;
    private String mString;
    private View view;
    private RelativeLayout barLine;
    private ImageView shareIv, goodsIv, imageIv, commentIv;
    private CustomProgressWebview webview;
    private ListViewForScrollView goodsListView, commentListView;
    private LinearLayout loadingView, nullNetView, nullView, nullDataView;
    private TextView reloadBtn, reloadNetBtn;
    private LinearLayout contactQQ;
    private RelativeLayout detailsTopLine;
    private TextView titleTv, areaTypeTv, areasTv, styleTv, budgetTv, nullGoods, nullComment, subtotalTv, commentNumTv;
    private LinearLayout totalLine, goodsAllBtn, commentAllLine, commentAllBtn;
    private FrameLayout frameLayout;
    private LinearLayout commentLine;

    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private CustomScrollView mScrollView;
    private GoodsAdapter articleShoppingAdapter;

    private Dialog mDialog;
    private DialogUtils mDialogUtil;
    private Context mContext;
    private float alpha = 0;
    private static final int START_ALPHA = 0;
    private static final int END_ALPHA = 255;
    private int fadingHeight = 0;   //当ScrollView滑动到什么位置时渐变消失（根据需要进行调整）
    private final int UPDATE_SHARE_TOAST = 8;

    private RequestQueue mRequestQueue;
    private int page = 1;
    private int pageSize;
    private int pageCount, commentCount = 0;
    private int commentNum;
    private String imageUrl, title, introduce, areaType, areaSize, style, budget, totalPrice;
    private List<CartGoodsItem> items = new ArrayList<CartGoodsItem>();
    private AlphaAnimation alphaAnimation;
    private List<Comment> comments = new ArrayList<Comment>();
    private CommentAdapter commentAdapter;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                titleTv.setText(title);
                areaTypeTv.setText(areaType);
                areasTv.setText(areaSize + "平");
                styleTv.setText(style);
                budgetTv.setText(budget);
                subtotalTv.setText("￥" + ToolUtils.isIntPrice(totalPrice));
                commentNumTv.setVisibility(commentNum > 0 ? View.VISIBLE : View.GONE);

                ToolUtils.setImageCacheUrl(imageUrl, imageIv, R.drawable.icon_loading_item);

                webview.loadData(introduce, "text/html; charset=UTF-8", "UTF-8");
                webview.setWebViewClient(new WebViewClient() {
                                             @Override
                                             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                 view.loadUrl(url);
                                                 return false;
                                             }
                                         }
                );

                loadingView.setVisibility(View.GONE);
                nullGoods.setVisibility(items.size() > 0 ? View.GONE : View.VISIBLE);
                totalLine.setVisibility(items.size() > 0 ? View.VISIBLE : View.GONE);

                articleShoppingAdapter.notifyDataSetChanged();
            } else if (msg.what == 2) {
                commentNumTv.setText(commentCount + "");
                commentNumTv.setVisibility(commentCount > 0 ? View.VISIBLE : View.GONE);
                nullComment.setVisibility(comments.size() > 0 ? View.GONE : View.VISIBLE);
                commentAllLine.setVisibility(comments.size() > 0 ? View.VISIBLE : View.GONE);
                commentAdapter.notifyDataSetChanged();
            } else if (msg.what == UPDATE_SHARE_TOAST) {
                mDialogUtil.dismiss();
                String result = (String) msg.obj;
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }

        }
    };

    private PullToRefreshBase.OnRefreshListener2 onRefreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            items.clear();
            page = 1;
            FetchData();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            page = page + 1;
            FetchData();
        }
    };

    public static SoftListDetailFragment newInstance(String param, String string) {
        SoftListDetailFragment fragment = new SoftListDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        fragment.setArguments(args);
        return fragment;
    }

    public SoftListDetailFragment() {
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            GoodsArticleListFragment goodsArticleListFragment = GoodsArticleListFragment.newInstance(title, mString);
            CommentListFragment commentListFragment = CommentListFragment.newInstance(title, mString);
            commentListFragment.setOnCommentListener(new CommentListFragment.OnCommentListener() {
                @Override
                public void GetComments(List<Comment> comments1, int num) {
                    if (comments != comments1) {
                        comments.clear();
                        comments.addAll(comments1);
                        commentAdapter.notifyDataSetChanged();
                    }
                    if (num != commentCount) {
                        commentCount = num;
                        commentNumTv.setText("(" + commentCount + ")");
                    }
                }
            });
            switch (view.getId()) {
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
                case R.id.rl_qq_contact:
//                    CommentListFragment commentListFragment = CommentListFragment.newInstance("", "");
//                    ((MainActivity) mContext).navigationToFragmentWithAnim(commentListFragment);
                    EaseUtils.startDesignerActivity(mContext);
                    break;
                case R.id.share_iv:
                    share();
                    break;
                case R.id.goods_iv:
                    ((MainActivity) mContext).navigationToFragment(goodsArticleListFragment);
                    break;
                case R.id.comment_iv:
                    ((MainActivity) mContext).navigationToFragment(commentListFragment);
                    break;
                case R.id.detailsGoodsAllTv:
                    ((MainActivity) mContext).navigationToFragment(goodsArticleListFragment);
                    break;
                case R.id.detailsCommentAllTv:
                    ((MainActivity) mContext).navigationToFragment(commentListFragment);
                    break;
                case R.id.commentEditLine:
                    if (checkLogin()) {
                        frameLayout.setVisibility(View.VISIBLE);
                        CommentSendFragment commentSendFragment = CommentSendFragment.newInstance(title, mString, null);
                        commentSendFragment.setOnCommentListener(new CommentSendFragment.OnCommentListener() {
                            @Override
                            public void onCommentResult(Comment comment) {
                                if (comment != null) {
                                    commentCount++;
                                    commentNumTv.setText("(" + commentCount + ")");
                                    if (comments.size() < 5) {
                                        comments.add(0, comment);
                                    } else {
                                        comments.remove(4);
                                        comments.add(0, comment);
                                    }
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        getFragmentManager().beginTransaction().add(R.id.frameLayout, commentSendFragment).addToBackStack(null).commitAllowingStateLoss();
                    } else {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }

                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getString(ARG_PARAM);
            mString = getArguments().getString(ARG_STRING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_soft_list_detail, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        return view;
    }

    private void initView() {
        barLine = (RelativeLayout) view.findViewById(R.id.actionbarBg);
        setAlphaAnimation(barLine, 1, 0);
        shareIv = (ImageView) view.findViewById(R.id.share_iv);
        shareIv.setOnClickListener(onClickListener);

        commentIv = (ImageView) view.findViewById(R.id.comment_iv);
        commentIv.setOnClickListener(onClickListener);

        goodsIv = (ImageView) view.findViewById(R.id.goods_iv);
        goodsIv.setOnClickListener(onClickListener);

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) view.findViewById(R.id.nullNetline);
        nullDataView = (LinearLayout) view.findViewById(R.id.nullDataline);
        nullView = (LinearLayout) view.findViewById(R.id.nullline);
        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        detailsTopLine = (RelativeLayout) view.findViewById(R.id.detailsTopLine);
        detailsTopLine.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 800 / 750));
        imageIv = (ImageView) view.findViewById(R.id.detailsImageIv);
        imageIv.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth * 400 / 750));
        fadingHeight = screenWidth * 400 / 750;

        titleTv = (TextView) view.findViewById(R.id.detailsTitleTv);
        areasTv = (TextView) view.findViewById(R.id.detailsAreasTv);
        areaTypeTv = (TextView) view.findViewById(R.id.detailsAreaTv);
        styleTv = (TextView) view.findViewById(R.id.detailsStyleTv);
        budgetTv = (TextView) view.findViewById(R.id.detailsBudgetTv);
        subtotalTv = (TextView) view.findViewById(R.id.detailsSubtotalTv);
        commentNumTv = (TextView) view.findViewById(R.id.detailsCommentNumTv);
        totalLine = (LinearLayout) view.findViewById(R.id.detailsTotalLine);
        goodsAllBtn = (LinearLayout) view.findViewById(R.id.detailsGoodsAllTv);
        goodsAllBtn.setOnClickListener(onClickListener);
        commentAllLine = (LinearLayout) view.findViewById(R.id.detailsCommentAllLine);
        commentAllBtn = (LinearLayout) view.findViewById(R.id.detailsCommentAllTv);
        commentAllBtn.setOnClickListener(onClickListener);

        webview = (CustomProgressWebview) view.findViewById(R.id.detailsWebView);
        mScrollView = (CustomScrollView) view.findViewById(R.id.scrollViewArticle);
        mScrollView.setOnScrollChangedListener(new CustomScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int x, int y, int oldx, int oldy) {
                if (y < 10) {
//                    barLine.getBackground().setAlpha( START_ALPHA);
                    setAlphaAnimation(barLine, 1, 0);
                } else {
                    if (y > fadingHeight) {
                        y = fadingHeight;   //当滑动到指定位置之后设置颜色为纯色，之前的话要渐变---实现下面的公式即可
                    }
                    setAlphaAnimation(barLine, alpha, y * 1f / fadingHeight);
                    alpha = y * 1f / fadingHeight;
//                    barLine.getBackground().setAlpha(y * (END_ALPHA - START_ALPHA) / fadingHeight + START_ALPHA);
                }

            }
        });
        goodsListView = (ListViewForScrollView) view.findViewById(R.id.homeItemList);
        goodsListView.setEmptyView(mEmptyView);
        articleShoppingAdapter = new GoodsAdapter(mContext, items);
        goodsListView.setAdapter(articleShoppingAdapter);
        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                enterGoods(position);
            }
        });

        commentListView = (ListViewForScrollView) view.findViewById(R.id.detailsCommentList);
        commentListView.setEmptyView(mEmptyView);
        commentAdapter = new CommentAdapter(mContext, comments);
        commentListView.setAdapter(commentAdapter);
        commentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checkLogin()) {
                    CommentSendFragment commentSendFragment = CommentSendFragment.newInstance(title, mString, comments.get(position));
                    commentSendFragment.setOnCommentListener(new CommentSendFragment.OnCommentListener() {
                        @Override
                        public void onCommentResult(Comment comment) {
                            if (comment != null) {
                                commentCount++;
                                commentNumTv.setText("(" + commentCount + ")");
                                if (comments.size() < 5) {
                                    comments.add(0, comment);
                                } else {
                                    comments.remove(4);
                                    comments.add(0, comment);
                                }
                                commentAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    getFragmentManager().beginTransaction().add(R.id.frameLayout, commentSendFragment).addToBackStack(null).commitAllowingStateLoss();
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(1);
                    getActivity().startActivity(intent);
                }
            }
        });

        commentNumTv = (TextView) view.findViewById(R.id.detailsCommentNumTv);

        nullGoods = (TextView) view.findViewById(R.id.nullGoods);
        nullComment = (TextView) view.findViewById(R.id.detailsNullComment);

        commentLine = (LinearLayout) view.findViewById(R.id.commentEditLine);
        commentLine.setOnClickListener(onClickListener);

        frameLayout = (FrameLayout) view.findViewById(R.id.frameLayout);

        contactQQ = (LinearLayout) view.findViewById(R.id.detailsContactLine);
        contactQQ.setOnClickListener(onClickListener);
        mRequestQueue = Volley.newRequestQueue(mContext);


        initData();

    }


    private void setAlphaAnimation(View view, float from, float to) {
        alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setFillAfter(true);
        view.setAnimation(alphaAnimation);
        alphaAnimation.start();
    }


    private void initData() {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
//            FetchCommentData();
        } else {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 分享
     */
    private void share() {
        mDialogUtil = new DialogUtils(mContext);
        String shareUrl = "http://m.zhaidou.com/case_item.html?caseId=" + mString;
        mDialogUtil.showShareDialog(mParam, mParam + "  " + shareUrl, imageUrl != null ? imageUrl : null, shareUrl, new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
                Message message = handler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_completed));
                handler.sendMessage(message);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Message message = handler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_error));
                handler.sendMessage(message);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Message message = handler.obtainMessage(UPDATE_SHARE_TOAST, mContext.getString(R.string.share_cancel));
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 跳转
     *
     * @param position
     */
    private void enterGoods(int position) {
        if (items.get(position).storeId.equals("S")) {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(position).name, items.get(position).goodsId);
            Bundle bundle = new Bundle();
            bundle.putString("index", items.get(position).goodsId);
            bundle.putString("page", items.get(position).name);
            goodsDetailsFragment.setArguments(bundle);
            ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);

        } else {
            Intent intent = new Intent();
            intent.putExtra("url", items.get(position).userId);
            intent.setClass(mContext, WebViewActivity.class);
            mContext.startActivity(intent);
        }
    }

    public void FetchData() {
        String url = ZhaiDou.HomeSofeListDetailUrl + mString;
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mDialog != null)
                            mDialog.dismiss();
                        if (response == null) {
                            if (page == 1) {
                                nullNetView.setVisibility(View.GONE);
                                nullView.setVisibility(View.VISIBLE);
                                nullDataView.setVisibility(View.GONE);
                            } else {
                                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            }
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null) {
                            pageCount = jsonObject1.optInt("totalCount");
                            pageSize = jsonObject1.optInt("pageSize");
                            totalPrice = jsonObject1.optString("totalPrice");
                            JSONObject jsonObject = jsonObject1.optJSONObject("designerListPO");
                            String id = jsonObject.optString("id");
                            title = jsonObject.optString("caseName");
                            long startTime = jsonObject.optLong("updateTime");
                            imageUrl = jsonObject.optString("mainPic");
                            introduce = jsonObject.optString("caseDesc");
                            areaType = jsonObject.optString("areaType");
                            areaSize = jsonObject.optString("areaSize");
                            style = jsonObject.optString("style");
                            budget = jsonObject.optString("budget");
                            commentNum = jsonObject.optInt("commentCount");

                            JSONArray jsonArray = jsonObject1.optJSONArray("designerListProductPOs");
                            if (jsonArray != null)
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    obj = jsonArray.optJSONObject(i);
                                    int baseid = obj.optInt("id");
                                    int caseId = obj.optInt("caseId");
                                    String type = obj.optString("goodsType");
                                    String title = obj.optString("goodsTitle");
                                    String productCode = obj.optString("productCode");
                                    String productSkuCode = obj.optString("productSkuCode");
                                    String productSku = obj.optString("goodsAttr");
                                    DecimalFormat df = new DecimalFormat("#.00");
                                    double price = Double.parseDouble(df.format(obj.optDouble("price")));
                                    String imageUrl = obj.optString("mainPic");
                                    String url = "http://" + obj.optString("url");
                                    int num = obj.optInt("quantity");
                                    CartGoodsItem cartGoodsItem = new CartGoodsItem();
                                    cartGoodsItem.id = baseid;
                                    cartGoodsItem.id = caseId;//商品id
                                    cartGoodsItem.goodsId = productCode;
                                    cartGoodsItem.num = num;
                                    cartGoodsItem.imageUrl = imageUrl;
                                    cartGoodsItem.size = productSku;
                                    cartGoodsItem.sizeId = productSkuCode;
                                    cartGoodsItem.currentPrice = price;
                                    cartGoodsItem.name = title;
                                    cartGoodsItem.storeId = type;//商品类型
                                    cartGoodsItem.userId = url;//跳转地址
                                    if (items.size() < 5)
                                        items.add(cartGoodsItem);
                                }
                            handler.sendEmptyMessage(1);
                        } else {
                            if (page == 1) {
                                nullNetView.setVisibility(View.GONE);
                                nullView.setVisibility(View.VISIBLE);
                                nullDataView.setVisibility(View.GONE);
                            } else {
                                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            }
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                if (page == 1) {
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                    nullDataView.setVisibility(View.GONE);
                } else {
                    page--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        ZDApplication.mRequestQueue.add(request);
    }

    public void FetchCommentData() {
        String url = ZhaiDou.HomeArticleCommentUrl + mString + "&pageNo=" + page + "&pageSize=20";
        ToolUtils.setLog(url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mDialog != null)
                            mDialog.dismiss();
                        if (response == null) {
                            ToolUtils.setToast(mContext, "抱歉,评论加载失败");
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null) {
                            commentCount = jsonObject1.optInt("totalCount");
                            JSONArray jsonArray = jsonObject1.optJSONArray("items");
                            if (jsonArray != null & jsonArray.length() > 0)
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    obj = jsonArray.optJSONObject(i);
                                    JSONObject jsonComment = obj.optJSONObject("comment");
                                    Comment comment = new Comment();
                                    if (jsonComment != null) {
                                        int commentid = jsonComment.optInt("id");
                                        String commentTitle = jsonComment.optString("content");
                                        String commentUrl = jsonComment.optString("imgMd5");
                                        List<String> commentImgs = new ArrayList<String>();
                                        if (commentUrl.length() > 0) {
                                            String[] commentUrls = commentUrl.split(",");
                                            for (int j = 0; j < commentUrls.length; j++) {
                                                commentImgs.add(commentUrls[j]);
                                            }
                                        }
                                        int commentUserId = jsonComment.optInt("commentUserId");
                                        String commentUserName = jsonComment.optString("commentUserName");
                                        String commentUserImg = "http://" + jsonComment.optString("commentUserImg");
                                        String articleId = jsonComment.optString("articleId");
                                        String articleTitle = jsonComment.optString("articleTitle");
                                        String commentType = jsonComment.optString("commentType");
                                        String commentId = jsonComment.optString("commentId");
                                        String commentStatus = jsonComment.optString("status");
                                        String commentCreateTime = "";
                                        try {
                                            commentCreateTime = ToolUtils.getDateDiff(jsonComment.optString("createTime"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        comment.articleId = articleId;
                                        comment.articleTitle = articleTitle;
                                        comment.id = commentid;
                                        comment.time = commentCreateTime;
                                        comment.comment = commentTitle;
                                        comment.images = commentImgs;
                                        comment.type = commentType;
                                        comment.status = commentStatus;
                                        comment.userName = commentUserName;
                                        comment.userImage = commentUserImg;
                                        comment.userId = commentUserId;
                                    }

                                    JSONObject jsonReComment = obj.optJSONObject("reComment");
                                    if (jsonReComment != null) {
                                        int reCommentid = jsonReComment.optInt("id");
                                        String reCommentTitle = jsonReComment.optString("content");
                                        String reCommentUrl = jsonReComment.optString("imgMd5");
                                        List<String> reCommentImgs = new ArrayList<String>();
                                        if (reCommentUrl.length() > 0) {
                                            String[] reCommentUrls = reCommentUrl.split(",");
                                            for (int j = 0; j < reCommentUrls.length; j++) {
                                                reCommentImgs.add(reCommentUrls[j]);
                                            }
                                        }
                                        int reCommentUserId = jsonReComment.optInt("commentUserId");
                                        String reCommentUserName = jsonReComment.optString("commentUserName");
                                        String reCommentUserImg = "http://" + jsonReComment.optString("commentUserImg");
                                        String reCommentArticleId = jsonReComment.optString("articleId");
                                        String reCommentArticleTitle = jsonReComment.optString("articleTitle");
                                        String reCommentType = jsonReComment.optString("commentType");
                                        String reCommentStatus = jsonReComment.optString("status");
                                        String reCommentCreateTime = "";
                                        try {
                                            reCommentCreateTime = ToolUtils.getDateDiff(jsonComment.optString("createTime"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        comment.idReply = reCommentid;
                                        comment.timeReply = reCommentCreateTime;
                                        comment.commentReply = reCommentTitle;
                                        comment.imagesReply = reCommentImgs;
                                        comment.typeReply = reCommentType;
                                        comment.statusReply = reCommentStatus;
                                        comment.userNameReply = reCommentUserName;
                                        comment.userImageReply = reCommentUserImg;
                                        comment.userIdReply = reCommentUserId;
                                    }
                                    if (comments.size() < 5) {
                                        comments.add(comment);
                                    }
                                }
                        } else {
                            ToolUtils.setToast(mContext, "抱歉,评论加载失败");
                        }
                        handler.sendEmptyMessage(2);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                if (page > 1) {
                    page--;
                }
                ToolUtils.setToast(mContext, "抱歉,评论加载失败");
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        ZDApplication.mRequestQueue.add(request);
    }


    public class GoodsAdapter extends BaseListAdapter<CartGoodsItem> {
        Context context;

        public GoodsAdapter(Context context, List<CartGoodsItem> list) {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(final int position, View convertView, ViewGroup parent) {
//            convertView = mHashMap.get(position);

            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_article_goods, null);

            TextView goodsNameTv = ViewHolder.get(convertView, R.id.goodsNameTv);
            TextView goodsSizeTv = ViewHolder.get(convertView, R.id.goodsSizeTv);
            ImageView goodsImageTv = ViewHolder.get(convertView, R.id.goodsImageTv);
            TextView goodsPriceTv = ViewHolder.get(convertView, R.id.goodsPriceTv);
            TextView goodsNumTv = ViewHolder.get(convertView, R.id.goodsNumTv);
            TextView goodsTypeTv = ViewHolder.get(convertView, R.id.goodsTypeTv);
            TextView goodsBuyTv = ViewHolder.get(convertView, R.id.goodsBuyTv);

            final CartGoodsItem goodsItem = getList().get(position);
            goodsNameTv.setText(goodsItem.name);
            goodsSizeTv.setText(goodsItem.size);
            goodsNumTv.setText("X" + goodsItem.num);
            goodsPriceTv.setText("￥" + ToolUtils.isIntPrice(goodsItem.currentPrice + ""));
            ToolUtils.setImageCacheUrl(goodsItem.imageUrl, goodsImageTv, R.drawable.icon_loading_defalut);

            if (goodsItem.storeId.equals("T")) {
                goodsTypeTv.setText("淘宝");
                goodsTypeTv.setTextColor(Color.parseColor("#FD783A"));
            } else if (goodsItem.storeId.equals("M")) {
                goodsTypeTv.setText("天猫");
                goodsTypeTv.setTextColor(Color.parseColor("#FD783A"));
            } else if (goodsItem.storeId.equals("J")) {
                goodsTypeTv.setText("京东");
                goodsTypeTv.setTextColor(Color.parseColor("#FD783A"));
            } else {
                goodsTypeTv.setText("宅豆");
                goodsTypeTv.setTextColor(getResources().getColor(R.color.green_color));
            }

            goodsBuyTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterGoods(position);
                }
            });


//            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    public class CommentAdapter extends BaseListAdapter<Comment> {
        Context context;

        public CommentAdapter(Context context, List<Comment> list) {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
//            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_comment_message, null);
            CircleImageView header = ViewHolder.get(convertView, R.id.commentHeader);
            TextView name = ViewHolder.get(convertView, R.id.commentNameTv);
            TextView time = ViewHolder.get(convertView, R.id.commentTimeTv);
            LinearLayout commentLine = ViewHolder.get(convertView, R.id.commentLine);
            LinearLayout commentImageLine = ViewHolder.get(convertView, R.id.commentImageLine);
            TextView commentInfo = ViewHolder.get(convertView, R.id.commentInfoTv);

            LinearLayout commentReplyLine = ViewHolder.get(convertView, R.id.commentReplyLine);
            LinearLayout commentImageFormerLine = ViewHolder.get(convertView, R.id.commentImageFormerLine);
            TextView commentInfoFormer = ViewHolder.get(convertView, R.id.commentInfoFormerTv);

            LinearLayout commentImageReplyLine = ViewHolder.get(convertView, R.id.commentImageReplyLine);
            TextView commentReply = ViewHolder.get(convertView, R.id.commentInfoReplyTv);

            Comment comment = getList().get(position);
            commentImageLine.removeAllViews();
            commentImageFormerLine.removeAllViews();
            commentImageReplyLine.removeAllViews();

            if (comment.commentReply == null & comment.imagesReply.size() == 0) {
                ToolUtils.setImageCacheUrl(comment.userImage, header, R.drawable.icon_loading_defalut);
                name.setText(comment.userName);
                time.setText(comment.time);
                commentLine.setVisibility(View.VISIBLE);
                commentReplyLine.setVisibility(View.GONE);

                if (comment.images == null | comment.images.size() == 0) {
                    commentImageLine.setVisibility(View.GONE);
                } else {
                    commentImageLine.setVisibility(View.VISIBLE);
                    addImageView(commentImageLine, comment.images);
                }
                commentInfo.setText(comment.comment);
            } else {
                ToolUtils.setImageCacheUrl(comment.userImage, header, R.drawable.icon_loading_defalut);
                name.setText(comment.userName);
                time.setText(comment.time);
                commentLine.setVisibility(View.GONE);
                commentReplyLine.setVisibility(View.VISIBLE);

                if (comment.imagesReply == null | comment.imagesReply.size() == 0) {
                    commentImageFormerLine.setVisibility(View.GONE);
                } else {
                    commentImageFormerLine.setVisibility(View.VISIBLE);
                    addImageView(commentImageFormerLine, comment.imagesReply);
                }
                commentInfoFormer.setText(comment.commentReply);

                if (comment.images == null | comment.images.size() == 0) {
                    commentImageReplyLine.setVisibility(View.GONE);
                } else {
                    commentImageReplyLine.setVisibility(View.VISIBLE);
                    addImageView(commentImageReplyLine, comment.images);
                }

                commentReply.setText(Html.fromHtml("<font size=\"14\" color=\"#3fcccb\">回复@" + comment.userNameReply + "</font><font size=\"14\" color=\"#666666\"> " + comment.comment + "</font>"));
            }
//            mHashMap.put(position, convertView);
            return convertView;
        }

        /**
         * 选择相片添加布局以及相关逻辑处理
         */
        private void addImageView(LinearLayout viewLayout, final List<String> ims) {
            final ArrayList<String> im = new ArrayList<String>();
            for (String key : ims) {
                im.add(key);
            }
            for (int i = 0; i < ims.size(); i++) {
                final int position = i;
                View mView = LayoutInflater.from(mContext).inflate(R.layout.item_comment_image, null);
                ImageView imageIv = (ImageView) mView.findViewById(R.id.imageBg_iv);
                TextView btn = (TextView) mView.findViewById(R.id.imageBgBtn);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommentImageFragment commentImageFragment = CommentImageFragment.newInstance(im, position);
                        ((MainActivity) mContext).navigationToFragment(commentImageFragment);

                    }
                });
                ToolUtils.setImageCacheUrl(ims.get(i), imageIv, R.drawable.icon_loading_defalut);
                viewLayout.addView(mView);
            }


        }
    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("案例详情");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("案例详情");
    }
}
