package com.mob.demo.mobim.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.DialogYesOrNo;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.MobIMMessageReceiver;
import com.mob.imsdk.MobIMReceiver;
import com.mob.imsdk.model.IMConversation;
import com.mob.imsdk.model.IMGroup;
import com.mob.imsdk.model.IMMessage;
import com.mob.imsdk.model.IMReminder;
import com.mob.imsdk.model.IMUser;

import java.text.SimpleDateFormat;
import java.util.List;

public class FragmentConversation extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	private SwipeRefreshLayout srLayout;
	private ListView listView;
	private QuickAdapter adapter;
	private MobIMMessageReceiver messageReceiver;
	private MobIMReceiver generalReceiver;
	private MobIMCallback<List<IMConversation>> conversationCallback;

	protected boolean useLoadingView() {
		//使用loadingView
		return true;
	}

	protected void reload() {
		refreshData();
	}

	public View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_conversation, container, false);
	}

	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		srLayout = (SwipeRefreshLayout) view.findViewById(R.id.srLayout);
		listView = (ListView) view.findViewById(R.id.listView);

		srLayout.setColorSchemeColors(Color.parseColor("#00C59C"));
		srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {
				refreshData();
			}
		});
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);

		srLayout.setEnabled(false);

		//先加载会话
		messageReceiver = new MobIMMessageReceiver() {
			public void onMessageReceived(List<IMMessage> messageList) {
				//接收到消息，则刷新界面
				refreshData();

				//更新未读消息总数
				((MainActivity) getActivity()).freshUnreadMessageCount();
			}
		};

		generalReceiver = new MobIMReceiver() {
			public void onConnected() {
				setIMConnectStatus(0);
				//连接im成功后，刷新会话列表
				refreshData();
			}

			public void onConnecting() {
				setIMConnectStatus(1);
			}

			public void onDisconnected(int error) {
				setIMConnectStatus(error);
			}
		};
		MobIM.addMessageReceiver(messageReceiver);
		MobIM.addGeneralReceiver(generalReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshData();
	}

	public void onDestroy() {
		super.onDestroy();
		MobIM.removeMessageReceiver(messageReceiver);
		MobIM.removeGeneralReceiver(generalReceiver);
	}

	private void refreshData() {
		if (UserManager.getUser() == null) {
			showLoadingFailed();
			return;
		}

		//加载本地会话
		MobIM.getChatManager().getAllLocalConversations(initConversationCallback());
	}

	private MobIMCallback<List<IMConversation>> initConversationCallback() {
		if (conversationCallback == null) {
			conversationCallback = new MobIMCallback<List<IMConversation>>() {
				public void onSuccess(List<IMConversation> list) {
					showContentView();
					setAdapter(list);
					srLayout.setEnabled(true);
					if (srLayout.isRefreshing()) {
						srLayout.setRefreshing(false);
					}
				}

				public void onError(int code, String message) {
					showLoadingFailed();
					Utils.showErrorToast(code);
					srLayout.setEnabled(true);
					if (srLayout.isRefreshing()) {
						srLayout.setRefreshing(false);
					}
				}
			};
		}
		return conversationCallback;
	}

	private void setAdapter(final List<IMConversation> list) {
		if (adapter == null) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
			adapter = new QuickAdapter<IMConversation>(getContext(), R.layout.list_conversation_item) {
				protected void initViews(ViewHolder viewHolder, int position, final IMConversation item) {
					final ImageView ivIcon = viewHolder.getView(R.id.ivIcon);
					final TextView tvName = viewHolder.getView(R.id.tvName);
					TextView tvTime = viewHolder.getView(R.id.tvTime);
					TextView tvMsg = viewHolder.getView(R.id.tvMsg);
					TextView tvDot = viewHolder.getView(R.id.tvDot);

					IMMessage lastMessage = item.getLastMessage();
					if (lastMessage == null) {
						tvTime.setText(dateFormat.format(item.getCreateTime()));
						tvMsg.setText("");
					} else {
						tvTime.setText(dateFormat.format(lastMessage.getCreateTime()));
						IMMessage.Attach attach = lastMessage.getAttach();
						if (attach == null) {
							String body = lastMessage.getBody();
							if (TextUtils.isEmpty(body)) {
								tvMsg.setText("");
							} else {
								tvMsg.setText(Utils.changeStrToWithEmoji(getActivity(), lastMessage.getBody()));
							}
						} else if (attach.getType() == IMMessage.Attach.AUDIO) {
							tvMsg.setText(R.string.attach_audio);
						} else if (attach.getType() == IMMessage.Attach.FILE) {
							tvMsg.setText(R.string.attach_file);
						} else if (attach.getType() == IMMessage.Attach.IMAGE) {
							tvMsg.setText(R.string.attach_image);
						} else if (attach.getType() == IMMessage.Attach.VIDEO) {
							tvMsg.setText(R.string.attach_video);
						} else {
							tvMsg.setText("");
						}
					}
					if (item.getType() == IMConversation.TYPE_USER) {
						final IMUser user = item.getOtherInfo();
						UserManager.getFullUserInfo(user, new MobIMCallback<IMUser>() {
							public void onSuccess(IMUser imUser) {
								if (imUser == null) {
									LoadImageUtils.showAvatar(getContext(), ivIcon, user == null ? null : user.getAvatar(), R.drawable.ic_default_user);
									tvName.setText(user == null ? "" : user.getNickname());
								} else {
									item.setOtherInfo(imUser);
									LoadImageUtils.showAvatar(getContext(), ivIcon, imUser == null ? null : imUser.getAvatar(), R.drawable.ic_default_user);
									tvName.setText(imUser == null ? "" : imUser.getNickname());
								}
							}

							public void onError(int code, String message) {
								LoadImageUtils.showAvatar(getContext(), ivIcon, user == null ? null : user.getAvatar(), R.drawable.ic_default_user);
								tvName.setText(user == null ? "" : user.getNickname());
							}
						});
					} else if (item.getType() == IMConversation.TYPE_GROUP) {
						IMGroup imGroup = item.getGroupInfo();
						LoadImageUtils.showAvatar(getContext(), ivIcon, null, R.drawable.ic_group);
						if (imGroup == null) {
							tvName.setText("");
						} else {
							tvName.setText(imGroup.getName() + "(" + imGroup.getMemberSize() + ")");
						}
					} else if (item.getType() == IMConversation.TYPE_REMINDER) {
						IMReminder imReminder = item.getReminderInfo();
						if (imReminder == null) {
							tvName.setText("");
							LoadImageUtils.showAvatar(getContext(), ivIcon, null, R.drawable.ic_default_reminder);
						} else {
							LoadImageUtils.showAvatar(getContext(), ivIcon, imReminder.getAvatar(), R.drawable.ic_default_reminder);
							tvName.setText(imReminder.getName());
						}
					} else {
						LoadImageUtils.showAvatar(getContext(), ivIcon, null, 0);
						tvName.setText("");
					}
					tvDot.setVisibility(View.GONE);
					tvDot.setEnabled(!item.isDisturb());
					int unreadMsgCount = item.getUnreadMsgCount();
					if (unreadMsgCount > 0) {
						tvDot.setVisibility(View.VISIBLE);
						if (unreadMsgCount > 99) {
							tvDot.setText("99+");
						} else {
							tvDot.setText(String.valueOf(unreadMsgCount));
						}
					} else {
						tvDot.setVisibility(View.GONE);
					}
				}
			};
			listView.setAdapter(adapter);
		}
		adapter.refreshData(list, false);
		//刷新数据后，回到顶部
		listView.post(new Runnable() {
			public void run() {
				listView.setSelection(0);
			}
		});
	}


	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (srLayout.isRefreshing()) {
			//加载中，返回
			return;
		}
		//点击事件
		IMConversation conversation = (IMConversation) adapter.getItem(position);
		if (conversation == null) {
			return;
		}
		adapter.notifyDataSetChanged();
		if (conversation.getType() == IMConversation.TYPE_GROUP) {
			ChatActivity.gotoGroupChatPage(getActivity(), conversation);
		} else if (conversation.getType() == IMConversation.TYPE_USER) {
			ChatActivity.gotoUserChatPage(getActivity(), conversation);
		} else if (conversation.getType() == IMConversation.TYPE_REMINDER) {
			ReminderDetailsActivity.gotoReminderDetailsPageForResult(this, conversation.getReminderInfo(), 1001);
		}
		//清除未消息标记
		conversation.setUnreadMsgCount(0);
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (srLayout.isRefreshing()) {
			//加载中，返回
			return true;
		}
		//长按事件
		final IMConversation conversation = (IMConversation) adapter.getItem(position);
		if (conversation == null) {
			return true;
		}
		//清除未消息标记
		final int conversationType = conversation.getType();
		String targetId = null;
		if (conversationType == IMConversation.TYPE_GROUP) {
			IMGroup imGroup = conversation.getGroupInfo();
			if (imGroup != null) {
				targetId = imGroup.getId();
			}
		} else if (conversation.getType() == IMConversation.TYPE_USER) {
			IMUser imUser = conversation.getOtherInfo();
			if (imUser != null) {
				targetId = imUser.getId();
			}
		} else if (conversation.getType() == IMConversation.TYPE_REMINDER) {
			IMReminder imReminder = conversation.getReminderInfo();
			if (imReminder != null) {
				targetId = imReminder.getId();
			}
		}

		if (TextUtils.isEmpty(targetId)) {
			return true;
		}

		final String tmpId = targetId;
		DialogYesOrNo yesOrNo = new DialogYesOrNo(getActivity(), getResources().getString(R.string.tip_delete_conversation),
				R.string.cancel, R.string.txt_ok, new DialogYesOrNo.OnConfirmClickListener() {
			public void onConfirm() {
				if (MobIM.getChatManager().delConversation(tmpId, conversationType)) {
					conversation.setUnreadMsgCount(0);
					((MainActivity) getActivity()).freshUnreadMessageCount();
					refreshData();
				}
			}
		});
		yesOrNo.show();
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1001) {
			//从聊天界面回来时，更新未读消息总数
			((MainActivity) getActivity()).freshUnreadMessageCount();
		}
	}
}
