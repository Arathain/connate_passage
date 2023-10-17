package arathain.connatepassage.logic;

import net.minecraft.util.math.Vec3d;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public interface ConnateMathUtil {
	/**
	 * Rotates a {@link Vec3d} by a quaternion.
	 **/
	static Vec3d rotateViaQuat(Vec3d transform, Quaternionf quat) {
		Vector3d temp = new Vector3d(transform.x, transform.y, transform.z).rotate(new Quaterniond(quat));
		return new Vec3d(temp.x, temp.y, temp.z);
	}
}
