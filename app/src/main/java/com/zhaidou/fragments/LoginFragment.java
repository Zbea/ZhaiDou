package com.zhaidou.fragments;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.model.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LoginFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView mEmailView,mPswView,mRegisterView,mResetView;
    private Button mLoginView;
    public static final String TAG=LoginFragment.class.getSimpleName();


    private RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LoginFragment() {
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

        View view=inflater.inflate(R.layout.fragment_login, container, false);
        mEmailView=(TextView)view.findViewById(R.id.tv_email);
        mPswView=(TextView)view.findViewById(R.id.tv_password);
        mLoginView=(Button)view.findViewById(R.id.bt_login);
        mRegisterView=(TextView)view.findViewById(R.id.tv_register);
        mResetView=(TextView)view.findViewById(R.id.tv_reset_psw);


        mLoginView.setOnClickListener(this);
        mRegisterView.setOnClickListener(this);
        mResetView.setOnClickListener(this);
        view.findViewById(R.id.ll_back).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
                new MyTask().execute();
                break;
            case R.id.tv_register:
                RegisterFragment fragment = RegisterFragment.newInstance("","");
                if (getActivity()!=null&&getActivity() instanceof ItemDetailActivity){
                    Log.i("R.id.ll_back:","getActivity()!=null&&getActivity() instanceof ItemDetailActivity");
                    ((ItemDetailActivity)getActivity()).navigationToFragment(fragment);
                    return;
                }else if (getActivity()!=null&&getActivity() instanceof MainActivity){
                    ((MainActivity)getActivity()).navigationToFragment(fragment);
                    return;
                }
                ((PersonalMainFragment)getParentFragment()).addToStack(fragment);
                break;
            case R.id.tv_reset_psw:
                break;
            case R.id.ll_back:
                if (getActivity()!=null&&getActivity() instanceof ItemDetailActivity){
                    Log.i("R.id.ll_back:","getActivity()!=null&&getActivity() instanceof ItemDetailActivity");
                    ((ItemDetailActivity)getActivity()).popToStack();
                    return;
                }else if (getActivity()!=null&&getActivity() instanceof MainActivity){
                    Log.i("R.id.ll_back:","getActivity()!=null&&getActivity() instanceof MainActivity");
                    ((MainActivity)getActivity()).popToStack(this);
                    return;
                }
                ((PersonalMainFragment)getParentFragment()).popToStack();
                ((PersonalMainFragment)getParentFragment()).toggleTabContainer();
                break;
            default:
                break;
        }
    }

    private class MyTask extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... voids) {
            String str=null;
            try {
                String email = mEmailView.getText().toString();
                String password =mPswView.getText().toString();

                str = executeHttpPost(email,password);
            }catch (Exception e){

            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("onPostExecute------------>", s);
            try {
                JSONObject json = new JSONObject(s);
                Object obj = json.opt("message");
                if (obj!=null){
                    JSONArray errMsg =  json.optJSONArray("message");
                    Toast.makeText(getActivity(),errMsg.optString(0),Toast.LENGTH_LONG).show();
                    return;
                }

                Log.i("before--->","before");
                JSONArray userArr = json.optJSONArray("users");
                for (int i=0;i<userArr.length();i++){
                    JSONObject userObj = userArr.optJSONObject(i);
                    int id = userObj.optInt("id");
                    Log.i("id--->",id+"");
                    String email=userObj.optString("email");
                    Log.i("email--->",email);
                    String nick = userObj.optString("nick_name");
                    Log.i("nickname--->",nick);
                    String token=json.optJSONObject("user_tokens").optString("token");
                    Log.i("token--->",token);

                    User user = new User(id,email,token,nick,null);
                    Log.i("LoginFragment----onRegisterOrLoginSuccess---->","onRegisterOrLoginSuccess");
                    mRegisterOrLoginListener.onRegisterOrLoginSuccess(user,LoginFragment.this);
                }

            }catch (Exception e){

            }

        }
    }
    public String executeHttpPost(String email,String psw) throws Exception {
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost("http://192.168.199.171/api/v1/user_tokens");

            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();


            parameters.add(new BasicNameValuePair("user_token[email]",email));
            parameters.add(new BasicNameValuePair("user_token[password]",psw));
//            parameters.add(new BasicNameValuePair("user[nick_name]",nick));

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    parameters);
            request.setEntity(formEntiry);
            // 执行请求
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String result = sb.toString();
            return result;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setRegisterOrLoginListener(RegisterFragment.RegisterOrLoginListener mRegisterOrLoginListener) {
        this.mRegisterOrLoginListener = mRegisterOrLoginListener;
    }
}
