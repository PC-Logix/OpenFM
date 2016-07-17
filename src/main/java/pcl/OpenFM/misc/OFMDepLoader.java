package pcl.OpenFM.misc;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class OFMDepLoader implements IFMLLoadingPlugin, IFMLCallHook {
	public void load() {
		System.out.println("Unpacking Sound plugins to mods directory");
		File jar = null;
		File f = new File("mods/");
			try {
				jar = new File(OFMDepLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			try {
				URL url = new URL("jar:file:" + jar + "!/assets/openfm/deps/");
				JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
				FileUtils.copyJarResourcesRecursively(f, jarConnection);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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