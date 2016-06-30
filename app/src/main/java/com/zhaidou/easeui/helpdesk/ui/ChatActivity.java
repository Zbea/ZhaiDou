package com.zhaidou.easeui.helpdesk.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.ui.EaseChatFragment;
import com.zhaidou.R;
import com.zhaidou.base.EaseManage;
import com.zhaidou.easeui.helpdesk.Constant;
import com.zhaidou.easeui.helpdesk.utils.HelpDeskPreferenceUtils;
import com.zhaidou.model.User;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.SharedPreferencesUtil;


/**
 * 聊天页面，需要fragment的使用{@link EaseChatFragment}
 */
public class ChatActivity extends BaseActivity {

    public static ChatActivity activityInstance;
    private ChatFragment chatFragment;
    String toChatUsername;
    private TextView mTitleView;

    private User mUser;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.em_activity_chat);
        mTitleView = (TextView) findViewById(R.id.title_tv);
        System.out.println("ChatActivity.onCreate--->" + getIntent().getStringExtra("queueName"));
        mTitleView.setText("service".equalsIgnoreCase(getIntent().getStringExtra(Constant.EXTRA_USER_ID)) ? "在线客服" : "在线设计师");
        activityInstance = this;
        // 聊天人或群id
        toChatUsername = HelpDeskPreferenceUtils.getInstance(this).getSettingCustomerAccount();
        mUser = (User) getIntent().getSerializableExtra("user");
        // 可以直接new EaseChatFratFragment使用
        final Intent intent = getIntent();
        intent.putExtra(Constant.EXTRA_USER_ID, getIntent().getStringExtra(Constant.EXTRA_USER_ID));
        intent.putExtra(Constant.EXTRA_SHOW_USERNICK, true);
        intent.putExtra(Constant.MESSAGE_TO_INTENT_EXTRA, "service".equalsIgnoreCase(getIntent().getStringExtra(Constant.EXTRA_USER_ID)) ? Constant.MESSAGE_TO_SERVICE : Constant.MESSAGE_TO_DESIGNER);
        intent.putExtra("userNickname", !TextUtils.isEmpty(mUser.getNickName()) ? mUser.getNickName() : "");
        intent.putExtra("trueName", !TextUtils.isEmpty(mUser.getNickName()) ? mUser.getNickName() : "");
        intent.putExtra("qq", "");
        intent.putExtra("phone", !TextUtils.isEmpty(mUser.getMobile()) ? mUser.getMobile() : "");
        intent.putExtra("companyName", "");
        intent.putExtra("description", !TextUtils.isEmpty(mUser.getDescription()) ? mUser.getDescription() : "");
        intent.putExtra("email", !TextUtils.isEmpty(mUser.getEmail()) ? mUser.getEmail() : "");
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                System.out.println("inputMethodManager.isActive() = " + inputMethodManager.isActive());
                if (inputMethodManager.isActive())
                    inputMethodManager.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);
                finish();
            }
        });
        EaseUI.getInstance().setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {
            @Override
            public EaseUser getUser(String username) {
                System.out.println("EaseHelper.getUser--------->" + username);
                EaseUser easeUser = new EaseUser(username);
                if (username.equalsIgnoreCase(EMChatManager.getInstance().getCurrentUser())) {
                    easeUser.setAvatar((String) SharedPreferencesUtil.getData(ChatActivity.this, "avatar", ""));
                } else if ("service".equalsIgnoreCase(username)) {
                    easeUser.setAvatar(R.drawable.icon_ease_servicer + "");
                } else if ("designer".equalsIgnoreCase(username)) {
                    easeUser.setAvatar(R.drawable.icon_ease_designer + "");
                }else if ("comment".equalsIgnoreCase(username)){
                    easeUser.setAvatar(R.drawable.icon_ease_comment +"");
                }
                return easeUser;
            }
        });
        if (EMChatManager.getInstance().getCurrentUser()==null) {
            User user = SharedPreferencesUtil.getUser(ChatActivity.this);
            EaseUtils.login(user, new EaseUtils.LoginListener() {
                @Override
                public void onSuccess() {
                    setUpFragment(intent);
                }
            });
        }else {
            setUpFragment(intent);
        }
    }

    private void setUpFragment(Intent intent){
        chatFragment = new ChatFragment();
        chatFragment.setArguments(intent.getExtras());// 传入参数
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).addToBackStack("ChatFragment").commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EaseManage.getInstance().refreshData();
        activityInstance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        System.out.println("ChatActivity.onBackPressed");
        chatFragment.onBackPressed();
    }

//    public String getToChatUsername() {
//        return toChatUsername;
//    }

//	public void sendTextMessage(String txtContent){
//		chatFragment.sendTextMessage(txtContent);
//	}

    public void sendRobotMessage(String txtContent, String menuId) {
        chatFragment.sendRobotMessage(txtContent, menuId);
    }


}
