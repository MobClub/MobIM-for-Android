package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.DialogYesOrNo;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMConversation;
import com.mob.imsdk.model.IMGroup;
import com.mob.imsdk.model.IMUser;

import java.util.ArrayList;
import java.util.List;


public class ChatDetailFragment extends BaseFragment {

	private Button btnBack;
	private TextView tvNick;
	private boolean isGroup;
	private String id;
	private GridView gdvFriends;
	private TextView txtSummaray;
	private TextView txtNotice;
	private TextView txtGroupName;
	private TextView txtGroupNO;
	private TextView txtGNick;
	private IMUser imUser;
	private Switch swhNoMsg;
	private IMGroup group;
	private boolean isGroupOwner;
	private Button btnDelAndExit = null;
	private TextView txtShowMoreFriends;
	private View layoutChagneGroupOwner;
	private static final String TAG = "ChatDetailFragment";
	private AdapterView.OnItemClickListener onItemClickListener = null;
	private View.OnClickListener layoutClick = null;
	private View.OnClickListener onClickListener = null;

	//	public static ChatDetailFragment instace(String id,boolean isgroup) {
//		ChatDetailFragment chatdetial = new ChatDetailFragment();
//		Bundle bundle = new Bundle();
//		bundle.putBoolean("isgroup",isgroup);
//		bundle.putString("id",id);
//		chatdetial.setArguments(bundle);
//		return chatdetial;
//	}
	public static ChatDetailFragment instace(IMGroup group) {
		ChatDetailFragment chatdetial = new ChatDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isgroup", true);
		bundle.putString("id", group.getId());
		bundle.putSerializable("group", group);
		chatdetial.setArguments(bundle);
		return chatdetial;
	}

