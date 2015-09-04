package com.zhaidou.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.zhaidou.R;
import com.zhaidou.activities.DiyActivity;

import java.util.ArrayList;

public class DrawerFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private long lastClickTime = 0L;
    private String mParam1;
    private String mParam2;
    public static String TAG=DrawerFragment.class.getSimpleName();
    private ListView mListView;

    private ArrayList<String> list=new ArrayList<String>();

    private OnFragmentInteractionListener mListener;

    public static DrawerFragment newInstance(String param1, String param2) {
        DrawerFragment fragment = new DrawerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public DrawerFragment() {
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
        View view = inflater.inflate(R.layout.fragment_drawer,container,false);
        mListView=(ListView) view.findViewById(R.id.drawer_listview);

        for (int i = 0; i < 10; i++) {
            list.add(i+"");
        }


        mListView.setAdapter(new ArrayAdapter<String>(inflater.getContext(),android.R.layout.simple_list_item_1,
                list));
        mListView.setOnItemClickListener(this);
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        long thisClickTime = SystemClock.elapsedRealtime();
        if ((thisClickTime - lastClickTime) < 1000) {
            return;
        }

        lastClickTime = thisClickTime;

        String item = list.get(position);
        DiyCategoryFragment fragment = DiyCategoryFragment.newInstance(item,item);
        ((DiyActivity)getActivity()).addToStack(fragment);
    }
}
