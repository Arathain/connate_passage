package arathain.miku_machines.content.block.entity;

import arathain.miku_machines.init.ConnateBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class WorldshellBlockEntity extends BlockEntity {
	private float speed = 1;

	public WorldshellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	public static WorldshellBlockEntity hinge(BlockPos pos, BlockState state) {
		return new WorldshellBlockEntity(ConnateBlocks.HINGE_BLOCK_ENTITY, pos, state);
	}
	public static WorldshellBlockEntity spline(BlockPos pos, BlockState state) {
		return new WorldshellBlockEntity(ConnateBlocks.SPLINE_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		speed = nbt.getFloat("spd");
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putFloat("spd", speed);
	}
}
