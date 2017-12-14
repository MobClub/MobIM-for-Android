package com.mob.demo.mobim.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	private String sdpath;

	public FileUtil() {

	}

	public String getSdpath() {
		return sdpath;
	}

	public FileUtil(String SDPATH) {
		//得到外部存储设备的目录（/SDCARD）
		SDPATH = Environment.getExternalStorageDirectory() + "/";
	}

	/**
	 * 在SD卡上创建文件
	 *
	 * @param fileName
	 * @return
	 * @throws java.io.IOException
	 */
	public File createSDFile(String fileName) throws IOException {
		File file = new File(sdpath + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 *
	 * @param dirName 目录名字
	 * @return 文件目录
	 */
	public File createDir(String dirName) {
		File dir = new File(sdpath + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * 判断文件是否存在
	 *
	 * @param fileName
	 * @return
	 */
	public boolean isFileExist(String fileName) {
		File file = new File(sdpath + fileName);
		return file.exists();
	}

	public File write2SDFromInput(String path, String fileName, InputStream input) {
		File file = null;
		OutputStream output = null;

		try {
			createDir(path);
			try {
				file = createSDFile(path + fileName);
			} catch (Exception ex) {
				//ex.printStackTrace();
				file = new File(path + "/" + fileName);
				Utils.showLog("", " path is  >>> " + path + "/" + fileName);
				//file.mkdir();
				String dir = file.getParent();
				Utils.showLog("", " dir is  >>> " + dir);
				new File(dir).mkdirs();
				//file.mkdirs();
				file.createNewFile();
			}

			output = new FileOutputStream(file);
			byte[] buffer = new byte[4 * 1024];
			int num = 0;
			while ((num = input.read(buffer)) != -1) {
				output.write(buffer, 0, num);
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
		return file;
	}
}
