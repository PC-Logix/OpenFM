package pcl.OpenFM.player;

public class DPlayBackEvent {
	public static int STOPPED = 1;
	public static int STARTED = 2;

	private DAdvancedPlayer source;
	private int frame;
	private int id;

	public DPlayBackEvent(DAdvancedPlayer source, int id, int frame)
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

	public DAdvancedPlayer getSource()
	{
		return this.source;
	}

	public void setSource(DAdvancedPlayer source) {
		this.source = source;
	}
}