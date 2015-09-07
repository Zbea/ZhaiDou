package com.zhaidou.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.activities.DiyActivity;
import com.zhaidou.base.BaseFragment;


public class DiyCategoryFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public static String TAG=DiyCategoryFragment.class.getSimpleName();
    private Button bt_back;

    private OnFragmentInteractionListener mListener;

    public static DiyCategoryFragment newInstance(String param1, String param2) {
        DiyCategoryFragment fragment = new DiyCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public DiyCategoryFragment() {
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
        View view = inflater.inflate(R.layout.fragment_diy_category,null);
        System.out.println("item------------>"+mParam1);
        Button mButton =(Button) view.findViewById(R.id.tv_text);
        bt_back=(Button) view.findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((DiyActivity)getActivity()).popToStack();
            }
        });
        mButton.setText(mParam1);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiyDetailFragment fragment = DiyDetailFragment.newInstance(mParam1,mParam2);
                ((DiyActivity)getActivity()).addToStack(fragment);
            }
        });
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

}
