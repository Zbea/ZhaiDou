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
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.view.TypeFaceEditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by roy on 15/8/28.
 */
public class CommentSendFragment extends BaseFragment implements PhotoMenuFragment.MenuSelectListener
{
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private String mPage;
    private String mIndex;

    private Dialog mDialog;
    private final int MENU_CAMERA_SELECTED = 0;
    private final int MENU_PHOTO_SELECTED = 1;

    private View mView;
    private FrameLayout menuView;
    private TextView backTv, sentTv;
    private LinearLayout imageLine;
    private ImageView imageAddBtn;
    private TypeFaceEditText editText;

    private PhotoMenuFragment menuFragment;
    private String filePath = "";
    private List<Bitmap> photos=new ArrayList<Bitmap>();
    private OnCommentListener onCommentListener;


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
                    ((MainActivity) getActivity()).popToStack(CommentSendFragment.this);
                    break;
                case R.id.commentOkTv:

                    break;
                case R.id.comment_image_add:
                    menuView.setVisibility(View.VISIBLE);
                    toggleMenu();
                    break;

            }
        }
    };

    public static CommentSendFragment newInstance(String page, String index)
    {
        CommentSendFragment fragment = new CommentSendFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
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
//        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "");

        backTv = (TextView) mView.findViewById(R.id.commentCancelTv);
        backTv.setOnClickListener(onClickListener);


        sentTv = (TextView) mView.findViewById(R.id.commentOkTv);
        sentTv.setOnClickListener(onClickListener);

        editText=(TypeFaceEditText)mView.findViewById(R.id.comment_edit);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(editText.getWindowToken(), 0);

        imageLine = (LinearLayout) mView.findViewById(R.id.comment_image_line);

        imageAddBtn = (ImageView) mView.findViewById(R.id.comment_image_add);
        imageAddBtn.setOnClickListener(onClickListener);

        menuView = (FrameLayout) mView.findViewById(R.id.commentMenuLayout);
        menuFragment = PhotoMenuFragment.newInstance("", "");
        menuFragment.setMenuSelectListener(this);
        getChildFragmentManager().beginTransaction().replace(R.id.commentMenuLayout, menuFragment).addToBackStack("").hide(menuFragment).commit();

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
                File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
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
                clearPhotoImage(photo);
                imageLine.removeView(mView);
            }
        });
        imageLine.addView(mView);
        if (photos.size()>3)
        {
            imageAddBtn.setVisibility(View.GONE);
        }
    }

    private void clearPhotoImage(Bitmap photo)
    {
        photos.remove(photo);
        if (photos.size()<3)
        {
            imageAddBtn.setVisibility(View.VISIBLE);
        }
    }

    public void setOnCommentListener(OnCommentListener onCommentListener)
    {
        this.onCommentListener=onCommentListener;
    }

    public interface OnCommentListener
    {
        public void onCommentResult(Boolean result);
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
}
