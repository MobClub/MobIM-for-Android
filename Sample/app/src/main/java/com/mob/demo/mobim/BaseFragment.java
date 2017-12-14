package com.mob.demo.mobim;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mob.imsdk.MobIMErrorCode;
import com.mob.tools.utils.ResHelper;

public class BaseFragment extends Fragment {
	private TextView tvLoading;
	private TextView tvConnectStatus;
	private ProgressBar pbLoading;
	private Button btnReLoad;

	private ViewGroup rlContainer;
	private View contentView;
	private View loadingView;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		rlContainer = new RelativeLayout(getContext());
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		rlContainer.setLayoutParams(rlp);
		if (useLoadingView()) {
			contentView = onCreateContentView(inflater, container, savedInstanceState);
			if (contentView != null) {
				rlContainer.addView(contentView, rlp);
				contentView.setVisibility(View.GONE);
			}
			loadingView = getLoadingView(inflater, container);
			if (loadingView != null) {
				rlContainer.addView(loadingView, rlp);
				loadingView.setVisibility(View.VISIBLE);
			}
			tvConnectStatus = getIMDisconnectView();
			if (tvConnectStatus != null) {
				rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ResHelper.dipToPx(getContext(), 40));
				rlContainer.addView(tvConnectStatus, rlp);
				tvConnectStatus.setVisibility(View.GONE);
			}
		}
		return rlContainer;
	}

	public View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return null;
	}

	protected boolean useLoadingView() {
		return false;
	}

	protected TextView getIMDisconnectView() {
		TextView textView = new TextView(getContext());
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResHelper.dipToPx(getContext(), 14));
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(0xFF666666);
		textView.setBackgroundColor(0xC7ED85A8);
		return textView;
	}

	/**
	 * 设置im连接状态，0连接成功，1连接中，其他连接错误
	 */
	protected void setIMConnectStatus(int status) {
		if (tvConnectStatus != null) {
			tvConnectStatus.setVisibility(View.VISIBLE);
			if (status == 0) {
				tvConnectStatus.setText(R.string.tip_im_connect_success);
				new Handler().postDelayed(new Runnable() {
					public void run() {
						tvConnectStatus.setVisibility(View.GONE);
					}
				}, 2000);
			} else if (status == 1) {
				tvConnectStatus.setText(R.string.tip_im_connect_ing);
			} else if (status == MobIMErrorCode.CONNECT_ERROR_ACCOUNT_EXCEPTION) {
				tvConnectStatus.setText(R.string.tip_im_connect_failed);
			} else if (status == MobIMErrorCode.CONNECT_ERROR_ACCOUNT_LOGIN_ANOTHER_DEVICE) {
				tvConnectStatus.setText(R.string.tip_im_connect_interrupt_account_error);
			} else if (status == MobIMErrorCode.CONNECT_ERROR_INTERRUPT) {
				tvConnectStatus.setText(R.string.tip_im_connect_interrupt_net_error);
			}
		}
	}

	protected View getLoadingView(LayoutInflater inflater, @Nullable ViewGroup container) {
		View loadView = inflater.inflate(R.layout.layout_loading, container, false);
		tvLoading = (TextView) loadView.findViewById(R.id.tvLoading);
		pbLoading = (ProgressBar) loadView.findViewById(R.id.pbLoading);
		btnReLoad = (Button) loadView.findViewById(R.id.btnReLoad);
		btnReLoad.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showLoadingView();
				reload();
			}
		});
		return loadView;
	}

	protected void showLoadingView() {
		if (loadingView != null) {
			loadingView.setVisibility(View.VISIBLE);
		}
		if (contentView != null) {
			contentView.setVisibility(View.GONE);
		}
		if (btnReLoad != null) {
			btnReLoad.setVisibility(View.GONE);
		}
		if (pbLoading != null) {
			pbLoading.setVisibility(View.VISIBLE);
		}
		if (tvLoading != null) {
			tvLoading.setText(R.string.tip_load_ing);
		}
	}

	protected void showLoadingFailed() {
		if (loadingView != null) {
			loadingView.setVisibility(View.VISIBLE);
		}
		if (contentView != null) {
			contentView.setVisibility(View.GONE);
		}
		if (btnReLoad != null) {
			btnReLoad.setVisibility(View.VISIBLE);
		}
		if (pbLoading != null) {
			pbLoading.setVisibility(View.GONE);
		}
		if (tvLoading != null) {
			tvLoading.setText(R.string.tip_load_failed);
		}
	}

	protected void showContentView() {
		if (contentView != null) {
			contentView.setVisibility(View.VISIBLE);
		}
		if (loadingView != null) {
			loadingView.setVisibility(View.GONE);
		}
	}

	protected void reload() {

	}
}
