package com.mob.demo.mobim.model;

import com.mob.imsdk.MobIMMessageReceiver;

public class MsgReceiverListener {

	private String fromId;
	private MobIMMessageReceiver imMessageReceiver;
	public MsgReceiverListener(String fromId,MobIMMessageReceiver imMessageReceiver) {
		this.fromId = fromId;
		this.imMessageReceiver = imMessageReceiver;
	}
	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public MobIMMessageReceiver getImMessageReceiver() {
		return imMessageReceiver;
	}

	public void setImMessageReceiver(MobIMMessageReceiver imMessageReceiver) {
		this.imMessageReceiver = imMessageReceiver;
	}
}
