package com.mob.demo.mobim.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mob.demo.mobim.BaseActivity;
import com.mob.demo.mobim.R;

public class UpdateUserInfoActivity extends BaseActivity {
	private final static int FRAGMENTUPDATEUSERAVATAR = 1;
	private final static int FRAGMENTUPDATEUSERNAME = 2;
	private final static int FRAGMENTBLACKLIST = 3;
	private final static String TAGFRAGMENTID = "fragment_id";

	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frame);

		Intent intent = getIntent();
		if (intent != null) {
			int fragmentId = intent.getIntExtra(TAGFRAGMENTID, 0);
			Fragment fragment = null;
			switch (fragmentId) {
				case FRAGMENTUPDATEUSERAVATAR: {
					fragment = new FragmentUpdateUserAvatar();
				} break;
				case FRAGMENTUPDATEUSERNAME: {
					fragment = new FragmentUpdateUserName();
				} break;
				case FRAGMENTBLACKLIST: {
					fragment = new FragmentBlackList();
				} break;
			}
			if (fragment != null) {
				getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
			}
		}
	}

	public static void gotoUpdateUserAvatarPage(Fragment fragment, int requestCode) {
		Intent intent = new Intent(fragment.getContext(), UpdateUserInfoActivity.class);
		intent.putExtra(TAGFRAGMENTID, FRAGMENTUPDATEUSERAVATAR);
		fragment.startActivityForResult(intent, requestCode);
	}

	public static void gotoUpdateUserInfoPage(Fragment fragment, int requestCode) {
		Intent intent = new Intent(fragment.getContext(), UpdateUserInfoActivity.class);
		intent.putExtra(TAGFRAGMENTID, FRAGMENTUPDATEUSERNAME);
		fragment.startActivityForResult(intent, requestCode);
	}

	public static void gotoBlackListPage(Context context) {
		Intent intent = new Intent(context, UpdateUserInfoActivity.class);
		intent.putExtra(TAGFRAGMENTID, FRAGMENTBLACKLIST);
		context.startActivity(intent);
	}
}
