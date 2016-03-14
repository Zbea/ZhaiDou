package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

/**
 * Created by roy on 15/11/11.
 */
public class MagicDesignFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private String mIndex;
    private Context mContext;
    private TextView backBtn,caseBtn;
    private LinearLayout designBtn,qqBtn;





    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(MagicDesignFragment.this);
                    break;
                case R.id.case_rl:
                    GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance("宅豆软装设计方案", "191100570001");
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(goodsDetailsFragment);
                    break;
                case R.id.design_rl:

                    break;
                case R.id.caseBtn:
                    MagicClassicCaseFragment magicClassicCaseFragment = MagicClassicCaseFragment.newInstance("", "");
                    ((MainActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    break;
            }
        }
    };

    public static MagicDesignFragment newInstance(String page, String index) {
        MagicDesignFragment fragment = new MagicDesignFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public MagicDesignFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(DATA);
            mIndex = getArguments().getString(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_magic_design_page, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }

    /**
     * 初始化
     */
    private void initView() {

        backBtn = (TextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);

        qqBtn = (LinearLayout) mView.findViewById(R.id.design_rl);
        qqBtn.setOnClickListener(onClickListener);

        designBtn = (LinearLayout) mView.findViewById(R.id.case_rl);
        designBtn.setOnClickListener(onClickListener);

        caseBtn= (TextView) mView.findViewById(R.id.caseBtn);
        caseBtn.setOnClickListener(onClickListener);
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("设计方案");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("设计方案");
    }

}
