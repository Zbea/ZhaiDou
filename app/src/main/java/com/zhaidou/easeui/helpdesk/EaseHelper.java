package com.zhaidou.easeui.helpdesk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.domain.EaseEmojiconGroupEntity;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.model.EaseNotifier;
import com.easemob.easeui.model.EaseNotifier.EaseNotificationInfoProvider;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.utils.EaseUserUtils;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.AccountManage;
import com.zhaidou.base.CountManager;
import com.zhaidou.base.EaseManage;
import com.zhaidou.easeui.helpdesk.domain.EmojiconExampleGroupData;
import com.zhaidou.easeui.helpdesk.ui.ChatActivity;
import com.zhaidou.easeui.helpdesk.utils.PreferenceManager;
import com.zhaidou.utils.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class EaseHelper {

    protected static final String TAG = EaseHelper.class.getSimpleName();
    
	private EaseUI easeUI;
	
    /**
     * EMEventListener
     */
    protected EMEventListener eventListener = null;

	private Map<String, EaseUser> contactList;

	private static EaseHelper instance = null;
	
	private EaseModel demoModel = null;
	
    private boolean alreadyNotified = false;
	
	public boolean isVoiceCalling;
    public boolean isVideoCalling;

	private String username;

    private Context appContext;

    private EMConnectionListener connectionListener;

	private EaseHelper() {
	}

	public synchronized static EaseHelper getInstance() {
		if (instance == null) {
			instance = new EaseHelper();
		}
		return instance;
	}

	/**
	 * init helper
	 * 
	 * @param context
	 *            application context
	 */
	public void init(Context context) {
		if (EaseUI.getInstance().init(context)) {
		    appContext = context;
            //?????????????????????app???kill??????????????????????????????????????????SDK??????????????????
            EMChatManager.getInstance().setMipushConfig("2882303761517361678", "5851736125678");
		    //??????????????????????????????????????????????????????false??????????????????????????????
		    EMChat.getInstance().setDebugMode(true);
		    //get easeui instance
		    easeUI = EaseUI.getInstance();
		    //??????easeui???api??????providers
		    setEaseUIProviders();
		    demoModel = new EaseModel(context);
			//?????????PreferenceManager
			PreferenceManager.init(context);
			//??????????????????
			setGlobalListeners();
//			broadcastManager = LocalBroadcastManager.getInstance(appContext);
		}
	}

    protected void setEaseUIProviders() {
        easeUI.setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {
            @Override
            public EaseUser getUser(String username) {
                    EaseUser easeUser = new EaseUser(username);
                if (username.equalsIgnoreCase(EMChatManager.getInstance().getCurrentUser())){
                    easeUser.setAvatar((String)SharedPreferencesUtil.getData(appContext,"avatar",""));
                }else if ("service".equalsIgnoreCase(username)){
                    easeUser.setAvatar(R.drawable.icon_ease_servicer+"");
                }else if ("designer".equalsIgnoreCase(username)){
                    easeUser.setAvatar(R.drawable.icon_ease_designer+"");
                }else if ("comment".equalsIgnoreCase(username)){
                    easeUser.setAvatar(R.drawable.icon_ease_comment +"");
                }
                return easeUser;
            }
        });
        //??????????????????
        //??????????????????????????????????????????????????????,
        easeUI.setEaseUserInfoProvider(new EaseUI.EaseUserInfoProvider() {
            @Override
            public void setNickAndAvatar(Context context, EMMessage message, ImageView userAvatarView, TextView usernickView) {
//                JSONObject jsonAgent = getAgentInfoByMessage(message);
                if (message.direct == EMMessage.Direct.SEND) {
                    EaseUserUtils.setUserAvatar(context, EMChatManager.getInstance().getCurrentUser(), userAvatarView);
                    //??????????????????nick
                    //            UserUtils.setUserNick(EMChatManager.getInstance().getCurrentUser(), usernickView);
                } else {
                    EaseUserUtils.setUserAvatar(context, message.getFrom(), userAvatarView);
//                    if (jsonAgent == null) {
//                        userAvatarView.setImageResource(R.drawable.ease_default_avatar);
//                        usernickView.setText(message.getFrom());
//                    } else {
//                        String strNick = null;
//                        String strUrl = null;
//                        try {
//                            strNick = jsonAgent.getString("userNickname");
//                            strUrl = jsonAgent.getString("avatar");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        //??????????????????
//                        if (!TextUtils.isEmpty(strNick)) {
//                            usernickView.setText(strNick);
//                        } else {
//                            usernickView.setText(message.getFrom());
//                        }
//                        //??????????????????
//                        if (!TextUtils.isEmpty(strUrl)) {
//                            if (!strUrl.startsWith("http")) {
//                                strUrl = "http:" + strUrl;
//                            }
//                            //?????????string??????
//                            Glide.with(context).load(strUrl).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ease_default_avatar).into(userAvatarView);
//                        } else {
//                            Glide.with(context).load(R.drawable.ease_default_avatar).into(userAvatarView);
//                        }
//                    }
                }
            }
        });

        //?????????????????????easeui?????????
        easeUI.getNotifier().setNotificationInfoProvider(new EaseNotificationInfoProvider() {

            @Override
            public String getTitle(EMMessage message) {
                //????????????,??????????????????
                return null;
            }

            @Override
            public int getSmallIcon(EMMessage message) {
                //?????????????????????????????????
                return 0;
            }

            @Override
            public String getDisplayedText(EMMessage message) {
                System.out.println("EaseHelper.getDisplayedText");
                // ?????????????????????????????????????????????message????????????????????????
                String ticker = EaseCommonUtils.getMessageDigest(message, appContext);
                if (message.getType() == Type.TXT) {
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[??????]");
                }
                String from = "?????????";
                if (message.getFrom().contentEquals("service")) {
                    from = message.getFrom().replaceFirst("service", "??????");
                } else if (message.getFrom().contentEquals("designer")) {
                    from = message.getFrom().replaceFirst("designer", "?????????");
                }
                return from + ": " + ticker;
            }

            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                System.out.println("message = [" + message + "], fromUsersNum = [" + fromUsersNum + "], messageNum = [" + messageNum + "]");
//                return null;
//                System.out.println("message = " + message);
//                TextMessageBody txtBody = (TextMessageBody) message.getBody();
//                Spannable span = EaseSmileUtils.getSmiledText(appContext, txtBody.getMessage());
//                // ????????????
//                contentView.setText(span, TextView.BufferType.SPANNABLE);
                System.out.println("EMChatManager.getInstance().getUnreadMsgsCount() = " + EMChatManager.getInstance().getUnreadMsgsCount());
                return EMChatManager.getInstance().getUnreadMsgsCount() + "???????????????";
            }

            @Override
            public Intent getLaunchIntent(EMMessage message) {
                //?????????????????????????????????
                Intent intent = new Intent(appContext, ChatActivity.class);
                ChatType chatType = message.getChatType();
                if (chatType == ChatType.Chat) { // ????????????
                    intent.putExtra(EaseConstant.EXTRA_USER_ID, message.getFrom());
                    intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_SINGLE);
                    intent.putExtra(EaseConstant.EXTRA_SHOW_USERNICK, true);
                    intent.putExtra("user",SharedPreferencesUtil.getUser(appContext));
                }
                return intent;
            }
        });

        //????????????provider
        easeUI.setEmojiconInfoProvider(new EaseUI.EaseEmojiconInfoProvider() {
            @Override
            public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
                EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                for (EaseEmojicon emojicon : data.getEmojiconList()) {
                    if (emojicon.getIdentityCode().equals(emojiconIdentityCode)) {
                        return emojicon;
                    }
                }
                return null;
            }

            @Override
            public Map<String, Object> getTextEmojiconMapping() {
                //??????????????????emoji???????????????(resource id??????????????????)?????????map
                return null;
            }
        });


    }

    /**
     * ????????????????????????
     */
    protected void setGlobalListeners(){
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                if (error == EMError.USER_REMOVED) {
                    onCurrentAccountRemoved();
                }else if (error == EMError.CONNECTION_CONFLICT) {
                    onConnectionConflict();
                }
            }

            @Override
            public void onConnected() {
                // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
            	EaseHelper.getInstance().notifyForRecevingEvents();
            }
        };
        //??????????????????
        EMChatManager.getInstance().addConnectionListener(connectionListener);       
        //????????????????????????
        registerEventListener();
    }
    
    /**
     * ???????????????????????????
     */
    protected void onConnectionConflict(){
        EaseHelper.getInstance().logout(true,null);
        AccountManage.getInstance().setConflict(true);
    }
    
    /**
     * ???????????????
     */
    protected void onCurrentAccountRemoved(){
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }
	
	 /**
     * ??????????????????
     * ??????????????????UI???????????????????????????????????????????????????UI???????????????????????????????????????????????????
     * activityList.size() <= 0 ??????????????????????????????????????????????????????????????????Activity Stack
     */
    protected void registerEventListener() {
        eventListener = new EMEventListener() {
            private BroadcastReceiver broadCastReceiver = null;
            
            @Override
            public void onEvent(EMNotifierEvent event) {
                EMMessage message = null;
                if(event.getData() instanceof EMMessage){
                    message = (EMMessage)event.getData();
                    EMLog.d(TAG, "receive the event : " + event.getEvent() + ",id : " + message.getMsgId());
                }
                EaseManage.getInstance().refreshData();
                CountManager.getInstance().notifyCommentChange();
                
                switch (event.getEvent()) {
                case EventNewMessage:
                    //?????????????????????????????????UI,????????????????????????
                    System.out.println("!easeUI.hasForegroundActivies() = " + !easeUI.hasForegroundActivies());
                    if(!easeUI.hasForegroundActivies()){
                        getNotifier().onNewMsg(message);
                    }
//                    getNotifier().onNewMsg(message);
                    break;
                case EventOfflineMessage:
                    if(!easeUI.hasForegroundActivies()){
                        EMLog.d(TAG, "received offline messages");
                        List<EMMessage> messages = (List<EMMessage>) event.getData();
                        getNotifier().onNewMesg(messages);
                    }
                    break;
                // below is just giving a example to show a cmd toast, the app should not follow this
                // so be careful of this
                case EventNewCMDMessage:
                { 
                    
                    EMLog.d(TAG, "??????????????????");
                    //????????????body
                    CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action;//???????????????action
                    
                    //?????????????????? ????????????
                    //message.getStringAttribute("");
                    EMLog.d(TAG, String.format("???????????????action:%s,message:%s", action,message.toString()));
                    final String str = appContext.getString(R.string.receive_the_passthrough);
                    
                    final String CMD_TOAST_BROADCAST = "easemob.demo.cmd.toast";
                    IntentFilter cmdFilter = new IntentFilter(CMD_TOAST_BROADCAST);
                    
                    if(broadCastReceiver == null){
                        broadCastReceiver = new BroadcastReceiver(){

                            @Override
                            public void onReceive(Context context, Intent intent) {
                                // TODO Auto-generated method stub
                                Toast.makeText(appContext, intent.getStringExtra("cmd_value"), Toast.LENGTH_SHORT).show();
                            }
                        };
                        
                      //?????????????????????
                        appContext.registerReceiver(broadCastReceiver,cmdFilter);
                    }

                    Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
                    broadcastIntent.putExtra("cmd_value", str+action);
                    appContext.sendBroadcast(broadcastIntent, null);
                    
                    break;
                }
                case EventDeliveryAck:
                    message.setDelivered(true);
                    break;
                case EventReadAck:
                    message.setAcked(true);
                    break;
                // add other events in case you are interested in
                default:
                    break;
                }
                
            }
        };
        
        EMChatManager.getInstance().registerEventListener(eventListener);
    }

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	public boolean isLoggedIn() {
		return EMChat.getInstance().isLoggedIn();
	}

	/**
	 * ????????????
	 * 
	 * @param unbindDeviceToken
	 *            ??????????????????token(??????GCM??????)
	 * @param callback
	 *            callback
	 */
	public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
		EMChatManager.getInstance().logout(unbindDeviceToken, new EMCallBack() {

			@Override
			public void onSuccess() {
				if (callback != null) {
					callback.onSuccess();
				}

			}

			@Override
			public void onProgress(int progress, String status) {
				if (callback != null) {
					callback.onProgress(progress, status);
				}
			}

			@Override
			public void onError(int code, String error) {
				if (callback != null) {
					callback.onError(code, error);
				}
			}
		});
	}
	
	/**
	 * ?????????????????????
	 * @return
	 */
	public EaseNotifier getNotifier(){
	    return easeUI.getNotifier();
	}
	
	public EaseModel getModel(){
        return (EaseModel) demoModel;
    }
	
    
    /**
     * ???????????????????????????id
     * @param username
     */
    public void setCurrentUserName(String username){
    	this.username = username;
    	demoModel.setCurrentUserName(username);
    }
    
    /**
     * ?????????????????????????????????
     */
    public void setCurrentPassword(String password){
    	demoModel.setCurrentUserPwd(password);
    }
    
    /**
     * ???????????????????????????id
     */
    public String getCurrentUsernName(){
    	if(username == null){
    		username = demoModel.getCurrentUsernName();
    	}
    	return username;
    }
	 
	public synchronized void notifyForRecevingEvents(){
        if(alreadyNotified){
            return;
        }
        
        // ??????sdk???UI ??????????????????????????????????????????receiver???listener, ????????????broadcast???
        EMChat.getInstance().setAppInited();
        alreadyNotified = true;
    }
	
    public void pushActivity(Activity activity) {
        easeUI.pushActivity(activity);
    }

    public void popActivity(Activity activity) {
        easeUI.popActivity(activity);
    }
    
    public boolean isRobotMenuMessage(EMMessage message){
    	try {
			JSONObject jsonObj = message.getJSONObjectAttribute(Constant.MESSAGE_ATTR_MSGTYPE);
			if (jsonObj.has("choice") && !jsonObj.isNull("choice")) {
                JSONObject jsonChoice = jsonObj.getJSONObject("choice");
                if(jsonChoice.has("items") || jsonChoice.has("list")){
                    return true;
                }
			}
		} catch (Exception e) {
		}
		return false;
    }
    
    public String getRobotMenuMessageDigest(EMMessage message) {
		String title = "";
		try {
			JSONObject jsonObj = message.getJSONObjectAttribute(Constant.MESSAGE_ATTR_MSGTYPE);
			if (jsonObj.has("choice")) {
				JSONObject jsonChoice = jsonObj.getJSONObject("choice");
				title = jsonChoice.getString("title");
			}
		} catch (Exception e) {
		}
		return title;
	}
    
    //it is evaluation message
    public boolean isEvalMessage(EMMessage message){
		try {
			JSONObject jsonObj = message.getJSONObjectAttribute(Constant.WEICHAT_MSG);
			if(jsonObj.has("ctrlType")){
				try {
					String type = jsonObj.getString("ctrlType");
					if(!TextUtils.isEmpty(type)&&(type.equalsIgnoreCase("inviteEnquiry")||type.equalsIgnoreCase("enquiry"))){
						return true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (EaseMobException e) {
		}
		return false;
	}
    
    /**
     * ????????????????????????????????????????????????
     * @param message
     * @return
     */
    public boolean isPictureTxtMessage(EMMessage message){
    	JSONObject jsonObj = null;
    	try {
			jsonObj = message.getJSONObjectAttribute(Constant.MESSAGE_ATTR_MSGTYPE);
		} catch (EaseMobException e) {
		}
    	if(jsonObj == null){
			return false;
		}
		if(jsonObj.has("order") || jsonObj.has("track")){
			return true;
		}
		return false;
    }

    /**
     * ?????????????????????????????????
     * ?????????????????????
     * @param message
     * @return
     */
    public JSONObject getAgentInfoByMessage(EMMessage message) {
        try {
            JSONObject jsonWeichat = message.getJSONObjectAttribute(Constant.WEICHAT_MSG);
            if (jsonWeichat == null) {
                return null;
            }
            if (jsonWeichat.has("agent") && !jsonWeichat.isNull("agent")) {
                return jsonWeichat.getJSONObject("agent");
            }
        } catch (EaseMobException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    public boolean isTransferToKefuMsg(EMMessage message){
        try {
            JSONObject jsonObj = message.getJSONObjectAttribute(Constant.WEICHAT_MSG);
            if(jsonObj.has("ctrlType")){
                try {
                    String type = jsonObj.getString("ctrlType");
                    if(!TextUtils.isEmpty(type)&&type.equalsIgnoreCase("TransferToKfHint")){
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (EaseMobException e) {
        }
        return false;
    }

}
