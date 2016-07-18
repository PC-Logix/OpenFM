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
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
		TileEntityRadio radio = (TileEntityRadio) tileEntity;
		float light = tileEntity.getWorld().getLightBrightness(tileEntity.getPos());
		mc.entityRenderer.disableLightmap();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light, light);

		//RenderManager renderMan = RenderManager.instance;
		
		GL11.glPushMatrix();

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
		
		//This is broken in 1.9.4+ If someone wants to PR a fix to scrollText that doesn't suck please do.
		
		//if (radio.getTicks() > 20) {
			//if (radio.getScreenText().length() > 6) {
				//mc.getRenderManager().getFontRenderer().drawString(scrollText(radio.getScreenText(), radio), -37 / 2, 0, radio.getScreenColor());
			//} else {
			String tempStr = "";
			if(radio.getScreenText().length() > 6) {
				tempStr	= radio.getScreenText().substring(0, 6);
			} else {
				tempStr = radio.getScreenText();
			}
			mc.getRenderManager().getFontRenderer().drawString(tempStr, -37 / 2, 0, radio.getScreenColor());
			//}
		//}
		
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public String scrollText(String text, TileEntityRadio radio) {
		String output = "";
		FontRenderer fontRenderer = mc.getRenderManager().getFontRenderer();
		text = "       " + text + "        ";
		if (text.length() > radio.getRenderCount() + 6) {
			output = text.substring(radio.getRenderCount(), radio.getRenderCount() + 6);
			if (fontRenderer.getStringWidth(output) / 6 < 5) {
				output = text.substring(radio.getRenderCount(), radio.getRenderCount() + 7);
			}
		}
		return output;
	}
}
