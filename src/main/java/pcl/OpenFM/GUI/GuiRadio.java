package pcl.OpenFM.GUI;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;

public class GuiRadio extends GuiRadioBase {
	private OFMGuiButton playBtn;
	protected OFMGuiTextField streamTextBox;
	protected OFMGuiTextField volumeBox;
	protected OFMGuiTextField colorBox;
	private OFMGuiButton redstoneBtn;
	private OFMGuiButton lockedBtn;
	private OFMGuiTextField screenTextBox;
	private OFMGuiButton updateColor;
	private OFMGuiButton updateText;
	private OFMGuiButton saveToMemoryCard;
	private OFMGuiButton loadFromMemoryCard;

	public GuiRadio(InventoryPlayer inventoryPlayer, TileEntityRadio TERadio) {
		super(inventoryPlayer, TERadio);
	}

	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		
		org.lwjgl.input.Keyboard.enableRepeatEvents(true);

		this.OFMbuttonList.clear();

		this.playBtn = new OFMGuiButton(0, this.width / 2 - 12, this.height / 2 + 28 - 5 - 45, 24, 24, 0, 0, "", OFMGuiButton.guiLocation); //Play
		this.OFMbuttonList.add(this.playBtn);

		this.OFMbuttonList.add(new OFMGuiButton(1, this.width / 2 + 12, this.height / 2 + 3 - 5 - 45, 10, 10, 58, 24, "", OFMGuiButton.guiLocation)); //VolDown
		this.OFMbuttonList.add(new OFMGuiButton(2, this.width / 2 - 22, this.height / 2 + 3 - 5 - 45, 10, 10, 48, 24, "", OFMGuiButton.guiLocation)); //VolUp

		this.OFMbuttonList.add(new OFMGuiButton(3, this.width / 2 - 107, this.height / 2 + 15 - 5 - 45, 6, 12, 75, 24, "", OFMGuiButton.guiLocation)); //Scroll Left
		this.OFMbuttonList.add(new OFMGuiButton(4, this.width / 2 + 101, this.height / 2 + 15 - 5 - 45, 6, 12, 81, 24, "", OFMGuiButton.guiLocation)); //Scroll Right

		this.OFMbuttonList.add(new OFMGuiButton(5, this.width / 2 - 12 - 50, this.height / 2 + 31 - 5 - 45, 48, 8, 48, 0, "", OFMGuiButton.guiLocation)); //Clear
		this.OFMbuttonList.add(new OFMGuiButton(6, this.width / 2 - 12 - 51, this.height / 2 + 41 - 5 - 45, 49, 8, 96, 1, "", OFMGuiButton.guiLocation)); //Paste

		this.OFMbuttonList.add(new OFMGuiButton(7, this.width / 2 + 12 + 2, this.height / 2 + 33 - 5 - 45, 42, 6, 145, 0, "", OFMGuiButton.guiLocation)); //Save
		this.OFMbuttonList.add(new OFMGuiButton(8, this.width / 2 + 12 + 2, this.height / 2 + 40 - 5 - 45, 54, 8, 187, 0, "", OFMGuiButton.guiLocation)); //Delete

		this.OFMbuttonList.add(new OFMGuiButton(9, this.width / 2 + 100, this.height / 2 + 3 - 5 - 45, 7, 8, 68, 24, "", OFMGuiButton.guiLocation)); //Close

		if (!this.redstoneButtonState) {
			this.redstoneBtn = new OFMGuiButton(10, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5 - 45, 8, 8, 87, 24, "", OFMGuiButton.guiLocation); //Redstone
		} else {
			this.redstoneBtn = new OFMGuiButton(10, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5 - 45, 8, 8, 95, 24, "", OFMGuiButton.guiLocation); //Redstone
		}

		if (!this.lockedButtonState) {
			this.lockedBtn = new OFMGuiButton(11, this.width / 2 + 100 - 4, this.height / 2 + 30 - 45, 12, 16, 115, 24, "", OFMGuiButton.guiLocation); //Locked
		} else {
			this.lockedBtn = new OFMGuiButton(11, this.width / 2 + 100 - 4, this.height / 2 + 30 - 45, 12, 16, 103, 24, "", OFMGuiButton.guiLocation); //Locked
		}

