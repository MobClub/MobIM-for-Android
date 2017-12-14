package cn.sharerec.editor.gui.trimvideo.recorder;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import com.mob.tools.MobHandlerThread;

import java.io.IOException;

public class AudioRecorderByMedia implements IRecorder {
	private boolean recordering = false;
	Handler checkVolume;
	MediaRecorder recorder;
	private long start;
	private long end;
	private int base = 600;
	private int space = 300;// 间隔取样时间

	private VolumeChangeListener volumeChangeListener;

	private void init() {
		if (recorder == null) {
			recorder = new MediaRecorder();
			try {
				recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			} catch (Exception ex) {
				ex.printStackTrace();
				recorder.reset();
				recorder.release();
				recorder = null;
				try {
					recorder = new MediaRecorder();
					recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					//recorder.setAudioSource(MediaRecorder.AudioSource.);
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}
			}

			recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		}
		MobHandlerThread handlerThread = new MobHandlerThread();
		handlerThread.start();
		checkVolume = new Handler(handlerThread.getLooper(),
				new Handler.Callback() {
					public boolean handleMessage(Message msg) {
						checkVolume.removeMessages(0);
						int volume = getVolume();
						if (recordering) {
							if (volumeChangeListener != null) {
								volumeChangeListener.onVolumeChange(volume);
							}
							checkVolume.sendEmptyMessageDelayed(0, space);
						}
						return true;
					}
				});

	}

	private int getVolume() {
		if (recorder != null) {
			// int vuSize = 10 * mMediaRecorder.getMaxAmplitude() / 32768;
			int ratio = 0;
			try {
				ratio = recorder.getMaxAmplitude() / base;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			int db = 0;// 分贝
			if (ratio > 1) {
				db = (int) (20 * Math.log10(ratio));
			}
			return db / 4;
		}
		return 0;
	}

	public void startRecorder(String path, VolumeChangeListener volumeChangeListener) {
		init();
		try {
			recorder.setOutputFile(path);// 设置录音文件输出路径
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.volumeChangeListener = volumeChangeListener;
			recorder.start();
			start = System.currentTimeMillis();
			recordering = true;
			checkVolume.sendEmptyMessageDelayed(0, space);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void stopRecorder() {
		if (recordering) {
			try {
				recorder.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			end = System.currentTimeMillis();
			recorder.reset();
			recorder.release();
			recorder = null;
		}
		recordering = false;
	}

	@Override
	public void pause() {
		if (recordering) {
//			recorder.
		}
	}

	public void resume() {

	}

	public long getDuration() {
		return end - start;
	}

}
