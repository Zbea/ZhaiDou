package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.User;
import com.zhaidou.utils.NativeHttpUtil;
import com.zhaidou.utils.PhotoUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, PhotoMenuFragment.MenuSelectListener,
        ProfileEditFragment.RefreshDataListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private View view;
    private Context mContext;

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
    private TextView tv_addr_username;
    private TextView tv_addr_mobile;
    private TextView tv_addr, tv_delete, tv_edit, tv_addr_null;

    private RelativeLayout mWorkLayout;

    private RelativeLayout rl_nickname;
    private RelativeLayout rl_mobile, mIntroLayout;
    private LinearLayout ll_addr_info;

    private FrameLayout mMenuContainer;
    private FrameLayout mChildContainer;
    private PhotoMenuFragment menuFragment;

    RequestQueue mRequestQueue;

    private SharedPreferences mSharedPreferences;
    private String token;
    private int id;
    private String profileId;

    private final int UPDATE_PROFILE_INFO = 0;
    private final int UPDATE_USER_INFO = 1;
    private final int MENU_CAMERA_SELECTED = 0;
    private final int MENU_PHOTO_SELECTED = 1;

    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    public String filePath = "";

    private ProfileListener profileListener;

    private Dialog mDialog;
    private User user;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROFILE_INFO:
                    user = (User) msg.obj;
                    tv_intro.setText(TextUtils.isEmpty(user.getDescription()) ? "" : user.getDescription());
                    tv_mobile.setText(TextUtils.isEmpty(user.getMobile()) ? "" : user.getMobile());
                    tv_job.setText(user.isVerified() ? "宅豆认证设计师" : "未认证设计师");

                    tv_addr_mobile.setText(TextUtils.isEmpty(user.getMobile()) ? "" : user.getMobile());
                    tv_addr.setText(TextUtils.isEmpty(user.getAddress2()) ? "" : user.getAddress2());
                    tv_addr_username.setText(TextUtils.isEmpty(user.getFirst_name()) ? "" : user.getFirst_name());

                    if (TextUtils.isEmpty(user.getAddress2()) || "null".equals(user.getAddress2())) {
                        ll_addr_info.setVisibility(View.GONE);
                        tv_addr_null.setVisibility(View.VISIBLE);
                    } else {
                        ll_addr_info.setVisibility(View.VISIBLE);
                        tv_addr_null.setVisibility(View.GONE);
                    }
                    break;
                case UPDATE_USER_INFO:
                    User user1 = (User) msg.obj;
                    ToolUtils.setImageUrl("http://" + user1.getAvatar(), iv_header);
                    tv_email.setText(user1.getEmail());
                    tv_nick.setText(TextUtils.isEmpty(user1.getNickName()) ? "" : user1.getNickName());

                    break;
            }
            setEndLoading();
            initEndLoading();
        }
    };

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
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


        if (view == null) {
            view = inflater.inflate(R.layout.fragment_profile, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }

        return view;
    }

    private void initView() {
        setStartLoading();

        mMenuContainer = (FrameLayout) view.findViewById(R.id.rl_header_menu);
        mChildContainer = (FrameLayout) view.findViewById(R.id.fl_child_container);
        view.findViewById(R.id.rl_header_layout).setOnClickListener(this);
        view.findViewById(R.id.ll_back).setOnClickListener(this);
        iv_header = (ImageView) view.findViewById(R.id.iv_header);
        tv_nick = (TextView) view.findViewById(R.id.tv_nick);
        tv_email = (TextView) view.findViewById(R.id.tv_email);
        tv_gender = (TextView) view.findViewById(R.id.tv_gender);
        tv_birthday = (TextView) view.findViewById(R.id.tv_birthday);
        tv_city = (TextView) view.findViewById(R.id.tv_city);
        tv_job = (TextView) view.findViewById(R.id.tv_job);
        tv_company = (TextView) view.findViewById(R.id.tv_company);
        tv_register_time = (TextView) view.findViewById(R.id.tv_register_time);
        tv_intro = (TextView) view.findViewById(R.id.tv_intro);
        tv_mobile = (TextView) view.findViewById(R.id.tv_mobile);
        tv_addr_mobile = (TextView) view.findViewById(R.id.tv_addr_mobile);
        tv_addr_username = (TextView) view.findViewById(R.id.tv_addr_username);
        tv_addr = (TextView) view.findViewById(R.id.tv_addr);
        ll_addr_info = (LinearLayout) view.findViewById(R.id.ll_addr_info);
        tv_addr_null = (TextView) view.findViewById(R.id.tv_addr_null);
        tv_delete = (TextView) view.findViewById(R.id.tv_delete);
        tv_edit = (TextView) view.findViewById(R.id.tv_edit);

        tv_addr_null = (TextView) view.findViewById(R.id.tv_addr_null);

        mWorkLayout = (RelativeLayout) view.findViewById(R.id.rl_job);
        mWorkLayout.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        view.findViewById(R.id.rl_into).setOnClickListener(this);

        view.findViewById(R.id.rl_manage_address).setOnClickListener(this);

        rl_mobile = (RelativeLayout) view.findViewById(R.id.rl_mobile);
        rl_nickname = (RelativeLayout) view.findViewById(R.id.rl_nickname);

        rl_nickname.setOnClickListener(this);
        rl_mobile.setOnClickListener(this);

        mSharedPreferences = getActivity().getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        id = mSharedPreferences.getInt("userId", -1);
        menuFragment = PhotoMenuFragment.newInstance("", "");
        if (menuFragment != null)
            getChildFragmentManager().beginTransaction().replace(R.id.rl_header_menu, menuFragment).addToBackStack("").hide(menuFragment).commit();
        menuFragment.setMenuSelectListener(this);

        if (id != -1) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    getUserData();
                    getUserInfo();
                }
            }, 300);
        }
        token = mSharedPreferences.getString("token", null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ((MainActivity) getActivity()).popToStack(ProfileFragment.this);
                break;
            case R.id.rl_header_layout:
                mMenuContainer.setVisibility(View.VISIBLE);
                toggleMenu();
                break;
            case R.id.ll_add_v:
                break;
            case R.id.rl_nickname:
                ProfileEditFragment profileFragment = ProfileEditFragment.newInstance("nick_name", tv_nick.getText().toString().trim(), id+"", "个人昵称");
                profileFragment.setRefreshDataListener(this);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container, profileFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_mobile:
                ProfileEditFragment mobileFragment = ProfileEditFragment.newInstance("mobile", tv_mobile.getText().toString().trim(), profileId, "手机号码");
                mobileFragment.setRefreshDataListener(this);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container, mobileFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_job:
                if ("未认证设计师".equalsIgnoreCase(tv_job.getText().toString())) {
                    ImageBgFragment addVFragment = ImageBgFragment.newInstance("如何加V");
                    ((MainActivity) getActivity()).navigationToFragmentWithAnim(addVFragment);
                }
                break;
            case R.id.rl_manage_address:
                ProfileAddrFragment fragment = ProfileAddrFragment.newInstance(user.getFirst_name(), user.getMobile(), user.getAddress2(), profileId);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(fragment);
                fragment.setAddressListener(new ProfileAddrFragment.AddressListener() {
                    @Override
                    public void onAddressDataChange(String name, String mobile, String address) {
                        user.setFirst_name(name);
                        user.setMobile(mobile);
                        user.setAddress2(address);
                        ll_addr_info.setVisibility(TextUtils.isEmpty(address) ? View.GONE : View.VISIBLE);
                        tv_addr_null.setVisibility(TextUtils.isEmpty(address) ? View.VISIBLE : View.GONE);
                        System.out.println("name = [" + name + "], mobile = [" + mobile + "], address = [" + address + "]");
                        tv_addr_username.setText(name);
                        tv_addr_mobile.setText(mobile);
                        tv_addr.setText(address);
                    }
                });
                break;
            case R.id.tv_edit:
                AddrManageFragment editFragment = AddrManageFragment.newInstance(user.getFirst_name(), user.getMobile(), user.getAddress2(), profileId, 1);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container, editFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_delete:
                new DeleteAddressTask().execute();
                break;
            case R.id.rl_into:
                ProfileEditFragment introFragment = ProfileEditFragment.newInstance("description", tv_intro.getText().toString().trim(), profileId, "个人简介");
                introFragment.setRefreshDataListener(this);
                getChildFragmentManager().beginTransaction().replace(R.id.fl_child_container, introFragment).addToBackStack(null).commit();
                mChildContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 设置开始加载进度
     */
    private void setStartLoading() {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading", true);
    }

    /**
     * 结束加载过程
     */
    private void setEndLoading() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    /**
     * 初始运行加载结束
     */
    private void initEndLoading() {
        mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> objectRequest) {
                setEndLoading();
            }
        });
    }

    public void toggleMenu() {
        if (menuFragment != null) {
            if (menuFragment.isHidden()) {
                getChildFragmentManager().beginTransaction().show(menuFragment).commit();
            } else {
                getChildFragmentManager().beginTransaction().hide(menuFragment).commit();
            }
        }
    }

    @Override
    public void onMenuSelect(int position, String tag) {
        switch (position) {
            case MENU_CAMERA_SELECTED:
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
                this.startActivityForResult(intent, MENU_CAMERA_SELECTED);
                break;
            case MENU_PHOTO_SELECTED:
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                this.startActivityForResult(intent1, MENU_PHOTO_SELECTED);
                break;
        }
        toggleMenu();
    }

    public void getUserData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_DETAIL_PROFILE_URL +"?id="+id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String msg = jsonObject.optString("message");
                if (status==200){
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    JSONObject userObj = dataObj.optJSONObject("profile");
                    if (userObj == null) return;
                    String mobile = userObj.optString("mobile");
                    mobile = mobile.equals("null") ? "" : mobile;
                    String description = userObj.optString("description");
                    description = description.equals("null") ? "" : description;
                    profileId = userObj.optString("id");
                    boolean verified = userObj.optBoolean("verified");
                    String first_name = userObj.optString("first_name");
                    String address2 = userObj.optString("address2");
                    User user = new User(null, null, null, verified, mobile, description);
                    user.setAddress2(address2);
                    user.setFirst_name(first_name);
                    Message message = new Message();
                    message.what = UPDATE_PROFILE_INFO;
                    message.obj = user;
                    mHandler.sendMessage(message);

                }else {
                    ShowToast(msg);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }
