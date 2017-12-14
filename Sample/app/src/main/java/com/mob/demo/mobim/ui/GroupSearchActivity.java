package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMGroup;

public class GroupSearchActivity extends BaseActivity {
	private InputMethodManager inputMethodManager;
	private String groupId = null;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_search);

		final EditText etContent = (EditText) findViewById(R.id.etContent);
		etContent.setHint(R.string.hint_input_group_id);
		final View rlResult = findViewById(R.id.rlResult);
		final TextView tvEmpty = (TextView) findViewById(R.id.tvEmpty);
		rlResult.setVisibility(View.GONE);

		final ImageView ivIcon = (ImageView) findViewById(R.id.ivIcon);
		final TextView tvName = (TextView) findViewById(R.id.tvName);
		final Button btnJoin = (Button) findViewById(R.id.btnJoin);
		btnJoin.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				if (TextUtils.isEmpty(groupId)) {
					return;
				}
				MobIM.getGroupManager().joinGroup(groupId, new MobIMCallback<IMGroup>() {
					public void onSuccess(IMGroup group) {
						v.setEnabled(false);
						btnJoin.setText(R.string.hint_join_ed);
						setResult(Activity.RESULT_OK);
					}

					public void onError(int code, String message) {
						Utils.showErrorToast(code);
					}
				});
			}
		});

		findViewById(R.id.tvTitleRight).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});

		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		etContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					btnJoin.setEnabled(true);
					btnJoin.setText(R.string.txt_join);
					groupId = etContent.getText().toString().trim();
					if (TextUtils.isEmpty(groupId)) {
						return false;
					}
					inputMethodManager.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
					MobIM.getGroupManager().findGroup(groupId, new MobIMCallback<IMGroup>() {
						public void onSuccess(IMGroup group) {
							if (group == null) {
								rlResult.setVisibility(View.GONE);
								tvEmpty.setVisibility(View.VISIBLE);//没找到时
								tvEmpty.setText(R.string.tip_group_id_not_found);
								return;
							}
							LoadImageUtils.showAvatar(GroupSearchActivity.this, ivIcon, null, R.drawable.ic_group);
							tvName.setText(group.getName() + "(" + group.getMemberSize() + ")");
							rlResult.setVisibility(View.VISIBLE);
							tvEmpty.setVisibility(View.GONE);
						}

						public void onError(int code, String message) {
							rlResult.setVisibility(View.GONE);
							tvEmpty.setVisibility(View.VISIBLE);//没找到时
							tvEmpty.setText(R.string.tip_group_id_not_found);
						}
					});
				}
				return false;
			}
		});
	}
}
