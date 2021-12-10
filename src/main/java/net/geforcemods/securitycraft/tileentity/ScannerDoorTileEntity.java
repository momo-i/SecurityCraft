package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IViewActivated;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ScannerDoorTileEntity extends SpecialDoorTileEntity implements IViewActivated, ILockable
{
	private int viewCooldown = 0;

	public ScannerDoorTileEntity()
	{
		super(SCContent.teTypeScannerDoor);
	}

	@Override
	public void tick() {
		super.tick();
		checkView(world, pos);
	}

	@Override
	public void onEntityViewed(LivingEntity entity)
	{
		BlockState upperState = world.getBlockState(pos);
		BlockState lowerState = world.getBlockState(pos.down());

		if(!world.isRemote && upperState.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER && !EntityUtils.isInvisible(entity))
		{
			if(!(entity instanceof PlayerEntity))
				return;

			PlayerEntity player = (PlayerEntity)entity;

			if (!isLocked()) {
				String name = entity.getName().getString();

				if (ConfigHandler.SERVER.trickScannersWithPlayerHeads.get() && player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() == Items.PLAYER_HEAD)
					name = PlayerUtils.getNameOfSkull(player);

				if (name == null || (!getOwner().getName().equals(name) && !ModuleUtils.isAllowed(this, name))) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.RETINAL_SCANNER.get().getTranslationKey()), Utils.localize("messages.securitycraft:retinalScanner.notOwner", PlayerUtils.getOwnerComponent(getOwner().getName())), TextFormatting.RED);
					return;
				}

				boolean open = !lowerState.get(DoorBlock.OPEN);
				int length = getSignalLength();

				world.setBlockState(pos, upperState.with(DoorBlock.OPEN, !upperState.get(DoorBlock.OPEN)), 3);
				world.setBlockState(pos.down(), lowerState.with(DoorBlock.OPEN, !lowerState.get(DoorBlock.OPEN)), 3);
				world.playEvent(null, open ? 1005 : 1011, pos, 0);

				if(open && length > 0)
					world.getPendingBlockTicks().scheduleTick(pos, SCContent.SCANNER_DOOR.get(), length);

				if(open && sendsMessages())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SCANNER_DOOR_ITEM.get().getTranslationKey()), Utils.localize("messages.securitycraft:retinalScanner.hello", name), TextFormatting.GREEN);
			}
			else if (sendsMessages()) {
				TranslationTextComponent blockName = Utils.localize(SCContent.SCANNER_DOOR_ITEM.get().getTranslationKey());

				PlayerUtils.sendMessageToPlayer(player, blockName, Utils.localize("messages.securitycraft:sonic_security_system.locked", blockName), TextFormatting.DARK_RED, false);
			}
		}
	}

	@Override
	public int getViewCooldown()
	{
		return viewCooldown;
	}

	@Override
	public void setViewCooldown(int viewCooldown) {
		this.viewCooldown = viewCooldown;
	}

	@Override
	public ModuleType[] acceptedModules()
	{
		return new ModuleType[]{ModuleType.ALLOWLIST};
	}

	@Override
	public int defaultSignalLength()
	{
		return 0;
	}
}
