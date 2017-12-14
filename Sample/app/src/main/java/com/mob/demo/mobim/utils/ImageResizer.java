package com.mob.demo.mobim.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.FileDescriptor;

public class ImageResizer extends ImageWorker {
	private static final String TAG = "ImageResizer";
	protected int imageWidth;
	protected int imageHeight;

	/**
	 * Initialize providing a single target image size (used for both width and
	 * height);
	 *
	 * @param context
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ImageResizer(Context context, int imageWidth, int imageHeight) {
		super(context);
		setImageSize(imageWidth, imageHeight);
	}

	/**
	 * Initialize providing a single target image size (used for both width and
	 * height);
	 *
	 * @param context
	 * @param imageSize
	 */
	public ImageResizer(Context context, int imageSize) {
		super(context);
		setImageSize(imageSize);
	}

	/**
	 * Set the target image width and height.
	 *
	 * @param width
	 * @param height
	 */
	public void setImageSize(int width, int height) {
		imageWidth = width;
		imageHeight = height;
	}

	/**
	 * Set the target image size (width and height will be the same).
	 *
	 * @param size
	 */
	public void setImageSize(int size) {
		setImageSize(size, size);
	}

	/**
	 * The main processing method. This happens in a background task. In this
	 * case we are just sampling down the bitmap and returning it from a
	 * resource.
	 *
	 * @param resId
	 * @return
	 */
	private Bitmap processBitmap(int resId) {
		return decodeSampledBitmapFromResource(resources, resId, imageWidth,
				imageHeight, getImageCache());
	}

	protected Bitmap processBitmap(Object data) {
		String filePath = String.valueOf(data);
		String lowercase = filePath.toLowerCase();
		if ( lowercase.endsWith(".png") || lowercase.endsWith(".jpeg") || lowercase.endsWith(".jpg") || lowercase.endsWith(".bmp") ) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
			int width = options.outWidth;
			int height = options.outHeight;
			int max = Math.max(width, height);
			if ( max > 512 ) {
				float scale = 512f / max;
//				int w = Math.round(scale * width);
//				int h = Math.round(scale * height);
//				bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
				options.inJustDecodeBounds = false;
				options.inSampleSize = (int) scale;
				bitmap = BitmapFactory.decodeFile(filePath, options);
			}
			return bitmap;
		}
		return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
	}

	/**
	 * Decode and sample down a bitmap from resources to the requested width and
	 * height.
	 *
	 * @param res       The resources object containing the image data
	 * @param resId     The resource id of the image data
	 * @param reqWidth  The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @param cache     The ImageCache used to find candidate bitmaps for use with
	 *                  inBitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 * ratio and dimensions that are equal to or greater than the
	 * requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
														int resId, int reqWidth, int reqHeight, ImageCache cache) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// BEGIN_INCLUDE (read_bitmap_dimensions)
		// First decode with inJustDecodeBounds=true to check dimensions
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// END_INCLUDE (read_bitmap_dimensions)

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decode and sample down a bitmap from a file to the requested width and
	 * height.
	 *
	 * @param filename  The full path of the file to decode
	 * @param reqWidth  The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @param cache     The ImageCache used to find candidate bitmaps for use with
	 *                  inBitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 * ratio and dimensions that are equal to or greater than the
	 * requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filename,
													int reqWidth, int reqHeight, ImageCache cache) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// First decode with inJustDecodeBounds=true to check dimensions
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	/**
	 * Decode and sample down a bitmap from a file input stream to the requested
	 * width and height.
	 *
	 * @param fileDescriptor The file descriptor to read from
	 * @param reqWidth       The requested width of the resulting bitmap
	 * @param reqHeight      The requested height of the resulting bitmap
	 * @param cache          The ImageCache used to find candidate bitmaps for use with
	 *                       inBitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 * ratio and dimensions that are equal to or greater than the
	 * requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight,
				ImageCache cache) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		return BitmapFactory
				.decodeFileDescriptor(fileDescriptor, null, options);
	}

	private static void addInBitmapOptions(BitmapFactory.Options options,ImageCache cache) {
		options.inMutable = true;
		// BEGIN_INCLUDE(add_bitmap_options)
		// inBitmap only works with mutable bitmaps so force the decoder to
		// return mutable bitmaps.

		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
		// END_INCLUDE(add_bitmap_options)
	}

	/**
	 * Calculate an inSampleSize for use in a
	 * {@link android.graphics.BitmapFactory.Options} object when decoding
	 * bitmaps using the decode* methods from
	 * {@link android.graphics.BitmapFactory}. This implementation calculates
	 * the closest inSampleSize that is a power of 2 and will result in the
	 * final decoded bitmap having a width and height equal to or larger than
	 * the requested width and height.
	 *
	 * @param options   An options object with out* params already populated (run
	 *                  through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth  The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
											int reqWidth, int reqHeight) {
		// BEGIN_INCLUDE (calculate_sample_size)
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			long totalPixels = width * height / inSampleSize;

			// Anything more than 2x the requested pixels we'll sample down
			// further
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize *= 2;
				totalPixels /= 2;
			}
		}
		return inSampleSize;
		// END_INCLUDE (calculate_sample_size)
	}

}
