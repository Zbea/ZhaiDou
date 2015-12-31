package com.zhaidou.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-12-31
 * Time: 10:45
 * Description:计数管理类
 * FIXME
 */
public class CountManage {
    private static CountManage mCountManage = null;
    private Map<Enum, Integer> map = new HashMap<Enum, Integer>();
    private onCountChangeListener onCountChangeListener;
    private List<onCountChangeListener> mListeners=new ArrayList<CountManage.onCountChangeListener>();

    private CountManage() {

    }

    public static CountManage getInstance() {
        if (mCountManage == null){
            mCountManage = new CountManage();
        }

        return mCountManage;
    }

    public void init(TYPE type, int count) {
        map.put(type, count);
        refreshData();
    }

    public void add(TYPE type) {
        Integer integer = map.get(type);
        if (integer == null) {
            init(type, 0);
        }
        int i = integer.intValue();
        init(type, ++i);
    }

    public void minus(TYPE type) {
        Integer integer = map.get(type);
        if (integer == null) {
            init(type, 0);
            return;
        }
        int i = integer.intValue();
        init(type, --i);
    }

    public int value(TYPE type) {
        Integer integer = map.get(type);
        return integer == null ? 0 : integer.intValue();
    }

    public enum TYPE {
        TAG_PREPAY, TAG_OTHER
    }

    public void setOnCountChangeListener(CountManage.onCountChangeListener onCountChangeListener) {
        if (this.onCountChangeListener==null)
           this.onCountChangeListener = onCountChangeListener;
        mListeners.add(onCountChangeListener);
    }

    private void refreshData(){
        for (onCountChangeListener listener:mListeners) {
            listener.onCount(value(TYPE.TAG_PREPAY));
        }
    }

    public interface onCountChangeListener{
        public void onCount(int count);
    }
}
