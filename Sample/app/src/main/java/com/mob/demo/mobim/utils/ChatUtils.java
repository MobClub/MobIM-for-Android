package com.mob.demo.mobim.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.component.IMultiItemSupport;
import com.mob.demo.mobim.component.PieView;
import com.mob.demo.mobim.component.QuickAdapter;
import com.mob.demo.mobim.component.ViewHolder;
import com.mob.demo.mobim.model.MsgItem;
import com.mob.demo.mobim.ui.UserDetailsActivity;
import com.mob.imsdk.MobIM;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMMessage;
import com.mob.imsdk.model.IMUser;
import com.mob.tools.utils.UIHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatUtils {
	private static String playAudioPath = null;
	private static ImageView lastAudioView;
	private static int lastAudioType;
	private static MediaPlayer player = null;
	private final static int IDPOSISITION = R.string.addgroupmember;
	public static ArrayList<String> attachpaths = new ArrayList<String>();
	private static long clicktime;
	private static int showidx;
	private static boolean fromLong = false;
	private static float x1;
	private static float x2;
	private static float y1;
	private static float y2;
	private static final String TAG = "chatfragment";

	public static int getMsgItemType(SparseArray<Object> item) {
		Object object = item.get(MsgItem.AUDIO_REV);
		if (object != null) {
			return MsgItem.AUDIO_REV;
		}

		object = item.get(MsgItem.AUDIO_SEND);
		if (object != null) {
			return MsgItem.AUDIO_SEND;
		}

		object = item.get(MsgItem.TEXT_REV);
		if (object != null) {
			return MsgItem.TEXT_REV;
		}
		object = item.get(MsgItem.TEXT_SEND);
		if (object != null) {
			return MsgItem.TEXT_SEND;
		}

		object = item.get(MsgItem.IMAGE_REV);
		if (object != null) {
			return MsgItem.IMAGE_REV;
		}
		object = item.get(MsgItem.IMAGE_SEND);
		if (object != null) {
			return MsgItem.IMAGE_SEND;
		}

		object = item.get(MsgItem.LOCATION_REV);
		if (object != null) {
			return MsgItem.LOCATION_REV;
		}
		object = item.get(MsgItem.LOCATION_SEND);
		if (object != null) {
			return MsgItem.LOCATION_SEND;
		}

		object = item.get(MsgItem.VIDEO_REV);
		if (object != null) {
			return MsgItem.VIDEO_REV;
		}

		object = item.get(MsgItem.VIDEO_SEND);
		if (object != null) {
			return MsgItem.VIDEO_SEND;
		}

		object = item.get(MsgItem.FILE_REV);
		if (object != null) {
			return MsgItem.FILE_REV;
		}

		object = item.get(MsgItem.FILE_SEND);
		if (object != null) {
			return MsgItem.FILE_SEND;
		}

		object = item.get(MsgItem.TIME_SHOW);
		if (object != null) {
			return MsgItem.TIME_SHOW;
		}

		object = item.get(MsgItem.WARN_DATA);
		if (object != null) {
			return MsgItem.WARN_DATA;
		}
		return MsgItem.TIME_SHOW;
	}

	public static int getLayoutIdByType(int itemViewType) {
		int layoutId = -1;
		switch (itemViewType) {
			case MsgItem.TEXT_REV: {
				layoutId = R.layout.msg_textrev_item;
			} break;
			case MsgItem.TEXT_SEND: {
				layoutId = R.layout.msg_text_item;
			} break;
			case MsgItem.AUDIO_REV: {
				layoutId = R.layout.msg_audiorev_item;
			} break;
			case MsgItem.AUDIO_SEND: {
				layoutId = R.layout.msg_audiosend_item;
			} break;
			case MsgItem.FILE_REV: {
				layoutId = R.layout.msg_filerev_item;
			} break;
			case MsgItem.FILE_SEND: {
				layoutId = R.layout.msg_file_item;
			} break;
			case MsgItem.IMAGE_REV: {
				layoutId = R.layout.msg_imagerev_item;
			} break;
			case MsgItem.IMAGE_SEND: {
				layoutId = R.layout.msg_image_item;
			} break;
			case MsgItem.LOCATION_REV:
			case MsgItem.LOCATION_SEND: {

			} break;
			case MsgItem.VIDEO_REV: {
				layoutId = R.layout.msg_videorev_item;
			} break;
			case MsgItem.VIDEO_SEND: {
				layoutId = R.layout.msg_video_item;
			} break;
			case MsgItem.TIME_SHOW: {
				layoutId = R.layout.time_show;
			} break;
			case MsgItem.WARN_DATA: {
				layoutId = R.layout.warn_data;
			} break;
		}
		if (layoutId == -1) {
			return R.layout.msg_text_item;
		} else {
			return layoutId;
		}
	}

	public static void initItem(final Activity context, final QuickAdapter<SparseArray<Object>> qickAdapter,
								IMultiItemSupport<SparseArray<Object>> itemSupport, boolean isGroup, IMUser toUser,
								IMUser owner, ViewHolder viewHolder, final int position, final SparseArray<Object> item,
								View.OnClickListener avatarClick, final Button btnRecorder) {
		SparseArray<Object> itemnow = qickAdapter.getItem(position);
		final int itemViewType = itemSupport.getItemViewType(itemnow);
		ImageView avatarleft = null;
		ImageView avatarright = null;
		ImageView igvReSend = null;
		ProgressBar pbLoading = null;
		IMMessage message = new IMMessage();
		MsgItem msgItem = null;
		switch (itemViewType) {
			case MsgItem.TEXT_REV: {
				msgItem = (MsgItem) item.get(MsgItem.TEXT_REV);
				message = msgItem.getImMessage();
				avatarleft = viewHolder.getView(R.id.txtFromAvatar);
				avatarright = viewHolder.getView(R.id.txtSendAvatar);
				TextView txtMsg = viewHolder.getView(R.id.txtMessage);
				//收到的消息
				avatarleft.setVisibility(View.VISIBLE);
				avatarright.setVisibility(View.INVISIBLE);

				if (isGroup) {
					IMUser user = message.getFromUserInfo();
					if (user != null) {
						LoadImageUtils.showAvatar(context, avatarleft, user.getAvatar(), R.drawable.ic_default_user);
					} else {
						avatarleft.setImageResource(R.drawable.ic_default_user);
					}
				} else {
					LoadImageUtils.showAvatar(context, avatarleft, toUser.getAvatar(), R.drawable.ic_default_user);
				}

				txtMsg.setBackgroundResource(R.drawable.msg_from);

				SpannableString spannableString = null;
				if (message.getAttach() != null) {
					spannableString = Utils.changeStrToWithEmoji(context, message.getAttach().getBody());
				} else {
					spannableString = Utils.changeStrToWithEmoji(context, message.getBody());
				}
				final TextView nowText = txtMsg;
				txtMsg.setText(spannableString);
				nowText.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);
				txtMsg.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initPopWindow(context, nowText, qickAdapter);
						return false;
					}
				});
			} break;
			case MsgItem.TEXT_SEND: {
				message = new IMMessage();
				msgItem = (MsgItem) item.get(MsgItem.TEXT_SEND);
				message = msgItem.getImMessage();
				avatarright = viewHolder.getView(R.id.txtSendAvatar);
				TextView txtMsg = viewHolder.getView(R.id.txtMessage);
				avatarright.setVisibility(View.VISIBLE);
				LoadImageUtils.showAvatar(context, avatarright, owner.getAvatar(), R.drawable.ic_default_user);
				//}
				SpannableString spannableString = null;
				//对发送的信息进行过滤，针对其中的emoji进行过滤替换显示
				if (message.getAttach() != null) {
					spannableString = Utils.changeStrToWithEmoji(context, message.getAttach().getBody());
				} else {
					spannableString = Utils.changeStrToWithEmoji(context, message.getBody());
				}
				igvReSend = viewHolder.getView(R.id.igvReSend);
				pbLoading = viewHolder.getView(R.id.pbLoading);

				final TextView revnowText = txtMsg;
				txtMsg.setText(spannableString);
				revnowText.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);
				txtMsg.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initPopWindow(context, revnowText, qickAdapter);
						return false;
					}
				});
			} break;
			case MsgItem.AUDIO_SEND: {
				avatarright = viewHolder.getView(R.id.txtSendAvatar);
				msgItem = (MsgItem) item.get(MsgItem.AUDIO_SEND);
				message = msgItem.getImMessage();
				TextView txtMessage = viewHolder.getView(R.id.txtMessage);
				ImageView igvRedCircle = viewHolder.getView(R.id.igvUnRead);
				igvReSend = viewHolder.getView(R.id.igvReSend);
				pbLoading = viewHolder.getView(R.id.pbLoading);
				igvRedCircle.setVisibility(View.INVISIBLE);
				RelativeLayout layoutInfor = viewHolder.getView(R.id.layoutInfor);
				LinearLayout layoutTip = viewHolder.getView(R.id.layoutTip);
				TextView txtDuration = viewHolder.getView(R.id.txtDuration);
				txtDuration.setText(message.getAttach().getDuration() + "'");
				final ImageView igvAudio = viewHolder.getView(R.id.igvAudio);

				int sumwidth = layoutInfor.getWidth();
				igvReSend = viewHolder.getView(R.id.igvReSend);
				pbLoading = viewHolder.getView(R.id.pbLoading);
				if (sumwidth == 0) {
					sumwidth = context.getWindow().getDecorView().getWidth() / 9 * 7;
				}
				int width = layoutTip.getWidth();
				int remain = sumwidth - width;
				int nowwidth = remain / 60 * message.getAttach().getDuration();
				int min = Utils.dip2px(context, 50);
				if (nowwidth < min) {
					nowwidth = min;
				}
				ViewGroup.LayoutParams layoutParams = txtMessage.getLayoutParams();
				layoutParams.width = nowwidth;
				txtMessage.setLayoutParams(layoutParams);

				String localpath = message.getAttach().getLocalPath();
				String tempPath = message.getAttach().getLocalPath();
				if (new File(localpath).exists()) {
				} else {
					tempPath = message.getAttach().getBody();
				}
				final String path = tempPath;
				final int idx = position;

				final TextView textViewSend = txtMessage;
				textViewSend.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);
				txtMessage.setOnTouchListener(new ClickAndLongClickHandler(new View.OnClickListener() {
					public void onClick(View v) {
						Object lastClick = v.getTag(R.string.Video_footage);
						if (lastClick != null) {
							Long lastTimeMills = (Long) lastClick;
							if (System.currentTimeMillis() - lastTimeMills < 850) {
								return;
							}
						}
						v.setTag(R.string.Video_footage, Long.valueOf(System.currentTimeMillis()));
						if (btnRecorder.isEnabled()) {
							playTheAudioByPath(context, qickAdapter, path, idx, btnRecorder, MsgItem.AUDIO_SEND, igvAudio);
						} else {
							if (playAudioPath.equals(path)) {
								stopAudio();
								btnRecorder.setEnabled(true);
								igvAudio.setImageResource(R.drawable.audio_send);
							} else {
								stopAudio();
								playTheAudioByPath(context, qickAdapter, path, idx, btnRecorder, MsgItem.AUDIO_SEND, igvAudio);
							}
						}
					}
				}, new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initDelPopWindow(context, textViewSend, qickAdapter);
						return true;
					}
				}));
				LoadImageUtils.showAvatar(context, avatarright, owner.getAvatar(), R.drawable.ic_default_user);
			} break;
			case MsgItem.AUDIO_REV: {
				avatarleft = viewHolder.getView(R.id.txtFromAvatar);
				msgItem = (MsgItem) item.get(MsgItem.AUDIO_SEND);
				if (msgItem == null) {
					msgItem = (MsgItem) item.get(MsgItem.AUDIO_REV);
				}
				message = msgItem.getImMessage();
				TextView txtMessage = viewHolder.getView(R.id.txtMessage);
				ImageView igvRedCircle = viewHolder.getView(R.id.igvUnRead);
				if (msgItem.isRead()) {
					igvRedCircle.setVisibility(View.INVISIBLE);
				} else {
					igvRedCircle.setVisibility(View.VISIBLE);
				}
				RelativeLayout layoutInfor = viewHolder.getView(R.id.layoutInfor);
				LinearLayout layoutTip = viewHolder.getView(R.id.layoutTip);
				TextView txtDuration = viewHolder.getView(R.id.txtDuration);
				final ImageView igvrevAudio = viewHolder.getView(R.id.igvAudio);
				txtDuration.setText(message.getAttach().getDuration() + "'");
				int sumwidth = layoutInfor.getWidth();
				if (sumwidth == 0) {
					sumwidth = context.getWindow().getDecorView().getWidth() / 9 * 7;
				}
//
				int width = layoutTip.getWidth();
				int remain = sumwidth - width;
				int nowwidth = remain / 60 * message.getAttach().getDuration();
				int min = Utils.dip2px(context, 50);
				if (nowwidth < min) {
					nowwidth = min;
				}
				ViewGroup.LayoutParams layoutParams = txtMessage.getLayoutParams();
				layoutParams.width = nowwidth;
				txtMessage.setLayoutParams(layoutParams);
				txtMessage.postInvalidate();
				final String audioPath = message.getAttach().getBody();
				final int audioPosition = position;

				final TextView textViewRev = txtMessage;
				textViewRev.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);
				txtMessage.setOnTouchListener(new ClickAndLongClickHandler(new View.OnClickListener() {
					public void onClick(View v) {
						Object lastClick = v.getTag(R.string.Video_footage);
						if (lastClick != null) {
							Long lastTimeMills = (Long) lastClick;
							if (System.currentTimeMillis() - lastTimeMills < 850) {
								return;
							}
						}
						v.setTag(R.string.Video_footage, Long.valueOf(System.currentTimeMillis()));
						if (btnRecorder.isEnabled()) {
							playTheAudioByPath(context, qickAdapter, audioPath, audioPosition, btnRecorder, MsgItem.AUDIO_REV, igvrevAudio);
						} else {
							if (playAudioPath.equals(audioPath)) {
								stopAudio();
								igvrevAudio.setImageResource(R.drawable.audio);
							} else {
								stopAudio();
								playTheAudioByPath(context, qickAdapter, audioPath, audioPosition, btnRecorder, MsgItem.AUDIO_REV, igvrevAudio);
							}
						}
					}
				}, new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initDelPopWindow(context, textViewRev, qickAdapter);
						return true;
					}
				}));

				avatarleft.setVisibility(View.VISIBLE);
				if (isGroup) {
					IMUser user = msgItem.getImMessage().getFromUserInfo();
					if (user != null) {
						LoadImageUtils.showAvatar(context, avatarleft, user.getAvatar(), R.drawable.ic_default_user);
					} else {
						avatarleft.setImageResource(R.drawable.ic_default_user);
					}
				} else {
					LoadImageUtils.showAvatar(context, avatarleft, toUser.getAvatar(), R.drawable.ic_default_user);
				}
			} break;
			case MsgItem.FILE_REV:
			case MsgItem.FILE_SEND: {
				msgItem = (MsgItem) item.get(MsgItem.FILE_REV);
				if (msgItem == null) {
					msgItem = (MsgItem) item.get(MsgItem.FILE_SEND);
				}
				message = msgItem.getImMessage();
				avatarleft = viewHolder.getView(R.id.txtFromAvatar);
				avatarright = viewHolder.getView(R.id.txtSendAvatar);
				final TextView txtFileStatus = viewHolder.getView(R.id.txtFileStatus);
				;
				if (message.getFrom().equals(owner.getId())) {//自己发的消息
					avatarleft.setVisibility(View.INVISIBLE);
					avatarright.setVisibility(View.VISIBLE);
					LoadImageUtils.showAvatar(context, avatarright, owner.getAvatar(), R.drawable.ic_default_user);
				} else {//收到的消息
					avatarleft.setVisibility(View.VISIBLE);
					avatarright.setVisibility(View.INVISIBLE);

					if (isGroup) {
						IMUser user = message.getFromUserInfo();
						LoadImageUtils.showAvatar(context, avatarleft, user.getAvatar(), R.drawable.ic_default_user);
					} else {
						LoadImageUtils.showAvatar(context, avatarleft, toUser.getAvatar(), R.drawable.ic_default_user);
					}
				}

				ImageView imageView = viewHolder.getView(R.id.igvIcon);
				TextView txtFileName = viewHolder.getView(R.id.txtFileName);
				View layoutFile = viewHolder.getView(R.id.layoutFile);
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.file);
				//imageView.
				if (itemViewType == MsgItem.FILE_SEND) {
					igvReSend = viewHolder.getView(R.id.igvReSend);
					pbLoading = viewHolder.getView(R.id.pbLoading);
				}

				final String igpath = message.getAttach().getBody();
				final String name = message.getAttach().getName();


				if (itemViewType == MsgItem.FILE_SEND) {
					String[] strs = message.getAttach().getLocalPath().split("/");
					if (strs != null && strs.length > 0) {
						txtFileName.setText(strs[strs.length - 1]);
					}
					//txtFileName.setText(name);
				} else {
					if (igpath.contains("/")) {
						txtFileName.setText(name);
					} else if (igpath.contains("\\")) {
						String[] strs = igpath.split("\\");
						if (strs != null && strs.length > 0) {
							txtFileName.setText(strs[strs.length - 1]);
						}
					}
				}
				final String localPath = message.getAttach().getLocalPath();
				final View fileLayout = layoutFile;
				fileLayout.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);
				layoutFile.setOnTouchListener(new ClickAndLongClickHandler(new View.OnClickListener() {
					public void onClick(View v) {
						if (itemViewType == MsgItem.FILE_SEND) {
							if (localPath != null && localPath.length() > 0) {
								Intent intent = Utils.openFile(context, localPath);
								context.startActivity(intent);
							} else {
								Intent intent = Utils.openFile(context, igpath);
								context.startActivity(intent);
							}
						} else {
							final String path = Utils.getLocalPath(name);
							File local = new File(path);
							if (local.exists()) {
								Intent intent = Utils.openFile(context, path);
								context.startActivity(intent);
							} else {
								txtFileStatus.setText(R.string.tip_file_download);
								Utils.saveFileFromHttp(igpath, path, new Utils.OnDownLoadListener() {

									public void onSucess() {
										context.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												txtFileStatus.setText(R.string.tip_file_downloaded);
												Intent intent = Utils.openFile(context, path);
												context.startActivity(intent);
											}
										});
									}

									public void onError(int status, String error) {
										context.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												txtFileStatus.setText(R.string.tip_file_downloadfail);
											}
										});

									}
								}, null);
							}
						}
					}
				}, new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initDelPopWindow(context, fileLayout, qickAdapter);
						return true;
					}
				}));
			} break;
			case MsgItem.IMAGE_REV:
			case MsgItem.IMAGE_SEND: {
				msgItem = (MsgItem) item.get(MsgItem.IMAGE_REV);
				if (msgItem == null) {
					msgItem = (MsgItem) item.get(MsgItem.IMAGE_SEND);
				}
				message = msgItem.getImMessage();
				avatarleft = viewHolder.getView(R.id.txtFromAvatar);
				avatarright = viewHolder.getView(R.id.txtSendAvatar);
				if (message.getFrom().equals(owner.getId())) {//自己发的消息
					avatarleft.setVisibility(View.INVISIBLE);
					avatarright.setVisibility(View.VISIBLE);
					LoadImageUtils.showAvatar(context, avatarright, owner.getAvatar(), R.drawable.ic_default_user);
				} else {//收到的消息
					avatarleft.setVisibility(View.VISIBLE);
					avatarright.setVisibility(View.INVISIBLE);
					if (isGroup) {
						IMUser user = message.getFromUserInfo();
						LoadImageUtils.showAvatar(context, avatarleft, user.getAvatar(), R.drawable.ic_default_user);
					} else {
						LoadImageUtils.showAvatar(context, avatarleft, toUser.getAvatar(), R.drawable.ic_default_user);
					}
				}
				if (itemViewType == MsgItem.IMAGE_SEND) {
					igvReSend = viewHolder.getView(R.id.igvReSend);
					pbLoading = viewHolder.getView(R.id.pbLoading);
				}

				ImageView imageView = viewHolder.getView(R.id.igvPreview);
				imageView.setVisibility(View.VISIBLE);

				String picpath = message.getAttach().getLocalPath();
				if (picpath != null && !TextUtils.isEmpty(picpath)) {
					Bitmap bitmap = LoadImageUtils.getBitmapByLocalPath(context, picpath, imageView.getLayoutParams().height);
					imageView.setImageBitmap(bitmap);
				} else {
					picpath = message.getAttach().getBody();
					Glide.with(context).load(picpath).placeholder(R.drawable.empty_photo).into(imageView);
				}
				final String igpathh = picpath;


				final ImageView imageViewText = imageView;

				imageViewText.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);

				imageView.setOnTouchListener(new ClickAndLongClickHandler(new View.OnClickListener() {
					public void onClick(View v) {
						showidx = attachpaths.indexOf(igpathh);
						showAttachDetailDialog(context);
					}
				}, new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initDelPopWindow(context, imageViewText, qickAdapter);
						return true;
					}
				}));
			} break;
			case MsgItem.LOCATION_REV: {
			} break;
			case MsgItem.VIDEO_REV:
			case MsgItem.VIDEO_SEND: {
				msgItem = (MsgItem) item.get(MsgItem.VIDEO_REV);
				if (msgItem == null) {
					msgItem = (MsgItem) item.get(MsgItem.VIDEO_SEND);
				}

				message = msgItem.getImMessage();

				avatarleft = viewHolder.getView(R.id.txtFromAvatar);
				avatarright = viewHolder.getView(R.id.txtSendAvatar);

				ImageView imageView = viewHolder.getView(R.id.igvPreview);
				imageView.setVisibility(View.VISIBLE);
				if (itemViewType == MsgItem.VIDEO_SEND) {
					igvReSend = viewHolder.getView(R.id.igvReSend);
					pbLoading = viewHolder.getView(R.id.pbLoading);
				}

				if (message.getFrom().equals(owner.getId())) {//自己发的消息
					avatarleft.setVisibility(View.INVISIBLE);
					avatarright.setVisibility(View.VISIBLE);
					LoadImageUtils.showAvatar(context, avatarright, owner.getAvatar(), R.drawable.ic_default_user);
					String pathvideo = message.getAttach().getLocalPath();
					if (pathvideo == null) {
						pathvideo = message.getAttach().getBody();
					}
					Bitmap videothumb = LoadImageUtils.getVideoThumbnailByLocalPath(context, pathvideo, 512, 384, MediaStore.Images.Thumbnails.MINI_KIND);
					imageView.setImageBitmap(videothumb);

				} else {//收到的消息
					avatarleft.setVisibility(View.VISIBLE);
					avatarright.setVisibility(View.INVISIBLE);
					if (isGroup) {
						IMUser user = message.getFromUserInfo();
						LoadImageUtils.showAvatar(context, avatarleft, user.getAvatar(), R.drawable.ic_default_user);
					} else {
						LoadImageUtils.showAvatar(context, avatarleft, toUser.getAvatar(), R.drawable.ic_default_user);
					}
					LoadImageUtils.loadIntoUseFitWidth(context, message.getAttach().getThumbnail(), R.drawable.empty_photo, imageView);
				}
				final PieView downloadPieView = viewHolder.getView(R.id.downloadPieView);
				final String igpaths = message.getAttach().getLocalPath();
				final String webpath = message.getAttach().getBody();
				final ImageView imageViewVideo = imageView;

				imageViewVideo.setTag(IDPOSISITION, msgItem.getImMessage().getId() + "-" + position);

				imageView.setOnTouchListener(new ClickAndLongClickHandler(new View.OnClickListener() {
					public void onClick(View v) {
						playOrDownload(context, igpaths, webpath, downloadPieView);
					}
				}, new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						initDelPopWindow(context, imageViewVideo, qickAdapter);
						return true;
					}
				}));

			} break;
			case MsgItem.TIME_SHOW: {
				msgItem = (MsgItem) item.get(MsgItem.TIME_SHOW);
				TextView txtTimeShow = viewHolder.getView(R.id.txtTime);
				txtTimeShow.setText(msgItem.getTip());
			} break;
			case MsgItem.WARN_DATA: {
				msgItem = (MsgItem) item.get(MsgItem.WARN_DATA);
				TextView txtTimeShow = viewHolder.getView(R.id.txtTime);
				int warnType = msgItem.getImMessage().getWarnData().getType();
				HashMap<String, Object> data = msgItem.getImMessage().getWarnData().getData();
				String tip = null;
				if (warnType == IMMessage.WarnData.WARN_CREATE_GROUP) {
					tip = context.getString(R.string.creategroupok);
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_MEMBER_JOINED) {
					IMUser user = (IMUser) data.get("user");
					if (user != null) {
						tip = context.getString(R.string.somejoingroup, user.getNickname());
					} else {
						tip = context.getString(R.string.somejoingroupchat);
					}

				} else if (warnType == IMMessage.WarnData.WARN_GROUP_NAME_CHANGED) {
					String newname = (String) data.get("name");
					IMUser user = (IMUser) data.get("user");

					if (user != null && newname != null) {
						tip = context.getString(R.string.changegroupname, user.getNickname(), newname);
					} else {
						tip = context.getString(R.string.groupnamechanged);
					}
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_NOTICE_CHANGED) {
					String notice = (String) data.get("notice");
					IMUser user = (IMUser) data.get("user");
					if (user != null) {
						tip = context.getString(R.string.somegroupnoticchange, user.getNickname(), notice);
					} else {
						tip = context.getString(R.string.groupnoticchange);
					}
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_OWNER_CHANGED) {
					IMUser user = (IMUser) data.get("newOwner");
					//IMUser user = (IMUser) data.get("oldOwner");
					if (user != null) {
						tip = context.getString(R.string.someisnewgroupowner, user.getNickname());
					}
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_REMOVED) {
					IMUser user = (IMUser) data.get("owner");
//					IMUser user = (String) data.get("owner");
					if (user != null) {
						tip = context.getString(R.string.groupareremoved, user.getNickname());
					}
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_MEMBER_INVITED) {
					List<IMUser> users = (List<IMUser>) data.get("users");
					IMUser inviter = (IMUser) data.get("inviter");
					if (inviter != null) {
						StringBuffer sbf = new StringBuffer();
						for (int i = 0; i < users.size(); i++) {
							sbf.append("" + users.get(i).getNickname());
							if (i != (users.size() - 1)) {
								sbf.append(",");
							}
						}
						tip = context.getString(R.string.invitejoingroup, inviter.getNickname(), sbf.toString());
					}
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_YOU_REMOVED) {
					tip = context.getString(R.string.youberomoved);
				} else if (warnType == IMMessage.WarnData.WARN_GROUP_MEMBER_OUT) {
					IMUser user = (IMUser) data.get("user");
					tip = context.getString(R.string.someleavegroup, user.getNickname());
				}
				txtTimeShow.setText(tip);
			} break;
		}
		if (igvReSend != null && pbLoading != null) {
			final IMMessage imMessage = msgItem.getImMessage();
//				final MsgItem msgText = msgItem;
			if (msgItem.getImMessage().getStatus() == IMMessage.STATUS_FAILED) {
				pbLoading.setVisibility(View.GONE);
				igvReSend.setVisibility(View.VISIBLE);
				if (igvReSend != null && igvReSend.getVisibility() == View.VISIBLE) {
					igvReSend.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							reSendNow(context, imMessage, qickAdapter, position, itemViewType);
						}
					});
				}
			} else if (msgItem.getImMessage().getStatus() == IMMessage.STATUS_SEND_ING) {
				igvReSend.setVisibility(View.GONE);
				pbLoading.setVisibility(View.VISIBLE);
			} else {
				igvReSend.setVisibility(View.GONE);
				pbLoading.setVisibility(View.GONE);
			}
		}
		if (avatarleft != null) {
			final IMMessage msg = message;
			if (!isGroup) {
				avatarleft.setOnClickListener(avatarClick);
			} else {
				avatarleft.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						if (msg.getFromUserInfo() != null) {
							UserDetailsActivity.gotoUserDetailsPage(context, msg.getFromUserInfo());
						}
					}
				});
			}
		}

		if (avatarright != null) {
			avatarright.setOnClickListener(avatarClick);
		}
	}

	private static void reSendNow(final Activity context, final IMMessage imMessage,
				final QuickAdapter<SparseArray<Object>> quickAdapter, final int position, final int itemViewType) {
		updateItemMessageStatus(context, imMessage, quickAdapter, position, itemViewType, IMMessage.STATUS_SEND_ING);
		MobIM.getChatManager().sendMessage(imMessage, new MobIMCallback<Void>() {
			public void onSuccess(Void result) {
				updateItemMessageStatus(context, imMessage, quickAdapter, position, itemViewType, IMMessage.STATUS_SUCCESS);
			}

			public void onError(int code, String message) {
				updateItemMessageStatus(context, imMessage, quickAdapter, position, itemViewType, IMMessage.STATUS_FAILED);
			}
		});
	}

	private static void updateItemMessageStatus(Activity context, IMMessage imMessage, final QuickAdapter<SparseArray<Object>> quickAdapter,
				final int position, int itemViewType, int status) {
		imMessage.setStatus(status);
		final SparseArray<Object> item = quickAdapter.getItem(position);
		MsgItem msgItem = (MsgItem) item.get(itemViewType);
		msgItem.setImMessage(imMessage);
		item.put(itemViewType, msgItem);
		context.runOnUiThread(new Runnable() {
			public void run() {
				quickAdapter.setData(position, item);
			}
		});
	}

	private static void playAudio(Context context, String path, final int idx, final Button btnRecorder) {
		if (player == null) {
			btnRecorder.setEnabled(false);
			try {
				player = MediaPlayer.create(context, Uri.parse(path));
				//player.prepare();
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						btnRecorder.setEnabled(true);
						player = null;
						if (lastAudioView != null) {
							if (lastAudioType == MsgItem.AUDIO_REV) {
								lastAudioView.setImageResource(R.drawable.audio);
							} else {
								lastAudioView.setImageResource(R.drawable.audio_send);
							}
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (player != null) {
				player.start();
			} else {
				Toast.makeText(context, R.string.tip_play_fail, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static void initPopWindow(final Context mContext, final View v, final QuickAdapter<SparseArray<Object>> quickAdapter) {
		final View view = LayoutInflater.from(mContext).inflate(R.layout.item_popup, null, false);

		//1.构造一个PopupWindow，参数依次是加载的View，宽高
		final PopupWindow popWindow = new PopupWindow(view,
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		popWindow.setAnimationStyle(R.anim.anim_pop);  //设置加载动画
		//这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
		//代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
		//PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
		popWindow.setTouchable(true);
		popWindow.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
				// 这里如果返回true的话，touch事件将被拦截
				// 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
			}
		});

		popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //要为popWindow设置一个背景才有效
		int width = v.getWidth();
		width = width / 3;
		int height = v.getHeight();

		int myheight = popWindow.getContentView().getMeasuredHeight() + 8;
		//设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
		popWindow.showAsDropDown(v, -10 + width, -height - myheight - 6);
		Button btnCopy = (Button) view.findViewById(R.id.btnCopy);
		Button btnDel = (Button) view.findViewById(R.id.btnDel);

		//设置popupWindow里的按钮的事件
		btnCopy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
//				Toast.makeText(mContext, "你点击了Copy~", Toast.LENGTH_SHORT).show();
				if (view instanceof TextView) {
					String value = ((TextView) v).getText().toString();
					ClipboardManager clipboard = (ClipboardManager)
							v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("copy", value);
					clipboard.setPrimaryClip(clip);
					Toast.makeText(v.getContext(), R.string.copynow, Toast.LENGTH_SHORT).show();
				}
				popWindow.dismiss();
			}
		});

		btnDel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String value = (String) (v).getTag(IDPOSISITION);
				String[] sbr = value.split("-");
				MobIM.getChatManager().delMessage(sbr[0]);
				quickAdapter.removeItem(Integer.parseInt(sbr[1]));
				popWindow.dismiss();
			}
		});
	}

	private static void initDelPopWindow(final Context mContext, final View v, final QuickAdapter<SparseArray<Object>> quickAdapter) {
		final View view = LayoutInflater.from(mContext).inflate(R.layout.item_popup, null, false);

		//1.构造一个PopupWindow，参数依次是加载的View，宽高
		final PopupWindow popWindow = new PopupWindow(view,
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		popWindow.setAnimationStyle(R.anim.anim_pop);  //设置加载动画
		//这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
		//代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
		//PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
		popWindow.setTouchable(true);
		popWindow.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
				// 这里如果返回true的话，touch事件将被拦截
				// 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
			}
		});

		popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //要为popWindow设置一个背景才有效
		int width = v.getWidth();
		width = width / 2;
		int height = v.getHeight();

		int myheight = popWindow.getContentView().getMeasuredHeight() + 8;
		//设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
		popWindow.showAsDropDown(v, width, -height - myheight - 6);
		Button btnCopy = (Button) view.findViewById(R.id.btnCopy);
		Button btnDel = (Button) view.findViewById(R.id.btnDel);
		btnDel.setVisibility(View.GONE);
		btnCopy.setText(R.string.del);
		//设置popupWindow里的按钮的事件
		btnCopy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String value = (String) (v).getTag(IDPOSISITION);
				String[] sbr = value.split("-");

				MobIM.getChatManager().delMessage(sbr[0]);
				quickAdapter.removeItem(Integer.parseInt(sbr[1]));

				popWindow.dismiss();
			}
		});
	}

	private static void playTheAudioByPath(Context context, QuickAdapter<SparseArray<Object>> qickAdapter,
										String path, int idx, Button btnRecorder, int msgType, ImageView igvAudio) {
		playAudioPath = path;
		lastAudioView = igvAudio;
		lastAudioType = msgType;
		if (msgType == MsgItem.AUDIO_REV) {
			igvAudio.setImageResource(R.drawable.anim_playrevaudio);
		} else {
			igvAudio.setImageResource(R.drawable.anim_playsendaudio);
		}
		AnimationDrawable sendAudio = (AnimationDrawable) igvAudio.getDrawable();
		sendAudio.start();
		playAudio(context, path, idx, btnRecorder);
		SparseArray<Object> itemnow = qickAdapter.getItem(idx);
		MsgItem msgItem = (MsgItem) itemnow.get(msgType);
		if (msgItem != null) {
			msgItem.setRead(true);
			MobIM.getChatManager().markAudioAsPlayed(msgItem.getImMessage().getId());
			itemnow.put(msgType, msgItem);
			qickAdapter.setData(idx, itemnow);
		}
	}

	private static void stopAudio() {
		if (player == null) {
			return;
		}
		player.stop();
		player.release();
		//btnRecorder.setEnabled(true);
		player = null;
		if (lastAudioView != null) {
			if (lastAudioType == MsgItem.AUDIO_REV) {
				lastAudioView.setImageResource(R.drawable.audio);
			} else {
				lastAudioView.setImageResource(R.drawable.audio_send);
			}
		}
	}

	private static void playOrDownload(final Activity context, String igpaths, String webpath, final PieView downloadPieView) {
		boolean play = false;
		if (igpaths != null && !TextUtils.isEmpty(igpaths)) {
			File videoFile = new File(igpaths);
			if (videoFile.exists()) {
				play = true;
				Intent intent = Utils.openFile(context, igpaths);
				context.startActivity(intent);
			}
		}
		if (!play) {
			final String path = Utils.getDownloadPath(webpath);
			File file = new File(path);
			if (!file.exists()) {
				Utils.saveFileFromHttp(webpath, path, new Utils.OnDownLoadListener() {

					public void onSucess() {
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Intent intent = Utils.openFile(context, path);
								context.startActivity(intent);
							}
						});
					}


					public void onError(int status, String error) {

					}
				}, new Utils.DownloadProgess() {
					public void progress(final float progess) {
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								downloadPieView.setPercentage(progess);
								if (progess >= 100) {
									downloadPieView.setVisibility(View.GONE);
								}
							}
						});

					}
				});
				downloadPieView.setMaxPercentage(100);
				downloadPieView.setVisibility(View.VISIBLE);
				downloadPieView.setInnerText(context.getString(R.string.downloading));

			} else {
				Intent intent = Utils.openFile(context, path);
				context.startActivity(intent);
			}
		}
	}

	private static void showAttachDetailDialog(final Activity context) {
		final Dialog dialog = new Dialog(context, R.style.ActivityDialogStyle);

		LayoutInflater inflater = LayoutInflater.from(context);
		View viewDialog = inflater.inflate(R.layout.attach_detail, null);
		//Display display = getActivity().getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		final LinearLayout layoutBottom = (LinearLayout) viewDialog.findViewById(R.id.layoutBottom);
		final ImageView igvPlay = (ImageView) viewDialog.findViewById(R.id.igvPlay);
		TextView txtSave = (TextView) viewDialog.findViewById(R.id.txtSave);
		txtSave.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String imagepath = attachpaths.get(showidx);
				String camerapath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
				File src = new File(imagepath);
				String name = src.getName();
				String dest = camerapath + File.separator + name;
				if (imagepath.toLowerCase().startsWith("http") || imagepath.toLowerCase().startsWith("ftp")) {
					Utils.saveFileFromHttp(imagepath, dest, new Utils.OnDownLoadListener() {
						@Override
						public void onSucess() {
							UIHandler.sendEmptyMessage(0, new Handler.Callback() {
								@Override
								public boolean handleMessage(Message msg) {
									Toast.makeText(context, R.string.saveok, Toast.LENGTH_SHORT).show();
									return false;
								}
							});
						}

						@Override
						public void onError(int status, String error) {
							UIHandler.sendEmptyMessage(0, new Handler.Callback() {
								@Override
								public boolean handleMessage(Message msg) {
									Toast.makeText(context, R.string.savefail, Toast.LENGTH_SHORT).show();
									return false;
								}
							});
						}
					}, null);
				} else {
					try {
						File destfile = new File(dest);
						if (!destfile.exists()) {
							Utils.forTransfer(src, destfile);
						}
						Toast.makeText(context, R.string.saveok, Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(context, R.string.savefail, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		TextView txtCancel = (TextView) viewDialog.findViewById(R.id.txtCancel);
		txtCancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				layoutBottom.setVisibility(View.GONE);
			}
		});

		int width = dm.widthPixels;
		int height = dm.heightPixels;

		final ImageView igvDetail = (ImageView) viewDialog.findViewById(R.id.igvDetial);
		final PieView downloadPieView = (PieView) viewDialog.findViewById(R.id.downloadPieView);
		igvDetail.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				fromLong = true;
				layoutBottom.setVisibility(View.VISIBLE);
				return false;
			}
		});

		igvDetail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - clicktime < 700) {
					dialog.dismiss();
				}
				clicktime = System.currentTimeMillis();
				if (!fromLong) {
					layoutBottom.setVisibility(View.GONE);
				}
			}
		});

		igvPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = attachpaths.get(showidx);
				String igpaths = null;
				String webpath = null;
				if (path.startsWith("http")) {
					webpath = path;
				} else {
					igpaths = path;
				}
				playOrDownload(context, igpaths, webpath, downloadPieView);

			}
		});
		igvDetail.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				//继承了Activity的onTouchEvent方法，直接监听点击事件
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					//当手指按下的时候
					x1 = event.getX();
					y1 = event.getY();
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					//当手指离开的时候
					x2 = event.getX();
					y2 = event.getY();
					if (y1 - y2 > 50) {
						//	Toast.makeText(MainActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
					} else if (y2 - y1 > 50) {
						//	Toast.makeText(MainActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
					} else if (x1 - x2 > 50) {
						//	Toast.makeText(MainActivity.this, "向左滑", Toast.LENGTH_SHORT).show()
						int idx = showidx + 1;
						if (idx <= attachpaths.size() - 1) {
							showidx = idx;
							String path = attachpaths.get(showidx);
							if (path.startsWith("http")) {
								LoadImageUtils.showAvatar(context, igvDetail, path, R.drawable.empty_photo);
							} else {
//								Bitmap bitmap = BitmapFactory.decodeFile(path);
//								igvDetail.setImageBitmap(bitmap);
								LoadImageUtils.loadImageViewTo(context, igvDetail, path, R.drawable.empty_photo);
							}
							if (path.endsWith(".3gp")) {
								igvPlay.setVisibility(View.VISIBLE);
							} else {
								igvPlay.setVisibility(View.GONE);
							}
						} else {
							Toast.makeText(context, R.string.nomore, Toast.LENGTH_SHORT).show();
						}
					} else if (x2 - x1 > 50) {
						//	Toast.makeText(MainActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
						int idx = showidx - 1;
						if (idx >= 0) {
							showidx = idx;
							String path = attachpaths.get(showidx);
							if (path.startsWith("http")) {
								LoadImageUtils.showAvatar(context, igvDetail, path, R.drawable.empty_photo);
							} else {
//								Bitmap bitmap = BitmapFactory.decodeFile(path);
//								igvDetail.setImageBitmap(bitmap);
								LoadImageUtils.loadImageViewTo(context, igvDetail, path, R.drawable.empty_photo);
							}
							if (path.endsWith(".3gp")) {
								igvPlay.setVisibility(View.VISIBLE);
							} else {
								igvPlay.setVisibility(View.GONE);
							}
						} else {
							Toast.makeText(context, R.string.nomore, Toast.LENGTH_SHORT).show();
						}
					}
				}
				return false;
			}
		});
		String path = attachpaths.get(showidx);
		if (path.toLowerCase().startsWith("http")) {
			LoadImageUtils.showAvatar(context, igvDetail, path, R.drawable.empty_photo);
		} else {
			LoadImageUtils.loadImageViewTo(context, igvDetail, path, R.drawable.empty_photo);
		}
		//设置dialog的宽高为屏幕的宽高
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height - Utils.getStatusBarHeight(context));
		dialog.setContentView(viewDialog, layoutParams);
		dialog.show();
	}

	private static class ClickAndLongClickHandler implements View.OnTouchListener {
		private View.OnLongClickListener longClickListener;
		private View.OnClickListener clickListener;
		private int startX;
		private int startY;
		private long startTime;
		private Handler handler;
		private long pressreange = 600;
		private int clickrange = 20;

		public ClickAndLongClickHandler(View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
			this.longClickListener = longClickListener;
			this.clickListener = clickListener;
			handler = new Handler();
		}

		private boolean hasView(int x, int y) {
			return true;
		}

		private void moveView(int x, int y) {

		}

		public boolean onTouch(final View touchView, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					//touchView = null;
					startX = (int) event.getX();
					startY = (int) event.getY();
					startTime = System.currentTimeMillis();
					/* 长按操作 */
					handler.postDelayed(new Runnable() {

						public void run() {
							//longPressedAlertDialog();
							longClickListener.onLongClick(touchView);
						}
					}, pressreange);
				} break;
				case MotionEvent.ACTION_MOVE: {
					int lastX = (int) event.getX();
					int lastY = (int) event.getY();
					//移动
					moveView(lastX, lastY);
					if (Math.abs(lastX - startX) > clickrange || Math.abs(lastY - startY) > clickrange) {
						this.handler.removeCallbacksAndMessages(null);
					}
				} break;
				case MotionEvent.ACTION_UP: {
					this.handler.removeCallbacksAndMessages(null);
					if (System.currentTimeMillis() - startTime < pressreange) {
						clickListener.onClick(touchView);
					}
				} break;
			}
			return true;
		}
	}

	public static void setMsgStatusView(int status, QuickAdapter<SparseArray<Object>> qickAdapter, int insertIdx) {
		SparseArray<Object> objectSparseNow = qickAdapter.getItem(insertIdx);
		int msgItemType = ChatUtils.getMsgItemType(objectSparseNow);
		MsgItem msgItem = (MsgItem) objectSparseNow.get(msgItemType);
		msgItem.getImMessage().setStatus(status);
		objectSparseNow.clear();
		objectSparseNow.append(msgItemType, msgItem);
		qickAdapter.setData(insertIdx, objectSparseNow);
	}

}
