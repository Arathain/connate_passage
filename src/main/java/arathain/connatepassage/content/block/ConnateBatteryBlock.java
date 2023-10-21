package arathain.connatepassage.content.block;

import arathain.connatepassage.init.ConnateWorldComponents;
import arathain.connatepassage.logic.worldshell.Worldshell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

/**
 * Block implementation for a 'timed worldshell activation' component, making the worldshell move for a limited amount of time.
 **/
public class ConnateBatteryBlock extends FacingBlock {
	private final boolean inverse;
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	public ConnateBatteryBlock(Settings settings, boolean inverse) {
		super(settings);
		this.inverse = inverse;
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.SOUTH).with(TRIGGERED, false));
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction d = ctx.getPlayerLookDirection();
		if(ctx.getPlayer().isSneaking())
			d = d.getOpposite();
		return this.getDefaultState().with(FACING, d);
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
		int g = world.getReceivedRedstonePower(pos);
		if(g == 0) {
			g = world.getReceivedRedstonePower(pos.up());
		}

		boolean bl2 = state.get(TRIGGERED);
		if (g != 0 && !bl2) {
			world.scheduleBlockTick(pos, this, 0);
			world.setBlockState(pos, state.with(TRIGGERED, true), 2);
		} else if (g == 0 && bl2) {
			world.setBlockState(pos, state.with(TRIGGERED, false), 2);
		}

	}

	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		int g = world.getReceivedRedstonePower(pos);
		for(Worldshell worldshell : world.getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells()) {
			if(Vec3d.ofCenter(pos.add(state.get(FACING).getVector())).distanceTo(worldshell.getPos()) < 2) {
				worldshell.activate(10 * MathHelper.clamp(g, 0, 12), world.getBlockState(pos.add(state.get(FACING).getOpposite().getVector())).isOf(Blocks.IRON_BLOCK) ^ inverse);
			}
		}
	}

}
