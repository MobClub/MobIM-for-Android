package com.mob.demo.mobim.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.demo.mobim.BaseFragment;
import com.mob.demo.mobim.MainApplication;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.biz.UserManager;
import com.mob.demo.mobim.component.DialogIKnown;
import com.mob.demo.mobim.component.IMultiItemSupport;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.emoji.DefaultEmojiconDatas;
import com.mob.demo.mobim.emoji.EmojiGridAdapter;
import com.mob.demo.mobim.emoji.Emojicon;
import com.mob.demo.mobim.emoji.SmileUtils;
import com.mob.demo.mobim.model.MsgItem;
import com.mob.demo.mobim.model.MsgReceiverListener;
import com.mob.demo.mobim.utils.ChatUtils;
import com.mob.demo.mobim.utils.Utils;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.MobIMMessageReceiver;
import com.mob.imsdk.model.IMConversation;
import com.mob.imsdk.model.IMGroup;
import com.mob.imsdk.model.IMMessage;
import com.mob.imsdk.model.IMUser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.sharerec.editor.gui.trimvideo.recorder.AudioRecorderByMedia;
import cn.sharerec.editor.gui.trimvideo.recorder.IRecorder;
import cn.sharerec.editor.gui.trimvideo.recorder.VolumeChangeListener;

public class ChatFragment extends BaseFragment {
	private boolean frombar = false;
	private InputMethodManager inputMethodManager;
	/***
	 *  群组ID or 聊天对象ID or 提醒号ID
	 */
	private String id;
	/**
	 * 一般 一对一聊天 or 群组聊天
	 */
	private boolean isGroup;
	private EditText edtInput;
	private Button btnBack;
	private Button btnInfor;
	private ListView lstChats;
	private TextView tvNick;
	private Button btnSpeak;
	private Button btnEmoj;
	private Button btnAtatch;
	private Button btnRecorder;
	private QuickAdapter<SparseArray<Object>> qickAdapter;
	private IMultiItemSupport itemSupport;
	private boolean voiceSpeek = false;
	private IMUser toUser;
	private IMUser owner;
	private static final String TAG = "ChatFragment";
	private IRecorder recorder;
	private Dialog recordDialog;
	private ImageView recorderVolume;
	private String nowaudiopath = null;
	private LinearLayout layoutMore;
	private ImageView igvPic;
	private ImageView igvCamera;
	private ImageView igvFile;
	private boolean isScrolled = false;
//	private EmojiconsView emojicons_view;
	private LinearLayout layoutEmoji;
	private static final int REQUEST_CODE_SELECT_FILE = 12;
	private MsgReceiverListener rev;
	private IMGroup group;
	private String sendId = null;
	private int chatType = 0;
	private long lastRevTime = 0;
	private long mindif = 2 * 1000 * 60;//相隔2分钟就显示时间
	private int pageSize = 10;
	private int unreadcount = 0;
	private int remain = 60;
	private boolean touch = true;
	private Handler myHandler = null;
	private boolean commit = false;
	private View.OnClickListener click = null;
	private List<View> dotViewsList;
	public static final int VALUE_PICK_PICTURE_PATHS = 3;
	private final static int PICK_CAMERA = 1;
	public static final int VALUE_PICK_PICTURE = 2;
	private final static int PICK_VIDEO = 13;
	//	private final static int START_FOR_INFOR = 4 ;
	private File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/mob", "temp.jpg");
	private File videoFile;
	private View.OnLayoutChangeListener onLayoutChangeListener = null;
	private static final int REQUEST_CODE_ASK_CALL_PHONE = 123;
	private View.OnClickListener avatarClick = null;
	private TextView txtRemain;
	private TextView txtTip;
	private String lastMsgID = null;
	private static volatile ArrayList<SparseArray<Object>> msgdata = new ArrayList<SparseArray<Object>>();

	AbsListView.OnScrollListener lstViewScrollListener = null;

	public static ChatFragment instace(String id, boolean isGroup) {
		//通过id 获得 用户信息 or Group 信息
		ChatFragment chatFragment = new ChatFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isgroup", isGroup);
		bundle.putString("id", id);
		chatFragment.setArguments(bundle);
		return chatFragment;
	}

	public static ChatFragment instace(IMUser toUser, int msgcount, boolean frombar) {
		//通过id 获得 用户信息 or Group 信息
		ChatFragment chatFragment = new ChatFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isgroup", false);
		bundle.putBoolean("frombar", frombar);
		bundle.putString("id", toUser.getId());
		bundle.putSerializable("user", toUser);
		bundle.putInt("unreadcount", msgcount);
		chatFragment.setArguments(bundle);
		return chatFragment;
	}

