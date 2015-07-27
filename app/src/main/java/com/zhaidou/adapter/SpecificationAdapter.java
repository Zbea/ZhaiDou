package com.zhaidou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Specification;
import java.util.List;

/**
 * Created by wangclark on 15/7/27.
 */
public class SpecificationAdapter extends BaseListAdapter<Specification>{
    int mCheckPosition=0;
    public SpecificationAdapter(Context context, List<Specification> list,int checked) {
        super(context, list);
        mCheckPosition=checked;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView=mInflater.inflate(R.layout.goods_details_size_item,null);
        TextView tv_item = ViewHolder.get(convertView, R.id.sizeTitleTv);
        Specification specification=getList().get(position);
        tv_item.setText(specification.getTitle());
        tv_item.setSelected(position==mCheckPosition?true:false);
        return convertView;
    }

    public void setCheckPosition(int mCheckPosition) {
        this.mCheckPosition = mCheckPosition;
    }
}
