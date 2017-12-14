package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.DialogYesOrNo;
import com.mob.demo.mobim.component.IMultiItemSupport;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMUser;

import java.util.ArrayList;
import java.util.List;

public class FragmentContacts extends BaseFragment {
	private static final int TYPE_SEARCH = 1;
	private static final int TYPE_GROUP = 2;
	private static final int TYPE_REMINDER = 3;
	private static final int TYPE_BAR = 4;
	private static final int TYPE_USER = 5;

	private ListView listView;
	private QuickAdapter<SparseArray<Object>> adapter;
	private IMultiItemSupport itemSupport;

	protected boolean useLoadingView() {
		//使用loadingView
		return true;
	}

	protected void reload() {
		loadData();
	}

	public View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contacts, container, false);
	}

	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (adapter == null) {
					return;
				}
				SparseArray<Object> item = adapter.getItem(position);
				if (item == null) {
					return;
				}
				int itemViewType = itemSupport.getItemViewType(item);
				switch (itemViewType) {
					case TYPE_SEARCH: {
						startActivityForResult(new Intent(getContext(), UserSearchActivity.class), 1001);
					} break;
					case TYPE_GROUP: {
						startActivity(new Intent(getContext(), GroupListActivity.class));
					} break;
					case TYPE_REMINDER: {
						startActivity(new Intent(getContext(), ReminderListActivity.class));
					} break;
					case TYPE_USER: {
						UserDetailsActivity.gotoUserDetailsPageForResult(FragmentContacts.this, (IMUser) item.get(TYPE_USER), 1002);
					} break;
				}
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (adapter == null) {
					return false;
				}
				final SparseArray<Object> item = adapter.getItem(position);
				if (item == null) {
					return false;
				}
				int itemViewType = itemSupport.getItemViewType(item);
				if (itemViewType != TYPE_USER) {
					return false;
				}
				final IMUser user = (IMUser) item.get(TYPE_USER);
				DialogYesOrNo yesOrNo = new DialogYesOrNo(getActivity(), getResources().getString(R.string.tip_delete_friend),
						R.string.cancel, R.string.txt_ok, new DialogYesOrNo.OnConfirmClickListener() {
					public void onConfirm() {
						UserManager.removeFriend(user.getId(), new MobIMCallback<Boolean>() {
							public void onSuccess(Boolean result) {
								if (result) {
									adapter.removeData(item);
								}
							}

							public void onError(int code, String message) {
								Utils.showErrorToast(code);
							}
						});
					}
				});
				yesOrNo.show();
				return true;
			}
		});
		loadData();
	}

	private void loadData() {
		showLoadingView();
		final List<SparseArray<Object>> list = new ArrayList<SparseArray<Object>>();
		SparseArray<Object> itemSearch = new SparseArray<Object>();
		itemSearch.put(TYPE_SEARCH, new Object());
		list.add(itemSearch);

		SparseArray<Object> itemGroup = new SparseArray<Object>();
		itemGroup.put(TYPE_GROUP, getString(R.string.txt_group));
		list.add(itemGroup);

		SparseArray<Object> itemReminder = new SparseArray<Object>();
		itemReminder.put(TYPE_REMINDER, getString(R.string.txt_reminder));
		list.add(itemReminder);

		SparseArray<Object> itemBar = new SparseArray<Object>();
		itemBar.put(TYPE_BAR, new Object());
		list.add(itemBar);

		UserManager.getFriends(new MobIMCallback<List<IMUser>>() {
			public void onSuccess(List<IMUser> userList) {
				if (userList != null && userList.size() > 0) {
					SparseArray<Object> item;
					for (IMUser user : userList) {
						item = new SparseArray<Object>();
						item.put(TYPE_USER, user);
						list.add(item);
					}
				}
				showContentView();
				setAdapter(list);
			}

			public void onError(int code, String message) {
				showContentView();
				setAdapter(list);
			}
		});
	}

	private void setAdapter(List<SparseArray<Object>> list) {
		if (adapter == null) {
			itemSupport = new IMultiItemSupport<SparseArray<Object>>() {
				public int getViewTypeCount() {
					return 5;
				}

				public int getItemViewType(SparseArray<Object> item) {
					Object object = item.get(TYPE_SEARCH);
					if (object != null) {
						return TYPE_SEARCH;
					}
					object = item.get(TYPE_GROUP);
					if (object != null) {
						return TYPE_GROUP;
					}
					object = item.get(TYPE_REMINDER);
					if (object != null) {
						return TYPE_REMINDER;
					}
					object = item.get(TYPE_BAR);
					if (object != null) {
						return TYPE_BAR;
					}
					return TYPE_USER;
				}

				public int getLayoutId(int itemViewType) {
					switch (itemViewType) {
						case TYPE_SEARCH: return R.layout.list_contacts_item_search;
						case TYPE_BAR: return R.layout.list_contacts_item_bar;
						case TYPE_GROUP:
						case TYPE_REMINDER:
						default: return R.layout.list_contacts_item;
					}
				}
			};
			adapter = new QuickAdapter<SparseArray<Object>>(getContext(), list, itemSupport) {
				protected void initViews(ViewHolder viewHolder, int position, SparseArray<Object> item) {
					int itemViewType = itemSupport.getItemViewType(item);
					ImageView ivIcon;
					TextView tvName;
					switch (itemViewType) {
						case TYPE_GROUP: {
							ivIcon = viewHolder.getView(R.id.ivIcon);
							LoadImageUtils.showAvatar(getContext(), ivIcon, null, R.drawable.ic_group);
							tvName = viewHolder.getView(R.id.tvName);
							tvName.setText(String.valueOf(item.get(itemViewType)));
						} break;
						case TYPE_REMINDER: {
							ivIcon = viewHolder.getView(R.id.ivIcon);
							LoadImageUtils.showAvatar(getContext(), ivIcon, null, R.drawable.ic_default_reminder);
							tvName = viewHolder.getView(R.id.tvName);
							tvName.setText(String.valueOf(item.get(itemViewType)));
						} break;
						case TYPE_USER: {
							IMUser user = (IMUser) item.get(TYPE_USER);
							ivIcon = viewHolder.getView(R.id.ivIcon);
							LoadImageUtils.showAvatar(getContext(), ivIcon, user.getAvatar(), R.drawable.ic_default_user);
							tvName = viewHolder.getView(R.id.tvName);
							tvName.setText(user.getNickname());
						} break;
					}
				}
			};
			listView.setAdapter(adapter);
		} else {
			adapter.refreshData(list, false);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1001 || requestCode == 1002) {
				loadData();
			}
		}
	}
}
