package com.zhaidou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;

/**
 * Created by roy on 15/7/13.
 */
public class CustomToastDialog
{
    public static Dialog setToastDialog(final Context mContext,String msg)
    {
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_toast,null);
        TextView textView=(TextView)view.findViewById(R.id.tv_msg);
        textView.setText(msg);
        TextView cancel=(TextView)view.findViewById(R.id.cancelTv);
        final Dialog mDialog=new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mDialog.dismiss();
            }
        });
        mDialog.show();
        return mDialog;
    };
}
