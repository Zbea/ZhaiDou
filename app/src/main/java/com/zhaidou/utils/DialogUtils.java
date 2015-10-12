package com.zhaidou.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;

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
    private Context mContext;

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

    ;

    public interface PositiveListener {
        public void onPositive();
    }

    public interface CancelListener {
        public void onCancel();
    }
}
