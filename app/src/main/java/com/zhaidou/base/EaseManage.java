package com.zhaidou.base;/**
 * Created by wangclark on 16/3/3.
 */


import com.easemob.chat.EMChatManager;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-03-03
 * Time: 10:59
 * Description:环信消息管理器
 * FIXME
 */
public class EaseManage {
    private static EaseManage mEaseManage= null;
    private onMessageChange onMessageChange;
    private List<onMessageChange> mListeners=new ArrayList<onMessageChange>();

    private EaseManage() {
    }

    public static EaseManage getInstance() {
        System.out.println("EaseManage.getInstance");
        if (mEaseManage == null){
            mEaseManage = new EaseManage();
        }

        return mEaseManage;
    }

    public void setOnMessageChange(onMessageChange onMessageChange) {
        if (this.onMessageChange==null)
            this.onMessageChange = onMessageChange;
        mListeners.add(onMessageChange);
    }

    public void refreshData(){
        System.out.println("EaseManage.refreshData------>"+mListeners.size());
        CountManager.getInstance().notifyCommentChange();
        for (onMessageChange listener:mListeners) {
            listener.onMessage(EMChatManager.getInstance().getUnreadMsgsCount());
        }
    }

    public interface onMessageChange{
        public void onMessage(int unreadMsgCount);
    }
}
