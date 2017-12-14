package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
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
import com.mob.imsdk.model.IMConversation;
import com.mob.imsdk.model.IMMessage;
import com.mob.imsdk.model.IMReminder;

import java.text.SimpleDateFormat;
import java.util.List;

public class ReminderDetailsActivity extends BaseActivity {
	private final static int PAGE_SIZE = 10;
	private ListView listView;
	private View pbLoading;
	private IMReminder imReminder;
	private QuickAdapter<IMMessage> adapter;
	private int page = 1;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder_details);
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(R.string.txt_notification_prompt);
		findViewById(R.id.ivTitleLeft).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
		if (getIntent() == null) {
			return;
		}
		listView = (ListView) findViewById(R.id.listView);
		pbLoading = findViewById(R.id.pbLoading);

		imReminder = (IMReminder) getIntent().getSerializableExtra("item");
		//获取提醒号内容
		if (imReminder == null) {
			return;
		}

		pbLoading.setVisibility(View.VISIBLE);
		MobIM.getChatManager().markConversationAllMessageAsRead(imReminder.getId(), IMConversation.TYPE_REMINDER);
		loadData(1);
	}

	private void loadData(int page) {
		this.page = page;
		MobIM.getChatManager().getMessageList(imReminder.getId(), IMConversation.TYPE_REMINDER, PAGE_SIZE, page, new MobIMCallback<List<IMMessage>>() {
			public void onSuccess(List<IMMessage> list) {
				pbLoading.setVisibility(View.GONE);
				if (list == null || list.isEmpty()) {
					return;
				}
				setAdapter(list);
			}

			public void onError(int code, String message) {
				pbLoading.setVisibility(View.GONE);
				Utils.showErrorToast(code);
			}
		});
	}

	private void setAdapter(List<IMMessage> list) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.txt_time_format_m_d));
		if (adapter == null) {
			adapter = new QuickAdapter<IMMessage>(ReminderDetailsActivity.this, R.layout.list_reminder_details) {
				protected void initViews(ViewHolder viewHolder, int position, IMMessage item) {
					ImageView ivAvatar = viewHolder.getView(R.id.ivAvatar);
					TextView tvName = viewHolder.getView(R.id.tvName);
					TextView tvTime = viewHolder.getView(R.id.tvTime);
					TextView tvBody = viewHolder.getView(R.id.tvBody);

					LoadImageUtils.showAvatar(getApplicationContext(), ivAvatar, imReminder.getAvatar(), R.drawable.ic_default_reminder);
					tvName.setText(imReminder.getName());
					if (item != null) {
						tvTime.setText(dateFormat.format(item.getCreateTime()));
						tvBody.setText(item.getBody());
					}
				}

				protected void requestMoreData() {
					page++;
					loadData(page);
				}
			};

			listView.setAdapter(adapter);
		}
		adapter.addData(list, list.size() >= PAGE_SIZE);
	}

	public static void gotoReminderDetailsPageForResult(Activity activity, IMReminder reminder, int requestCode) {
		Intent intent = new Intent(activity, ReminderDetailsActivity.class);
		intent.putExtra("item", reminder);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void gotoReminderDetailsPageForResult(Fragment fragment, IMReminder reminder, int requestCode) {
		Intent intent = new Intent(fragment.getContext(), ReminderDetailsActivity.class);
		intent.putExtra("item", reminder);
		fragment.startActivityForResult(intent, requestCode);
	}
}
