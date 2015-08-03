package com.zhaidou.fragments;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pulltorefresh.PullToRefreshBase;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.dialog.CustomShopCartDeleteDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceEditText;
import com.zhaidou.view.TypeFaceTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by roy on 15/7/24.
 */
public class ShopOrderOkFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;
    private Dialog mDialog;
    private String bzInfo_Str;

    private TypeFaceTextView backBtn,titleTv;
    private Button okBtn;
    private LinearLayout orderAddressInfoLine,orderAddressNullLine,orderAddressEditLine;
    private LinearLayout orderGoodsListLine;
    private TypeFaceEditText bzInfo;
    private TypeFaceTextView moneyTv,moneyYfTv,moneyTotalTv,moneyNumTv;
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

    private TextWatcher textWatcher=new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {
            bzInfo_Str=charSequence.toString();
        }
        @Override
        public void afterTextChanged(Editable editable)
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
                    ((MainActivity)getActivity()).popToStack(ShopOrderOkFragment.this);
                break;

                case R.id.jsOkBtn:
                    ShopPaymentFragment shopPaymentFragment=ShopPaymentFragment.newInstance("",0);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("goodsList",items);
                    bundle.putInt("moneyNum",num);
                    bundle.putDouble("money",money);
                    bundle.putDouble("moneyYF",moneyYF);
                    shopPaymentFragment.setArguments(bundle);
                    ((MainActivity)getActivity()).navigationToFragment(shopPaymentFragment);
                    break;
            }
        }
    };

    public static ShopOrderOkFragment newInstance(String page, int index) {
        ShopOrderOkFragment fragment = new ShopOrderOkFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public ShopOrderOkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
            items=(ArrayList<CartItem>)getArguments().getSerializable("goodsList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null)
        {
            mView=inflater.inflate(R.layout.shop_settlement_page, container, false);
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
        mDialog= CustomLoadingDialog.setLoadingDialog(mContext,"loading");

        backBtn = (TypeFaceTextView) mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv = (TypeFaceTextView) mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.shop_order_ok_text);

        okBtn=(Button)mView.findViewById(R.id.jsOkBtn);
        okBtn.setOnClickListener(onClickListener);

        moneyTv=(TypeFaceTextView)mView.findViewById(R.id.jsPriceTotalTv);
        moneyYfTv=(TypeFaceTextView)mView.findViewById(R.id.jsPriceYfTv);
        moneyTotalTv=(TypeFaceTextView)mView.findViewById(R.id.jsTotalMoney);
        moneyNumTv=(TypeFaceTextView)mView.findViewById(R.id.jsTotalNum);

        bzInfo=(TypeFaceEditText)mView.findViewById(R.id.jsEditBzInfo);
        bzInfo.addTextChangedListener(textWatcher);

        orderAddressInfoLine=(LinearLayout)mView.findViewById(R.id.jsAddressInfoLine);
        orderAddressNullLine=(LinearLayout)mView.findViewById(R.id.jsAddressNullLine);
        orderAddressEditLine=(LinearLayout)mView.findViewById(R.id.jsEditAddressBtn);

        orderGoodsListLine=(LinearLayout)mView.findViewById(R.id.orderGoodsList);

        initData();
    }

    private void initData()
    {
        if (items.size()>0)
        {
            addGoodsView();
        }

        for (int i = 0; i < items.size(); i++)
        {
            CartItem cartItem = items.get(i);
            num = num + cartItem.num;
            money = money + cartItem.num * cartItem.currentPrice;
        }
        moneyNumTv.setText("" + num);
        moneyYF=10;

        DecimalFormat df = new DecimalFormat("##.0");
        money = Double.parseDouble(df.format(money));
        totalMoney = Double.parseDouble(df.format(money+10));
        moneyTv.setText("￥" + money);
        moneyTotalTv.setText("￥" + totalMoney);
        moneyYfTv.setText("￥" + 10);

        mDialog.dismiss();
    };

    /**
     * 添加商品集合
     */
    private void addGoodsView()
    {
        orderGoodsListLine.removeAllViews();
        for (int position= 0; position<items.size() ; position++)
        {
            View childeView = LayoutInflater.from(mContext).inflate(R.layout.shop_settlement_goods_item, null);
            TypeFaceTextView itemName = (TypeFaceTextView) childeView.findViewById(R.id.orderItemNameTv);
            TypeFaceTextView itemSize = (TypeFaceTextView) childeView.findViewById(R.id.orderItemSizeTv);
            TextView itemCurrentPrice = (TextView) childeView.findViewById(R.id.orderItemCurrentPrice);
            TextView itemFormalPrice = (TextView) childeView.findViewById(R.id.orderItemFormalPrice);
            final TypeFaceTextView itemNum = (TypeFaceTextView) childeView.findViewById(R.id.orderItemNum);
            ImageView itemImage = (ImageView) childeView.findViewById(R.id.orderImageItemTv);
            ImageView itemLine = (ImageView) childeView.findViewById(R.id.orderItemLine);

            if (items.size()>1)
            {
                if (position == items.size()-1)
                {
                    itemLine.setVisibility(View.GONE);
                }
                if (position == 0)
                {
                    itemLine.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                itemLine.setVisibility(View.GONE);
            }

            final CartItem cartItem=items.get(position);

            itemName.setText(cartItem.name);
            itemSize.setText(cartItem.size);
            itemCurrentPrice.setText("￥ " + cartItem.currentPrice);
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥ " + cartItem.formalPrice);
            itemNum.setText("" + cartItem.num);
            ToolUtils.setImageCacheUrl(cartItem.imageUrl, itemImage);

            orderGoodsListLine.addView(childeView);
        }
    }


}