//        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + id + "/profile", new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject jsonObject) {
//                Log.i("getUserData--->", jsonObject.toString());
//                JSONObject userObj = jsonObject.optJSONObject("profile");
//                if (userObj == null) return;
//                String mobile = userObj.optString("mobile");
//                mobile = mobile.equals("null") ? "" : mobile;
//                String description = userObj.optString("description");
//                description = description.equals("null") ? "" : description;
//                profileId = userObj.optString("id");
//                boolean verified = userObj.optBoolean("verified");
//                String first_name = userObj.optString("first_name");
//                String address2 = userObj.optString("address2");
//                User user = new User(null, null, null, verified, mobile, description);
//                user.setAddress2(address2);
//                user.setFirst_name(first_name);
//                Message message = new Message();
//                message.what = UPDATE_PROFILE_INFO;
//                message.obj = user;
//                mHandler.sendMessage(message);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                Log.i("volleyError---------->", volleyError.toString());
//            }
//        });

//    }

    public void getUserInfo() {

        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + "?id="+id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String msg = jsonObject.optString("message");
                if (status==200){
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    JSONObject userObj = dataObj.optJSONObject("user");
                    String email = userObj.optString("email");
                    String nick = userObj.optString("nick_name");
                    String avatar = userObj.optJSONObject("avatar").optString("url");
                    User user = new User(avatar, email, nick, false, null, null);
                    Message message = new Message();
                    message.what = UPDATE_USER_INFO;
                    message.obj = user;
                    mHandler.sendMessage(message);
                }else {
                    ShowToast(msg);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("volleyError---------->", volleyError.toString());
            }
        });
        mRequestQueue.add(request);
    }

    public void popToStack() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.popBackStack();
    }

    @Override
    public void onRefreshData(String type, String msg, String json) {
        Log.i(type, type);
        Log.i(msg, msg);
        Log.i("json", json);
        User user = new User();
        if ("mobile".equalsIgnoreCase(type)) {
            tv_mobile.setText(msg);
        } else if ("description".equalsIgnoreCase(type)) {
            user.setDescription(msg);
            tv_intro.setText(msg);
        } else {
            tv_nick.setText(msg);
            user.setNickName(msg);
        }
        profileListener.onProfileChange(user);
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
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isFromCamera = true;
                    File file = new File(filePath);
                    degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
                    startImageAction(Uri.fromFile(file), 200, 200,
                            2, true);
                }
                break;
            case MENU_PHOTO_SELECTED:// 本地修改头像
                Log.i("requestCode", "本地修改头像");
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isFromCamera = false;
                    uri = data.getData();
                    startImageAction(uri, 200, 200,
                            2, true);
                } else {
                    Toast.makeText(getActivity(), "照片获取失败", Toast.LENGTH_SHORT).show();
                }

                break;
            case 2:// 裁剪头像返回
                // TODO sent to crop
                Log.i("裁剪头像返回----->", "裁剪头像返回");
                if (data == null) {
                    Toast.makeText(getActivity(), "取消选择", Toast.LENGTH_SHORT).show();
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
        Intent intent = null;
        if (isCrop) {
            intent = new Intent("com.android.camera.action.CROP");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        }
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
        Log.i("saveCropAvator--------->", "saveCropAvator");
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            Log.i("life", "avatar - bitmap = " + bitmap);

            String base64str = PhotoUtil.bitmapToBase64(bitmap);
            Log.i("base64str0---------->", base64str);
            UpLoadTask(base64str);
        }
    }

    private void UpLoadTask(String base64) {
        token = mSharedPreferences.getString("token", null);
        id = mSharedPreferences.getInt("userId", -1);
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id + "");
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("avatar", "data:image/png;base64," + base64);
        params.put("user", userParams.toString());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ZhaiDou.USER_UPDATE_AVATAR_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String msg = jsonObject.optString("message");
                if (status == 200) {
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    JSONObject userJson = dataObj.optJSONObject("user");
                    String avatar = userJson.optJSONObject("avatar").optJSONObject("thumb").optString("url");
                    String email = userJson.optString("email");
                    User user = new User();
                    user.setAvatar(avatar);
                    user.setEmail(email);
                    user.setNickName(tv_nick.getText().toString());
                    Message message = new Message();
                    message.what = UPDATE_USER_INFO;
                    message.obj = user;
                    mHandler.sendMessage(message);
                    profileListener.onProfileChange(user);
                } else {
                    ShowToast(msg);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }

    public void setProfileListener(ProfileListener profileListener) {
        this.profileListener = profileListener;
    }

    public interface ProfileListener {
        public void onProfileChange(User user);
    }

    private class DeleteAddressTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String name = user.getFirst_name();
            String mobile = user.getMobile();
            String address = user.getAddress2();

            Map<String, String> map = new HashMap<String, String>();
            map.put("_method", "PUT");
            map.put("profile[first_name]", name);
            map.put("profile[mobile]", mobile);
            map.put("profile[address2]", "");
            map.put("profile[id]", profileId);
            String result = null;
            try {
                result = NativeHttpUtil.post(ZhaiDou.USER_EDIT_PROFILE_URL + profileId, token, map);
            } catch (Exception e) {
                Log.e("Exception-------->", e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject json = new JSONObject(s);
                    JSONObject profile = json.optJSONObject("profile");
                    tv_addr.setText("");
                    if (profile != null) {
                        ll_addr_info.setVisibility(View.GONE);
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_profile));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_profile));
    }
}
