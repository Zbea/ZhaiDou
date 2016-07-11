package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomPhotoMenuDialog;
import com.zhaidou.model.Comment;
import com.zhaidou.utils.Api;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by roy on 15/8/28.
 */
public class CommentSendFragment extends BaseFragment
{
    private static final String DATA = "page";
    private static final String INDEX = "index";
    private static final String COMMENT = "comment";

    private String mPage;
    private String mIndex;
    private Comment mComment,comment;
    private int mCommentId;
    private String mCommentContent;
    private String mCommentTime;
    private List<String> mCommentImages;
    private String mCommentUserName;
    private String mCommentUserImage;
    private int mCommentUserId;

    private Dialog mDialog;
    private final static int REFRESH_COMMIT_SUCCESS=0;//刷新列表
    private final static int REFRESH_COMMIT_FAIl=1;//刷新列表

    private View mView;
    private TextView backTv, sentTv,inputNumTv;
    private LinearLayout imageLine,cancelLine;
    private ImageView imageAddBtn;
    private TypeFaceEditText editText;

    private InputMethodManager inputMethodManagers;
    private List<Bitmap> photos=new ArrayList<Bitmap>();
    private List<String> files=new ArrayList<String>();
    private File file;
    private String pathUrl;
    private String commentInfo="";
    private int userId;
    private String userName;
    private OnCommentListener onCommentListener;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            mDialog.dismiss();
            switch (msg.what)
            {
                case REFRESH_COMMIT_SUCCESS:
                    ToolUtils.setLog("成功："+msg.obj.toString());
                    JSONObject jsonObject;
                    try
                    {
                        jsonObject=new JSONObject(msg.obj.toString());
                        int status=jsonObject.optInt("status");
                        String message=jsonObject.optString("message");
                        if (status!=200)
                        {
                            ToolUtils.setToastLong(mContext, message);
                        }
                        else
                        {

                            JSONObject dataJsonObject=jsonObject.optJSONObject("data");
                            if (dataJsonObject!=null)
                            {
                                JSONArray itemsArray=dataJsonObject.optJSONArray("items");
                                if (itemsArray!=null)
                                for (int i = 0; i < itemsArray.length(); i++)
                                {
                                    JSONObject object=itemsArray.optJSONObject(i);
                                    JSONObject commentJsonObject=object.optJSONObject("comment");
                                    if (commentJsonObject!=null)
                                    {
                                        int commentid=commentJsonObject.optInt("id");
                                        String commentTitle=commentJsonObject.optString("content");
                                        String commentUrl=commentJsonObject.optString("imgMd5");
                                        List<String> commentImgs=new ArrayList<String>();
                                        if (commentUrl.length()>0)
                                        {
                                            String[] commentUrls=commentUrl.split(",");
                                            for (int j = 0; j <commentUrls.length; j++)
                                            {
                                                commentImgs.add(commentUrls[j]);
                                            }
                                        }
                                        int commentUserId=commentJsonObject.optInt("commentUserId");
                                        String commentUserName=commentJsonObject.optString("commentUserName");
                                        String commentUserImg=(String)SharedPreferencesUtil.getData(mContext,"avatar","");
                                        String articleId=commentJsonObject.optString("articleId");
                                        String articleTitle=commentJsonObject.optString("articleTitle");
                                        String commentType=commentJsonObject.optString("commentType");
                                        String commentStatus=commentJsonObject.optString("status");
                                        String commentCreateTime= "";
                                        try
                                        {
                                            commentCreateTime = ToolUtils.getDateDiff(commentJsonObject.optString("createTime"));
                                        } catch (ParseException e)
                                        {
                                            e.printStackTrace();
                                        }
                                        ToolUtils.setLog(""+commentImgs.size());
                                        comment=new Comment();
                                        if (mComment==null)
                                        {
                                            comment.articleId=articleId;
                                            comment.articleTitle=articleTitle;
                                            comment.id=commentid;
                                            comment.time=commentCreateTime;
                                            comment.comment=commentTitle;
                                            comment.images=commentImgs;
                                            comment.type=commentType;
                                            comment.status=commentStatus;
                                            comment.userName=commentUserName;
                                            comment.userImage=commentUserImg;
                                            comment.userId=commentUserId;
                                        }
                                        else
                                        {
                                            //将返回的信息当初回复信息
                                            comment.id=commentid;
                                            comment.time=commentCreateTime;
                                            comment.comment=commentTitle;
                                            comment.images=commentImgs;
                                            comment.type=commentType;
                                            comment.status=commentStatus;
                                            comment.userName=commentUserName;
                                            comment.userImage=commentUserImg;
                                            comment.userId=commentUserId;

                                            comment.articleId=mIndex;
                                            comment.articleTitle=mPage;

                                            comment.idReply=mCommentId;
                                            comment.timeReply=mCommentTime;
                                            comment.commentReply=mCommentContent;
                                            comment.imagesReply= mCommentImages;
                                            comment.typeReply="A";
                                            comment.statusReply="N";
                                            comment.userIdReply=mCommentUserId;
                                            comment.userNameReply=mCommentUserName;
                                            comment.userImageReply=mCommentUserImage;

                                        }
                                        onCommentListener.onCommentResult(comment);
                                        ((BaseActivity) getActivity()).popToStack(CommentSendFragment.this);
                                        if (editText!=null)
                                            closeInput();
                                    }
                                }

                            }
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case REFRESH_COMMIT_FAIl:
                    ToolUtils.setToastLong(mContext, "抱歉,评论失败");
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
                case R.id.commentCancelLine:
                    ((BaseActivity) getActivity()).popToStack(CommentSendFragment.this);
                    if (editText!=null)
                        closeInput();
                    break;
                case R.id.commentCancelTv:
                    ((BaseActivity) getActivity()).popToStack(CommentSendFragment.this);
                    if (editText!=null)
                        closeInput();
                    break;
                case R.id.commentOkTv:
                    commitComment();
                    break;
                case R.id.comment_image_add:
//                    menuView.setVisibility(View.VISIBLE);
//                    toggleMenu();
                    File dir = new File(ZhaiDou.MyCommentDir);
                    if (!dir.exists())
                    {
                        dir.mkdirs();
                    }
                    pathUrl=ZhaiDou.MyCommentDir+"cm"+new SimpleDateFormat("yyMMddHHmmss").format(new Date())+".jpg";
                    file = new File(pathUrl);
                    CustomPhotoMenuDialog customPhotoMenuDialog =new CustomPhotoMenuDialog(mContext,pathUrl);
                    customPhotoMenuDialog.showPhotoMenuDialog();
                    break;

            }
        }
    };

