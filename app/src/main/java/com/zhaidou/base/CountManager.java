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
 */
public class CountManager {
    private static CountManager mCountManage = null;
    private Map<Enum, Integer> map = new HashMap<Enum, Integer>();
    private onCountChangeListener onCountChangeListener;
    private List<onCountChangeListener> mListeners = new ArrayList<CountManager.onCountChangeListener>();
    private List<onCommentChangeListener> mCommentListeners = new ArrayList<onCommentChangeListener>();

    private CountManager() {

    }

    public static CountManager getInstance() {
        if (mCountManage == null) {
            mCountManage = new CountManager();
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
            integer = 0;
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
        init(type, i > 0 ? --i : 0);
    }

    public void clearCache() {
        map.clear();
        refreshData();
    }

    public int value(TYPE type) {
        Integer integer = map.get(type);
        return integer == null ? 0 : integer.intValue();
    }

    public enum TYPE {
        TAG_PREPAY, TAG_EASE, TAG_OTHER
    }

    public void setOnCountChangeListener(CountManager.onCountChangeListener onCountChangeListener) {
        if (this.onCountChangeListener == null)
            this.onCountChangeListener = onCountChangeListener;
        mListeners.add(onCountChangeListener);
    }

    public void setOnCommentChangeListener(onCommentChangeListener onCommentChangeListener) {
        mCommentListeners.add(onCommentChangeListener);
    }

    public void refreshData() {
        for (onCountChangeListener listener : mListeners) {
            listener.onCount(value(TYPE.TAG_PREPAY));
        }
    }

    public void notifyCommentChange() {
        for (onCommentChangeListener listener : mCommentListeners) {
            listener.onChange();
        }
    }

    public interface onCountChangeListener {
        public void onCount(int count);
    }

    public interface onCommentChangeListener {
        public void onChange();
    }
}
