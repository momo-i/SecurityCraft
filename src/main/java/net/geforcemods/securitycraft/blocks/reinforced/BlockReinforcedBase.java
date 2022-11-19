package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blocks.BlockOwnable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockReinforcedBase extends BlockOwnable implements IReinforcedBlock {
	private List<Block> vanillaBlocks;
	private int amount;

	public BlockReinforcedBase(Material mat, int a, Block... vB) {
		super(mat);

		vanillaBlocks = Arrays.asList(vB);
		amount = a;
	}

	public BlockReinforcedBase(Material mat, int a, SoundType sound, Block... vB) {
		super(mat);

		setSoundType(sound);
		vanillaBlocks = Arrays.asList(vB);
		amount = a;
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
		IBlockState plant = plantable.getPlant(world, pos.offset(direction));
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));

		if (super.canSustainPlant(state, world, pos, direction, plantable))
			return true;

		if (plant.getBlock() == Blocks.CACTUS)
			return this == SCContent.reinforcedSand;

		if (plantable instanceof BlockBush) { //a nasty workaround because BaseReinforcedBlock can't use BlockBush#canSustainBush because it is protected
			boolean condition = false;

			if (plantable instanceof BlockLilyPad)
				condition = world.getBlockState(pos).getBlock() == SCContent.fakeWater;
			else if (plantable instanceof BlockDeadBush)
				condition = state.getBlock() == SCContent.reinforcedSand || state.getBlock() == SCContent.reinforcedHardenedClay || state.getBlock() == SCContent.reinforcedStainedHardenedClay || state.getBlock() == SCContent.reinforcedDirt;

			if (condition)
				return true;
		}

		switch (plantType) {
			case Desert:
				return state.getBlock() == SCContent.reinforcedSand || state.getBlock() == SCContent.reinforcedHardenedClay || state.getBlock() == SCContent.reinforcedStainedHardenedClay;
			case Plains:
				return state.getBlock() == SCContent.reinforcedGrass || state.getBlock() == SCContent.reinforcedDirt;
			case Beach:
				boolean isBeach = state.getBlock() == SCContent.reinforcedGrass || state.getBlock() == SCContent.reinforcedDirt || state.getBlock() == SCContent.reinforcedSand;
				boolean hasWater = world.getBlockState(pos.east()).getMaterial() == Material.WATER || world.getBlockState(pos.west()).getMaterial() == Material.WATER || world.getBlockState(pos.north()).getMaterial() == Material.WATER || world.getBlockState(pos.south()).getMaterial() == Material.WATER;

				return isBeach && hasWater;
			default:
				return false;
		}
	}

	@Override
	public List<Block> getVanillaBlocks() {
		return vanillaBlocks;
	}

	@Override
	public int getAmount() {
		return amount;
	}
}