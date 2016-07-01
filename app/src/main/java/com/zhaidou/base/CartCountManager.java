package com.zhaidou.base;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车数量管理Created by Zbea on 16/7/1.
 */
public class CartCountManager
{
    private static CartCountManager cartCountManager;

    private List<OnCartCountListener> listeners=new ArrayList<OnCartCountListener>();

    public static CartCountManager newInstance()
    {
        if (cartCountManager==null)
            cartCountManager=new CartCountManager();
        return cartCountManager;
    }

    public void setOnCartCountListener(OnCartCountListener onCartCountListener)
    {
        listeners.add(onCartCountListener);
    }

    public void notify(int count)
    {
        for (OnCartCountListener onCartCountListener:listeners)
        {
            onCartCountListener.onChange(count);
        }
    }

    public interface OnCartCountListener
    {
        public void onChange(int count);
    }

}
