package com.zhaidou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.utils.EaseUtils;


/**
 * 软装魔法
 */
public class MainMagicFragment extends BaseFragment
{
    private View mView;
    private LinearLayout magicClassicLine,magicGuideLine,magicImageLine,magicDesignLine,classroomLine,diyLine,serverLine;
    private long formerTime;

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
                    if (isTimeInterval())
                    {
                        MagicClassicCaseFragment magicClassicCaseFragment = MagicClassicCaseFragment.newInstance("", "");
                        ((BaseActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    }
                    break;
                case R.id.magicConsultLine:
                    if (isTimeInterval())
                    {
                        MagicGuideFragment magicClassicCaseFragment = MagicGuideFragment.newInstance("", "");
                        ((BaseActivity) getActivity()).navigationToFragment(magicClassicCaseFragment);
                    }
                    break;
                case R.id.magicImageLine:
                    if (isTimeInterval())
                    {
                        MagicImageCaseFragment magicImageCaseFragment = MagicImageCaseFragment.newInstance("", "");
                        ((BaseActivity) getActivity()).navigationToFragment(magicImageCaseFragment);
                    }
                    break;
                case R.id.magicDesignLine:
                    if (isTimeInterval())
                    {
                        MagicDesignFragment orderDetailFragment = MagicDesignFragment.newInstance("", "");
                        ((BaseActivity) getActivity()).navigationToFragment(orderDetailFragment);
                    }
                    break;
                case R.id.magicDIYLine:
                    if (isTimeInterval())
                    {
                        DiyFragment diyFragment = DiyFragment.newInstance("" ,"");
                        ((BaseActivity) getActivity()).navigationToFragment(diyFragment);
                    }
                    break;
                case R.id.magicClassroomLine:
                    if (isTimeInterval())
                    {
                        HomeBeautifulFragment goodsDetailsFragment = HomeBeautifulFragment.newInstance("色彩课堂", 0+"");
                        ((BaseActivity) mContext).navigationToFragment(goodsDetailsFragment);
                    }
                    break;
                case R.id.magicConnectServer:
                    if (isTimeInterval())
                    {
                        EaseUtils.startKeFuActivity(mContext);
                    }
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

        magicGuideLine=(LinearLayout)mView.findViewById(R.id.magicConsultLine);
        magicGuideLine.setOnClickListener(onClickListener);

        magicImageLine=(LinearLayout)mView.findViewById(R.id.magicImageLine);
        magicImageLine.setOnClickListener(onClickListener);

        magicDesignLine=(LinearLayout)mView.findViewById(R.id.magicDesignLine);
        magicDesignLine.setOnClickListener(onClickListener);

        diyLine=(LinearLayout)mView.findViewById(R.id.magicDIYLine);
        diyLine.setOnClickListener(onClickListener);

        classroomLine=(LinearLayout)mView.findViewById(R.id.magicClassroomLine);
        classroomLine.setOnClickListener(onClickListener);

        serverLine=(LinearLayout)mView.findViewById(R.id.magicConnectServer);
        serverLine.setOnClickListener(onClickListener);

    }

    /**
     * 设置时间间隔,防止重复点击
     */
    private boolean isTimeInterval()
    {
        long currentTime=System.currentTimeMillis();

        if ((currentTime- formerTime)>1500)
        {
            formerTime =currentTime;
            return true;
        }
        else
        {
            formerTime =currentTime;
            return false;
        }
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
