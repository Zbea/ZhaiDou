package com.zhaidou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;

/**
 * Created by roy on 15/7/13.
 */
public class CustomLoadingDialog {
    public static Dialog setLoadingDialog(final Context mContext, String msg) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_loading, null);
        TextView textView = (TextView) view.findViewById(R.id.loading_tv);
        textView.setText(msg);
        Dialog mDialog = new Dialog(mContext, R.style.custom_dialog_no);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
//        view.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.page_enter_into_the));
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    }

    ;

    /**
     * Author Scoield
     * Created at 15/9/16 18:23
     * isNeedAnim 是否需要动画，TRUE需要
     */
    public static Dialog setLoadingDialog(final Context mContext, String msg, boolean isNeedAnim) {
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

    ;

}
