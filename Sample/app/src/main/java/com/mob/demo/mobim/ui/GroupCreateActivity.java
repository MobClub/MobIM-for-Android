package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;

public class GroupCreateActivity extends BaseActivity implements View.OnClickListener {
	private InputMethodManager inputMethodManager;
	private EditText etName;
	private EditText etProfile;
	private String defUid;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		setContentView(R.layout.activity_group_create);
		defUid = getIntent().getStringExtra("defuid");
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		etName = (EditText) findViewById(R.id.etName);
		etProfile = (EditText) findViewById(R.id.etProfile);

		tvTitle.setText(R.string.txt_create_group);
		findViewById(R.id.ivTitleLeft).setOnClickListener(this);
		findViewById(R.id.btnInvite).setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btnInvite) {
			String groupName = etName.getText().toString().trim();
			inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
			String profile = etProfile.getText().toString().trim();
			if (groupName.length() > 10) {
				//群名称最多10
				groupName = groupName.substring(0, 10);
			}
			if (profile.length() > 140) {
				//简介最多140
				profile = profile.substring(0, 140);
			}

			Intent intent = new Intent(GroupCreateActivity.this, GroupAddUserActivity.class);
			if (!TextUtils.isEmpty(groupName)) {
				intent.putExtra("name", groupName);
			}
			intent.putExtra("profile", profile);
			if (defUid != null) {
				intent.putExtra("defuid", defUid);
			}

			startActivityForResult(intent, 1001);
		} else {
			inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
			onBackPressed();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
			setResult(RESULT_OK);
			onBackPressed();
		}
	}
}
