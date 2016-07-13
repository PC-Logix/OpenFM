package pcl.OpenFM.misc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class OFMDepLoader implements IFMLLoadingPlugin, IFMLCallHook {
	public void load() {
		File jar = null;
		File f = new File("mods/");
		if (!f.exists()) {
			f.mkdirs();
			try {
				jar = new File(OFMDepLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			try {
				FileUtils.copyResourcesRecursively(new URL("file://" + jar  + "/assets/openfm/deps/"), f);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public String[] getASMTransformerClass() {
		return null;
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return getClass().getName();
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public Void call() {
		load();

		return null;
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}