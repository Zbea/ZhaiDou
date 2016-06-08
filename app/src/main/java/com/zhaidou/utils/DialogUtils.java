package com.zhaidou.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.view.CustomEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-09-09
 * Time: 15:59
 * Description:显示Dialog的工具类
 * FIXME
 */
public class DialogUtils {

    private PositiveListener positiveListener;
    private CancelListener cancelListener;
    private VerifyCodeListener verifyCodeListener;
    private Context mContext;
    private int initTime = 0;
    private Timer mTimer;
    TextView mGetCode;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initTime = initTime - 1;
                    mGetCode.setText("重新获取(" + initTime + ")");
                    if (initTime <= 0) {
                        if (mTimer != null)
                            mTimer.cancel();
                        mGetCode.setText("获取验证码");
                        mGetCode.setBackgroundResource(R.drawable.btn_green_click_bg);
                        mGetCode.setClickable(true);
                    }
                    break;
            }
        }
    };
    private Dialog mDialog;
    private PhotoAdapter photoAdapter;

    public DialogUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showDialog(String msg, final PositiveListener positiveListener, final CancelListener cancelListener) {
        this.positiveListener = positiveListener;
        this.cancelListener = cancelListener;
        final Dialog dialog = new Dialog(mContext, R.style.custom_dialog);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);
        TextView cancelTv = (TextView) view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (cancelListener != null)
                    cancelListener.onCancel();
            }
        });

        TextView okTv = (TextView) view.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (positiveListener != null)
                    positiveListener.onPositive();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();
    }

    /**
     * Author Scoield
     * Created at 15/9/23 11:44
     * Description:默认加载对话框,不带提示信息
     * FIXME
     */
    public Dialog showLoadingDialog() {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_loading, null);
        TextView textView = (TextView) view.findViewById(R.id.loading_tv);
        textView.setText("");
        mDialog = new Dialog(mContext, R.style.custom_dialog_no);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    }

    /**
     * Author Scoield
     * Created at 15/9/23 11:44
     * Description:默认加载对话框,带提示信息
     *
     * @param msg 提示信息
     *            FIXME
     */
    public Dialog showLoadingDialog(String msg) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_loading, null);
        TextView textView = (TextView) view.findViewById(R.id.loading_tv);
        textView.setText(msg);
        Dialog mDialog = new Dialog(mContext, R.style.custom_dialog_no);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    }

    /**
     * Author Scoield
     * Created at 15/9/23 11:44
     * Description:默认加载对话框,带提示信息
     *
     * @param msg        提示信息
     * @param isNeedAnim 是否需要动画，TRUE需要
     *                   FIXME
     */

    public Dialog showLoadingDialog(String msg, boolean isNeedAnim) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_loading, null);
        TextView textView = (TextView) view.findViewById(R.id.loading_tv);
        textView.setText(msg);
        Dialog mDialog = new Dialog(mContext, R.style.custom_dialog_no);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        if (isNeedAnim) {
            Window window = mDialog.getWindow();
            window.setWindowAnimations(R.style.anim_slide_in_from_right);
            WindowManager.LayoutParams wl = window.getAttributes();
            window.setAttributes(wl);
        }
        return mDialog;
    }

    /**
     * Author Scoield
     * Created at 15/10/12 15:19
     * Description:收藏成功,收藏失败,取消收藏对话框
     * param: DrawableRes 图片资源id
     * param: msgRes 文字信息资源id
     */
    public Dialog showCollectDialog(Context mContext, int DrawableRes, int msgRes) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_loading_result, null);
        TextView textView = (TextView) view.findViewById(R.id.dialogTv);
        textView.setText(mContext.getResources().getString(msgRes));

        ImageView imageView = (ImageView) view.findViewById(R.id.dialogIv);
        imageView.setImageResource(DrawableRes);
        Dialog mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    }

    public Dialog showVerifyDialog(VerifyCodeListener verifyCodeListener, BindPhoneListener bindPhoneListener) {
        return showVerifyDialog(verifyCodeListener, bindPhoneListener, false);
    }

    public Dialog showVerifyDialog(final VerifyCodeListener verifyCodeListener, final BindPhoneListener bindPhoneListener, boolean b) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_phone_verify, null);
        ImageView closeView = (ImageView) view.findViewById(R.id.iv_close);
        closeView.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        final Dialog mDialog = new Dialog(mContext, R.style.custom_dialog) {
            @Override
            public void dismiss() {
                super.dismiss();
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        };
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();

        final CustomEditText mCodeView = (CustomEditText) view.findViewById(R.id.tv_code);
        final CustomEditText mPhoneView = (CustomEditText) view.findViewById(R.id.tv_phone);
        mGetCode = (TextView) view.findViewById(R.id.bt_getCode);
        mGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mPhoneView.getText().toString();
                if (ToolUtils.isPhoneOk(phone)) {
//                    codeTimer();
                    if (verifyCodeListener != null) {
                        verifyCodeListener.onVerify(phone, mDialog);
                    }
                } else {
                    ToolUtils.setToast(mContext, "抱歉,无效手机号码");
                }
            }
        });
        TextView okTv = (TextView) view.findViewById(R.id.bt_ok);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = mCodeView.getText().toString();
                String phone = mPhoneView.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    mPhoneView.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(code)) {
                    mCodeView.setShakeAnimation();
                    return;
                }
                if (ToolUtils.isPhoneOk(phone)) {
                    if (bindPhoneListener != null) {
                        bindPhoneListener.onBind(phone, code, mDialog);
                    }
                } else {
                    ToolUtils.setToast(mContext, "抱歉,无效手机号码");
                }
            }
        });
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        return mDialog;
    }

    public Dialog showCouponDialog(final PositiveListener2 positiveListener, final CancelListener2 cancelListener) {
        final Dialog mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_coupon);
        mDialog.show();
        final EditText editText = (EditText) mDialog.findViewById(R.id.message);
        mDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancelListener != null)
                    cancelListener.onCancel(editText);
                mDialog.dismiss();
            }
        });
        mDialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                    Toast.makeText(mContext, "请输入兑换码", Toast.LENGTH_SHORT).show();
                    return;
                }
                mDialog.dismiss();
                positiveListener.onPositive(editText.getText().toString());
            }
        });
        return mDialog;
    }

    public void showShareDialog(final String title, final String content, final String imageUrl, final String url, final PlatformActionListener platformActionListener) {
        System.out.println("title = [" + title + "], content = [" + content + "], imageUrl = [" + imageUrl + "], url = [" + url + "], platformActionListener = [" + platformActionListener + "]");
        ShareSDK.initSDK(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_share_custom, null);
        GridView mGridView = (GridView) view.findViewById(R.id.gv_share);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        String[] titlearr = mContext.getResources().getStringArray(R.array.share_title);
        List<String> titleList = Arrays.asList(titlearr);
        int[] drawableId = {R.drawable.skyblue_logo_wechat_checked, R.drawable.skyblue_logo_wechatmoments_checked, R.drawable.skyblue_logo_sinaweibo_checked,
                R.drawable.skyblue_logo_qq_checked, R.drawable.skyblue_logo_qzone_checked, R.drawable.skyblue_logo_sinaweibo_checked};
        ShareAdapter shareAdapter = new ShareAdapter(mContext, titleList, drawableId);
        mGridView.setAdapter(shareAdapter);
        mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        Window win = mDialog.getWindow();
        win.setWindowAnimations(R.style.pop_anim_style);
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;

        win.setAttributes(lp);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
//                        mDialog.dismiss();
                        if (!com.alibaba.sdk.android.util.NetworkUtils.isNetworkAvaiable(mContext)) {
                            Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Wechat.ShareParams weChatSP = new Wechat.ShareParams();
                        weChatSP.setTitle(title);
                        weChatSP.setText(content);
                        weChatSP.setImageUrl(imageUrl);
                        weChatSP.setUrl(url);
                        weChatSP.setShareType(Platform.SHARE_WEBPAGE);
                        Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                        wechat.setPlatformActionListener(platformActionListener);
                        wechat.removeAccount(true);
                        wechat.share(weChatSP);
                        break;
                    case 1:
//                        mDialog.dismiss();
                        if (!com.alibaba.sdk.android.util.NetworkUtils.isNetworkAvaiable(mContext)) {
                            Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        WechatMoments.ShareParams WMSP = new WechatMoments.ShareParams();
                        WMSP.setShareType(Platform.SHARE_WEBPAGE);
                        WMSP.setTitle(title);
                        WMSP.setText(content);
                        WMSP.setImageUrl(imageUrl);
                        WMSP.setUrl(url);
                        Platform wm = ShareSDK.getPlatform(WechatMoments.NAME);
                        wm.setPlatformActionListener(platformActionListener);
                        wm.removeAccount(true);
                        wm.share(WMSP);
                        break;
                    case 2:
                        mDialog.dismiss();
                        if (!com.alibaba.sdk.android.util.NetworkUtils.isNetworkAvaiable(mContext)) {
                            Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cn.sharesdk.sina.weibo.SinaWeibo.ShareParams sp = new cn.sharesdk.sina.weibo.SinaWeibo.ShareParams();
                        sp.setText(content);
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            Collection<String> keys = ImageLoader.getInstance().getMemoryCache().keys();
                            Iterator<String> iterator = keys.iterator();
                            while (iterator.hasNext()) {
                                String next = iterator.next();
                                if (!TextUtils.isEmpty(imageUrl) && next.contains(imageUrl)) {
                                    Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(next);
                                    File sina = PhotoUtil.saveBitmapFile(bitmap, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/sina.jpg");
                                    sp.setImagePath(sina.getAbsolutePath());
                                }
                            }
                        }
                        Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                        weibo.removeAccount(true);
                        weibo.setPlatformActionListener(platformActionListener);
                        weibo.share(sp);
                        break;
                    case 3:
                        mDialog.dismiss();
                        if (!com.alibaba.sdk.android.util.NetworkUtils.isNetworkAvaiable(mContext)) {
                            Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        QQ.ShareParams QQSp = new QQ.ShareParams();
                        QQSp.setTitle(title);
                        QQSp.setTitleUrl(url);
                        QQSp.setText(content);
                        QQSp.setImageUrl(imageUrl);
                        Platform qq = ShareSDK.getPlatform(QQ.NAME);
                        qq.removeAccount(true);
                        qq.setPlatformActionListener(platformActionListener);
                        qq.share(QQSp);
                        break;
                    case 4:
                        mDialog.dismiss();
                        if (!com.alibaba.sdk.android.util.NetworkUtils.isNetworkAvaiable(mContext)) {
                            Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        QZone.ShareParams QZoneSP = new QZone.ShareParams();
                        QZoneSP.setTitle(title);
                        QZoneSP.setTitleUrl(url); // 标题的超链接
                        QZoneSP.setText(content);
                        QZoneSP.setImageUrl(imageUrl);
                        QZoneSP.setSite(mContext.getString(R.string.app_name));
                        QZoneSP.setSiteUrl(url);
                        Platform qzone = ShareSDK.getPlatform(QZone.NAME);
                        qzone.removeAccount(true);
                        qzone.setPlatformActionListener(platformActionListener); // 设置分享事件回调
                        qzone.share(QZoneSP);
                        break;
                    default:
                        mDialog.dismiss();
                        break;
                }
            }
        });
    }


    public void showListDialog(final AdapterView.OnItemClickListener onItemClickListener) {
        ShareSDK.initSDK(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_list_custom, null);
        ListView mListView = (ListView) view.findViewById(R.id.listView);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        String[] titlearr = mContext.getResources().getStringArray(R.array.share_title);
        List<String> titleList = Arrays.asList(titlearr);
        int[] drawableId = {R.drawable.skyblue_logo_wechat_checked, R.drawable.skyblue_logo_wechatmoments_checked, R.drawable.skyblue_logo_sinaweibo_checked,
                R.drawable.skyblue_logo_qq_checked, R.drawable.skyblue_logo_qzone_checked, R.drawable.skyblue_logo_sinaweibo_checked};
        mListView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, new String[]{"删除"}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view1 = (TextView) super.getView(position, convertView, parent);
                view1.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                view1.setTextColor(mContext.getResources().getColor(R.color.red));
                view1.setTypeface(((ZDApplication) mContext.getApplicationContext()).getTypeFace());
                view1.setGravity(Gravity.CENTER);
                return view1;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDialog.dismiss();
                onItemClickListener.onItemClick(parent, view, position, id);
            }
        });
        mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        Window win = mDialog.getWindow();
        win.setWindowAnimations(R.style.pop_anim_style);
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;

        win.setAttributes(lp);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    public void showCommentDialog(final onCommentListener onCommentListener, final PickerListener pickerListener) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_comment_view, null);
        final Dialog mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDialog.show();
        Window win = mDialog.getWindow();
        win.setWindowAnimations(R.style.pop_anim_style);
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;

        win.setAttributes(lp);
        final TextView commentOkTv = (TextView) view.findViewById(R.id.commentOkTv);
        final TextView commentContent = (TextView) view.findViewById(R.id.comment_edit);
        GridView mGridView = (GridView) view.findViewById(R.id.gridView);
        photoAdapter = new PhotoAdapter(mContext, Arrays.asList(new String[]{""}));
        mGridView.setAdapter(photoAdapter);
        ImageView comment_image_add = (ImageView) view.findViewById(R.id.comment_image_add);
        view.findViewById(R.id.commentCancelTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        commentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                commentOkTv.setClickable(!TextUtils.isEmpty(commentContent.getText().toString()));
                commentOkTv.setTextColor(TextUtils.isEmpty(commentContent.getText().toString()) ?
                        mContext.getResources().getColor(R.color.green_color) :
                        mContext.getResources().getColor(R.color.text_gary_color));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        commentOkTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = commentContent.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    Toast.makeText(mContext, "评论内容不能为空哦", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String,Object> params=new HashMap<String, Object>();
                params.put("content",str);
                params.put("images",photoAdapter.getList());
                System.out.println("photoAdapter = " + photoAdapter.getList());
                onCommentListener.onComment(params);
                mDialog.dismiss();
            }
        });
        comment_image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerDialog(pickerListener);
            }
        });
        photoAdapter.setOnInViewClickListener(R.id.photoLayout, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                String image = (String) values;
                if (TextUtils.isEmpty(image)) {
                    showPickerDialog(pickerListener);
                }
            }
        });
        photoAdapter.setOnInViewClickListener(R.id.imageClear_iv, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                photoAdapter.remove(position);
            }
        });
    }

    public void notifyPhotoAdapter(String image) {
        if (photoAdapter != null) {
            List<String> list = photoAdapter.getList();
            List<String> images=new ArrayList<String>(list);
            System.out.println("DialogUtils.notifyPhotoAdapter------>" + images.toString() + "-----" + images.size());
            images.add(images.size()-1,image);
            photoAdapter.setList(images);
        }
    }

    private void showPickerDialog(final PickerListener pickerListener) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_photo_menu, null);
        TextView tv_camera = (TextView) view.findViewById(R.id.tv_camera);
        TextView tv_photo = (TextView) view.findViewById(R.id.tv_photo);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        final Dialog mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDialog.show();
        Window win = mDialog.getWindow();
        win.setWindowAnimations(R.style.pop_anim_style);
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        tv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                pickerListener.onCamera();
            }
        });
        tv_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                pickerListener.onPhoto();
            }
        });
    }

    public void dismiss() {
        System.out.println("mDialog = " + mDialog);
        if (mDialog != null)
            mDialog.dismiss();
    }

    public class ShareAdapter extends BaseListAdapter<String> {
        private int[] drawableId;
        private List<String> titles;

        public ShareAdapter(Context context, List<String> titles, int[] drawableId) {
            super(context, titles);
            this.drawableId = drawableId;
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_share_view, null);
            ImageView imageView = ViewHolder.get(convertView, R.id.iv_plat);
            TextView textView = ViewHolder.get(convertView, R.id.tv_plat);
            String title = getList().get(position);
            textView.setText(title);
            imageView.setImageResource(drawableId[position]);
            imageView.setVisibility(TextUtils.isEmpty(title) ? View.INVISIBLE : View.VISIBLE);
            return convertView;
        }
    }


    public class PhotoAdapter extends BaseListAdapter<String> {

        public PhotoAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public int getCount() {
            return list.size() >= 4 ? 4 : super.getCount();
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_image_crop, null);
            }
            ImageView mImageView = ViewHolder.get(convertView, R.id.imageBg_iv);
            ImageView mClearView = ViewHolder.get(convertView, R.id.imageClear_iv);
            String item = getItem(position);
            System.out.println("bindView--item = " + item);
            if (!TextUtils.isEmpty(item)) {
                mImageView.setImageBitmap(BitmapFactory.decodeFile(item));
                mClearView.setVisibility(View.VISIBLE);
            } else {
                mImageView.setImageResource(R.drawable.icon_commet_add);
                mClearView.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    /**
     * 验证码倒计时事件处理
     */
    public void codeTimer() {
        initTime = ZhaiDou.VERFIRY_TIME;
        mGetCode.setBackgroundResource(R.drawable.btn_no_click_selector);
        mGetCode.setText("重新获取(" + initTime + ")");
        mGetCode.setClickable(false);
        mTimer = new Timer();
        mTimer.schedule(new MyTimer(), 1000, 1000);
    }


    /**
     * 倒计时
     */
    class MyTimer extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
        }
    }

    public interface PositiveListener {
        public void onPositive();
    }

    public interface PositiveListener2 {
        public void onPositive(Object o);
    }

    public interface CancelListener {
        public void onCancel();
    }

    public interface CancelListener2 {
        public void onCancel(Object o);
    }

    public interface VerifyCodeListener {
        public void onVerify(String phone, Dialog mDialog);

    }

    public interface BindPhoneListener {
        public void onBind(String phone, String verifyCode, Dialog mDialog);
    }

    public interface ShareListener {
        public void onShare(int position, String platform);
    }

    public interface PickerListener {
        public void onCamera();

        public void onPhoto();
    }

    public interface onCommentListener {
        public void onComment(Object object);
    }

}
