package com.mob.demo.mobim.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.mob.demo.mobim.R;

/***
 * 输入文本信息，并返回其输入
 */
public class InputForResultActivity extends Activity {
	public static final int FORNICKGROUP = 0;
	public static final int FORNAMEGROUP = 1;
	public static final int FORNOTICEGROUP = 2;
	public static final int FORSUMMARYGROUP = 3;

	private TextView txtBack;
	private TextView txtSave;
	private TextView tvTitle;
	private EditText edtInput;
	private TextView txtShow;
	private boolean readOnly = false;
	private String showstr;
	private TextView txtTip;
	private TextView txtLengthTip;
	private int type;
	private View.OnClickListener clickListener = null;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.input_frame);
//		Toast.makeText(this,"test .... in  ..... ",Toast.LENGTH_SHORT).show();
//		Utils.showLog("InputForResultAct"," ===================== onCreate ...");

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		txtBack = (TextView) findViewById(R.id.txtBack);
		txtSave = (TextView) findViewById(R.id.txtSave);
		edtInput = (EditText) findViewById(R.id.edtInput);
		txtShow = (TextView) findViewById(R.id.txtShow);
		txtTip = (TextView) findViewById(R.id.txtTip);
		txtLengthTip = (TextView) findViewById(R.id.txtLengthTip);
//		txtShowInfor = (TextView) findViewById(R.id.txtShowInfor);
		clickListener = new View.OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.txtBack: {
						setResult(RESULT_CANCELED);
						finish();
					} break;
					case R.id.txtSave: {
						if (edtInput.getVisibility() == View.GONE) {
							edtInput.setVisibility(View.VISIBLE);
							edtInput.setEnabled(true);
							txtShow.setVisibility(View.GONE);
							txtSave.setText(R.string.wancheng);
							int cl = Color.parseColor("#00C59C");
							txtSave.setTextColor(cl);
							if (FORNOTICEGROUP == type) {
								txtLengthTip.setVisibility(View.VISIBLE);
								txtLengthTip.setText(R.string.groupnoticlength);
							} else if (FORSUMMARYGROUP == type) {
								txtLengthTip.setVisibility(View.VISIBLE);
								txtLengthTip.setText(R.string.groupsummarylength);
							}
						} else {
							Intent data = new Intent();
							data.putExtra("data", edtInput.getText().toString());
							setResult(Activity.RESULT_OK, data);
							finish();
						}
					} break;
				}
			}
		};
		txtBack.setOnClickListener(clickListener);
		txtSave.setOnClickListener(clickListener);

		readOnly = getIntent().getBooleanExtra("readonly", true);
		showstr = getIntent().getStringExtra("showstr");
		type = getIntent().getIntExtra("type", 0);
//		Utils.showLog("InputForResultAct"," ===================== readOnly "+readOnly +" showstr >> "+showstr+" type > "+type);
		if (type == FORNICKGROUP) {
			tvTitle.setText(R.string.nickofgroup);
			txtSave.setText(R.string.wancheng);
			edtInput.setText(showstr);
			edtInput.setEnabled(true);
			edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
			if (!readOnly) {
				edtInput.setMaxLines(1);
				txtLengthTip.setVisibility(View.VISIBLE);
				txtLengthTip.setText(R.string.mygroupnicknotlengthten);
				edtInput.setHint(R.string.mygroupnicknotlengthten);
			}

		} else if (FORNAMEGROUP == type) {
			tvTitle.setText(R.string.groupofname);
			txtTip.setVisibility(View.VISIBLE);

			if (!readOnly) {
				edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
				edtInput.setMaxLines(1);
				txtLengthTip.setVisibility(View.VISIBLE);
				txtLengthTip.setText(R.string.groupnamenotlengthten);
				edtInput.setHint(R.string.groupnamenotlengthten);
			}
		} else if (FORNOTICEGROUP == type) {
			tvTitle.setText(R.string.notice);
			txtTip.setVisibility(View.VISIBLE);
			if (!readOnly) {
				edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(140)});
				edtInput.setMinLines(5);
				edtInput.setMaxLines(5);
				txtLengthTip.setVisibility(View.VISIBLE);
				txtLengthTip.setText(R.string.groupnoticlength);
				edtInput.setHint(R.string.groupnoticlength);
			}
		} else if (FORSUMMARYGROUP == type) {
			tvTitle.setText(R.string.summary);
			txtTip.setVisibility(View.VISIBLE);
			if (!readOnly) {
				edtInput.setMinLines(5);
				edtInput.setMaxLines(5);
				txtLengthTip.setVisibility(View.VISIBLE);
				txtLengthTip.setText(R.string.groupsummarylength);
				edtInput.setHint(R.string.groupsummarylength);
				edtInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(140)});
			}
		}
		//if(readOnly)
		if (readOnly) {
			txtSave.setVisibility(View.GONE);
			edtInput.setVisibility(View.GONE);
		}
		txtShow.setText(showstr);
		txtShow.setVisibility(View.VISIBLE);
		if (!readOnly) {
			edtInput.setText(showstr);
			txtSave.setVisibility(View.VISIBLE);
			txtSave.setText(R.string.edit);
			txtSave.setTextColor(Color.BLACK);
			edtInput.setText(showstr);
			edtInput.setVisibility(View.GONE);
			txtShow.setText(showstr);
			txtShow.setVisibility(View.VISIBLE);
		}

	}


}