	public static ChatFragment instace(IMGroup imGroup, int msgcount, boolean frombar) {
		//通过id 获得 用户信息 or Group 信息
		ChatFragment chatFragment = new ChatFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isgroup", true);
		bundle.putBoolean("frombar", frombar);
		bundle.putString("id", imGroup.getId());
		bundle.putSerializable("group", imGroup);
		bundle.putInt("unreadcount", msgcount);
		chatFragment.setArguments(bundle);
		return chatFragment;
	}

	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//id = getArguments().getString("id");
		isGroup = getArguments().getBoolean("isgroup");
		frombar = getArguments().getBoolean("frombar");
		unreadcount = getArguments().getInt("unreadcount");
//		Utils.showLog("chatfragment"," the unreadcount >> "+unreadcount);
		if (isGroup) {
			group = (IMGroup) getArguments().getSerializable("group");
			id = group.getId();
			sendId = group.getId();
			chatType = IMConversation.TYPE_GROUP;
		} else {
			toUser = (IMUser) getArguments().getSerializable("user");
			id = toUser.getId();
			sendId = toUser.getId();
			chatType = IMConversation.TYPE_USER;
		}
		MainApplication application = (MainApplication) getActivity().getApplication();
		rev = new MsgReceiverListener(id, new MobIMMessageReceiver() {
			public void onMessageReceived(List<IMMessage> messageList) {
				MobIM.getChatManager().markConversationAllMessageAsRead(sendId, chatType);
				for (IMMessage msg : messageList) {
					Utils.showLog("ChatFragment", " onCreate msg.getType() " + msg.getType());
					Utils.showLog("ChatFragment", " onCreate msg.getBody() " + msg.getBody());
					Utils.showLog("ChatFragment", " onCreate msg.getAttach() is null ?  " + (msg.getAttach() == null));
					//如果发过来的是音频计算获取音频时长
					if (msg.getAttach() != null && msg.getAttach().getType() == IMMessage.Attach.AUDIO) {
						if (msg.getAttach().getDuration() <= 0) {
							final IMMessage audiomsg = msg;
							Utils.getMediaDuration(msg.getAttach().getBody(), new Utils.OnGetDurationListener() {
								public void getGetDuration(int duration) {
									duration = Math.round(duration / 1000);
									audiomsg.getAttach().setDuration(duration);
									Utils.showLog("ChatFragment", " ==== duration >> " + audiomsg.getAttach().getDuration());
									addRevData(audiomsg);
								}
							});
						} else {
							addRevData(msg);
						}

					} else if (msg.getAttach() != null && msg.getAttach().getType() == IMMessage.Attach.VIDEO) {
						final String path = Utils.getDownloadPath(msg.getAttach().getBody());
						final IMMessage videomsg = msg;

						//如果发过来的是视频，默认下载到本地
						Utils.saveFileFromHttp(msg.getAttach().getBody(), path, new Utils.OnDownLoadListener() {

							public void onSucess() {
								//videomsg.getAttach().setLocalPath(path);
								MobIM.getChatManager().updateIMMessageLocalPath(videomsg.getId(), path, new MobIMCallback<Boolean>() {
									public void onSuccess(Boolean aBoolean) {
										qickAdapter.fresh(videomsg.getId(), path);
									}
									public void onError(int code, String message) {

									}
								});
								Utils.showLog("ChatFragment", " ==== duration >> " + videomsg.getAttach().getBody());
							}
							public void onError(int status, String error) {
								//addRevData(videomsg);
							}
						}, null);
						addRevData(msg);
					} else {
						Utils.showLog("ChatFragment", " ==== onCreate rev the msg ....");
						addRevData(msg);
					}
				}
			}
		});
		if (isGroup) {
			application.addGroupMsgRever(rev);
		} else {
			application.addMsgRever(rev);
		}
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (!touch || recordDialog == null || !recordDialog.isShowing()) {
					return;
				}
				if (remain >= 0) {
					remain = remain - 1;
					myHandler.sendEmptyMessageDelayed(0, 1000);
				} else {
					try {
						recorder.stopRecorder();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					int duration = (int) (recorder.getDuration() / 1000);
					if (duration >= 1) {
						Utils.showLog(TAG, " ");
						addMsgData(MobIM.getChatManager().createAudioMessage(sendId, nowaudiopath, duration, chatType));
					}
					recordDialog.dismiss();
				}
				if (remain <= 5 && remain >= 0) {
					if (recorderVolume.getVisibility() == View.VISIBLE) {
						recorderVolume.setVisibility(View.GONE);
					}
					if (txtRemain.getVisibility() == View.GONE) {
						txtRemain.setVisibility(View.VISIBLE);
					}
					txtTip.setText(R.string.remaintimetip);
					if (!getActivity().isFinishing()) {
						String remainTime = getString(R.string.remaintime, remain);
						txtRemain.setText(remainTime);
					}
				}
				////	mHandler.removeMessages(0);
			}
		};

		click = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.btnBack: {
						getActivity().finish();
					} break;
					case R.id.btnInfor: {
						Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
						intent.putExtra("isgroup", isGroup);
						intent.putExtra("id", id);
						if (isGroup) {
							intent.putExtra("group", group);
						} else {
							intent.putExtra("user", toUser);
						}
						getActivity().startActivity(intent);
//					getActivity().startActivityForResult(intent,START_FOR_INFOR);
					} break;
					case R.id.btnSpeak: {
						voiceSpeek = !voiceSpeek;
						freshVoiceSpeek(voiceSpeek);
					} break;
					case R.id.btnAttach: {
						freshMoreLayout();
					} break;
					case R.id.btnEmoj: {
						if (layoutMore.getVisibility() == View.VISIBLE) {
							layoutMore.setVisibility(View.GONE);
						}
						if (layoutEmoji.getVisibility() == View.GONE) {
							layoutEmoji.setVisibility(View.VISIBLE);
							voiceSpeek = false;
							freshVoiceSpeek(voiceSpeek);

							inputMethodManager.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
							btnEmoj.setBackgroundResource(R.drawable.emoj);
							if (!commit) {
								commit = true;
//							getActivity().getSupportFragmentManager().beginTransaction()
//									.replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
//									.commit();
							}
						} else {
							layoutEmoji.setVisibility(View.GONE);
							btnEmoj.setBackgroundResource(R.drawable.keybord);
							edtInput.setFocusable(true);
							edtInput.requestFocus();
//						inputMethodManager.showSoftInputFromInputMethod(edtInput.getWindowToken(), 0);
							inputMethodManager.showSoftInput(edtInput, 0);
//						getActivity().getSupportFragmentManager().beginTransaction()
//								.replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
//								.commit();
						}
					} break;
					case R.id.btnRecorder: {
					} break;
					case R.id.igvCamera: {
						//makePicFromCamera();
						boolean bl = Utils.checkCameraPermission(getContext());
						if (bl) {
							showSelectCamera();
							freshMoreLayout();
						} else {
							showPermisionDialog();
						}
					} break;
					case R.id.igvPic: {
						//selectPicFromLocal();
						freshMoreLayout();
						Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
						intent.putExtra(ImagePickerActivity.SHOWTYPE, ImagePickerActivity.IMAGE);
						startActivityForResult(intent, VALUE_PICK_PICTURE_PATHS);
					} break;
					case R.id.igvFile: {
						freshMoreLayout();
						selectFileFromLocal();
					} break;
					case R.id.txtEmojiSend: {
						sendTxtMsg();
					} break;
				}
			}
		};
		avatarClick = new View.OnClickListener() {

			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.txtFromAvatar: {
						if (isGroup) {
							UserDetailsActivity.gotoUserDetailsPage(getActivity(), (IMUser) v.getTag());
						} else {
							UserDetailsActivity.gotoUserDetailsPage(getActivity(), toUser);
						}
					} break;
					case R.id.txtSendAvatar: {
						UserDetailsActivity.gotoUserDetailsPage(getActivity(), owner);
					} break;
				}
			}
		};
		onLayoutChangeListener = new View.OnLayoutChangeListener() {

			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				//获取屏幕高度
				int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
				//阀值设置为屏幕高度的1/3
				int keyHeight = screenHeight / 3;
				//old是改变前的左上右下坐标点值，没有old的是改变后的左上右下坐标点值
				//现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
				if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {

					//Toast.makeText(MainActivity.this, "监听到软键盘弹起...", Toast.LENGTH_SHORT).show()
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							layoutEmoji.setVisibility(View.GONE);
							if (layoutMore.getVisibility() == View.VISIBLE) {
								layoutMore.setVisibility(View.GONE);
							}
						}
					}, 300);
					//layoutEmoji.setVisibility(View.GONE);
				} else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
					//Toast.makeText(MainActivity.this, "监听到软件盘关闭...", Toast.LENGTH_SHORT).show();
				}
			}
		};

		lstViewScrollListener = new AbsListView.OnScrollListener() {

			private long lastTouch = -1;
			private long nowfirstposition = -1;
			private boolean isEnd = false;
			private boolean isHead = false;

			public void onScrollStateChanged(AbsListView view, int scrollState) {
//			Utils.showLog("ChatFragment", " ===== onScrollStateChanged nowfirstposition >> " + nowfirstposition);
				switch (scrollState) {
					case SCROLL_STATE_IDLE: {
						//滑动停止时调用
						lastTouch = System.currentTimeMillis();
						if (isHead) {
							List<SparseArray<Object>> datas = qickAdapter.getData();
//
							long timefirst = -1;
							int idx = 0;
							do {
								SparseArray<Object> first = datas.get(idx);
								//first.get()
								int itemType = itemSupport.getItemViewType(first);
								MsgItem msgItem = (MsgItem) first.get(itemType);

								if (msgItem.getMsg_type() == MsgItem.TIME_SHOW) {
									idx = idx + 1;
								} else {
									timefirst = msgItem.getRev_time() - 1;
									break;
								}
							} while (timefirst == -1);

							getAndShowPageData(timefirst, datas, false);
						}
						isScrolled = false;
					} break;
					case SCROLL_STATE_TOUCH_SCROLL: {
						//正在滚动时调用
//					Utils.showLog("ChatFragment", " ===== 正在滚动 ");
						isScrolled = true;
					} break;
					case SCROLL_STATE_FLING: {
						//手指快速滑动时,在离开ListView由于惯性滑动
//					Utils.showLog("ChatFragment", " ===== 手指快速滑动时,在离开ListView由于惯性滑动 ");
						isScrolled = true;
					} break;
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				nowfirstposition = firstVisibleItem;
				if (firstVisibleItem == 0) {
					View firstVisibleItemView = lstChats.getChildAt(0);
					if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
						isEnd = false;
						isHead = true;
					}
				} else if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
					View lastVisibleItemView = lstChats.getChildAt(lstChats.getChildCount() - 1);
					if (lastVisibleItemView != null && lastVisibleItemView.getBottom() == lstChats.getHeight()) {
						isEnd = true;
						isHead = false;
					}
				}
			}
		};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MainApplication application = (MainApplication) getActivity().getApplication();
		if (isGroup) {
			application.removeGroupMsgRever(rev);
		} else {
			application.removeMsgRever(rev);
		}
		if (frombar) {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
			getActivity().startActivity(intent);
		}
	}

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_chat, container, false);
	}

	private void freshVoiceSpeek(boolean bl) {
		voiceSpeek = bl;
		if (voiceSpeek) {
			btnSpeak.setBackgroundResource(R.drawable.keybord);
			btnRecorder.setVisibility(View.VISIBLE);
			inputMethodManager.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
			edtInput.setVisibility(View.GONE);
		} else {
			btnSpeak.setBackgroundResource(R.drawable.speak);
			btnEmoj.setBackgroundResource(R.drawable.keybord);
			btnRecorder.setVisibility(View.GONE);
			edtInput.setVisibility(View.VISIBLE);
		}
	}
	public void onViewCreated(View view,Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (toUser == null && !isGroup) {

		} else {
//			Utils.showLog("ChatFragment", "toUser ====> is not null ...");
		}
		owner = UserManager.getUser();
		if (owner == null) {

		} else {
//			Utils.showLog("ChatFragment", " owner ====> is not null ...");
		}
		Utils.saveLocalUserID(getContext(), owner.getId());
		edtInput = (EditText) view.findViewById(R.id.edtInput);
		btnBack = (Button) view.findViewById(R.id.btnBack);
		btnInfor = (Button) view.findViewById(R.id.btnInfor);
		if (isGroup) {
			btnInfor.setBackgroundResource(R.drawable.ic_group);
		}
		lstChats = (ListView) view.findViewById(R.id.lstChats);
		tvNick = (TextView) view.findViewById(R.id.tvNick);
		btnSpeak = (Button) view.findViewById(R.id.btnSpeak);
		btnEmoj = (Button) view.findViewById(R.id.btnEmoj);
		btnAtatch = (Button) view.findViewById(R.id.btnAttach);
		btnRecorder = (Button) view.findViewById(R.id.btnRecorder);
		layoutMore = (LinearLayout) view.findViewById(R.id.layoutMore);

		igvPic = (ImageView) view.findViewById(R.id.igvPic);
		igvCamera = (ImageView) view.findViewById(R.id.igvCamera);
		igvFile = (ImageView) view.findViewById(R.id.igvFile);
//		emojicons_view = (EmojiconsView) view.findViewById(R.id.emojicons_view);
		layoutEmoji = (LinearLayout) view.findViewById(R.id.layoutEmoji);
		TextView txtEmojiSend = (TextView) view.findViewById(R.id.txtEmojiSend);
		txtEmojiSend.setOnClickListener(click);

		view.addOnLayoutChangeListener(onLayoutChangeListener);
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.vp_horizontal_gridview);
		LinearLayout lldotcontainer = (LinearLayout) view.findViewById(R.id.ll_dot_container);
		int sum = SmileUtils.getSmilesSize();
		Emojicon[] datas = DefaultEmojiconDatas.getData();
		int count = 23;
		int page = (int) (sum / count);
		if (sum % count != 0) {
			page = page + 1;
		}
		addDot(page, lldotcontainer);
		List<GridView> gridViews = new ArrayList<GridView>();
		//	GridView gridview = (GridView) view.findViewById(R.id.gridview);
		//	Utils.showLog("ChatFragment"," sum >>"+ sum +" page >> "+page);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			public void onPageSelected(int position) {
				setDotShow(position);
			}

			public void onPageScrollStateChanged(int state) {

			}
		});
		for (int i = 0; i < page; i++) {
			GridView appPage = new GridView(getContext());
			appPage.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.WRAP_CONTENT));
			List<Emojicon> emojicons = new ArrayList<Emojicon>();
