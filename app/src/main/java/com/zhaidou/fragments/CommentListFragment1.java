package com.zhaidou.fragments;


import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
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
        ArrayList<Entity> list = new ArrayList<Entity>();
        commentListAdapter = new CommentListAdapter(mContext, list);
        listView.setAdapter(commentListAdapter);
        mDialogUtils = new DialogUtils(mContext);
        mDialogUtils.showLoadingDialog();
        fetchData(mCurrentPage = 1);
        commentListAdapter.setOnInViewClickListener(R.id.commentContainerLayout, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, final Integer position, Object values) {
                final Entity entity = (Entity) values;
                mDialogUtils.showListDialog(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                        Api.deleteComment(entity.comment.id, new Api.SuccessListener() {
                            @Override
                            public void onSuccess(Object object) {
                                if (object != null) {
                                    commentListAdapter.remove(position);
                                    ShowToast("删除成功");
                                }
                            }
                        }, null);
                    }
                });
            }
        });

//        commentListAdapter.setOnInViewClickListener(R.id.avatar, new BaseListAdapter.onInternalClickListener() {
//            @Override
//            public void OnClickListener(View parentV, View v, Integer position, Object values) {
//                System.out.println("parentV = [" + parentV + "], v = [" + v + "], position = [" + position + "], values = [" + values + "]");
//                showCommentDialog(values);
//            }
//        });
//        commentListAdapter.setOnInViewClickListener(R.id.username, new BaseListAdapter.onInternalClickListener() {
//            @Override
//            public void OnClickListener(View parentV, View v, Integer position, Object values) {
//                Entity entity = (Entity) values;
//                showCommentDialog(entity);
//            }
//        });
        commentListAdapter.setOnInViewClickListener(R.id.subject,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                System.out.println("parentV = [" + parentV + "], v = [" + v + "], position = [" + position + "], values = [" + values + "]");
                Entity entity= (Entity) values;
                HomeArticleGoodsDetailsFragment homeArticleGoodsDetailsFragment=HomeArticleGoodsDetailsFragment.newInstance("",entity.comment.articleId);
                ((BaseActivity)mContext).navigationToFragment(homeArticleGoodsDetailsFragment);
            }
        });
        listView.setEmptyView(inflater.inflate(R.layout.emptyview,null));
        return listView;
    }

    private void showCommentDialog(Object values) {
        final Entity entity = (Entity) values;
        final Comment comment = entity.comment;
        mDialogUtils.showCommentDialog(new DialogUtils.onCommentListener() {
            @Override
            public void onComment(Object object) {
                System.out.println("CommentListFragment1.onComment--->" + object);
                final Map<String, Object> params = (Map<String, Object>) object;
                params.put("commentUserId", comment.commentUserId);
                params.put("commentUserName", comment.commentUserName);
                params.put("articleId", comment.articleId);
                params.put("articleTitle", comment.articleTitle);
                params.put("commentType", comment.commentType);
                params.put("commentId", comment.id);
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
//                                        mHandler.sendEmptyMessage(UPDATE_UI_LIST);
                                        ShowToast("回复成功");
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
                startActivityForResult(intent, MENU_CAMERA_SELECTED);
            }

            @Override
            public void onPhoto() {
                System.out.println("CommentListFragment1.onPhoto");
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, MENU_PHOTO_SELECTED);
            }
        });
    }

    private void fetchData(int page) {
        Integer userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        Map<String, String> params = new HashMap<String, String>();
        params.put("commentType", mParam2);
        params.put("commentUserId", userId + "");
        params.put("pageSize", "15");
        params.put("pageNo", "" + page);
        ZhaiDouRequest request = new ZhaiDouRequest(mContext, Request.Method.POST, ZhaiDou.COMMENT_LIST_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("jsonObject = " + jsonObject);
                listView.onRefreshComplete();
                mDialogUtils.dismiss();
                int status = jsonObject.optInt("status");
                if (status == 200) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    JSONArray items = data.optJSONArray("items");
                    int totalCount = data.optInt("totalCount");
                    if (totalCount <15)
                        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    List<Entity> replays = new ArrayList<Entity>();
                    for (int i = 0; items != null && i < items.length(); i++) {
                        JSONObject itemObj = items.optJSONObject(i);
//                        Entity entity = JSON.parseObject(itemObj.toString(), Entity.class);
                        JSONObject commentObj = itemObj.optJSONObject("comment");
                        JSONObject reCommentObj = itemObj.optJSONObject("reComment");
                        Comment comment = null, reComment = null;
                        if (commentObj != null)
                            comment = JSON.parseObject(commentObj.toString(), Comment.class);
                        if (reCommentObj != null)
                            reComment = JSON.parseObject(reCommentObj.toString(), Comment.class);
                        Entity replay = new Entity(comment, reComment);
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


    private class CommentListAdapter extends BaseListAdapter<Entity> {

        public CommentListAdapter(Context context, List<Entity> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (contentView == null)
                convertView = mInflater.inflate(R.layout.item_comment_receive, null);
            ImageView mAvatar = ViewHolder.get(convertView, R.id.avatar);
            TextView mUserName = ViewHolder.get(convertView, R.id.username);
            TextView mTime = ViewHolder.get(convertView, R.id.time);
            GridView mGridView = ViewHolder.get(convertView, R.id.gridView);
            GridView mReGridView = ViewHolder.get(convertView, R.id.re_gridView);
            TextView mContent = ViewHolder.get(convertView, R.id.content);
            TextView mSubject = ViewHolder.get(convertView, R.id.subject);
            TextView mReplay = ViewHolder.get(convertView, R.id.reply);
            LinearLayout mCommentLayout = ViewHolder.get(convertView, R.id.commentLayout);
            LinearLayout mReCommentLayout = ViewHolder.get(convertView, R.id.reCommentLayout);
            TextView mTargetName=ViewHolder.get(convertView,R.id.targetName);
            Entity replay = getList().get(position);
            if (replay.comment != null) {
                ToolUtils.setImageCacheUrl(replay.comment.commentUserImg.contains("http") ? replay.comment.commentUserImg : "http://" + replay.comment.commentUserImg, mAvatar);
                mUserName.setText("我的评论");//replay.comment.commentUserName
                mSubject.setText(Html.fromHtml("来自<font color=#50c2bf>《" + replay.comment.articleTitle + "》</font>"));
                List<String> list=!TextUtils.isEmpty(replay.comment.imgMd5) ?
                        Arrays.asList(replay.comment.imgMd5.split(",")) : new ArrayList<String>();
                final ImageAdapter imageAdapter = new ImageAdapter(convertView.getContext(), list);
                mGridView.setAdapter(imageAdapter);
                mGridView.setVisibility(list.size()>0&&!"F".equalsIgnoreCase(replay.comment.status)?View.VISIBLE:View.GONE);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    mTime.setText(DateUtils.getDescriptionTimeFromTimestamp(sdf.parse(replay.comment.createTime)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//                mContent.setVisibility(replay.reComment!=null? VVISIBLE : View.GONE);
                    mContent.setText(Html.fromHtml(replay.comment.content));
                mContent.setVisibility(!TextUtils.isEmpty(replay.comment.content)?View.VISIBLE:View.GONE);
//                if (!TextUtils.isEmpty(replay.comment.content)) {
//                    mContent.setText(Html.fromHtml(replay.comment.content));
//                }
                imageAdapter.setOnInViewClickListener(R.id.imageView,new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Object values) {
                        System.out.println("parentV = [" + parentV + "], v = [" + v + "], position = [" + position + "], values = [" + values + "]");
                        List<String> images = imageAdapter.getList();
                        System.out.println("images = " + images);
                        Intent intent=new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra("images",images.toArray(new String[]{}));
                        intent.putExtra("position",position);
                        startActivity(intent);
                        System.out.println("imageAdapter = " + images);
                    }
                });
            } else {
                mCommentLayout.setVisibility(View.GONE);

            }
            if (replay.reComment != null) {
                mTargetName.setText(replay.reComment.commentUserName);
                List<String> reImageList=!TextUtils.isEmpty(replay.reComment.imgMd5) ?
                        Arrays.asList(replay.reComment.imgMd5.split(",")) : new ArrayList<String>();
                final ImageAdapter adapter = new ImageAdapter(convertView.getContext(), reImageList);
                mReGridView.setAdapter(adapter);
                mReGridView.setVisibility(reImageList.size()>0&&!"F".equalsIgnoreCase(replay.reComment.status)?View.VISIBLE:View.GONE);
                mReplay.setVisibility(!TextUtils.isEmpty(replay.reComment.content) ? View.VISIBLE : View.GONE);
                mReplay.setText(replay.reComment.content);
                adapter.setOnInViewClickListener(R.id.imageView,new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Object values) {
                        List<String> images = adapter.getList();
                        System.out.println("images = " + images);
                        Intent intent=new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra("images",images.toArray(new String[]{}));
                        intent.putExtra("position",position);
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
            ImageView imageView = new ImageView(mContext);
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
                    File file = new File(filePath);
                    startImageAction(Uri.fromFile(file), 200, 200, 2, true);
                }
                break;
            case MENU_PHOTO_SELECTED:// 本地修改头像
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uri = data.getData();
                    startImageAction(uri, 200, 200, 2, true);
                } else {
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
        file = new File(ZhaiDou.MyCommentDir, new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
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
        startActivityForResult(intent, requestCode);
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

    public class Entity {
        public Comment comment;
        public Comment reComment;

        public Entity() {
        }

        public Entity(Comment comment, Comment reComment) {
            this.comment = comment;
            this.reComment = reComment;
        }
    }
}
