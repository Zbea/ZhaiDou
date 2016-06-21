package com.zhaidou.fragments;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.PhotoViewActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Comment;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.DateUtils;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.PhotoUtil;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplayFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView> {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private CommentListAdapter commentListAdapter;

    private int mCurrentPage;
    private PullToRefreshListView listView;
    private DialogUtils mDialogUtils;
    private String filePath = "";
    private final int MENU_CAMERA_SELECTED = 0;
    private final int MENU_PHOTO_SELECTED = 1;
    private File file;

    private final int UPDATE_UI_LIST = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI_LIST:
                    commentListAdapter.clear();
                    listView.setMode(PullToRefreshBase.Mode.BOTH);
                    fetchData(mCurrentPage = 1);
                    break;
            }
        }
    };

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
        System.out.println("ReplayFragment.onCreateView");
        if (null != listView) {
            ViewGroup parent = (ViewGroup) listView.getParent();
            if (null != parent) {
                parent.removeView(listView);
            }
        } else {
            listView = new PullToRefreshListView(mContext);
            listView.setMode(PullToRefreshBase.Mode.BOTH);
            listView.setOnRefreshListener(this);
            ArrayList<Replay> list = new ArrayList<Replay>();
            commentListAdapter = new CommentListAdapter(mContext, list);
            listView.setAdapter(commentListAdapter);
            mDialogUtils = new DialogUtils(mContext);
            mDialogUtils.showLoadingDialog();
            fetchData(mCurrentPage = 1);
            commentListAdapter.setOnInViewClickListener(R.id.commentContainerLayout, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
//                    Replay replay= (Replay) values;
//                    if (replay.reComment!=null){
//                        Integer userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", 0);
//                        if (userId==replay.reComment.commentUserId){
//                            Toast.makeText(mContext,"自己不能评论自己哦",Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    }
                    showCommentDialog(values);
                }
            });
            commentListAdapter.setOnInViewClickListener(R.id.subject, new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    Replay entity = (Replay) values;
                    HomeArticleGoodsDetailsFragment homeArticleGoodsDetailsFragment = HomeArticleGoodsDetailsFragment.newInstance("", entity.comment.articleId);
                    ((BaseActivity) mContext).navigationToFragment(homeArticleGoodsDetailsFragment);
                }
            });
            listView.setEmptyView(inflater.inflate(R.layout.emptyview, null));
        }
        return listView;
    }

    private void showCommentDialog(Object values) {
        final Replay replay = (Replay) values;
        mDialogUtils.showCommentDialog(new DialogUtils.onCommentListener() {
            @Override
            public void onComment(Object object) {
                System.out.println("CommentListFragment1.onComment--->" + object);
                final Map<String, Object> params = (Map<String, Object>) object;
                String userId = SharedPreferencesUtil.getData(mContext, "userId", -1) + "";
                String nickName = (String) SharedPreferencesUtil.getData(mContext, "nickName", "");
                System.out.println("replay.reComment.articleTitle = " + replay.reComment.articleTitle);
                params.put("commentUserId", userId);
                params.put("commentUserName", nickName);
                params.put("articleId", replay.comment.articleId);
                params.put("commentType", replay.comment.commentType);
                params.put("articleTitle", new String(replay.comment.articleTitle));
                params.put("commentId", replay.comment.id);
                mDialogUtils.showLoadingDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Api.comment(params, new Api.SuccessListener() {
                            @Override
                            public void onSuccess(Object object) {
                                mDialogUtils.dismiss();
                                JSONObject jsonObject = (JSONObject) object;
                                if (jsonObject != null) {
                                    int status = jsonObject.optInt("status");
                                    if (status == 200) {
                                        mHandler.sendEmptyMessage(UPDATE_UI_LIST);
//                                        ShowToast("回复成功");
                                    }
                                }

                                System.out.println("onSuccess---object = " + object);
                            }
                        }, new Api.ErrorListener() {
                            @Override
                            public void onError(Object object) {
                                System.out.println("onError----object = " + object);
                            }
                        });
                    }
                }).start();
            }
        }, new DialogUtils.PickerListener() {
            @Override
            public void onCamera() {
                System.out.println("CommentListFragment1.onCamera");
                File dir = new File(ZhaiDou.MyCommentDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 原图
                File file = new File(dir, "cc" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
                filePath = file.getAbsolutePath();// 获取相片的保存路径
                Uri imageUri = Uri.fromFile(file);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                getParentFragment().startActivityForResult(intent, MENU_CAMERA_SELECTED);
            }

            @Override
            public void onPhoto() {
                System.out.println("CommentListFragment1.onPhoto");
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                getParentFragment().startActivityForResult(intent1, MENU_PHOTO_SELECTED);
            }
        });
    }

    private void fetchData(int page) {
        Integer userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        System.out.println("page = " + page);
        Map<String, String> params = new HashMap<String, String>();
        params.put("commentType", mParam2);
        params.put("commentUserId", userId + "");
        params.put("pageSize", "20");
        params.put("pageNo", "" + page);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext, Request.Method.POST, ZhaiDou.COMMENT_LIST_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("jsonObject = " + jsonObject);
                mDialogUtils.dismiss();
                listView.onRefreshComplete();
                int status = jsonObject.optInt("status");
                if (status == 200) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    JSONArray items = data.optJSONArray("items");
                    int totalCount = data.optInt("totalCount");
                    if (totalCount < 20)
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    List<Replay> replays = new ArrayList<Replay>();
                    for (int i = 0; items != null && i < items.length(); i++) {
                        JSONObject itemObj = items.optJSONObject(i);
                        JSONObject commentObj = itemObj.optJSONObject("comment");
                        JSONObject reCommentObj = itemObj.optJSONObject("reComment");
                        Comment comment = JSON.parseObject(commentObj.toString(), Comment.class);
                        Comment reComment = JSON.parseObject(reCommentObj.toString(), Comment.class);
                        Replay replay = new Replay(comment, reComment);
                        replays.add(replay);
                    }
                    commentListAdapter.addAll(replays);
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
            final TextView mTime = ViewHolder.get(convertView, R.id.time);
            GridView mGridView = ViewHolder.get(convertView, R.id.gridView);
            GridView mReGridView = ViewHolder.get(convertView, R.id.re_gridView);
            TextView mContent = ViewHolder.get(convertView, R.id.content);
            TextView mSubject = ViewHolder.get(convertView, R.id.subject);
            TextView mReplay = ViewHolder.get(convertView, R.id.reply);
            LinearLayout mCommentLayout = ViewHolder.get(convertView, R.id.commentLayout);
            LinearLayout mReCommentLayout = ViewHolder.get(convertView, R.id.reCommentLayout);
            final Replay replay = getList().get(position);
            if (replay.comment != null) {
                ToolUtils.setImageCacheUrl(replay.comment.commentUserImg.contains("http") ? replay.comment.commentUserImg : "http://" + replay.comment.commentUserImg, mAvatar);
                mUserName.setText(replay.comment.commentUserName);
                mSubject.setText(Html.fromHtml("来自<font color=#50c2bf>《" + replay.comment.articleTitle + "》</font>"));
                List<String> list = !TextUtils.isEmpty(replay.comment.imgMd5) ?
                        Arrays.asList(replay.comment.imgMd5.split(",")) : new ArrayList<String>();
                final ImageAdapter adapter = new ImageAdapter(convertView.getContext(), list);
                mGridView.setAdapter(adapter);
                mGridView.setVisibility(list.size() > 0 && !"F".equalsIgnoreCase(replay.comment.status) ? View.VISIBLE : View.GONE);
                adapter.setOnInViewClickListener(R.id.imageView, new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Object values) {
                        List<String> images = adapter.getList();
                        Intent intent = new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra("images", images.toArray(new String[]{}));
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }
                });
