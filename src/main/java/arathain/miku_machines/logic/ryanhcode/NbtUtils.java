package arathain.miku_machines.logic.ryanhcode;

import net.minecraft.nbt.NbtCompound;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public interface NbtUtils {
	static Quaterniond readQuaternion(NbtCompound nbt) {
		return new Quaterniond(nbt.getDouble("qX"), nbt.getDouble("qY"), nbt.getDouble("qZ"), nbt.getDouble("qW"));
	}
	static NbtCompound writeQuaternion(Quaterniond q) {
		NbtCompound nbt = new NbtCompound();
		nbt.putDouble("qX", q.x);
		nbt.putDouble("qY", q.y);
		nbt.putDouble("qZ", q.z);
		nbt.putDouble("qW", q.w);
		return nbt;
	}

	static Vector3d readVector3d(NbtCompound nbt) {
		return new Vector3d(nbt.getDouble("pX"), nbt.getDouble("pY"), nbt.getDouble("pZ"));
	}

	static NbtCompound writeVector3d(Vector3d v) {
		NbtCompound nbt = new NbtCompound();
		nbt.putDouble("pX", v.x);
		nbt.putDouble("pY", v.y);
		nbt.putDouble("pZ", v.z);
		return nbt;
	}
}
