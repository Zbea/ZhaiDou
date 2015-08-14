package com.zhaidou.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.model.User;
import com.zhaidou.utils.ToolUtils;

public class PersonalMainFragment extends Fragment implements View.OnClickListener,RegisterFragment.RegisterOrLoginListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private FrameLayout mContainer;
    private FrameLayout mChildContainer;
    private RegisterFragment mRegisterFragment;
    private LoginFragment mLoginFragment;
    private SharedPreferences mSharedPreferences;

    private String token;
    private int id;

    private int userId;

    // TODO: Rename and change types and number of parameters
    public static PersonalMainFragment newInstance(String param1, String param2) {
        PersonalMainFragment fragment = new PersonalMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PersonalMainFragment() {
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
        View view=inflater.inflate(R.layout.fragment_personal_main, container, false);

        mContainer=(FrameLayout)view.findViewById(R.id.fl_container);
        mChildContainer=(FrameLayout)view.findViewById(R.id.fl_child_container);

        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);


        mRegisterFragment=RegisterFragment.newInstance("","");
        mRegisterFragment.setRegisterOrLoginListener(this);

        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction =manager.beginTransaction();
        PersonalFragment personalFragment = PersonalFragment.newInstance("","");
        transaction.add(R.id.fl_container,personalFragment,PersonalFragment.class.getSimpleName()).show(personalFragment).commit();

        token=mSharedPreferences.getString("token", null);
        id=mSharedPreferences.getInt("userId",-1);

        if (TextUtils.isEmpty(token)){
            Toast.makeText(getActivity(),"VISIBLE",0).show();
            mLoginFragment=LoginFragment.newInstance("","");
            addToStack(mLoginFragment);
            mChildContainer.setVisibility(View.VISIBLE);

        }else {
            Toast.makeText(getActivity(),"GONE",0).show();
            mChildContainer.setVisibility(View.GONE);
        }

//        mChildContainer.setVisibility(token==mSharedPreferences.getString("token",null)?View.VISIBLE:View.GONE);
        return view;
    }

    @Override
    public void onResume() {
        userId = mSharedPreferences.getInt("userId",-1);
//        if (TextUtils.isEmpty(userId))

        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
                break;
            case R.id.tv_register:
//                getChildFragmentManager().beginTransaction().add(
//                        R.id.container, mRegisterFragment, RegisterFragment.TAG
//                ).addToBackStack(RegisterFragment.TAG).commit();
                addToStack(RegisterFragment.newInstance("",""));
                mContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_reset_psw:
                break;
            default:
                break;
        }
    }

    @Override
    public void onRegisterOrLoginSuccess(User user,Fragment fragment) {
        saveUserToSP(user);

        ToolUtils.setLog("要刷新登录了");
        Intent intent=new Intent(ZhaiDou.IntentRefreshLoginTag);
        getActivity().sendBroadcast(intent);

        FragmentManager manager = getChildFragmentManager();
        if (manager.findFragmentByTag(RegisterFragment.class.getSimpleName())!=null)
            manager.popBackStack();
        if (manager.findFragmentByTag(LoginFragment.class.getSimpleName())!=null)
            manager.popBackStack();
    }
    private void saveUserToSP(User user){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("userId",user.getId());
        editor.putString("email", user.getEmail());
        editor.putString("token",user.getAuthentication_token());
        editor.putString("avatar",user.getAvatar());
        editor.putString("nickName",user.getNickName());
        editor.commit();
    }

    public void addToStack(Fragment fragment){
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fl_child_container,fragment,fragment.getClass().getSimpleName());
        System.out.println("fragment.getTag()------->" + fragment.getTag());
        transaction.addToBackStack(null);
        transaction.commit();
        mChildContainer.setVisibility(View.VISIBLE);
        showTabContainer(View.GONE);
    }
    public void popToStack(){

        Log.i("popToStack---->","popToStack");
        FragmentManager childFragmentManager = getChildFragmentManager();
        Log.i("childFragmentManager--->", childFragmentManager.getBackStackEntryCount()+"");
        childFragmentManager.popBackStack();
        Log.i("childFragmentManager--->", childFragmentManager.getBackStackEntryCount()+"");
    }
    public void toggleTabContainer(){
        ((MainActivity)getActivity()).toggleTabContainer();
    }

    public void showTabContainer(int visible){
        ((MainActivity)getActivity()).toggleTabContainer(visible);
    }
}