	public static ChatDetailFragment instace(IMUser user) {
		ChatDetailFragment chatdetial = new ChatDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isgroup", false);
		bundle.putString("id", user.getId());
		bundle.putSerializable("user", user);
		chatdetial.setArguments(bundle);
		return chatdetial;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isGroup = getArguments().getBoolean("isgroup");
		if (isGroup) {
			group = (IMGroup) getArguments().getSerializable("group");
			id = group.getId();

			Utils.showLog("chatdetailFragment", " ===== group.getId() " + group.getId());
		} else {
			imUser = (IMUser) getArguments().getSerializable("user");
			Utils.showLog("chatdetailFragment", " ===== user.getId() " + imUser.getId());
			id = imUser.getId();
		}

	}

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (isGroup) {
			return inflater.inflate(R.layout.fragment_groupchatdetail, container, false);
		}
		return inflater.inflate(R.layout.fragment_chatdetail, container, false);
	}

	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		onItemClickListener = new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!isGroup) {
					if (position == gdvFriends.getCount() - 1) {//删除群里的好友
						Intent intentnow = new Intent(getActivity(), GroupCreateActivity.class);
//					intentnow.putExtra("groupid",group.getId());
//					intentnow.putExtra("members",group.getMemberList());
//					intentnow.putExtra("control",GroupPickUserActivity.FOR_REMOVE_MEMBER);
						intentnow.putExtra("defuid", imUser.getId());
						startActivityForResult(intentnow, 10001);
					}
				} else {
					if (position == gdvFriends.getCount() - 2 && isGroupOwner) {//向群内添加好友
						Intent intentnow = new Intent(getActivity(), GroupPickUserActivity.class);
						intentnow.putExtra("groupid", group.getId());
						intentnow.putExtra("members", group.getMemberList());
						intentnow.putExtra("control", GroupPickUserActivity.FOR_ADD_MEMBER);
						startActivityForResult(intentnow, GroupPickUserActivity.FOR_ADD_MEMBER);
					} else if (position == gdvFriends.getCount() - 1 && isGroupOwner) {//删除群里的好友
						Intent intentnow = new Intent(getActivity(), GroupPickUserActivity.class);
						intentnow.putExtra("groupid", group.getId());
						intentnow.putExtra("members", group.getMemberList());
						intentnow.putExtra("control", GroupPickUserActivity.FOR_REMOVE_MEMBER);
						startActivityForResult(intentnow, GroupPickUserActivity.FOR_REMOVE_MEMBER);
					} else if (position == gdvFriends.getCount() - 1 && !isGroupOwner) {//删除群里的好友
						Intent intentnow = new Intent(getActivity(), GroupPickUserActivity.class);
						intentnow.putExtra("groupid", group.getId());
						intentnow.putExtra("members", group.getMemberList());
						intentnow.putExtra("control", GroupPickUserActivity.FOR_ADD_MEMBER);
						startActivityForResult(intentnow, GroupPickUserActivity.FOR_ADD_MEMBER);
					}
				}
			}
		};
		layoutClick = new View.OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.layoutSumary: {
						Intent intent = new Intent(getActivity(), InputForResultActivity.class);
						intent.putExtra("type", InputForResultActivity.FORSUMMARYGROUP);
						intent.putExtra("readonly", !isGroupOwner);
						intent.putExtra("showstr", txtSummaray.getText().toString());
//					Utils.showLog("ChatDetail"," ================== 1111 ");
						startActivityForResult(intent, InputForResultActivity.FORSUMMARYGROUP);
					} break;
					case R.id.layoutNotice: {
						Intent intent = new Intent(getActivity(), InputForResultActivity.class);
						intent.putExtra("type", InputForResultActivity.FORNOTICEGROUP);
						intent.putExtra("readonly", !isGroupOwner);
						intent.putExtra("showstr", txtNotice.getText().toString());
//					Utils.showLog("ChatDetail"," ================== 2222 ");
						startActivityForResult(intent, InputForResultActivity.FORNOTICEGROUP);
					} break;
					case R.id.layoutGoupName: {
						Intent intent = new Intent(getActivity(), InputForResultActivity.class);
						intent.putExtra("type", InputForResultActivity.FORNAMEGROUP);
						intent.putExtra("readonly", !isGroupOwner);
						intent.putExtra("showstr", txtGroupName.getText().toString());
//					Utils.showLog("ChatDetail"," ================== 2222 ");
						startActivityForResult(intent, InputForResultActivity.FORNAMEGROUP);
					} break;
					case R.id.layoutNick: {
						Intent intent = new Intent(getActivity(), InputForResultActivity.class);
						intent.putExtra("type", InputForResultActivity.FORNICKGROUP);
						intent.putExtra("readonly", false);
						intent.putExtra("showstr", txtGNick.getText().toString());
//					Utils.showLog("ChatDetail"," ================== 2222 ");
						startActivityForResult(intent, InputForResultActivity.FORNICKGROUP);
					} break;
					case R.id.layoutChagneGroupOwner: {
						Intent intentnow = new Intent(getActivity(), GroupPickUserActivity.class);
						intentnow.putExtra("groupid", group.getId());
						intentnow.putExtra("members", group.getMemberList());
						intentnow.putExtra("control", GroupPickUserActivity.FOR_CHANGE_OWNER);
						startActivityForResult(intentnow, GroupPickUserActivity.FOR_CHANGE_OWNER);
					} break;
				}
			}
		};

		onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.btnBack: {
						Utils.showLog(TAG, "");
						getActivity().setResult(Activity.RESULT_OK);
						getActivity().finish();
					} break;
					case R.id.btnDelAndExit: {
						String tip = null;
						if (isGroupOwner) {
							tip = getString(R.string.exitgroupower);
						} else {
							tip = getString(R.string.exitgroupmember);
						}
						DialogYesOrNo yesOrNo = new DialogYesOrNo(getActivity(), tip, R.string.cancel, R.string.txt_ok, new DialogYesOrNo.OnConfirmClickListener() {
							public void onConfirm() {
								MobIM.getGroupManager().exitGroup(group.getId(), new MobIMCallback<Void>() {
									public void onSuccess(Void result) {

									}
									public void onError(int code, String message) {

									}
								});
								getActivity().finish();
							}
						});
						yesOrNo.show();
					} break;
					case R.id.txtShowMoreFriends: {
						Intent intentnow = new Intent(getActivity(), GroupPickUserActivity.class);
						intentnow.putExtra("groupid", group.getId());
						intentnow.putExtra("members", group.getMemberList());
						intentnow.putExtra("control", GroupPickUserActivity.FOR_SHOW_ALL_MEMBERS);
						startActivityForResult(intentnow, GroupPickUserActivity.FOR_SHOW_ALL_MEMBERS);
					} break;
				}
			}
		};

		if (isGroup) {
			initGroupDetail(view);
		} else {
			initPrivateDetail(view);
		}
	}

	private void initGroupDetail(View view) {
		btnBack = (Button) view.findViewById(R.id.btnBack);
		tvNick = (TextView) view.findViewById(R.id.tvNick);
		txtSummaray = (TextView) view.findViewById(R.id.txtSummaray);
		txtNotice = (TextView) view.findViewById(R.id.txtNotice);
		txtGroupName = (TextView) view.findViewById(R.id.txtGroupName);
		txtGroupNO = (TextView) view.findViewById(R.id.txtGroupNO);
//		friendsLayout1 = (LinearLayout) view.findViewById(R.id.friendsLayout1);
//		friendsLayout2 = (LinearLayout) view.findViewById(R.id.friendsLayout2);
		gdvFriends = (GridView) view.findViewById(R.id.gdvFriends);
		txtGNick = (TextView) view.findViewById(R.id.txtGNick);
		btnDelAndExit = (Button) view.findViewById(R.id.btnDelAndExit);
		txtShowMoreFriends = (TextView) view.findViewById(R.id.txtShowMoreFriends);
		swhNoMsg = (Switch) view.findViewById(R.id.swhNoMsg);
		View layoutSumary = view.findViewById(R.id.layoutSumary);
		View layoutNotice = view.findViewById(R.id.layoutNotice);
		View layoutGoupName = view.findViewById(R.id.layoutGoupName);
		View layoutGroupId = view.findViewById(R.id.layoutGroupId);
		View layoutNick = view.findViewById(R.id.layoutNick);
		layoutChagneGroupOwner = view.findViewById(R.id.layoutChagneGroupOwner);

		layoutGroupId.setOnLongClickListener(new View.OnLongClickListener() {

			public boolean onLongClick(View v) {
				ClipboardManager clipboard = (ClipboardManager)
						getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("groupId", txtGroupNO.getText().toString());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(getActivity(), R.string.coped, Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		layoutSumary.setOnClickListener(layoutClick);
		layoutNotice.setOnClickListener(layoutClick);
		layoutGoupName.setOnClickListener(layoutClick);
		layoutNick.setOnClickListener(layoutClick);
		initGroupView();
	}

	private void initGroupView() {
		MobIM.getGroupManager().getGroupInfo(id, true, new MobIMCallback<IMGroup>() {
			public void onSuccess(final IMGroup imGroup) {
				if (imGroup == null) {
					getActivity().finish();
					return;
				}
				UserManager.getFullUserInfo(imGroup.getMemberList(), new MobIMCallback<ArrayList<IMUser>>() {
					public void onSuccess(ArrayList<IMUser> imUsers) {
						imGroup.setMemberList(imUsers);
						group = imGroup;
						Utils.showLog("ChatDetailFragment", " =======   group.getMemberList().size() >> " + group.getMemberList().size());
						String title = getString(R.string.groupdetail);
						String result = String.format(title, group.getMemberList().size());
						tvNick.setText(result);
						freshGroup();
						FriendListAdapter bindAdapter = new FriendListAdapter(group.getMemberList());
						gdvFriends.setHorizontalScrollBarEnabled(false);
						gdvFriends.setVerticalScrollBarEnabled(false);
						gdvFriends.setAdapter(bindAdapter);
						//GridView 外面嵌套一个 ScrollView 只显示一行数据，调用本方法，重新计算GridView的高度
						Utils.setGridViewHeightBasedOnChildren(gdvFriends);
						gdvFriends.setOnItemClickListener(onItemClickListener);
						getGroupMemberNick();
					}

					public void onError(int code, String message) {

					}
				});
			}

			public void onError(int code, String message) {
				getActivity().finish();
			}
		});
	}

	private void freshGroup() {
		if (group.getMemberSize() > 8 && isGroupOwner) {
			txtShowMoreFriends.setOnClickListener(onClickListener);
		} else if (group.getMemberSize() > 9 && !isGroupOwner) {
			txtShowMoreFriends.setOnClickListener(onClickListener);
		} else {
			txtShowMoreFriends.setVisibility(View.GONE);
		}
		btnDelAndExit.setOnClickListener(onClickListener);
		txtGroupName.setText(group.getName());
		txtGroupNO.setText(group.getId());

		IMUser owner = UserManager.getUser();
		if (owner.getId().equals(group.getOwnerId())) {
			isGroupOwner = true;
		} else {
			isGroupOwner = false;
		}
		txtSummaray.setText(group.getDesc());
		txtNotice.setText(group.getNotice());


		if (isGroupOwner) {
			layoutChagneGroupOwner.setOnClickListener(layoutClick);
		} else {
			layoutChagneGroupOwner.setVisibility(View.GONE);
		}

		String title = getString(R.string.groupdetail);
		String result = String.format(title, group.getMemberSize());
		tvNick.setText(result);
		btnBack.setOnClickListener(onClickListener);

		MobIM.getChatManager().getConversationDisturb(group.getId(), IMConversation.TYPE_GROUP, new MobIMCallback<Boolean>() {

			public void onSuccess(Boolean aBoolean) {
				if (aBoolean) {
					swhNoMsg.setChecked(true);
				} else {
					swhNoMsg.setChecked(false);
				}
			}

			public void onError(int code, String message) {

			}
		});
		swhNoMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MobIM.getChatManager().setConversationDisturb(group.getId(), IMConversation.TYPE_GROUP, isChecked, new MobIMCallback<Boolean>() {

					public void onSuccess(Boolean aBoolean) {
						swhNoMsg.setEnabled(true);

					}

					public void onError(int code, String message) {
						swhNoMsg.setEnabled(true);
					}
				});
				swhNoMsg.setEnabled(false);
			}
		});
	}

	private void getGroupMemberNick() {
		IMUser owner = UserManager.getUser();
		List<IMUser> users = group.getMemberList();

		//boolean find = false;
		String nick = null;
		if (users != null) {
			for (int i = 0; i < users.size(); i++) {
				IMUser temp = users.get(i);
				if (temp.getId().equals(owner.getId())) {
					nick = temp.getGroupNickname();
				}
			}
			if (nick == null || TextUtils.isEmpty(nick)) {
				nick = owner.getNickname();
			}

			txtGNick.setText(nick);
		} else {
			txtGNick.setText(owner.getNickname());
		}
	}

	private void initPrivateDetail(View view) {
		btnBack = (Button) view.findViewById(R.id.btnBack);
		tvNick = (TextView) view.findViewById(R.id.tvNick);
		gdvFriends = (GridView) view.findViewById(R.id.gdvFriends);
		swhNoMsg = (Switch) view.findViewById(R.id.swhNoMsg);
		List<IMUser> users = new ArrayList<IMUser>();
		if (imUser != null) {
			users.add(imUser);
		} else {
			users.add(new IMUser());
		}
		MobIM.getChatManager().getConversationDisturb(imUser.getId(), IMConversation.TYPE_USER, new MobIMCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				if (aBoolean) {
					swhNoMsg.setChecked(true);
				} else {
					swhNoMsg.setChecked(false);
				}
			}

			@Override
			public void onError(int code, String message) {

			}
		});
		swhNoMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MobIM.getChatManager().setConversationDisturb(imUser.getId(), IMConversation.TYPE_USER, isChecked, new MobIMCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean aBoolean) {
						swhNoMsg.setEnabled(true);

					}

					@Override
					public void onError(int code, String message) {
						swhNoMsg.setEnabled(true);
					}
				});
				swhNoMsg.setEnabled(false);
			}
		});

		FriendListAdapter bindAdapter = new FriendListAdapter(users);
		gdvFriends.setHorizontalScrollBarEnabled(false);
		gdvFriends.setVerticalScrollBarEnabled(false);
		gdvFriends.setAdapter(bindAdapter);
		//GridView 外面嵌套一个 ScrollView 只显示一行数据，调用本方法，重新计算GridView的高度
		Utils.setGridViewHeightBasedOnChildren(gdvFriends);
		//gdvFriends.setOnClickListener(onClickListener);
		gdvFriends.setOnItemClickListener(onItemClickListener);
		btnBack.setOnClickListener(onClickListener);
	}

	private class FriendListAdapter extends BaseAdapter {
		private List<IMUser> users = null;
		private LayoutInflater layoutInflater;

		public FriendListAdapter(List<IMUser> users) {
			this.users = users;
			if (users == null) {
				users = new ArrayList<IMUser>();
			}
			layoutInflater = LayoutInflater.from(getContext());
		}

		public int getCount() {
			int size = users.size();
			if (!isGroup) {
				return users == null ? 0 : users.size() + 1;
			}
			if (size > 8 && isGroupOwner && isGroup) {
				return 10;
			}
			if (size > 9 && !isGroupOwner && isGroup) {
				return 10;
			}
//			Utils.showLog(TAG, " isGroupOwner >>>  " + isGroupOwner);
//			Utils.showLog(TAG, " isGroup >>>  " + isGroup);

			if (isGroupOwner && isGroup) {
				return users == null ? 0 : users.size() + 2;
			}
			return users == null ? 0 : users.size() + 1;
		}

		public Object getItem(int position) {
			if (position > users.size() - 1) {
				return null;
			}
			return users.get(position);
		}

		public long getItemId(int position) {
			return position;
		}


		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.friend_item, null);
				holder = ViewHolder.getHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
