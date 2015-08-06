package com.zhaidou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.model.CartItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.ToolUtils;

/**
 * Created by roy on 15/7/28.
 */
public class CustomShopCartDeleteDialog
{
    static Dialog mDialog;
    static CreatCartDB creatCartDB;
    static CartItem mCartItem;
    static Context mContext;
    static LinearLayout parentView;
    static View childView;


    private static View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.okTv:
                    delete();

                    break;
                case R.id.cancelTv:
                    mDialog.dismiss();
                    break;
            }
        }
    };

    private static void delete()
    {
        CreatCartTools.deleteByData(creatCartDB, mCartItem);
        //发送广播
        Intent intent=new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
        mContext.sendBroadcast(intent);

        Intent intent1=new Intent(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        mContext.sendBroadcast(intent1);
        mDialog.dismiss();
        parentView.removeView(childView);
    }

    public static Dialog setDelateDialog(Context context,CartItem cartItem,LinearLayout view1,View view2)
    {
        mCartItem=cartItem;
        mContext=context;
        parentView=view1;
        childView=view2;
        creatCartDB=new CreatCartDB(mContext);

        mDialog=new Dialog(mContext, R.style.custom_dialog);
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_shop_cart_delete,null);
        TextView okTv=(TextView)view.findViewById(R.id.okTv);
        okTv.setOnClickListener(onClickListener);
        TextView cancelTv=(TextView)view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(onClickListener);

        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
        return mDialog;
    };


}
