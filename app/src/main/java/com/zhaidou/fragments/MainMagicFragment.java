package com.zhaidou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.utils.EaseUtils;


/**
 * 软装魔法
 */
public class MainMagicFragment extends BaseFragment
{
    private View mView;
    private LinearLayout magicClassicLine,magicConsultLine,magicImageLine,magicDesignLine;
    private ImageView diyBtn;


    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.magicClassicLine:
                    MagicClassicCaseFragment magicClassicCaseFragment = MagicClassicCaseFragment.newInstance("", "");
                    ((MainActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    break;
                case R.id.magicConsultLine:
                    EaseUtils.startDesignerActivity(mContext);
                    break;
                case R.id.magicImageLine:
                    MagicImageCaseFragment magicImageCaseFragment = MagicImageCaseFragment.newInstance("", "");
                    ((MainActivity) getActivity()).navigationToFragment(magicImageCaseFragment);
                    break;
                case R.id.magicDesignLine:
                    MagicDesignFragment orderDetailFragment = MagicDesignFragment.newInstance("", "");
                    ((MainActivity) getActivity()).navigationToFragment(orderDetailFragment);
                    break;
                case R.id.magicDiyBtn:
                    DiyFragment diyFragment = DiyFragment.newInstance("" ,"");
                    ((MainActivity) getActivity()).navigationToFragment(diyFragment);
                    break;
            }
        }
    };

    public MainMagicFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContext = getActivity();
        if (mView == null) {
            mView= inflater.inflate(R.layout.fragment_main_magic, container, false);
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
    private void initView()
    {

        magicClassicLine=(LinearLayout)mView.findViewById(R.id.magicClassicLine);
        magicClassicLine.setOnClickListener(onClickListener);

        magicConsultLine=(LinearLayout)mView.findViewById(R.id.magicConsultLine);
        magicConsultLine.setOnClickListener(onClickListener);

        magicImageLine=(LinearLayout)mView.findViewById(R.id.magicImageLine);
        magicImageLine.setOnClickListener(onClickListener);

        magicDesignLine=(LinearLayout)mView.findViewById(R.id.magicDesignLine);
        magicDesignLine.setOnClickListener(onClickListener);

        diyBtn=(ImageView)mView.findViewById(R.id.magicDiyBtn);
        diyBtn.setOnClickListener(onClickListener);

    }



    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_beauty));
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_beauty));
    }
}
