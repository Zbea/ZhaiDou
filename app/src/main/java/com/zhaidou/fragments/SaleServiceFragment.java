package com.zhaidou.fragments;



import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;


public class SaleServiceFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context mContext;
    private LinearLayout mServiceContainer;

    public static SaleServiceFragment newInstance(String param1, String param2) {
        SaleServiceFragment fragment = new SaleServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SaleServiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.goods_details_aftersale_page, container, false);
        mContext=getActivity();
        mServiceContainer=(LinearLayout)view.findViewById(R.id.ll_service_container);
        ViewTreeObserver viewTreeObserver = mServiceContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i("mDetailContainer.getMeasuredWidth()-------------------->", mServiceContainer.getMeasuredWidth() + "");
                Log.i("mDetailContainer.getMeasuredHeight()-------------------->", mServiceContainer.getMeasuredHeight() + "");
                Log.i("mDetailContainer.getWidth()-------------------->", mServiceContainer.getWidth() + "");
                Log.i("mDetailContainer.getHeight()-------------------->", mServiceContainer.getHeight() + "");
                mServiceContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        view.findViewById(R.id.rl_qq_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url="mqqwpa://im/chat?chat_type=wpa&uin="+mContext.getResources().getString(R.string.QQ_Number);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
        return view;
    }


}
