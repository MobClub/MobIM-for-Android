package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GroupPickUserActivity extends BaseActivity implements View.OnClickListener {
	private Button btnConfirm;
	private QuickAdapter<IMUser> adapter;
	private HashSet<IMUser> selectedUser = new HashSet<IMUser>();
	/**
	 * 转让群给成员
	 */
	public static final int FOR_CHANGE_OWNER = 0;
	/**
	 * 从好友里添加成员到群
	 */
	public static final int FOR_ADD_MEMBER = 1;
	/**
	 * 从群里移除成员
	 */
	public static final int FOR_REMOVE_MEMBER = 2;

	/**
	 * 显示所有成员
	 */
	public static final int FOR_SHOW_ALL_MEMBERS = 3;

	private int controlType = 0 ;
	private List<IMUser> members;
	private ListView listView = null;
	private TextView tvEmpty = null;
	private HashSet<String> ids = new HashSet<String>();
	private String groupId = null;
	View.OnClickListener onSelectClickListener = null;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		controlType = getIntent().getIntExtra("control",0);
		groupId = getIntent().getStringExtra("groupid");
		members = (List<IMUser>) getIntent().getSerializableExtra("members");
		setContentView(R.layout.activity_group_add_user);
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		onSelectClickListener = new View.OnClickListener() {
			public void onClick(View v) {
				int position = (Integer) v.getTag();
				IMUser item = adapter.getItem(position);

				if(controlType == FOR_CHANGE_OWNER) {
					selectedUser.clear();
					if (v.isSelected()) {
//					v.setSelected(false);
//					selectedUser.remove(item);
					} else {
						v.setSelected(true);
						selectedUser.add(item);
					}
					adapter.notifyDataSetChanged();
				} else {
					if (v.isSelected()) {
						v.setSelected(false);
						selectedUser.remove(item);
					} else {
						v.setSelected(true);
						selectedUser.add(item);
					}
					//btnConfirm.setText(getString(R.string.txt_confirm_create_group, String.valueOf(selectedUser.size())));
				}

			}
		};
		findViewById(R.id.ivTitleLeft).setOnClickListener(this);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(this);

		btnConfirm.setText(getString(R.string.txt_confirm_create_group, "0"));

		listView = (ListView) findViewById(R.id.listView);
		tvEmpty = (TextView) findViewById(R.id.tvEmpty);

		if(controlType == FOR_CHANGE_OWNER) {
			tvTitle.setText(R.string.changegroupowner);
			btnConfirm.setText(R.string.confirmchange);

			if(members != null) {
				IMUser user = UserManager.getUser();
				for(int i = 0;i < members.size(); i++) {
					if(members.get(i).getId().equals(user.getId())){
						members.remove(i);
						break;
					}
				}
				showUsers(members);
			}

		} else if(controlType == FOR_ADD_MEMBER) {
			tvTitle.setText(R.string.addgroupmember);
//			btnConfirm.setVisibility(View.GONE);
			btnConfirm.setText(R.string.confirm);

			for (int i = 0; i < members.size(); i++) {
				ids.add(members.get(i).getId());
			}

			UserManager.getFriends(new MobIMCallback<List<IMUser>>() {
				public void onSuccess(List<IMUser> imUsers) {
					if (imUsers == null || imUsers.isEmpty()) {
						listView.setVisibility(View.GONE);
						tvEmpty.setVisibility(View.VISIBLE);
						return;
					}

					if (imUsers != null && imUsers.size() > 0) {
						for (int i = imUsers.size() - 1; i >= 0; i--) {
							if (ids.contains(imUsers.get(i).getId())) {
								imUsers.remove(i);
							}
						}
					}

					tvEmpty.setVisibility(View.GONE);
					listView.setVisibility(View.VISIBLE);

					adapter = new QuickAdapter<IMUser>(GroupPickUserActivity.this, R.layout.list_group_add_user_item, imUsers) {
						protected void initViews(ViewHolder viewHolder, int position, IMUser item) {
							ImageView ivSelect = viewHolder.getView(R.id.ivSelect);
							ImageView ivIcon = viewHolder.getView(R.id.ivIcon);
							TextView tvName = viewHolder.getView(R.id.tvName);
							tvName.setText(item.getNickname());
							LoadImageUtils.showAvatar(GroupPickUserActivity.this, ivIcon, item.getAvatar(), R.drawable.ic_default_user);

							ivSelect.setTag(position);
							ivSelect.setSelected(selectedUser.contains(item));
							ivSelect.setOnClickListener(onSelectClickListener);
						}
					};
					listView.setAdapter(adapter);
				}

				public void onError(int code, String message) {
					listView.setVisibility(View.GONE);
					tvEmpty.setVisibility(View.VISIBLE);
				}
			});

		} else if(controlType == FOR_REMOVE_MEMBER) {
			tvTitle.setText(R.string.delgroupmember);
			btnConfirm.setText(R.string.del);
			if(members != null) {
				IMUser user = UserManager.getUser();
				for(int i = 0;i < members.size(); i++) {
					if(members.get(i).getId().equals(user.getId())){
						members.remove(i);
						break;
					}
				}
				showUsers(members);
			}
		} else if(controlType == FOR_SHOW_ALL_MEMBERS) {
			tvTitle.setText(getString(R.string.sumpeople,members.size()));
			btnConfirm.setVisibility(View.GONE);
			showUsers(members);
		}
	}

	private void showUsers(List<IMUser> imUsers) {
		tvEmpty.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		adapter = new QuickAdapter<IMUser>(GroupPickUserActivity.this, R.layout.list_group_add_user_item, imUsers) {
			protected void initViews(ViewHolder viewHolder, int position, final IMUser item) {
				ImageView ivSelect = viewHolder.getView(R.id.ivSelect);
				ImageView ivIcon = viewHolder.getView(R.id.ivIcon);
				TextView tvName = viewHolder.getView(R.id.tvName);
				tvName.setText(item.getNickname());
				LoadImageUtils.showAvatar(GroupPickUserActivity.this, ivIcon, item.getAvatar(), R.drawable.ic_default_user);

				ivIcon.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						UserDetailsActivity.gotoUserDetailsPage(GroupPickUserActivity.this,item);
					}
				});
				if(controlType == FOR_SHOW_ALL_MEMBERS) {
					ivSelect.setVisibility(View.GONE);
				} else {
					ivSelect.setTag(position);
					ivSelect.setSelected(selectedUser.contains(item));
					ivSelect.setOnClickListener(onSelectClickListener);
				}
			}
		};
		listView.setAdapter(adapter);
	}

	public void onClick(View v) {
		if (v == btnConfirm) {
			Intent intent = getIntent();
			if (intent == null) {
				return;
			}
			if (selectedUser.isEmpty()) {
				Toast.makeText(getApplicationContext(), R.string.tipempty, Toast.LENGTH_SHORT).show();
				return;
			}
			String name = intent.getStringExtra("name");
			String profile = intent.getStringExtra("profile");
			List<String> list = new ArrayList<String>();
			for (IMUser item : selectedUser) {
				if (item != null) {
					list.add(item.getId());
				}
			}

			if(controlType == FOR_CHANGE_OWNER) {
				MobIM.getGroupManager().transferGroup(groupId, list.get(0), new MobIMCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						Toast.makeText(getApplicationContext(), R.string.dosucess, Toast.LENGTH_SHORT).show();
						setResult(Activity.RESULT_OK);
						onBackPressed();
					}

					public void onError(int code, String message) {
						Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				});
			} else if(controlType == FOR_ADD_MEMBER) {
				MobIM.getGroupManager().inviteUserIntoGroup(groupId, list.toArray(new String[]{}), new MobIMCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						Toast.makeText(getApplicationContext(), R.string.dosucess, Toast.LENGTH_SHORT).show();
						setResult(Activity.RESULT_OK);
						onBackPressed();
					}

					@Override
					public void onError(int code, String message) {
						Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				});
			} else if(controlType == FOR_REMOVE_MEMBER) {
				MobIM.getGroupManager().removeUserFromGroup(groupId, list.toArray(new String[]{}), new MobIMCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						Toast.makeText(getApplicationContext(), R.string.dosucess, Toast.LENGTH_SHORT).show();
						setResult(Activity.RESULT_OK);
						onBackPressed();
					}

					@Override
					public void onError(int code, String message) {
						Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				});
			} else if(controlType == FOR_SHOW_ALL_MEMBERS) {

			}

//			String[] members = list == null ? null : list.toArray(new String[]{});
//			MobIM.getGroupManager().createGroup(name, profile, members, new MobIMCallback<IMGroup>() {
//				public void onSuccess(IMGroup group) {
//					Toast.makeText(getApplicationContext(), R.string.dosucess, Toast.LENGTH_SHORT).show();
//					setResult(Activity.RESULT_OK);
//					onBackPressed();
//				}
//
//				public void onError(int code, String message) {
//					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//				}
//			});
		} else {
			onBackPressed();
		}
	}
}
