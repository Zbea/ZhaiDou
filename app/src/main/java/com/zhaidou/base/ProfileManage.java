package com.zhaidou.base;/**
 * Created by wangclark on 16/1/5.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-01-05
 * Time: 10:50
 * Description:资料信息管理类
 * FIXME
 */
public class ProfileManage {
    private static ProfileManage profileManage=null;
    private OnProfileChange onProfileChange;
    private List<OnProfileChange> onProfileChanges=new ArrayList<OnProfileChange>();
    public static ProfileManage getInstance() {
        if (profileManage==null){
            profileManage=new ProfileManage();
        }
        return profileManage;
    }

    public enum TAG {
        HEADER, NICK,DESC,MOBILE,OTHER
    }

    public void register(OnProfileChange onProfileChange){
        this.onProfileChange=onProfileChange;
        onProfileChanges.add(onProfileChange);
    }

    public void notify(TAG tag,String message){
        for (OnProfileChange onProfileChange:onProfileChanges) {
            onProfileChange.onProfileChange(tag,message);
        }
    }

    public interface OnProfileChange{
        public void onProfileChange(TAG tag,String message);
    }
}
