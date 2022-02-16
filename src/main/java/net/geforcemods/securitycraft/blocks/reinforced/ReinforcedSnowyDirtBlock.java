package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.List;
import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedSnowyDirtBlock extends SnowyDirtBlock implements IReinforcedBlock, BonemealableBlock, EntityBlock {
	private Block vanillaBlock;

	public ReinforcedSnowyDirtBlock(Block.Properties properties, Block vB) {
		super(properties);
		this.vanillaBlock = vB;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (facing != Direction.UP)
			return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
		else
			return state.setValue(SNOWY, isSnowySetting(facingState));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos().above());
		return defaultBlockState().setValue(SNOWY, isSnowySetting(state));
	}

	private static boolean isSnowySetting(BlockState state) {
		return state.is(BlockTags.SNOW);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
		if (this == SCContent.REINFORCED_MYCELIUM.get()) {
			super.animateTick(state, level, pos, rand);

			if (rand.nextInt(10) == 0)
				level.addParticle(ParticleTypes.MYCELIUM, (double) pos.getX() + (double) rand.nextFloat(), pos.getY() + 1.1D, (double) pos.getZ() + (double) rand.nextFloat(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
		return this == SCContent.REINFORCED_GRASS_BLOCK.get() && level.getBlockState(pos.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random rand, BlockPos pos, BlockState state) {
		return this == SCContent.REINFORCED_GRASS_BLOCK.get();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void performBonemeal(ServerLevel level, Random rand, BlockPos pos, BlockState state) {
		BlockPos posAbove = pos.above();
		BlockState grass = Blocks.GRASS.defaultBlockState();

		for (int i = 0; i < 128; ++i) {
			BlockPos tempPos = posAbove;
			int j = 0;

			while (true) {
				if (j >= i / 16) {
					BlockState tempState = level.getBlockState(tempPos);

					if (tempState.getBlock() == grass.getBlock() && rand.nextInt(10) == 0)
						((BonemealableBlock) grass.getBlock()).performBonemeal(level, rand, tempPos, tempState);

					if (!tempState.isAir())
						break;

					BlockState placeState;

					if (rand.nextInt(8) == 0) {
						List<ConfiguredFeature<?, ?>> flowers = level.getBiome(tempPos).getGenerationSettings().getFlowerFeatures();

						if (flowers.isEmpty())
							break;

						ConfiguredFeature<?, ?> configuredfeature = flowers.get(0);
						AbstractFlowerFeature flowersfeature = (AbstractFlowerFeature) configuredfeature.feature;

						placeState = flowersfeature.getRandomFlower(rand, tempPos, configuredfeature.config());
					}
					else
						placeState = grass;

					if (placeState.canSurvive(level, tempPos))
						level.setBlock(tempPos, placeState, 3);

					break;
				}

				tempPos = tempPos.offset(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1);

				if (level.getBlockState(tempPos.below()).getBlock() != this || level.getBlockState(tempPos).isCollisionShapeFullBlock(level, tempPos))
					break;

				++j;
			}
		}
	}

	@Override
	public Block getVanillaBlock() {
		return vanillaBlock;
	}

	@Override
	public BlockState getConvertedState(BlockState vanillaState) {
		return defaultBlockState().setValue(SNOWY, vanillaState.getValue(SNOWY));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof Player player)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(level, pos, player));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnableBlockEntity(pos, state);
	}
}