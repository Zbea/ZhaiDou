package com.zhaidou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;

import java.util.List;

/**
 * Created by wangclark on 15/6/10.
 */
public class SearchAdapter extends BaseListAdapter<String> {
    public SearchAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView=mInflater.inflate(R.layout.search_item_gv,null);
        TextView tv_item = ViewHolder.get(convertView,R.id.tv_search_item);
        String item = getList().get(position);
        tv_item.setText(item);
        return convertView;
    }
}
