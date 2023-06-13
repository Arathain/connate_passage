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
		if(rotation == null) {
			rotation = new Quaternionf();
		}
		if(prevRotation == null) {
			prevRotation = new Quaternionf();
		}
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
		nbt.putFloat("quatX", rotation.x);
		nbt.putFloat("quatY", rotation.y);
		nbt.putFloat("quatZ", rotation.z);
		nbt.putFloat("quatW", rotation.w);
		nbt.putFloat("pQuatX", prevRotation.x);
		nbt.putFloat("pQuatY", prevRotation.y);
		nbt.putFloat("pQuatZ", prevRotation.z);
		nbt.putFloat("pQuatW", prevRotation.w);

		nbt.putDouble("posX", pos.x);
		nbt.putDouble("posY", pos.y);
		nbt.putDouble("posZ", pos.z);
		nbt.putDouble("prevPosX", prevPos.x);
		nbt.putDouble("prevPosY", prevPos.y);
		nbt.putDouble("prevPosZ", prevPos.z);
		return nbt;
	}
	public void readUpdateNbt(NbtCompound nbt) {
		this.prevRotation = new Quaternionf(nbt.getFloat("pQuatX"), nbt.getFloat("pQuatY"), nbt.getFloat("pQuatZ"), nbt.getFloat("pQuatW"));
		this.rotation = new Quaternionf(nbt.getFloat("quatX"), nbt.getFloat("quatY"), nbt.getFloat("quatZ"), nbt.getFloat("quatW"));
		this.pos = new Vec3d(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));
		this.prevPos = new Vec3d(nbt.getDouble("prevPosX"), nbt.getDouble("prevPosY"), nbt.getDouble("prevPosZ"));
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
