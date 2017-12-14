package com.mob.demo.mobim.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.MobSDK;
import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.MainApplication;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMUser;

public class MainActivity extends BaseActivity implements View.OnClickListener {
	private TextView tvTitle;
	private View tabConversation;
	private View tabContacts;
	private View tabMine;
	private TextView tvMsgCount;

	private Fragment currentFragment;
	private long lastBackTime;

	private View llContent;
	private TextView tvLogin;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(R.string.tab_conversation);

		llContent = findViewById(R.id.llContent);
		tvLogin = (TextView) findViewById(R.id.tvLogin);
		tvMsgCount = (TextView) findViewById(R.id.tvMsgCount);
		tabConversation = findViewById(R.id.tabConversation);
		tabContacts = findViewById(R.id.tabContacts);
		tabMine = findViewById(R.id.tabMine);
		tvLogin.setOnClickListener(this);
		tabConversation.setOnClickListener(this);
		tabMine.setOnClickListener(this);
		tabContacts.setOnClickListener(this);

		if (UserManager.getUser() == null) {
			login();
		} else {
			showTabConversation();
		}

		checkPermissions();
	}

	private void login() {
		tvLogin.setEnabled(false);
		tvLogin.setText(R.string.tip_login_ing);
		llContent.setVisibility(View.GONE);
		tvLogin.setVisibility(View.VISIBLE);
		//如果用户未登录，则登录
		UserManager.login(new MobIMCallback<IMUser>() {
			public void onSuccess(IMUser imUser) {
				if (imUser == null) {
					tvLogin.setText(R.string.tip_login_failed);
					return;
				}
				//登录成功后，设置用户信息（IM会使用此用户进行通讯）
				MobSDK.setUser(imUser.getId(), imUser.getNickname(), imUser.getAvatar(), null);

				showTabConversation();
			}

			public void onError(int code, String message) {
				tvLogin.setEnabled(true);
				tvLogin.setText(R.string.tip_login_failed);
			}
		});
	}

	private void showTabConversation() {
		llContent.setVisibility(View.VISIBLE);
		tvLogin.setVisibility(View.GONE);
		tabConversation.performClick();//执行展示会话界面
		MainApplication application = (MainApplication) getApplication();
		application.regMsgRev();
	}

	protected void onResume() {
		super.onResume();
		//更新未读消息个数
		freshUnreadMessageCount();

		//清除通知栏的所有通知消息
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
	}

	public void freshUnreadMessageCount() {
		int allUnreadMsgCounts = MobIM.getChatManager().getAllUnreadMessageCount(true);
		if (allUnreadMsgCounts > 0) {
			tvMsgCount.setVisibility(View.VISIBLE);
			if (allUnreadMsgCounts > 99) {
				tvMsgCount.setText("99+");
			} else {
				tvMsgCount.setText(String.valueOf(allUnreadMsgCounts));
			}
		} else {
			tvMsgCount.setVisibility(View.GONE);
		}
	}

	public void onClick(View v) {
		int vId = v.getId();
		switch (vId) {
			case R.id.tvLogin: {
				login();
			} break;
			case R.id.tabConversation:
			case R.id.tabContacts:
			case R.id.tabMine: {
				if (v.isSelected()) {
					return;
				}
				v.setSelected(true);
				if (v == tabConversation) {
					tabMine.setSelected(false);
					tabContacts.setSelected(false);
					tvTitle.setText(R.string.tab_conversation);
				} else if (v == tabContacts) {
					tabConversation.setSelected(false);
					tabMine.setSelected(false);
					tvTitle.setText(R.string.tab_contacts);
				} else if (v == tabMine) {
					tabConversation.setSelected(false);
					tabContacts.setSelected(false);
					tvTitle.setText(R.string.tab_mine);
				}
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				Fragment fragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(vId));
				if (fragment == null) {
					if (v == tabConversation) {
						fragment = new FragmentConversation();
					} else if (v == tabMine) {
						fragment = new FragmentMine();
					} else if (v == tabContacts) {
						fragment = new FragmentContacts();
					}
					transaction.add(R.id.flContainer, fragment, String.valueOf(vId));
				}
				if (fragment != currentFragment) {
					if (currentFragment != null) {
						transaction.hide(currentFragment);
					}
					transaction.show(fragment);
					transaction.commit();
					currentFragment = fragment;
				}
			} break;

		}
	}

	public void onBackPressed() {
		long curTime = System.currentTimeMillis();
		if (curTime - lastBackTime < 2000) {
			super.onBackPressed();
		} else {
			lastBackTime = curTime;
			Toast.makeText(getApplicationContext(), R.string.toast_exit_app, Toast.LENGTH_SHORT).show();
		}
	}
}
