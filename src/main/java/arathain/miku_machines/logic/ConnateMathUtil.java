package arathain.miku_machines.logic;

import arathain.miku_machines.logic.ryanhcode.Pose;
import arathain.miku_machines.logic.ryanhcode.ProjectionContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;

/**
 * Math utility class.
 **/
public interface ConnateMathUtil {
	/**
	 * Rotates a {@link Vec3d} by a quaternion.
	 **/
	static Vec3d rotateViaQuat(Vec3d transform, Quaterniond quat) {
		Vector3d temp = new Vector3d(transform.x, transform.y, transform.z).rotate(new Quaterniond(quat));
		return new Vec3d(temp.x, temp.y, temp.z);
	}

	@NotNull
	static Box inverseTransformBox(ProjectionContext context, Pose pose, Box box) {
		Vector3d min = context.transformMin.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Vector3d max = context.transformMax.set(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

		pose.fromProjectedToWorld(context.tempVert1.set(box.minX, box.minY, box.minZ));
		pose.fromProjectedToWorld(context.tempVert2.set(box.maxX, box.minY, box.minZ));
		pose.fromProjectedToWorld(context.tempVert3.set(box.maxX, box.minY, box.maxZ));
		pose.fromProjectedToWorld(context.tempVert4.set(box.minX, box.minY, box.maxZ));

		pose.fromProjectedToWorld(context.tempVert5.set(box.minX, box.maxY, box.minZ));
		pose.fromProjectedToWorld(context.tempVert6.set(box.maxX, box.maxY, box.minZ));
		pose.fromProjectedToWorld(context.tempVert7.set(box.maxX, box.maxY, box.maxZ));
		pose.fromProjectedToWorld(context.tempVert8.set(box.minX, box.maxY, box.maxZ));


		for (Vector3d v : context.tempVerts) {
			min.min(v);
			max.max(v);
		}

		return new Box(min.x, min.y, min.z, max.x, max.y, max.z);
	}
}
