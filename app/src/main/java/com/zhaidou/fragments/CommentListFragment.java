package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Comment;
import com.zhaidou.model.User;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CircleImageView;
import com.zhaidou.view.ListViewForScrollView;

import java.util.ArrayList;
import java.util.List;
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
    private TextView commentNumTv;
    private PullToRefreshScrollView scrollView;
    private ListViewForScrollView listView;
    private FrameLayout frameLayout;
    private  LinearLayout commentLine;

    private int num=5;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private List<Comment> comments=new ArrayList<Comment>();


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:

                    break;
            }
        }
    };


    private PullToRefreshBase.OnRefreshListener2 onRefreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {

        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {

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
                    frameLayout.setVisibility(View.VISIBLE);
                    CommentSendFragment commentSendFragment = CommentSendFragment.newInstance("", "");
                    getFragmentManager().beginTransaction().add(R.id.frameLayout, commentSendFragment).addToBackStack(null).commitAllowingStateLoss();
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
//        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");
        commentNumTv=(TextView)mView.findViewById(R.id.commentNumTv);
        commentNumTv.setText(""+num);

        scrollView=(PullToRefreshScrollView)mView.findViewById(R.id.scrollView);
        scrollView.setMode(PullToRefreshBase.Mode.BOTH);
        scrollView.setOnRefreshListener(onRefreshListener);
        listView=(ListViewForScrollView)mView.findViewById(R.id.lv_special_list);

        commentLine=(LinearLayout)mView.findViewById(R.id.commentEditLine);
        commentLine.setOnClickListener(onClickListener);

        frameLayout=(FrameLayout)mView.findViewById(R.id.frameLayout);

        initData();
    }

    private void initData()
    {
        List<String> images=new ArrayList<String>();
        images.add("http://imgs.zhaidou.com/goods/20/151305001620/gd2_20160001.jpg");
        images.add("http://imgs.zhaidou.com/goods/35/131205001435/131205001435001/sk1_20160001.jpg");
        images.add("http://imgs.zhaidou.com/goods/96/141105000796/141105000796002/sk1_20160001.jpg");
        images.add("http://imgs.zhaidou.com/goods/36/151205000736/151205000736003/sk1_20160002.jpg");

        Comment comment=new Comment();
        comment.time="11：30";
        comment.comment="哇，好厉害！这是我家的风格......大家喜欢吗？喜欢我可以帮忙设计";
        comment.images=images;
        comment.commentReply="哇，好厉害！喜欢我也想要一套，可以帮我设计吗？";
        comment.imagesReply=images;
        User user=new User();
        user.setNickName("周杰伦");
        user.setAvatar("http://imgs.zhaidou.com/goods/36/151205000736/151205000736003/sk1_20160002.jpg");
        comment.user=user;
        User userReply=new User();
        userReply.setNickName("周杰伦");
        comment.userReply=userReply;

        comments.add(comment);

        Comment comment1=new Comment();
        comment1.time="昨天";
        comment1.comment="哇，好厉害！这是我家的风格......大家喜欢吗？喜欢我可以帮忙设计";
        comment1.images=images;
        comment1.user=user;
        comments.add(comment1);

        CommentAdapter commentAdapter=new CommentAdapter(mContext,comments);
        listView.setAdapter(commentAdapter);

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
        public View bindView(int position, View convertView, ViewGroup parent)
        {
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
            TextView commentReply= ViewHolder.get(convertView, R.id.commentInfoReplyTv);

            Comment comment=getList().get(position);
            ToolUtils.setImageCacheUrl(comment.user.getAvatar(), header, R.drawable.icon_loading_defalut);
            name.setText(comment.user.getNickName());
            time.setText(comment.time);

            commentImageLine.removeAllViews();
            commentImageFormerLine.removeAllViews();
            commentImageReplyLine.removeAllViews();

            if (comment.commentReply==null&comment.imagesReply.size()==0)
            {
                commentLine.setVisibility(View.VISIBLE);
                commentReplyLine.setVisibility(View.GONE);

                if (comment.images==null|comment.images.size()==0)
                {
                    commentImageLine.setVisibility(View.GONE);
                }
                else
                {
                    addImageView(commentImageLine,comment.images);
                }
                commentInfo.setText(comment.comment);
            }
            else
            {
                commentLine.setVisibility(View.GONE);
                commentReplyLine.setVisibility(View.VISIBLE);

                if (comment.images==null|comment.images.size()==0)
                {
                    commentImageFormerLine.setVisibility(View.GONE);
                }
                else
                {
                    addImageView(commentImageFormerLine,comment.images);
                }
                commentInfoFormer.setText(comment.comment);

                if (comment.imagesReply==null|comment.imagesReply.size()==0)
                {
                    commentImageReplyLine.setVisibility(View.GONE);
                }
                else
                {
                    addImageView(commentImageReplyLine,comment.imagesReply);
                }
                commentReply.setText("                         "+comment.commentReply);
            }
//            mHashMap.put(position, convertView);
            return convertView;
        }

        /**
         * 选择相片添加布局以及相关逻辑处理
         */
        private void addImageView(LinearLayout viewLayout, final List<String> ims)
        {
            final ArrayList<String> im=new ArrayList<String>();
            for (String key:ims)
            {
                im.add(key);
            }
            for (int i = 0; i < ims.size(); i++)
            {
                final int position=i;
                View mView = LayoutInflater.from(mContext).inflate(R.layout.item_comment_image, null);
                ImageView imageIv = ( ImageView ) mView.findViewById(R.id.imageBg_iv);
                mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ToolUtils.setLog("position:"+position+"点击了");
                        CommentImageFragment commentImageFragment=CommentImageFragment.newInstance(im,position);
                        ((MainActivity)mContext).navigationToFragmentWithAnim(commentImageFragment);

                    }
                });
                ToolUtils.setImageCacheUrl(ims.get(i), imageIv, R.drawable.icon_loading_defalut);
                viewLayout.addView(mView);
            }


        }
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
}
