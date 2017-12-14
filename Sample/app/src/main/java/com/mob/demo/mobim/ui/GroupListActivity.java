package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.component.IMultiItemSupport;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.model.IMGroup;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends BaseActivity {
	private static final int TYPE_SEARCH = 1;
	private static final int TYPE_CREATE = 2;
	private static final int TYPE_BAR = 3;
	private static final int TYPE_GROUP = 4;

	private ListView listView;
	private View pbLoading;
	private QuickAdapter<SparseArray<Object>> adapter;
	private IMultiItemSupport itemSupport;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_list);
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		pbLoading = findViewById(R.id.pbLoading);
		listView = (ListView) findViewById(R.id.listView);

		findViewById(R.id.ivTitleLeft).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});

		tvTitle.setText(R.string.txt_group);
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
						startActivityForResult(new Intent(GroupListActivity.this, GroupSearchActivity.class), 1001);
					} break;
					case TYPE_CREATE: {
						startActivityForResult(new Intent(GroupListActivity.this, GroupCreateActivity.class), 1002);
					} break;
					case TYPE_GROUP: {
						ChatActivity.gotoGroupChatPage(GroupListActivity.this, (IMGroup) item.get(TYPE_GROUP));
					} break;
				}
			}
		});

		setAdapter(getHeaderDataList());
		loadData();
	}

	private List<SparseArray<Object>> getHeaderDataList() {
		List<SparseArray<Object>> list = new ArrayList<SparseArray<Object>>();
		SparseArray<Object> itemSearch = new SparseArray<Object>();
		itemSearch.put(TYPE_SEARCH, new Object());
		list.add(itemSearch);

		SparseArray<Object> itemGroup = new SparseArray<Object>();
		itemGroup.put(TYPE_CREATE, new Object());
		list.add(itemGroup);

		SparseArray<Object> itemBar = new SparseArray<Object>();
		itemBar.put(TYPE_BAR, new Object());
		list.add(itemBar);
		return list;
	}

	private void loadData() {
		pbLoading.setVisibility(View.VISIBLE);
		MobIM.getGroupManager().getGroupList(new MobIMCallback<List<IMGroup>>() {
			public void onSuccess(List<IMGroup> groupList) {
				if (groupList != null && groupList.size() > 0) {
					List<SparseArray<Object>> list = getHeaderDataList();
					SparseArray<Object> item;
					for (IMGroup group : groupList) {
						item = new SparseArray<Object>();
						item.put(TYPE_GROUP, group);
						list.add(item);
					}
					setAdapter(list);
				}
				pbLoading.setVisibility(View.GONE);
			}

			public void onError(int code, String message) {
				pbLoading.setVisibility(View.GONE);
				Utils.showErrorToast(code);
			}
		});
	}

	private void setAdapter(List<SparseArray<Object>> list) {
		if (adapter == null) {
			itemSupport = new IMultiItemSupport<SparseArray<Object>>() {
				public int getViewTypeCount() {
					return 4;
				}

				public int getItemViewType(SparseArray<Object> item) {
					Object object = item.get(TYPE_SEARCH);
					if (object != null) {
						return TYPE_SEARCH;
					}
					object = item.get(TYPE_CREATE);
					if (object != null) {
						return TYPE_CREATE;
					}
					object = item.get(TYPE_BAR);
					if (object != null) {
						return TYPE_BAR;
					}
					return TYPE_GROUP;
				}

				public int getLayoutId(int itemViewType) {
					switch (itemViewType) {
						case TYPE_SEARCH: return R.layout.list_contacts_item_search;
						case TYPE_BAR: return R.layout.list_contacts_item_bar;
						case TYPE_CREATE:
						case TYPE_GROUP:
						default: return R.layout.list_contacts_item;
					}
				}
			};
			adapter = new QuickAdapter<SparseArray<Object>>(GroupListActivity.this, list, itemSupport) {
				protected void initViews(ViewHolder viewHolder, int position, SparseArray<Object> item) {
					int itemViewType = itemSupport.getItemViewType(item);
					TextView tvName;
					ImageView ivIcon;
					switch (itemViewType) {
						case TYPE_SEARCH: {
							TextView tvInput = viewHolder.getView(R.id.tvInput);
							tvInput.setText(R.string.hint_input_group_id_to_add_group);
						} break;
						case TYPE_BAR: {
							TextView tvBar = viewHolder.getView(R.id.tvBar);
							tvBar.setText(R.string.txt_my_group);
						} break;
						case TYPE_CREATE: {
							tvName = viewHolder.getView(R.id.tvName);
							ivIcon = viewHolder.getView(R.id.ivIcon);
							tvName.setText(R.string.txt_create_group);
							LoadImageUtils.showAvatar(GroupListActivity.this, ivIcon, null, R.drawable.ic_add);
						} break;
						case TYPE_GROUP: {
							ivIcon = viewHolder.getView(R.id.ivIcon);
							tvName = viewHolder.getView(R.id.tvName);
							IMGroup imGroup = (IMGroup) item.get(itemViewType);
							if (imGroup != null) {
								tvName.setText(imGroup.getName() + "(" + imGroup.getMemberSize() + ")");
								LoadImageUtils.showAvatar(GroupListActivity.this, ivIcon, null, R.drawable.ic_group);
							}
						} break;
					}
				}
			};
			listView.setAdapter(adapter);
		} else {
			adapter.refreshData(list, false);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1001 || requestCode == 1002) {
				//加群成功\创建群成功 刷新界面
				loadData();
			}
		}
	}
}
