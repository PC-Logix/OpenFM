package pcl.OpenFM.Block.Gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.GUI.DRMGuiButton;
import pcl.OpenFM.GUI.DRMGuiTextField;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;

public class NGuiRadio extends GuiRadio {
	private DRMGuiButton playBtn;
	protected DRMGuiTextField streamTextBox;
	protected DRMGuiTextField volumeBox;
	private DRMGuiButton redstoneBtn;

	public NGuiRadio(TileEntityRadio r)
	{
		super(r);
	}



	@SuppressWarnings("unchecked")
	public void initGui()	{
		org.lwjgl.input.Keyboard.enableRepeatEvents(true);

		this.DRMbuttonList.add(new DRMGuiButton(2, this.width / 2 + 12, this.height / 2 + 3 - 5, 10, 10, 58, 24, "", DRMGuiButton.guiLocation)); //VolDown
		this.DRMbuttonList.add(new DRMGuiButton(3, this.width / 2 - 22, this.height / 2 + 3 - 5, 10, 10, 48, 24, "", DRMGuiButton.guiLocation)); //VolUp


		this.DRMbuttonList.add(new DRMGuiButton(6, this.width / 2 - 12 - 50, this.height / 2 + 31 - 5, 48, 8, 48, 0, "", DRMGuiButton.guiLocation)); //clear
		this.DRMbuttonList.add(new DRMGuiButton(7, this.width / 2 + 12 + 2, this.height / 2 + 33 - 5, 55, 8, 96, 1, "", DRMGuiButton.guiLocation)); //paste

		this.DRMbuttonList.add(new DRMGuiButton(10, this.width / 2 + 100, this.height / 2 + 3 - 5, 7, 8, 68, 24, "", DRMGuiButton.guiLocation)); //Close

		if (!this.redstoneButtonState) {
			this.redstoneBtn = new DRMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 87, 24, "", DRMGuiButton.guiLocation); //Redstone
		} else {
			this.redstoneBtn = new DRMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 95, 24, "", DRMGuiButton.guiLocation); //Redstone
		}
		if (this.radio.getWorldObj().provider.dimensionId == 0) {
			this.DRMbuttonList.add(this.redstoneBtn);
		}

		this.playBtn = new DRMGuiButton(1, this.width / 2 - 12, this.height / 2 + 28 - 5, 24, 24, 0, 0, "", DRMGuiButton.guiLocation); //Play
		this.DRMbuttonList.add(this.playBtn);


		this.streamTextBox = new DRMGuiTextField(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 5 + 17, 200, 20);
		this.streamTextBox.setMaxStringLength(1000);
		if (!this.radio.streamURL.equals("")) {
			this.streamTextBox.setText(this.radio.streamURL);
		} else {
			this.streamTextBox.setText(OpenFM.defaultURL);
		}
		this.volumeBox = new DRMGuiTextField(this.fontRendererObj, this.width / 2 - 6, this.height / 2 - 5 + 4, 50, 20);
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
		this.volumeBox.setText(" " + (int)(this.radio.volume * 10.0F));

		if ((this.radio.isPlaying()) && (!this.playButtonPlayingState)) {
			this.playButtonPlayingState = true;
			this.DRMbuttonList.remove(this.playBtn);
			this.playBtn = new DRMGuiButton(1, this.width / 2 - 12, this.height / 2 + 28 - 5, 24, 24, 24, 0, "", DRMGuiButton.guiLocation);
			this.DRMbuttonList.add(this.playBtn);
		}
		if ((!this.radio.isPlaying()) && (this.playButtonPlayingState)) {
			this.playButtonPlayingState = false;
			this.DRMbuttonList.remove(this.playBtn);
			this.playBtn = new DRMGuiButton(1, this.width / 2 - 12, this.height / 2 + 28 - 5, 24, 24, 0, 0, "", DRMGuiButton.guiLocation);
			this.DRMbuttonList.add(this.playBtn);
		}
		if ((this.radio.listenToRedstone & !this.redstoneButtonState)) {
			this.redstoneButtonState = true;
			this.DRMbuttonList.remove(this.redstoneBtn);
			this.redstoneBtn = new DRMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 95, 24, "", DRMGuiButton.guiLocation);
			this.DRMbuttonList.add(this.redstoneBtn);
		}
		if ((!this.radio.listenToRedstone & this.redstoneButtonState)) {
			this.redstoneButtonState = false;
			this.DRMbuttonList.remove(this.redstoneBtn);
			this.redstoneBtn = new DRMGuiButton(11, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5, 8, 8, 87, 24, "", DRMGuiButton.guiLocation);
			this.DRMbuttonList.add(this.redstoneBtn);
		}
	}


	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void drawScreen(int par1, int par2, float par3)
	{
		super.drawScreen(par1, par2, par3);
		this.streamTextBox.drawTextBox();
		this.volumeBox.drawTextBox();
	}




	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.id == 1)
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
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, !this.radio.isPlaying(), this.radio.volume, 1));
		}
		if (par1GuiButton.id == 2)
		{
			this.saving = false;
			float v = (float)(this.radio.volume + 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), v, 2));
			}
		}
		if (par1GuiButton.id == 3)
		{
			this.saving = false;
			float v = (float)(this.radio.volume - 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), v, 3));
			}
		}
		if (par1GuiButton.id == 6)
		{
			this.saving = false;
			this.streamTextBox.setText("");
			this.streamTextBox.setCursorPosition(0);
		}
		if (par1GuiButton.id == 7)
		{
			java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
			try {
				String result = (String)clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
				this.streamTextBox.setText(result);
			}
			catch (Exception localException) {}
		}
		if (par1GuiButton.id == 10)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
			Minecraft.getMinecraft().setIngameFocus();
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
			actionPerformed((GuiButton)this.DRMbuttonList.get(1));
		}
		super.keyTyped(par1, par2);
	}
}


