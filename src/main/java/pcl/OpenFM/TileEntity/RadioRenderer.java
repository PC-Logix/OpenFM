package pcl.OpenFM.TileEntity;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import pcl.OpenFM.player.MpegInfo;

public class RadioRenderer extends TileEntitySpecialRenderer {

	public Integer ticks = 0;
	public String text = null;
	MpegInfo tagData;
	String output = "";
	int count = 0;
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
		TileEntityRadio radio = (TileEntityRadio) tileEntity;
		
		float light = tileEntity.getWorldObj().getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 15);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light, light);

		RenderManager renderMan = RenderManager.instance;
		FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();
		GL11.glPushMatrix();

		int dir = tileEntity.getBlockMetadata();
		GL11.glNormal3f(0, 1, 0);
		if (dir == 1) {
			GL11.glTranslatef((float)x +.501F, (float)y + .72F, (float)z + 0F);
			GL11.glRotatef(0F, 0F, 1F, 0F);
		} else if (dir == 2) {
			GL11.glTranslatef((float)x + 1.01f, (float)y + .72F, (float)z + .5F);
			GL11.glRotatef(270F, 0F, 1F, 0F);
		} else if (dir == 3) {
			GL11.glTranslatef((float)x + .5f, (float)y + .72F, (float)z + 1.01F);
			GL11.glRotatef(180F, 0F, 1F, 0F);
		} else if (dir == 4) {
			GL11.glTranslatef((float)x - .01f, (float)y + .72F, (float)z + .5f);
			GL11.glRotatef(90, 0F, 1F, 0F);
		}

		GL11.glScalef(-0.016666668F, -0.016666668F, 0.016666668F);
		GL11.glDisable(GL11.GL_LIGHTING);
		//GL11.glDepthMask(false);
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		++this.ticks;
		if (this.ticks > 20) {
			if (radio.getScreenText().length() > 6) {
				if (radio.getScreenText().length() > count + 6) {
					output = radio.getScreenText().substring(count, count + 6);
					if (fontRenderer.getStringWidth(output) / 6 < 5) {
						output = radio.getScreenText().substring(count, count + 7);
					}
					count++;
					this.ticks = 0;
					if (count > radio.getScreenText().length()) {
						count = 0;
					}
				} else {
					count = 0;
				}
			} else {
				output = radio.getScreenText();
			}
		}
		renderMan.getFontRenderer().drawString(output, -37 / 2, 0, radio.getScreenColor());
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		//GL11.glDepthMask(true);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}
}
