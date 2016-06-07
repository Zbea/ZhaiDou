package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.zhaidou.utils.Api;
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

public class CommentListFragment1 extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView> {

    private static final String ARG_INDEX = "index";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mIndex;
    private String mParam2;
    private CommentListAdapter commentListAdapter;
    private PullToRefreshListView listView;

    private int mCurrentPage;
    private DialogUtils mDialogUtils;

    public static CommentListFragment1 newInstance(int index, String param2) {
        CommentListFragment1 fragment = new CommentListFragment1();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentListFragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_INDEX);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        listView = new PullToRefreshListView(mContext);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(this);
        ArrayList<Comment> list = new ArrayList<Comment>();
        commentListAdapter = new CommentListAdapter(mContext, list);
        listView.setAdapter(commentListAdapter);
        mDialogUtils = new DialogUtils(mContext);
        mDialogUtils.showLoadingDialog();
        fetchData(mCurrentPage = 1);
        commentListAdapter.setOnInViewLongClickListener(R.id.commemtLayout, new BaseListAdapter.onInternalLongClickListener() {
            @Override
            public boolean OnLongClickListener(View parentV, View v, final Integer position, Object values) {
                mDialogUtils.showListDialog(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                        Api.deleteComment(new Api.SuccessListener() {
                            @Override
                            public void onSuccess(Object object) {
                                if (object!=null){
                                    commentListAdapter.remove(position);
                                    ShowToast("删除成功");
                                }
                            }
                        },null);
                    }
                });
                return true;
            }
        });

        return listView;
    }

    private void fetchData(int page) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("commentType", mParam2);
        params.put("commentUserId", "28822");
        params.put("pageSize", "20");
        params.put("pageNo", "" + page);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext, Request.Method.POST, "http://tportal-web.zhaidou.com/comment/getCommentList.action", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("jsonObject = " + jsonObject);
                listView.onRefreshComplete();
                mDialogUtils.dismiss();
                int status = jsonObject.optInt("status");
                if (status == 200) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    int totalCount = data.optInt("totalCount");
                    if (totalCount < 20)
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    JSONArray items = data.optJSONArray("items");
                    List<Comment> commentList=new ArrayList<Comment>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject commentObj = items.optJSONObject(i).optJSONObject("comment");
                        Comment comments = JSON.parseObject(commentObj.toString(), Comment.class);
                        commentList.add(comments);
                    }
                    commentListAdapter.addAll(commentList);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication) mContext.getApplicationContext()).mRequestQueue.add(request);
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


    private class CommentListAdapter extends BaseListAdapter<Comment> {

        public CommentListAdapter(Context context, List<Comment> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (contentView == null)
                convertView = mInflater.inflate(R.layout.fragment_comment_list_fragment1, null);
            ImageView mAvatar = ViewHolder.get(convertView, R.id.avatar);
            TextView mUserName = ViewHolder.get(convertView, R.id.username);
            TextView mTime = ViewHolder.get(convertView, R.id.time);
            GridView mGridView = ViewHolder.get(convertView, R.id.gridView);
            TextView mContent = ViewHolder.get(convertView, R.id.content);
            TextView mSubject = ViewHolder.get(convertView, R.id.subject);
            Comment comment = getList().get(position);
            ToolUtils.setImageCacheUrl(comment.commentUserImg.contains("http") ? comment.commentUserImg : "http://" + comment.commentUserImg, mAvatar);
            mUserName.setText(comment.commentUserName);
            mSubject.setText(Html.fromHtml("来自<font color=#50c2bf>《" + comment.articleTitle + "》</font>"));
            mGridView.setAdapter(new ImageAdapter(mContext, Arrays.asList(comment.imgMd5.split(","))));
            mContent.setText(comment.content);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                mTime.setText(DateUtils.getDescriptionTimeFromTimestamp(sdf.parse(comment.createTime).getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    private class ImageAdapter extends BaseListAdapter<String> {

        public ImageAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);
            imageView.setLayoutParams(new AbsListView.LayoutParams(DeviceUtils.dp2px(mContext, 70), DeviceUtils.dp2px(mContext, 70)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String url = getList().get(position);
            ToolUtils.setImageCacheUrl(url, imageView);
            return imageView;
        }
    }

}
