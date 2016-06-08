package com.zhaidou.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.fragments.CommentListFragment1;
import com.zhaidou.fragments.ReplayFragment;

public class CommentActivity extends FragmentActivity {

    private int index;
    private String[] commentType={"A","C"};


    private Fragment mCommentListFragment;
    private Fragment mReplyFragment;
    private Fragment[] fragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        TextView mTitle= (TextView) findViewById(R.id.title_tv);
        mTitle.setText("我的评论");
        final RadioGroup radioGroup= (RadioGroup)findViewById(R.id.radioGroup);


        radioGroup.check(R.id.received);
        mCommentListFragment=CommentListFragment1.newInstance(index,commentType[1]);
        mReplyFragment= ReplayFragment.newInstance(index+"",commentType[0]);
        fragments= new Fragment[]{mReplyFragment, mCommentListFragment};
        getSupportFragmentManager().beginTransaction().add(R.id.container,mCommentListFragment)
                .add(R.id.container,mReplyFragment).hide(mCommentListFragment).hide(mReplyFragment).commit();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroup.check(checkedId);
                index = checkedId== R.id.received?0:1;
                showFragment(fragments[index]);
            }
        });
        showFragment(fragments[index]);
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void showFragment(Fragment fragment){
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.beginTransaction().show(fragment).hide(fragments[index==0?1:0]).commit();
    }

}
