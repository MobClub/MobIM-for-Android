package com.mob.demo.mobim.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.mob.demo.mobim.utils.Utils;

import java.util.List;

public class EmojiGridAdapter extends BaseAdapter {

	private Context context;
	private List<Emojicon> emojicons;

	public EmojiGridAdapter(Context context, List<Emojicon> emojicons) {
		this.context = context;
		this.emojicons = emojicons;
	}

	/**
	 * How many items are in the data set represented by this Adapter.
	 *
	 * @return Count of items.
	 */
	@Override
	public int getCount() {
		if (emojicons != null) {
			return emojicons.size();
		}
		return 0;
	}

	/**
	 * Get the data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's
	 *                 data set.
	 * @return The data at the specified position.
	 */
	@Override
	public Object getItem(int position) {
		if (emojicons != null) {
			return emojicons.get(position);
		}
		return null;
	}

	/**
	 * Get the row id associated with the specified position in the list.
	 *
	 * @param position The position of the item within the adapter's data set whose row id we want.
	 * @return The id of the item at the specified position.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Get a View that displays the data at the specified position in the data set. You can either
	 * create a View manually or inflate it from an XML layout file. When the View is inflated, the
	 * parent View (GridView, ListView...) will apply default layout parameters unless you use
	 * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 *
	 * @param positionThe position of the item within the adapter's data set of the item whose view
	 *                    we want.
	 * @param convertView The old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible to convert
	 *                    this view to display the correct data, this method can create a new view.
	 *                    Heterogeneous lists can specify their number of view types, so that this View is
	 *                    always of the right type (see {@link #getViewTypeCount()} and
	 *                    {@link #getItemViewType(int)}).
	 * @param parent      The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		Utils.showLog("EmojiGridAdapter", " position >> " + position);
		if (convertView == null) {
			// if it's not recycled, initialize some attributes
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(90, 90));
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setPadding(8, 8, 8, 8);
			//imageView.setBackgroundColor(Color.RED);
		} else {
			imageView = (ImageView) convertView;
		}
		Emojicon emojicon = emojicons.get(position);
		imageView.setImageResource(emojicon.getIcon());
		Utils.showLog("EmojiGridAdapter", " position Icon >> " + emojicon.getIcon() + " EmojiText >> " + emojicon.getEmojiText());
		imageView.setTag(emojicon.getEmojiText());
		return imageView;
	}
}
