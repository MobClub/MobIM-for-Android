package com.mob.demo.mobim;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.mob.demo.mobim.model.MsgReceiverListener;
import com.mob.demo.mobim.ui.ChatActivity;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.MobIMMessageReceiver;
import com.mob.imsdk.model.IMGroup;
import com.mob.imsdk.model.IMMessage;
import com.mob.tools.utils.DeviceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleMobIMMessageReceiver implements MobIMMessageReceiver {
	private Context context;
	private HashMap<String, MsgReceiverListener> receiverListeners;
	private HashMap<String, MsgReceiverListener> groupreceiverListeners;
	private NotificationManager notificationManager;

	public SimpleMobIMMessageReceiver(Context context) {
		this.context = context;
		if (receiverListeners == null) {
			receiverListeners = new HashMap<String, MsgReceiverListener>();
			groupreceiverListeners = new HashMap<String, MsgReceiverListener>();
		}
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void onMessageReceived(List<IMMessage> messageList) {
		int size = messageList.size();
		for (int i = 0; i < size; i++) {
			IMMessage imMessage = messageList.get(i);
			//1v1 聊天信息进行分发
			if (imMessage.getType() == IMMessage.TYPE_USER) {
				MsgReceiverListener listener = receiverListeners.get(imMessage.getFrom());
				if (listener != null) {
					List<IMMessage> msgs = new ArrayList<IMMessage>();
					msgs.add(imMessage);
					listener.getImMessageReceiver().onMessageReceived(msgs);
				} else {
					String msgType = imMessage.getBody();

					if (msgType == null) {
						if (imMessage.getAttach() != null) {
							if (imMessage.getAttach().getType() == IMMessage.Attach.AUDIO) {
								msgType = context.getString(R.string.audio);
							} else if (imMessage.getAttach().getType() == IMMessage.Attach.IMAGE) {
								msgType = context.getString(R.string.pic);
							} else if (imMessage.getAttach().getType() == IMMessage.Attach.VIDEO) {
								msgType = context.getString(R.string.video);
							} else {
								msgType = context.getString(R.string.attach);
							}
						}
					}
					final String msg = msgType;
					//显示Status Bar
					addNotifyction(R.mipmap.ic_launcher, imMessage.getFromUserInfo().getNickname(), msg, false, imMessage.getFrom());
				}
			} else if (imMessage.getType() == IMMessage.TYPE_GROUP) {
				//接收对象应该是 groupId
				MsgReceiverListener listener = groupreceiverListeners.get(imMessage.getTo());
				if (listener != null) {
					List<IMMessage> msgs = new ArrayList<IMMessage>();
					msgs.add(imMessage);
					listener.getImMessageReceiver().onMessageReceived(msgs);
				} else {
					String msgType = imMessage.getBody();

					if (msgType == null) {
						if (imMessage.getAttach() != null) {
							if (imMessage.getAttach().getType() == IMMessage.Attach.AUDIO) {
								msgType = context.getString(R.string.audio);
							} else if (imMessage.getAttach().getType() == IMMessage.Attach.IMAGE) {
								msgType = context.getString(R.string.pic);
							} else if (imMessage.getAttach().getType() == IMMessage.Attach.VIDEO) {
								msgType = context.getString(R.string.video);
							} else {
								msgType = context.getString(R.string.attach);
							}
						}
					}
					final String msg = msgType;
					MobIM.getGroupManager().getGroupInfo(imMessage.getTo(), false, new MobIMCallback<IMGroup>() {
						@Override
						public void onSuccess(IMGroup imGroup) {
							addNotifyction(R.mipmap.ic_launcher, imGroup.getName(), msg, true, imGroup.getId());
						}

						@Override
						public void onError(int code, String message) {

						}
					});
					//显示Status Bar

				}
			} else if (imMessage.getType() == IMMessage.TYPE_WARN) {
				//接收对象应该是 groupId
				String id = (String) imMessage.getWarnData().getData().get("id");
				MsgReceiverListener listener = groupreceiverListeners.get(id);
				if (listener != null) {
					List<IMMessage> msgs = new ArrayList<IMMessage>();
					msgs.add(imMessage);
					listener.getImMessageReceiver().onMessageReceived(msgs);
				} else {
				}
			}
		}
	}

	private void addNotifyction(int icon, String name, String msg, boolean isGroup, String id) {
		//设置一个Intent,不然点击通知不会自动消失
		if (DeviceHelper.getInstance(context).amIOnForeground()) {
			//应用在前台时，不添加通知
			return;
		}
		Intent resultIntent = new Intent(context, ChatActivity.class);
		resultIntent.putExtra("isGroup", isGroup);
		resultIntent.putExtra("id", id);
		resultIntent.putExtra("frombar", true);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(name)
				.setContentText(msg)
				.setContentIntent(resultPendingIntent);
		Notification notification = builder.build();
		//设置 Notification 的 flags = FLAG_NO_CLEAR
		//FLAG_AUTO_CANCEL 表示该通知能被状态栏的清除按钮给清除掉
		//等价于 builder.setAutoCancel(true);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify((int) System.currentTimeMillis(), notification);
	}

	public void addMsgRever(MsgReceiverListener listener) {
		if (!receiverListeners.containsKey(listener.getFromId())) {
			receiverListeners.put(listener.getFromId(), listener);
		}
	}

	public void removeMsgRever(MsgReceiverListener listener) {
		if (receiverListeners.containsKey(listener.getFromId())) {
			receiverListeners.remove(listener.getFromId());
		}
	}

	public void addGroupMsgRever(MsgReceiverListener listener) {
		if (!groupreceiverListeners.containsKey(listener.getFromId())) {
			groupreceiverListeners.put(listener.getFromId(), listener);
		}
	}

	public void removeGroupMsgRever(MsgReceiverListener listener) {
		if (groupreceiverListeners.containsKey(listener.getFromId())) {
			groupreceiverListeners.remove(listener.getFromId());
		}
	}
}
