package com.zhaidou.activities;

import java.util.Random;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaidou.R;
import com.zhaidou.wxapi.MD5;
import com.zhaidou.wxapi.WxPayUtile;

public class PayActivity extends Activity {

	TextView show;
	
	public static Handler handler = new Handler(new Callback() {
		
//		msg.what== 0 ：表示支付成功
//		msg.what== -1 ：表示支付失败
//		msg.what== -2 ：表示取消支付
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch (msg.what) {
			case 800://商户订单号重复或生成错误
                Log.i("----->","商户订单号重复或生成错误");
				break;
			case 0://支付成功
                Log.i("----->","支付成功");
				break;
			case -1://支付失败
                Log.i("----->","支付失败");
				break;
			case -2://取消支付
                Log.i("----->","取消支付");
				break;
			default:
				break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay);
		show = (TextView) findViewById(R.id.editText_prepay_id);
		// 生成prepay_id
		Button payBtn = (Button) findViewById(R.id.unifiedorder_btn);
		payBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WxPayUtile.getInstance(PayActivity.this, "1",
                        "http://121.40.35.3/test", "测试商品",
                        genOutTradNo()).doPay();
			}
		});

	}
	
	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}
}
