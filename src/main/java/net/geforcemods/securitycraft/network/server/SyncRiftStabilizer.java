package net.geforcemods.securitycraft.network.server;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.tileentity.TileEntityRiftStabilizer;
import net.geforcemods.securitycraft.tileentity.TileEntityRiftStabilizer.TeleportationType;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncRiftStabilizer implements IMessage {
	private BlockPos pos;
	private TeleportationType teleportationType;
	private boolean allowed;

	public SyncRiftStabilizer() {}

	public SyncRiftStabilizer(BlockPos pos, TeleportationType teleportationType, boolean allowed) {
		this.pos = pos;
		this.teleportationType = teleportationType;
		this.allowed = allowed;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		teleportationType = TeleportationType.values()[buf.readInt()];
		allowed = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeInt(teleportationType.ordinal());
		buf.writeBoolean(allowed);
	}

	public static class Handler implements IMessageHandler<SyncRiftStabilizer, IMessage> {
		@Override
		public IMessage onMessage(SyncRiftStabilizer message, MessageContext ctx) {
			WorldUtils.addScheduledTask(ctx.getServerHandler().player.world, () -> {
				if (message.teleportationType != null) {
					World world = ctx.getServerHandler().player.world;
					BlockPos pos = message.pos;
					boolean allowed = message.allowed;

					if (world.getTileEntity(pos) instanceof TileEntityRiftStabilizer) {
						TileEntityRiftStabilizer te = ((TileEntityRiftStabilizer) world.getTileEntity(pos));

						if (te.isOwnedBy(ctx.getServerHandler().player)) {
							IBlockState state = world.getBlockState(pos);

							te.setFilter(message.teleportationType, allowed);
							world.notifyBlockUpdate(pos, state, state, 2);
						}
					}
				}
			});

			return null;
		}
	}
}
