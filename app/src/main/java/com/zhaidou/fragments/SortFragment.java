package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.activities.SearchActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SortFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SortFragment extends BaseFragment implements AdapterView.OnItemClickListener,
                                  View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    // TODO: Rename and change types of parameters
    private String mPage;
    private int mIndex;

    private int checked;
    private ListView mListView;
    private List<String> data;
    private SortAdapter mSortAdapter;
    private HashMap<String,Integer> checkedMap = new HashMap<String, Integer>();
    private RefreshDataListener mRefreshDataListener;
    private RelativeLayout mRelativeLayout;
    public static String TAG=SortFragment.class.getSimpleName();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SortFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SortFragment newInstance(String page, int index) {
        SortFragment fragment = new SortFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public SortFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
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
        ((SearchActivity)getActivity()).toggleSortMenu();
//        mRefreshDataListener.refreshData(data.get(i));
        mRefreshDataListener.refreshData(i);
    }

    public void setRefreshDataListener(RefreshDataListener mRefreshDataListener) {
        this.mRefreshDataListener = mRefreshDataListener;
    }

    public interface RefreshDataListener{
//        public void refreshData(String sortMsg);
        public void refreshData(int index);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_menu_close:
                ((SearchActivity)getActivity()).toggleSortMenu();
                break;
            default:
                break;
        }
    }
}
