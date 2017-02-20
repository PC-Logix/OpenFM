package pcl.OpenFM.player;

import pcl.OpenFM.player.AudioPlayer;

public class PlayBackEvent {
	public static int STOPPED = 1;
	public static int STARTED = 2;

	private AudioPlayer source;
	private int frame;
	private int id;

	public PlayBackEvent(AudioPlayer source, int id, int frame)
	{
		this.id = id;
		this.source = source;
		this.frame = frame;
	}

	public int getId()
	{
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFrame()
	{
		return this.frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public AudioPlayer getSource()
	{
		return this.source;
	}

	public void setSource(AudioPlayer source) {
		this.source = source;
	}
}