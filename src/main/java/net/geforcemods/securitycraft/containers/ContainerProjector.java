package net.geforcemods.securitycraft.containers;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.inventory.TileEntityInventoryWrapper;
import net.geforcemods.securitycraft.network.server.SyncProjector;
import net.geforcemods.securitycraft.tileentity.TileEntityProjector;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ContainerProjector extends ContainerStateSelectorAccess {
	public static final int SIZE = 1;
	private final TileEntityProjector te;
	private Slot projectedBlockSlot;

	public ContainerProjector(InventoryPlayer inventory, TileEntityProjector te) {
		this.te = te;

		// A custom slot that prevents non-Block items from being inserted into the projector
		projectedBlockSlot = addSlotToContainer(new Slot(new TileEntityInventoryWrapper<>(te, this), 36, 79, 23) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() instanceof ItemBlock;
			}
		});

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; ++x) {
				addSlotToContainer(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 + 69));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(inventory, x, 8 + x * 18, 142 + 69));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			slotStackCopy = slotStack.copy();

			if (index < 1) {
				if (!mergeItemStack(slotStack, 1, 36, true))
					return ItemStack.EMPTY;
			}
			else if (index >= 1) {
				if (!mergeItemStack(slotStack, 0, 1, false))
					return ItemStack.EMPTY;
			}

			if (slotStack.getCount() == 0)
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if (slotStack.getCount() == slotStackCopy.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, slotStack);
		}

		return slotStackCopy;
	}

	@Override
	public void onStateChange(IBlockState state) {
		te.setProjectedState(state);
		detectAndSendChanges();

		if (te.getWorld().isRemote)
			SecurityCraft.network.sendToServer(new SyncProjector(te.getPos(), state));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return BlockUtils.isWithinUsableDistance(te.getWorld(), te.getPos(), player, SCContent.projector);
	}

	@Override
	public ItemStack getStateStack() {
		return projectedBlockSlot.getStack();
	}

	@Override
	public IBlockState getSavedState() {
		return te.getProjectedState();
	}
}