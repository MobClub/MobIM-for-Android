package com.mob.demo.mobim.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMConversation;
import com.mob.imsdk.model.IMGroup;
import com.mob.imsdk.model.IMUser;

public class ChatActivity extends BaseActivity {
	private IMUser user = null;
	private String id = null;
	private boolean isGroup;
	private ChatFragment chatFragment = null;
	private int msgcount = 0;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frame);
		Intent intent = getIntent();
		if (intent != null) {
			id = getIntent().getExtras().getString("id");
			isGroup = getIntent().getExtras().getBoolean("isGroup");
			msgcount = getIntent().getExtras().getInt("unreadcount");
			final boolean frombar = getIntent().getExtras().getBoolean("frombar");
			//intent.putExtra("unreadcount", msgcount);

			if (!isGroup) {
				user = (IMUser) getIntent().getSerializableExtra("user");
				if (user != null) {
					chatFragment = ChatFragment.instace(user, msgcount, frombar);
				} else {
					if (id != null) {
						MobIM.getUserManager().getUserInfo(id, new MobIMCallback<IMUser>() {
							@Override
							public void onSuccess(IMUser user) {
								chatFragment = ChatFragment.instace(user, msgcount, frombar);
								getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, chatFragment).commit();
							}

							@Override
							public void onError(int code, String message) {

							}
						});
					}
				}

			} else {
				IMGroup group = (IMGroup) getIntent().getSerializableExtra("group");
				if (group != null) {
					chatFragment = ChatFragment.instace(group, msgcount, frombar);
				} else {
					if (id != null) {
						MobIM.getGroupManager().getGroupInfo(id, false, new MobIMCallback<IMGroup>() {
							@Override
							public void onSuccess(IMGroup group) {
								chatFragment = ChatFragment.instace(group, msgcount, frombar);
								getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, chatFragment).commit();
							}

							@Override
							public void onError(int code, String message) {

							}
						});
					}
				}

			}

			if (chatFragment != null) {
				getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, chatFragment).commit();
			}

		}
	}

	/**
	 * 进入与用户私聊界面
	 *
	 * @param user 聊天用户
	 */
	public static void gotoUserChatPage(Context context, IMUser user) {
		if (user == null) {
			return;
		}
		//IMUser user = conversation.getOtherInfo();
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("isGroup", false);
		intent.putExtra("id", "" + user.getId());
		intent.putExtra("user", user);
		//intent.putExtra("conversationid",conversation.getId());
		context.startActivity(intent);
	}

	/**
	 * 进入与用户私聊界面
	 *
	 * @param conversation 聊天用户
	 */
	public static void gotoUserChatPage(Context context, IMConversation conversation) {
		if (conversation == null) {
			return;
		}
		IMUser user = conversation.getOtherInfo();
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("isGroup", false);
		//intent.putExtra("id", "" + user.getId());
		int msgcount = conversation.getUnreadMsgCount();
		Utils.showLog("chatact", " =====  unmsgcount >> " + msgcount);
		intent.putExtra("unreadcount", msgcount);

		intent.putExtra("user", user);
		intent.putExtra("conversationid", conversation.getId());
		context.startActivity(intent);
	}

	/**
	 * 进入与群聊界面
	 *
	 * @param context
	 * @param conversation 聊天群
	 */
	public static void gotoGroupChatPage(Context context, IMConversation conversation) {
		IMGroup group = conversation.getGroupInfo();
		if (group == null) {
			return;
		}
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("isGroup", true);
		intent.putExtra("unreadcount", conversation.getUnreadMsgCount());
		intent.putExtra("group", group);
		context.startActivity(intent);
	}

	/**
	 * 进入与群聊界面
	 *
	 * @param context
	 * @param group   聊天群
	 */
	public static void gotoGroupChatPage(Context context, IMGroup group) {
		if (group == null) {
			return;
		}
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("isGroup", true);
		//intent.putExtra("id", "" + group.getId());
		intent.putExtra("group", group);
		context.startActivity(intent);
	}
}
