package arathain.connatepassage.logic.worldshell;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

public abstract class Worldshell {
	protected final Map<BlockPos, BlockState> contained;
	protected Quaternionf rotation, prevRotation = new Quaternionf();
	protected Vec3d prevPos, pos;
	protected final BlockPos pivot;

	public Worldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot) {
		this.contained = contained;
		this.pos = initialPos;
		this.pivot = pivot;
		this.prevPos = pos;
	}
	public abstract Identifier getId();

	public void tick() {
		this.prevPos = pos;
	}

	public Quaternionf getRotation() {
		return getRotation(1);
	}
	public Quaternionf getRotation(float tickDelta) {
		checkRotation();
		return prevRotation.slerp(rotation, tickDelta);
	}

	public BlockPos getPivot() {
		return pivot;
	}

	public void setPos(Vec3d pos) {
		this.pos = pos;
	}
	public void rotate(float pitch, float yaw, float roll) {
		rotation.rotateZYX(roll, yaw, pitch);
	}
	public void setRotation(Quaternionf quat) {
		rotation.set(quat);
	}

	public Vec3d getPos() {
		return pos;
	}
	public Vec3d getPos(float tickDelta) {
		return prevPos.lerp(pos, tickDelta);
	}

	public Map<BlockPos, BlockState> getContained() {
		return contained;
	}

	public void writeNbt(NbtCompound nbt) {
		writeUpdateNbt(nbt);

		NbtList list = new NbtList();
		contained.forEach((key, value) -> {
			NbtCompound compound = NbtHelper.fromBlockPos(key);
			compound.put("state", NbtHelper.fromBlockState(value));
			list.add(compound);
		});
		nbt.putString("id", getId().toString());
		nbt.put("containedBlocks", list);
		nbt.put("pivot", NbtHelper.fromBlockPos(pivot));
	}
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		checkRotation();
		nbt.putFloat("qX", rotation.x);
		nbt.putFloat("qY", rotation.y);
		nbt.putFloat("qZ", rotation.z);
		nbt.putFloat("qW", rotation.w);

		nbt.putDouble("pX", pos.x);
		nbt.putDouble("pY", pos.y);
		nbt.putDouble("pZ", pos.z);
		return nbt;
	}
	protected void checkRotation() {
		if(rotation == null) {
			rotation = new Quaternionf();
		}
		if(prevRotation == null) {
			prevRotation = new Quaternionf();
		}
	}
	public void readUpdateNbt(NbtCompound nbt) {
		this.rotation = new Quaternionf(nbt.getFloat("qX"), nbt.getFloat("qY"), nbt.getFloat("qZ"), nbt.getFloat("qW"));
		this.pos = new Vec3d(nbt.getDouble("pX"), nbt.getDouble("pY"), nbt.getDouble("pZ"));
	}
	public void readNbt(NbtCompound nbt) {
		readUpdateNbt(nbt);
	}
	public static BlockPos getBlockPosFromNbt(NbtCompound nbt) {
		return NbtHelper.toBlockPos(nbt.getCompound("pivot"));
	}

	public static Map<BlockPos, BlockState> getBlocksFromNbt(NbtCompound nbt) {
		NbtList list = nbt.getList("containedBlocks", 10);
		Map<BlockPos, BlockState> map = new HashMap<>();
		list.forEach(bNbt -> {
			map.put(NbtHelper.toBlockPos((NbtCompound) bNbt), NbtHelper.toBlockState(Registries.BLOCK.asLookup(), ((NbtCompound) bNbt).getCompound("state")));
		});
		return map;
	}
}
