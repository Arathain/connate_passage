package arathain.connatepassage.logic.worldshell;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.quiltmc.qsl.tag.impl.client.QuiltHolderReferenceHooks;

import java.util.HashMap;
import java.util.Map;

public abstract class Worldshell {
	private final Map<BlockPos, BlockState> contained;
	private Quaternionf rotation = new Quaternionf(0, 0, 0, 0);
	private Vec3d pos;

	public Worldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos) {
		this.contained = contained;
		this.pos = initialPos;
	}

	public Quaternionf getRotation() {
		return rotation;
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

	public void writeNbt(NbtCompound nbt) {
		nbt.putFloat("quatX", rotation.x);
		nbt.putFloat("quatY", rotation.y);
		nbt.putFloat("quatZ", rotation.z);
		nbt.putFloat("quatW", rotation.w);

		nbt.putDouble("posX", pos.x);
		nbt.putDouble("posY", pos.y);
		nbt.putDouble("posZ", pos.z);

		NbtList list = new NbtList();
		contained.forEach((key, value) -> {
			NbtCompound compound = NbtHelper.fromBlockPos(key);
			compound.put("state", NbtHelper.fromBlockState(value));
			list.add(compound);
		});
		nbt.put("containedBlocks", list);
	}
	public void readNbt(NbtCompound nbt) {
		this.rotation = new Quaternionf(nbt.getFloat("quatX"), nbt.getFloat("quatY"), nbt.getFloat("quatZ"), nbt.getFloat("quatW"));
		this.pos = new Vec3d(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));

		NbtList list = nbt.getList("containedBlocks", 10);
		contained.clear();
		list.forEach(bNbt -> {
			contained.put(NbtHelper.toBlockPos((NbtCompound) bNbt), NbtHelper.toBlockState(Registries.BLOCK.asLookup(), nbt.getCompound("state")));
		});
	}
}
