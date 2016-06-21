package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.activities.PhotoViewActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Comment;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CircleImageView;
import com.zhaidou.view.ListViewForScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by roy on 15/8/28.
 */
public class CommentListFragment extends BaseFragment
{
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private String mPage;
    private String mIndex;

    private Dialog mDialog;
    private View mView;
    private TextView commentNumTv,nullCommentTv;
    private PullToRefreshScrollView scrollView;
    private ListViewForScrollView listView;
    private FrameLayout frameLayout;
    private  LinearLayout commentLine;

    private int page = 1;
    private int pageSize;
    private int pageCount;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private List<Comment> comments=new ArrayList<Comment>();
    private CommentAdapter commentAdapter;
    private DialogUtils mDialogUtils;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:
                    if (mDialog != null)
                    {
                        mDialog.dismiss();
                    }
                    commentNumTv.setText("("+pageCount+")");
                    commentNumTv.setVisibility(pageCount>0?View.VISIBLE:View.GONE);
                    nullCommentTv.setVisibility(pageCount>0?View.GONE:View.VISIBLE);
                    commentAdapter.notifyDataSetChanged();
                    scrollView.onRefreshComplete();
                    if (comments.size()< pageCount)
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
            page=1;
            comments.clear();
            FetchData();
        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {
            page++;
            FetchData();
        }
    };



    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.commentEditLine:
                    if (checkLogin())
                    {
                        frameLayout.setVisibility(View.VISIBLE);
                        CommentSendFragment commentSendFragment = CommentSendFragment.newInstance(mPage, mIndex,null);
                        commentSendFragment.setOnCommentListener(new CommentSendFragment.OnCommentListener()
                        {
                            @Override
                            public void onCommentResult(Comment comment)
                            {
                                if (comment!=null)
                                {
                                    pageCount++;
                                    commentNumTv.setText("("+pageCount+")");
                                    comments.add(0,comment);
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        getFragmentManager().beginTransaction().add(R.id.frameLayout, commentSendFragment).addToBackStack(null).commitAllowingStateLoss();
                    }
                    else
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(1);
                        getActivity().startActivity(intent);
                    }
                    break;

            }
        }
    };

    public static CommentListFragment newInstance(String page, String index)
    {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mPage = getArguments().getString(DATA);
            mIndex = getArguments().getString(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        if (mView == null)
        {
            mView = inflater.inflate(R.layout.fragment_comment_list, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }

        return mView;
    }

    /**
     * 初始化
     */
    private void initView()
    {
        mDialogUtils = new DialogUtils(mContext);
        commentNumTv=(TextView)mView.findViewById(R.id.commentNumTv);
        nullCommentTv=(TextView)mView.findViewById(R.id.commentNullTv);

        scrollView=(PullToRefreshScrollView)mView.findViewById(R.id.scrollView);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView=(ListViewForScrollView)mView.findViewById(R.id.lv_special_list);
        commentAdapter=new CommentAdapter(mContext,comments);
        listView.setAdapter(commentAdapter);
        commentLine=(LinearLayout)mView.findViewById(R.id.commentEditLine);
        commentLine.setOnClickListener(onClickListener);

        frameLayout=(FrameLayout)mView.findViewById(R.id.frameLayout);

        if (NetworkUtils.isNetworkAvailable(mContext))
        {
            mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
            FetchData();
        } else
        {
            Toast.makeText(mContext, "抱歉,网络链接失败", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendComment(final int position)
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
                                    commentAdapter.remove(position);
                                    pageCount--;
                                    commentNumTv.setText("("+pageCount+")");
                                    commentNumTv.setVisibility(pageCount>0?View.VISIBLE:View.GONE);
                                    ShowToast("删除成功");
                                }
                            }
                        }, null);
                    }
                });
            }
            else
            {
                frameLayout.setVisibility(View.VISIBLE);
                CommentSendFragment commentSendFragment = CommentSendFragment.newInstance(mPage, mIndex, comment);
                commentSendFragment.setOnCommentListener(new CommentSendFragment.OnCommentListener()
                {
                    @Override
                    public void onCommentResult(Comment comment)
                    {
                        if (comment != null)
                        {
                            pageCount++;
                            commentNumTv.setText("(" + pageCount + ")");
                            comments.add(0, comment);
                            commentAdapter.notifyDataSetChanged();
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


    public void FetchData()
    {
        String url = ZhaiDou.HomeArticleCommentUrl + mIndex + "&pageNo=" + page + "&pageSize=20";
        ToolUtils.setLog(url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if (mDialog != null)
                            mDialog.dismiss();
                        scrollView.onRefreshComplete();
                        if (response == null)
                        {
                            ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            return;
                        }
                        ToolUtils.setLog(response.toString());
                        JSONObject obj;
                        int status = response.optInt("status");
                        JSONObject jsonObject1 = response.optJSONObject("data");
                        if (jsonObject1 != null)
                        {
                            pageCount = jsonObject1.optInt("totalCount");
                            pageSize = jsonObject1.optInt("pageSize");
                            JSONArray jsonArray=jsonObject1.optJSONArray("items");
                            if (jsonArray!=null&jsonArray.length()>0)
                                for (int i = 0; i <jsonArray.length() ; i++)
                                {
                                    obj=jsonArray.optJSONObject(i);
                                    JSONObject jsonComment=obj.optJSONObject("comment");
                                    Comment comment=new Comment();
                                    if (jsonComment!=null)
                                    {
                                        int commentId=jsonComment.optInt("id");
                                        String commentTitle=jsonComment.optString("content");
                                        String commentUrl=jsonComment.optString("imgMd5");
                                        List<String> commentImgs=new ArrayList<String>();
                                        if (commentUrl.length()>0)
                                        {
                                            String[] commentUrls=commentUrl.split(",");
                                            for (int j = 0; j <commentUrls.length; j++)
                                            {
                                                commentImgs.add(commentUrls[j]);
                                            }
                                        }
                                        int commentUserId=jsonComment.optInt("commentUserId");
                                        String commentUserName=jsonComment.optString("commentUserName");
                                        String commentUserImg=jsonComment.optString("commentUserImg");
                                        String articleId=jsonComment.optString("articleId");
                                        String articleTitle=jsonComment.optString("articleTitle");
                                        String commentType=jsonComment.optString("commentType");
                                        String commentStatus=jsonComment.optString("status");
                                        String commentCreateTime= "";
                                        try
                                        {
                                            commentCreateTime = ToolUtils.getDateDiff(jsonComment.optString("createTime"));
                                        } catch (ParseException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        comment.articleId=articleId;
                                        comment.articleTitle=articleTitle;
                                        comment.id=commentId;
                                        comment.time=commentCreateTime;
                                        comment.comment=commentTitle;
                                        comment.images=commentImgs;
                                        comment.type=commentType;
                                        comment.status=commentStatus;
                                        comment.userName=commentUserName;
                                        comment.userImage=commentUserImg;
                                        comment.userId=commentUserId;
                                    }

                                    JSONObject jsonReComment=obj.optJSONObject("reComment");
                                    if(jsonReComment!=null)
                                    {
                                        int reCommentId=jsonReComment.optInt("id");
                                        String reCommentTitle=jsonReComment.optString("content");
                                        String reCommentUrl=jsonReComment.optString("imgMd5");
                                        List<String> reCommentImgs=new ArrayList<String>();
                                        if (reCommentUrl.length()>0)
                                        {
                                            String[] reCommentUrls=reCommentUrl.split(",");
                                            for (int j = 0; j <reCommentUrls.length; j++)
                                            {
                                                reCommentImgs.add(reCommentUrls[j]);
                                            }
                                        }
                                        int reCommentUserId=jsonReComment.optInt("commentUserId");
                                        String reCommentUserName=jsonReComment.optString("commentUserName");
                                        String reCommentUserImg=jsonReComment.optString("commentUserImg");
                                        String reCommentArticleId=jsonReComment.optString("articleId");
                                        String reCommentArticleTitle=jsonReComment.optString("articleTitle");
                                        String reCommentType=jsonReComment.optString("commentType");
                                        String reCommentStatus=jsonReComment.optString("status");
                                        String reCommentCreateTime= "";
                                        try
                                        {
                                            reCommentCreateTime = ToolUtils.getDateDiff(jsonComment.optString("createTime"));
                                        } catch (ParseException e)
                                        {
                                            e.printStackTrace();
                                        }
                                        comment.idReply=reCommentId;
                                        comment.timeReply=reCommentCreateTime;
                                        comment.commentReply=reCommentTitle;
                                        comment.imagesReply=reCommentImgs;
                                        comment.typeReply=reCommentType;
                                        comment.statusReply=reCommentStatus;
                                        comment.userNameReply=reCommentUserName;
                                        comment.userImageReply=reCommentUserImg;
                                        comment.userIdReply=reCommentUserId;
                                    }
                                    comments.add(comment);
                                }
                            mHandler.sendEmptyMessage(1);
                        } else
                        {
                            ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                            return;
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mDialog.dismiss();
                scrollView.onRefreshComplete();
                if (page >1)
                {
                    page--;
                    ToolUtils.setToast(mContext, R.string.loading_fail_txt);
                }
            }
        }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
//        request.setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ZDApplication.mRequestQueue.add(request);
    }


    public class CommentAdapter extends BaseListAdapter<Comment>
    {
        Context context;

        public CommentAdapter(Context context, List<Comment> list)
        {
            super(context, list);
            this.context = context;
        }

        @Override
        public View bindView(final int position, View convertView, ViewGroup parent)
        {
//            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_comment_message, null);
            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendComment(position);
                }
            });
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
            TextView commentReply= ViewHolder.get(convertView, R.id.commentInfoReplyTv);

            Comment comment=getList().get(position);
            commentImageLine.removeAllViews();
            commentImageFormerLine.removeAllViews();
            commentImageReplyLine.removeAllViews();

            if (comment.commentReply==null&comment.imagesReply.size()==0)
            {
                ToolUtils.setImageCacheUrl(comment.userImage, header, R.drawable.icon_loading_defalut);
                name.setText(comment.userName);
                time.setText(comment.time);
                commentLine.setVisibility(View.VISIBLE);
                commentReplyLine.setVisibility(View.GONE);

                if (comment.images==null|comment.images.size()==0)
                {
                    commentImageLine.setVisibility(View.GONE);
                }
                else
                {
                    commentImageLine.setVisibility(View.VISIBLE);
                    addImageView(commentImageLine,comment.images);
                }
                commentInfo.setText(comment.comment);
                commentInfo.setVisibility(comment.comment.length()>0?View.VISIBLE: View.GONE);
                if(comment.status.equals("F"))
                {
                    commentImageLine.setVisibility(View.GONE);
                }

            }
            else
            {
                ToolUtils.setImageCacheUrl(comment.userImage, header, R.drawable.icon_loading_defalut);
                name.setText(comment.userName);
                time.setText(comment.time);
                commentLine.setVisibility(View.GONE);
                commentReplyLine.setVisibility(View.VISIBLE);

                if (comment.imagesReply==null|comment.imagesReply.size()==0)
                {
                    commentImageFormerLine.setVisibility(View.GONE);
                }
                else
                {
                    commentImageFormerLine.setVisibility(View.VISIBLE);
                    addImageView(commentImageFormerLine,comment.imagesReply);
                }
                commentInfoFormer.setText(comment.commentReply);
                if(comment.statusReply.equals("F"))
                {
                    commentImageFormerLine.setVisibility(View.GONE);
                }

                if (comment.images==null|comment.images.size()==0)
                {
                    commentImageReplyLine.setVisibility(View.GONE);
                }
                else
                {
                    commentImageReplyLine.setVisibility(View.VISIBLE);
                    addImageView(commentImageReplyLine,comment.images);
                }
                if(comment.status.equals("F"))
                {
                    commentImageReplyLine.setVisibility(View.GONE);
                }
                commentReply.setText(Html.fromHtml("<font size=\"14\" color=\"#3fcccb\">回复@"+comment.userNameReply+"</font><font size=\"14\" color=\"#666666\"> "+comment.comment+"</font>"));
            }
//            mHashMap.put(position, convertView);
            return convertView;
        }

        /**
         * 选择相片添加布局以及相关逻辑处理
         */
        private void addImageView(LinearLayout viewLayout, final List<String> ims)
        {
            for (int i = 0; i < ims.size(); i++)
            {
                final int position=i;
                View mView = LayoutInflater.from(mContext).inflate(R.layout.item_comment_image, null);
                ImageView imageIv = ( ImageView ) mView.findViewById(R.id.imageBg_iv);
                TextView btn=( TextView ) mView.findViewById(R.id.imageBgBtn);
                btn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent=new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra("images",ims.toArray(new String[]{}));
                        intent.putExtra("position",position);
                        startActivity(intent);

                    }
                });
                ToolUtils.setImageCacheUrl(ims.get(i), imageIv, R.drawable.icon_loading_defalut);
                viewLayout.addView(mView);
            }


        }
    }

    private OnCommentListener onCommentListener;

    public void setOnCommentListener(OnCommentListener onCommentListener)
    {
        this.onCommentListener=onCommentListener;
    }

    public interface OnCommentListener
    {
        void GetComments(List<Comment> comments1,int num);
    }


    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("评论列表");
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("评论列表");
    }

    @Override
    public void onDestroy()
    {
        if (comments!=null)
        {
            if (comments.size()>4)
            {
                List<Comment> comments1=new ArrayList<Comment>();
                for (int i = 0; i <5 ; i++)
                {
                    comments1.add(comments.get(i));
                }
                onCommentListener.GetComments(comments1,pageCount);
            }
            else
            {
                onCommentListener.GetComments(comments,pageCount);
            }
        }
        super.onDestroy();
    }
}
