package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mob.MobSDK;
import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMUser;

import java.util.ArrayList;
import java.util.List;

public class FragmentUpdateUserAvatar extends BaseFragment {
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_update_user_avatar, container, false);
	}

	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		GridView gridView = (GridView) view.findViewById(R.id.gridView);

		view.findViewById(R.id.ivTitleLeft).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		tvTitle.setText(R.string.title_update_user_avatar);

		//加载数据
		final String currentAvatar = "";
		//模拟的已经上传过的用户头像
		List<String> list = new ArrayList<String>();
		list.add("http://download.sdk.mob.com/e72/83d/e247e8b45bd557f70ac6dcc0cb.png");
		list.add("http://download.sdk.mob.com/7b6/264/2c4a9fef9ffa03e5deb5973ab9.png");
		list.add("http://download.sdk.mob.com/bbd/480/d993f23339944e4de27e4b0a12.png");
		list.add("http://download.sdk.mob.com/3a6/b11/ba6a81f2c13fb0ba3b96d99619.png");
		list.add("http://download.sdk.mob.com/a0b/7d0/0520d3554a69ad50a3b87d1760.png");
		list.add("http://download.sdk.mob.com/510/deb/0c0731ac543eb71311c482a2e2.png");
		list.add("http://download.sdk.mob.com/7d7/e2b/91d898dfde6fb787ab3d926f9d.png");
		list.add("http://download.sdk.mob.com/29f/06f/e6a941cd02e3f29465cd438d16.png");
		list.add("http://download.sdk.mob.com/167/bc4/38197ca7950aec7020d516fbb2.png");
		list.add("http://download.sdk.mob.com/f57/a5e/72ecd0c6ca96361c7f3bcd7144.png");
		list.add("http://download.sdk.mob.com/e31/c6e/315fdfa6abc4b17d8c139605de.png");
		list.add("http://download.sdk.mob.com/cc3/00e/dedc8bf1514d6c6a5e456fba74.png");
		list.add("http://download.sdk.mob.com/f22/154/e27eaf3fc3e24047bd5d4ec3a8.png");
		list.add("http://download.sdk.mob.com/d33/6f9/c15ee2d2f01aba51d33985e6c5.png");
		list.add("http://download.sdk.mob.com/cc6/115/2628761069dd35867eda68fe2a.png");
		list.add("http://download.sdk.mob.com/047/a51/38cfad789e9808443d11f2f9be.png");

		final QuickAdapter<String> adapter = new QuickAdapter<String>(getContext(), R.layout.update_user_avatar_item, list) {
			protected void initViews(ViewHolder viewHolder, int position, String item) {
				ImageView ivAvatar = viewHolder.getView(R.id.ivAvatar);
				LoadImageUtils.showAvatar(getContext(), ivAvatar, item, R.drawable.ic_group);
			}
		};
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String avatar = adapter.getItem(position);
				if (TextUtils.isEmpty(avatar)) {
					return;
				}
				if (currentAvatar.equals(avatar)) {
					return;
				}
				final IMUser imUser = UserManager.getUser();
				if (imUser == null) {
					return;
				}
				//更新用户头像
				UserManager.updateUserInfo(imUser.getId(), null, avatar, new MobIMCallback<Boolean>() {
					public void onSuccess(Boolean aBoolean) {
						MobSDK.setUser(imUser.getId(), imUser.getNickname(), avatar, null);//更新IM用户信息
						Intent data = new Intent();
						data.putExtra("link", avatar);
						getActivity().setResult(Activity.RESULT_OK, data);
						getActivity().onBackPressed();
					}

					public void onError(int code, String message) {
						getActivity().onBackPressed();
					}
				});
			}
		});
		gridView.setAdapter(adapter);
	}
}
