package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.blocks.BlockDisplayCase;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TileEntityDisplayCase extends CustomizableSCTE implements ITickable, IPasswordProtected, ILockable {
	private AxisAlignedBB renderBoundingBox = Block.FULL_BLOCK_AABB;
	private OptionBoolean sendMessage = new OptionBoolean("sendMessage", true);
	private DisabledOption disabled = new DisabledOption(false);
	private ItemStack displayedStack = ItemStack.EMPTY;
	private boolean shouldBeOpen;
	private float openness;
	private float oOpenness;
	private String passcode;
	private IBlockState state;

	@Override
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		renderBoundingBox = new AxisAlignedBB(pos);
	}

	@Override
	public void update() {
		oOpenness = openness;

		if (!shouldBeOpen && openness > 0.0F)
			openness = Math.max(openness - 0.1F, 0.0F);
		else if (shouldBeOpen && openness < 1.0F)
			openness = Math.min(openness + 0.1F, 1.0F);
	}

	@Override
	public void activate(EntityPlayer player) {
		if (!world.isRemote) {
			Block block = world.getBlockState(pos).getBlock();

			if (block instanceof BlockDisplayCase)
				((BlockDisplayCase) block).activate(this);
		}
	}

	@Override
	public void openPasswordGUI(EntityPlayer player) {
		if (!world.isRemote) {
			if (getPassword() != null)
				player.openGui(SecurityCraft.instance, GuiHandler.INSERT_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
			else {
				if (getOwner().isOwner(player))
					player.openGui(SecurityCraft.instance, GuiHandler.SETUP_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
				else
					PlayerUtils.sendMessageToPlayer(player, new TextComponentString("SecurityCraft"), Utils.localize("messages.securitycraft:passwordProtected.notSetUp"), TextFormatting.DARK_RED);
			}
		}
	}

	@Override
	public boolean onCodebreakerUsed(IBlockState state, EntityPlayer player) {
		if (isDisabled()) {
			player.sendStatusMessage(Utils.localize("gui.securitycraft:scManual.disabled"), true);
			return false;
		}
		else {
			activate(player);
			return true;
		}
	}

	@Override
	public String getPassword() {
		return (passcode != null && !passcode.isEmpty()) ? passcode : null;
	}

	@Override
	public void setPassword(String password) {
		passcode = password;
		markDirty();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setTag("DisplayedStack", getDisplayedStack().writeToNBT(new NBTTagCompound()));
		tag.setBoolean("ShouldBeOpen", shouldBeOpen);

		if (passcode != null && !passcode.isEmpty())
			tag.setString("Passcode", passcode);

		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		load(tag, true);
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		load(tag, false);
	}

	public void load(NBTTagCompound tag, boolean forceOpenness) {
		super.readFromNBT(tag);
		setDisplayedStack(new ItemStack(tag.getCompoundTag("DisplayedStack")));
		shouldBeOpen = tag.getBoolean("ShouldBeOpen");
		passcode = tag.getString("Passcode");

		if (forceOpenness)
			forceOpen(shouldBeOpen);
	}

	@Override
	public EnumModuleType[] acceptedModules() {
		return new EnumModuleType[] {
				EnumModuleType.ALLOWLIST, EnumModuleType.DENYLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendMessage, disabled
		};
	}

	public boolean sendsMessages() {
		return sendMessage.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public void setDisplayedStack(ItemStack displayedStack) {
		this.displayedStack = displayedStack;
		sync();
	}

	public ItemStack getDisplayedStack() {
		return displayedStack;
	}

	public void setOpen(boolean shouldBeOpen) {
		world.playSound(null, pos, shouldBeOpen ? SCSounds.DISPLAY_CASE_OPEN.event : SCSounds.DISPLAY_CASE_CLOSE.event, SoundCategory.BLOCKS, 1.0F, 1.0F);
		this.shouldBeOpen = shouldBeOpen;
		sync();
	}

	public void forceOpen(boolean open) {
		shouldBeOpen = open;
		oOpenness = openness = open ? 1.0F : 0.0F;
		sync();
	}

	public float getOpenness(float partialTicks) {
		return oOpenness + (openness - oOpenness) * partialTicks;
	}

	public boolean isOpen() {
		return shouldBeOpen;
	}

	@Override
	public void sync() {
		if (world != null && !world.isRemote) {
			IBlockState state = world.getBlockState(pos);

			markDirty();
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return renderBoundingBox;
	}

	public void setBlockState(IBlockState state) {
		this.state = state;
	}

	public IBlockState getBlockState() {
		if (state != null)
			return state;

		if (world != null)
			return world.getBlockState(pos);

		return SCContent.displayCase.getDefaultState();
	}
}