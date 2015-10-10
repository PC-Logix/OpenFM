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
 public class DRMGuiButton extends GuiButton
 {
   private int offx;
   private int offy;
   public static String guiLocation = "openfm:textures/gui/buttons.png";
   private ResourceLocation DRMbuttonTextures = new ResourceLocation("openfm:textures/gui/buttons.png");
   
   public DRMGuiButton(int par1, int par2, int par3, int par4, int par5, String par6Str) {
     super(par1, par2, par3, par4, par5, par6Str);
   }
   
 
   public DRMGuiButton(int i, int j, int k, int l, int m, int n, int o, String string, String string2)
   {
     super(i, j, k, l, m, string);
     setDRMData(n, o, string2);
   }
   
   public void setDRMData(int x, int y, String loc) {
     this.offx = x;
     this.offy = y;
     this.DRMbuttonTextures = new ResourceLocation(loc);
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
   
   public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_) {
     if (this.visible)
     {
       FontRenderer fontrenderer = p_146112_1_.fontRenderer;
       p_146112_1_.getTextureManager().bindTexture(this.DRMbuttonTextures);
       GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
       this.field_146123_n = ((p_146112_2_ >= this.xPosition) && (p_146112_3_ >= this.yPosition) && (p_146112_2_ < this.xPosition + this.width) && (p_146112_3_ < this.yPosition + this.height));
       int k = getHoverState(this.field_146123_n);
       GL11.glEnable(3042);
       OpenGlHelper.glBlendFunc(770, 771, 1, 0);
       GL11.glBlendFunc(770, 771);
       drawTexturedModalRect(this.xPosition, this.yPosition, this.offx, this.offy + k * Math.round(this.height / 2), this.width, this.height);
       mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
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