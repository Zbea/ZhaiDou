package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.pulltorefresh.PullToRefreshBase;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.CartItem;
import com.zhaidou.view.TypeFaceTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by roy on 15/7/31.
 */
public class ShopPaymentFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;

    private int initTime=15*60*1000;

    private final int UPDATE_COUNT_DOWN_TIME=1001,UPDATE_UI_TIMER_FINISH=1002,UPDATE_TIMER_START=1003;

    private TypeFaceTextView backBtn,titleTv;
    private TypeFaceTextView timeInfoTv;
    private Timer mTimer;

    private ImageView paymentIcon;
    private Button paymentBtn;
    private TypeFaceTextView paymentTv;

    private ArrayList<CartItem> items;
    private int num = 0;
    private double money = 0;
    private double moneyYF=0;
    private double totalMoney = 0;

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {

        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {

        }
    };



    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity)getActivity()).popToStack(ShopPaymentFragment.this);
                break;
            }
        }
    };

    public static ShopPaymentFragment newInstance(String page, int index) {
        ShopPaymentFragment fragment = new ShopPaymentFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public ShopPaymentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
            items=(ArrayList<CartItem>)getArguments().getSerializable("goodsList");
            num=getArguments().getInt("moneyNum");
            money=getArguments().getDouble("money");
            moneyYF=getArguments().getDouble("moneyYF");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (mView == null)
        {
            mView=inflater.inflate(R.layout.shop_payment_page, container, false);
            mContext=getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }

        return mView;
    }


    /**
     * 初始化数据
     */
    private void initView()
    {
        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_payment_text);

        timeInfoTv = (TypeFaceTextView) mView.findViewById(R.id.failTimeInfo);
        paymentTv = (TypeFaceTextView) mView.findViewById(R.id.paymentNameTv);
        paymentIcon=(ImageView)mView.findViewById(R.id.paymentIcon);
        paymentBtn=(Button)mView.findViewById(R.id.paymentBtn);

        mTimer=new Timer();
        mTimer.schedule(new MyTimer(),1000,1000);

    }

    class MyTimer extends TimerTask
    {
        @Override
        public void run()
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    initTime=initTime-1000;
                    timeInfoTv.setText(new SimpleDateFormat("mm:ss").format(new Date(initTime)));
                    if (initTime==0)
                    {
                        if (mTimer!=null)
                        {
                            mTimer.cancel();
                            timeInfoTv.setText("00:00");
                            stopView();
                        }
                    }
                }
            });
        }
    }

    private void stopView()
    {
        paymentTv.setText("支付失效");
        paymentIcon.setImageResource(R.drawable.shop_order_payment_lose_icon);
//        paymentBtn.setBackgroundResource(R.drawable.);

    }


}
