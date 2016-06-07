package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Comment;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DateUtils;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link com.zhaidou.fragments.ReplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReplayFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView>{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private CommentListAdapter commentListAdapter;

    private int mCurrentPage;
    private PullToRefreshListView listView;
    private DialogUtils mDialogUtils;

    public static ReplayFragment newInstance(String param1, String param2) {
        ReplayFragment fragment = new ReplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ReplayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new PullToRefreshListView(mContext);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(this);
        ArrayList<Replay> list = new ArrayList<Replay>();
        commentListAdapter = new CommentListAdapter(mContext, list);
        listView.setAdapter(commentListAdapter);
        mDialogUtils = new DialogUtils(mContext);
        mDialogUtils.showLoadingDialog();
        fetchData(mCurrentPage = 1);
        return listView;
    }

    private void fetchData(int page) {
        System.out.println("page = " + page);
        Map<String,String> params=new HashMap<String, String>();
        params.put("commentType",mParam2);
        params.put("commentUserId", "28822");
        params.put("pageSize", "20");
        params.put("pageNo", ""+page);
        ZhaiDouRequest request=new ZhaiDouRequest(mContext, Request.Method.POST,"http://tportal-web.zhaidou.com/comment/getCommentList.action",params,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("jsonObject = " + jsonObject);
                mDialogUtils.dismiss();
                listView.onRefreshComplete();
                int status = jsonObject.optInt("status");
                if (status==200){
                    JSONObject data = jsonObject.optJSONObject("data");
                    JSONArray items = data.optJSONArray("items");
                    int totalCount = data.optInt("totalCount");
                    if (totalCount<20)
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    List<Replay> replays=new ArrayList<Replay>();
                    for (int i = 0;items!=null&&i < items.length(); i++) {
                        JSONObject itemObj = items.optJSONObject(i);
                        JSONObject commentObj = itemObj.optJSONObject("comment");
                        JSONObject reCommentObj = itemObj.optJSONObject("reComment");
                        Comment comment = JSON.parseObject(commentObj.toString(), Comment.class);
                        Comment reComment = JSON.parseObject(reCommentObj.toString(), Comment.class);
                        Replay replay=new Replay(comment,reComment);
                        replays.add(replay);
                    }
                    commentListAdapter.addAll(replays);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication)mContext.getApplicationContext()).mRequestQueue.add(request);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        commentListAdapter.clear();
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        fetchData(mCurrentPage = 1);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchData(++mCurrentPage);
    }

    private class CommentListAdapter extends BaseListAdapter<Replay> {

        public CommentListAdapter(Context context, List<Replay> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_comment_receive, null);
            ImageView mAvatar = ViewHolder.get(convertView, R.id.avatar);
            TextView mUserName = ViewHolder.get(convertView, R.id.username);
            TextView mTime = ViewHolder.get(convertView, R.id.time);
            GridView mGridView = ViewHolder.get(convertView, R.id.gridView);
            GridView mReGridView = ViewHolder.get(convertView, R.id.re_gridView);
            TextView mContent = ViewHolder.get(convertView, R.id.content);
            TextView mSubject = ViewHolder.get(convertView, R.id.subject);
            TextView mReplay=ViewHolder.get(convertView,R.id.reply);
            Replay replay = getList().get(position);
            ToolUtils.setImageCacheUrl(replay.comment.commentUserImg.contains("http") ? replay.comment.commentUserImg : "http://" + replay.comment.commentUserImg, mAvatar);
            mUserName.setText(replay.reComment.commentUserName);
            mSubject.setText(Html.fromHtml("来自<font color=#50c2bf>《" + replay.comment.articleTitle + "》</font>"));
            mGridView.setAdapter(new ImageAdapter(convertView.getContext(), !TextUtils.isEmpty(replay.reComment.imgMd5) ?
                    Arrays.asList(replay.reComment.imgMd5.split(",")) : new ArrayList<String>()));
            mReGridView.setAdapter(new ImageAdapter(convertView.getContext(),!TextUtils.isEmpty(replay.comment.imgMd5)?
                    Arrays.asList(replay.comment.imgMd5.split(",")):new ArrayList<String>()));
            mContent.setText(Html.fromHtml("<font color=#50c2bf>回复我的</font>   "+replay.reComment.content));
            mReplay.setText(replay.comment.content);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                mTime.setText(DateUtils.getDescriptionTimeFromTimestamp(sdf.parse(replay.reComment.createTime).getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    private class ImageAdapter extends BaseListAdapter<String>{

        public ImageAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            ImageView imageView=new ImageView(mContext);
            imageView.setLayoutParams(new AbsListView.LayoutParams( DeviceUtils.dp2px(mContext, 70), DeviceUtils.dp2px(mContext,70)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String url = getList().get(position);
            ToolUtils.setImageCacheUrl(url,imageView);
            return imageView;
        }
    }


    public class Replay{
        public Comment comment;
        public Comment reComment;

        public Replay(Comment comment, Comment reComment) {
            this.comment = comment;
            this.reComment = reComment;
        }
    }
}
