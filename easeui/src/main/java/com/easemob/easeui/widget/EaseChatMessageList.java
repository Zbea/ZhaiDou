package com.easemob.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;

public class EaseChatMessageList extends RelativeLayout{
    
    protected static final String TAG = "EaseChatMessageList";
    protected ListView listView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected Context context;
    protected EMConversation conversation;
    protected int chatType;
    protected String toChatUsername;
    protected EaseMessageAdapter messageAdapter;
    protected boolean showUserNick;
    protected boolean showAvatar;
    protected Drawable myBubbleBg;
    protected Drawable otherBuddleBg;

	public EaseChatMessageList(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseChatMessageList(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	parseStyle(context, attrs);
    	init(context);
    }

    public EaseChatMessageList(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_layout);
        listView = (ListView) findViewById(R.id.list);
    }
    
    /**
     * init widget
     * @param toChatUsername
     * @param chatType
     * @param customChatRowProvider
     */
    public void init(String toChatUsername, int chatType, EaseCustomChatRowProvider customChatRowProvider) {
        this.chatType = chatType;
        this.toChatUsername = toChatUsername;
        
        conversation = EMChatManager.getInstance().getConversation(toChatUsername);
        messageAdapter = new EaseMessageAdapter(context, toChatUsername, chatType, listView);
        messageAdapter.setShowAvatar(showAvatar);
        messageAdapter.setShowUserNick(showUserNick);
        messageAdapter.setMyBubbleBg(myBubbleBg);
        messageAdapter.setOtherBuddleBg(otherBuddleBg);
        messageAdapter.setCustomChatRowProvider(customChatRowProvider);
        // ??????adapter????????????
        listView.setAdapter(messageAdapter);
        
        refreshSelectLast();
    }
    
    protected void parseStyle(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatMessageList);
        showAvatar = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserAvatar, true);
        myBubbleBg = ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        otherBuddleBg = ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        showUserNick = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserNick, false);
        ta.recycle();
    }
    
    
    /**
     * ????????????
     */
    public void refresh(){
        messageAdapter.refresh();
    }
    
    /**
     * ???????????????????????????????????????item
     */
    public void refreshSelectLast(){
        messageAdapter.refreshSelectLast();
    }
    
    /**
     * ????????????,???????????????position
     * @param position
     */
    public void refreshSeekTo(int position){
        messageAdapter.refreshSeekTo(position);;
    }
    
    

    /**
     * ??????listview
     * @return
     */
	public ListView getListView() {
		return listView;
	} 
	
	/**
	 * ??????SwipeRefreshLayout
	 * @return
	 */
	public SwipeRefreshLayout getSwipeRefreshLayout(){
	    return swipeRefreshLayout;
	}
	
	public EMMessage getItem(int position){
	    return messageAdapter.getItem(position);
	}
	
	/**
	 * ??????????????????????????????
	 * @param showUserNick
	 */
	public void setShowUserNick(boolean showUserNick){
	    this.showUserNick = showUserNick;
	}
	
	public boolean isShowUserNick(){
	    return showUserNick;
	}
	
	public interface MessageListItemClickListener{
	    void onResendClick(EMMessage message);
	    /**
	     * ???????????????????????????????????????????????????????????????????????????return true???
	     * ???????????????????????????chatrow???onBubbleClick()???????????????????????????
	     * @param message
	     * @return
	     */
	    boolean onBubbleClick(EMMessage message);
	    void onBubbleLongClick(EMMessage message);
	    void onUserAvatarClick(String username);
	}
	
	/**
	 * ??????list item????????????????????????
	 * @param listener
	 */
	public void setItemClickListener(MessageListItemClickListener listener){
	    messageAdapter.setItemClickListener(listener);
	}
	
	/**
	 * ???????????????chatrow?????????
	 * @param rowProvider
	 */
	public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider){
        messageAdapter.setCustomChatRowProvider(rowProvider);
    }
}
