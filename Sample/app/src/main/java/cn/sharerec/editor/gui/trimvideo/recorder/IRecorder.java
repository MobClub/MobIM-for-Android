package cn.sharerec.editor.gui.trimvideo.recorder;

public interface IRecorder {

	public void startRecorder(String path, VolumeChangeListener volumeChangeListener);

	public void stopRecorder();

	public void pause();

	public void resume();

	public long getDuration();
}
