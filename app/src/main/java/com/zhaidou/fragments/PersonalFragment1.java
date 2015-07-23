package com.zhaidou.fragments;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link com.zhaidou.fragments.PersonalFragment1#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PersonalFragment1 extends BaseFragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CONTEXT = "context";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TextView mPrePayView,mPreReceivedView,mReturnView;
    private RelativeLayout mCouponsView,mRewardView,mAddrView,mSettingView;
    private FrameLayout mChildContainer;
    SettingFragment settingFragment;
    // TODO: Rename and change types and number of parameters
    public static PersonalFragment1 personalFragment;
    public static PersonalFragment1 newInstance(String param1, String context) {
        if (personalFragment==null)
        personalFragment = new PersonalFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_CONTEXT,context);
        personalFragment.setArguments(args);
        return personalFragment;
    }
    public PersonalFragment1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_CONTEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("onCreateView----------------->","onCreateView");
        View view=inflater.inflate(R.layout.personal, container, false);
        mPrePayView=(TextView)view.findViewById(R.id.tv_pre_pay);
        mPreReceivedView=(TextView)view.findViewById(R.id.tv_pre_received);
        mReturnView=(TextView)view.findViewById(R.id.tv_return);
        mChildContainer=(FrameLayout)view.findViewById(R.id.fl_child_container);

        mCouponsView=(RelativeLayout)view.findViewById(R.id.rl_coupons);
        mRewardView=(RelativeLayout)view.findViewById(R.id.rl_reward_history);
        mAddrView=(RelativeLayout)view.findViewById(R.id.rl_reward_history);
        mSettingView=(RelativeLayout)view.findViewById(R.id.rl_setting);

        mPrePayView.setOnClickListener(this);
        mPreReceivedView.setOnClickListener(this);
        mReturnView.setOnClickListener(this);
        mCouponsView.setOnClickListener(this);
        mRewardView.setOnClickListener(this);
        mAddrView.setOnClickListener(this);
        mSettingView.setOnClickListener(this);
        settingFragment=SettingFragment.newInstance("","");
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_pre_pay:
                break;
            case R.id.tv_pre_received:
                break;
            case R.id.tv_return:
                break;
            case R.id.rl_coupons:
                break;
            case R.id.rl_reward_history:
                break;
            case R.id.rl_manage_address:
                break;
            case R.id.rl_setting:
                Log.i("rl_setting---->","rl_setting");
//              SettingFragment settingFragment1=SettingFragment.newInstance("","");
                Log.i("getactivity---------->",getActivity().toString());
                ((MainActivity)getActivity()).navigationToFragment(settingFragment);
                break;
        }
    }
}
