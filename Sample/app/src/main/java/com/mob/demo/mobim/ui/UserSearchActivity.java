package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMUser;

public class UserSearchActivity extends BaseActivity {
	private InputMethodManager inputMethodManager;
	private ImageView ivAdd;
	private View tvStatus;
	private IMUser searchUser;
	private TextView tvName;
	private View rlResult;
	private TextView tvEmpty;
	private ImageView ivIcon;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_search);

		final EditText etContent = (EditText) findViewById(R.id.etContent);
		rlResult = findViewById(R.id.rlResult);
		tvEmpty = (TextView) findViewById(R.id.tvEmpty);
		rlResult.setVisibility(View.GONE);

		ivIcon = (ImageView) findViewById(R.id.ivIcon);
		tvName = (TextView) findViewById(R.id.tvName);
		tvStatus = findViewById(R.id.tvStatus);
		ivAdd = (ImageView) findViewById(R.id.ivAdd);
		ivAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				UserManager.addFriend(searchUser, new MobIMCallback<Boolean>() {
					public void onSuccess(Boolean aBoolean) {
						if (aBoolean) {
							v.setVisibility(View.GONE);
							tvStatus.setVisibility(View.VISIBLE);
							setResult(Activity.RESULT_OK);
						}
					}

					public void onError(int code, String message) {
						Utils.showErrorToast(code);
					}
				});
			}
		});

		rlResult.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (searchUser != null) {
					UserDetailsActivity.gotoUserDetailsPage(UserSearchActivity.this, searchUser);
				}
			}
		});

		findViewById(R.id.tvTitleRight).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});

		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		etContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					final String userId = etContent.getText().toString();
					if (TextUtils.isEmpty(userId)) {
						return false;
					}
					searchUser = null;
					tvEmpty.setVisibility(View.VISIBLE);
					rlResult.setVisibility(View.GONE);
					ivAdd.setVisibility(View.VISIBLE);
					tvStatus.setVisibility(View.GONE);
					tvEmpty.setText(R.string.tip_load_ing);
					inputMethodManager.hideSoftInputFromWindow(etContent.getWindowToken(), 0);

					IMUser loginUser = UserManager.getUser();
					if (loginUser != null && userId.equals(loginUser.getId())) {
						//如果是自己
						searchUser = loginUser;
						ivAdd.setVisibility(View.GONE);
						tvStatus.setVisibility(View.GONE);
						showIMUser(loginUser);
						return false;
					}

					UserManager.checkIsMyFriend(userId, new MobIMCallback<IMUser>() {
						public void onSuccess(IMUser imUser) {
							if (imUser != null) {
								//如果本来就是自己的好友
								searchUser = imUser;
								ivAdd.setVisibility(View.GONE);
								tvStatus.setVisibility(View.VISIBLE);
								showIMUser(imUser);
							} else {
								//从自己用户系统找用户
								UserManager.findUser(userId, new MobIMCallback<IMUser>() {
									public void onSuccess(final IMUser user) {
										if (user == null) {
											tvEmpty.setVisibility(View.VISIBLE);//没找到时
											rlResult.setVisibility(View.GONE);
											tvEmpty.setText(R.string.tip_user_id_not_found);
										} else {
											searchUser = user;
											showIMUser(searchUser);
										}
									}

									public void onError(int code, String message) {
										tvEmpty.setVisibility(View.VISIBLE);//没找到时
										rlResult.setVisibility(View.GONE);
										tvEmpty.setText(R.string.tip_user_id_not_found);
									}
								});
							}
						}

						public void onError(int code, String message) {

						}
					});

				}
				return false;
			}
		});
	}

	private void showIMUser(IMUser imUser) {
		if (imUser != null) {
			//如果im系统的用户信息变更了，则更新
			if (!TextUtils.isEmpty(imUser.getAvatar())) {
				searchUser.setAvatar(imUser.getAvatar());
			}
			if (!TextUtils.isEmpty(imUser.getNickname())) {
				searchUser.setNickname(imUser.getNickname());
			}
			if (imUser.getExtra() != null) {
				searchUser.setExtra(imUser.getExtra());
			}
		}

		rlResult.setVisibility(View.VISIBLE);
		tvEmpty.setVisibility(View.GONE);
		LoadImageUtils.showAvatar(UserSearchActivity.this, ivIcon, searchUser.getAvatar(), R.drawable.ic_default_user);
		tvName.setText(searchUser.getNickname());
	}

}
