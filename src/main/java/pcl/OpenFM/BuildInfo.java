package pcl.OpenFM;

/**
 * This file is automatically updated by Jenkins as part of the CI build script
 * in Ant. Don't put any pre-set values here.
 * 
 * @author AfterLifeLochie, stolen from LanteaCraft, another fine PC-Logix Minecraft mod.
 */
public class BuildInfo {
	public static final String modName = "OpenPrinter";
	public static final String modID = "openprinter";

	public static final String versionNumber = "@VERSION@";
	public static final String buildNumber = "@BUILD@";

	public static int getBuildNumber() {
		if (buildNumber.equals("@" + "BUILD" + "@"))
			return 0;
		return Integer.parseInt(buildNumber);
	}

	public static boolean isDevelopmentEnvironment() {
		return getBuildNumber() == 0;
	}
}