		if (this.radio.getWorldObj().provider.dimensionId == 0) {
			this.OFMbuttonList.add(this.redstoneBtn);
		}
		
		this.OFMbuttonList.add(this.lockedBtn);

		this.updateColor = new OFMGuiButton(12, 0, 0, 0, 0, 0, 0, "", OFMGuiButton.guiLocation); //Update Color
		this.OFMbuttonList.add(this.updateColor);
		this.updateText  = new OFMGuiButton(13, 0, 0, 0, 0, 0, 0, "", OFMGuiButton.guiLocation); //Update Text
		this.OFMbuttonList.add(this.updateText);

		this.streamTextBox = new OFMGuiTextField(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 5 + 17 - 45, 200, 20);
		this.streamTextBox.setMaxStringLength(1000);
		
		if (!this.radio.streamURL.equals("")) {
			this.streamTextBox.setText(this.radio.streamURL);
		} else {
			this.streamTextBox.setText(OFMConfiguration.defaultURL);
		}
		
		this.volumeBox = new OFMGuiTextField(this.fontRendererObj, this.width / 2 - 6, this.height / 2 - 5 + 4 - 45, 50, 20);
		this.volumeBox.setMaxStringLength(2);

		this.colorBox = new OFMGuiTextField(this.fontRendererObj, this.width / 2 - 97, this.height / 2 - 5 + 72 - 45, 200, 20);
		this.colorBox.setText(toHexString(this.radio.getScreenColor()));
		this.colorBox.setTextColor(this.radio.getScreenColor());
		this.colorBox.setMaxStringLength(6);

