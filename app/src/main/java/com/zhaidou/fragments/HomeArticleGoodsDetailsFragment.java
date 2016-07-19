package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.adapter.ArticleGoodsAdapter;
import com.zhaidou.adapter.CommentAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.model.Comment;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
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

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
 * 文章商品
 */
public class HomeArticleGoodsDetailsFragment extends BaseFragment
{
    private static final String ARG_PARAM = "param";
    private static final String ARG_STRING = "string";
    private static final String ARG_FLAGS = "flags";

    private String mParam;
    private String mString;
    private int mFlags;//1方案详情2软装方案详情
    private View view;
    private RelativeLayout barLine,rl_video;
    private CircleImageView headerImageIv;
    private ImageView backIv,shareIv, goodsIv, imageIv, commentIv,iv_videoImage;
    private CustomProgressWebview webview;
    private ListViewForScrollView goodsListView, commentListView;
    private LinearLayout loadingView, nullNetView, nullView, nullDataView;
    private TextView reloadBtn, reloadNetBtn;
    private LinearLayout contactQQ;
    private RelativeLayout detailsTopLine;
    private TextView headerTopTv, headerNameTv,titleTv, areaTypeTv, areasTv, styleTv, budgetTv, nullGoods, nullComment, subtotalTv, commentNumTv;
    private LinearLayout ll_videoPage,totalLine, goodsAllBtn, ll_commentPage,commentAllLine, commentAllBtn,commentLine,ll_contact;
    private FrameLayout frameLayout;

    private CustomScrollView mScrollView;
    private ArticleGoodsAdapter articleShoppingAdapter;

    private Dialog mDialog;
    private DialogUtils mDialogUtil;
    private Context mContext;
    private float alpha = 0;
    private static final int START_ALPHA = 0;
    private static final int END_ALPHA = 255;
    private int fadingHeight = 0;   //当ScrollView滑动到什么位置时渐变消失（根据需要进行调整）
    private final int UPDATE_SHARE_TOAST=8;

    private RequestQueue mRequestQueue;
    private int page = 1;
    private int pageSize;
    private int commentCount=0;
    private String imageUrl,header,headerName,title, introduce, areaType, areaSize, style, budget, totalPrice,videoUrl,vid,vImage,shareName;
    private List<CartGoodsItem> items = new ArrayList<CartGoodsItem>();
    private AlphaAnimation alphaAnimation;
    private List<Comment> comments = new ArrayList<Comment>();
    private CommentAdapter commentAdapter;
    private DialogUtils mDialogUtils;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                headerNameTv.setText(headerName);
                titleTv.setText(title);
                areaTypeTv.setText(areaType);
                areasTv.setText(areaSize);
                styleTv.setText(style);
                budgetTv.setText(budget);
                subtotalTv.setText("￥" + ToolUtils.isIntPrice(totalPrice));

                ll_videoPage.setVisibility(TextUtils.isEmpty(videoUrl)?View.GONE:View.VISIBLE);
                ToolUtils.setImageCacheUrl(vImage, iv_videoImage, R.drawable.icon_loading_item);
                ToolUtils.setImageCacheUrl(header, headerImageIv, R.drawable.icon_loading_item);
                ToolUtils.setImageCacheUrl(imageUrl, imageIv, R.drawable.icon_loading_item);

                webview.loadData(introduce, "text/html; charset=UTF-8", "UTF-8");

