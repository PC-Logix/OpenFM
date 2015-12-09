package pcl.OpenFM.GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.TileEntity.RadioContainer;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GuiRadioBase extends GuiContainer {
	protected FontRenderer fontRenderer;
	protected TileEntityRadio radio;
	protected int gui_width = 256;
	protected int gui_height = 252;
	protected boolean saving = false;
	protected String URL;
	protected boolean playButtonPlayingState = false;
	protected boolean redstoneButtonState = false;
	protected boolean lockedButtonState;
	protected boolean isLocked = false;

	@SuppressWarnings("rawtypes")
	public List OFMbuttonList = new ArrayList();

	public GuiRadioBase(InventoryPlayer inventoryPlayer, TileEntityRadio tileEntity) {
		// the container is instanciated and passed to the superclass for
		// handling
		super(new RadioContainer(inventoryPlayer, tileEntity));
		this.radio = tileEntity;
		this.redstoneButtonState = tileEntity.isListeningToRedstoneInput();
		this.lockedButtonState = tileEntity.isLocked;
		this.gui_width = 256;
		this.gui_height = 252; 
	}

	public boolean exists = true;

	public void initGui() {
		
		super.initGui();
	}

	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3){
		int k = (this.width - this.gui_width) / 2;
		int l = (this.height - this.gui_height) / 2 + 30 - 5 - 45;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(2896);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.mc.renderEngine.bindTexture(new ResourceLocation("openfm:textures/gui/gui_radio.png"));
		drawTexturedModalRect(k, l, 0, 0, this.gui_width, this.gui_height);

		for (k = 0; k < this.OFMbuttonList.size(); k++)
		{
			((OFMGuiButton)this.OFMbuttonList.get(k)).drawButton(this.mc, par1, par2);
		}
		this.mc.fontRenderer.drawString("OpenFM", this.width / 2 - 16, this.height / 2 + 90 - 45, this.radio.getScreenColor());

		this.mc.fontRenderer.drawString(StatCollector.translateToLocal("gui.string.OpenFM.ScreenColor"), this.width / 2 - 101, this.height / 2 + 55 - 45, 0xFFFFFF);
		this.mc.fontRenderer.drawString(StatCollector.translateToLocal("gui.string.OpenFM.ScreenText"), this.width / 2 - 20, this.height / 2 + 55 - 45, 0xFFFFFF);

		super.drawScreen(par1, par2, par3);
		RenderHelper.disableStandardItemLighting();
	}
	
	@Override
    public void drawDefaultBackground() {
        
    }
	
	public void updateScreen() {}

	protected void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);
	}


	@SideOnly(Side.CLIENT)
	protected void mouseClicked(int par1, int par2, int par3) {
		if (par3 == 0) {
			for (int l = 0; l < this.OFMbuttonList.size(); l++) {
				GuiButton guibutton = (GuiButton)this.OFMbuttonList.get(l);
				if (guibutton.mousePressed(this.mc, par1, par2)) {
					GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.OFMbuttonList);
					if (MinecraftForge.EVENT_BUS.post(event))
						break;
					event.button.func_146113_a(this.mc.getSoundHandler());
					actionPerformed(event.button.id);
					if (equals(this.mc.currentScreen))
						MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.OFMbuttonList));
				}
			} 
		}
		super.mouseClicked(par1, par2, par3);
	}

	@SideOnly(Side.CLIENT)
	protected void actionPerformed(int par1GuiButton) {}

	@SideOnly(Side.CLIENT)
	public boolean getState() {
		return this.radio.isPlaying();
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	public String takeFirstEntryFromM3U(String m3uurl)
	{
		String out = OFMConfiguration.defaultURL;
		try
		{
			URL m3u = new URL(m3uurl);
			URLConnection con = m3u.openConnection();
			BufferedReader i = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String mp3;
			while ((mp3 = i.readLine()) != null)
			{
				if (!mp3.startsWith("#")) {
					break;
				}
			}

			out = mp3;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return out;
	}

	public String parsePls(String plsurl) {
		String out = "Stream URL";
		try
		{
			URL pls = new URL(plsurl);
			URLConnection con = pls.openConnection();
			BufferedReader i = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String mp3;
			while ((mp3 = i.readLine()) != null)
			{
				String f = mp3.trim();

				if (f.contains("http://"))
				{
					out = f.substring(f.indexOf("http://"));
					break;
				}
			}
		}
		catch (IOException e)
		{
			OpenFM.logger.info("Inserted URL for a radio is not in a correct form.");
		}
		return out;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
	}
}