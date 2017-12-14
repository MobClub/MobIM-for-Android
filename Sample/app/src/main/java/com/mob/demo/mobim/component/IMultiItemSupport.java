package com.mob.demo.mobim.component;

public interface IMultiItemSupport<T> {
	int getViewTypeCount();

	/***
	 * @return 返回itemViewType, 从1开始
	 */
	int getItemViewType(T t);

	int getLayoutId(int itemViewType);
}
