package net.geforcemods.securitycraft.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class OwnerCommand {
	private static final SimpleCommandExceptionType ERROR_SET_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.securitycraft.owner.set.failed"));
	private static final SimpleCommandExceptionType ERROR_FILL_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.securitycraft.owner.fill.failed"));

	private OwnerCommand() {}

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		//@formatter:off
		return Commands.literal("owner")
                .requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set")
                		.then(Commands.argument("pos", BlockPosArgument.blockPos())
                				.then(Commands.literal("reset")
                						.executes(ctx -> setOwner(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), "ownerUUID", "owner")))
                				.then(Commands.literal("random")
                						.executes(ctx -> setRandomOwner(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))
                				.then(Commands.literal("player")
                						.then(Commands.argument("owner", SingleGameProfileArgument.singleGameProfile())
                								.executes(ctx -> setOwner(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), SingleGameProfileArgument.getGameProfile(ctx, "owner")))))))
                .then(Commands.literal("fill")
                		.then(Commands.argument("from", BlockPosArgument.blockPos())
                				.then(Commands.argument("to", BlockPosArgument.blockPos())
                						.then(Commands.literal("reset")
                        						.executes(ctx -> fillOwner(ctx, "ownerUUID", "owner")))
                        				.then(Commands.literal("random")
                        						.executes(ctx -> fillRandomOwner(ctx)))
                        				.then(Commands.literal("player")
                        						.then(Commands.argument("owner", SingleGameProfileArgument.singleGameProfile())
                        								.executes(ctx -> fillOwner(ctx, SingleGameProfileArgument.getGameProfile(ctx, "owner"))))))));
		//@formatter:on
	}

	private static int setRandomOwner(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
		return setOwner(source, pos, UUID.randomUUID().toString(), RandomStringUtils.randomAlphanumeric(10));
	}

	private static int setOwner(CommandSourceStack source, BlockPos pos, GameProfile gameProfile) throws CommandSyntaxException {
		return setOwner(source, pos, gameProfile.getId().toString(), gameProfile.getName());
	}

	private static int setOwner(CommandSourceStack source, BlockPos pos, String uuid, String name) throws CommandSyntaxException {
		ServerLevel level = source.getLevel();

		if (!(level.getBlockEntity(pos) instanceof IOwnable ownable))
			throw ERROR_SET_FAILED.create();

		Owner previousOwner = ownable.getOwner();

		if (!previousOwner.getUUID().equals(uuid) || !previousOwner.getName().equals(name)) {
			BlockState state = ((BlockEntity) ownable).getBlockState();

			ownable.setOwner(uuid, name);
			ownable.onOwnerChanged(state, level, pos, null);
			ownable.getOwner().setValidated(true);
			level.sendBlockUpdated(pos, state, state, 3);
			source.sendSuccess(new TranslatableComponent("commands.securitycraft.owner.set.success", pos.getX(), pos.getY(), pos.getZ()), true);
			return 1;
		}
		else
			throw ERROR_SET_FAILED.create();
	}

	private static int fillRandomOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		return fillOwner(ctx, UUID.randomUUID().toString(), RandomStringUtils.randomAlphanumeric(10));
	}

	private static int fillOwner(CommandContext<CommandSourceStack> ctx, GameProfile gameProfile) throws CommandSyntaxException {
		return fillOwner(ctx, gameProfile.getId().toString(), gameProfile.getName());
	}

	private static int fillOwner(CommandContext<CommandSourceStack> ctx, String uuid, String name) throws CommandSyntaxException {
		BoundingBox area = BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(ctx, "from"), BlockPosArgument.getLoadedBlockPos(ctx, "to"));
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		int blockCount = area.getXSpan() * area.getYSpan() * area.getZSpan();

		if (blockCount > 32768)
			throw FillCommand.ERROR_AREA_TOO_LARGE.create(32768, blockCount);
		else {
			List<BlockEntity> modifiedBlocks = new ArrayList<>();

			for (BlockPos pos : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {
				if (level.getBlockEntity(pos) instanceof IOwnable ownable) {
					Owner previousOwner = ownable.getOwner();

					if (!previousOwner.getUUID().equals(uuid) || !previousOwner.getName().equals(name)) {
						ownable.setOwner(uuid, name);
						modifiedBlocks.add((BlockEntity) ownable);
					}
				}
			}

			int blocksModified = modifiedBlocks.size();

			if (blocksModified == 0)
				throw ERROR_FILL_FAILED.create();
			else {
				for (BlockEntity be : modifiedBlocks) {
					BlockPos pos = be.getBlockPos();
					BlockState state = be.getBlockState();

					((IOwnable) be).onOwnerChanged(state, level, pos, null);
					((IOwnable) be).getOwner().setValidated(true);
					level.sendBlockUpdated(pos, state, state, 3);
				}

				source.sendSuccess(new TranslatableComponent("commands.securitycraft.owner.fill.success", blocksModified), true);
				return blocksModified;
			}
		}
	}
}