//			Utils.showLog("ChatFragment", " i >> " + i);
			int start = i * count;
			int end = (i + 1) * count - 1;
//			Utils.showLog("ChatFragment", " start >> " + start + " end >> " + end);
			for (int j = start; j <= end; j++) {
//				Utils.showLog("ChatFragment", " j >> " + j);
				if (j < sum) {
//					Utils.showLog("ChatFragment", " add ok >> " + j);
					emojicons.add(datas[j]);
				}
			}
			appPage.setNumColumns(8);
			appPage.setVerticalSpacing(30);
			appPage.setHorizontalSpacing(30);
			appPage.setHorizontalScrollBarEnabled(false);
			appPage.setVerticalScrollBarEnabled(false);
			EmojiGridAdapter adapter = new EmojiGridAdapter(getContext(), emojicons);
			appPage.setAdapter(adapter);
			appPage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (view != null) {
						String str = (String) view.getTag();
						if (str.startsWith("[") && str.endsWith("]")) {
							//edtInput.append(str);
							// 根据Bitmap对象创建ImageSpan对象
							ImageView igv = (ImageView) view;
							BitmapDrawable drawable = (BitmapDrawable) igv.getDrawable();

							ImageSpan imageSpan = new ImageSpan(getContext(), drawable.getBitmap());
							// 创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
							String tempUrl = str;
							SpannableString spannableString = new SpannableString(tempUrl);
							// 用ImageSpan对象替换你指定的字符串
							spannableString.setSpan(imageSpan, 0, tempUrl.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							// 将选择的图片追加到EditText中光标所在位置
							int index = edtInput.getSelectionStart(); // 获取光标所在位置
							Editable edittext = edtInput.getEditableText();
							if (index < 0 || index >= edittext.length()) {
								edittext.append(spannableString);
							} else {
								edittext.insert(index, spannableString);
							}
						} else {
							//执行删除表情的操作

						}
					}
				}
			});
			//gridview.setAdapter(adapter);
			gridViews.add(appPage);
