package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.entity.BouncingBetty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BouncingBettyRenderer extends EntityRenderer<BouncingBetty> {
	public BouncingBettyRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		shadowRadius = 0.5F;
	}

	@Override
	public void render(BouncingBetty entity, float entityYaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight) {
		matrix.pushPose();
		matrix.translate(0.0D, 0.5D, 0.0D);

		if (entity.fuse - partialTicks + 1.0F < 10.0F) {
			float alpha = 1.0F - (entity.fuse - partialTicks + 1.0F) / 10.0F;
			alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
			alpha *= alpha;
			alpha *= alpha;
			float scale = 1.0F + alpha * 0.3F;
			matrix.scale(scale, scale, scale);
		}

		matrix.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		matrix.translate(-0.5D, -0.5D, 0.5D);
		matrix.mulPose(Vector3f.YP.rotationDegrees(90.0F));
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(SCContent.BOUNCING_BETTY.get().defaultBlockState(), matrix, buffer, packedLight, OverlayTexture.NO_OVERLAY);
		matrix.popPose();
		super.render(entity, entityYaw, partialTicks, matrix, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(BouncingBetty entity) {
		return AtlasTexture.LOCATION_BLOCKS;
	}
}