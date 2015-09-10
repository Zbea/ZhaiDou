package com.zhaidou.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface PositiveListener {
        public void onPositive();
    }

    public interface CancelListener {
        public void onCancel();
    }
}
