package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pulltorefresh.PullToRefreshListView;
import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Logistics;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogisticsMsgFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LogisticsMsgFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PullToRefreshListView mLogisticsView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogisticsMsgFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogisticsMsgFragment newInstance(String param1, String param2) {
        LogisticsMsgFragment fragment = new LogisticsMsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LogisticsMsgFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_logistics, container, false);
        mLogisticsView=(PullToRefreshListView)view.findViewById(R.id.lv_logistics);
        return view;
    }

//    private class LogisticsAdapter extends BaseListAdapter<Logistics>{
//        public LogisticsAdapter(Context context, List<String> list) {
//            super(context, list);
//        }
//
//        @Override
//        public View bindView(int position, View convertView, ViewGroup parent) {
//            if (convertView==null)
//                convertView=mInflater.inflate(R.layout.search_item_gv,null);
//            return convertView;
//        }
//    }
}
