/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhaidou.easeui.helpdesk;

import android.app.Application;
import android.content.Context;

import com.easemob.chat.EMChat;
import com.zhaidou.easeui.helpdesk.utils.HelpDeskPreferenceUtils;


public class EaseApplication extends Application {

	public static Context applicationContext;
	private static EaseApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";

	@Override
	public void onCreate() {
		super.onCreate();
		applicationContext = this;
		instance = this;

		//代码中设置环信IM的Appkey
		String appkey = HelpDeskPreferenceUtils.getInstance(this).getSettingCustomerAppkey();
        EMChat.getInstance().setAppkey(appkey);
		// init demo helper
		EaseHelper.getInstance().init(applicationContext);
	}

	public static EaseApplication getInstance() {
		return instance;
	}

}
