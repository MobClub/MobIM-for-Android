package com.mob.demo.mobim.component;

import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

public class ViewHolder {
	private View convertView;
	private SparseArray<View> views;

	public ViewHolder(View convertView) {
		this.convertView = convertView;
		views = new SparseArray<View>();
	}

	public <T extends View> T getView(int viewId) {
		View view = views.get(viewId);
		if (view == null) {
			view = convertView.findViewById(viewId);
			views.put(viewId, view);
		}
		return (T) view;
	}

	public void setText(int resId, String text) {
		((TextView) getView(resId)).setText(text);
	}

}
