package arathain.connatepassage.content.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class HingeBlock extends WorldshellCreationBlock {

	protected static final VoxelShape X_SHAPE = Block.createCuboidShape(5.0, 0.0, 0.0, 11.0, 16.0, 16.0);
	protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(0.0, 0.0, 5.0, 16.0, 16.0, 11.0);
	protected static final VoxelShape Y_SHAPE = Block.createCuboidShape(0.0, 5.0, 0.0, 16.0, 11.0, 16.0);
	public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
	public HingeBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y));
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
			switch (state.get(AXIS)) {
				case X -> {
					return X_SHAPE;
				}
				case Y -> {
					return Y_SHAPE;
				}
				default -> {
					return Z_SHAPE;
				}
			}
	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return changeRotation(state, rotation);
	}

	public static BlockState changeRotation(BlockState state, BlockRotation rotation) {
		return switch (rotation) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.get(AXIS)) {
				case X -> state.with(AXIS, Direction.Axis.Z);
				case Z -> state.with(AXIS, Direction.Axis.X);
				default -> state;
			};
			default -> state;
		};
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(AXIS, ctx.getSide().getAxis());
	}
}
