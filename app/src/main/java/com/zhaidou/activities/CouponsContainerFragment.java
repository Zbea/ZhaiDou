package com.zhaidou.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
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
public class CouponsContainerFragment extends BaseFragment implements View.OnClickListener {

    private String mTextViewArray[] = {"未使用", "已使用", "已到期"};
    private String mStatusArrAy[]={"N","U","O"};
    private LayoutInflater mLayoutInflater;
    private DialogUtils mDialogUtils;
    private com.zhaidou.fragments.CouponsFragment mCurrentFragment;
    private int index;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_coupons,null);
        mDialogUtils=new DialogUtils(mContext);
        view.findViewById(R.id.rl_back).setOnClickListener(this);
        view.findViewById(R.id.exchange).setOnClickListener(this);
        final RadioGroup radioGroup= (RadioGroup)view.findViewById(R.id.radioGroup);
        radioGroup.check(R.id.unused);
        showFragment(0);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroup.check(checkedId);
                index = checkedId== R.id.unused?0:checkedId==R.id.used?1:2;
                showFragment(index);
            }
        });
        return view;
    }

    private void showFragment(int index){
        FragmentManager supportFragmentManager = getChildFragmentManager();
        mCurrentFragment = CouponsFragment.newInstance(mStatusArrAy[index],mTextViewArray[index]);
        supportFragmentManager.beginTransaction().replace(R.id.container, mCurrentFragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.exchange:
                mDialogUtils.showCouponDialog(new DialogUtils.PositiveListener2() {
                    @Override
                    public void onPositive(Object o) {
                        mDialogUtils.showLoadingDialog();
                        Map<String,String> map=new HashMap<String, String>();
                        map.put("userId", SharedPreferencesUtil.getData(mContext,"userId",-1)+"");
                        map.put("couponCode",o.toString().trim());
                        map.put("nickName",SharedPreferencesUtil.getData(mContext,"nickName","").toString());
                        ZhaiDouRequest request=new ZhaiDouRequest(mContext, Request.Method.POST, ZhaiDou.GetRedeemCouponUrl,map,new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                mDialogUtils.dismiss();
                                JSONObject data = jsonObject.optJSONObject("data");
                                int code = data.optInt("code");
                                if (code==-1){
                                    String msg = data.optString("msg");
                                    Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
                                    return;
                                }else if (code==0){
                                    String status = mCurrentFragment.getArguments().getString("status");
                                    System.out.println("status = " + status);
                                    if ("N".equalsIgnoreCase(status)) {
                                        mCurrentFragment.getListView().setRefreshing(true);
                                        mCurrentFragment.onPullDownToRefresh(mCurrentFragment.getListView());
                                    }
                                    Toast.makeText(mContext,"兑换成功",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                hideInputMethod();
                                mDialogUtils.dismiss();
                            }
                        });
                        ((ZDApplication)mContext.getApplicationContext()).mRequestQueue.add(request);
                    }
                },null);
                break;
        }
    }
}
