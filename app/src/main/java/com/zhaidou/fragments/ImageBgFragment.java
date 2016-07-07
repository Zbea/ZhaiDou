package com.zhaidou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.utils.ToolUtils;

public class ImageBgFragment extends BaseFragment {
    private static final String TITLE = "title";
    private String mTitle;

    private TextView tv_title;
    private ImageView iv_bg_1,iv_bg_2,iv_bg_3,iv_bg_4,iv_bg_5,iv_bg_6;

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
        View view=inflater.inflate(R.layout.fragment_add_v, container, false);
        tv_title=(TextView)view.findViewById(R.id.title_tv);
        iv_bg_1=(ImageView)view.findViewById(R.id.iv_bg_1);
        iv_bg_2=(ImageView)view.findViewById(R.id.iv_bg_2);
        iv_bg_3=(ImageView)view.findViewById(R.id.iv_bg_3);
        iv_bg_4=(ImageView)view.findViewById(R.id.iv_bg_4);
        iv_bg_5=(ImageView)view.findViewById(R.id.iv_bg_5);
        iv_bg_6=(ImageView)view.findViewById(R.id.iv_bg_6);
        tv_title.setText(mTitle);

        String imageUri = "drawable://" + R.drawable.bg_collocation_1;
        String imageUri1 = "drawable://" + R.drawable.bg_collocation_2;
        String imageUri2 = "drawable://" + R.drawable.bg_collocation_3;
        String imageUri3 = "drawable://" + R.drawable.bg_collocation_4;
        String imageUri4 = "drawable://" + R.drawable.bg_collocation_5;
        String imageUri5 = "drawable://" + R.drawable.bg_collocation_6;

        String addUrl = "drawable://" + R.drawable.add_v_1;
        String addUrl1 = "drawable://" + R.drawable.add_v_2;
        String addUrl2 = "drawable://" + R.drawable.add_v_3;

        if ("豆搭教程".equalsIgnoreCase(mTitle)){
            ToolUtils.setImagePreventMemoryLeaksUrl(imageUri, iv_bg_1);
            ToolUtils.setImagePreventMemoryLeaksUrl(imageUri1, iv_bg_2);
            ToolUtils.setImagePreventMemoryLeaksUrl(imageUri2, iv_bg_3);
            ToolUtils.setImagePreventMemoryLeaksUrl(imageUri3, iv_bg_4);
            ToolUtils.setImagePreventMemoryLeaksUrl(imageUri4, iv_bg_5);
            ToolUtils.setImagePreventMemoryLeaksUrl(imageUri5, iv_bg_6);
        }else {
            ToolUtils.setImagePreventMemoryLeaksUrl(addUrl, iv_bg_1);
            ToolUtils.setImagePreventMemoryLeaksUrl(addUrl1, iv_bg_2);
            ToolUtils.setImagePreventMemoryLeaksUrl(addUrl2, iv_bg_3);
        }

        view.findViewById(R.id.rl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)getActivity()).popToStack(ImageBgFragment.this);
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
