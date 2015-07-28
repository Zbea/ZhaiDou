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
 * Created by roy on 15/7/28.
 */
public class CustomShopCartDeleteDialog
{
    static Dialog mDialog;

    public static Dialog setLoadingDialog(Context mContext,String msg)
    {
        mDialog=new Dialog(mContext, R.style.custom_dialog);
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_shop_cart_delete,null);
        TextView okTv=(TextView)view.findViewById(R.id.loading_tv);
        okTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });
        TextView cancelTv=(TextView)view.findViewById(R.id.loading_tv);
        cancelTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mDialog.dismiss();
            }
        });

        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    };
}
