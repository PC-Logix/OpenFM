 package pcl.OpenFM.GUI;
 
 import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
 
 
 @SideOnly(Side.CLIENT)
 public class OFMGuiButton extends GuiButton
 {
   private int offx;
   private int offy;
   public static String guiLocation = "openfm:textures/gui/buttons.png";
   private ResourceLocation OFMbuttonTextures = new ResourceLocation("openfm:textures/gui/buttons.png");
   
   public OFMGuiButton(int par1, int par2, int par3, int par4, int par5, String par6Str) {
     super(par1, par2, par3, par4, par5, par6Str);
   }
   
 /**
  * GUI Button
  * @param int Button ID
  * @param int X
  * @param int Y
  * @param int Icon X size
  * @param int Icon Y size
  * @param int Icon X location
  * @param int Icon Y location
  * @param string
  * @param string2
  */
   public OFMGuiButton(int i, int j, int k, int l, int m, int n, int o, String string, String string2)
   {
     super(i, j, k, l, m, string);
     setOFMData(n, o, string2);
   }
   
   public void setOFMData(int x, int y, String loc) {
     this.offx = x;
     this.offy = y;
     this.OFMbuttonTextures = new ResourceLocation(loc);
   }
 
   public int getHoverState(boolean p_146114_1_) {
     byte b0 = 0;
     
     if (!this.enabled)
     {
       b0 = 0;
     }
     else if (p_146114_1_)
     {
       b0 = 2;
     }
     
     return b0;
   }
   
   public void drawButton(Minecraft minecraft, int posX, int posY) {
     if (this.visible) {
       FontRenderer fontrenderer = minecraft.fontRenderer;
       minecraft.getTextureManager().bindTexture(this.OFMbuttonTextures);
       GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
       //Check if the button is being hovered over
       this.field_146123_n = ((posX >= this.xPosition) && (posY >= this.yPosition) && (posX < this.xPosition + this.width) && (posY < this.yPosition + this.height));
       int k = getHoverState(this.field_146123_n);
       GL11.glEnable(3042);
       OpenGlHelper.glBlendFunc(770, 771, 1, 0);
       GL11.glBlendFunc(770, 771);
       drawTexturedModalRect(this.xPosition, this.yPosition, this.offx, this.offy + k * Math.round(this.height / 2), this.width, this.height);
       mouseDragged(minecraft, posX, posY);
       int l = 14737632;
       
       if (this.packedFGColour != 0)
       {
         l = this.packedFGColour;
       }
       else if (!this.enabled)
       {
         l = 10526880;
       }
       else if (this.field_146123_n)
       {
         l = 16777120;
       }
       
       drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
     }
   }
 }