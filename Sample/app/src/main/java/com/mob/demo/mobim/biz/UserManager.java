package com.mob.demo.mobim.biz;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.mob.MobSDK;
import com.mob.imsdk.MobIMCallback;
import com.mob.imsdk.model.IMUser;
import com.mob.tools.network.HttpConnection;
import com.mob.tools.network.HttpResponseCallback;
import com.mob.tools.network.NetworkHelper;
import com.mob.tools.network.StringPart;
import com.mob.tools.utils.Hashon;
import com.mob.tools.utils.ResHelper;
import com.mob.tools.utils.UIHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserManager {

	private static final String SERVER_URL = "http://demo.im.mob.com";
	private static IMUser user = null;
	private static final Executor EXECUTOR = Executors.newCachedThreadPool();

	public static IMUser getUser() {
		if (user == null) {
			user = getCachedUser();
		}
		return user;
	}

	private static void saveUserInfo(IMUser imUser) {
		final String cacheFilePath = new File(MobSDK.getContext().getCacheDir(), "user").getPath();
		ResHelper.saveObjectToFile(cacheFilePath, imUser);
	}

	public synchronized static IMUser getCachedUser() {
		final String cacheFilePath = new File(MobSDK.getContext().getCacheDir(), "user").getPath();
		//从缓存中取用户
		final IMUser imUser = (IMUser) ResHelper.readObjectFromFile(cacheFilePath);
		return imUser;
	}

	public static void logout(final MobIMCallback<Boolean> callback) {
		saveUserInfo(null);
		user = null;
		callback.onSuccess(true);
	}

	/* 登录 */
	public synchronized static void login(final MobIMCallback<IMUser> callback) {
		EXECUTOR.execute(new Runnable() {
			public void run() {
				if (user == null) {
					//如果用户为空，则注册一个
					register(new MobIMCallback<IMUser>() {
						public void onSuccess(final IMUser imUser) {
							user = imUser;
							saveUserInfo(imUser);
							UIHandler.sendEmptyMessage(0, new Handler.Callback() {
								public boolean handleMessage(Message msg) {
									if (callback != null) {
										callback.onSuccess(imUser);
									}
									return false;
								}
							});
						}

						public void onError(final int code, final String message) {
							UIHandler.sendEmptyMessage(0, new Handler.Callback() {
								public boolean handleMessage(Message msg) {
									if (callback != null) {
										callback.onError(code, message);
									}
									return false;
								}
							});
						}
					});
				} else if (callback != null) {
					callback.onSuccess(user);
				}
			}
		});
	}

	/**
	 * 更新好友信息
	 */
	public synchronized static void updateFriendInfo(final IMUser imUser) {
		if (imUser == null) {
			return;
		}
		EXECUTOR.execute(new Runnable() {
			public void run() {
				final String cacheFilePath = getFriendsCachePath();
				//从缓存中取好友别表
				final ArrayList<IMUser> list = (ArrayList<IMUser>) ResHelper.readObjectFromFile(cacheFilePath);
				if (list != null && list.size() > 0) {
					for (IMUser item : list) {
						if (item != null && item.getId().equals(imUser.getId())) {
							item.setExtra(imUser.getExtra());
							item.setNickname(imUser.getNickname());
							item.setAvatar(imUser.getAvatar());
							break;
						}
					}
					ResHelper.saveObjectToFile(cacheFilePath, list);
				}
			}
		});
	}

	/* 获取好友列表 */
	public synchronized static void getFriends(final MobIMCallback<List<IMUser>> callback) {
		EXECUTOR.execute(new Runnable() {
			public void run() {
				final String cacheFilePath = getFriendsCachePath();
				//从缓存中取好友别表
				final ArrayList<IMUser> list = (ArrayList<IMUser>) ResHelper.readObjectFromFile(cacheFilePath);
				UIHandler.sendEmptyMessage(0, new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						callback.onSuccess(list);
						return false;
					}
				});
			}
		});
	}

	public synchronized static void checkIsMyFriend(final String userId, final MobIMCallback<IMUser> callback) {
		if (TextUtils.isEmpty(userId)) {
			callback.onSuccess(null);
			return;
		}
		EXECUTOR.execute(new Runnable() {
			public void run() {
				final String cacheFilePath = getFriendsCachePath();
				//从缓存中取好友别表
				final ArrayList<IMUser> list = (ArrayList<IMUser>) ResHelper.readObjectFromFile(cacheFilePath);
				IMUser imUser = null;
				if (list != null && !list.isEmpty()) {
					for (IMUser item : list) {
						if (item != null && userId.equals(item.getId())) {
							imUser = item;
							break;
						}
					}
				}
				final IMUser res = imUser;
				UIHandler.sendEmptyMessage(0, new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						callback.onSuccess(res);
						return false;
					}
				});
			}
		});
	}

	/* 添加好友 */
	public static void addFriend(final IMUser imUser, final MobIMCallback<Boolean> callback) {
		if (imUser == null || user == null) {
			callback.onError(-9999, "user not found");
			return;
		}
		if (imUser.getId().equals(user.getId())) {
			callback.onError(-9999, "Can't add yourself as a friend");
			return;
		}
		EXECUTOR.execute(new Runnable() {
			public void run() {
				final String cacheFilePath = getFriendsCachePath();
				//从缓存中取好友别表
				ArrayList<IMUser> list = (ArrayList<IMUser>) ResHelper.readObjectFromFile(cacheFilePath);
				if (list == null) {
					list = new ArrayList<IMUser>();
				}
				boolean isNew = true;
				if (list.size() > 0) {
					for (IMUser item : list) {
						if (item.getId().equals(imUser.getId())) {
							isNew = false;
							break;
						}
					}
				}
				Handler.Callback uiCallback = new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						callback.onSuccess(msg.what == 0);
						return false;
					}
				};
				if (isNew) {
					list.add(imUser);
					ResHelper.saveObjectToFile(cacheFilePath, list);
				}
				UIHandler.sendEmptyMessage(isNew ? 0 : 1, uiCallback);
			}
		});
	}

	/* 删除好友 */
	public static void removeFriend(final String userId, final MobIMCallback<Boolean> callback) {
		EXECUTOR.execute(new Runnable() {
			public void run() {
				final String cacheFilePath = getFriendsCachePath();
				//从缓存中取好友别表
				ArrayList<IMUser> list = (ArrayList<IMUser>) ResHelper.readObjectFromFile(cacheFilePath);
				if (list == null) {
					list = new ArrayList<IMUser>();
				}
				IMUser imUser = null;
				if (list.size() > 0) {
					for (IMUser item : list) {
						if (item.getId().equals(userId)) {
							imUser = item;
							break;
						}
					}
				}
				Handler.Callback uiCallback = new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						callback.onSuccess(msg.what == 0);
						return false;
					}
				};
				if (imUser != null) {
					list.remove(imUser);
					ResHelper.saveObjectToFile(cacheFilePath, list);
				}
				UIHandler.sendEmptyMessage(imUser == null ? 1 : 0, uiCallback);
			}
		});
	}

	private static String getFriendsCachePath() {
		return new File(MobSDK.getContext().getCacheDir(), "friends").getPath();
	}

	private static void register(final MobIMCallback<IMUser> callback) {
		NetworkHelper networkHelper = new NetworkHelper();
		NetworkHelper.NetworkTimeOut timeOut = new NetworkHelper.NetworkTimeOut();
		timeOut.connectionTimeout = 10000;
		timeOut.readTimout = 30000;
		try {
			StringPart sp = new StringPart();
			sp.append("appkey=" + MobSDK.getAppkey());
			networkHelper.rawPost(SERVER_URL + "/register", null, sp, new HttpResponseCallback() {
				public void onResponse(HttpConnection conn) throws Throwable {
					final int code = conn.getResponseCode();
					InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[256];
					for (int len = is.read(buf); len != -1; len = is.read(buf)) {
						baos.write(buf, 0, len);
					}
					is.close();
					baos.close();
					final String result = new String(baos.toByteArray());
					if (code == 200) {
						HashMap<String, Object> map = new Hashon().fromJson(result);
						HashMap<String, Object> res = (HashMap<String, Object>) map.get("res");
						String id = (String) res.get("id");
						String avatar = (String) res.get("avatar");
						String nickname = (String) res.get("nickname");
						final IMUser imUser = new IMUser();
						imUser.setAvatar(avatar);
						imUser.setId(id);
						imUser.setNickname(nickname);
						if (callback != null) {
							callback.onSuccess(imUser);
						}
					} else {
						if (callback != null) {
							callback.onError(code, result);
						}
					}
				}
			}, timeOut);
		} catch (Throwable t) {
			t.printStackTrace();
			if (callback != null) {
				callback.onError(-1, t.getMessage());
			}
		}
	}

	public static void updateUserInfo(String id, String nickname, String avatar, final MobIMCallback<Boolean> callback) {
		if (TextUtils.isEmpty(id) || user == null) {
			if (callback != null) {
				callback.onError(-1, "id is null");
			}
			return;
		}
		final StringPart sp = new StringPart();
		sp.append("appkey=" + MobSDK.getAppkey());
		sp.append("&id=" + id);
		if (!TextUtils.isEmpty(nickname)) {
			sp.append("&nickname=" + nickname);
			user.setNickname(nickname);
		}
		if (!TextUtils.isEmpty(avatar)) {
			sp.append("&avatar=" + avatar);
			user.setAvatar(avatar);
		}
		saveUserInfo(user);
		EXECUTOR.execute(new Runnable() {
			public void run() {
				try {
					NetworkHelper networkHelper = new NetworkHelper();
					NetworkHelper.NetworkTimeOut timeOut = new NetworkHelper.NetworkTimeOut();
					timeOut.connectionTimeout = 10000;
					timeOut.readTimout = 30000;
					networkHelper.rawPost(SERVER_URL + "/update/user", null, sp, new HttpResponseCallback() {
						public void onResponse(HttpConnection conn) throws Throwable {
							final int code = conn.getResponseCode();
							InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
							final ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] buf = new byte[256];
							for (int len = is.read(buf); len != -1; len = is.read(buf)) {
								baos.write(buf, 0, len);
							}
							is.close();
							baos.close();
							final String result = new String(baos.toByteArray());
							if (code == 200) {
								HashMap<String, Object> map = new Hashon().fromJson(result);
								final String res = (String) map.get("res");
								UIHandler.sendEmptyMessage(0, new Handler.Callback() {
									public boolean handleMessage(Message msg) {
										if (callback != null) {
											callback.onSuccess("success".equals(res));
										}
										return false;
									}
								});
							} else {
								UIHandler.sendEmptyMessage(0, new Handler.Callback() {
									public boolean handleMessage(Message msg) {
										if (callback != null) {
											callback.onError(code, result);
										}
										return false;
									}
								});
							}
						}
					}, timeOut);
				} catch (final Throwable t) {
					t.printStackTrace();
					UIHandler.sendEmptyMessage(0, new Handler.Callback() {
						public boolean handleMessage(Message msg) {
							if (callback != null) {
								callback.onError(-1, t.getMessage());
							}
							return false;
						}
					});
				}
			}
		});
	}

	/**
	 * 从用户系统中搜索用户信息
	 *
	 * @param userId   用户id
	 * @param callback 回调
	 */
	public static void findUser(final String userId, final MobIMCallback<IMUser> callback) {
		EXECUTOR.execute(new Runnable() {
			public void run() {
				findUserFromUserServer(userId, new MobIMCallback<IMUser>() {
					public void onSuccess(final IMUser imUser) {
						UIHandler.sendEmptyMessage(0, new Handler.Callback() {
							public boolean handleMessage(Message msg) {
								if (callback != null) {
									callback.onSuccess(imUser);
								}
								return false;
							}
						});
					}

					public void onError(final int code, final String message) {
						UIHandler.sendEmptyMessage(0, new Handler.Callback() {
							public boolean handleMessage(Message msg) {
								if (callback != null) {
									callback.onError(code, message);
								}
								return false;
							}
						});
					}
				});
			}
		});
	}

	/**
	 * 将sdk返回的用户，转换成自己的用户（场景：未登录用户，在IM端只有id，而没有用户信息的）
	 *
	 * @param user     sdk返回的用户
	 * @param callback 回调
	 */
	public static void getFullUserInfo(final IMUser user, final MobIMCallback<IMUser> callback) {
		if (user == null || TextUtils.isEmpty(user.getId()) || (!TextUtils.isEmpty(user.getNickname()) && !TextUtils.isEmpty(user.getAvatar()))) {
			//如果用户为空，或者用户的头像和昵称都不为空，则直接返回
			callback.onSuccess(user);
			return;
		}
		//没有用户昵称和头像时，获取
		EXECUTOR.execute(new Runnable() {
			public void run() {
				findUserFromUserServer(user.getId(), new MobIMCallback<IMUser>() {
					public void onSuccess(IMUser imUser) {
						if (imUser != null) {
							//更新用户信息
							user.setNickname(imUser.getNickname());
							user.setAvatar(imUser.getAvatar());
							user.setExtra(imUser.getExtra());
						}
					}

					public void onError(int code, String message) {

					}
				});
				UIHandler.sendEmptyMessage(0, new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						callback.onSuccess(user);
						return false;
					}
				});
			}
		});
	}

	/**
	 * 将sdk返回的用户列表，转换成自己的用户（场景：未登录用户，在IM端只有id，而没有用户信息的）
	 *
	 * @param list     sdk返回的用户列表
	 * @param callback 回调
	 */
	public static void getFullUserInfo(final List<IMUser> list, final MobIMCallback<ArrayList<IMUser>> callback) {
		if (list == null || list.isEmpty()) {
			callback.onSuccess(null);
			return;
		}
		EXECUTOR.execute(new Runnable() {
			public void run() {
				final ArrayList<IMUser> newList = new ArrayList<IMUser>();
				for (final IMUser item : list) {
					if (!TextUtils.isEmpty(item.getId()) && (TextUtils.isEmpty(item.getNickname()) || TextUtils.isEmpty(item.getAvatar()))) {
						findUserFromUserServer(item.getId(), new MobIMCallback<IMUser>() {
							public void onSuccess(IMUser imUser) {
								if (imUser != null) {
									//更新用户信息
									item.setNickname(imUser.getNickname());
									item.setAvatar(imUser.getAvatar());
									item.setExtra(imUser.getExtra());
								}
								newList.add(item);
							}

							public void onError(int code, String message) {
								newList.add(item);
							}
						});
					} else {
						newList.add(item);
					}
				}
				UIHandler.sendEmptyMessage(0, new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						callback.onSuccess(newList);
						return false;
					}
				});
			}
		});
	}

	/**
	 * 从用户系统中搜索用户信息
	 *
	 * @param userId   用户id
	 * @param callback 回调
	 */
	private static void findUserFromUserServer(final String userId, final MobIMCallback<IMUser> callback) {
		NetworkHelper networkHelper = new NetworkHelper();
		NetworkHelper.NetworkTimeOut timeOut = new NetworkHelper.NetworkTimeOut();
		timeOut.connectionTimeout = 10000;
		timeOut.readTimout = 30000;
		try {
			StringPart sp = new StringPart();
			sp.append("appkey=" + MobSDK.getAppkey());
			sp.append("&");
			sp.append("id=" + userId);
			networkHelper.rawPost(SERVER_URL + "/find", null, sp, new HttpResponseCallback() {
				public void onResponse(HttpConnection conn) throws Throwable {
					final int code = conn.getResponseCode();
					InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[256];
					for (int len = is.read(buf); len != -1; len = is.read(buf)) {
						baos.write(buf, 0, len);
					}
					is.close();
					baos.close();
					final String result = new String(baos.toByteArray());
					if (code == 200) {
						HashMap<String, Object> map = new Hashon().fromJson(result);
						HashMap<String, Object> res = (HashMap<String, Object>) map.get("res");
						String id = (String) res.get("id");
						String avatar = (String) res.get("avatar");
						String nickname = (String) res.get("nickname");
						final IMUser imUser = new IMUser();
						imUser.setAvatar(avatar);
						imUser.setId(String.valueOf(id));
						imUser.setNickname(nickname);
						if (callback != null) {
							callback.onSuccess(imUser);
						}
					} else {
						if (callback != null) {
							callback.onError(code, result);
						}
					}
				}
			}, timeOut);
		} catch (final Throwable t) {
			t.printStackTrace();
			if (callback != null) {
				callback.onError(-1, t.getMessage());
			}
		}
	}
}
