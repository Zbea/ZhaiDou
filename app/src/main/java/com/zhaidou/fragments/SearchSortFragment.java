package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SearchSortFragment extends BaseFragment implements AdapterView.OnItemClickListener,
                                  View.OnClickListener{
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;

    private int checked;
    private ListView mListView;
    private List<String> data;
    private SortAdapter mSortAdapter;
    private HashMap<String,Integer> checkedMap = new HashMap<String, Integer>();
    private RefreshDataListener mRefreshDataListener;
    private RelativeLayout mRelativeLayout;
    public static String TAG=SearchSortFragment.class.getSimpleName();


    public static SearchSortFragment newInstance(String page, int index) {
        SearchSortFragment fragment = new SearchSortFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public SearchSortFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_sort, container, false);
        mListView=(ListView)view.findViewById(R.id.lv_sort);
        mRelativeLayout=(RelativeLayout)view.findViewById(R.id.rl_menu_close);
        mRelativeLayout.setOnClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(this);

        checkedMap.put("product",0);
        checkedMap.put("strategy",0);

        mSortAdapter = new SortAdapter(getActivity(),new ArrayList<String>());
        mListView.setAdapter(mSortAdapter);

        return view;
    }

    private class SortAdapter extends BaseListAdapter<String>{
        public SortAdapter(Context context, List<String> list) {
            super(context, list);
        }
        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.item_sort_sortfragment,null);
            CheckedTextView mTextView = ViewHolder.get(convertView,R.id.ct_text);
            String item = getList().get(position);
            mTextView.setText(item);
            mTextView.setChecked(checkedMap.get(mPage)!=null&&position==checkedMap.get(mPage));
            return convertView;
        }
    }

    public void setData(int page,int checked){
        this.checked=checked;
        if (page==0){
            mPage="product";
            data=Arrays.asList(getResources().getStringArray(R.array.product_sort));
        }else {
            mPage="strategy";
            data=Arrays.asList(getResources().getStringArray(R.array.strategy_sort));
        }
        mSortAdapter.setList(data);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        boolean isChecked = mListView.isItemChecked(i);
        checkedMap.put(mPage,i);
        mSortAdapter.notifyDataSetChanged();
        ((SearchFragment)getParentFragment()).toggleSortMenu();
//        mRefreshDataListener.refreshData(data.get(i));
        mRefreshDataListener.refreshData(i);
    }

    public void setRefreshDataListener(RefreshDataListener mRefreshDataListener) {
        this.mRefreshDataListener = mRefreshDataListener;
    }

    public interface RefreshDataListener{
        public void refreshData(int index);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_menu_close:
                ((SearchFragment)getParentFragment()).toggleSortMenu();
                break;
            default:
                break;
        }
    }
}
