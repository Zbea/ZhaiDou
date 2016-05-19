package com.zhaidou.fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

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

    private int num=5;


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


    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.commentCancelTv:
                    ((MainActivity) getActivity()).popToStack(CommentListFragment.this);
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
