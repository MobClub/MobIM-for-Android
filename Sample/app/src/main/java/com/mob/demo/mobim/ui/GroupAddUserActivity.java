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
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMGroup;
import com.mob.imsdk.model.IMUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GroupAddUserActivity extends BaseActivity implements View.OnClickListener {
	private Button btnConfirm;
	private QuickAdapter<IMUser> adapter;
	private HashSet<IMUser> selectedUser = new HashSet<IMUser>();
	private String defUid;


	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_add_user);
		defUid = getIntent().getStringExtra("defuid");
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);

		tvTitle.setText(R.string.txt_select_contacts);
		findViewById(R.id.ivTitleLeft).setOnClickListener(this);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(this);
		btnConfirm.setText(getString(R.string.txt_confirm_create_group, "0"));

		final ListView listView = (ListView) findViewById(R.id.listView);
		final TextView tvEmpty = (TextView) findViewById(R.id.tvEmpty);

		final View.OnClickListener onSelectClickListener = new View.OnClickListener() {
			public void onClick(View v) {
				int position = (Integer) v.getTag();
				IMUser item = adapter.getItem(position);
				if (v.isSelected()) {
					v.setSelected(false);
					selectedUser.remove(item);
				} else {
					v.setSelected(true);
					selectedUser.add(item);
				}
				btnConfirm.setText(getString(R.string.txt_confirm_create_group, String.valueOf(selectedUser.size())));
			}
		};

		UserManager.getFriends(new MobIMCallback<List<IMUser>>() {
			public void onSuccess(List<IMUser> imUsers) {
				if (imUsers == null || imUsers.isEmpty()) {
					listView.setVisibility(View.GONE);
					tvEmpty.setVisibility(View.VISIBLE);
					return;
				}
				if(defUid != null) {
					int size = imUsers.size();
					for (int i = 0;i < size; i++){
						if(imUsers.get(i).getId().equals(defUid)) {
							imUsers.remove(i);
							break;
						}
					}
				}

				tvEmpty.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				adapter = new QuickAdapter<IMUser>(GroupAddUserActivity.this, R.layout.list_group_add_user_item, imUsers) {
					protected void initViews(ViewHolder viewHolder, int position, IMUser item) {
						ImageView ivSelect = viewHolder.getView(R.id.ivSelect);
						ImageView ivIcon = viewHolder.getView(R.id.ivIcon);
						TextView tvName = viewHolder.getView(R.id.tvName);
						tvName.setText(item.getNickname());
						LoadImageUtils.showAvatar(GroupAddUserActivity.this, ivIcon, item.getAvatar(), R.drawable.ic_default_user);

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

	}

	public void onClick(View v) {
		if (v == btnConfirm) {
			Intent intent = getIntent();
			if (intent == null) {
				return;
			}
			if (selectedUser.isEmpty()) {
				Toast.makeText(getApplicationContext(), R.string.tip_group_members_null, Toast.LENGTH_SHORT).show();
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
			if(defUid != null) {
				list.add(defUid);
			}
			String[] members = list == null ? null : list.toArray(new String[]{});
			MobIM.getGroupManager().createGroup(name, profile, members, new MobIMCallback<IMGroup>() {
				public void onSuccess(IMGroup group) {
					Toast.makeText(getApplicationContext(), R.string.tip_group_create_success, Toast.LENGTH_SHORT).show();
					setResult(Activity.RESULT_OK);
					ChatActivity.gotoGroupChatPage(GroupAddUserActivity.this, group);
					finish();
				}

				public void onError(int code, String message) {
					Utils.showErrorToast(code);
				}
			});
		} else {
			onBackPressed();
		}
	}
}
