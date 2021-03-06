package com.zhaidou.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.EaseManage;

public class ConversationListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        TextView mTitle= (TextView) findViewById(R.id.title_tv);
        mTitle.setText("我的消息");
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EaseManage.getInstance().refreshData();
    }
}
