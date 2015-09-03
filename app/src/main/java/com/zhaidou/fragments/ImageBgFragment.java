package com.zhaidou.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

public class ImageBgFragment extends BaseFragment {
    private static final String TITLE = "title";
    private String mTitle;

    private TextView tv_title;
    private ImageView iv_bg_1,iv_bg_2,iv_bg_3;

    public static ImageBgFragment newInstance(String title) {
        ImageBgFragment fragment = new ImageBgFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }
    public ImageBgFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("onCreateView---->","onCreateView");
        View view=inflater.inflate(R.layout.fragment_add_v, container, false);
        tv_title=(TextView)view.findViewById(R.id.tv_title);
        Log.i("tv_title---->",tv_title.toString());
        iv_bg_1=(ImageView)view.findViewById(R.id.iv_bg_1);
        iv_bg_2=(ImageView)view.findViewById(R.id.iv_bg_2);
        iv_bg_3=(ImageView)view.findViewById(R.id.iv_bg_3);
        tv_title.setText(mTitle);
        if ("豆搭教程".equalsIgnoreCase(mTitle)){
            iv_bg_1.setImageResource(R.drawable.bg_collocation_1);
            iv_bg_2.setImageResource(R.drawable.bg_collocation_2);
            iv_bg_3.setImageResource(R.drawable.bg_collocation_3);
        }else {
            iv_bg_1.setImageResource(R.drawable.add_v_1);
            iv_bg_2.setImageResource(R.drawable.add_v_2);
        }

        view.findViewById(R.id.rl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).popToStack(ImageBgFragment.this);
            }
        });
        return view;
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mTitle);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mTitle);
    }

}
