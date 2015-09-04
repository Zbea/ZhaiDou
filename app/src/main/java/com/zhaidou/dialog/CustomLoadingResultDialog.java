package com.zhaidou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;

/**
 * Created by roy on 15/8/10.
 */
public class CustomLoadingResultDialog
{
    public static Dialog setLoadingDialog(Context mContext,boolean flags,String msg)
    {
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_loading_result,null);
        TextView textView=(TextView)view.findViewById(R.id.dialogTv);
        textView.setText(msg);

        ImageView imageView=(ImageView)view.findViewById(R.id.dialogIv);
        if (flags)
        {
            imageView.setImageResource(R.drawable.dialog_loading_success_icon);
        }
        else
        {
            imageView.setImageResource(R.drawable.dialog_loading_fail_icon);
        }

        Dialog mDialog=new Dialog(mContext, R.style.custom_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    };
}
