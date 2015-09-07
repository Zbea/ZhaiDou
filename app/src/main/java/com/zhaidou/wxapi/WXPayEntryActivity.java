package com.zhaidou.wxapi;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.PayActivity;
import com.zhaidou.fragments.ShopPaymentFailFragment;
import com.zhaidou.fragments.ShopPaymentFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class WXPayEntryActivity extends FragmentActivity implements IWXAPIEventHandler
{

    private static final String TAG = "com.zhaidou.WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.out.println("WXPayEntryActivity.onCreate---------------->" + Thread.currentThread() + "");
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req)
    {
    }

    @Override
    public void onResp(BaseResp resp)
    {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode + "----->" + resp.errStr + "-------------->" + resp.getType());

        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX)
        {
            System.out.println("WXPayEntryActivity.onResp----------------------->" + Thread.currentThread());
//            			MainActivity.handler.sendEmptyMessage(resp.errCode);
//            resp.errCode=0;
            if (resp.errCode == -2)
            {
                Toast.makeText(WXPayEntryActivity.this, "取消支付", Toast.LENGTH_SHORT).show();
            } else
            {
                Intent intent = new Intent(ZhaiDou.BROADCAST_WXAPI_FILTER);
                intent.putExtra("code", resp.errCode);
                sendBroadcast(intent);
            }
            finish();
            overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_out);
        }
    }
}