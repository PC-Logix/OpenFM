package pcl.OpenFM.Handler;

import pcl.OpenFM.OpenFM;

public class ChatMessage {

	public static void writeline(String message) {
		OpenFM.logger.info(message);
	}

	public static void writeError(Exception e) {
		OpenFM.logger.error(e);
		OpenFM.logger.error("Please report to http://github.com/PC-Logix/OpenFM/");
	}
}