package com.zhaidou.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.view.ListViewForScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 软装指南
 */
public class MagicGuideFragment extends BaseFragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private View rootView;
    private ListViewForScrollView listView;
    private StringAdapter arrayAdapter;
    private List<String> arrays=new ArrayList<String>();
    private HashMap<Integer,View> maps=new HashMap<Integer, View>();


    public static MagicGuideFragment newInstance(String param1, String param2) {
        MagicGuideFragment fragment = new MagicGuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public MagicGuideFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView)
        {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent)
            {
                parent.removeView(rootView);
            }
        } else
        {
            rootView = inflater.inflate(R.layout.fragment_magic_guide, container, false);
            initView();
        }
        return rootView;
    }

    private void initView()
    {
        rootView.findViewById(R.id.back_btn).setOnClickListener(this);
        listView=(ListViewForScrollView)rootView.findViewById(R.id.listView);

        String[] array=mContext.getResources().getStringArray(R.array.magicGuide);
        for (String item :array)
        {
            arrays.add(item);
        }
        arrayAdapter=new StringAdapter(mContext,arrays);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                ((MainActivity)getActivity()).popToStack(MagicGuideFragment.this);
                break;
        }
    }


//    public class StringAdapter extends BaseAdapter
//    {
//        ViewHolder viewHolder;
//
//        class ViewHolder
//        {
//            TypeFaceTextView itemName;
//        }
//
//        @Override
//        public int getCount()
//        {
//            return arrays.size();
//        }
//
//        @Override
//        public Object getItem(int arg0)
//        {
//            return arrays.get(arg0);
//        }
//
//        @Override
//        public long getItemId(int position)
//        {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent)
//        {
//            if (convertView == null)
//            {
//                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_magic_guide_list, null);
//                viewHolder = new ViewHolder();
//                viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.guideTitle);
//                convertView.setTag(viewHolder);
//            }
//            else
//            {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//
//            viewHolder.itemName.setText(arrays.get(position));
//
//            return convertView;
//        }
//    }

    public class StringAdapter extends BaseListAdapter<String>
    {
        public StringAdapter(Context context, List<String> strings)
        {
            super(context, strings);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent)
        {
            convertView=maps.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_magic_guide_list,null);
            TextView tv_name = ViewHolder.get(convertView, R.id.guideTitle);
            tv_name.setText(getList().get(position));
            maps.put(position,convertView);
            return convertView;
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_main_magic_guide)); //统计页面
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_main_magic_guide));
    }
}
