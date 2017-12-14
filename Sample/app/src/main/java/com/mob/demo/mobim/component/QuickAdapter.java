package com.mob.demo.mobim.component;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.mob.demo.mobim.utils.Utils;
import com.mob.tools.utils.ResHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class QuickAdapter<T> extends BaseAdapter {
	private Context context;
	private int layoutResId;
	private List<T> dataList;
	private IMultiItemSupport iMultiItemSupport;
	private boolean showLoadingView = false;
	private boolean isLoading = false;

	public QuickAdapter(Context context, int layoutResId) {
		this(context, layoutResId, null);
	}

	public QuickAdapter(Context context, int layoutResId, List<T> dataList) {
		this(context, layoutResId, dataList, null);
	}

	public QuickAdapter(Context context, List<T> dataList, IMultiItemSupport<T> adapter) {
		this(context, 0, dataList, adapter);
	}

	public QuickAdapter(Context context, int layoutResId, List<T> dataList, IMultiItemSupport<T> adapter) {
		this.context = context;
		this.layoutResId = layoutResId;
		this.dataList = dataList;
		if (this.dataList == null) {
			this.dataList = new ArrayList<T>();
		}
		iMultiItemSupport = adapter;
	}

	public void refreshData(List<T> dataList) {
		refreshData(dataList, false);
	}

	public void refreshData(List<T> dataList, boolean hasMore) {
		this.dataList.clear();
		if (dataList != null) {
			this.dataList.addAll(dataList);
		}
		showLoadingView = hasMore;
		notifyDataSetChanged();
		isLoading = false;
	}

	public void addData(List<T> dataList, boolean hasMore) {
		showLoadingView = hasMore;
		if (dataList != null) {
			this.dataList.addAll(dataList);
		}
		notifyDataSetChanged();
		isLoading = false;

	}


	public void removeItem(int position) {
		if (position < dataList.size()) {
			removeData(dataList.get(position));
		}
	}

	public void removeData(T t) {
		if (containData(t)) {
			dataList.remove(t);
			notifyDataSetChanged();
		}
	}

	public void removeAllData() {
		if (dataList != null) {
			dataList.clear();
			notifyDataSetChanged();
		}
	}

	public void setData(int position, T t) {
		if (position < dataList.size()) {
			dataList.set(position, t);
			notifyDataSetChanged();
		}
	}

	public boolean containData(T t) {
		return dataList.contains(t);
	}

	public List<T> getData() {
		return dataList;
	}

	public boolean isLoading() {
		return isLoading;
	}

	@Override
	public int getCount() {
		return dataList.size() + (showLoadingView ? 1 : 0);
	}

	@Override
	public T getItem(int position) {
		if (position >= dataList.size()) {
			return null;
		}
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return iMultiItemSupport == null ? 2 : (iMultiItemSupport.getViewTypeCount() + 1);
	}

	@Override
	public int getItemViewType(int position) {
		if (iMultiItemSupport == null) {
			return position >= dataList.size() ? 0 : 1;
		}
		return position >= dataList.size() ? 0 : iMultiItemSupport.getItemViewType(getItem(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == 0) {
			return createLoadingView(convertView);
		}
		if (iMultiItemSupport != null) {
			return createContentView(convertView, position, iMultiItemSupport.getLayoutId(iMultiItemSupport
					.getItemViewType(getItem(position))));
		}
		return createContentView(convertView, position, layoutResId);
	}

	protected abstract void initViews(ViewHolder viewHolder, int position, T item);

	private View createContentView(View convertView, int position, int layoutResId) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(layoutResId, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		try {
			initViews(viewHolder, position, dataList.get(position));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return convertView;
	}

	//如果有更多数据,则加载更多
	protected void requestMoreData() {

	}

	public boolean isLoadingMore(){
		return showLoadingView;
	}

	private View createLoadingView(View convertView) {
		if (convertView == null) {
			convertView = createLoadingView(context);
		}
		if (!isLoading) {
			isLoading = true;
			requestMoreData();
		}
		return convertView;
	}

	//最底部的loadingView, 可以override定义自己的loadingView
	protected View createLoadingView(Context context) {
		FrameLayout container = new FrameLayout(context);
		container.setForegroundGravity(Gravity.CENTER);
		ProgressBar progress = new ProgressBar(context);
		FrameLayout.LayoutParams lp =new FrameLayout.LayoutParams(ResHelper.dipToPx(context,20), ResHelper.dipToPx(context,20));
		lp.gravity = Gravity.CENTER;
		lp.topMargin = ResHelper.dipToPx(context,10);
		lp.bottomMargin = ResHelper.dipToPx(context,10);
		progress.setLayoutParams(lp);
		container.addView(progress);
		return container;
	}

	public void fresh(String id, String path) {

	}
}
