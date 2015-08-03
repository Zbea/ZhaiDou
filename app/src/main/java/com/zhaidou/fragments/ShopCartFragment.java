package com.zhaidou.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pulltorefresh.PullToRefreshBase;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.ShopCartLoseAdapter;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomShopCartDeleteDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/24.
 */
public class ShopCartFragment extends BaseFragment
{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;

    private TypeFaceTextView backBtn, titleTv;
    private Button okBuyBtn;
    private LinearLayout nullView;
    private TypeFaceTextView ggBtn;
    private RelativeLayout contentView;
    private TypeFaceTextView numTv;
    private TextView totalMoneyTv, saveMoneyTv;
    private CheckBox allCb;
    private LinearLayout cartGoodsLine;//添加商品view
    private LinearLayout contentLoseView,cartGoodsLoseLine;//总体失效View、添加失效商品view
    private View childeView,childLoseView;

    private CreatCartDB creatCartDB;
    private List<CartItem> items = new ArrayList<CartItem>();
    private List<CartItem> itemsLose = new ArrayList<CartItem>();
    private ArrayList<CartItem> itemsCheck = new ArrayList<CartItem>();
    private List<CheckBox> boxs=new ArrayList<CheckBox>();


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                items.removeAll(items);
                items=CreatCartTools.selectByAll(creatCartDB);
                if (items.size() > 0)
                {
                    for (int i = 0; i <items.size() ; i++)
                    {
                        if (items.get(i).isPublish.equals("true"))
                        {
                            itemsLose.add(items.get(i));
                            items.remove(items.get(i));
                        }
                    }
                    setGoodsCheckChange();
                    if(itemsLose.size()==0)
                    {
                        contentLoseView.setVisibility(View.GONE);
                    }
                } else
                {
                    nullView.setVisibility(View.VISIBLE);
                    contentView.setVisibility(View.GONE);
                }

            }
        }
    };

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener = new PullToRefreshBase.OnRefreshListener2()
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
     * 全选选择事件处理
     */
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b)
        {
            if (b)
            {
                for (int i = 0; i < boxs.size(); i++)
                {
                    boxs.get(i).setChecked(true);
                }

            } else
            {
                for (int i = 0; i < boxs.size(); i++)
                {
                    boxs.get(i).setChecked(false);
                }
            }
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity) getActivity()).popToStack(ShopCartFragment.this);
                    break;
                case R.id.cartGgTv:
                    ShopSpecialFragment shopSpecialFragment = ShopSpecialFragment.newInstance("", 0);
                    ((MainActivity) getActivity()).navigationToFragment(shopSpecialFragment);
                    break;
                case R.id.okBuyBtn:
                    if (itemsCheck.size()>0)
                    {
                        ShopOrderOkFragment shopOrderOkFragment = ShopOrderOkFragment.newInstance("", 0);
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("goodsList",itemsCheck);
                        shopOrderOkFragment.setArguments(bundle);
                        ((MainActivity) getActivity()).navigationToFragment(shopOrderOkFragment);
                    }
                    else
                    {
                        ToolUtils.setToast(mContext,"抱歉,先选择商品");
                    }
                    break;
            }
        }
    };

    public static ShopCartFragment newInstance(String page, int index)
    {
        ShopCartFragment fragment = new ShopCartFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public ShopCartFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        if (mView == null)
        {
            mContext = getActivity();
            initBroadcastReceiver();
            mView = inflater.inflate(R.layout.shop_cart_page, container, false);
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
        titleTv.setText(R.string.shop_cart_text);

        okBuyBtn = (Button) mView.findViewById(R.id.okBuyBtn);
        okBuyBtn.setOnClickListener(onClickListener);

        nullView = (LinearLayout) mView.findViewById(R.id.cartNullLine);
        ggBtn = (TypeFaceTextView) mView.findViewById(R.id.cartGgTv);
        ggBtn.setOnClickListener(onClickListener);
        contentView = (RelativeLayout) mView.findViewById(R.id.cartContentLine);


        numTv = (TypeFaceTextView) mView.findViewById(R.id.cartNum);
        totalMoneyTv = (TextView) mView.findViewById(R.id.moneyTotalTv);
        saveMoneyTv = (TextView) mView.findViewById(R.id.moneySaveTv);
        allCb = (CheckBox) mView.findViewById(R.id.allCB);
        allCb.setOnCheckedChangeListener(onCheckedChangeListener);

        cartGoodsLine=(LinearLayout)mView.findViewById(R.id.cartGoodsLine);
        contentLoseView = (LinearLayout) mView.findViewById(R.id.cartLoseLine);
        cartGoodsLoseLine = (LinearLayout) mView.findViewById(R.id.cartGoodsLoseLine);
        creatCartDB = new CreatCartDB(mContext);
        initData();
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        items = CreatCartTools.selectByAll(creatCartDB);
        if (items.size() > 0)
        {
            for (int i = 0; i <items.size() ; i++)
            {
                if (items.get(i).isPublish.equals("true"))
                {
                    itemsLose.add(items.get(i));
                    items.remove(items.get(i));
                }
            }
            nullView.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
            addCartGoods();
            if (itemsLose.size()>0)//如果失效商品存在，则显示
            {
                addCartLoseGoods();
                contentLoseView.setVisibility(View.VISIBLE);
            }
        } else
        {
            nullView.setVisibility(View.VISIBLE);
            contentView.setVisibility(View.GONE);
        }
    }

    /**
     * 添加商品信息
     */
    private void addCartGoods()
    {
        cartGoodsLine.removeAllViews();
        for (int position= 0; position<items.size() ; position++)
        {
            childeView = LayoutInflater.from(mContext).inflate(R.layout.shop_cart_goods_item, null);
            TypeFaceTextView itemName = (TypeFaceTextView) childeView.findViewById(R.id.cartItemNameTv);
            TypeFaceTextView itemSize = (TypeFaceTextView) childeView.findViewById(R.id.cartItemSizeTv);
            TextView itemCurrentPrice = (TextView) childeView.findViewById(R.id.cartItemCurrentPrice);
            TextView itemFormalPrice = (TextView) childeView.findViewById(R.id.cartItemFormalPrice);
            TypeFaceTextView itemSubBtn = (TypeFaceTextView) childeView.findViewById(R.id.cartItemSubBtn);
            TypeFaceTextView itemAddBtn = (TypeFaceTextView) childeView.findViewById(R.id.cartItemAddBtn);
            final TypeFaceTextView itemNum = (TypeFaceTextView) childeView.findViewById(R.id.cartItemNum);
            ImageView itemImage = (ImageView) childeView.findViewById(R.id.cartImageItemTv);
            final CheckBox itemCheck = (CheckBox) childeView.findViewById(R.id.chatItemCB);
            ImageView itemDeleteBtn = (ImageView) childeView.findViewById(R.id.cartItemDelBtn);
            ImageView itemLine = (ImageView) childeView.findViewById(R.id.cartItemLine);

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

            boxs.add(itemCheck);

            itemName.setText(cartItem.name);
            itemSize.setText(cartItem.size);
            itemCurrentPrice.setText("￥ " + cartItem.currentPrice);
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥ " + cartItem.formalPrice);
            itemNum.setText("" + cartItem.num);
            ToolUtils.setImageCacheUrl(cartItem.imageUrl, itemImage);

            itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    if (b)
                    {
                        cartItem.isCheck=true;
                        itemsCheck.add(cartItem);
                    } else
                    {
                        cartItem.isCheck=false;
                        itemsCheck.remove(cartItem);
                    }
                    setGoodsCheckChange();
                }
            });
            itemDeleteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    items.remove(cartItem);
                    itemsCheck.remove(cartItem);
                    CustomShopCartDeleteDialog.setDelateDialog(mContext, cartItem,cartGoodsLine,childeView);
                }
            });
            itemSubBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (cartItem.num-1>0)
                    {
                        cartItem.num=cartItem.num-1;
                        CreatCartTools.editByData(creatCartDB,cartItem);
                        sendBroadCastEditAll();
                        itemNum.setText("" + cartItem.num);
                    }
                }
            });
            itemAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    cartItem.num=cartItem.num+1;
                    CreatCartTools.editByData(creatCartDB,cartItem);
                    sendBroadCastEditAll();
                    itemNum.setText("" + cartItem.num);
                }
            });
            cartGoodsLine.addView(childeView);
        }
    }

    /**
     * 添加失效商品
     */
    private void addCartLoseGoods()
    {
        cartGoodsLoseLine.removeAllViews();
        for (int position= 0; position<itemsLose.size() ; position++)
        {
            childLoseView = LayoutInflater.from(mContext).inflate(R.layout.shop_cart_goods_lose_item, null);
            TypeFaceTextView itemName = (TypeFaceTextView) childLoseView.findViewById(R.id.cartItemNameTv);
            TypeFaceTextView itemSize = (TypeFaceTextView) childLoseView.findViewById(R.id.cartItemSizeTv);
            TextView itemCurrentPrice = (TextView) childLoseView.findViewById(R.id.cartItemCurrentPrice);
            TextView itemFormalPrice = (TextView) childLoseView.findViewById(R.id.cartItemFormalPrice);
            final TypeFaceTextView itemNum = (TypeFaceTextView) childLoseView.findViewById(R.id.cartItemNum);
            ImageView itemImage = (ImageView) childLoseView.findViewById(R.id.cartImageItemTv);
            ImageView itemDeleteBtn = (ImageView) childLoseView.findViewById(R.id.cartItemDelBtn);
            ImageView itemLine = (ImageView) childLoseView.findViewById(R.id.cartItemLine);

            if (itemsLose.size()>1)
            {
                if (position == itemsLose.size()-1)
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

            final CartItem cartItem=itemsLose.get(position);

            itemName.setText(cartItem.name);
            itemSize.setText(cartItem.size);
            itemCurrentPrice.setText("￥ " + cartItem.currentPrice);
            itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            itemFormalPrice.setText("￥ " + cartItem.formalPrice);
            itemNum.setText("" + cartItem.num);
            ToolUtils.setImageCacheUrl(cartItem.imageUrl, itemImage);

            itemDeleteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    itemsLose.remove(cartItem);
                    CustomShopCartDeleteDialog.setDelateDialog(mContext, cartItem,cartGoodsLoseLine,childLoseView);
                }
            });
            cartGoodsLoseLine.addView(childLoseView);
        }
    }

    /**
     * 发送全局修改数量广播刷新
     */
    public void sendBroadCastEditAll()
    {
        //发送数量修改广播
        Intent intent=new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
        mContext.sendBroadcast(intent);
    }

    /**
     * 设置选中商品价格数量变化
     */
    private void setGoodsCheckChange()
    {
        int num = 0;
        double totalMoney = 0;
        double saveMoney = 0;
        for (int i = 0; i < itemsCheck.size(); i++)
        {
            CartItem cartItem = itemsCheck.get(i);
            num = num + cartItem.num;
            totalMoney = totalMoney + cartItem.num * cartItem.currentPrice;
            saveMoney = saveMoney + ((cartItem.formalPrice - cartItem.currentPrice) * cartItem.num);
        }
        numTv.setText("" + num);
        DecimalFormat df = new DecimalFormat("##.0");
        saveMoney = Double.parseDouble(df.format(saveMoney));
        totalMoney = Double.parseDouble(df.format(totalMoney));

        totalMoneyTv.setText("￥" + totalMoney);
        saveMoneyTv.setText("￥" + saveMoney);

    }



    @Override
    public void onDestroy()
    {
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }


}
