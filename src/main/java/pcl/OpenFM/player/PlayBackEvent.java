package pcl.OpenFM.player;

import pcl.OpenFM.player.MP3Player;

public class PlayBackEvent {
	public static int STOPPED = 1;
	public static int STARTED = 2;

	private MP3Player source;
	private int frame;
	private int id;

	public PlayBackEvent(MP3Player source, int id, int frame)
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

	public MP3Player getSource()
	{
		return this.source;
	}

	public void setSource(MP3Player source) {
		this.source = source;
	}
}