//			Utils.showLog("ChatDetail", " =========== now position >> " + position);
//			Utils.showLog("ChatDetail", " =========== getCount() - 2>> " + (getCount() - 2));
//			Utils.showLog("ChatDetail", " =========== getCount()>> " + (getCount()));
			if (position >= getCount() - 2 && isGroupOwner) {
//				Utils.showLog("ChatDetail"," in getcont - 2 "+position);
				if (position == getCount() - 2) {
					holder.igvFace.setImageResource(R.drawable.chat_add);
				} else {
					holder.igvFace.setImageResource(R.drawable.chat_remove);
				}
				holder.txtNick.setText("");
			} else if (position >= getCount() - 1 && !isGroupOwner) {
				if (position == getCount() - 1) {
					holder.igvFace.setImageResource(R.drawable.chat_add);
				}
				holder.txtNick.setText("");
			} else if (position >= (getCount() - 1) && !isGroup) {
//				Utils.showLog("ChatDetail"," in getcont - 2 "+position);
				if (position == (getCount() - 1)) {
					holder.igvFace.setImageResource(R.drawable.chat_add);
				}
//				else if(position == (getCount() - 1) ) {
//					holder.igvFace.setImageResource(R.drawable.chat_remove);
//				}
				holder.txtNick.setText("");
			} else {
				final IMUser user = (IMUser) getItem(position);
				//holder.igvFace.setImageResource(R.drawable.face1);
				if (user != null) {
					LoadImageUtils.showAvatar(getContext(), holder.igvFace, user.getAvatar(), R.drawable.ic_default_user);
					holder.txtNick.setText(user.getNickname());
					holder.igvFace.setOnClickListener(new View.OnClickListener() {

						public void onClick(View v) {
							UserDetailsActivity.gotoUserDetailsPage(getActivity(), user);
						}
					});
//					Utils.showLog("ChatDetail", " =====  user.getNickname() >> " + user.getNickname() + " user.getAvatar() >>  " + user.getAvatar());
//					Utils.showLog("ChatDetail", " =====  user.getId() >>  " + user.getId());
				} else {
					holder.txtNick.setText("");
					holder.igvFace.setImageResource(R.drawable.ic_default_user);
				}


//				Utils.showLog("ChatDetail"," in the not getcont - 2   position ");
			}
			return convertView;
		}
	}

	public void onDestroy() {
		super.onDestroy();
		getActivity().setResult(Activity.RESULT_OK);
	}

	static class ViewHolder {
		ImageView igvFace;
		TextView txtNick;

		static ViewHolder getHolder(View contentView) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.igvFace = (ImageView) contentView.findViewById(R.id.igvFace);
			viewHolder.txtNick = (TextView) contentView.findViewById(R.id.txtNick);
			return viewHolder;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		Utils.showLog("ChatDeailFragment", " ============ onActivityResult >>  requestCode >>  " + requestCode);
		if (resultCode == Activity.RESULT_OK) {
//			Utils.showLog("ChatDeailFragment", " rev data ======= >> Activity.RESULT_OK ");
			if (data != null) {
				String newdata = data.getStringExtra("data");
//				Utils.showLog("ChatDeailFragment"," rev data ======= >> "+newdata);
				if (newdata != null) {
					if (requestCode == InputForResultActivity.FORNAMEGROUP) {
						txtGroupName.setText(newdata);
						group.setName(newdata);
					} else if (requestCode == InputForResultActivity.FORNICKGROUP) {
						txtGNick.setText(newdata);
						MobIM.getGroupManager().updateUserNickname(group.getId(), newdata, new MobIMCallback<Void>() {
							public void onSuccess(Void result) {
								Toast.makeText(getContext(), R.string.saveok, Toast.LENGTH_SHORT).show();
							}

							public void onError(int code, String message) {
								Toast.makeText(getContext(), R.string.savefail, Toast.LENGTH_SHORT).show();
							}
						});
						return;
					} else if (requestCode == InputForResultActivity.FORNOTICEGROUP) {
						txtNotice.setText(newdata);
						group.setNotice(newdata);
					} else if (requestCode == InputForResultActivity.FORSUMMARYGROUP) {
						txtSummaray.setText(newdata);
						group.setDesc(newdata);
					}
					MobIM.getGroupManager().updateGroupInfo(group.getId(), group.getName(), group.getDesc(), group.getNotice(), new MobIMCallback<Void>() {
						public void onSuccess(Void result) {
							Toast.makeText(getContext(), R.string.saveok, Toast.LENGTH_SHORT).show();
						}

						public void onError(int code, String message) {
							Toast.makeText(getContext(), R.string.savefail, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}

			if (requestCode == GroupPickUserActivity.FOR_ADD_MEMBER) {
				MobIM.getGroupManager().getGroupInfo(group.getId(), true, new MobIMCallback<IMGroup>() {
					@Override
					public void onSuccess(final IMGroup imGroup) {
						if (imGroup == null) {
							group = imGroup;
							initGroupView();
						} else {
							UserManager.getFullUserInfo(imGroup.getMemberList(), new MobIMCallback<ArrayList<IMUser>>() {
								public void onSuccess(ArrayList<IMUser> imUsers) {
									imGroup.setMemberList(imUsers);
									group = imGroup;
									initGroupView();
								}

								public void onError(int code, String message) {

								}
							});
						}
					}

					@Override
					public void onError(int code, String message) {

					}
				});
			} else if (requestCode == GroupPickUserActivity.FOR_CHANGE_OWNER) {
				isGroupOwner = false;
				initGroupView();
			} else if (requestCode == GroupPickUserActivity.FOR_REMOVE_MEMBER) {
//				Utils.showLog(TAG, " ========== the FOR_REMOVE_MEMBER in  ");
				MobIM.getGroupManager().getGroupInfo(group.getId(), true, new MobIMCallback<IMGroup>() {
					public void onSuccess(final IMGroup imGroup) {
						if (imGroup == null) {
//							Utils.showLog(TAG, " ========== the group infor is not null");
							group = imGroup;
							checkAndDelGroup();
							initGroupView();
						} else {
//							Utils.showLog(TAG, " ========== the group infor is null");
							UserManager.getFullUserInfo(imGroup.getMemberList(), new MobIMCallback<ArrayList<IMUser>>() {
								public void onSuccess(ArrayList<IMUser> imUsers) {
//									Utils.showLog(TAG, " ========== onSuccess 2222 ");
									imGroup.setMemberList(imUsers);
									group = imGroup;
									checkAndDelGroup();
									initGroupView();
								}

								public void onError(int code, String message) {
//									Utils.showLog(TAG, " ========== onError 2222 ");
								}
							});
						}
					}

					public void onError(int code, String message) {

					}
				});
			}
		} else {
//			Utils.showLog("ChatDeailFragment", " rev data not ======= >> Activity.RESULT_OK ");
			if (requestCode == GroupPickUserActivity.FOR_REMOVE_MEMBER) {
//				Utils.showLog(TAG, " ========== the FOR_REMOVE_MEMBER in  ");
				MobIM.getGroupManager().getGroupInfo(group.getId(), true, new MobIMCallback<IMGroup>() {
					public void onSuccess(final IMGroup imGroup) {
						if (imGroup == null) {
//							Utils.showLog(TAG, " ========== the group infor is not null");
							group = imGroup;
							checkAndDelGroup();
							initGroupView();
						} else {
//							Utils.showLog(TAG, " ========== the group infor is null");
							UserManager.getFullUserInfo(imGroup.getMemberList(), new MobIMCallback<ArrayList<IMUser>>() {
								public void onSuccess(ArrayList<IMUser> imUsers) {
//									Utils.showLog(TAG, " ========== onSuccess 2222 ");
									imGroup.setMemberList(imUsers);
									group = imGroup;
									checkAndDelGroup();
									initGroupView();
								}

								public void onError(int code, String message) {
//									Utils.showLog(TAG, " ========== onError 2222 ");
								}
							});
						}
					}

					public void onError(int code, String message) {

					}
				});
			}
//			Utils.showLog("ChatDeailFragment"," rev data ======= >> not Activity.RESULT_OK ");
		}
	}

	private void checkAndDelGroup() {
		if (group.getMemberSize() <= 2) {
			if (isGroupOwner) {
				MobIM.getGroupManager().exitGroup(group.getId(), new MobIMCallback<Void>() {
					public void onSuccess(Void result) {
						Utils.showErrorToast(R.string.delallgroupmember);
						getActivity().finish();
					}

					public void onError(int code, String message) {

					}
				});
			}
		}
	}
}
