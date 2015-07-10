package com.zhaidou.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * Created by wangclark on 15/7/10.
 */
public class CategoryAdapter extends BaseExpandableListAdapter {
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return 0;
    }

    @Override
    public int getGroupCount() {
        return 0;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return false;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public Object getChild(int i, int i2) {
        return null;
    }

    @Override
    public long getChildId(int i, int i2) {
        return 0;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }
}
