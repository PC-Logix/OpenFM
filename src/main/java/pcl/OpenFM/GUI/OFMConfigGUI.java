package pcl.OpenFM.GUI;

import pcl.OpenFM.BuildInfo;
import pcl.OpenFM.OFMConfiguration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class OFMConfigGUI extends GuiConfig {
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public OFMConfigGUI(GuiScreen parent) {
        super(parent, new ConfigElement(OFMConfiguration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), BuildInfo.modID, false, false, "OpenFM Configuation");
    }
}
