package net.geforcemods.securitycraft.network.client;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class RefreshDisguisableModel {
	private BlockPos pos;
	private boolean insert;
	private ItemStack stack;

	public RefreshDisguisableModel() {}

	public RefreshDisguisableModel(BlockPos pos, boolean insert, ItemStack stack) {
		this.pos = pos;
		this.insert = insert;
		this.stack = stack;
	}

	public static void encode(RefreshDisguisableModel message, FriendlyByteBuf buf) {
		buf.writeBlockPos(message.pos);
		buf.writeBoolean(message.insert);
		buf.writeItem(message.stack);
	}

	public static RefreshDisguisableModel decode(FriendlyByteBuf buf) {
		RefreshDisguisableModel message = new RefreshDisguisableModel();

		message.pos = buf.readBlockPos();
		message.insert = buf.readBoolean();
		message.stack = buf.readItem();
		return message;
	}

	public static void onMessage(RefreshDisguisableModel message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			IModuleInventory be = (IModuleInventory) Minecraft.getInstance().level.getBlockEntity(message.pos);

			if (be != null) {
				if (message.insert)
					be.insertModule(message.stack);
				else
					be.removeModule(ModuleType.DISGUISE);

				ClientHandler.refreshModelData(be.getBlockEntity());
			}
		});

		ctx.get().setPacketHandled(true);
	}
}