                nullGoods.setVisibility(items.size() > 0 ? View.GONE : View.VISIBLE);
                totalLine.setVisibility(items.size() > 0 ? View.VISIBLE : View.GONE);
                articleShoppingAdapter.notifyDataSetChanged();
                loadingView.setVisibility(View.GONE);
                if (mDialog != null)
                    mDialog.dismiss();
            } else if (msg.what == 2)
            {
                commentNumTv.setText(commentCount + "");
                commentNumTv.setVisibility(commentCount > 0 ? View.VISIBLE : View.GONE);
                nullComment.setVisibility(comments.size() > 0 ? View.GONE : View.VISIBLE);
                commentAllLine.setVisibility(comments.size() > 0 ? View.VISIBLE : View.GONE);
                commentAdapter.upDate(comments);
            }
            else if (msg.what ==UPDATE_SHARE_TOAST)
            {
                    mDialogUtil.dismiss();
                    String result = (String) msg.obj;
                    Toast.makeText(mContext,result,Toast.LENGTH_SHORT).show();
            }

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            GoodsArticleListFragment goodsArticleListFragment = GoodsArticleListFragment.newInstance(mFlags, mString);
            CommentListFragment commentListFragment = CommentListFragment.newInstance(title, mString);
            commentListFragment.setOnCommentListener(new CommentListFragment.OnCommentListener()
            {
                @Override
                public void GetComments(List<Comment> comments1,int num)
                {
                    if (comments1!=null&&comments1.size()>0)
                        if (comments!=comments1)
                        {
                            comments.clear();
                            comments.addAll(comments1);
                            commentAdapter.upDate(comments);
                            nullComment.setVisibility(comments.size() > 0 ? View.GONE : View.VISIBLE);
                            commentAllLine.setVisibility(comments.size() > 0 ? View.VISIBLE : View.GONE);
                        }
                    if (commentCount!=num)
                    {
                        commentCount=num;
                        commentNumTv.setText("(" + commentCount + ")");
                    }
                }
            });
            switch (view.getId())
            {
                case R.id.nullReload:
                    initData();
                    break;
                case R.id.netReload:
                    initData();
                    break;
                case R.id.ll_contact:
                    EaseUtils.startKeFuActivity(mContext);
                    break;
                case R.id.share_iv:
                    share();
                    break;
                case R.id.goods_iv:
                    ((BaseActivity) mContext).navigationToFragment(goodsArticleListFragment);
                    break;
                case R.id.comment_iv:
                    ((BaseActivity) mContext).navigationToFragment(commentListFragment);
                    break;
                case R.id.detailsGoodsAllTv:
                    ((BaseActivity) mContext).navigationToFragment(goodsArticleListFragment);
                    break;
                case R.id.detailsCommentAllTv:
                    ((BaseActivity) mContext).navigationToFragment(commentListFragment);
                    break;
                case R.id.rl_video:
                    Intent video = new Intent();
                    String url=null;
                    if (!TextUtils.isEmpty(vid))
                    {
                        url="http://player.youku.com/embed/"+vid+"?client_id=814e6ba73e9be572";
                    }
                    else
                    {
                        url=videoUrl;
                    }
                    video.putExtra("url", url);
                    video.setClass(mContext, WebViewActivity.class);
                    mContext.startActivity(video);
                    break;
                case R.id.commentEditLine:
                    if (checkLogin())
                    {
                        frameLayout.setVisibility(View.VISIBLE);
                        CommentSendFragment commentSendFragment = CommentSendFragment.newInstance(title, mString, null);
                        commentSendFragment.setOnCommentListener(new CommentSendFragment.OnCommentListener()
                        {
                            @Override
                            public void onCommentResult(Comment comment)
                            {
                                if (comment != null)
                                {
                                    commentCount++;
                                    commentNumTv.setText("(" + commentCount + ")");
                                    comments.add(0, comment);
                                    commentAdapter.upDate(comments);
                                    nullComment.setVisibility(comments.size() > 0 ? View.GONE : View.VISIBLE);
                                    commentAllLine.setVisibility(comments.size() > 0 ? View.VISIBLE : View.GONE);
                                }
                            }
                        });
                        getFragmentManager().beginTransaction().add(R.id.frameLayout, commentSendFragment).addToBackStack(null).commitAllowingStateLoss();
                    } else
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }

                    break;
            }
        }
    };




    public static HomeArticleGoodsDetailsFragment newInstance(String param, String string,int flags)
    {
        HomeArticleGoodsDetailsFragment fragment = new HomeArticleGoodsDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        args.putString(ARG_STRING, string);
        args.putInt(ARG_FLAGS, flags);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeArticleGoodsDetailsFragment()
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
            mFlags= getArguments().getInt(ARG_FLAGS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_home_article_details, container, false);
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
        mDialogUtils = new DialogUtils(mContext);
        barLine = (RelativeLayout) view.findViewById(R.id.actionbarBg);
        setAlphaAnimation(barLine, 1, 0);
        shareIv = (ImageView) view.findViewById(R.id.share_iv);
        shareIv.setOnClickListener(onClickListener);

        commentIv = (ImageView) view.findViewById(R.id.comment_iv);
        commentIv.setOnClickListener(onClickListener);

        goodsIv = (ImageView) view.findViewById(R.id.goods_iv);
        goodsIv.setOnClickListener(onClickListener);

        backIv = (ImageView) view.findViewById(R.id.backIv);
        headerTopTv=(TextView) view.findViewById(R.id.tv_title);

        headerTopTv.setTextColor(getResources().getColor(R.color.white));
        backIv.setImageResource(R.drawable.icon_back);
        shareIv.setImageResource(R.drawable.share_white);
        commentIv.setImageResource(R.drawable.icon_home_article_comment_white);
        goodsIv.setImageResource(R.drawable.icon_home_article_goods_list_white);

        loadingView = (LinearLayout) view.findViewById(R.id.loadingView);
        nullNetView = (LinearLayout) view.findViewById(R.id.nullNetline);
        nullDataView = (LinearLayout) view.findViewById(R.id.nullDataline);
        nullView = (LinearLayout) view.findViewById(R.id.nullline);
        reloadBtn = (TextView) view.findViewById(R.id.nullReload);
        reloadBtn.setOnClickListener(onClickListener);
        reloadNetBtn = (TextView) view.findViewById(R.id.netReload);
        reloadNetBtn.setOnClickListener(onClickListener);

        headerImageIv= (CircleImageView) view.findViewById(R.id.detailsHeaderIv);
        headerNameTv= (TextView) view.findViewById(R.id.detailsHeaderNameTv);

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

        rl_video= (RelativeLayout) view.findViewById(R.id.rl_video);
        rl_video.setOnClickListener(onClickListener);
        iv_videoImage= (ImageView) view.findViewById(R.id.iv_videoImage);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(screenWidth- DeviceUtils.dp2px(mContext,20),(screenWidth- DeviceUtils.dp2px(mContext,20))*9/16);
        layoutParams.topMargin=DeviceUtils.dp2px(mContext,20);
        layoutParams.leftMargin=DeviceUtils.dp2px(mContext,10);
        layoutParams.rightMargin=DeviceUtils.dp2px(mContext,10);
        layoutParams.bottomMargin=DeviceUtils.dp2px(mContext,20);
        rl_video.setLayoutParams(layoutParams);
        ll_videoPage = (LinearLayout) view.findViewById(R.id.ll_videoPage);

        commentNumTv = (TextView) view.findViewById(R.id.detailsCommentNumTv);
        totalLine = (LinearLayout) view.findViewById(R.id.detailsTotalLine);
        goodsAllBtn = (LinearLayout) view.findViewById(R.id.detailsGoodsAllTv);
        goodsAllBtn.setOnClickListener(onClickListener);
        ll_commentPage= (LinearLayout) view.findViewById(R.id.ll_commentPage);
        commentAllLine = (LinearLayout) view.findViewById(R.id.detailsCommentAllLine);
        commentAllBtn = (LinearLayout) view.findViewById(R.id.detailsCommentAllTv);
        commentAllBtn.setOnClickListener(onClickListener);

        webview = (CustomProgressWebview) view.findViewById(R.id.detailsWebView);
        mScrollView = (CustomScrollView) view.findViewById(R.id.scrollViewArticle);
        mScrollView.setOnScrollChangedListener(new CustomScrollView.OnScrollChangedListener()
        {
            @Override
            public void onScrollChanged(int x, int y, int oldx, int oldy)
            {
                if (y < 100| y==0)
                {
//                    barLine.getBackground().setAlpha( START_ALPHA);
                    headerTopTv.setTextColor(getResources().getColor(R.color.white));
                    backIv.setImageResource(R.drawable.icon_back);
                    shareIv.setImageResource(R.drawable.share_white);
                    commentIv.setImageResource(R.drawable.icon_home_article_comment_white);
                    goodsIv.setImageResource(R.drawable.icon_home_article_goods_list_white);
                    setAlphaAnimation(barLine, 1, 0);
                } else
                {
                    headerTopTv.setTextColor(getResources().getColor(R.color.text_main_color));
                    backIv.setImageResource(R.drawable.icon_back_gary);
                    shareIv.setImageResource(R.drawable.share);
                    commentIv.setImageResource(R.drawable.icon_home_article_comment);
                    goodsIv.setImageResource(R.drawable.icon_home_article_goods_list);
                    if (y > fadingHeight)
                    {
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
        articleShoppingAdapter = new ArticleGoodsAdapter(mContext, items);
        goodsListView.setAdapter(articleShoppingAdapter);
        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                enterGoods(position);
            }
        });

        commentListView = (ListViewForScrollView) view.findViewById(R.id.detailsCommentList);
        commentListView.setEmptyView(mEmptyView);
        commentAdapter = new CommentAdapter(mContext, comments);
        commentListView.setAdapter(commentAdapter);
        commentListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                final Comment comment=comments.get(position);
                if (checkLogin())
                {
                    int userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
                    if (comment.userId==userId)
                    {
                        mDialogUtils.showListDialog(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                                mDialog=CustomLoadingDialog.setLoadingDialog(mContext,"");
                                Api.deleteComment(comment.id, new Api.SuccessListener()
                                {
                                    @Override
                                    public void onSuccess(Object object)
                                    {
                                        if (object != null)
                                        {
                                            mDialog.dismiss();
                                            ToolUtils.setLog(object.toString());
                                            comments.remove(position);
                                            commentAdapter.upDate(comments);
                                            commentCount--;
                                            commentNumTv.setText("(" + commentCount + ")");
                                            commentNumTv.setVisibility(commentCount > 0 ? View.VISIBLE : View.GONE);
                                            ShowToast("删除成功");
                                        }
                                    }
                                }, null);
                            }
                        });
                    }
                    else
                    {

                        CommentSendFragment commentSendFragment = CommentSendFragment.newInstance(title, mString, comments.get(position));
                        commentSendFragment.setOnCommentListener(new CommentSendFragment.OnCommentListener()
                        {
                            @Override
                            public void onCommentResult(Comment comment)
                            {
                                if (comment != null)
                                {
                                    commentCount++;
                                    commentNumTv.setText("(" + commentCount + ")");
                                    comments.add(0, comment);
                                    commentAdapter.upDate(comments);
                                    nullComment.setVisibility(comments.size() > 0 ? View.GONE : View.VISIBLE);
                                    commentAllLine.setVisibility(comments.size() > 0 ? View.VISIBLE : View.GONE);
                                }
                            }
                        });
                        getFragmentManager().beginTransaction().add(R.id.frameLayout, commentSendFragment).addToBackStack(null).commitAllowingStateLoss();
                    }
                } else
                {
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

        ll_contact = (LinearLayout) view.findViewById(R.id.ll_contact);
        ll_contact.setOnClickListener(onClickListener);
        mRequestQueue = ZDApplication.newRequestQueue();


        if (mFlags==2)
        {
            commentIv.setVisibility(View.GONE);
            ll_contact.setVisibility(View.VISIBLE);
            commentLine.setVisibility(View.GONE);
            ll_commentPage.setVisibility(View.GONE);
        }
        else
        {
            commentIv.setVisibility(View.VISIBLE);
            ll_contact.setVisibility(View.GONE);
            commentLine.setVisibility(View.VISIBLE);
            ll_commentPage.setVisibility(View.VISIBLE);
        }
        initData();

    }


    private void setAlphaAnimation(View view, float from, float to)
    {
        alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setFillAfter(true);
        view.setAnimation(alphaAnimation);
        alphaAnimation.start();
    }


    private void initData()
    {
        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
            if (mFlags==1)
            {
                FetchCommentData();
            }
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 分享
     */
    private void share()
    {
        mDialogUtil = new DialogUtils(mContext);
        String shareUrl=ZhaiDou.HOME_BASE_WAP_URL+"case_item.html?caseId="+mString;
        mDialogUtil.showShareDialog(TextUtils.isEmpty(shareName)?mParam:shareName, TextUtils.isEmpty(shareName)?mParam:shareName + "  " + shareUrl, imageUrl != null ?imageUrl : null, shareUrl, new PlatformActionListener() {
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
    private void enterGoods(int position)
    {
        if (items.get(position).storeId.equals("S"))
        {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(position).name, items.get(position).goodsId);
            Bundle bundle = new Bundle();
            bundle.putString("index", items.get(position).goodsId);
            bundle.putString("page", items.get(position).name);
            bundle.putString("sizeId", items.get(position).sizeId);
            goodsDetailsFragment.setArguments(bundle);
            ((BaseActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);

        } else
        {
            Intent intent = new Intent();
            intent.putExtra("url", items.get(position).userId);
            intent.setClass(mContext, WebViewActivity.class);
            mContext.startActivity(intent);
        }
    }

    public void FetchData()
    {
        String url = (mFlags==1?ZhaiDou.HomeArticleGoodsDetailsUrl:ZhaiDou.HomeSofeListDetailUrl) + mString;
        ZhaiDouRequest request = new ZhaiDouRequest(url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        if (response == null)
                        {
                            if (page == 1)
                            {
                                nullNetView.setVisibility(View.GONE);
                                nullView.setVisibility(View.VISIBLE);
                                nullDataView.setVisibility(View.GONE);
                            } else
                            {
                                ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            }
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null)
                        {
                            int pageCount = jsonObject1.optInt("totalCount");
                            pageSize = jsonObject1.optInt("pageSize");
                            Double aDouble= jsonObject1.optDouble("totalPrice");
                            DecimalFormat df=new DecimalFormat("#.00");
                            totalPrice=df.format(aDouble);

                            JSONObject jsonObject = jsonObject1.optJSONObject(mFlags==1?"freeClassicsCasePO":"designerListPO");
                            String id = jsonObject.optString("id");
                            title = jsonObject.optString("caseName");
                            long startTime = jsonObject.optLong("updateTime");
                            imageUrl = jsonObject.optString("mainPic");
                            introduce = jsonObject.optString("caseDesc");
                            areaType = jsonObject.optString("areaType");
                            areaSize = jsonObject.optString("areaSize");
                            style = jsonObject.optString("style");
                            budget = jsonObject.optString("budget");
                            videoUrl = jsonObject.optString("videoUrl");
                            vid = jsonObject.optString("vid");
                            vImage = jsonObject.optString("vImage");
                            shareName = jsonObject.optString("shareName");

                            JSONObject jsonObject2 = jsonObject1.optJSONObject("designerUserPO");
                            header = jsonObject2.optString("imageUrl");
                            headerName = jsonObject2.optString("name");

                            JSONArray jsonArray = jsonObject1.optJSONArray(mFlags==1?"changeCaseProductPOs":"designerListProductPOs");
                            if (jsonArray != null)
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    obj = jsonArray.optJSONObject(i);
                                    int baseid = obj.optInt("id");
                                    int caseId = obj.optInt("caseId");
                                    String type = obj.optString("goodsType");
                                    String title = obj.optString("goodsTitle");
                                    String productCode = obj.optString("productCode");
                                    String productSkuCode = obj.optString("productSkuCode");
                                    String productSku = obj.optString("goodsAttr");
                                    double price = Double.parseDouble(df.format(obj.optDouble("price")));
                                    String imageUrl = obj.optString("mainPic");
                                    String url =obj.optString("aUrl");
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
                                    if (items.size()<5)
                                    items.add(cartGoodsItem);
                                }
                            handler.sendEmptyMessage(1);
                        } else
                        {
                            if (mDialog != null)
                                mDialog.dismiss();
                            nullNetView.setVisibility(View.GONE);
                            nullView.setVisibility(View.VISIBLE);
                            nullDataView.setVisibility(View.GONE);
                            return;
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                if (mDialog != null)
                    mDialog.dismiss();
                if (page == 1)
                {
                    nullNetView.setVisibility(View.GONE);
                    nullView.setVisibility(View.VISIBLE);
                    nullDataView.setVisibility(View.GONE);
                } else
                {
                    page--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
            }
        });
        mRequestQueue.add(request);
    }

    public void FetchCommentData()
    {
        String url = ZhaiDou.HomeArticleCommentUrl + mString + "&pageNo=" + page + "&pageSize=15";
        ToolUtils.setLog(url);
        ZhaiDouRequest request = new ZhaiDouRequest(url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if (response == null)
                        {
                            ToolUtils.setToast(mContext, "抱歉,评论加载失败");
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null)
                        {
                            commentCount = jsonObject1.optInt("totalCount");
                            JSONArray jsonArray = jsonObject1.optJSONArray("items");
                            if (jsonArray != null & jsonArray.length() > 0)
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    obj = jsonArray.optJSONObject(i);
                                    JSONObject jsonComment = obj.optJSONObject("comment");
                                    Comment comment = new Comment();
                                    if (jsonComment != null)
                                    {
                                        int commentid = jsonComment.optInt("id");
                                        String commentTitle = jsonComment.optString("content");
                                        String commentUrl = jsonComment.optString("imgMd5");
                                        List<String> commentImgs = new ArrayList<String>();
                                        if (commentUrl.length() > 0)
                                        {
                                            String[] commentUrls = commentUrl.split(",");
                                            for (int j = 0; j < commentUrls.length; j++)
                                            {
                                                commentImgs.add(commentUrls[j]);
                                            }
                                        }
                                        int commentUserId = jsonComment.optInt("commentUserId");
                                        String commentUserName = jsonComment.optString("commentUserName");
                                        String commentUserImg = jsonComment.optString("commentUserImg");
                                        String articleId = jsonComment.optString("articleId");
                                        String articleTitle = jsonComment.optString("articleTitle");
                                        String commentType = jsonComment.optString("commentType");
                                        String commentId = jsonComment.optString("commentId");
                                        String commentStatus = jsonComment.optString("status");
                                        String commentCreateTime = "";
                                        try
                                        {
                                            commentCreateTime = ToolUtils.getDateDiff(jsonComment.optString("createTime"));
                                        } catch (ParseException e)
                                        {
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
                                        comment.userName=commentUserName;
                                        comment.userImage=commentUserImg;
                                        comment.userId=commentUserId;
                                    }

                                    JSONObject jsonReComment = obj.optJSONObject("reComment");
                                    if (jsonReComment != null)
                                    {
                                        int reCommentid = jsonReComment.optInt("id");
                                        String reCommentTitle = jsonReComment.optString("content");
                                        String reCommentUrl = jsonReComment.optString("imgMd5");
                                        List<String> reCommentImgs = new ArrayList<String>();
                                        if (reCommentUrl.length() > 0)
                                        {
                                            String[] reCommentUrls = reCommentUrl.split(",");
                                            for (int j = 0; j < reCommentUrls.length; j++)
                                            {
                                                reCommentImgs.add(reCommentUrls[j]);
                                            }
                                        }
                                        int reCommentUserId = jsonReComment.optInt("commentUserId");
                                        String reCommentUserName = jsonReComment.optString("commentUserName");
                                        String reCommentUserImg = jsonReComment.optString("commentUserImg");
                                        String reCommentArticleId = jsonReComment.optString("articleId");
                                        String reCommentArticleTitle = jsonReComment.optString("articleTitle");
                                        String reCommentType = jsonReComment.optString("commentType");
                                        String reCommentStatus = jsonReComment.optString("status");
                                        String reCommentCreateTime = "";
                                        try
                                        {
                                            reCommentCreateTime = ToolUtils.getDateDiff(jsonComment.optString("createTime"));
                                        } catch (ParseException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        comment.idFormer = reCommentid;
                                        comment.timeFormer = reCommentCreateTime;
                                        comment.commentFormer = reCommentTitle;
                                        comment.imagesFormer = reCommentImgs;
                                        comment.typeFormer = reCommentType;
                                        comment.statusFormer = reCommentStatus;
                                        comment.userNameFormer =reCommentUserName;
                                        comment.userImageFormer =reCommentUserImg;
                                        comment.userIdFormer =reCommentUserId;
                                    }
                                    comments.add(comment);
                                }
                        } else
                        {
                            ToolUtils.setToast(mContext, "抱歉,评论加载失败");
                        }
                        handler.sendEmptyMessage(2);
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                if (page > 1)
                {
                    page--;
                }
                ToolUtils.setToast(mContext, "抱歉,评论加载失败");
            }
        });
        mRequestQueue.add(request);
    }


    private OnCommentListener onCommentListener;

    public  void setOnCommentListener(OnCommentListener onCommentListener)
    {
        this.onCommentListener=onCommentListener;
    }

    public  interface  OnCommentListener
    {
        void setComment(int num);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("案例详情");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("案例详情");
    }

    @Override
    public void onDestroy()
    {
        if (onCommentListener!=null)
            onCommentListener.setComment(commentCount);
        super.onDestroy();
    }
}
