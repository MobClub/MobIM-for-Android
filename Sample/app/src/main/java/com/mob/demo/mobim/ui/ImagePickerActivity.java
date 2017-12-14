package com.mob.demo.mobim.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.model.RecyclingImageView;
import com.mob.demo.mobim.model.VideoEntity;
import com.mob.demo.mobim.utils.ImageCache;
import com.mob.demo.mobim.utils.ImageResizer;
import com.mob.demo.mobim.utils.LoadImageUtils;
import com.mob.demo.mobim.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerActivity extends BaseActivity {
	private static final String TAG = "ImagePickerActivity";
	private int imageThumbSize;
	private int imageThumbSpacing;
	private ImageAdapter adapter;
	private ImageResizer imageResizer;
	List<VideoEntity> list;
	private boolean isShowPhote;
	public final static String SHOWTYPE = "show_type";
	public final static String VIDEO = "vidoe";
	public final static String IMAGE = "image";
	private List<Integer> selects;
	private Button btnInfor;

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pickpic);
		String showtype = getIntent().getStringExtra("show_type");
		imageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		imageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);
		list = new ArrayList<VideoEntity>();
		selects = new ArrayList<Integer>();
		adapter = new ImageAdapter(this);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams();

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
		// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		imageResizer = new ImageResizer(this, imageThumbSize);
		imageResizer.setLoadingImage(R.drawable.empty_photo);
		imageResizer.addImageCache(getSupportFragmentManager(),
				cacheParams);

		if (showtype != null && showtype.equals("video")) {
			isShowPhote = false;
			getVideoFile();
		} else {
			isShowPhote = true;
			getPhotoFile();
//			getVideoFile();
		}
		final GridView gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				imageResizer.setPauseWork(true);
				VideoEntity vEntty = list.get(position);
				Intent intent = ImagePickerActivity.this.getIntent().putExtra("path", vEntty.filePath).putExtra("dur", vEntty.duration);
			}
		});
		gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView absListView,int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// Before Honeycomb pause image loading on scroll to help
					// with performance
					if (!Utils.hasHoneycomb()) {
						imageResizer.setPauseWork(true);
					}
				} else {
					imageResizer.setPauseWork(false);
				}
			}

			public void onScroll(AbsListView absListView, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {

			}
		});

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		gridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@TargetApi(16)
					@Override
					public void onGlobalLayout() {
						final int numColumns = (int) Math.floor(gridView
								.getWidth()
								/ (imageThumbSize + imageThumbSpacing));
						if (numColumns > 0) {
							final int columnWidth = (gridView.getWidth() / numColumns)
									- imageThumbSpacing;
							adapter.setItemHeight(columnWidth);
//							if (BuildConfig.DEBUG) {
//								Log.d(TAG,
//										"onCreateView - numColumns set to "
//												+ numColumns);
//							}
							if (Utils.hasJellyBean()) {
								gridView.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							} else {
								gridView.getViewTreeObserver()
										.removeGlobalOnLayoutListener(this);
							}
						}
					}
				});
		btnInfor = (Button) findViewById(R.id.btnInfor);
		btnInfor.setOnClickListener(new View.OnClickListener() {

			/**
			 * Called when a view has been clicked.
			 *
			 * @param v The view that was clicked.
			 */
			public void onClick(View v) {
				ArrayList<String> images = new ArrayList<String>();
				for (int i = 0; i < selects.size(); i++) {
					images.add(list.get(selects.get(i)).filePath);
				}
				Intent intent = ImagePickerActivity.this.getIntent().putStringArrayListExtra("paths", images);

				ImagePickerActivity.this.setResult(Activity.RESULT_OK, intent);
				ImagePickerActivity.this.finish();

			}
		});
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ImagePickerActivity.this.setResult(Activity.RESULT_CANCELED);
				ImagePickerActivity.this.finish();
			}
		});
		freshSum();
	}

	private void freshSum() {
		String s = getResources().getString(R.string.selects);
		btnInfor.setText(String.format(s, "" + selects.size()));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (imageResizer != null) {
			imageResizer.setExitTasksEarly(false);
		}
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (imageResizer != null) {
			imageResizer.closeCache();
		}
		if (imageResizer != null) {
			imageResizer.clearCache();
		}

	}

	private class ImageAdapter extends BaseAdapter {

		private final Context context;
		private int itemHeight = 0;
		private RelativeLayout.LayoutParams imageViewLayoutParams;

		public ImageAdapter(Context context) {
			super();
			this.context = context;
			imageViewLayoutParams = new RelativeLayout.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
		public View getView(final int position,View convertView,ViewGroup container) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.choose_griditem, container, false);
				holder.imageView = (RecyclingImageView) convertView.findViewById(R.id.imageView);
				holder.icon = (ImageView) convertView.findViewById(R.id.video_icon);

				holder.tvDur = (TextView) convertView.findViewById(R.id.chatting_length_iv);
				holder.ckbSelect = (CheckBox) convertView.findViewById(R.id.ckbSelect);
				holder.tvSize = (TextView) convertView.findViewById(R.id.chatting_size_iv);
				holder.videodataarea = convertView.findViewById(R.id.video_data_area);

				holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.imageView.setLayoutParams(imageViewLayoutParams);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Check the height matches our calculated column width
			if (holder.imageView.getLayoutParams().height != itemHeight) {
				holder.imageView.setLayoutParams(imageViewLayoutParams);
			}

			final ViewHolder nowHolder = holder;
			holder.videodataarea.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					nowHolder.ckbSelect.setChecked(!nowHolder.ckbSelect.isChecked());
				}
			});

			holder.ckbSelect.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

				/**
				 * Called when the checked state of a compound button has changed.
				 *
				 * @param buttonView The compound button view whose state has changed.
				 * @param isChecked  The new checked state of buttonView.
				 */
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked && selects.size() >= 9 && !selects.contains(Integer.valueOf(position))) {
						buttonView.setChecked(false);
						Toast.makeText(getApplicationContext(), R.string.selectmore9, Toast.LENGTH_SHORT).show();

					} else {
						if (isChecked) {
							if (!selects.contains(Integer.valueOf(position))) {
								selects.add(Integer.valueOf(position));
								if (selects.size() >= 9) {
									notifyDataSetChanged();
								}
							}

						} else {
							if (selects.contains(Integer.valueOf(position))) {
								selects.remove(Integer.valueOf(position));
								if (selects.size() == 8) {
									notifyDataSetChanged();
								}
							}
						}
						freshSum();
					}
				}
			});
			VideoEntity entty = null;
			if (position >= list.size()) {
				entty = list.get(position - 1);
			} else {
				entty = list.get(position);
			}
			if (!isShowPhote) {
				holder.tvDur.setVisibility(View.VISIBLE);
				holder.tvDur.setText(Utils.toTime(entty.duration));
				holder.tvSize.setText(Utils.getDataSize(entty.size));
			} else {
				holder.icon.setVisibility(View.VISIBLE);
			}

			holder.imageView.setImageResource(R.drawable.empty_photo);

			if (selects.contains(Integer.valueOf(position))) {
				if (!holder.ckbSelect.isChecked()) {
					holder.ckbSelect.setChecked(true);
				}

				if (selects.size() >= 9) {
					if (Build.VERSION.SDK_INT >= 16) {
						int alpha = holder.imageView.getImageAlpha();
						if (alpha != 255) {
							holder.imageView.setImageAlpha(255);
						}
					} else {
						float alpha = holder.imageView.getAlpha();
						if (alpha != 1.0f) {
							holder.imageView.setAlpha(1.0f);
						}
					}
				}
			} else {
				if (holder.ckbSelect.isChecked()) {
					holder.ckbSelect.setChecked(false);
				}

				if (selects.size() >= 9) {
					if (Build.VERSION.SDK_INT >= 16) {
						int alpha = holder.imageView.getImageAlpha();
						if (alpha != 126) {
							holder.imageView.setImageAlpha(106);
						}
					} else {
						float alpha = holder.imageView.getAlpha();
						if (alpha != 05f) {
							holder.imageView.setAlpha(0.4f);
						}

					}
				} else {
					if (Build.VERSION.SDK_INT >= 16) {
						int alpha = holder.imageView.getImageAlpha();
						if (alpha != 255) {
							holder.imageView.setImageAlpha(255);
						}
					} else {
						float alpha = holder.imageView.getAlpha();
						if (alpha != 1.0f) {
							holder.imageView.setAlpha(1.0f);
						}
					}
				}
			}
			if (isShowPhote) {
				LoadImageUtils.loadImageView(getApplicationContext(), holder.imageView, entty.filePath, R.drawable.empty_photo);
			} else {
				imageResizer.loadImage(entty.filePath, holder.imageView);
			}
			//	Log.e(TAG, position +" : position entty.filePath >> "+entty.filePath);
			// END_INCLUDE(load_gridview_item)
			return convertView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 *
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == itemHeight) {
				return;
			}
			itemHeight = height;
			imageViewLayoutParams = new RelativeLayout.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT, itemHeight);
			imageResizer.setImageSize(height);
			notifyDataSetChanged();
		}

		class ViewHolder {
			CheckBox ckbSelect;
			RecyclingImageView imageView;
			ImageView icon;
			TextView tvDur;
			TextView tvSize;
			View videodataarea;
		}
	}

	private void getVideoFile() {
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.moveToFirst()) {
			do {

				// ID:MediaStore.Audio.Media._ID
				int id = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media._ID));

				// title：MediaStore.Audio.Media.TITLE
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				// path：MediaStore.Audio.Media.DATA
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

				// duration：MediaStore.Audio.Media.DURATION
				int duration = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

				// 大小：MediaStore.Audio.Media.SIZE
				int size = (int) cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

				VideoEntity entty = new VideoEntity();
				entty.id = id;
				entty.title = title;
				entty.filePath = url;
				entty.duration = duration;
				entty.size = size;
				list.add(entty);
			} while (cursor.moveToNext());

		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

	private void getPhotoFile() {
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media._ID + " desc ");

		if (cursor != null && cursor.moveToFirst()) {
			do {

				// ID:MediaStore.Audio.Media._ID
				int id = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

				// title：MediaStore.Audio.Media.TITLE
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
				// path：MediaStore.Audio.Media.DATA
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

				// duration：MediaStore.Audio.Media.DURATION
//				int duration = cursor
//						.getInt(cursor
//								.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));
				// 大小：MediaStore.Audio.Media.SIZE
				int size = (int) cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

				VideoEntity entty = new VideoEntity();
				entty.id = id;
				entty.title = title;
				entty.filePath = url;
//				entty.duration = duration;
				entty.size = size;
				list.add(entty);
			} while (cursor.moveToNext());

		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

}
