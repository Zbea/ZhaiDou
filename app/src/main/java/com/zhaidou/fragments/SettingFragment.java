package com.zhaidou.fragments;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.User;
import com.zhaidou.utils.SharedPreferencesUtil;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SettingFragment extends BaseFragment implements View.OnClickListener,ProfileFragment.ProfileListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int CLEAR_USER_DATA=0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ProfileFragment mProfileFragment;

    SharedPreferences mSharedPreferences;
    RequestQueue requestQueue;
    private ProfileListener profileListener;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CLEAR_USER_DATA:
                    SharedPreferencesUtil.clearUser(getActivity());
                    ((MainActivity)getActivity()).logout(SettingFragment.this);
                    break;
            }
        }
    };
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
        view.findViewById(R.id.bt_logout).setOnClickListener(this);
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(getActivity());
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_back:
//                ((PersonalMainFragment)getParentFragment()).popToStack();
                ((MainActivity)getActivity()).popToStack(SettingFragment.this);
                break;
            case R.id.ll_profile:
                mProfileFragment=ProfileFragment.newInstance("","");
                mProfileFragment.setProfileListener(this);
//                ((PersonalMainFragment)getParentFragment()).addToStack(mProfileFragment);
                ((MainActivity)getActivity()).navigationToFragment(mProfileFragment);
                break;
            case R.id.ll_competition:
                WebViewFragment webViewFragment=WebViewFragment.newInstance("http://www.zhaidou.com/competitions/current?zdclient=ios",true);
//                ((PersonalMainFragment)getParentFragment()).addToStack(webViewFragment);
                ((MainActivity)getActivity()).navigationToFragment(webViewFragment);
                break;
            case R.id.ll_bbs_question:
                break;
            case R.id.ll_collocation:
                ImageBgFragment fragment= ImageBgFragment.newInstance("豆搭教程");
//                ((PersonalMainFragment)getParentFragment()).addToStack(fragment);
                ((MainActivity)getActivity()).navigationToFragment(fragment);
                break;
            case R.id.ll_add_v:
                ImageBgFragment addVFragment= ImageBgFragment.newInstance("如何加V");
//                ((PersonalMainFragment)getParentFragment()).addToStack(addVFragment);
                ((MainActivity)getActivity()).navigationToFragment(addVFragment);
                break;
            case R.id.ll_award_history:
                break;
            case R.id.ll_score:
                break;
            case R.id.ll_about:
                AboutFragment aboutFragment = AboutFragment.newInstance("","");
//                ((PersonalMainFragment)getParentFragment()).addToStack(aboutFragment);
                ((MainActivity)getActivity()).navigationToFragment(aboutFragment);
                break;
            case R.id.bt_logout:
                logout();
                break;
            default:
                break;
        }
    }

    public void logout(){

        JsonObjectRequest request=new JsonObjectRequest(ZhaiDou.USER_LOGOUT_URL
         ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("SettingFragment---->",jsonObject.toString());

                mHandler.sendEmptyMessage(CLEAR_USER_DATA);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        requestQueue.add(request);
    }

    @Override
    public void onProfileChange(User user) {
        Log.i("SettingFragment--->","onProfileChange");
        profileListener.onProfileChange(user);
    }

    public void setProfileListener(ProfileListener profileListener) {
        this.profileListener = profileListener;
    }

    public interface ProfileListener{
        public void onProfileChange(User user);
    }


}
