package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.MobSDK;
import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMUser;

public class FragmentUpdateUserName extends BaseFragment implements View.OnClickListener {
	private InputMethodManager inputMethodManager;
	private EditText etContent;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_update_user_name, container, false);
	}

	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

		TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		etContent = (EditText) view.findViewById(R.id.etContent);
		view.findViewById(R.id.tvTitleLeft).setOnClickListener(this);
		view.findViewById(R.id.tvTitleRight).setOnClickListener(this);

		tvTitle.setText(R.string.title_update_user_name);
		IMUser imUser = UserManager.getUser();
		if (imUser == null) {
			etContent.setHint(R.string.hint_input_user_name);
		} else {
			etContent.setText(imUser.getNickname());
		}

		etContent.setSelection(etContent.getText().length());
		new Handler().postDelayed(new Runnable() {
			public void run() {
				inputMethodManager.showSoftInput(etContent, 0);
			}
		}, 200);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.tvTitleRight) {
			String temp = etContent.getText().toString().trim();
			if (TextUtils.isEmpty(temp)) {
				Toast.makeText(getContext().getApplicationContext(), R.string.hint_input_user_name, Toast.LENGTH_SHORT).show();
				return;
			}
			final IMUser user = UserManager.getUser();
			if (user == null) {
				getActivity().onBackPressed();
				return;
			}
			final String userName = temp.length() > 10 ? temp.substring(0, 10) : temp;//用户名限制10个字长
			inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
			String oldUserName = user.getNickname();
			if (!userName.equals(oldUserName)) {
				//更新用户名
				UserManager.updateUserInfo(user.getId(), userName, null, new MobIMCallback<Boolean>() {
					public void onSuccess(Boolean aBoolean) {
						MobSDK.setUser(user.getId(), userName, user.getAvatar(), null);//更新IM用户信息

						Intent data = new Intent();
						data.putExtra("name", userName);
						getActivity().setResult(Activity.RESULT_OK, data);
						getActivity().onBackPressed();
					}

					public void onError(int code, String message) {
						getActivity().onBackPressed();
					}
				});
			} else {
				getActivity().onBackPressed();
			}
		} else if (v.getId() == R.id.tvTitleLeft) {
			getActivity().onBackPressed();
		}
	}
}
