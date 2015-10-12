 package pcl.OpenFM.GUI;
 
 import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
 
 
 @SideOnly(Side.CLIENT)
 public class OFMGuiTextField
   extends GuiTextField
 {
   public OFMGuiTextField(FontRenderer par1FontRenderer, int par2, int par3, int par4, int par5)
   {
     super(par1FontRenderer, par2, par3, par4, par5);
     removeBackground(par1FontRenderer, par2, par3, par4, par5);
   }
   
   public GuiTextField removeBackground(FontRenderer par1FontRenderer, int par2, int par3, int par4, int par5) {
     setEnableBackgroundDrawing(false);
     return this;
   }
 }


