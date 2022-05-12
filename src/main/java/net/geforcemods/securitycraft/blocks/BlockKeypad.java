package net.geforcemods.securitycraft.blocks;

import java.util.Random;
import java.util.function.Function;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordConvertible;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.tileentity.TileEntityKeypad;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockKeypad extends BlockDisguisable {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool POWERED = PropertyBool.create("powered");

	public BlockKeypad(Material material) {
		super(material);
		setSoundType(SoundType.STONE);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state.getValue(POWERED))
			return false;
		else if (!world.isRemote) {
			TileEntityKeypad te = (TileEntityKeypad) world.getTileEntity(pos);

			if (ModuleUtils.isDenied(te, player)) {
				if (te.sendsMessages())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getTranslationKey() + ".name"), Utils.localize("messages.securitycraft:module.onDenylist"), TextFormatting.RED);

				return true;
			}

			if (ModuleUtils.isAllowed(te, player)) {
				if (te.sendsMessages())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(getTranslationKey() + ".name"), Utils.localize("messages.securitycraft:module.onAllowlist"), TextFormatting.GREEN);

				activate(state, world, pos, te.getSignalLength());
				return true;
			}

			if (!PlayerUtils.isHoldingItem(player, SCContent.codebreaker, hand))
				te.openPasswordGUI(player);
		}

		return true;
	}

	public void activate(IBlockState state, World world, BlockPos pos, int signalLength) {
		world.setBlockState(pos, state.withProperty(POWERED, true));
		BlockUtils.updateIndirectNeighbors(world, pos, SCContent.keypad);
		world.scheduleUpdate(pos, this, signalLength);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		world.setBlockState(pos, state.withProperty(POWERED, false));
		BlockUtils.updateIndirectNeighbors(world, pos, SCContent.keypad);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		setDefaultFacing(world, pos, state);
	}

	private void setDefaultFacing(World world, BlockPos pos, IBlockState state) {
		IBlockState north = world.getBlockState(pos.north());
		IBlockState south = world.getBlockState(pos.south());
		IBlockState west = world.getBlockState(pos.west());
		IBlockState east = world.getBlockState(pos.east());
		EnumFacing facing = state.getValue(FACING);

		if (facing == EnumFacing.NORTH && north.isFullBlock() && !south.isFullBlock())
			facing = EnumFacing.SOUTH;
		else if (facing == EnumFacing.SOUTH && south.isFullBlock() && !north.isFullBlock())
			facing = EnumFacing.NORTH;
		else if (facing == EnumFacing.WEST && west.isFullBlock() && !east.isFullBlock())
			facing = EnumFacing.EAST;
		else if (facing == EnumFacing.EAST && east.isFullBlock() && !west.isFullBlock())
			facing = EnumFacing.WEST;

		world.setBlockState(pos, state.withProperty(FACING, facing), 2);
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (blockState.getValue(POWERED))
			return 15;
		else
			return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (blockState.getValue(POWERED))
			return 15;
		else
			return 0;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(POWERED, false);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta == 15)
			return getDefaultState();

		if (meta <= 5)
			return getDefaultState().withProperty(FACING, EnumFacing.values()[meta].getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : EnumFacing.values()[meta]).withProperty(POWERED, false);
		else
			return getDefaultState().withProperty(FACING, EnumFacing.values()[meta - 6]).withProperty(POWERED, true);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state.getProperties().containsKey(POWERED) && state.getValue(POWERED))
			return (state.getValue(FACING).getIndex() + 6);
		else {
			if (!state.getProperties().containsKey(FACING))
				return 15;

			return state.getValue(FACING).getIndex();
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, POWERED);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityKeypad();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	public static class Convertible implements Function<Object, IPasswordConvertible>, IPasswordConvertible {
		@Override
		public IPasswordConvertible apply(Object o) {
			return this;
		}

		@Override
		public Block getOriginalBlock() {
			return SCContent.frame;
		}

		@Override
		public boolean convert(EntityPlayer player, World world, BlockPos pos) {
			Owner oldOwner = ((IOwnable) world.getTileEntity(pos)).getOwner();

			world.setBlockState(pos, SCContent.keypad.getDefaultState().withProperty(BlockKeypad.FACING, world.getBlockState(pos).getValue(BlockFrame.FACING)).withProperty(BlockKeypad.POWERED, false));
			((IOwnable) world.getTileEntity(pos)).setOwner(oldOwner.getUUID(), oldOwner.getName());
			return true;
		}
	}
}