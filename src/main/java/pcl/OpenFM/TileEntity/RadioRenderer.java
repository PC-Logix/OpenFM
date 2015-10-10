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
	public String songInfo = null;
	public String text = null;
	MpegInfo tagData;
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
		TileEntityRadio radio = (TileEntityRadio) tileEntity;
		if (songInfo == null) {
			text = "OpenFM";
		} else {
			text = "Song Data";	
		}
		
		float light = tileEntity.getWorldObj().getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 15);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light, light);

		RenderManager renderMan = RenderManager.instance;
		FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();
		GL11.glPushMatrix();

		int dir = tileEntity.getBlockMetadata();
		GL11.glNormal3f(0, 1, 0);

		if (dir == 1) {
			GL11.glTranslatef((float)x +.5F, (float)y + .72F, (float)z + 0F);
			GL11.glRotatef(0F, 0F, 1F, 0F);
		} else if (dir == 2) {
			GL11.glTranslatef((float)x + 1f, (float)y + .72F, (float)z + .5F);
			GL11.glRotatef(270F, 0F, 1F, 0F);
		} else if (dir == 3) {
			GL11.glTranslatef((float)x + .5f, (float)y + .72F, (float)z + 1F);
			GL11.glRotatef(180F, 0F, 1F, 0F);
		} else if (dir == 4) {
			GL11.glTranslatef((float)x, (float)y + .72F, (float)z + .5F);
			GL11.glRotatef(90, 0F, 1F, 0F);
		}

		GL11.glScalef(-0.016666668F, -0.016666668F, 0.016666668F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		renderMan.getFontRenderer().drawString(text, -fontRenderer.getStringWidth(text) / 2, 0, radio.getScreenColor());	
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}
}
