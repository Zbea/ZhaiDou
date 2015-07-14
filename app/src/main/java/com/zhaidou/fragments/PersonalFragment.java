package com.zhaidou.fragments;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.User;
import com.zhaidou.utils.AsyncImageLoader1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PersonalFragment extends BaseFragment implements View.OnClickListener,CollectFragment.CollectCountChangeListener,
        CollocationFragment.CollocationCountChangeListener,SettingFragment.ProfileListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TabPageIndicator mIndicator;
    private ViewPager mViewpager;

    private List<Fragment> mFragments;
    private CollectFragment mCollectFragment;
    private CollocationFragment mCollocationFragment;
    private SettingFragment mSettingFragment;
    private PersonalFragmentAdapter mAdapter;
    private TextView tv_nickname,tv_province,tv_city,tv_desc;
    private ImageView iv_setting;
    private ImageView iv_header;

    private SharedPreferences mSharedPreferences;

    AsyncImageLoader1 imageLoader;
    private RequestQueue mRequestQueue;
    private final int UPDATE_USER_INFO=1;
    private final int UPDATE_USER_DESCRIPTION=2;

    private Map<String,String> cityMap = new HashMap<String, String>();

    private int collect_count=0;
    private int collocation_count=0;

    private User user;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_USER_INFO:
                    User user=(User)msg.obj;
                    imageLoader.LoadImage("http://"+user.getAvatar(),iv_header);
                    if (!TextUtils.isEmpty(user.getNickName()))
                        tv_nickname.setText(user.getNickName());
                    tv_province.setText(cityMap.get(user.getProvince()));
                    tv_city.setText(cityMap.get(user.getCity()));
                    break;
                case UPDATE_USER_DESCRIPTION:
                    User u = (User)msg.obj;
                    tv_desc.setText("null".equalsIgnoreCase(u.getDescription())||u.getDescription() == null ? "" : u.getDescription());
                    break;
            }
        }
    };

    // TODO: Rename and change types and number of parameters
    public static PersonalFragment newInstance(String param1, String param2) {
        PersonalFragment fragment = new PersonalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PersonalFragment() {
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
        View view=inflater.inflate(R.layout.fragment_personal, container, false);
        mIndicator=(TabPageIndicator)view.findViewById(R.id.tab_personal);
        mViewpager=(ViewPager)view.findViewById(R.id.vp_personal);
        iv_setting=(ImageView)view.findViewById(R.id.iv_setting);
        iv_header=(ImageView)view.findViewById(R.id.iv_header);
        tv_city=(TextView)view.findViewById(R.id.tv_city);
        tv_province=(TextView)view.findViewById(R.id.tv_province);
        tv_nickname=(TextView)view.findViewById(R.id.tv_nickname);
        tv_desc=(TextView)view.findViewById(R.id.tv_desc);

        mFragments=new ArrayList<Fragment>();
        mCollectFragment =CollectFragment.newInstance("","");
        mCollocationFragment=CollocationFragment.newInstance("","");
        mSettingFragment=SettingFragment.newInstance("","");

        mSettingFragment.setProfileListener(this);

        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        mCollectFragment.setCollectCountChangeListener(this);
        mCollocationFragment.setCollocationCountChangeListener(this);

        mRequestQueue= Volley.newRequestQueue(getActivity());
        imageLoader=new AsyncImageLoader1(getActivity());

        mFragments.add(mCollectFragment);
        mFragments.add(mCollocationFragment);

        mAdapter =new PersonalFragmentAdapter(getFragmentManager());
        mViewpager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewpager);
        Button login=(Button)view.findViewById(R.id.bt_login);
        login.setOnClickListener(this);
        iv_setting.setOnClickListener(this);
        getCityList();
        return view;
    }

    @Override
    public void onClick(View view) {
//        LoginFragment fragment = LoginFragment.newInstance("","");
//        ((PersonalMainFragment)getParentFragment()).addToStack(fragment);
        switch (view.getId()){
            case R.id.bt_login:
                ((PersonalMainFragment)getParentFragment()).toggleTabContainer();
                break;
            case R.id.iv_setting:
                Log.i("iv_setting---->","iv_setting");
//                ((PersonalMainFragment)getParentFragment()).addToStack(mSettingFragment);
                ((MainActivity)getActivity()).navigationToFragment(mSettingFragment);
                break;
        }

    }
    private class PersonalFragmentAdapter extends FragmentPagerAdapter {
        public PersonalFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position==0)
                return collect_count==0?"收藏":"收藏 "+collect_count;
            return collocation_count==0?"豆搭":"豆搭 "+collocation_count;
        }
    }

    public void getUserInfo(){

        int id=mSharedPreferences.getInt("userId",-1);
        JsonObjectRequest request=new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL+id,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject userObj = jsonObject.optJSONObject("user");
                String email=userObj.optString("email");
                String avatar=userObj.optJSONObject("avatar").optString("url");
                String nick_name=userObj.optString("nick_name");
                String province=userObj.optString("province");
                String city=userObj.optString("city");
                User user=new User();
                user.setAvatar(avatar);
                user.setNickName(nick_name);
                user.setProvince(province);
                user.setCity(city);
                Message message=new Message();
                message.what=UPDATE_USER_INFO;
                message.obj=user;
                mHandler.sendMessage(message);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("volleyError---------->",volleyError.toString());
            }
        });
        mRequestQueue.add(request);
    }

    public void getUserDetail(){
        int id=mSharedPreferences.getInt("userId",-1);
        JsonObjectRequest request=new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL+id+"/profile",new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject userObj = jsonObject.optJSONObject("profile");
                String nick_name=userObj.optString("nick_name");
                String mobile=userObj.optString("mobile");
                String description=userObj.optString("description");
//                int profileId=userObj.optString("id");
                boolean verified=userObj.optBoolean("verified");
                User user=new User(null,null,nick_name,verified,mobile,description);
                Message message=new Message();
                message.what=UPDATE_USER_DESCRIPTION;
                message.obj=user;
                mHandler.sendMessage(message);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("volleyError---------->",volleyError.toString());
            }
        });
        mRequestQueue.add(request);
    }

    public void getCityList(){
        String city = getString(R.string.city);
        try{
            JSONArray cityArr = new JSONArray(city);
            for(int i=0;i<cityArr.length();i++){
                JSONObject cityObj = cityArr.optJSONObject(i);
                String index = cityObj.optString("index");
                String desc =cityObj.optString("desc");
                cityMap.put(index,desc);
            }
            getUserInfo();
            getUserDetail();
        }catch (Exception e){

        }
    }

    @Override
    public void onCountChange(int count,Fragment fragment) {
        Log.i("onCountChange------->",count+"");
        if (fragment instanceof CollectFragment){
            collect_count=count;
        }else if (fragment instanceof CollocationFragment){
            collocation_count=count;
        }
        mIndicator.notifyDataSetChanged();
    }

    @Override
    public void onProfileChange(User user) {
        Log.i("PersonalFragment--->","onProfileChange");
        Log.i("onProfileChange---->",user.toString());
        if (!TextUtils.isEmpty(user.getNickName()))
            tv_nickname.setText(user.getNickName());
        if (!TextUtils.isEmpty(user.getAvatar()))
            imageLoader.LoadImage("http://"+user.getAvatar(),iv_header);
        if (!TextUtils.isEmpty(user.getDescription()))
            tv_desc.setText(user.getDescription());
    }

    public void refreshData(){
        Log.i("PersonalFragment------->","refreshData");
        getUserDetail();
        getUserInfo();
        if (mFragments.size()>0){
            mCollocationFragment.refreshData();
            mCollectFragment.refreshData();
        }
    }
}
