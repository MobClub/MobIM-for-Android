package com.mob.demo.mobim.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.DialogYesOrNo;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMConversation;
import com.mob.imsdk.model.IMUser;

public class UserDetailsActivity extends BaseActivity implements View.OnClickListener {
	private IMUser user;

	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_details);
		findViewById(R.id.ivTitleLeft).setOnClickListener(this);
		user = (IMUser) getIntent().getSerializableExtra("user");

		if (user == null) {
			return;
		}

		IMUser loginUser = UserManager.getUser();
		if (loginUser == null) {
			return;
		}

		final View pbLoading = findViewById(R.id.pbLoading);
		final TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		final ImageView ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
		final TextView tvUserName = (TextView) findViewById(R.id.tvUserName);
		TextView tvUserId = (TextView) findViewById(R.id.tvUserId);
		tvUserId.setText(user.getId());
		tvTitle.setText(user.getNickname());
		tvUserName.setText(user.getNickname());
		LoadImageUtils.showAvatar(this, ivAvatar, user.getAvatar(), R.drawable.ic_default_user);

		final Switch ivNoDisturbing = (Switch) findViewById(R.id.swNoDisturbing);
		final Switch ivBlackList = (Switch) findViewById(R.id.swNoBlackList);
		View btnSend = findViewById(R.id.btnSend);

		if (user.getId().equals(loginUser.getId())) {
			//如果是自己，则直接返回
			ivNoDisturbing.setEnabled(false);
			ivBlackList.setEnabled(false);
			btnSend.setVisibility(View.GONE);
			pbLoading.setVisibility(View.GONE);
			return;
		}

		btnSend.setOnClickListener(this);
		pbLoading.setVisibility(View.VISIBLE);
		//从自己用户系统，获取并更新最新的用户信息（防止对方改了信息，而没有及时更新）
		UserManager.findUser(user.getId(), new MobIMCallback<IMUser>() {
			public void onSuccess(IMUser imUser) {
				pbLoading.setVisibility(View.GONE);
				if (imUser == null) {
					return;
				}
				boolean hasNewInfo = false;
				if (!TextUtils.isEmpty(imUser.getNickname())) {
					hasNewInfo = true;
					user.setNickname(imUser.getNickname());
				}
				if (!TextUtils.isEmpty(imUser.getAvatar())) {
					hasNewInfo = true;
					user.setAvatar(imUser.getAvatar());
				}
				//更新用户信息
				tvTitle.setText(user.getNickname());
				tvUserName.setText(user.getNickname());
				LoadImageUtils.showAvatar(UserDetailsActivity.this, ivAvatar, user.getAvatar(), R.drawable.ic_default_user);
				//更新本地存储的好友信息
				if (hasNewInfo) {
					UserManager.updateFriendInfo(imUser);
				}
				setResult(RESULT_OK);
			}

			public void onError(int code, String message) {
				pbLoading.setVisibility(View.GONE);
			}
		});
		//更新MobIM保存的用户信息
		MobIM.getUserManager().getUserInfo(user.getId(), null);

		ivBlackList.setEnabled(false);
		MobIM.getUserManager().checkBlack(user.getId(), new MobIMCallback<Boolean>() {
			public void onSuccess(Boolean success) {
				ivBlackList.setChecked(success);
				ivBlackList.setEnabled(true);
			}

			public void onError(int code, String message) {
				ivBlackList.setEnabled(true);
			}
		});

		ivNoDisturbing.setEnabled(false);
		MobIM.getChatManager().getConversationDisturb(user.getId(), IMConversation.TYPE_USER, new MobIMCallback<Boolean>() {
			public void onSuccess(Boolean success) {
				ivNoDisturbing.setChecked(success);
				ivNoDisturbing.setEnabled(true);
			}

			public void onError(int code, String message) {
				ivNoDisturbing.setEnabled(true);
			}
		});

		ivNoDisturbing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				MobIM.getChatManager().setConversationDisturb(user.getId(), IMConversation.TYPE_USER, isChecked, new MobIMCallback<Boolean>() {
					public void onSuccess(Boolean success) {
						if (!success) {
							ivNoDisturbing.setChecked(!isChecked);
						}
						ivNoDisturbing.setEnabled(true);
					}

					public void onError(int code, String message) {
						ivNoDisturbing.setChecked(!isChecked);
						ivNoDisturbing.setEnabled(true);
					}
				});
				ivNoDisturbing.setEnabled(false);
			}
		});

		ivBlackList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				if (!ivBlackList.isEnabled()) {
					return;
				}
				if (isChecked) {
					new DialogYesOrNo(UserDetailsActivity.this, getString(R.string.tip_confirm_to_add_to_black_list), 0, 0,
							new DialogYesOrNo.OnConfirmClickListener() {
						public void onConfirm() {
							MobIM.getUserManager().addToBlacklist(user.getId(), new MobIMCallback<Void>() {
								public void onSuccess(Void result) {
									ivBlackList.setEnabled(true);
								}

								public void onError(int code, String message) {
									ivBlackList.setChecked(!isChecked);
									ivBlackList.setEnabled(true);
								}
							});
						}
					}, new DialogYesOrNo.OnCancelClickListener() {
						public void onCancel() {
							ivBlackList.setChecked(!isChecked);
							ivBlackList.setEnabled(true);
						}
					}).show();
				} else {
					MobIM.getUserManager().removeFromBlacklist(user.getId(), new MobIMCallback<Void>() {
						public void onSuccess(Void result) {
							ivBlackList.setEnabled(true);
						}

						public void onError(int code, String message) {
							ivBlackList.setChecked(!isChecked);
							ivBlackList.setEnabled(true);
						}
					});
				}
				ivBlackList.setEnabled(false);
			}
		});
	}

	public void onClick(final View v) {
		int vId = v.getId();
		if (vId == R.id.ivTitleLeft) {
			onBackPressed();
			return;
		}
		if (user == null) {
			return;
		}
		if (vId == R.id.btnSend) {
			ChatActivity.gotoUserChatPage(this, user);
			finish();//去掉聊天界面，则直接finish掉，模仿QQ交互
		}
	}

	public static void gotoUserDetailsPage(Context context, IMUser user) {
		Intent intent = new Intent(context, UserDetailsActivity.class);
		intent.putExtra("user", user);
		context.startActivity(intent);
	}

	public static void gotoUserDetailsPageForResult(Fragment fragment, IMUser user, int requestCode) {
		Intent intent = new Intent(fragment.getContext(), UserDetailsActivity.class);
		intent.putExtra("user", user);
		fragment.startActivityForResult(intent, requestCode);
	}
}
