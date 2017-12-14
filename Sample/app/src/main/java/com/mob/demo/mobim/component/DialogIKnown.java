package com.mob.demo.mobim.component;

import android.app.Dialog;
import android.content.Context;
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

public class DialogIKnown extends Dialog {
	private OnConfirmClickListener listener;

	public DialogIKnown(@NonNull Context context, String message, int knownResId, OnConfirmClickListener listener) {
		super(context, R.style.DialogStyle);
		this.listener = listener;
		init(context, message, knownResId);
	}

	private void init(Context context, String message, int knownResId) {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_i_known, null);
		setContentView(view);

		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		TextView tvOK = (TextView) view.findViewById(R.id.tvOK);

		tvContent.setText(message);
		if (knownResId > 0) {
			tvOK.setText(knownResId);
		}

		tvOK.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
				if (listener != null) {
					listener.onConfirm();
				}
			}
		});
	}

	public void show() {
		Window window = getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.width = MobSDK.getContext().getResources().getDisplayMetrics().widthPixels - ResHelper.dipToPx(MobSDK.getContext(), 50);
		super.show();
	}

	public interface OnConfirmClickListener {
		void onConfirm();
	}
}
