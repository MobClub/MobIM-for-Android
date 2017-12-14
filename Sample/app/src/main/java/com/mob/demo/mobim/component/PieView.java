package com.mob.demo.mobim.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mob.demo.mobim.R;
import com.mob.demo.mobim.component.annotation.ColorInt;


public class PieView extends View {
	private RelativeLayout baseLayout;
	private TextView percentageTextView = null;
	private int percentageSize;
	private int innerCirclePadding;
	private Paint percentageFill;
	private Paint backgroundFill;
	private Paint centerFill;
	private RectF rect;
	private RectF rectCent;
	private float percentage = 0;
	private float maxPercentage = 100;
	private float angle = 0;

	public PieView(Context context) {
		super(context);

		percentage = 0;
		angle = 0;

		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		baseLayout = new RelativeLayout(context);
		baseLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		percentageTextView = new TextView(context);
		int roundedPercentage = (int) (percentage * this.maxPercentage);
		percentageTextView.setText(Integer.toString(roundedPercentage) + "%");
		baseLayout.addView(percentageTextView);
		percentageSize = 50;
		percentageTextView.setTextSize(percentageSize);

		init();
	}

	public PieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupWidgetWithParams(context, attrs, 0);
		init();
	}

	public PieView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupWidgetWithParams(context, attrs, defStyle);
		init();
	}

	private void setupWidgetWithParams(Context context, AttributeSet attrs, int defStyle) {
		percentageTextView = new TextView(context);
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.PieView,
				0, 0);
		try {
			this.percentage = a.getFloat(R.styleable.PieView_percentage, 0) / this.maxPercentage;
			this.angle = (360 * this.percentage);
			this.percentageSize = a.getInteger(R.styleable.PieView_percentage_size, 0);
			this.innerCirclePadding = a.getInteger(R.styleable.PieView_inner_pie_padding, 0);
			this.percentageTextView.setText(a.getString(R.styleable.PieView_inner_text));
			this.percentageTextView.setVisibility(a.getBoolean(R.styleable.PieView_inner_text_visibility, true) ? View.VISIBLE : View.INVISIBLE);
		} finally {
			a.recycle();
		}
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		baseLayout = new RelativeLayout(context);
		baseLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		if (percentageTextView.getText().toString().trim().equals("")) {
			int roundedPercentage = (int) (percentage * this.maxPercentage);
			percentageTextView.setText(Integer.toString(roundedPercentage) + "%");
		}
		percentageTextView.setTextSize(percentageSize);
		baseLayout.addView(percentageTextView);
	}

	private void init() {
		percentageTextView.setTextColor(getContext().getResources().getColor(R.color.percentageTextColor));

		percentageFill = new Paint();
		percentageFill.setColor(getContext().getResources().getColor(R.color.percentageFillColor));
		percentageFill.setAntiAlias(true);
		percentageFill.setStyle(Paint.Style.FILL);
		backgroundFill = new Paint();
		backgroundFill.setColor(getContext().getResources().getColor(R.color.percentageUnfilledColor));
		backgroundFill.setAntiAlias(true);
		backgroundFill.setStyle(Paint.Style.FILL);

		centerFill = new Paint();
		centerFill.setColor(getContext().getResources().getColor(R.color.percentageTextBackground));
		centerFill.setAntiAlias(true);
		centerFill.setStyle(Paint.Style.FILL);

		rect = new RectF();
		rectCent = new RectF();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int left = 0;
		int width = getWidth();
		int height = getHeight();
		int top = 0;

		rect.set(left, top, left + width, top + width);
		rectCent.set(left + innerCirclePadding, top + innerCirclePadding, (left - innerCirclePadding) + width, (top - innerCirclePadding) + width);

		canvas.drawArc(rect, -90, 360, true, backgroundFill);

		if (percentage != 0) {
			canvas.drawArc(rect, -90, this.angle, true, percentageFill);
			canvas.drawArc(rectCent, -90, 360, true, centerFill);
		}

		percentageTextView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		percentageTextView.layout(left, top, left + width, top + height);
		percentageTextView.setGravity(Gravity.CENTER);

		baseLayout.draw(canvas);
	}

	public float getPieAngle() {
		return this.angle;
	}

	public void setPieAngle(float arcAngle) {
		this.angle = arcAngle;
	}

	/**
	 * Toggle the visibility of the inner label of the widget
	 *
	 * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
	 */
	public void setInnerTextVisibility(int visibility) {
		this.percentageTextView.setVisibility(visibility);
		invalidate();
	}

	/**
	 * Set the text of the inner label of the widget
	 *
	 * @param text any valid String value
	 */
	public void setInnerText(String text) {
		this.percentageTextView.setText(text);
		invalidate();
	}

	/**
	 * Determine the thickness of the mPercentage pie bar
	 *
	 * @param padding value ranging from 1 to the width of the widget
	 */
	public void setPieInnerPadding(int padding) {
		this.innerCirclePadding = padding;
		invalidate();
	}

	/**
	 * Get the thickness of the mPercentage pie bar
	 */
	public int getPieInnerPadding() {
		return this.innerCirclePadding;
	}

	/**
	 * Get the percentage
	 */
	public float getPercentage() {
		return percentage * this.maxPercentage;
	}

	/**
	 * Set a percentage between 0 and maxPercentage
	 *
	 * @param percentage any float value from 0 to maxPercentage
	 */
	public void setPercentage(float percentage) {
		this.percentage = percentage / this.maxPercentage;
		int roundedPercentage = (int) percentage;
		this.percentageTextView.setText(Integer.toString(roundedPercentage) + "%");
		this.angle = (360 * percentage);
		invalidate();
	}

	/**
	 * Set the size of the inner text of the widget
	 *
	 * @param size any valid float
	 */
	public void setPercentageTextSize(float size) {
		this.percentageTextView.setTextSize(size);
		invalidate();
	}

	/**
	 * Determine the background color of the center of the widget where the label is shown
	 *
	 * @param color The new color (including alpha) to set in the paint.
	 */
	public void setInnerBackgroundColor(@ColorInt int color) {
		centerFill.setColor(color);
	}

	/**
	 * Determine the background color of the bar representing the mPercentage set to the widget
	 *
	 * @param color The new color (including alpha) to set in the paint.
	 */
	public void setPercentageBackgroundColor(@ColorInt int color) {
		percentageFill.setColor(color);
	}

	/**
	 * Determine the background color of the back of the widget
	 *
	 * @param color The new color (including alpha) to set in the paint.
	 */
	public void setMainBackgroundColor(@ColorInt int color) {
		backgroundFill.setColor(color);
	}

	/**
	 * Determine the color of the text
	 *
	 * @param color The new color (including alpha) to set in the paint.
	 */
	public void setTextColor(@ColorInt int color) {
		percentageTextView.setTextColor(color);
	}

	/**
	 * Set the max value (default = 100)
	 *
	 * @param maxPercentage max value.
	 */
	public void setMaxPercentage(float maxPercentage) {
		this.maxPercentage = maxPercentage;
	}
}
