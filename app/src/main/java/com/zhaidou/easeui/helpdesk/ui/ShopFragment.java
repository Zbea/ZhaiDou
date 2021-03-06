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
package com.zhaidou.easeui.helpdesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.easeui.helpdesk.Constant;
import com.zhaidou.easeui.helpdesk.widget.PopupListWindow;

public class ShopFragment extends Fragment implements OnClickListener {

	private PopupListWindow mPopupListWindow;
	private TextView tvCustomer;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.em_shop_fragment, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().findViewById(R.id.ib_shop_imageone).setOnClickListener(this);
		getActivity().findViewById(R.id.ib_shop_imagetwo).setOnClickListener(this);
		getActivity().findViewById(R.id.ib_shop_imagethree).setOnClickListener(this);
		getActivity().findViewById(R.id.ib_shop_imagefour).setOnClickListener(this);
		tvCustomer = (TextView) getView().findViewById(R.id.textview_customer);
		tvCustomer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopupListWindow == null) {
					mPopupListWindow = new PopupListWindow(getActivity());
				}
				mPopupListWindow.showAsDropDown(tvCustomer);
			}
		});
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(getActivity(), ShopDetailsActivity.class);
		switch (v.getId()) {
		case R.id.ib_shop_imageone:
			intent.putExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY, Constant.INTENT_CODE_IMG_SELECTED_1);
			break;
		case R.id.ib_shop_imagetwo:
			intent.putExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY, Constant.INTENT_CODE_IMG_SELECTED_2);
			break;
		case R.id.ib_shop_imagethree:
			intent.putExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY, Constant.INTENT_CODE_IMG_SELECTED_3);
			break;
		case R.id.ib_shop_imagefour:
			intent.putExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY, Constant.INTENT_CODE_IMG_SELECTED_4);
			break;
		}
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mPopupListWindow != null && mPopupListWindow.isShowing()) {
			mPopupListWindow.dismiss();
		}
	}

}
