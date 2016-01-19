package com.zhaidou.base;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.model.Store;
import com.zhaidou.utils.NetStateUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.HeaderLayout;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public abstract class BaseFragment extends Fragment implements View.OnTouchListener,View.OnClickListener{
    /**
     * 公用的Header布局
     */
    public HeaderLayout mHeaderLayout;

    protected View contentView;
    protected View mBackView;

    public LayoutInflater mInflater;

    private Handler handler = new Handler();

    public void runOnWorkThread(Runnable action) {
        new Thread(action).start();
    }

    public void runOnUiThread(Runnable action) {
        handler.post(action);
    }

    protected InputMethodManager inputMethodManager;

    protected Fragment currentFragment;

    protected View mEmptyView;

    protected int screenWidth;
    protected int screenHeight;
    public Context mContext;
    protected boolean isDialogFirstVisible=true;

    public String versionCode;
    public String versionName;

    private onFragmentCloseListener onFragmentCloseListener;
    protected OnReturnSuccess onReturnSuccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        mInflater = LayoutInflater.from(getActivity());
        mEmptyView =mInflater.inflate(R.layout.list_empty_view,null);
        mContext=getActivity();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth=dm.widthPixels;
        screenHeight=dm.heightPixels;
        PackageInfo packageInfo= null;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0);
            versionCode = packageInfo.versionCode+"";
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        currentFragment=this;
        view.setOnTouchListener(this);
        view.setOnClickListener(null);
        mBackView=view.findViewById(R.id.ll_back);
        inputMethodManager=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mBackView!=null)
            mBackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputMethodManager.isActive())
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getWindow().peekDecorView().getApplicationWindowToken(),0);
                    if (currentFragment.getParentFragment()!=null){
                        currentFragment.getParentFragment().getChildFragmentManager().popBackStack();
                        return;
                    }
                    if (getActivity() instanceof ItemDetailActivity){
                        ((ItemDetailActivity)getActivity()).onBackClick(currentFragment);
                    }
                    ((BaseActivity)getActivity()).popToStack(currentFragment);
                }
            });
    }

    public BaseFragment() {

    }

    public int getScreenWidth()
    {
        return screenWidth;
    }

    Toast mToast;

    public void ShowToast(String text) {
        if (mToast == null)
        {
            if(getActivity()!=null)
            {
                mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
            }
        }
        else
        {
            mToast.setText(text);
        }
        if (mToast!=null)
        mToast.show();
    }

    public void ShowToast(int text) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    /** 打Log
     * ShowLog
     * @return void
     * @throws
     */
    public void ShowLog(String msg){
        Log.i("zhaidou",msg);
    }

    public View findViewById(int paramInt) {
        return getView().findViewById(paramInt);
    }

    /**
     * 检查是否登录
     * @return
     */
    public boolean checkLogin()
    {
        String token = (String) SharedPreferencesUtil.getData(mContext, "token", "");
        int userId = (Integer) SharedPreferencesUtil.getData(mContext, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && userId > -1;
        return isLogin;
    }

    /**
     * 动画启动页面 startAnimActivity
     * @throws
     */
    public void startAnimActivity(Intent intent) {
        this.startActivity(intent);
    }

    public void startAnimActivity(Class<?> cla) {
        getActivity().startActivity(new Intent(getActivity(), cla));
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_back:
                Log.i("onClick---->","onClick");
                break;
        }
    }
    protected void hideInputMethod(){
        if (inputMethodManager==null)
            inputMethodManager=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(getActivity().getWindow().peekDecorView().getApplicationWindowToken(),0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 关闭当前页面
     */
    public void colseFragment(Fragment fragment)
    {
        ((MainActivity) getActivity()).popToStack(fragment);
    }

    public void setOnFragmentCloseListener(BaseFragment.onFragmentCloseListener onFragmentCloseListener) {
        this.onFragmentCloseListener = onFragmentCloseListener;
    }

    public interface onFragmentCloseListener{
        public void onClose();
    }

    public void setOnReturnSuccess(OnReturnSuccess onReturnSuccess) {
        this.onReturnSuccess = onReturnSuccess;
    }

    public interface OnReturnSuccess{
        public void onSuccess(Store store);
    }
}
