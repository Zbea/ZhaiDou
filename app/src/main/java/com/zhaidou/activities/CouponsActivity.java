package com.zhaidou.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.CouponsFragment;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.SharedPreferencesUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-04-18
 * Time: 11:01
 * Description:优惠卷
 * FIXME
 */
public class CouponsActivity extends BaseActivity implements View.OnClickListener {

    private String mTextViewArray[] = {"未使用", "已使用", "已到期"};
    private String mStatusArrAy[]={"N","U","O"};
    private LayoutInflater mLayoutInflater;
    private DialogUtils mDialogUtils;
    private CouponsFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupons);
        mDialogUtils=new DialogUtils(this);
        findViewById(R.id.rl_back).setOnClickListener(this);
        findViewById(R.id.exchange).setOnClickListener(this);
        final RadioGroup radioGroup= (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.check(1);
        showFragment(1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroup.check(checkedId);
                showFragment(checkedId);
            }
        });
    }

    private void showFragment(int index){
        System.out.println("index = " + index);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        mCurrentFragment = CouponsFragment.newInstance(mStatusArrAy[index - 1], "");
        supportFragmentManager.beginTransaction().replace(R.id.container, mCurrentFragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_back:
                finish();
                break;
            case R.id.exchange:
                mDialogUtils.showCouponDialog(new DialogUtils.PositiveListener2() {
                    @Override
                    public void onPositive(Object o) {
                        mDialogUtils.showLoadingDialog();
                        Map<String,String> map=new HashMap<String, String>();
                        map.put("userId", SharedPreferencesUtil.getData(CouponsActivity.this,"userId",-1)+"");
                        map.put("couponCode",o.toString().trim());
                        map.put("nickName",SharedPreferencesUtil.getData(CouponsActivity.this,"nickName","").toString());
                        ZhaiDouRequest request=new ZhaiDouRequest(CouponsActivity.this, Request.Method.POST,"http://tportal-web.zhaidou.com/user/activateCoupons.action",map,new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                mDialogUtils.dismiss();
                                JSONObject data = jsonObject.optJSONObject("data");
                                int code = data.optInt("code");
                                if (code==-1){
                                    String msg = data.optString("msg");
                                    Toast.makeText(CouponsActivity.this,msg,Toast.LENGTH_SHORT).show();
                                    return;
                                }else if (code==-1){
                                    String status = mCurrentFragment.getArguments().getString("status");
                                    System.out.println("status = " + status);
                                    if ("N".equalsIgnoreCase(status)) {
                                        mCurrentFragment.getListView().setRefreshing(true);
                                        mCurrentFragment.onPullDownToRefresh(mCurrentFragment.getListView());
                                    }
                                }
                            }
                        },new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                mDialogUtils.dismiss();
                            }
                        });
                        ((ZDApplication)getApplicationContext()).mRequestQueue.add(request);
                    }
                },null);
                break;
        }
    }
}
