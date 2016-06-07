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
import com.zhaidou.R;
import com.zhaidou.activities.CommentActivity;
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
                    Toast.makeText(getActivity(), R.string.Cant_chat_with_yourself, 0).show();
                }else if ("comment".equalsIgnoreCase(username)){
                    System.out.println("username = " + username);
                    Intent intent=new Intent(getActivity(), CommentActivity.class);
                    startActivity(intent);
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


//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        getActivity().getMenuInflater().inflate(R.menu.em_delete_message, menu);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        boolean deleteMessage = false;
//        if (item.getItemId() == R.id.delete_message) {
//            deleteMessage = true;
//        } else if (item.getItemId() == R.id.delete_conversation) {
//            deleteMessage = false;
//        }
//    	EMConversation tobeDeleteCons = conversationListView.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
//    	if (tobeDeleteCons == null) {
//    	    return true;
//    	}
//        // 删除此会话
//        EMClient.getInstance().chatManager().deleteConversation(tobeDeleteCons.getUserName(), deleteMessage);
//        InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
//        inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
//        refresh();
//
//        // 更新消息未读数
//        ((MainActivity) getActivity()).updateUnreadLabel();
//        return true;
//    }

}
