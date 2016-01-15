package com.zhaidou.base;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-01-14
 * Time: 18:04
 * Description:账户登录推出管理类
 * FIXME
 */
public class AccountManage {
    private static AccountManage mAccountManage = null;
    private List<AccountListener> mAccountListener=new ArrayList<AccountListener>();

    public static AccountManage getInstance() {
        if (mAccountManage==null)
            mAccountManage=new AccountManage();
        return mAccountManage;
    }

    public interface AccountListener{
        public void onLogOut();
    }

    public void register(AccountListener accountListener){
        mAccountListener.add(accountListener);
    }

    public void notifyLogOut(){
        for (AccountListener listener:mAccountListener) {
            listener.onLogOut();
        }
    }
}
