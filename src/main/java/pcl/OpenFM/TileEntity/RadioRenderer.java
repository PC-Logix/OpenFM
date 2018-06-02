package pcl.OpenFM.TileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RadioRenderer extends TileEntitySpecialRenderer {

	private static final Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
		TileEntityRadio radio = (TileEntityRadio) tileEntity;
		//float light = tileEntity.getWorld().getLightBrightness(tileEntity.getPos());
		float light = 1f;
		mc.entityRenderer.disableLightmap();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light, light);
		String output;
		//RenderManager renderMan = RenderManager.instance;

		GL11.glPushMatrix();
		if (tileEntity.getWorld() != null) {
			int dir = tileEntity.getBlockMetadata();
			GL11.glNormal3f(0, 1, 0);
			if (dir == 0) {
				GL11.glTranslatef((float)x +.501F, (float)y + .72F, (float)z - .01F);
				GL11.glRotatef(0F, 0F, 1F, 0F);
			} else if (dir == 1) {
				GL11.glTranslatef((float)x + 1.01f, (float)y + .72F, (float)z + .5F);
				GL11.glRotatef(270F, 0F, 1F, 0F);
			} else if (dir == 2) {
				GL11.glTranslatef((float)x + .5f, (float)y + .72F, (float)z + 1.01F);
				GL11.glRotatef(180F, 0F, 1F, 0F);
			} else if (dir == 3) {
				GL11.glTranslatef((float)x - .01f, (float)y + .72F, (float)z + .5f);
				GL11.glRotatef(90, 0F, 1F, 0F);
			}

			GL11.glScalef(-0.016666668F, -0.016666668F, 0.016666668F);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			mc.getRenderManager().getFontRenderer().drawString(radio.scrollText(radio), -37 / 2, 0, radio.getScreenColor());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_LIGHTING);
		} else {
			//System.out.println("World null");
		}
		GL11.glPopMatrix();
	}
}