    public static CommentSendFragment newInstance(String page, String index,Comment comment)
    {
        CommentSendFragment fragment = new CommentSendFragment();
        Bundle args = new Bundle();
        args.putString(DATA, page);
        args.putString(INDEX, index);
        args.putSerializable(COMMENT, comment);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentSendFragment()
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
            mComment = (Comment) getArguments().getSerializable(COMMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        if (mView == null)
        {
            mView = inflater.inflate(R.layout.fragment_comment_page, container, false);
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
        if (mComment!=null)
        {
            mCommentId=mComment.id;
            mCommentContent=mComment.comment;
            mCommentImages=mComment.images;
            mCommentUserName=mComment.userName;
            mCommentUserImage=mComment.userImage;
            mCommentUserId=mComment.userId;
            mCommentTime=mComment.time;
        }
        inputMethodManagers=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        userId= (Integer)SharedPreferencesUtil.getData(mContext,"userId",0);
        userName= (String)SharedPreferencesUtil.getData(mContext,"nickName","");

        backTv = (TextView) mView.findViewById(R.id.commentCancelTv);
        backTv.setOnClickListener(onClickListener);

        sentTv = (TextView) mView.findViewById(R.id.commentOkTv);
        sentTv.setClickable(false);

        editText=(TypeFaceEditText)mView.findViewById(R.id.comment_edit);
        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                commentInfo=s.toString();
                commentInfo=commentInfo.replaceAll("\\s*","");
                listenerCommitBtn();
                inputNumTv.setText((200-commentInfo.length())+"");
            }
            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
//        editText.setFocusable(true);
//        editText.setFocusableInTouchMode(true);
//        editText.requestFocus();
//        forceOpenSoftKeyboard(mContext);

        inputNumTv= (TextView) mView.findViewById(R.id.inputNumTv);

        imageLine = (LinearLayout) mView.findViewById(R.id.comment_image_line);

        imageAddBtn = (ImageView) mView.findViewById(R.id.comment_image_add);
        imageAddBtn.setOnClickListener(onClickListener);

        cancelLine = (LinearLayout) mView.findViewById(R.id.commentCancelLine);
        cancelLine.setOnClickListener(onClickListener);

    }

    private void commitComment()
    {
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"");
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("commentUserId", userId);
        params.put("commentUserName", userName);
        params.put("articleId", mIndex+"");
        params.put("commentType", "C");
        params.put("articleTitle", mPage+"");
        params.put("commentId", mCommentId);
        params.put("content", commentInfo+"");
        params.put("images", files);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Api.comment(params, new Api.SuccessListener()
                {
                    @Override
                    public void onSuccess(Object object)
                    {
                        mHandler.obtainMessage(REFRESH_COMMIT_SUCCESS, object).sendToTarget();
                    }
                }, new Api.ErrorListener()
                {
                    @Override
                    public void onError(Object object)
                    {
                        mHandler.sendEmptyMessage(REFRESH_COMMIT_FAIl);
                    }
                });
            }
        }).start();

    }

    /**
     * 监听是否可以提交回复
     */
    private void listenerCommitBtn()
    {
        if (commentInfo != null)
        {
            if (commentInfo.length() > 0|photos.size()>0)
            {
                sentTv.setTextColor(getResources().getColor(R.color.green_color));
                sentTv.setClickable(true);
                sentTv.setOnClickListener(onClickListener);
            } else
            {
                sentTv.setTextColor(getResources().getColor(R.color.text_gary_color));
                sentTv.setClickable(false);
            }
        } else
        {
            if (photos.size()>0)
            {
                sentTv.setTextColor(getResources().getColor(R.color.green_color));
                sentTv.setClickable(true);
                sentTv.setOnClickListener(onClickListener);
            }
            else
            {
                sentTv.setTextColor(getResources().getColor(R.color.text_gary_color));
                sentTv.setClickable(false);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case CustomPhotoMenuDialog.MENU_CAMERA_SELECTED:// 拍照修改头像
                if (resultCode == getActivity().RESULT_OK)
                {
                    if (!ToolUtils.hasSdcard())
                    {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startImageAction(Uri.fromFile(file), 250, 250, 2, true);
                }
                break;
            case CustomPhotoMenuDialog.MENU_PHOTO_SELECTED:// 本地修改头像
                Uri uri = null;
                if (data == null)
                {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK)
                {
                    if (!ToolUtils.hasSdcard())
                    {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uri = data.getData();
                    startImageAction(uri, 250,250, 2, true);
                } else
                {
                    Toast.makeText(getActivity(), "照片获取失败", Toast.LENGTH_SHORT).show();
                }

                break;
            case 2:// 裁剪头像返回
                if (data == null)
                {
                    Toast.makeText(getActivity(), "取消选择", Toast.LENGTH_SHORT).show();
                    return;
                } else
                {
                  saveCropPhoto(data);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 剪裁
     *
     * @param uri
     * @param outputX
     * @param outputY
     * @param requestCode
     * @param isCrop
     */
    private void startImageAction(Uri uri, int outputX, int outputY, int requestCode, boolean isCrop)
    {
        Intent intent = null;
        if (isCrop)
        {
            intent = new Intent("com.android.camera.action.CROP");
        } else
        {
            intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", outputX);
//        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
        ToolUtils.setLog("检测");
    }

    /**
     * 保存裁剪的头像
     * @param data
     */
    private void saveCropPhoto(Intent data)
    {
        Bundle extras = data.getExtras();
        Bitmap photo = null;
        if (extras != null)
        {
            photo = extras.getParcelable("data");
            if (photo == null)
            {
                photo = BitmapFactory.decodeFile(pathUrl);
            }
            addImageView(photo);
        }
        else
        {
            photo = BitmapFactory.decodeFile(pathUrl);
            addImageView(photo);
        }
    }

    /**
     * 选择相片添加布局以及相关逻辑处理
     *
     * @param photo
     */
    private void addImageView(final Bitmap photo)
    {
        files.add(pathUrl);
        photos.add(photo);
        final View mView = LayoutInflater.from(mContext).inflate(R.layout.item_image_crop, null);
        ImageView imageIv = ( ImageView ) mView.findViewById(R.id.imageBg_iv);
        imageIv.setImageBitmap(photo);
        ImageView clearIv = ( ImageView ) mView.findViewById(R.id.imageClear_iv);
        clearIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clearPhotoImage(photo,file);
                imageLine.removeView(mView);
            }
        });
        imageLine.addView(mView);
        if (photos.size()>3)
        {
            imageAddBtn.setVisibility(View.GONE);
        }
        listenerCommitBtn();
    }

    /**
     * 删除选择的图片以及file文件
     * @param photo
     * @param file
     */
    private void clearPhotoImage(Bitmap photo,File file)
    {
        files.remove(file);
        photos.remove(photo);
        if (photos.size()<3)
        {
            imageAddBtn.setVisibility(View.VISIBLE);
        }
        listenerCommitBtn();
    }


    public void setOnCommentListener(OnCommentListener onCommentListener)
    {
        this.onCommentListener=onCommentListener;
    }

    public interface OnCommentListener
    {
        public void onCommentResult(Comment comment);
    }

    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("评论发送");
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("评论发送");
    }

    public  void forceOpenSoftKeyboard(Context context)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void closeInput()
    {
        if (editText!=null)
        {
            if (inputMethodManagers.isActive())
                inputMethodManagers.hideSoftInputFromWindow(editText.getWindowToken(),0);
        }
    }

    @Override
    public void onDestroy()
    {
            closeInput();
        super.onDestroy();
    }
}
