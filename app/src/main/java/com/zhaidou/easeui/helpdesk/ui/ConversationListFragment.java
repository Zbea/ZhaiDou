package com.zhaidou.easeui.helpdesk.ui;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.easeui.ui.EaseConversationListFragment;
import com.easemob.util.NetUtils;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.activities.CommentContainerFragment;
import com.zhaidou.base.EaseManage;
import com.zhaidou.easeui.helpdesk.Constant;
import com.zhaidou.model.User;
import com.zhaidou.utils.SharedPreferencesUtil;


public class ConversationListFragment extends EaseConversationListFragment implements EaseManage.onMessageChange {

    private TextView errorText;

    @Override
    protected void initView() {
        super.initView();
//        View errorView =View.inflate(getActivity(), R.layout.em_chat_neterror_item, null);
//        errorItemContainer.addView(errorView);
//        errorText = (TextView) errorView.findViewById(R.id.tv_connect_errormsg);
        EaseManage.getInstance().setOnMessageChange(this);
//        Api.getUnReadComment();
    }
    
    @Override
    protected void setUpView() {
        super.setUpView();
        // 注册上下文菜单
        registerForContextMenu(conversationListView);
        conversationListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = conversationListView.getItem(position);
                String username = conversation.getUserName();
                System.out.println("ConversationListFragment.onItemClick---------->"+username+"----"+EMChatManager.getInstance().getCurrentUser());
                System.out.println("ConversationListFragment.onItemClick-->"+conversation.toString());
                if (username.equals(EMChatManager.getInstance().getCurrentUser())){
                    Toast.makeText(getActivity(), R.string.Cant_chat_with_yourself, Toast.LENGTH_SHORT).show();
                }else if ("comment".equalsIgnoreCase(username)){
                    System.out.println("username = " + username);
//                    Intent intent=new Intent(getActivity(), CommentContainerFragment.class);
//                    startActivity(intent);
                    CommentContainerFragment commentContainerFragment=new CommentContainerFragment();
                    ((MainActivity)getActivity()).navigationToFragment(commentContainerFragment);
                }else {
                    // 进入聊天页面
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    User user = SharedPreferencesUtil.getUser(getActivity());
                    intent.putExtra("user",user);
                    if(conversation.isGroup()){
                        if(conversation.getType() == EMConversation.EMConversationType.ChatRoom){
                            // it's group chat
                            intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_CHATROOM);
                        }else{
                            intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_GROUP);
                        }
                        
                    }
                    // it's single chat
                    intent.putExtra(Constant.EXTRA_USER_ID, username);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onConnectionDisconnected() {
        super.onConnectionDisconnected();
        if (NetUtils.hasNetwork(getActivity())){
//         errorText.setText(R.string.can_not_connect_chat_server_connection);
        } else {
//          errorText.setText(R.string.the_current_network);
        }
    }

    @Override
    public void onMessage(int unreadMsgCount) {
        System.out.println("ConversationListFragment.onMessage");
        refresh();
    }
}
