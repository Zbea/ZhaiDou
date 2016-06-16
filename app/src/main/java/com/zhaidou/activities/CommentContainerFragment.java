package com.zhaidou.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.fragments.CommentListFragment1;
import com.zhaidou.fragments.ReplayFragment;

public class CommentContainerFragment extends BaseFragment {

    private int index;
    private String[] commentType = {"A", "C"};


    private Fragment mCommentListFragment;
    private Fragment mReplyFragment;
    private Fragment[] fragments;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.activity_comment, container, false);
            TextView mTitle = (TextView) rootView.findViewById(R.id.title_tv);
            mTitle.setText("我的评论");
            final RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);


            radioGroup.check(R.id.received);
            mCommentListFragment = CommentListFragment1.newInstance(index, commentType[1]);
            mReplyFragment = ReplayFragment.newInstance(index + "", commentType[0]);
            fragments = new Fragment[]{mReplyFragment, mCommentListFragment};
            getChildFragmentManager().beginTransaction().add(R.id.container, mCommentListFragment)
                    .add(R.id.container, mReplyFragment).hide(mCommentListFragment).hide(mReplyFragment).commit();
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    radioGroup.check(checkedId);
                    index = checkedId == R.id.received ? 0 : 1;
                    showFragment(fragments[index]);
                }
            });
            showFragment(fragments[index]);
        }

        return rootView;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getChildFragmentManager();
        supportFragmentManager.beginTransaction().show(fragment).hide(fragments[index == 0 ? 1 : 0]).commit();
    }

}
