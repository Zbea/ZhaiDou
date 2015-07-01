package com.zhaidou.fragments;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.model.User;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.PhotoUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProfileFragment extends Fragment implements View.OnClickListener,PhotoMenuFragment.MenuSelectListener,EditProfileFragment.RefreshDataListener{
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
    private TextView tv_mobile;

    private RelativeLayout rl_nickname;
    private RelativeLayout rl_mobile;

    private FrameLayout mMenuContainer;
    private FrameLayout mChildContainer;
    private PhotoMenuFragment menuFragment;

    RequestQueue mRequestQueue;
    private AsyncImageLoader1 imageLoader;

    private SharedPreferences mSharedPreferences;
    private String token;
    private int id;
    private String profileId;

    private final int UPDATE_PROFILE_INFO=0;
    private final int UPDATE_USER_INFO=1;
    private final int MENU_CAMERA_SELECTED=0;
    private final int MENU_PHOTO_SELECTED=1;

    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    public String filePath = "";

    private ProfileListener profileListener;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PROFILE_INFO:
                    User user=(User)msg.obj;
                    Log.i("imageLoader---->",imageLoader.toString());
                    tv_nick.setText(TextUtils.isEmpty(user.getNickName())?"":user.getNickName());
                    tv_intro.setText(TextUtils.isEmpty(user.getDescription())?"":user.getDescription());
                    tv_mobile.setText(TextUtils.isEmpty(user.getMobile())?"":user.getMobile());
                    tv_job.setText(user.isVerified()?"宅豆认证工程师":"未认证工程师");
//                    tv_gender.setText("male".equalsIgnoreCase(user.getGender())?"男":"女");
                    break;
                case UPDATE_USER_INFO:
                    User user1=(User)msg.obj;
                    Log.i("user1-->",user1.toString());
                    imageLoader.LoadImage("http://"+user1.getAvatar(),iv_header);
                    tv_email.setText(user1.getEmail());
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
        mChildContainer=(FrameLayout)view.findViewById(R.id.fl_child_container);
        view.findViewById(R.id.rl_header_layout).setOnClickListener(this);
        view.findViewById(R.id.ll_back).setOnClickListener(this);
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
        tv_mobile=(TextView)view.findViewById(R.id.tv_mobile);


        rl_mobile=(RelativeLayout)view.findViewById(R.id.rl_mobile);
        rl_nickname=(RelativeLayout)view.findViewById(R.id.rl_nickname);

        rl_nickname.setOnClickListener(this);
        rl_mobile.setOnClickListener(this);
        tv_intro.setOnClickListener(this);


        mSharedPreferences=getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        mRequestQueue=  Volley.newRequestQueue(getActivity());
        imageLoader=new AsyncImageLoader1(getActivity());
        id=mSharedPreferences.getInt("userId",-1);
        menuFragment=PhotoMenuFragment.newInstance("","");
        if (menuFragment!=null)
            getChildFragmentManager().beginTransaction().replace(R.id.rl_header_menu,menuFragment).addToBackStack("").hide(menuFragment).commit();
        menuFragment.setMenuSelectListener(this);

        Log.i("id--------------->",id+"");
        if (id!=-1){
            getUserData();
            getUserInfo();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_back:
                ((MainActivity)getActivity()).popToStack(ProfileFragment.this);
                break;
            case R.id.rl_header_layout:
                mMenuContainer.setVisibility(View.VISIBLE);
                toggleMenu();
                break;
            case R.id.ll_add_v:
                break;
            case R.id.rl_nickname:
                EditProfileFragment profileFragment=EditProfileFragment.newInstance("nick_name",tv_nick.getText().toString(),profileId);
                profileFragment.setRefreshDataListener(this);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container,profileFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_mobile:
                EditProfileFragment mobileFragment=EditProfileFragment.newInstance("mobile",tv_mobile.getText().toString(),profileId);
                mobileFragment.setRefreshDataListener(this);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container,mobileFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_intro:
                EditProfileFragment introFragment=EditProfileFragment.newInstance("description",tv_intro.getText().toString(),profileId);
                introFragment.setRefreshDataListener(this);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container,introFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
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
        switch (position){
            case MENU_CAMERA_SELECTED:
                Toast.makeText(getActivity(),position+"--->"+tag,Toast.LENGTH_LONG).show();
                File dir = new File(ZhaiDou.MyAvatarDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 原图
                File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss")
                        .format(new Date()));
                filePath = file.getAbsolutePath();// 获取相片的保存路径
                Uri imageUri = Uri.fromFile(file);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                this.startActivityForResult(intent,
                        MENU_CAMERA_SELECTED);
                break;
            case MENU_PHOTO_SELECTED:
                Toast.makeText(getActivity(),position+"--->"+tag,Toast.LENGTH_LONG).show();
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                this.startActivityForResult(intent1,
                        MENU_PHOTO_SELECTED);
                break;
        }
        toggleMenu();
    }

    public void getUserData(){

        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.171/api/v1/users/"+id+"/profile",new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("getUserData---->",jsonObject.toString());
                JSONObject userObj = jsonObject.optJSONObject("profile");
                String nick_name=userObj.optString("nick_name");
                String mobile=userObj.optString("mobile");
                String description=userObj.optString("description");
                profileId=userObj.optString("id");
                boolean verified=userObj.optBoolean("verified");
                User user=new User(null,null,nick_name,verified,mobile,description);
                Message message=new Message();
                message.what=UPDATE_PROFILE_INFO;
                message.obj=user;
                mHandler.sendMessage(message);
                Log.i("user--->",user.toString());
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("volleyError---------->",volleyError.toString());
            }
        });
        mRequestQueue.add(request);
    }

    public void getUserInfo(){

        JsonObjectRequest request=new JsonObjectRequest("http://192.168.199.171/api/v1/users/"+id,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("getUserInfo---->",jsonObject.toString());
                JSONObject userObj = jsonObject.optJSONObject("user");
                String email=userObj.optString("email");
                String avatar=userObj.optJSONObject("avatar").optString("url");
                User user=new User(avatar,email,null,false,null,null);
                Message message=new Message();
                message.what=UPDATE_USER_INFO;
                message.obj=user;
                mHandler.sendMessage(message);
                Log.i("user--->",user.toString());
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
    public void popToStack(){

        Log.i("popToStack---->","popToStack");
        FragmentManager childFragmentManager = getChildFragmentManager();
        Log.i("childFragmentManager--->", childFragmentManager.getBackStackEntryCount()+"");
        childFragmentManager.popBackStack();
        Log.i("childFragmentManager--->", childFragmentManager.getBackStackEntryCount()+"");
    }

    @Override
    public void onRefreshData(String type, String msg, String json) {
        Log.i(type,type);
        Log.i(msg,msg);
        Log.i("json",json);
        if ("mobile".equalsIgnoreCase(type)){
            tv_mobile.setText(msg);
        }else if ("description".equalsIgnoreCase(type)){
            tv_intro.setText(msg);
        }else {
            tv_nick.setText(msg);
        }
        popToStack();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MENU_CAMERA_SELECTED:// 拍照修改头像
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(),"SD不可用",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i("拍照修改头像------------>","拍照修改头像");
                    isFromCamera = true;
                    File file = new File(filePath);
                    degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
                    Log.i("life", "拍照后的角度：" + degree);
                    startImageAction(Uri.fromFile(file), 200, 200,
                            2, true);
                }
                break;
            case MENU_PHOTO_SELECTED:// 本地修改头像
                Log.i("requestCode","本地修改头像");
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(),"SD不可用",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isFromCamera = false;
                    uri = data.getData();
                    startImageAction(uri, 200, 200,
                            2, true);
                } else {
                    Toast.makeText(getActivity(),"照片获取失败",Toast.LENGTH_SHORT).show();
                }

                break;
            case 2:// 裁剪头像返回
                // TODO sent to crop
                Log.i("裁剪头像返回----->","裁剪头像返回");
                if (data == null) {
                    Toast.makeText(getActivity(), "取消选择", Toast.LENGTH_SHORT).show();
                    Log.i("data == null----->","data == null");
                    return;
                } else {
                    saveCropAvator(data);
                }
                // 初始化文件路径
                filePath = "";
                break;
            default:
                break;
    }
    }
    private void startImageAction(Uri uri, int outputX, int outputY,
                                  int requestCode, boolean isCrop) {
        Log.i("startImageAction--------------->","startImageAction");
        Intent intent = null;
        if (isCrop) {
            intent = new Intent("com.android.camera.action.CROP");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        }
        Log.i("isCrop-----------------",isCrop+"");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }
    /**
     * 保存裁剪的头像
     *
     * @param data
     */
    private void saveCropAvator(Intent data) {
        Log.i("saveCropAvator--------->","saveCropAvator");
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            Log.i("life", "avatar - bitmap = " + bitmap);

            String base64str=PhotoUtil.bitmapToBase64(bitmap);
            Log.i("base64str0---------->",base64str);
            new UpLoadAvatar().execute(base64str);

//            if (bitmap != null) {
//                bitmap = PhotoUtil.toRoundCorner(bitmap, 0);//将图片变为圆角
//                if (isFromCamera && degree != 0) {
//                    bitmap = PhotoUtil.rotaingImageView(degree, bitmap);
//                }
//                iv_set_avator.setImageBitmap(bitmap);
//                // 保存图片
//                String filename = new SimpleDateFormat("yyMMddHHmmss")
//                        .format(new Date());
//                path = ZhaiDou.MyAvatarDir + filename;
//                PhotoUtil.saveBitmap(ZhaiDou.MyAvatarDir, filename,
//                        bitmap, true);
//                // 上传头像
//                if (bitmap != null && bitmap.isRecycled()) {
//                    bitmap.recycle();
//                }
//            }
        }
    }

    private class UpLoadAvatar extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String s=null;
            try{
                s=executeHttpPost(strings[0]);
            }catch (Exception e){

            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("UpLoadAvatar-------->",s);
            try {
                JSONObject jsonObject=new JSONObject(s);
                JSONObject userJson = jsonObject.optJSONObject("user");
                String avatar = userJson.optJSONObject("avatar").optJSONObject("thumb").optString("url");
                String email =userJson.optString("email");
                User user = new User();
                user.setAvatar(avatar);
                Log.i("onPostExecute-->avatar-->",avatar);
                user.setEmail(email);
                Log.i("onPostExecute-->email-->",email);
                Message message = new Message();
                message.what=UPDATE_USER_INFO;
                message.obj=user;
                mHandler.sendMessage(message);
                profileListener.onProfileChange(user);
            }catch (Exception e){

            }
        }
    }

    public String executeHttpPost(String base64) throws Exception {
        token=mSharedPreferences.getString("token", null);
        id=mSharedPreferences.getInt("userId",-1);
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();

            // 实例化HTTP方法
            HttpPost request = new HttpPost("http://192.168.199.171/api/v1/users/"+id);

            request.addHeader("SECAuthorization", token);
            // 创建名/值组列表
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();


            parameters.add(new BasicNameValuePair("_method","PUT"));
            parameters.add(new BasicNameValuePair("user[avatar]","data:image/png;base64,"+base64));

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

    public void setProfileListener(ProfileListener profileListener) {
        this.profileListener = profileListener;
    }

    public interface ProfileListener{
        public void onProfileChange(User user);
    }
}