package com.mob.demo.mobim.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMUser;

import java.util.ArrayList;
import java.util.List;

public class FragmentBlackList extends BaseFragment {
	private QuickAdapter<IMUser> adapter;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_black_list, container, false);
	}

	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		final View pbLoading = view.findViewById(R.id.pbLoading);
		final ListView listView = (ListView) view.findViewById(R.id.listView);

		view.findViewById(R.id.ivTitleLeft).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		tvTitle.setText(R.string.txt_black_list);

		final View.OnClickListener onRemoveItemClickListener = new View.OnClickListener() {
			public void onClick(View v) {
				int position = (Integer) v.getTag();
				final IMUser item = adapter.getItem(position);
				if (item == null) {
					return;
				}
				//移除黑名单
				MobIM.getUserManager().removeFromBlacklist(item.getId(), new MobIMCallback<Void>() {
					public void onSuccess(Void result) {
						adapter.removeData(item);
						Toast.makeText(getContext().getApplicationContext(), R.string.toast_remove_success, Toast.LENGTH_SHORT).show();
					}

					public void onError(int code, String message) {
						Utils.showErrorToast(code);
					}
				});
			}
		};


		pbLoading.setVisibility(View.VISIBLE);
		MobIM.getUserManager().getBlackList(new MobIMCallback<List<IMUser>>() {
			public void onSuccess(List<IMUser> list) {
				if (list == null || list.size() < 1) {
					pbLoading.setVisibility(View.GONE);
					return;
				}
				UserManager.getFullUserInfo(list, new MobIMCallback<ArrayList<IMUser>>() {
					public void onSuccess(ArrayList<IMUser> imUsers) {
						pbLoading.setVisibility(View.GONE);
						if (imUsers == null || imUsers.size() < 1) {
							return;
						}
						adapter = new QuickAdapter<IMUser>(getContext(), R.layout.list_black_list_item, imUsers) {
							protected void initViews(ViewHolder viewHolder, int position, IMUser item) {
								ImageView ivIcon = viewHolder.getView(R.id.ivIcon);
								TextView tvName = viewHolder.getView(R.id.tvName);
								View btnRemove = viewHolder.getView(R.id.btnRemove);
								btnRemove.setTag(position);
								btnRemove.setOnClickListener(onRemoveItemClickListener);
								if (item != null) {
									LoadImageUtils.showAvatar(getContext(), ivIcon, item.getAvatar(), R.drawable.ic_default_user);
									tvName.setText(item.getNickname());
								}
							}
						};

						listView.setAdapter(adapter);
					}

					public void onError(int code, String message) {
						pbLoading.setVisibility(View.GONE);
					}
				});
			}

			public void onError(int code, String message) {
				Utils.showErrorToast(code);
				pbLoading.setVisibility(View.GONE);
			}
		});
	}

}