		this.screenTextBox = new OFMGuiTextField(this.fontRendererObj, this.width / 2 - 17, this.height / 2 - 5 + 72 - 45, 200, 20);
		this.screenTextBox.setText(this.radio.getScreenText());
		this.screenTextBox.setTextColor(this.radio.getScreenColor());
		
		
		this.saveToMemoryCard = new OFMGuiButton(15,  this.width / 2 - 12 - 83, this.height / 2 + 31 - 5 - 40, 8, 8, 127, 24, "", OFMGuiButton.guiLocation); //Update Color
		this.OFMbuttonList.add(this.saveToMemoryCard);
		this.loadFromMemoryCard = new OFMGuiButton(16,  this.width / 2 - 12 - 83, this.height / 2 + 41 - 5 - 40, 8, 8, 135, 24, "", OFMGuiButton.guiLocation); //Update Text
		this.OFMbuttonList.add(this.loadFromMemoryCard);

		
	}

	public static String toHexString(int decimal) {
		return String.format("%06X", (0xFFFFFF & decimal));
	}

	@SuppressWarnings("unchecked")
	public void updateScreen() {
		super.updateScreen();
		
		this.streamTextBox.updateCursorCounter();
		this.volumeBox.updateCursorCounter();
		this.colorBox.updateCursorCounter();
		
		if (this.radio.isInvalid()) {
			this.mc.displayGuiScreen((net.minecraft.client.gui.GuiScreen)null);
			this.mc.setIngameFocus();
		}
		
		this.volumeBox.setText(" " + (int)(this.radio.getVolume() * 10.0F));
		
		if ((this.radio.isPlaying()) && (!this.playButtonPlayingState)) {
			this.playButtonPlayingState = true;
			this.OFMbuttonList.remove(this.playBtn);
			this.playBtn = new OFMGuiButton(0, this.width / 2 - 12, this.height / 2 + 28 - 5 - 45, 24, 24, 24, 0, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.playBtn);
		}
		
		if ((!this.radio.isPlaying()) && (this.playButtonPlayingState)) {
			this.playButtonPlayingState = false;
			this.OFMbuttonList.remove(this.playBtn);
			this.playBtn = new OFMGuiButton(0, this.width / 2 - 12, this.height / 2 + 28 - 5 - 45, 24, 24, 0, 0, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.playBtn);
		}
		
		if ((this.radio.isListeningToRedstoneInput() & !this.redstoneButtonState)) {
			this.redstoneButtonState = true;
			this.OFMbuttonList.remove(this.redstoneBtn);
			this.redstoneBtn = new OFMGuiButton(10, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5 - 45, 8, 8, 95, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.redstoneBtn);
		}
		
		if ((!this.radio.isListeningToRedstoneInput() & this.redstoneButtonState)) {
			this.redstoneButtonState = false;
			this.OFMbuttonList.remove(this.redstoneBtn);
			this.redstoneBtn = new OFMGuiButton(10, this.width / 2 + 100 - 13, this.height / 2 + 3 - 5 - 45, 8, 8, 87, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.redstoneBtn);
		}
		
		if ((this.radio.isLocked & !this.lockedButtonState)) {
			this.lockedButtonState = true;
			this.OFMbuttonList.remove(this.lockedBtn);
			this.lockedBtn = new OFMGuiButton(11, this.width / 2 + 100 - 4, this.height / 2 + 30 - 45, 12, 16, 103, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.lockedBtn);
		}
		
		if ((!this.radio.isLocked & this.lockedButtonState)) {
			this.lockedButtonState = false;
			this.OFMbuttonList.remove(this.lockedBtn);
			this.lockedBtn = new OFMGuiButton(11, this.width / 2 + 100 - 4, this.height / 2 + 30 - 45, 12, 16, 115, 24, "", OFMGuiButton.guiLocation);
			this.OFMbuttonList.add(this.lockedBtn);
		}
	}

	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		
		this.streamTextBox.drawTextBox();
		this.volumeBox.drawTextBox();
		this.colorBox.drawTextBox();
		this.screenTextBox.drawTextBox();
		
		for (int k = 0; k < this.OFMbuttonList.size(); k++) {
			OFMGuiButton btn = (OFMGuiButton)this.OFMbuttonList.get(k);
			if (btn.func_146115_a()) { // Tells you if the button is hovered by mouse
				if (btn.id == 0) {
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
				} else if (btn.id == 1) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.VolumeUp") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 2) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.VolumeDown") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 3) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.ScrollLeft") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 4) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.ScrollRight") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 5) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Clear") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 6) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Paste") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 7) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Save") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 8) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Delete") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 9) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.Close") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 10) {
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
				} else if (btn.id == 11) {
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
				} else if (btn.id == 15) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.SaveToCard") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				} else if (btn.id == 16) {
					String[] desc = { StatCollector.translateToLocal("gui.string.OpenFM.LoadFromCard") };
					@SuppressWarnings("rawtypes")
					List temp = Arrays.asList(desc);
					drawHoveringText(temp, par1, par2, fontRendererObj);
				}
			}
		}
	}

	@Override
    public void drawDefaultBackground() {}

	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	protected void actionPerformed(int buttonID) {
		if (buttonID == 0) { //Play button
			if (this.streamTextBox.getText().toLowerCase().endsWith(".m3u")) {
				this.radio.streamURL = takeFirstEntryFromM3U(this.streamTextBox.getText());
			} else if (this.streamTextBox.getText().toLowerCase().endsWith(".pls")) {
				this.radio.streamURL = parsePls(this.streamTextBox.getText());
			} else {
				this.radio.streamURL = this.streamTextBox.getText();
			}
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, !this.radio.isPlaying(), this.radio.getVolume(), 1));
		}
		if (buttonID == 1) { //VolUp
			this.saving = false;
			float v = (float)(this.radio.getVolume() + 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), v, 2));
			}
		} if (buttonID == 2) { //VolDown 
			this.saving = false;
			float v = (float)(this.radio.getVolume() - 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), v, 3));
			}
		} if (buttonID == 3) { //Scroll left 
			this.streamTextBox.setText(this.radio.getPrevious(this.streamTextBox.getText()));
		} if (buttonID == 4) { //Scroll Right 
			this.streamTextBox.setText(this.radio.getNext(this.streamTextBox.getText()));
		} if (buttonID == 5) { //Clear 
			this.saving = false;
			this.streamTextBox.setText("");
			this.streamTextBox.setCursorPosition(0);
		} if (buttonID == 6) { //Paste 
			java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
			try {
				String result = (String)clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
				this.streamTextBox.setText(result);
			}
			catch (Exception localException) {}
		} if (buttonID == 7) { //Save
			this.radio.addStation(this.streamTextBox.getText());
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 42));

		} if (buttonID == 8) { //Delete 
			this.radio.delStation(this.streamTextBox.getText());
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 43));
		} if (buttonID == 9) { //Close 
			Minecraft.getMinecraft().displayGuiScreen(null);
			Minecraft.getMinecraft().setIngameFocus();
		}
		if (buttonID == 10) { //Redstone 
			if (!this.redstoneButtonState) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), this.radio.getVolume(), 11));
			} else {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.radio.isPlaying(), this.radio.getVolume(), 12));
			}
		} if (buttonID == 11) { //Lock
			if (!this.lockedButtonState) {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 44));
			} else {
				PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 45));
			}
		} if (buttonID == 13) { //Screen Color
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.colorBox.getText(), 48, 0));
		}

		if (buttonID == 14) { //Screen Text
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, this.screenTextBox.getText(), 49, 0));
		}
		if (buttonID == 16) { //Read from card
			//PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 51));
			if (this.radio.RadioItemStack[0].hasTagCompound()) {
				this.screenTextBox.setText(this.radio.RadioItemStack[0].getTagCompound().getString("screenText"));
				this.colorBox.setText(toHexString(this.radio.RadioItemStack[0].getTagCompound().getInteger("screenColor")));
				this.streamTextBox.setText(this.radio.RadioItemStack[0].getTagCompound().getString("streamURL"));
				actionPerformed(0);
				actionPerformed(13);
				actionPerformed(14);
			}
		}
		if (buttonID == 15) { //Write to card
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.radio.xCoord, this.radio.yCoord, this.radio.zCoord, this.radio.getWorldObj(), this.radio.streamURL, 50));
		}
	}



	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		if (betweenExclusive(par1, this.width / 2 - 100, this.width / 2 - 100 + 198) && betweenExclusive(par2, this.height / 2 - 5 + 17 - 45, this.height / 2 - 5 + 17 + 8 - 45)) {
			this.streamTextBox.mouseClicked(par1, par2, par3);
			this.colorBox.setFocused(false);
			this.screenTextBox.setFocused(false);
		} else if (betweenExclusive(par1, this.width / 2 - 97, this.width / 2 - 97 + 61) && betweenExclusive(par2, this.height / 2 - 5 + 72 - 45, this.height / 2 - 5 + 72 + 10 - 45)) {
			this.colorBox.mouseClicked(par1, par2, par3);
			this.screenTextBox.setFocused(false);
			this.streamTextBox.setFocused(false);
		} else if (betweenExclusive(par1, this.width / 2 - 17, this.width / 2 - 17 + 124) && betweenExclusive(par2, this.height / 2 - 5 + 72 - 45, this.height / 2 - 5 + 72 + 10 - 45)) {
			this.screenTextBox.mouseClicked(par1, par2, par3);
			this.streamTextBox.setFocused(false);
			this.colorBox.setFocused(false);
		} else {
			this.streamTextBox.setFocused(false);
			this.colorBox.setFocused(false);
			this.screenTextBox.setFocused(false);

			//actionPerformed(13);
			this.colorBox.setTextColor(Integer.parseInt(this.colorBox.getText(), 16));
			this.screenTextBox.setTextColor(Integer.parseInt(this.colorBox.getText(), 16));
			//actionPerformed(14);
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (this.streamTextBox.isFocused()) {
			this.streamTextBox.textboxKeyTyped(par1, par2);

			if (par1 == '\r')
			{
				actionPerformed(0);
			}
		} else if (this.colorBox.isFocused()) {
			boolean isHex = Character.toString(par1).matches("[0-9A-F]+");
			if (isHex || par1 == '\b' || par2 == 205 || par2 == 203 || par2 == 211) {
				this.colorBox.textboxKeyTyped(par1, par2);
			}

			if (par1 == '\r')
			{
				actionPerformed(13);
				this.colorBox.setTextColor(Integer.parseInt(this.colorBox.getText(), 16));
				this.screenTextBox.setTextColor(Integer.parseInt(this.colorBox.getText(), 16));
			}
		} else if (this.screenTextBox.isFocused()) {
			this.screenTextBox.textboxKeyTyped(par1, par2);
			if (par1 == '\r')
			{
				actionPerformed(14);
			}
		}
		if(!( par2== Keyboard.KEY_E  &&  this.screenTextBox.isFocused() || this.streamTextBox.isFocused() || this.colorBox.isFocused())) super.keyTyped(par1, par2);
	}

	public static boolean betweenExclusive(int x, int min, int max)
	{
		return x>min && x<max;    
	}
}

