package arathain.connatepassage.content.block;

import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.logic.worldshell.Worldshell;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

public class ConnateBatteryBlock extends FacingBlock {
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	protected ConnateBatteryBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.SOUTH).with(TRIGGERED, false));
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite().getOpposite());
	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED);
	}
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
		boolean bl2 = state.get(TRIGGERED);
		if (bl && !bl2) {
			world.scheduleBlockTick(pos, this, 4);
			world.setBlockState(pos, state.with(TRIGGERED, true), 4);
		} else if (!bl && bl2) {
			world.setBlockState(pos, state.with(TRIGGERED, false), 4);
		}

	}

	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		for(Worldshell worldshell : world.getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells()) {
			if(Vec3d.ofCenter(pos.add(state.get(FACING).getVector())).distanceTo(worldshell.getPos()) < 2) {
				//spin goes here
			}
		}
	}

}
