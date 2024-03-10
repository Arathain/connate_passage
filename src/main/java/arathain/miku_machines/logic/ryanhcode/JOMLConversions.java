package arathain.miku_machines.logic.ryanhcode;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class JOMLConversions {
	public static Vec3d toMinecraft(Vector3d vec) {
		return new Vec3d(vec.x, vec.y, vec.z);
	}

	public static Vector3d toJOML(Vec3d vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}
}
