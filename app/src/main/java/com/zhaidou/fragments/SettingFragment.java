package com.zhaidou.fragments;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zhaidou.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SettingFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ProfileFragment mProfileFragment;




    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SettingFragment() {
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
        View view=inflater.inflate(R.layout.fragment_setting, container, false);

        view.findViewById(R.id.rl_back).setOnClickListener(this);
        view.findViewById(R.id.ll_profile).setOnClickListener(this);
        view.findViewById(R.id.ll_competition).setOnClickListener(this);
        view.findViewById(R.id.ll_bbs_question).setOnClickListener(this);
        view.findViewById(R.id.ll_collocation).setOnClickListener(this);
        view.findViewById(R.id.ll_add_v).setOnClickListener(this);
        view.findViewById(R.id.ll_award_history).setOnClickListener(this);
        view.findViewById(R.id.ll_score).setOnClickListener(this);
        view.findViewById(R.id.ll_about).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_back:
                ((PersonalMainFragment)getParentFragment()).popToStack();
                break;
            case R.id.ll_profile:
                mProfileFragment=ProfileFragment.newInstance("","");
                ((PersonalMainFragment)getParentFragment()).addToStack(mProfileFragment);
                break;
            case R.id.ll_competition:
                WebViewFragment webViewFragment=WebViewFragment.newInstance("http://www.zhaidou.com/competitions/current?zdclient=ios");
                ((PersonalMainFragment)getParentFragment()).addToStack(webViewFragment);
                break;
            case R.id.ll_about:
                AboutFragment aboutFragment = AboutFragment.newInstance("","");
                ((PersonalMainFragment)getParentFragment()).addToStack(aboutFragment);
                break;
            default:
                break;
        }
    }
}
