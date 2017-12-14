package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMReminder;

import java.util.List;

public class ReminderListActivity extends BaseActivity {
	private QuickAdapter<IMReminder> adapter;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_black_list);
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		final View pbLoading = findViewById(R.id.pbLoading);
		final ListView listView = (ListView) findViewById(R.id.listView);

		findViewById(R.id.ivTitleLeft).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});

		tvTitle.setText(R.string.txt_reminder);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IMReminder item = adapter.getItem(position);
				if (item == null) {
					return;
				}
				//清除未读消息标记
				item.setUnreadMsgCount(0);
				adapter.notifyDataSetChanged();
				ReminderDetailsActivity.gotoReminderDetailsPageForResult(ReminderListActivity.this, item, 1001);
			}
		});

		pbLoading.setVisibility(View.VISIBLE);
		MobIM.getChatManager().getReminderList(new MobIMCallback<List<IMReminder>>() {
			public void onSuccess(List<IMReminder> list) {
				adapter = new QuickAdapter<IMReminder>(ReminderListActivity.this, R.layout.list_reminder_list_item, list) {
					protected void initViews(ViewHolder viewHolder, int position, IMReminder item) {
						TextView tvName = viewHolder.getView(R.id.tvName);
						TextView tvDot = viewHolder.getView(R.id.tvDot);
						ImageView ivAvatar = viewHolder.getView(R.id.ivAvatar);
						tvDot.setVisibility(View.GONE);
						if (item != null) {
							tvName.setText(item.getName());
							if (item.getUnreadMsgCount() > 0) {
								tvDot.setEnabled(!item.isDisturb());
								tvDot.setVisibility(View.VISIBLE);
								tvDot.setText(String.valueOf(item.getUnreadMsgCount()));
							}
							LoadImageUtils.showAvatar(ReminderListActivity.this, ivAvatar, item.getAvatar(), R.drawable.ic_default_reminder);
						} else {
							tvName.setText("");
							LoadImageUtils.showAvatar(ReminderListActivity.this, ivAvatar, null, R.drawable.ic_default_reminder);
						}
					}
				};

				listView.setAdapter(adapter);
				pbLoading.setVisibility(View.GONE);
			}

			public void onError(int code, String message) {
				Utils.showErrorToast(code);
				pbLoading.setVisibility(View.GONE);
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
	}
}
