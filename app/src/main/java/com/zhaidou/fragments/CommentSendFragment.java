package com.zhaidou.fragments;


import android.app.Dialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
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
import com.zhaidou.model.Comment;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceEditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by roy on 15/8/28.
 */
public class CommentSendFragment extends BaseFragment implements PhotoMenuFragment.MenuSelectListener
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
    private final int MENU_CAMERA_SELECTED = 0;
    private final int MENU_PHOTO_SELECTED = 1;
    private final static int REFRESH_COMMIT_SUCCESS=2;//刷新列表
    private final static int REFRESH_COMMIT_FAIl=3;//刷新列表

    private View mView;
    private FrameLayout menuView;
    private TextView backTv, sentTv;
    private LinearLayout imageLine,cancelLine;
    private ImageView imageAddBtn;
    private TypeFaceEditText editText;

    private InputMethodManager inputMethodManagers;
    private PhotoMenuFragment menuFragment;
    private String filePath = "";
    private List<Bitmap> photos=new ArrayList<Bitmap>();
    private List<File> files=new ArrayList<File>();
    private File file;
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
                    menuView.setVisibility(View.VISIBLE);
                    toggleMenu();
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
        sentTv.setOnClickListener(onClickListener);

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

        imageLine = (LinearLayout) mView.findViewById(R.id.comment_image_line);

        imageAddBtn = (ImageView) mView.findViewById(R.id.comment_image_add);
        imageAddBtn.setOnClickListener(onClickListener);

        cancelLine = (LinearLayout) mView.findViewById(R.id.commentCancelLine);
        cancelLine.setOnClickListener(onClickListener);

        menuView = (FrameLayout) mView.findViewById(R.id.commentMenuLayout);
        menuFragment = PhotoMenuFragment.newInstance("", "");
        menuFragment.setMenuSelectListener(this);
        getChildFragmentManager().beginTransaction().replace(R.id.commentMenuLayout, menuFragment).addToBackStack("").hide(menuFragment).commit();

    }

    private void commitComment()
    {
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String result=postSer();
                    if (result!=null&&result.length()>0)
                    {
                        mHandler.obtainMessage(REFRESH_COMMIT_SUCCESS, result).sendToTarget();
                    }
                    else
                    {
                        mHandler.sendEmptyMessage(REFRESH_COMMIT_FAIl);
                    }

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
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
            }
            else
            {
                sentTv.setTextColor(getResources().getColor(R.color.text_gary_color));
                sentTv.setClickable(false);
            }
        }
    }

    @Override
    public void onMenuSelect(int position, String tag)
    {
        switch (position)
        {
            case 0:
                File dir = new File(ZhaiDou.MyCommentDir);
                if (!dir.exists())
                {
                    dir.mkdirs();
                }
                // 原图
                File file = new File(dir, "cc"+new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
                filePath = file.getAbsolutePath();// 获取相片的保存路径
                Uri imageUri = Uri.fromFile(file);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                this.startActivityForResult(intent, MENU_CAMERA_SELECTED);
                break;

            case 1:
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                this.startActivityForResult(intent1, MENU_PHOTO_SELECTED);
                break;
        }
        toggleMenu();
    }

    public void toggleMenu()
    {
        if (menuFragment != null)
        {
            if (menuFragment.isHidden())
            {
                getChildFragmentManager().beginTransaction().show(menuFragment).commit();
            } else
            {
                getChildFragmentManager().beginTransaction().hide(menuFragment).commit();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case MENU_CAMERA_SELECTED:// 拍照修改头像
                if (resultCode == getActivity().RESULT_OK)
                {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED))
                    {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File file = new File(filePath);
                    startImageAction(Uri.fromFile(file), 200, 200, 2, true);
                }
                break;
            case MENU_PHOTO_SELECTED:// 本地修改头像
                Uri uri = null;
                if (data == null)
                {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK)
                {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED))
                    {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uri = data.getData();
                    startImageAction(uri, 200, 200, 2, true);
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
                // 初始化文件路径
                filePath = "";
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
        file=new File(ZhaiDou.MyCommentDir,new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
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
                photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + filePath);
            }
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
        files.add(file);
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

    private String postSer() throws JSONException
    {
        String BOUNDARY = UUID.randomUUID().toString();
        String result = null;
        BufferedReader in = null;
        try
        {
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, Charset.defaultCharset());
            multipartEntity.addPart("commentUserId",new StringBody(userId+""));
            multipartEntity.addPart("commentUserName",new StringBody( userName,Charset.defaultCharset()));
            multipartEntity.addPart("content",new StringBody(commentInfo,Charset.defaultCharset()));
            multipartEntity.addPart("articleId",new StringBody(mIndex+""));
            multipartEntity.addPart("articleTitle",new StringBody(mPage+"",Charset.defaultCharset()));
            multipartEntity.addPart("commentType",new StringBody("C"));
            if (mComment!=null)
            {
                multipartEntity.addPart("commentId",new StringBody(mCommentId+""));
            }

            for (int i = 0; i < files.size(); i++)
            {
                FileBody fileBody = new FileBody(files.get(i));
                multipartEntity.addPart("files", fileBody);
            }

            HttpPost request = new HttpPost(ZhaiDou.CommentAddUrl);
            request.addHeader("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
            request.setEntity(multipartEntity);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null)
            {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            ToolUtils.setLog(""+result);
            return result;

        } catch (Exception e)
        {

        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result;
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
