package com.zhaidou.fragments;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.model.User;
import com.zhaidou.utils.AsyncImageLoader1;

import org.json.JSONObject;
import org.w3c.dom.Text;


public class ProfileFragment extends Fragment implements View.OnClickListener,PhotoMenuFragment.MenuSelectListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView iv_header;
    private TextView tv_nick;
    private TextView tv_email;
    private TextView tv_gender;
    private TextView tv_birthday;
    private TextView tv_city;
    private TextView tv_job;
    private TextView tv_company;
    private TextView tv_register_time;
    private TextView tv_intro;
    private FrameLayout mMenuContainer;
    private PhotoMenuFragment menuFragment;

    RequestQueue mRequestQueue;
    private AsyncImageLoader1 imageLoader;

    private SharedPreferences mSharedPreferences;
    private int id;

    private final int UPDATE_PROFILE_INFO=0;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PROFILE_INFO:
                    User user=(User)msg.obj;
                    Log.i("imageLoader---->",imageLoader.toString());
                    imageLoader.LoadImage("http://"+user.getAvatar(),iv_header);
//                    getHeaderBitMap(user.getAvatar(),iv_header);
                    tv_nick.setText(user.getNickName());
                    tv_email.setText(user.getEmail());
                    tv_gender.setText("male".equalsIgnoreCase(user.getGender())?"男":"女");
//                    tv_birthday.setText(user.get);
//                    tv_job.setText(user.get);
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
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public ProfileFragment() {
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
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        mMenuContainer=(FrameLayout)view.findViewById(R.id.rl_header_menu);
        view.findViewById(R.id.rl_header_layout).setOnClickListener(this);
        iv_header=(ImageView)view.findViewById(R.id.iv_header);
        tv_nick=(TextView)view.findViewById(R.id.tv_nick);
        tv_email=(TextView)view.findViewById(R.id.tv_email);
        tv_gender=(TextView)view.findViewById(R.id.tv_gender);
        tv_birthday=(TextView)view.findViewById(R.id.tv_birthday);
        tv_city=(TextView)view.findViewById(R.id.tv_city);
        tv_job=(TextView)view.findViewById(R.id.tv_job);
        tv_company=(TextView)view.findViewById(R.id.tv_company);
        tv_register_time=(TextView)view.findViewById(R.id.tv_register_time);
        tv_intro=(TextView)view.findViewById(R.id.tv_intro);
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        mRequestQueue=  Volley.newRequestQueue(getActivity());
        imageLoader=new AsyncImageLoader1(getActivity());
        id=mSharedPreferences.getInt("userId",-1);
        menuFragment=PhotoMenuFragment.newInstance("","");
        if (menuFragment!=null)
            getChildFragmentManager().beginTransaction().replace(R.id.rl_header_menu,menuFragment).addToBackStack("").commit();
        menuFragment.setMenuSelectListener(this);

        Log.i("id--------------->",id+"");
        if (id!=-1){
            getUserData();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_header_layout:
                toggleMenu();
                mMenuContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void toggleMenu(){
        if (menuFragment!=null){
            if (menuFragment.isHidden()){
                getChildFragmentManager().beginTransaction().show(menuFragment).commit();
            }else {
                getChildFragmentManager().beginTransaction().hide(menuFragment).commit();
            }
        }

    }

    @Override
    public void onMenuSelect(int position, String tag) {
        Toast.makeText(getActivity(),position+"--->"+tag,1).show();
    }

    public void getUserData(){

        JsonObjectRequest request=new JsonObjectRequest("http://www.zhaidou.com/api/v1/users/"+id+"/profile",new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("jsonObject---->",jsonObject.toString());
                JSONObject userObj = jsonObject.optJSONObject("profile");
                String email=userObj.optString("email");
                String nick_name=userObj.optString("nick_name");
                String mobile=userObj.optString("mobile");
                String description=userObj.optString("description");

                Message message=new Message();
                message.what=UPDATE_PROFILE_INFO;
//                message.obj=user;
                mHandler.sendMessage(message);
//                Log.i("user--->",user.toString());
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("volleyError---------->",volleyError.toString());
            }
        });
        mRequestQueue.add(request);
    }

    public void getHeaderBitMap(String url, final ImageView image){
        ImageRequest irequest = new ImageRequest(
                "http://"+url,
                new Response.Listener<Bitmap>() {
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        image.setBackgroundDrawable(new BitmapDrawable(
                                getActivity().getResources(), bitmap));
                    }
                }, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
            }
        });
//        mRequestQueue.add(irequest);
    }
}
