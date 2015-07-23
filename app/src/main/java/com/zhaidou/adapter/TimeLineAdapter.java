package com.zhaidou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.model.GroupStatusEntity;

import java.util.List;

/**
 * Created by wangclark on 15/7/23.
 */
public class TimeLineAdapter extends BaseExpandableListAdapter{
    private LayoutInflater inflater = null;
    private List<GroupStatusEntity> groupList;

    public TimeLineAdapter(Context context,
                               List<GroupStatusEntity> group_list) {
        this.groupList = group_list;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 返回一级Item总数
     */
    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return groupList.size();
    }

    /**
     * 返回二级Item总数
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupList.get(groupPosition).getChildList() == null) {
            return 0;
        }else if (groupPosition==groupList.size()-1){
            return 0;
        }else {
            return 1;
        }
    }

    /**
     * 获取一级Item内容
     */
    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return groupList.get(groupPosition);
    }

    /**
     * 获取二级Item内容
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groupList.get(groupPosition).getChildList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        GroupViewHolder holder = new GroupViewHolder();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_status_item, null);
        }
        holder.groupName = (TextView) convertView
                .findViewById(R.id.one_status_name);

        holder.groupName.setText(groupList.get(groupPosition).getGroupName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder viewHolder = null;
//        ChildStatusEntity entity = (ChildStatusEntity) getChild(groupPosition,
//                childPosition);
        if (convertView != null) {
            viewHolder = (ChildViewHolder) convertView.getTag();
        } else {
            viewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.adapter_timeline_child, null);
//            viewHolder.twoStatusTime = (TextView) convertView
//                    .findViewById(R.id.two_complete_time);
        }
//        viewHolder.twoStatusTime.setText(entity.getCompleteTime());

        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }

    private class GroupViewHolder {
        TextView groupName;
    }

    private class ChildViewHolder {
        public TextView twoStatusTime;
    }

}