//			Utils.setGridViewHeightBasedOnChildren(appPage);
		}
//		Utils.showLog("ChatFragment", " set data ok  gridViews size >> " + gridViews.size());
		EmojiPagerAdater emojiPagerAdater = new EmojiPagerAdater(gridViews);
		viewPager.setAdapter(emojiPagerAdater);
//		Utils.showLog("ChatFragment", " set pageadapter ok ");

		freshVoiceSpeek(voiceSpeek);

		inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		edtInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					sendTxtMsg();
				}
				return true;
			}
		});
		btnAtatch.setOnClickListener(click);
		btnEmoj.setOnClickListener(click);
		btnBack.setOnClickListener(click);
		btnInfor.setOnClickListener(click);
		btnSpeak.setOnClickListener(click);
		btnRecorder.setOnClickListener(click);

		igvCamera.setOnClickListener(click);
		igvFile.setOnClickListener(click);
		igvPic.setOnClickListener(click);

		btnRecorder.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					boolean bl = Utils.checkRecorderPermission(getContext());
					if (!bl) {
						showPermisionDialog();
						return true;
					}
					touch = true;
					if (recorder == null) {
						recorder = new AudioRecorderByMedia();
					}
					nowaudiopath = Utils.getAudioPath(getContext());
					recorder.startRecorder(nowaudiopath, new VolumeChangeListener() {

						public void onVolumeChange(final long volume) {
							if (recorderVolume != null) {
								getActivity().runOnUiThread(new Runnable() {
									public void run() {
										changeRecordView((int) volume);
									}
								});
							}
						}
					});
					showRecorderView();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (recordDialog != null && recordDialog.isShowing()) {
						touch = false;
						try {
							recorder.stopRecorder();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						recordDialog.dismiss();
						int duration = (int) (recorder.getDuration() / 1000);
						if (duration >= 1) {
							addMsgData(MobIM.getChatManager().createAudioMessage(sendId, nowaudiopath, duration, chatType));
						} else {
							Toast.makeText(getActivity(), R.string.too_short_tosend, Toast.LENGTH_SHORT).show();
						}
					}
				}
				return false;
			}
		});
		if (!isGroup) {
			tvNick.setText(toUser.getNickname());
		} else {
			MobIM.getGroupManager().getGroupInfo(group.getId(), false, new MobIMCallback<IMGroup>() {

				public void onSuccess(IMGroup imGroup) {
					btnInfor.setVisibility(View.VISIBLE);
				}

				public void onError(int code, String message) {
//					btnInfor.setVisibility(View.GONE);
				}
			});

			tvNick.setText(group.getName() + "(" + group.getMemberSize() + ")");

		}
		if (qickAdapter == null) {
			itemSupport = new IMultiItemSupport<SparseArray<Object>>() {

				public int getViewTypeCount() {
					return 17;
				}

				public int getItemViewType(SparseArray<Object> item) {
					return ChatUtils.getMsgItemType(item);
				}

				@Override
				public int getLayoutId(int itemViewType) {
					return ChatUtils.getLayoutIdByType(itemViewType);
				}
			};
			List<SparseArray<Object>> list = new ArrayList<SparseArray<Object>>();
			qickAdapter = new QuickAdapter<SparseArray<Object>>(getContext(), list, itemSupport) {
				@Override
				protected void initViews(ViewHolder viewHolder, final int position, SparseArray<Object> item) {
//					if (!isScrolled) {
					ChatUtils.initItem(getActivity(), qickAdapter, itemSupport, isGroup, toUser, owner, viewHolder, position, item, avatarClick, btnRecorder);
//					}
				}

				public void fresh(String id, String path) {
					List<SparseArray<Object>> datas = getData();
					int size = datas.size();
					for (int i = 0; i < size; i++) {
						SparseArray<Object> data = datas.get(i);

						int msgType = ChatUtils.getMsgItemType(data);
						MsgItem msgItem = (MsgItem) data.get(msgType);
						IMMessage msg = msgItem.getImMessage();
						if (msg == null || msg.getAttach() == null) {

						} else {
							if (id.equals(msg.getId())) {
								msg.getAttach().setLocalPath(path);
								SparseArray<Object> nowdata = new SparseArray<Object>();
								nowdata.put(msgType, msg);
								qickAdapter.setData(i, nowdata);
								break;
							}
						}
					}
				}
			};
			lstChats.setAdapter(qickAdapter);
			lstChats.setDividerHeight(0);
		}


		if (isGroup) {
			sendId = group.getId();
			chatType = IMConversation.TYPE_GROUP;
		} else {
			sendId = toUser.getId();
			chatType = IMConversation.TYPE_USER;
		}
		getAndShowData(System.currentTimeMillis());
		//		Configuration config = getResources().getConfiguration();
