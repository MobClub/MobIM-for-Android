package com.mob.demo.mobim.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.mob.MobSDK;
import com.mob.demo.mobim.R;
import com.mob.demo.mobim.emoji.SmileUtils;
import com.mob.demo.mobim.ui.ImagePickerActivity;
import com.mob.tools.utils.ResHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	private final static String TAG = "Utils";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm");
	private static SimpleDateFormat todaysdf = new SimpleDateFormat("HH:mm");
	private static String localUserID = null;
	private static final String DATA = "data";
	private static final String KEYLOCALUSERID = "localuserid";

	public static String getTimeShowStr(Context context, long milensseconds) {
		Date date = new Date(milensseconds);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		calendar.set(Calendar.HOUR_OF_DAY, 24);
		calendar.set(Calendar.MINUTE, 0);
		Date yesday = calendar.getTime();
//		Utils.showLog(TAG," formate data : "+sdf.format(yesday));
		long yesdayTime = yesday.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date yesyesday = calendar.getTime();
//		Utils.showLog(TAG," formate data : "+sdf.format(yesyesday));
		long yesyesdayTime = yesyesday.getTime();

		if (milensseconds > yesdayTime) {
			return todaysdf.format(date);
		}

		if (milensseconds > yesyesdayTime) {
			String tip = context.getString(R.string.yesdaytime, todaysdf.format(date));
			return tip;
		}

		return sdf.format(date);
	}

	public static interface DownloadProgess {
		public void progress(float progess);
	}

	public static String getDownloadPath(String url) {
		String path = Environment.getExternalStorageDirectory() + "/" + "mobim";

		String[] f = url.split("/");
		String name = f[f.length - 1];
		path = path + "/" + name;
		File file = new File(path);
		String parent = file.getParent();
		File dir = new File(parent);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		//file.mkdir();
		//Utils.showLog("Utils"," ===== path >> "+path);
		return path;
	}

	public static String getDownloadPath(String url, String name) {
		String path = Environment.getExternalStorageDirectory() + "/" + "mobim";

		String[] f = url.split("/");
		//String name = f[f.length-1];
		path = path + "/" + name;
		File file = new File(path);
		String parent = file.getParent();
		File dir = new File(parent);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		//file.mkdir();
		//Utils.showLog("Utils"," ===== path >> "+path);
		return path;
	}

	public static String getLocalPath(String name) {
		String path = Environment.getExternalStorageDirectory() + "/" + "mobim";
		//String[] f = url.split("/");
		//String name = f[f.length-1];
		path = path + "/" + name;
		File file = new File(path);
		String parent = file.getParent();
		File dir = new File(parent);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		//file.mkdir();
		//Utils.showLog("Utils"," ===== path >> "+path);
		return path;
	}

	public static String getFileFromLocal(String igpath) {
		return getDownloadPath(igpath);
	}

	public static String getFileFromLocal(String igpath, String name) {
		return getDownloadPath(igpath);
	}

	public static interface OnGetDurationListener {
		public void getGetDuration(int duration);
	}

	public static void getMediaDuration(String path, final OnGetDurationListener getDurationListener) {
		MediaPlayer player = new MediaPlayer();
		try {
			player.setDataSource(path);  //recordingFilePath（）为音频文件的路径
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					if (getDurationListener != null) {
						getDurationListener.getGetDuration(mp.getDuration());
					}
					mp.release();//记得释放资源
				}
			});
			player.prepareAsync();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 *
	 * @param videoPath 视频的路径
	 * @param width     指定输出视频缩略图的宽度
	 * @param height    指定输出视频缩略图的高度度
	 * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}


	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static void showLog(String tag, String infor) {
//		Log.e(tag, infor);
	}

	public static interface OnDownLoadListener {
		public void onSucess();

		public void onError(int status, String error);
	}

	public static String getAudioPath(Context context) {
		File file = context.getExternalCacheDir();
		String path = file.getAbsolutePath() + File.separator + System.nanoTime() + ".amr";
		return path;
	}

	private static String getValueFromKey(Context context, String key) {
		if (context != null) {
			return context.getSharedPreferences(DATA, 0).getString(key, "");
		}
		return null;
	}

	private static void saveKeyAndValue(Context context, String key, String value) {
		if (context != null) {
			context.getSharedPreferences(DATA, 0).edit().putString(key, value).commit();
		}
	}

	public static void saveLocalUserID(Context context, String localUserID) {
		Utils.localUserID = localUserID;
		saveKeyAndValue(context, KEYLOCALUSERID, localUserID);
	}

	public static String getLocalUserID(Context context) {
		if (localUserID == null) {
			if (context != null) {
				localUserID = getValueFromKey(context, KEYLOCALUSERID);
				if (TextUtils.isEmpty(localUserID)) {
					localUserID = null;
				}
				return localUserID;
			}
		}
		return null;
	}

	public static String getRealFilePath(Context context, final Uri uri) {
		if (null == uri) {
			return null;
		}
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}

				}
				cursor.close();
			}
			if (data == null) {
				data = getImageAbsolutePath(context, uri);
			}
		}
		return data;
	}

	/**
	 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
	 *
	 * @param context
	 * @param imageUri
	 * @author yaoxing
	 * @date 2014-10-12
	 */
	@TargetApi(19)
	public static String getImageAbsolutePath(Context context, Uri imageUri) {
		if (context == null || imageUri == null) {
			return null;
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[]{split[1]};
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// MediaStore (and general)
			// Return the remote address
			if (isGooglePhotosUri(imageUri)) {
				return imageUri.getLastPathSegment();
			}
			return getDataColumn(context, imageUri, null, null);
		} else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = {column};
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	public static int getStatusBarHeight(Activity activity) {
		/**
		 * 获取状态栏高度——方法
		 * 状态栏高度 = 屏幕高度 - 应用区高度
		 * *注意*该方法不能在初始化的时候用
		 * */
		//屏幕
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		//应用区域
		Rect outRect1 = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
		int statusBar = dm.heightPixels - outRect1.height();  //状态栏高度=屏幕高度-应用区域高度
		return statusBar;
	}

	public static String getLocalUserID() {
		return localUserID;
	}

	@SuppressLint("NewApi")
	public static void enableStrictMode() {
		if (Utils.hasGingerbread()) {
			StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
					new StrictMode.ThreadPolicy.Builder()
							.detectAll()
							.penaltyLog();
			StrictMode.VmPolicy.Builder vmPolicyBuilder =
					new StrictMode.VmPolicy.Builder()
							.detectAll()
							.penaltyLog();

			if (Utils.hasHoneycomb()) {
				threadPolicyBuilder.penaltyFlashScreen();
				vmPolicyBuilder
						.setClassInstanceLimit(ImagePickerActivity.class, 1);
			}
			StrictMode.setThreadPolicy(threadPolicyBuilder.build());
			StrictMode.setVmPolicy(vmPolicyBuilder.build());
		}
	}

	public static boolean hasFroyo() {
		//return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
		return Build.VERSION.SDK_INT >= 8;
	}

	public static boolean hasGingerbread() {
//		return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
		return Build.VERSION.SDK_INT >= 9;
	}

	public static boolean hasHoneycomb() {
//		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
		return Build.VERSION.SDK_INT >= 11;
	}

	public static boolean hasHoneycombMR1() {
//		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
		return Build.VERSION.SDK_INT >= 12;
	}

	public static boolean hasJellyBean() {
//		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
		return Build.VERSION.SDK_INT >= 16;
	}

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= 19;
	}

	public static List<Camera.Size> getResolutionList(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		return parameters.getSupportedPreviewSizes();
	}

	public static class ResolutionComparator implements Comparator<Camera.Size> {
		public int compare(Camera.Size lhs, Camera.Size rhs) {
			if (lhs.height != rhs.height) {
				return lhs.height - rhs.height;
			} else {
				return lhs.width - rhs.width;
			}
		}
	}

	public static String toTime(int var0) {
		var0 /= 1000;
		int var1 = var0 / 60;
		boolean var2 = false;
		if (var1 >= 60) {
			int var4 = var1 / 60;
			var1 %= 60;
		}
		int var3 = var0 % 60;
		return String.format("%02d:%02d", new Object[]{Integer.valueOf(var1), Integer.valueOf(var3)});
	}

	public static String getDataSize(long var0) {
		DecimalFormat var2 = new DecimalFormat("###.00");
		return var0 < 0L ? "error" : ( var0 < 1024L ? var0 + "bytes" : ( var0 < 1048576L
				 ? var2.format( (double) ((float) var0 / 1024.0F ) ) + "KB" : ( var0 < 1073741824L
				 ? var2.format( (double) ((float) var0 / 1024.0F / 1024.0F ) ) + "MB"
				 : var2.format( (double) ((float) var0 / 1024.0F / 1024.0F / 1024.0F ) ) + "GB")) );
	}

	/**
	 * copy f1 到 f2 文件目录里。
	 *
	 * @param f1
	 * @param f2
	 * @return
	 * @throws Exception
	 */
	public static long forTransfer(File f1, File f2) throws Exception {
		long time = new Date().getTime();
		int length = 2097152;
		FileInputStream in = new FileInputStream(f1);
		FileOutputStream out = new FileOutputStream(f2);
		FileChannel inC = in.getChannel();
		FileChannel outC = out.getChannel();
		int i = 0;
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				return new Date().getTime() - time;
			}
			if ((inC.size() - inC.position()) < 20971520) {
				length = (int) (inC.size() - inC.position());
			} else {
				length = 20971520;
			}
			inC.transferTo(inC.position(), length, outC);
			inC.position(inC.position() + length);
			i++;
		}
	}

	public static void saveFileFromHttp(final String url, final String path,
										final OnDownLoadListener downLoadListener, final DownloadProgess downloadProgess) {
//		Utils.showLog("Utils"," ==== the url is >> "+url+" path is >>"+path);
		File file = new File(path);
		if (!file.exists()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int status = downlaodFile(url, path, downloadProgess);
					if (status < 0) {
						downLoadListener.onError(status, "");
					} else {
						downLoadListener.onSucess();
					}
				}
			}).start();
		} else {
			if (downLoadListener != null) {
				downLoadListener.onSucess();
			}
		}
	}

	/**
	 * 读取任何文件
	 * 返回-1 ，代表下载失败。返回0，代表成功。返回1代表文件已经存在
	 *
	 * @param urlStr
	 * @param path
	 * @return
	 */
	private static int downlaodFile(String urlStr, String path, DownloadProgess downloadProgess) {
		InputStream input = null;
		try {
			File file = new File(path);
			String fileName = file.getName();
			path = file.getParentFile().getAbsolutePath();
			FileUtil fileUtil = new FileUtil();
			if (fileUtil.isFileExist(path)) {
				return 1;
			} else {
				//input = getInputStearmFormUrl(urlStr);
				URL url = new URL(urlStr);
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				input = urlConn.getInputStream();
				int contentLength = urlConn.getContentLength();
				//File resultFile = fileUtil.write2SDFromInput(path,fileName,input);
				String parent = file.getParent();

				File dir = new File(parent);
				dir.mkdirs();

				input.available();
				OutputStream output = null;
				try {
					output = new FileOutputStream(file);
					byte[] buffer = new byte[4 * 1024];
					int total = 0;
					int sum = 0;
					int num = 0;
					while ((num = input.read(buffer)) != -1) {
						output.write(buffer, 0, num);
						sum = sum + num;
						total = (int) (((float) sum / (float) contentLength) * 100);
						if (downloadProgess != null) {
							downloadProgess.progress(total);
						}
					}
					output.flush();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (output != null) {
							output.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}


				if (!file.exists()) {
					return -1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}


	private static InputStream getInputStearmFormUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		InputStream input = urlConn.getInputStream();
		return input;
	}

	public static Intent openFile(Context context, String filePath) {
//		Utils.showLog(TAG," === filePath >>> "+filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		/* 取得扩展名 */
		String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase();
		/* 依扩展名的类型决定MimeType */
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				 || end.equals("xmf") || end.equals("ogg") || end.equals("wav") ) {
			return getAudioFileIntent(context, filePath);
		} else if (end.equals("3gp") || end.equals("mp4")) {
			return getAudioFileIntent(context, filePath);
		} else if ( end.equals("jpg") || end.equals("gif") || end.equals("png")
				 || end.equals("jpeg") || end.equals("bmp") ) {
			return getImageFileIntent(context, filePath);
		} else if ( end.equals("apk") ) {
			return getApkFileIntent(context, filePath);
		} else if ( end.equals("ppt") ) {
			return getPptFileIntent(context, filePath);
		} else if ( end.equals("xls") ) {
			return getExcelFileIntent(context, filePath);
		} else if ( end.equals("doc") || end.equals("docx") ) {
			return getWordFileIntent(context, filePath);
		} else if ( end.equals("pdf") ) {
			return getPdfFileIntent(context, filePath);
		} else if ( end.equals("chm") ) {
			return getChmFileIntent(context, filePath);
		} else if ( end.equals("txt") ) {
			return getTextFileIntent(context, filePath, false);
		} else {
			return getAllIntent(context, filePath);
		}
	}

	//Android获取一个用于打开APK文件的intent
	public static Intent getAllIntent(Context context, String param) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "*/*");
		return intent;
	}

	//Android获取一个用于打开APK文件的intent
	public static Intent getApkFileIntent(Context context, String param) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}

	//Android获取一个用于打开VIDEO文件的intent
	public static Intent getVideoFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	//Android获取一个用于打开AUDIO文件的intent
	public static Intent getAudioFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	//Android获取一个用于打开Html文件的intent
	public static Intent getHtmlFileIntent(Context context, String param) {
		Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	//Android获取一个用于打开图片文件的intent
	public static Intent getImageFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	//Android获取一个用于打开PPT文件的intent
	public static Intent getPptFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	//Android获取一个用于打开Excel文件的intent
	public static Intent getExcelFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	//Android获取一个用于打开Word文件的intent
	public static Intent getWordFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	//Android获取一个用于打开CHM文件的intent
	public static Intent getChmFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	//Android获取一个用于打开文本文件的intent
	public static Intent getTextFileIntent(Context context, String param, boolean paramBoolean) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (paramBoolean) {
			Uri uri1 = Uri.parse(param);
			intent.setDataAndType(uri1, "text/plain");
		} else {
			Uri uri2 = Utils.getUriForFile(context, new File(param));
			intent.setDataAndType(uri2, "text/plain");
		}
		return intent;
	}

	//Android获取一个用于打开PDF文件的intent
	public static Intent getPdfFileIntent(Context context, String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Utils.getUriForFile(context, new File(param));
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	private static SpannableString changeStrWithEmoji(Context context, String input) {
		//1.匹配字符串input中“[xxx]”
		Pattern pattern = Pattern.compile("\\[[^\\[\\]]*\\]");
		//2.获取匹配器
		Matcher matcher = pattern.matcher(input);
		//3.创建SP
		SpannableString sp = new SpannableString(input);
		//4.依次找到字符串input匹配到的子字符串
		while (matcher.find()) {
			//5.获取匹配到的子字符串[emoxxx]
			String group = matcher.group();
			//6.在该字符串开始的位置 与结束的位置
			int start = matcher.start();
			int end = matcher.end();
			//Log.d(TAG, group);
			//7.获取 R.mipmap.class 实例
			Class<R.mipmap> mipmapClass = R.mipmap.class;
			try {
				//8.去掉[emoxxx] 中括号查找 定义的变量
				Field field = mipmapClass.getDeclaredField(group.substring(1, group.length() - 1));
				//9 R.mipmap.class 定义的变量都是 static的 可直接获取
				int rid = field.getInt(null);

				//	Log.d(TAG, rid + "");
				//10.获取该[emoxxx]对应的emo Drawable
				Drawable drawable = context.getResources().getDrawable(rid);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				//11.在指定的位置设置该 ImageSpan即可
				sp.setSpan(new ImageSpan(drawable), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//12.显示出来
		//tv.setText(sp);
		return sp;
	}

	public static SpannableString changeStrToWithEmoji(Context context, String input) {
		//1.匹配字符串input中“[xxx]”
		Pattern pattern = Pattern.compile("\\[[^\\[\\]]*\\]");
		//2.获取匹配器
		Matcher matcher = pattern.matcher(input);
		//3.创建SP
		SpannableString sp = new SpannableString(input);
		//4.依次找到字符串input匹配到的子字符串
		while (matcher.find()) {
			//5.获取匹配到的子字符串[emoxxx]
			String group = matcher.group();
			//6.在该字符串开始的位置 与结束的位置
			int start = matcher.start();
			int end = matcher.end();
			//Log.d(TAG, group);
			Utils.showLog("changeStrToWithEmoji", " group >> " + group);

			try {
				Spannable span = null;
				if (SmileUtils.containsKey(group)) {
					//	Utils.showLog("changeStrToWithEmoji"," contains key ");
					//10.获取对应的emo Drawable
					Object icon = SmileUtils.getIcon(group);
					if (icon != null) {
						//	Utils.showLog("changeStrToWithEmoji"," icon not null ");
						try {
							int res = (Integer) icon;
							//		Utils.showLog("changeStrToWithEmoji"," res >> "+res);
							Bitmap bp = BitmapFactory.decodeResource(context.getResources(), res);
							ImageSpan imageSpan = new ImageSpan(context, bp);
							//11.在指定的位置设置该 Spannable即可
							sp.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						} catch (Exception ex) {
							ex.printStackTrace();
							String res = (String) icon;
							Bitmap bp = BitmapFactory.decodeFile(res);
							ImageSpan imageSpan = new ImageSpan(context, bp);
							//11.在指定的位置设置该 Spannable即可
							sp.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						}
					} else {
						//	Utils.showLog("changeStrToWithEmoji"," icon is null ");
					}
					//span = SmileUtils.getSmiledText(context,group);
				} else {
					Utils.showLog("changeStrToWithEmoji", " not contains key ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//12.显示出来
		//tv.setText(sp);
		return sp;
	}

	public static Uri getUriForFile(Context context, File file) {
		if (context == null || file == null) {
			throw new NullPointerException();
		}
		Uri uri;
		if (Build.VERSION.SDK_INT >= 24) {
			uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.mob.demo.mobim.fileprovider", file);
		} else {
			uri = Uri.fromFile(file);
		}
		return uri;
	}

	/**
	 * 计算gridview高度
	 *
	 * @param gridView
	 */
	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		// 获取GridView对应的Adapter
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int rows;
		int columns = 0;
		int horizontalBorderHeight = 0;
		Class<?> clazz = gridView.getClass();
		try {
			// 利用反射，取得每行显示的个数
			Field column = clazz.getDeclaredField("mRequestedNumColumns");
			column.setAccessible(true);
			columns = (Integer) column.get(gridView);
			// 利用反射，取得横向分割线高度
			Field horizontalSpacing = clazz
					.getDeclaredField("mRequestedHorizontalSpacing");
			horizontalSpacing.setAccessible(true);
			horizontalBorderHeight = (Integer) horizontalSpacing.get(gridView);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// 判断数据总数除以每行个数是否整除。不能整除代表有多余，需要加一行
		if (listAdapter.getCount() % columns > 0) {
			rows = listAdapter.getCount() / columns + 1;
		} else {
			rows = listAdapter.getCount() / columns;
		}
		Utils.showLog(TAG, " ======== rows >>>" + rows);
		int totalHeight = 0;
		for (int i = 0; i < rows; i++) { // 只计算每项高度*行数
			Utils.showLog(TAG, " row idx >> " + i);
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
			totalHeight += listItem.getPaddingBottom();
			totalHeight += listItem.getPaddingTop();
			Utils.showLog(TAG, " totalHeight >> " + totalHeight);

		}
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + horizontalBorderHeight * (rows - 1);// 最后加上分割线总高度
		gridView.setLayoutParams(params);
	}

	public static ViewGroup.LayoutParams calcutLayouParams(ImageView imageView, int width, int height) {
		ViewGroup.LayoutParams params = imageView.getLayoutParams();
		int vw = imageView.getHeight() - imageView.getPaddingBottom() - imageView.getPaddingTop();
		if (vw < height) {
			float scale = (float) vw / (float) height;
			int vh = Math.round(width * scale);
			params.width = vh + imageView.getPaddingLeft() + imageView.getPaddingRight();
		} else {
			//params.width = resource.getIntrinsicWidth();
		}
		return params;
	}

	/**
	 * 统一展示sdk api的错误信息
	 *
	 * @param errorCode sdk返回的错误码
	 */
	public static void showErrorToast(int errorCode) {
		try {
			String errorMsg;
			int resId = ResHelper.getStringRes(MobSDK.getContext(), "im_sdk_error_" + Math.abs(errorCode));
			if (resId > 0) {
				errorMsg = MobSDK.getContext().getString(resId);
			} else {
				errorMsg = MobSDK.getContext().getString(ResHelper.getStringRes(MobSDK.getContext(), "im_sdk_error_unknown")) + " " + errorCode;
			}
			Toast.makeText(MobSDK.getContext(), errorMsg, Toast.LENGTH_SHORT).show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

//	public static int getHeigtOfText() {
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setTextSize(18);
//		String text = "Android";
//		Rect rect = new Rect();
//		paint.getTextBounds(text, 0, text.length(), rect);
//		int height = rect.height();//文本的高度
//		return height;
//	}

	public static boolean checkRecorderPermission(Context context) {
		String permissionname = "android.permission.RECORD_AUDIO";
		if ( Build.VERSION.SDK_INT >= 23 ) {
			return checkPermissionHighVersion( context, permissionname);
		} else {
			return checkThePermission( context, permissionname);
		}
	}

	public static boolean checkCameraPermission(Context context) {
		String permissionname = "android.permission.CAMERA";
		if ( Build.VERSION.SDK_INT >= 23 ) {
			return checkPermissionHighVersion( context, permissionname);
		} else {
			return checkThePermission( context, permissionname);
		}
	}

	public static boolean checkWritePermission(Context context) {
		String permissionname = "android.permission.WRITE_EXTERNAL_STORAGE";
		if ( Build.VERSION.SDK_INT >= 23 ) {
			return checkPermissionHighVersion( context, permissionname);
		} else {
			return checkThePermission( context, permissionname);
		}
	}


	private static boolean checkPermissionHighVersion(Context context, String permissionname) {
		if (Build.VERSION.SDK_INT >= 23 ) {
			return context.getApplicationContext().checkSelfPermission(permissionname) == PackageManager.PERMISSION_GRANTED;
		}
		return false;
	}

	private static boolean checkThePermission(Context context, String permissionname) {
		PackageManager pm = context.getPackageManager();
//		boolean permission = (PackageManager.PERMISSION_GRANTED ==
//				pm.checkPermission("android.permission.RECORD_AUDIO", "packageName"));
		boolean permission = (PackageManager.PERMISSION_GRANTED
				 == pm.checkPermission(permissionname, context.getPackageName()));
		if (permission) {
			return true;
		}
		return false;
	}

	/**
	 * 获取指定文件大小
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File file) throws Exception {
		long size = 0;
		FileInputStream fis = null;
		FileChannel fc = null;
		if (file.exists() && file.isFile()) {
			try {
				fis = new FileInputStream(file);
				fc = fis.getChannel();
				size = fc.size();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (null != fc) {
					try {
						fc.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fis != null) {
					fis.close();
				}
			}
		} else {
			//file.createNewFile();
		}
		return size;
	}

	/**
	 * 转换文件大小
	 *
	 * @param fileS
	 * @return
	 */
	public static String FormetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (fileS == 0) {
			return wrongSize;
		}
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}
		return fileSizeString;
	}
}