//                mContent.setVisibility(!TextUtils.isEmpty(replay.comment.content) ? View.VISIBLE : View.GONE);
                mContent.setText(Html.fromHtml("<font color=#50c2bf>回复我的</font>   " + replay.comment.content));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    mTime.setText(DateUtils.getDescriptionTimeFromTimestamp(sdf.parse(replay.comment.createTime)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                mCommentLayout.setVisibility(View.GONE);
            }
            if (replay.reComment != null) {
                List<String> imageList = !TextUtils.isEmpty(replay.reComment.imgMd5) ?
                        Arrays.asList(replay.reComment.imgMd5.split(",")) : new ArrayList<String>();
                final ImageAdapter adapter = new ImageAdapter(convertView.getContext(), imageList);
                mReGridView.setAdapter(adapter);
                mReGridView.setVisibility(imageList.size() > 0 && !"F".equalsIgnoreCase(replay.reComment.status) ? View.VISIBLE : View.GONE);
                mReplay.setText(replay.reComment.content);
                mReplay.setVisibility(!TextUtils.isEmpty(replay.reComment.content) ? View.VISIBLE : View.GONE);
                adapter.setOnInViewClickListener(R.id.imageView, new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Object values) {
                        List<String> images = adapter.getList();
                        Intent intent = new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra("images", images.toArray(new String[]{}));
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }
                });
            } else {
                mReCommentLayout.setVisibility(View.GONE);
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
            ImageView imageView = new ImageView(mContext);//DeviceUtils.dp2px(mContext, 70)
            imageView.setId(R.id.imageView);
            imageView.setLayoutParams(new AbsListView.LayoutParams(DeviceUtils.dp2px(mContext, 60), DeviceUtils.dp2px(mContext, 60)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String url = getList().get(position);
            ToolUtils.setImageCacheUrl(url, imageView);
            return imageView;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MENU_CAMERA_SELECTED:// 拍照修改头像
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Uri uri = null;
                    if (data == null) {
                        return;
                    }
                    uri = data.getData();
                    try {
                        Bitmap bm = null;
                        ContentResolver resolver = getActivity().getContentResolver();
                        bm = MediaStore.Images.Media.getBitmap(resolver, uri);
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().managedQuery(uri, proj, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        String path = cursor.getString(column_index);
                        System.out.println("path = " + path);
//                        String bitmap = PhotoUtil.saveBitmap(bm);
//                        System.out.println("bitmap = " + bitmap);
                        mDialogUtils.notifyPhotoAdapter(path);
                    } catch (Exception e) {

                    }
//                    File file = new File(filePath);
//                    startImageAction(Uri.fromFile(file), 200, 200, 2, true);
                }
                break;
            case MENU_PHOTO_SELECTED:// 本地修改头像
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK) {
                    System.out.println("ReplayFragment.onActivityResult---RESULT_OK");
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        System.out.println("ReplayFragment.onActivityResult!Environment.getExternalStorageState().equals");
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uri = data.getData();
                    try {
                        Bitmap bm = null;
                        ContentResolver resolver = getActivity().getContentResolver();
                        bm = MediaStore.Images.Media.getBitmap(resolver, uri);
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().managedQuery(uri, proj, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        String path = cursor.getString(column_index);
                        System.out.println("path = " + path);
//                        String bitmap = PhotoUtil.saveBitmap(bm);
//                        System.out.println("bitmap = " + bitmap);
                        mDialogUtils.notifyPhotoAdapter(path);
                    } catch (Exception e) {

                    }
//                    startImageAction(uri, 200, 200, 2, true);
//                    saveCropPhoto(data);
                } else {
                    System.out.println("ReplayFragment.onActivityResult---->else");
                    Toast.makeText(getActivity(), "照片获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:// 裁剪头像返回
                if (data == null) {
                    Toast.makeText(getActivity(), "取消选择", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    saveCropPhoto(data);
                }
                // 初始化文件路径
                filePath = "";
                break;
            default:
                break;
        }
    }

    private void startImageAction(Uri uri, int outputX, int outputY, int requestCode, boolean isCrop) {
        System.out.println("ReplayFragment.startImageAction");
        file = new File(ZhaiDou.MyCommentDir, new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
        System.out.println("file = " + file.getAbsolutePath());
        Intent intent = null;
        if (isCrop) {
            intent = new Intent("com.android.camera.action.CROP");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        getParentFragment().startActivityForResult(intent, requestCode);
    }

    private void saveCropPhoto(Intent data) {
        Bundle extras = data.getExtras();
        System.out.println("CommentListFragment1.saveCropPhoto-------->" + extras);
        System.out.println("filePath = " + filePath);
        Bitmap photo = null;
        if (extras != null) {
            photo = extras.getParcelable("data");
            if (photo == null) {
                photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + filePath);
            }
            System.out.println("photo.getRowBytes() = " + photo.getRowBytes());
            String bitmap = PhotoUtil.saveBitmap(photo);
            System.out.println("bitmap = " + bitmap);
            mDialogUtils.notifyPhotoAdapter(bitmap);
//            addImageView(photo);
        }

    }

    public class Replay {
        public Comment comment;
        public Comment reComment;

        public Replay(Comment comment, Comment reComment) {
            this.comment = comment;
            this.reComment = reComment;
        }
    }
}
