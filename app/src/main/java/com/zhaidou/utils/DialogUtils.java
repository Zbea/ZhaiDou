package com.zhaidou.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.view.CustomEditText;

import java.util.Timer;
import java.util.TimerTask;

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

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
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

    public Dialog showVerifyDialog(final VerifyCodeListener verifyCodeListener, final BindPhoneListener bindPhoneListener) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_phone_verify, null);
        final Dialog mDialog = new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
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
                    codeTimer();
                    if (verifyCodeListener != null) {
                        verifyCodeListener.onVerify(phone);
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
                        bindPhoneListener.onBind(phone, code,mDialog);
                    }
                } else {
                    ToolUtils.setToast(mContext, "抱歉,无效手机号码");
                }
            }
        });
        return mDialog;
    }

    /**
     * 验证码倒计时事件处理
     */
    private void codeTimer() {
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

    public interface CancelListener {
        public void onCancel();
    }

    public interface VerifyCodeListener {
        public void onVerify(String phone);
    }

    public interface BindPhoneListener {
        public void onBind(String phone, String verifyCode,Dialog mDialog);
    }
}
