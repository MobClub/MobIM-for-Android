package com.mob.demo.mobim.model;

import android.text.TextUtils;

import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.model.IMMessage;

public class MsgItem {

	public static final int TEXT_REV = 15;
	public static final int TEXT_SEND = 1;

	public static final int AUDIO_REV = 2;
	public static final int AUDIO_SEND = 3;

	public static final int IMAGE_SEND = 4;
	public static final int IMAGE_REV = 5;

	public static final int VIDEO_REV = 6;
	public static final int VIDEO_SEND = 7;

	public static final int DIY_REV = 8;
	public static final int DIY_SEND = 9;

	public static final int FILE_REV = 10;
	public static final int FILE_SEND = 11;

	public static final int LOCATION_REV = 12;
	public static final int LOCATION_SEND = 13;

	public static final int TIME_SHOW = 14;
	public static final int WARN_DATA = 16;

	/**
	 * 是否是本地发送的
	 */
	private boolean isLocalSend;

	/**
	 * 封装的消息
	 */
	private IMMessage imMessage;

	/***
	 * 显示时间 or 显示 已撤回
	 */
	private String tip;
	/**
	 * 信息类型
	 * 以下类型：文本 语音 图片 视频 自定义表情
	 * 收到 还是 本地发送
	 * 显示时间 间断
	 * 显示已撤回 or 发送失败 （可进行重发 操作）
	 * 仅仅在本地显示而已，并不参与信息的收发
	 */
	private int msgType;
	/**
	 * 接受时间
	 */
	private long revTime;

	/**
	 * 本地消息发送状态： 发送成功 发送失败
	 */
	private int status;

	/**
	 * 标识用于发送的语音 or 视频 是否已经被播放过，如果被播放过则改为已读状态
	 */
	private boolean isRead = false;


	public static MsgItem getMsgItemFromImMessage(IMMessage message) {
		MsgItem msgItem = new MsgItem();
		msgItem.setImMessage(message);
		if (message.getFrom() == null || TextUtils.isEmpty(message.getFrom())) {
			msgItem.setLocalSend(true);
		} else {
			if (Utils.getLocalUserID() != null && Utils.getLocalUserID().equals(message.getFrom())) {
				msgItem.setLocalSend(true);
			} else {
				msgItem.setLocalSend(false);
			}

		}
		msgItem.setRev_time(message.getCreateTime());
		msgItem.setStatus(message.getStatus());
		if (message.getAttach() != null) {
			if (message.getAttach().getType() == IMMessage.Attach.AUDIO) {
				if (msgItem.isLocalSend) {
					msgItem.setMsg_type(AUDIO_SEND);
				} else {
					msgItem.setMsg_type(AUDIO_REV);
					msgItem.setRead(message.getAttach().isPlay());
				}
			} else if (message.getAttach().getType() == IMMessage.Attach.IMAGE) {
				if (msgItem.isLocalSend) {
					msgItem.setMsg_type(IMAGE_SEND);
				} else {
					msgItem.setMsg_type(IMAGE_REV);
				}
			} else if (message.getAttach().getType() == IMMessage.Attach.TEXT) {
				if (msgItem.isLocalSend) {
					msgItem.setMsg_type(TEXT_SEND);
				} else {
					msgItem.setMsg_type(TEXT_REV);
				}
			} else if (message.getAttach().getType() == IMMessage.Attach.VIDEO) {
				if (msgItem.isLocalSend) {
					msgItem.setMsg_type(VIDEO_SEND);
				} else {
					msgItem.setMsg_type(VIDEO_REV);
				}
			} else if (message.getAttach().getType() == IMMessage.Attach.FILE) {
				if (msgItem.isLocalSend) {
					msgItem.setMsg_type(FILE_SEND);
				} else {
					msgItem.setMsg_type(FILE_REV);
				}
			}
		} else if (message.getType() == IMMessage.TYPE_WARN && message.getWarnData() != null) {
			msgItem.setMsg_type(WARN_DATA);
		} else {
			if (msgItem.isLocalSend) {
				msgItem.setMsg_type(TEXT_SEND);
			} else {
				msgItem.setMsg_type(TEXT_REV);
			}
		}
		return msgItem;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean read) {
		isRead = read;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getMsg_type() {
		return msgType;
	}

	public void setMsg_type(int msg_type) {
		this.msgType = msg_type;
	}

	public long getRev_time() {
		return revTime;
	}

	public void setRev_time(long rev_time) {
		this.revTime = rev_time;
	}

	public boolean isLocalSend() {
		return isLocalSend;
	}

	public void setLocalSend(boolean localSend) {
		isLocalSend = localSend;
	}

	public IMMessage getImMessage() {
		return imMessage;
	}

	public void setImMessage(IMMessage imMessage) {
		this.imMessage = imMessage;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}
}
