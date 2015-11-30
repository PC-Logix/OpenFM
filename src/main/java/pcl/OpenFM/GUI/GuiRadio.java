package pcl.OpenFM.GUI;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.Handler.RadioContainer;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;

public class GuiRadio extends GuiRadioBase {
	private OFMGuiButton playBtn;
	protected OFMGuiTextField streamTextBox;
	protected OFMGuiTextField volumeBox;
	private OFMGuiButton redstoneBtn;
	private OFMGuiButton lockedBtn;

	public GuiRadio(InventoryPlayer inventoryPlayer, TileEntityRadio r)
	{
		super(new RadioContainer(inventoryPlayer, r));
	}



	@SuppressWarnings("unchecked")
	public void initGui()	{
		super.initGui();
		org.lwjgl.input.Keyboard.enableRepeatEvents(true);

		this.OFMbuttonList.clear();
		
		this.OFMbuttonList.add(new OFMGuiButton(2, this.width / 2 + 12, this.height / 2 + 3 - 5, 10, 10, 58, 24, "", OFMGuiButton.guiLocation)); //VolDown
		this.OFMbuttonList.add(new OFMGuiButton(3, this.width / 2 - 22, this.height / 2 + 3 - 5, 10, 10, 48, 24, "", OFMGuiButton.guiLocation)); //VolUp

	    this.OFMbuttonList.add(new OFMGuiButton(4, this.width / 2 - 107, this.height / 2 + 15 - 5, 6, 12, 75, 24, "", OFMGuiButton.guiLocation)); //Scroll Left
	    this.OFMbuttonList.add(new OFMGuiButton(5, this.width / 2 + 101, this.height / 2 + 15 - 5, 6, 12, 81, 24, "", OFMGuiButton.guiLocation)); //Scroll Right
		
	    this.OFMbuttonList.add(new OFMGuiButton(6, this.width / 2 - 12 - 50, this.height / 2 + 31 - 5, 48, 8, 48, 0, "", OFMGuiButton.guiLocation)); //Clear
	    this.OFMbuttonList.add(new OFMGuiButton(7, this.width / 2 - 12 - 51, this.height / 2 + 41 - 5, 49, 8, 96, 1, "", OFMGuiButton.guiLocation)); //Paste

	    this.OFMbuttonList.add(new OFMGuiButton(8, this.width / 2 + 12 + 2, this.height / 2 + 33 - 5, 42, 6, 145, 0, "", OFMGuiButton.guiLocation)); //Save
	    this.OFMbuttonList.add(new OFMGuiButton(9, this.width / 2 + 12 + 2, this.height / 2 + 40 - 5, 54, 8, 187, 0, "", OFMGuiButton.guiLocation)); //Delete
	    
		this.OFMbuttonList.add(new OFMGuiButton(10, this.width / 2 + 100, this.height / 2 + 3 - 5, 7, 8, 68, 24, "", OFMGuiButton.guiLocation)); //Close

		if (!this.redstoneButtonState) {
			this.redstoneBtn = new OFMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 87, 24, "", OFMGuiButton.guiLocation); //Redstone
		} else {
			this.redstoneBtn = new OFMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 95, 24, "", OFMGuiButton.guiLocation); //Redstone
		}
		
		if (!this.lockedButtonState) {
			this.lockedBtn = new OFMGuiButton(12, this.width / 2 + 100 - 4, this.height / 2 + 30, 12, 16, 115, 24, "", OFMGuiButton.guiLocation); //Locked
		} else {
			this.lockedBtn = new OFMGuiButton(12, this.width / 2 + 100 - 4, this.height / 2 + 30, 12, 16, 103, 24, "", OFMGuiButton.guiLocation); //Locked
		}
		
		if (this.radio.getWorldObj().provider.dimensionId == 0) {
			this.OFMbuttonList.add(this.redstoneBtn);
		}
		this.OFMbuttonList.add(this.lockedBtn);
		this.playBtn = new OFMGuiButton(1, this.width / 2 - 12, this.height / 2 + 28 - 5, 24, 24, 0, 0, "", OFMGuiButton.guiLocation); //Play
		this.OFMbuttonList.add(this.playBtn);


		this.streamTextBox = new OFMGuiTextField(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 5 + 17, 200, 20);
		this.streamTextBox.setMaxStringLength(1000);
		if (!this.radio.streamURL.equals("")) {
			this.streamTextBox.setText(this.radio.streamURL);
		} else {
			this.streamTextBox.setText(OFMConfiguration.defaultURL);
		}
		this.volumeBox = new OFMGuiTextField(this.fontRendererObj, this.width / 2 - 6, this.height / 2 - 5 + 4, 50, 20);
		this.volumeBox.setMaxStringLength(2);
	}



	@SuppressWarnings("unchecked")
	public void updateScreen()
	{
		super.updateScreen();
		this.streamTextBox.updateCursorCounter();
		this.volumeBox.updateCursorCounter();
		if (this.radio.isInvalid())
		{
			this.mc.displayGuiScreen((net.minecraft.client.gui.GuiScreen)null);
			this.mc.setIngameFocus();
		}
		this.volumeBox.setText(" " + (int)(this.radio.getVolume() * 10.0F));

		if ((this.radio.isPlaying()) && (!this.playButtonPlayingState)) {
			this.playButtonPlayingState = true;
			this.OFMbuttonList.remove(this.playBtn);
			this.playBtn = new OFMGuiButton(1, this.width / 2 - 12, this.height / 2 + 28 - 5, 24, 24, 24, 0, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.playBtn);
		}
		if ((!this.radio.isPlaying()) && (this.playButtonPlayingState)) {
			this.playButtonPlayingState = false;
			this.OFMbuttonList.remove(this.playBtn);
			this.playBtn = new OFMGuiButton(1, this.width / 2 - 12, this.height / 2 + 28 - 5, 24, 24, 0, 0, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.playBtn);
		}
		if ((this.radio.isListeningToRedstoneInput() & !this.redstoneButtonState)) {
			this.redstoneButtonState = true;
			this.OFMbuttonList.remove(this.redstoneBtn);
			this.redstoneBtn = new OFMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 95, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.redstoneBtn);
		}
		if ((!this.radio.isListeningToRedstoneInput() & this.redstoneButtonState)) {
			this.redstoneButtonState = false;
			this.OFMbuttonList.remove(this.redstoneBtn);
			this.redstoneBtn = new OFMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 87, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.redstoneBtn);
		}
		if ((this.radio.isLocked & !this.lockedButtonState)) {
			this.lockedButtonState = true;
			this.OFMbuttonList.remove(this.lockedBtn);
			this.lockedBtn = new OFMGuiButton(12, this.width / 2 + 100 - 4, this.height / 2 + 30, 12, 16, 103, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.lockedBtn);
		}
		if ((!this.radio.isLocked & this.lockedButtonState)) {
			this.lockedButtonState = false;
			this.OFMbuttonList.remove(this.lockedBtn);
			this.lockedBtn = new OFMGuiButton(12, this.width / 2 + 100 - 4, this.height / 2 + 30, 12, 16, 115, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.lockedBtn);
		}

	}


	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void drawScreen(int par1, int par2, float par3)
	{
		super.drawScreen(par1, par2, par3);
		this.streamTextBox.drawTextBox();
		this.volumeBox.drawTextBox();
		
		for (int k = 0; k < this.OFMbuttonList.size(); k++) {
			OFMGuiButton btn = (OFMGuiButton)this.OFMbuttonList.get(k);
			if (btn.func_146115_a()) { // Tells you if the button is hovered by mouse
				if (btn.id == 1) {
					String hover;
					if (!this.radio.isPlaying) {
						hover = StatCollector.translateToLocal("gui.string.OpenFM.PlayButton");
					} else {
						hover = StatCollector.translateToLocal("gui.string.OpenFM.StopButton");
					}
					String[] desc = { hover };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 2) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.VolumeUp") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 3) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.VolumeDown") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 4) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.ScrollLeft") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 5) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.ScrollRight") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 6) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Clear") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 7) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Paste") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 8) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Save") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 9) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Delete") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 10) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Close") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 11) {
					String hover;
					if (!this.radio.isListeningToRedstoneInput()) {
						hover = StatCollector.translateToLocal("gui.string.OpenFM.RedstoneOn");
					} else {
						hover = StatCollector.translateToLocal("gui.string.OpenFM.RedstoneOff");
					}
					String[] desc = { hover };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 12) {
					String hover;
					if (!this.radio.isLocked) {
						hover = StatCollector.translateToLocal("gui.string.OpenFM.Lock");
					} else {
						hover = StatCollector.translateToLocal("gui.string.OpenFM.Unlock");
					}
					String[] desc = { hover };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				}
			}
		}
	}




	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.id == 1) //Play button
		{
			if (this.streamTextBox.getText().toLowerCase().endsWith(".m3u"))
			{
				this.radio.streamURL = takeFirstEntryFromM3U(this.streamTextBox.getText());
			}
			else if (this.streamTextBox.getText().toLowerCase().endsWith(".pls"))
			{
				this.radio.streamURL = parsePls(this.streamTextBox.getText());
			}
			else
			{
				this.radio.streamURL = this.streamTextBox.getText();
			}
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, !this.radio.isPlaying(), this.radio.getVolume(), 1));
		}
		if (par1GuiButton.id == 2) //VolUp
		{
			this.saving = false;
			float v = (float)(this.radio.getVolume() + 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), v, 2));
			}
		}
		if (par1GuiButton.id == 3) //VolDown
		{
			this.saving = false;
			float v = (float)(this.radio.getVolume() - 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), v, 3));
			}
		}
		if (par1GuiButton.id == 4) //Scroll left
		{
			this.streamTextBox.setText(this.radio.getPrevious(this.streamTextBox.getText()));
		}
		if (par1GuiButton.id == 5) //Scroll Right
		{
			this.streamTextBox.setText(this.radio.getNext(this.streamTextBox.getText()));
		}
		if (par1GuiButton.id == 6) //Clear
		{
			this.saving = false;
			this.streamTextBox.setText("");
			this.streamTextBox.setCursorPosition(0);
		}
		if (par1GuiButton.id == 7) //Paste
		{
			java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
			try {
				String result = (String)clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
				this.streamTextBox.setText(result);
			}
			catch (Exception localException) {}
		}
		if (par1GuiButton.id == 8) //Save
		{
			this.radio.addStation(this.streamTextBox.getText());
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 42));

		}
		if (par1GuiButton.id == 9) //Delete
		{
			this.radio.delStation(this.streamTextBox.getText());
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 43));
		}
		if (par1GuiButton.id == 10) //Close
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
			Minecraft.getMinecraft().setIngameFocus();
		}
	    if (par1GuiButton.id == 11) //Redstone
	    {
	      if (!this.redstoneButtonState) {
	        PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), this.radio.getVolume(), 11));
	      } else {
	        PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), this.radio.getVolume(), 12));
	      }
	    }
	    if (par1GuiButton.id == 12) //Lock
	    {
	      if (!this.lockedButtonState) {
	        PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 44));
	      } else {
	        PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 45));
	      }
	    }
	}



	protected void mouseClicked(int par1, int par2, int par3)
	{
		this.streamTextBox.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}


	protected void keyTyped(char par1, int par2)
	{
		this.streamTextBox.textboxKeyTyped(par1, par2);

		if (par1 == '\r')
		{
			actionPerformed((GuiButton)this.OFMbuttonList.get(1));
		}
		super.keyTyped(par1, par2);
	}
}


