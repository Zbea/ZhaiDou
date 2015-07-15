package com.zhaidou.fragments;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EditProfileFragment extends BaseFragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String PROFILE_ID="profileId";
    private static final String ARG_TITLE="title";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mProfileId;
    private String mTitle;

    private String token;

    private TextView tv_edit_msg,tv_done,tv_description,tv_length;
    private ImageView iv_cancel;
    private TextView mTitleView;

    private RequestQueue mRequestQueue;
    private SharedPreferences mSharedPreferences;

    private LinearLayout ll_input_msg;
    private RelativeLayout rl_description;

    RefreshDataListener refreshDataListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditProfileFragment newInstance(String param1, String param2,String profileId,String title) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(PROFILE_ID,profileId);
        args.putString(ARG_TITLE,title);
        fragment.setArguments(args);
        return fragment;
    }
    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mProfileId=getArguments().getString(PROFILE_ID);
            mTitle=getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_edit_profile, container, false);
        rl_description=(RelativeLayout)view.findViewById(R.id.rl_description);
        ll_input_msg=(LinearLayout)view.findViewById(R.id.ll_input_msg);
        mTitleView=(TextView)view.findViewById(R.id.tv_title);
        mTitleView.setText(mTitle);

        iv_cancel=(ImageView)view.findViewById(R.id.iv_cancel);
        tv_edit_msg=(EditText)view.findViewById(R.id.tv_edit_msg);
        tv_description=(EditText)view.findViewById(R.id.tv_description);
        tv_length=(TextView)view.findViewById(R.id.tv_length);
        tv_edit_msg.setText(TextUtils.isEmpty(mParam2)?"":mParam2);

        tv_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.i("beforeTextChanged--->","beforeTextChanged");
                Log.i("beforeTextChanged--charSequence",charSequence+"");
                Log.i("beforeTextChanged--i",""+i);
                Log.i("beforeTextChanged--i2",""+i2);
                Log.i("beforeTextChanged--i3",""+i3);
//                tv_length.setText((75-tv_description.getText().toString().length())+"");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.i("onTextChanged--->","beforeTextChanged");
                Log.i("onTextChanged--charSequence",charSequence+"");
                Log.i("onTextChanged--i",""+i);
                Log.i("onTextChanged--i2",""+i2);
                Log.i("onTextChanged--i3",""+i3);
                tv_length.setText((75-tv_description.getText().toString().length())+"");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("afterTextChanged--->","afterTextChanged");
            }
        });
        Log.i("mParam1",mParam1);
        Log.i("mParam2",mParam2);
        if ("description".equalsIgnoreCase(mParam1)){
            ll_input_msg.setVisibility(View.GONE);
            rl_description.setVisibility(View.VISIBLE);
            tv_description.setText(TextUtils.isEmpty(mParam2)?"":mParam2);
            tv_length.setText((75-(!TextUtils.isEmpty(tv_description.getText().toString())?tv_description.getText().toString().length():0))+"");
        }else {
            ll_input_msg.setVisibility(View.VISIBLE);
            rl_description.setVisibility(View.GONE);
        }
        mRequestQueue= Volley.newRequestQueue(getActivity());
        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        token=mSharedPreferences.getString("token", null);
        view.findViewById(R.id.ll_back).setOnClickListener(this);
        view.findViewById(R.id.tv_done).setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        return view;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_back:
                Log.i("------","dsfdsfs");
                ((ProfileFragment)getParentFragment()).popToStack();
                break;
            case R.id.iv_cancel:
                tv_edit_msg.setText("");
                break;
            case R.id.tv_done:
                hideInputMethod();
                new MyTask().execute(mParam1,mParam2,mProfileId);
                break;
        }
    }

    private class MyTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {

            String s = null;
            try {
                s =executeHttpPost(strings[0],strings[1],strings[2]);
            }catch (Exception e){

            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("EditProfileFragment---->onPostExecute---->",s);
            if ("description".equalsIgnoreCase(mParam1)){
                refreshDataListener.onRefreshData(mParam1,tv_description.getText().toString(),s);
            }else {
                refreshDataListener.onRefreshData(mParam1,tv_edit_msg.getText().toString(),s);
            }
        }
    }

    public String executeHttpPost(String type,String msg,String id) throws Exception {
        Log.i("type--->",type==null?"":type);
        Log.i("msg--->",msg==null?"":msg);
        Log.i("id--->",id==null?"":id);
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.USER_EDIT_PROFILE_URL+id);
            request.addHeader("SECAuthorization", token);


            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();

            parameters.add(new BasicNameValuePair("_method","PUT"));
            if ("description".equalsIgnoreCase(type)){
                String old = tv_description.getText().toString();
                String newStr = new String(old.getBytes("UTF-8"));
                parameters.add(new BasicNameValuePair("profile["+type+"]",newStr));
            }else {
                String old = tv_edit_msg.getText().toString();
                String newStr = new String(old.getBytes("UTF-8"));
                parameters.add(new BasicNameValuePair("profile["+type+"]",newStr));
            }
            parameters.add(new BasicNameValuePair("profile[id]",id));

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    parameters, HTTP.UTF_8);//这里要设置，不然回来乱码
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
            Log.i("EditProfileFragment--------->",result);
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

    public void setRefreshDataListener(RefreshDataListener refreshDataListener) {
        this.refreshDataListener = refreshDataListener;
    }

    public interface RefreshDataListener{
        public void onRefreshData(String type,String msg,String json);
    }
}
