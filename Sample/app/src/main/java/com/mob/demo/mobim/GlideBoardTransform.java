package com.mob.demo.mobim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class GlideBoardTransform extends BitmapTransformation {
	private float radius = 0f;
	private Paint borderPaint;
	private float borderWidth;

	public GlideBoardTransform(Context context, int radius) {
		super(context);
		this.radius = radius;
	}

	public GlideBoardTransform(Context context, int radius, int borderWidth, int borderColor) {
		super(context);
		this.radius = radius;
		this.borderWidth = Resources.getSystem().getDisplayMetrics().density * borderWidth;
		borderPaint = new Paint();
		borderPaint.setDither(true);
		borderPaint.setAntiAlias(true);
		borderPaint.setColor(borderColor);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(this.borderWidth);
	}

	protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
		return roundCrop(pool, toTransform);
	}

	private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
		if (source == null) {
			return null;
		}
		int size = (int) (Math.min(source.getWidth(), source.getHeight()) - (borderWidth / 2));
		int x = (source.getWidth() - size) / 2;
		int y = (source.getHeight() - size) / 2;
		// TODO this could be acquired from the pool too
		Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
		Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
		if (result == null) {
			result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
		paint.setAntiAlias(true);
		float r = size / 2f;
		canvas.drawCircle(r, r, r, paint);
		if (borderPaint != null) {
			float borderRadius = r - borderWidth / 2;
			canvas.drawCircle(r, r, borderRadius, borderPaint);
		}
		return result;
	}

	@Override
	public String getId() {
		return getClass().getName() + Math.round(radius);
	}
}