//		int smallestScreenWidth = config.smallestScreenWidthDp;
//		//L.i("smallest width : "+ smallestScreenWidth);
//		Toast.makeText(getActivity(),":"+smallestScreenWidth,Toast.LENGTH_SHORT).show();
		lstChats.setOnScrollListener(lstViewScrollListener);
	}

	private void getAndShowData(long checkTime) {
		//获取并显示未读的消息
		MobIM.getChatManager().getMessageList(sendId, chatType, pageSize + unreadcount, checkTime, new MobIMCallback<List<IMMessage>>() {

			public void onSuccess(List<IMMessage> imMessages) {
//				System.out.println("test message = " + new Hashon().fromObject(imMessages));
				if (imMessages != null && imMessages.size() > 0) {
					Collections.reverse(imMessages);
//					Utils.showLog("ChatFragment", " imMessages >>> " + imMessages.size());
					for (int i = 0; i < imMessages.size(); i++) {
						IMMessage imMessage = imMessages.get(i);
//						Utils.showLog("ChatFragment", " imMessage.getType() " + imMessage.getType());
//						Utils.showLog("ChatFragment", " imMessage.getBody() " + imMessage.getBody());
//						Utils.showLog("ChatFragment", " imMessage.getAttach() is null ?  " + (imMessage.getAttach() == null));
						if (imMessage.getAttach() != null && imMessage.getAttach().getType() == IMMessage.Attach.AUDIO) {
							if (imMessage.getAttach().getDuration() <= 0) {
								final IMMessage audiomsg = imMessage;
								Utils.getMediaDuration(imMessage.getAttach().getBody(), new Utils.OnGetDurationListener() {
									@Override
									public void getGetDuration(int duration) {
										duration = Math.round(duration / 1000);
										audiomsg.getAttach().setDuration(duration);
										Utils.showLog("ChatFragment", " ==== duration >> " + audiomsg.getAttach().getDuration());
										addRevData(audiomsg);
									}
								});
							} else {
								addRevData(imMessage);
							}
						} else if (imMessage.getAttach() != null && imMessage.getAttach().getType() == IMMessage.Attach.VIDEO) {
							addRevData(imMessage);
						} else {
							addRevData(imMessage);
						}
					}
				} else {
//					Utils.showLog("ChatFragment", " imMessages >>> null ");
				}
				MobIM.getChatManager().markConversationAllMessageAsRead(sendId, chatType);
			}
			public void onError(int code, String message) {

			}
		});
	}

	private void getAndShowPageData(long checkTime, final List<SparseArray<Object>> olddata, final boolean isBottom) {
//		Utils.showLog("", " ==== checkTime >> " + checkTime + " isBottom >> " + isBottom);
		//获取并显示未读的消息
		MobIM.getChatManager().getMessageList(sendId, chatType, pageSize, checkTime, new MobIMCallback<List<IMMessage>>() {
			public void onSuccess(List<IMMessage> imMessages) {
				if (imMessages != null && imMessages.size() > 0) {
//					Utils.showLog("", "=============================== get the imMessage size >> " + imMessages.size());
					msgdata.clear();
					msgdata = null;
					msgdata = new ArrayList<SparseArray<Object>>();
					if (isBottom) {
						for (int i = 0; i < olddata.size(); i++) {
							msgdata.add(olddata.get(i));
						}
					}

					Collections.reverse(imMessages);
//					Utils.showLog("ChatFragment", " imMessages >>> " + imMessages.size());

					for (int i = 0; i < imMessages.size(); i++) {
						IMMessage imMessage = imMessages.get(i);
//						Utils.showLog("ChatFragment", " imMessage.getType() " + imMessage.getType());
//						Utils.showLog("ChatFragment", " imMessage.getBody() " + imMessage.getBody());
//						Utils.showLog("ChatFragment", " imMessage.getAttach() is null ?  " + (imMessage.getAttach() == null));
						if (imMessage.getAttach() != null && imMessage.getAttach().getType() == IMMessage.Attach.AUDIO) {
							if (imMessage.getAttach().getDuration() <= 0) {
								final IMMessage audiomsg = imMessage;
								addData(audiomsg);

							} else {
								addData(imMessage);
							}
						} else if (imMessage.getAttach() != null && imMessage.getAttach().getType() == IMMessage.Attach.VIDEO) {
							final String path = Utils.getDownloadPath(imMessage.getAttach().getBody());
							final IMMessage videomsg = imMessage;
							//如果发过来的是视频，下载到本地再做处理
							addData(videomsg);
						} else {
							addData(imMessage);
						}
//						Utils.showLog("", " ===============================  add in for msgdata size >> " + msgdata.size());
					}
				} else {
//					Utils.showLog("ChatFragment", " imMessages >>> null ");
					Toast.makeText(getContext(), R.string.nomore, Toast.LENGTH_LONG).show();
				}
				if (imMessages != null && imMessages.size() > 0) {
					if (!isBottom) {
						for (int i = 0; i < olddata.size(); i++) {
//							if(i < olddata.size()) {
							msgdata.add(olddata.get(i));
//							}
						}
//						Utils.showLog("", "===============================  add in !isBottom msgdata size >> " + msgdata.size());
					}
					int size = msgdata.size();
					ChatUtils.attachpaths.clear();
					for (int i = 0; i < size; i++) {
						SparseArray<Object> data = msgdata.get(i);
						int msgType = ChatUtils.getMsgItemType(data);
						MsgItem msgItem = (MsgItem) data.get(msgType);
						IMMessage msg = msgItem.getImMessage();
						if (msg == null || msg.getAttach() == null) {
						} else {
							if (msg.getAttach().getType() == IMMessage.Attach.TEXT) {
							} else if (msg.getAttach().getType() == IMMessage.Attach.AUDIO) {
							} else if (msg.getAttach().getType() == IMMessage.Attach.IMAGE) {
								String picpath = msg.getAttach().getLocalPath();
								if (picpath != null && !TextUtils.isEmpty(picpath)) {
								} else {
									picpath = msg.getAttach().getBody();
								}
								ChatUtils.attachpaths.add(picpath);
							} else if (msg.getAttach().getType() == IMMessage.Attach.VIDEO) {
								String picpath = msg.getAttach().getLocalPath();
								if (picpath != null && !TextUtils.isEmpty(picpath)) {
								} else {
									picpath = msg.getAttach().getBody();
								}
								ChatUtils.attachpaths.add(picpath);
							} else if (msg.getAttach().getType() == IMMessage.Attach.FILE) {
							}
						}
					}
					distinctTheMsg();
					qickAdapter.refreshData(msgdata);
//					Utils.showLog("ChatFragment", " ============= chatfragmet refreshData ok ... size >>>> " + msgdata.size());
					//MobIM.getChatManager().markConversationAllMessageAsRead(sendId, chatType);
				}
			}

			public void onError(int code, String message) {

			}
		});
	}

	private void distinctTheMsg() {
		if (msgdata.size() == 2) {
			SparseArray<Object> data = msgdata.get(0);
			int msgType = ChatUtils.getMsgItemType(data);
			MsgItem msgItem = (MsgItem) data.get(msgType);
			IMMessage msg = msgItem.getImMessage();

			SparseArray<Object> compare = msgdata.get(1);
			int msgTypeCompare = ChatUtils.getMsgItemType(compare);
			MsgItem msgItemCompore = (MsgItem) data.get(msgTypeCompare);
			IMMessage msgCompare = msgItemCompore.getImMessage();
			if (msg.getId().equals(msgCompare.getId())) {
				msgdata.remove(0);
			}
		}
	}

	/**
	 * 对于6.0以后的机器动态权限申请
	 */
	/**
	 * 对于6.0以后的机器动态权限申请
	 */
	public void Accessibility() {
		if (Build.VERSION.SDK_INT >= 23) {
			int checkCallPhonePermission2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
			int checkCallPhonePermission3 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
			if (checkCallPhonePermission2 != PackageManager.PERMISSION_GRANTED && checkCallPhonePermission3 != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_CALL_PHONE);
				return;
			} else {
			}
		} else {
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_ASK_CALL_PHONE: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				} else {
				}
			} break;
			default: {
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			}
		}
	}

	private void sendTxtMsg() {
		String input = edtInput.getText().toString();
		if (TextUtils.isEmpty(input)) {
			return;
		}
		if(input.length() > 5000) {
			String tip = getString(R.string.tiptexttoolong,input.length());
			Toast.makeText(getContext(),tip,Toast.LENGTH_SHORT).show();
			return;
		}
		edtInput.setText("");
		addMsgData(MobIM.getChatManager().createTextMessage(sendId, input, chatType));
	}

	private static class EmojiPagerAdater extends PagerAdapter {

		private List<GridView> views;

		public EmojiPagerAdater(List<GridView> views) {
			this.views = views;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			//container.removeAllViews();
			container.addView(views.get(position));
			//Log.d("tag",String.valueOf(position));
			return views.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(views.get(position));
		}

		@Override
		public int getCount() {
			if (views != null) {
				return views.size();
			}
			return 0;
		}

		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}


	private void changeRecordView(int volume) {
		switch (volume) {
			case 1:
			case 2: {
				//recorderVolume.setBackground();
				//new Drawable();
				recorderVolume.setImageResource(R.drawable.recorder_volume1);
			} break;
			case 3: {
				recorderVolume.setImageResource(R.drawable.recorder_volume2);
			} break;
			case 4: {
				recorderVolume.setImageResource(R.drawable.recorder_volume3);
			} break;
			case 5:
			case 6: {
				recorderVolume.setImageResource(R.drawable.recorder_volume4);
			} break;
			case 7:
			case 8: {
				recorderVolume.setImageResource(R.drawable.recorder_volume5);
			} break;
			default: {
				recorderVolume.setImageResource(R.drawable.recorder_volume1);
			}
		}
	}




	private void showRecorderView() {
		Accessibility();
		View view = LayoutInflater.from(getContext()).inflate(R.layout.record_dialog, null);
		recorderVolume = (ImageView) view.findViewById(R.id.recorderVolume);
		recordDialog = new AlertDialog.Builder(getContext(), R.style.RecorderDialogStyle).setView(view).create();
		txtRemain = (TextView) view.findViewById(R.id.txtRemain);
		txtTip = (TextView) view.findViewById(R.id.txtTip);
		remain = 60;
		recordDialog.show();
		myHandler.sendEmptyMessage(0);
	}

	/**
	 * 将数据加到list的底部
	 *
	 * @param msg
	 */
	private void addRevData(IMMessage msg) {
//		Utils.showLog(TAG," msg .getID "+msg.getId());
//		Utils.showLog(TAG," msg .getID "+lastMsgID);
		if (msg != null && lastMsgID == null) {
			lastMsgID = msg.getId();
		} else {
			if (lastMsgID != null) {
				if (lastMsgID.equals(msg.getId())) {
					return;
				}
			}
		}
		lastMsgID = msg.getId();

		SparseArray<Object> objectSparseArray = new SparseArray<Object>();

		MsgItem msgitem = MsgItem.getMsgItemFromImMessage(msg);

		objectSparseArray.append(msgitem.getMsg_type(), msgitem);
		List<SparseArray<Object>> msgdata = new ArrayList<SparseArray<Object>>();
		msgdata.add(objectSparseArray);
//		Utils.showLog("ChatFramg", " ==== in addRevData >> getMsg_type " + msgitem.getMsg_type());
//		Utils.showLog("ChatFramg"," ==== in MsgItem.FILE_REV >> msg. "+ MsgItem.FILE_REV);
		beforeAddMsg(msg);
		qickAdapter.addData(msgdata, false);
		lstChats.setSelection(qickAdapter.getCount() - 1);
		//间隔超过一定时间就需要添加时间标签
	}



	/**
	 * 将数据加到头部
	 *
	 * @param msg
	 */
	private void addData(IMMessage msg) {
		SparseArray<Object> objectSparseArray = new SparseArray<Object>();
		MsgItem msgitem = MsgItem.getMsgItemFromImMessage(msg);
		objectSparseArray.append(msgitem.getMsg_type(), msgitem);
		//msgdata = new ArrayList<SparseArray<Object>>();
//		Utils.showLog("ChatFramg"," ==== in MsgItem.FILE_REV >> msg. "+ MsgItem.FILE_REV);

		if (msg.getType() != IMMessage.TYPE_WARN) {
			long dif = Math.abs(lastRevTime - msg.getCreateTime());
			if (dif >= mindif) {
				addTimeShow(msg.getCreateTime(), null);
				lastRevTime = msg.getCreateTime();
			}
		}
		msgdata.add(objectSparseArray);
//		Utils.showLog("ChatFramg", " ==== in addData >> getMsg_type " + msgitem.getMsg_type());
	}

	private void addTimeShow(long timeshow) {
		SparseArray<Object> objectSparseArray = new SparseArray<Object>();

		MsgItem msgitem = new MsgItem();
		msgitem.setMsg_type(MsgItem.TIME_SHOW);

		msgitem.setTip(Utils.getTimeShowStr(getContext(), timeshow));

		objectSparseArray.append(msgitem.getMsg_type(), msgitem);

		List<SparseArray<Object>> nowdata = new ArrayList<SparseArray<Object>>();
		nowdata.add(objectSparseArray);

		qickAdapter.addData(nowdata, false);
	}

	private void addTimeShow(long timeshow, List<SparseArray<Object>> msgdataa) {
		SparseArray<Object> objectSparseArray = new SparseArray<Object>();

		MsgItem msgitem = new MsgItem();
		msgitem.setMsg_type(MsgItem.TIME_SHOW);
		msgitem.setTip(Utils.getTimeShowStr(getContext(), timeshow));

		objectSparseArray.append(msgitem.getMsg_type(), msgitem);

		if (msgdata == null) {
			msgdata = new ArrayList<SparseArray<Object>>();
		}
		msgdata.add(objectSparseArray);
		//qickAdapter.addData(msgdata,false);
	}

	private void addMsgData(IMMessage msg) {
		msg.setCreateTime(System.currentTimeMillis());
		msg.setStatus(IMMessage.STATUS_SEND_ING);
		final SparseArray<Object> objectSparseArray = new SparseArray<Object>();

		MsgItem msgitem = MsgItem.getMsgItemFromImMessage(msg);

		objectSparseArray.append(msgitem.getMsg_type(), msgitem);

		List<SparseArray<Object>> nowdata = new ArrayList<SparseArray<Object>>();
		nowdata.add(objectSparseArray);

		beforeAddMsg(msg);

		qickAdapter.addData(nowdata, false);
		lstChats.setSelection(qickAdapter.getCount() - 1);
		final int insertIdx = qickAdapter.getCount() - 1;
//		String sendId = null;
//		int chatType = -1;
		if (sendId == null) {
			if (isGroup) {
				sendId = group.getId();
				chatType = IMMessage.TYPE_GROUP;
			} else {
				sendId = toUser.getId();
				chatType = IMMessage.TYPE_USER;
			}
		}

		MobIM.getChatManager().sendMessage(msg, new MobIMCallback<Void>() {

			public void onSuccess(Void result) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						ChatUtils.setMsgStatusView(IMMessage.STATUS_SUCCESS, qickAdapter, insertIdx);
					}
				});
			}

			public void onError(int code, String message) {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						ChatUtils.setMsgStatusView(IMMessage.STATUS_FAILED, qickAdapter, insertIdx);
					}
				});
			}
		});
	}

	private void beforeAddMsg(IMMessage msg) {
//	Log.e(TAG," ============== ...");
		if (msg.getAttach() == null) {
		} else {
			if (msg.getAttach().getType() == IMMessage.Attach.TEXT) {
			} else if (msg.getAttach().getType() == IMMessage.Attach.AUDIO) {
			} else if (msg.getAttach().getType() == IMMessage.Attach.IMAGE) {
				String picpath = msg.getAttach().getLocalPath();
				if (picpath != null && !TextUtils.isEmpty(picpath)) {
				} else {
					picpath = msg.getAttach().getBody();
				}
				ChatUtils.attachpaths.add(picpath);
			} else if (msg.getAttach().getType() == IMMessage.Attach.VIDEO) {
				String picpath = msg.getAttach().getLocalPath();
				if (picpath != null && !TextUtils.isEmpty(picpath)) {
				} else {
					picpath = msg.getAttach().getBody();
				}
				ChatUtils.attachpaths.add(picpath);
			} else if (msg.getAttach().getType() == IMMessage.Attach.FILE) {
			}
		}

		if (msg.getType() != IMMessage.TYPE_WARN) {
			long dif = Math.abs(lastRevTime - msg.getCreateTime());
			if (dif >= mindif) {
				addTimeShow(msg.getCreateTime());
				lastRevTime = msg.getCreateTime();
			}
		}
	}


	private void showPermisionDialog() {
		DialogIKnown dialogIKnown = new DialogIKnown(getContext(), getString(R.string.permissiontip),
				R.string.txt_ok, new DialogIKnown.OnConfirmClickListener() {
			public void onConfirm() {

			}
		});
		dialogIKnown.show();
	}

	private void showSelectCamera() {
		String[] res = new String[]{
				getString(R.string.takecamera),
				getString(R.string.takevideo)
		};
		new AlertDialog.Builder(getActivity()).setTitle(R.string.pselect).setSingleChoiceItems(res, 0, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					makePicFromCamera();
				} else {
					maketVideoFromCamera();
				}
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	private void freshMoreLayout() {
		if (layoutEmoji.getVisibility() == View.VISIBLE) {
			layoutEmoji.setVisibility(View.GONE);
		}
		if (layoutMore.getVisibility() == View.VISIBLE) {
			layoutMore.setVisibility(View.GONE);
		} else {
			inputMethodManager.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
			layoutMore.setVisibility(View.VISIBLE);
		}
	}



	private void makePicFromCamera() {
		//Log.e(TAG," selectPicFromCamera ");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/mob", System.currentTimeMillis() + "temp.jpg");
		// 下面这句指定调用相机拍照后的照片存储的路径
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getUriForFile(getContext(), file));
		startActivityForResult(intent, PICK_CAMERA);
	}

	private void maketVideoFromCamera() {
		//Log.e(TAG," selectPicFromCamera ");
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		//mFile = null;
		videoFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/mob", System.currentTimeMillis() + "temp.3gp");
//		Uri videoUri = Uri.fromFile(videoFile);
		// 下面这句指定调用相机拍摄视频后的存储的路径
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getUriForFile(getContext(), videoFile));
		//最多可录制15秒
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
		startActivityForResult(intent, PICK_VIDEO);
	}

	private void selectPicFromLocal() {
		//Log.e(TAG," selectPicFromLocal ");
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intent, VALUE_PICK_PICTURE);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isGroup) {
			MobIM.getGroupManager().getGroupInfo(group.getId(), false, new MobIMCallback<IMGroup>() {

				public void onSuccess(IMGroup imGroup) {
					group = imGroup;
					btnInfor.setVisibility(View.VISIBLE);
					tvNick.setText(group.getName() + "(" + group.getMemberSize() + ")");
					//Utils.showLog(TAG," onSuccess > getGroupInfo  onResume  ");
				}

				public void onError(int code, String message) {
					if (message.contains("too") && message.contains("frequently")) {
					} else {
						btnInfor.setVisibility(View.GONE);
					}

				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		Utils.showLog(TAG, " I am in RESULT OK >>>  requestCode >>>>>>> " + requestCode + " resultCode >>> " + resultCode);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case PICK_CAMERA: {// 如果是直接从相册获取
					//startPhotoZoom(data.getData());//拿到所选图片的Uri
//					Uri uri = data.getData();
					String path = file.getAbsolutePath();// Utils.getRealFilePath(getContext(),uri);
					boolean bl = checkFileTooBig(new File(path));
					if (bl) {
						addMsgData(MobIM.getChatManager().createImageMessage(sendId, path, chatType));
					}
				} break;
				case PICK_VIDEO: {
					String path = videoFile.getAbsolutePath();// Utils.getRealFilePath(getContext(),uri);
					//	File file = new File(path);
					boolean bl = checkFileTooBig(new File(path));
					if (bl) {
						addMsgData(MobIM.getChatManager().createVideoMessage(sendId, path, chatType));
					}
				} break;
				case VALUE_PICK_PICTURE_PATHS: {
					ArrayList<String> pics = data.getStringArrayListExtra("paths");
					String path = null;
					for (int i = 0; i < pics.size(); i++) {
						path = pics.get(i);
						addMsgData(MobIM.getChatManager().createImageMessage(sendId, path, chatType));
					}
				} break;
				case REQUEST_CODE_SELECT_FILE: {//send the file
					if (data != null) {
						Uri uri = data.getData();
						String path = null;
						boolean bl = false;
						if (uri != null) {
							//sendFileByUri(uri);
							path = Utils.getRealFilePath(getContext(), uri);
							bl = checkFileTooBig(new File(path));
							if (bl) {
								addMsgData(MobIM.getChatManager().createFileMessage(sendId, path, chatType));
							}
						}
					}
				} break;
			}
		}
	}

	private boolean checkFileTooBig(File file) {
//		File file = new File(path);
		long size = 0;
		try {
			size = Utils.getFileSize(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		Utils.showLog(TAG, "size >>> " + size);
//		Utils.showLog(TAG, "path >>> " + file.getAbsolutePath());
//		Utils.showLog(TAG, "1048576 * 200 >>> " + 1048576 * 200);
		if (size > 1048576 * 20) {
			Toast.makeText(getActivity(), R.string.filetoobig, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * select file
	 */
	protected void selectFileFromLocal() {
		Intent intent = null;
		if (Build.VERSION.SDK_INT < 19) { //api 19 and later, we can't use this way, demo just select from images

			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

		} else {
			//intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
		}
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
	}


	/**
	 * 创建指定数量的圆点
	 *
	 * @param dotNumber viewPager的数量
	 */
	private void addDot(int dotNumber, LinearLayout lldotcontainer) {
		dotViewsList = new ArrayList<View>();
		LinearLayout dotLayout = lldotcontainer;
		for (int i = 0; i < dotNumber; i++) {
			ImageView dotView = new ImageView(getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);

			// 圆点与圆点之间的距离
			params.leftMargin = 10;
			params.rightMargin = 10;

			// 圆点的大小
			params.height = 15;
			params.width = 15;

			dotLayout.addView(dotView, params);
			dotViewsList.add(dotView);
		}
		// 设置圆点默认选中第一个
		setDotShow(0);
	}

	/**
	 * 显示底部圆点导航
	 *
	 * @param position 选中哪个圆点
	 */
	private void setDotShow(int position) {
		if (dotViewsList == null) {
			return;
		}
		for (int i = 0; i < dotViewsList.size(); i++) {
			if (i == position) {
				dotViewsList.get(position).setBackgroundResource(R.drawable.circle_check);
			} else {
				dotViewsList.get(i).setBackgroundResource(R.drawable.circle_uncheck);
			}
		}
	}
}
