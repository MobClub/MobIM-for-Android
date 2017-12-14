package com.mob.demo.mobim.component;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mob.MobSDK;
import com.mob.demo.mobim.R;
import com.mob.tools.utils.ResHelper;

public class DialogYesOrNo extends Dialog implements View.OnClickListener {
	private OnConfirmClickListener listener;
	private OnCancelClickListener cancelClickListener;
	private boolean isConfirmClick = false;

	public DialogYesOrNo(@NonNull Context context, String message, int noResId, int yesResId, OnConfirmClickListener listener) {
		this(context, message, noResId, yesResId, listener, null);
	}

	public DialogYesOrNo(@NonNull Context context, String message, int noResId, int yesResId, OnConfirmClickListener listener,
				OnCancelClickListener cancelClickListener) {
		super(context, R.style.DialogStyle);
		this.listener = listener;
		this.cancelClickListener = cancelClickListener;
		init(context, message, noResId, yesResId);
	}

	private void init(Context context, String message, int noResId, int yesResId) {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_yes_or_no, null);
		setContentView(view);

		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		TextView tvCancel = (TextView) view.findViewById(R.id.tvCancel);
		TextView tvOK = (TextView) view.findViewById(R.id.tvOK);

		tvContent.setText(message);
		if (noResId > 0) {
			tvCancel.setText(noResId);
		}
		if (yesResId > 0) {
			tvOK.setText(yesResId);
		}

		tvCancel.setOnClickListener(this);
		tvOK.setOnClickListener(this);

		setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if (isConfirmClick) {
					if (listener != null) {
						listener.onConfirm();
					}
				} else if (cancelClickListener != null) {
					cancelClickListener.onCancel();
				}
			}
		});
	}

	public void show() {
		isConfirmClick = false;
		Window window = getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.width = MobSDK.getContext().getResources().getDisplayMetrics().widthPixels - ResHelper.dipToPx(MobSDK.getContext(), 50);
		super.show();
	}

	public void onClick(View v) {
		if (v.getId() == R.id.tvOK) {
			isConfirmClick = true;
		}
		dismiss();
	}

	public interface OnConfirmClickListener {
		void onConfirm();
	}

	public interface OnCancelClickListener {
		void onCancel();
	}
}
