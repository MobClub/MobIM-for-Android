package com.mob.demo.mobim.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mob.demo.mobim.GlideRoundTransform;
import com.mob.tools.utils.ResHelper;

import java.io.File;

public class LoadImageUtils {
	private static LruCache<String, Bitmap> bitmapLruCache = null;

	/**
	 * 实现圆形ImageView
	 */
	public static void showAvatar(Context context, ImageView imageView, String imgUrl, int defaultResId) {
		ViewGroup.LayoutParams lp = imageView.getLayoutParams();
		Glide.with(context).load(imgUrl)
				.error(defaultResId)
				.placeholder(defaultResId)
				.crossFade()
				.dontAnimate()
				.transform(new CenterCrop(context), new GlideRoundTransform(context, ResHelper.dipToPx(context, lp.width / 2)))
				.into(imageView);
	}

	public static void loadImageView(Context context, ImageView imageView, String path, int defResId) {
		ViewGroup.LayoutParams lp = imageView.getLayoutParams();
		Glide.with(context).load(new File(path))
				.placeholder(defResId)
				.crossFade()
				.dontAnimate()
				.transform(new FitCenter(context), new GlideRoundTransform(context, ResHelper.dipToPx(context, lp.width / 3)))
				.into(imageView);
	}

	public static void loadImageViewTo(Context context, ImageView imageView, String path, int defResId) {
		//ViewGroup.LayoutParams lp = imageView.getLayoutParams();
		Glide.with(context).load(new File(path))
				.placeholder(defResId)
				.crossFade()
				.dontAnimate()
				.into(imageView);
	}

	/**
	 * 自适应宽度加载图片。保持图片的长宽比例不变，通过修改imageView的高度来完全显示图片。
	 */
	public static void loadIntoUseFitWidth(Context context, final String imageUrl, int errorImageId, final ImageView imageView) {
		Glide.with(context)
				.load(imageUrl)
				.diskCacheStrategy(DiskCacheStrategy.SOURCE)
				.listener(new RequestListener<String, GlideDrawable>() {
					@Override
					public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
						return false;
					}

					@Override
					public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
												boolean isFromMemoryCache, boolean isFirstResource) {
						if (imageView == null) {
							return false;
						}
						if (imageView.getScaleType() != ImageView.ScaleType.FIT_XY) {
							imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						}
						ViewGroup.LayoutParams params = imageView.getLayoutParams();
						int vw = imageView.getHeight() - imageView.getPaddingBottom() - imageView.getPaddingTop();
						if (vw < resource.getIntrinsicHeight()) {
							float scale = (float) vw / (float) resource.getIntrinsicHeight();
							int vh = Math.round(resource.getIntrinsicWidth() * scale);
							params.width = vh + imageView.getPaddingLeft() + imageView.getPaddingRight();
						} else {
							//params.width = resource.getIntrinsicWidth();
						}
						imageView.setLayoutParams(params);
						return false;
					}
				})
				.placeholder(errorImageId)
				.error(errorImageId)
				.into(imageView);
	}

	/**
	 * 实现圆角ImageView
	 *
	 * @param radius 指定圆角半径弧度
	 */
	public static void showAvatar(Context context, ImageView imageView, String imgUrl, String defaultResId, int radius) {
		Glide.with(context).load(imgUrl)
				.error(ResHelper.getBitmapRes(context, defaultResId))
				.placeholder(ResHelper.getBitmapRes(context, defaultResId))
				.crossFade()
				.dontAnimate()
				.transform(new CenterCrop(context), new GlideRoundTransform(context, ResHelper.dipToPx(context, radius)))
				.into(imageView);
	}

	/**
	 * 对一个Resources的资源文件进行指定长宽来加载进内存, 并把这个bitmap对象返回
	 *
	 * @param reqHeight 最终想要得到bitmap的高度
	 * @return 返回采样之后的bitmap对象
	 */
	public static Bitmap decodeFixedSizeForResource(String localPath, int reqHeight) {
		// 首先先指定加载的模式 为只是获取资源文件的大小
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(localPath, options);
		//Calculate Size  计算要设置的采样率 并把值设置到option上
		options.inSampleSize = calculateInSampleSize(options, reqHeight);
		// 关闭只加载属性模式, 并重新加载的时候传入自定义的options对象
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(localPath, options);
	}

	/**
	 * 一个计算工具类的方法, 传入图片的属性对象和 想要实现的目标大小. 通过计算得到采样值
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqHeight) {
		//Raw height and width of image
		//原始图片的宽高属性
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		// 如果想要实现的宽高比原始图片的宽高小那么就可以计算出采样率, 否则不需要改变采样率
		if (reqHeight < width) {
			int halfWidth = width / 2;
			int halfHeight = height / 2;
			// 判断原始长宽的一半是否比目标大小小, 如果小那么增大采样率2倍, 直到出现修改后原始值会比目标值大的时候
			while ((halfHeight / inSampleSize) >= reqHeight) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap getBitmapByLocalPath(Context context, String localPath, int reqHeight) {
		if (bitmapLruCache == null) {
			bitmapLruCache = new LruCache<String, Bitmap>(getMemoryCacheSize(context));
		}
		Bitmap bitmap = bitmapLruCache.get(localPath);
		if (bitmap == null) {
			bitmap = decodeFixedSizeForResource(localPath, reqHeight);
			bitmapLruCache.put(localPath, bitmap);
			return bitmap;
		}
		return bitmap;
	}

	/**
	 * @param context
	 * @return 得到需要分配的缓存大小，这里用八分之一的大小来做
	 * @description
	 */
	private static int getMemoryCacheSize(Context context) {
		// Get memory class of this device, exceeding this amount will throw an
		// OutOfMemory exception.
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		// Use 1/8th of the available memory for this memory cache.
		return 1024 * 1024 * memClass / 8;
	}

	public static Bitmap getVideoThumbnailByLocalPath(Context context, String videoPath, int width, int height, int kind) {
		if (bitmapLruCache == null) {
			bitmapLruCache = new LruCache<String, Bitmap>(getMemoryCacheSize(context));
		}
		Bitmap bitmap = bitmapLruCache.get(videoPath);
		if (bitmap == null) {
			bitmap = Utils.getVideoThumbnail(videoPath, width, height, kind);
			bitmapLruCache.put(videoPath, bitmap);
			return bitmap;
		}
		return bitmap;
	}

}
