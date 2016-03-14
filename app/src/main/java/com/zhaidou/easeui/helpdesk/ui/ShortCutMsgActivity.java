package com.zhaidou.easeui.helpdesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zhaidou.R;
import com.zhaidou.easeui.helpdesk.Constant;


/**
 * 常用语列表
 */
public class ShortCutMsgActivity extends BaseActivity {

    private ArrayAdapter<String> mAdapter;
    private ListView mListView;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.em_activity_shortcut);
        initView();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        String extra = getIntent().getStringExtra(Constant.EXTRA_USER_ID);
        String[] array = getResources().getStringArray(extra.equalsIgnoreCase("service") ? R.array.serviceWord : R.array.designerWord);

        for (String item : array) {
            mAdapter.add(item);
        }
        mListView.setAdapter(mAdapter);
        findViewById(R.id.ll_back).setOnClickListener(new onBackClickListener());
        mListView.setOnItemClickListener(new ListOnItemClick());
    }


    private void initView() {
        mListView = (ListView) findViewById(R.id.list);
    }


    class ListOnItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String content = parent.getItemAtPosition(position).toString();
            setResult(RESULT_OK, new Intent().putExtra("content", content));
            closeActivity();
        }
    }

    class onBackClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            closeActivity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }

    private void closeActivity() {
        finish();
        overridePendingTransition(0, R.anim.activity_close);
    }


}
