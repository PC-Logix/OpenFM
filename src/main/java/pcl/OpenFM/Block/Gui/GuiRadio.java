package pcl.OpenFM.Block.Gui;

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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.GUI.DRMGuiButton;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GuiRadio extends GuiScreen {
	protected FontRenderer fontRenderer;
	protected TileEntityRadio radio;
	protected int gui_width = 256;
	protected int gui_height = 252;
	protected boolean saving = false;
	protected String URL;
	private String nickURL;
	protected boolean playButtonPlayingState = false;
	protected boolean redstoneButtonState = false;

	public List DRMbuttonList = new ArrayList();

	public GuiRadio(TileEntityRadio r)
	{
		this.radio = r;
		this.redstoneButtonState = r.listenToRedstone;
		this.gui_width = 256;
		this.gui_height = 252; }

	public boolean exists = true;

	private GuiButton selectedButton;

	public void initGui() {}

	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	public void drawScreen(int par1, int par2, float par3)
	{
		int k = (this.width - this.gui_width) / 2;
		int l = (this.height - this.gui_height) / 2 + 30 - 5;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(2896);
		this.mc.renderEngine.bindTexture(new ResourceLocation("openfm:textures/gui/gui_radio.png"));
		drawTexturedModalRect(k, l, 0, 0, this.gui_width, this.gui_height);

		for (k = 0; k < this.DRMbuttonList.size(); k++)
		{
			((DRMGuiButton)this.DRMbuttonList.get(k)).drawButton(this.mc, par1, par2);
		}
	}

	public void updateScreen() {}

	protected void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);
	}


	@SideOnly(Side.CLIENT)
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0)
		{
			for (int l = 0; l < this.DRMbuttonList.size(); l++)
			{
				GuiButton guibutton = (GuiButton)this.DRMbuttonList.get(l);
				if (guibutton.mousePressed(this.mc, par1, par2))
				{
					GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.DRMbuttonList);
					if (MinecraftForge.EVENT_BUS.post(event))
						break;
					this.selectedButton = event.button;
					event.button.func_146113_a(this.mc.getSoundHandler());
					actionPerformed(event.button);
					if (equals(this.mc.currentScreen))
						MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.DRMbuttonList));
				}
			} 
		}
	}

	@SideOnly(Side.CLIENT)
	protected void actionPerformed(GuiButton par1GuiButton) {}

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
			System.out.println("Inserted URL for a radio is not in a correct form.");
		}
		return out;
	}
}