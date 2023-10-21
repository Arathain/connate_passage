package arathain.connatepassage.content.block;

import arathain.connatepassage.init.ConnateWorldComponents;
import arathain.connatepassage.logic.worldshell.Worldshell;
import io.netty.buffer.Unpooled;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.*;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
/**
 * Block implementation for a 'worldshell activation' component.
 **/
public class ConnatePulseNode extends FacingBlock {
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

	private static final VoxelShape BASE_SHAPE_Y = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	private static final VoxelShape BASE_SHAPE_X = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	private static final VoxelShape BASE_SHAPE_NX = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape BASE_SHAPE_NY = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape BASE_SHAPE_Z = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	private static final VoxelShape BASE_SHAPE_NZ = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NODE_SHAPE_Y = Block.createCuboidShape(3.5, 3.0, 3.5, 12.5, 15.0, 12.5);
	private static final VoxelShape NODE_SHAPE_X = Block.createCuboidShape(3.0, 3.5, 3.5, 15.0, 12.5, 12.5);
	private static final VoxelShape NODE_SHAPE_NX = Block.createCuboidShape(1.0, 3.5, 3.5, 13.0, 12.5, 12.5);
	private static final VoxelShape NODE_SHAPE_NY = Block.createCuboidShape(3.5, 1.0, 3.5, 12.5, 13.0, 12.5);
	private static final VoxelShape NODE_SHAPE_Z = Block.createCuboidShape(3.5, 3.5, 3.0, 12.5, 12.5, 15.0);
	private static final VoxelShape NODE_SHAPE_NZ = Block.createCuboidShape(3.5, 3.5, 1.0, 12.5, 12.5, 13.0);
	private static final VoxelShape[] FINAL_SHAPE = {VoxelShapes.union(BASE_SHAPE_NY, NODE_SHAPE_NY), VoxelShapes.union(BASE_SHAPE_Y, NODE_SHAPE_Y), VoxelShapes.union(BASE_SHAPE_NZ, NODE_SHAPE_NZ), VoxelShapes.union(BASE_SHAPE_Z, NODE_SHAPE_Z), VoxelShapes.union(BASE_SHAPE_NX, NODE_SHAPE_NX), VoxelShapes.union(BASE_SHAPE_X, NODE_SHAPE_X)};

	public ConnatePulseNode(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.SOUTH).with(TRIGGERED, false));
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FINAL_SHAPE[state.get(FACING).ordinal()];
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction d = ctx.getSide();
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
			if(Vec3d.ofCenter(pos.add(state.get(FACING).getVector().multiply(2))).distanceTo(worldshell.getPos()) < 2) {
				worldshell.activate(-666, world.getBlockState(pos.add(state.get(FACING).getOpposite().getVector())).isOf(Blocks.IRON_BLOCK));
			}
		}
		world.getPlayers().stream().filter(players -> players.getWorld().isChunkLoaded(new ChunkPos(pos).x, new ChunkPos(pos).z)).forEach(player -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			new ResonanceVFXPacket(Vec3d.ofCenter(pos), true).write(buf);
			ServerPlayNetworking.send(player, ResonanceVFXPacket.ID, buf);
		});
	}
}
