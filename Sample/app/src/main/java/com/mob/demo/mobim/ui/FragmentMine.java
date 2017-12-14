package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.imsdk.model.IMUser;

public class FragmentMine extends BaseFragment implements View.OnClickListener {
	private ImageView ivAvatar;
	private TextView tvUserName;
	private TextView tvUserId;

	protected boolean useLoadingView() {
		//使用loadingView
		return true;
	}

	protected void reload() {
		loadData();
	}

	public View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mine, container, false);
	}

	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ivAvatar = (ImageView) view.findViewById(R.id.ivAvatar);
		tvUserName = (TextView) view.findViewById(R.id.tvUserName);
		tvUserId = (TextView) view.findViewById(R.id.tvUserId);

		view.findViewById(R.id.llAvatar).setOnClickListener(this);
		view.findViewById(R.id.llUserName).setOnClickListener(this);
		view.findViewById(R.id.llBlackList).setOnClickListener(this);
		view.findViewById(R.id.llUserId).setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				//复制内容到剪切板
				ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setPrimaryClip(ClipData.newPlainText("id", tvUserId.getText()));
				Toast.makeText(getContext().getApplicationContext(), R.string.toast_id_copied, Toast.LENGTH_LONG).show();
				return true;
			}
		});

		loadData();
	}

	void loadData() {
		IMUser user = UserManager.getUser();
		if (user == null) {
			showLoadingFailed();
			Toast.makeText(getContext(), R.string.toast_get_user_failed, Toast.LENGTH_SHORT).show();
		} else {
			LoadImageUtils.showAvatar(getContext(), ivAvatar, user.getAvatar(), R.drawable.ic_group);
			tvUserName.setText(user.getNickname());
			tvUserId.setText(user.getId());
			showContentView();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1001 && data != null) {
				String link = data.getStringExtra("link");
				LoadImageUtils.showAvatar(getContext(), ivAvatar, link, R.drawable.ic_group);
			} else if (requestCode == 1002 && data != null) {
				String name = data.getStringExtra("name");
				tvUserName.setText(name);
			}
		}
	}

	public void onClick(View v) {
		int vId = v.getId();
		switch (vId) {
			case R.id.llAvatar: {
				UpdateUserInfoActivity.gotoUpdateUserAvatarPage(this, 1001);
			} break;
			case R.id.llUserName: {
				UpdateUserInfoActivity.gotoUpdateUserInfoPage(this, 1002);
			} break;
			case R.id.llBlackList: {
				UpdateUserInfoActivity.gotoBlackListPage(getContext());
			} break;
		}
	}
}
