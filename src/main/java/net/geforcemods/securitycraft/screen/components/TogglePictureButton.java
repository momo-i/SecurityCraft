package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class TogglePictureButton extends IdButton{

	private ResourceLocation textureLocation;
	private int[] u;
	private int[] v;
	private int currentIndex = 0;
	private final int toggleCount;
	private final int drawOffset;
	private final int drawWidth;
	private final int drawHeight;
	private final int textureWidth;
	private final int textureHeight;

	public TogglePictureButton(int id, int xPos, int yPos, int width, int height, ResourceLocation texture, int[] textureX, int[] textureY, int drawOffset, int toggleCount, Consumer<IdButton> onClick)
	{
		this(id, xPos, yPos, width, height, texture, textureX, textureY, drawOffset, 16, 16, 256, 256, toggleCount, onClick);
	}

	public TogglePictureButton(int id, int xPos, int yPos, int width, int height, ResourceLocation texture, int[] textureX, int[] textureY, int drawOffset, int drawWidth, int drawHeight, int textureWidth, int textureHeight, int toggleCount, Consumer<IdButton> onClick)
	{
		super(id, xPos, yPos, width, height, "", onClick);

		if(textureX.length != toggleCount || textureY.length != toggleCount)
			throw new RuntimeException("TogglePictureButton was set up incorrectly. Array lengths must match toggleCount!");

		textureLocation = texture;
		u = textureX;
		v = textureY;
		this.toggleCount = toggleCount;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.drawOffset = drawOffset;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		if (visible)
		{
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, x, y, 0, 46 + getYImage(isHovered()) * 20, width, height, 200, 20, 2, 3, 2, 2, blitOffset);

			if(getTextureLocation() != null)
			{
				Minecraft.getInstance().getTextureManager().bindTexture(getTextureLocation());
				blit(x + drawOffset, y + drawOffset, drawWidth, drawHeight, u[currentIndex], v[currentIndex], drawWidth, drawHeight, textureWidth, textureHeight);
			}
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY)
	{
		setCurrentIndex(currentIndex + 1);
		super.onClick(mouseX, mouseY);
	}

	public int getCurrentIndex()
	{
		return currentIndex;
	}

	public void setCurrentIndex(int newIndex)
	{
		currentIndex = newIndex % toggleCount;
	}

	public ResourceLocation getTextureLocation()
	{
		return textureLocation;
	}
}
