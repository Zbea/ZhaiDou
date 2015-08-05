package com.zhaidou.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaidou.R;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.fragments.LoginFragment1;
import com.zhaidou.utils.NetStateUtils;
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
    public static NetStateUtils netStateUtils;
    public static boolean isNetState;

    public boolean isLogin;
    public int userId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        mInflater = LayoutInflater.from(getActivity());
        mEmptyView =mInflater.inflate(R.layout.list_empty_view,null);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth=dm.widthPixels;
        screenHeight=dm.heightPixels;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        currentFragment=this;
        view.setOnTouchListener(this);
//        initRegisterBroadcast();
        mBackView=view.findViewById(R.id.ll_back);
        inputMethodManager=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mBackView!=null)
            mBackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("WebViewFragment--233-->","R.id.rl_back:");
//                    Log.i("currentFragment.getParentFragment()",currentFragment.getParentFragment().toString());
                    if (inputMethodManager.isActive())
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getWindow().peekDecorView().getApplicationWindowToken(),0);
                    if (currentFragment.getParentFragment()!=null){
                        currentFragment.getParentFragment().getChildFragmentManager().popBackStack();
                        if (currentFragment.getParentFragment() instanceof LoginFragment1)
                            ((LoginFragment1)currentFragment.getParentFragment()).toggleChildView();
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

    private void initRegisterBroadcast()
    {
        netStateUtils=new NetStateUtils();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(netStateUtils,intentFilter);
    }

    public static Boolean getNetState()
    {
        isNetState=netStateUtils.isNetState;
        return isNetState;
    }

    public int getScreenWidth()
    {
        return screenWidth;
    }

    Toast mToast;

    public void ShowToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
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
        ShowLog("------------------------->"+paramInt);
        ShowLog("------------------------->"+getView().toString());
        return getView().findViewById(paramInt);
    }


    /**
     * 只有title initTopBarLayoutByTitle
     * @Title: initTopBarLayoutByTitle
     * @throws
     */
//    public void initTopBarForOnlyTitle(String titleName) {
//        mHeaderLayout = (HeaderLayout)findViewById(R.id.common_actionbar);
//        mHeaderLayout.init(HeaderLayout.HeaderStyle.DEFAULT_TITLE);
//        mHeaderLayout.setDefaultTitle(titleName);
//    }

    /**
     * 初始化标题栏-带左右按钮
     *
     * @return void
     * @throws
     */
//    public void initTopBarForBoth(String titleName,int leftDrawableId,int rightDrawableId,
//                                  HeaderLayout.onLeftImageButtonClickListener leftListener,
//                                  HeaderLayout.onRightImageButtonClickListener listener) {
//        ShowLog("mHeaderLayout");
//        mHeaderLayout = (HeaderLayout)findViewById(R.id.common_actionbar);
//        ShowLog("mHeaderLayout");
//        mHeaderLayout.init(HeaderLayout.HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
//        mHeaderLayout.setTitleAndLeftImageButton(titleName,
//                leftDrawableId,
//                leftListener);
//        mHeaderLayout.setTitleAndRightImageButton(titleName, rightDrawableId,
//                listener);
//    }

    /**
     * 只有左边按钮和Title initTopBarLayout
     *
     * @throws
     */
//    public void initTopBarForLeft(String titleName) {
//        mHeaderLayout = (HeaderLayout)findViewById(R.id.common_actionbar);
//        mHeaderLayout.init(HeaderLayout.HeaderStyle.TITLE_LIFT_IMAGEBUTTON);
//        mHeaderLayout.setTitleAndLeftImageButton(titleName,
//                R.drawable.base_action_bar_back_bg_selector,
//                new OnLeftButtonClickListener());
//    }

    /** 右边+title
     * initTopBarForRight
     * @return void
     * @throws
     */
//    public void initTopBarForRight(String titleName,int rightDrawableId,
//                                   HeaderLayout.onRightImageButtonClickListener listener) {
//        mHeaderLayout = (HeaderLayout)findViewById(R.id.common_actionbar);
//        mHeaderLayout.init(HeaderLayout.HeaderStyle.TITLE_RIGHT_IMAGEBUTTON);
//        mHeaderLayout.setTitleAndRightImageButton(titleName, rightDrawableId,
//                listener);
//    }
//    public void initTopBarForRight(String titleName, int rightDrawableId,String text,
//                                   HeaderLayout.onRightImageButtonClickListener listener) {
//        mHeaderLayout = (HeaderLayout)findViewById(R.id.common_actionbar);
//        mHeaderLayout.init(HeaderLayout.HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
//        mHeaderLayout.setTitleAndRightButton(titleName, rightDrawableId,text,
//                listener);
//    }
    // 左边按钮的点击事件
    public class OnLeftButtonClickListener implements
            HeaderLayout.onLeftImageButtonClickListener {

        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
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
//        getActivity().unregisterReceiver(netStateUtils);
        super.onDestroy();
    }
}
