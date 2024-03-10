package arathain.miku_machines.logic.ryanhcode;


import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

/**
 * The full pose of a 3D object. Contains information including:
 * <ul>
 *     <li>Projected position of the COR (Center of rotation, or mass)</li>
 *     <li>The COR of the shell in world space</li>
 *     <li>The scale as a vector in local space</li>
 *     <li>The unit quaternion orientation of the shell</li>
 * </ul>
 */
public class Pose {
	private Vector3d position;
	private Vector3d rotationPoint;
	private Vector3d scale;
	private Quaterniond orientation;

	private Matrix4d cachedMatrix = new Matrix4d();
	private Vector4d temp = new Vector4d();

	/**
	 * Constructs a new pose with default identity and origin values.
	 */
	public Pose() {
		this.position = new Vector3d();
		this.rotationPoint = new Vector3d();
		this.orientation = new Quaterniond();
		this.scale = new Vector3d(1, 1, 1);
	}

	/**
	 * Constructs a new pose.
	 *
	 * @param position      Projected position of the COR
	 * @param rotationPoint The COR in world space
	 * @param orientation   The scale as a vector in local space
	 * @param scale         The unit quaternion orientation
	 */
	public Pose(Vector3d position, Vector3d rotationPoint, Quaterniond orientation, Vector3d scale) {
		this.position = position;
		this.rotationPoint = rotationPoint;
		this.orientation = orientation;
		this.scale = scale;
	}

	public Vector3d getPosition() {
		return this.position;
	}

	public Vector3d getRotationPoint() {
		return this.rotationPoint;
	}

	public Quaterniond getOrientation() {
		return this.orientation;
	}

	public Vector3d getScale() {
		return this.scale;
	}

	public void setPosition(Vector3dc position) {
		this.position.set(position);
	}

	public void setRotationPoint(Vector3dc rotationPoint) {
		this.rotationPoint.set(rotationPoint);
	}

	public void setOrientation(Quaterniondc orientation) {
		this.orientation.set(orientation);
	}

	public void setScale(Vector3dc scale) {
		this.scale.set(scale);
	}

	/**
	 * Copies this pose.
	 *
	 * @return An exact duplicate of this pose
	 */
	public Pose copy() {
		Pose p = new Pose();

		p.position.set(this.position);
		p.rotationPoint.set(this.rotationPoint);
		p.orientation.set(this.orientation);
		p.scale.set(this.scale);

		return p;
	}

	/**
	 * Linearly interpolates between two poses.
	 *
	 * @param other The second pose in the interpolation
	 * @param t     The fraction, from 0 to 1 to lerp between this and {@code other}
	 * @return A new pose, without mutating either existing pose
	 */
	public Pose lerp(Pose other, double t) {
		Pose p = new Pose();

		this.position.lerp(other.getPosition(), t, p.position);
		this.rotationPoint.lerp(other.rotationPoint, t, p.rotationPoint);
		this.scale.lerp(other.scale, t, p.scale);
		this.orientation.slerp(other.orientation, t, p.orientation);

		return p;
	}

	/**
	 * Linearly interpolates between two poses.
	 *
	 * @param other The second pose in the interpolation
	 * @param t     The fraction, from 0 to 1 to lerp between this and {@code other}
	 * @param result the pose to store the result inr
	 * @return A new pose, without mutating either existing pose
	 */
	public Pose lerp(Pose other, double t, Pose result) {
		this.position.lerp(other.getPosition(), t, result.position);
		this.rotationPoint.lerp(other.rotationPoint, t, result.rotationPoint);
		this.scale.lerp(other.scale, t, result.scale);
		this.orientation.slerp(other.orientation, t, result.orientation);

		return result;
	}

	/**
	 * Projects from "world space," or coordinates in the shell to coordinates in the
	 * shell projection.
	 *
	 * @param point The point in local space of the shell
	 */
	public @NotNull Vector3d fromWorldToProjectedCached(@NotNull Vector3d point) {
		cachedMatrix.transform(temp.set(point, 1.0));
		return point.set(temp.x, temp.y, temp.z);
	}

	/**
	 * Calculates the matrix for this pose.
	 */
	public @NotNull Matrix4d calculateMatrix() {
		return cachedMatrix.identity().translate(position).rotate(orientation).scale(scale).translate(-rotationPoint.x, -rotationPoint.y, -rotationPoint.z);
	}

	/**
	 * Projects from "world space," or coordinates in the shell to coordinates in the
	 * shell projection.
	 *
	 * @param point The point in local space of the shell
	 */
	public @NotNull Vector3d fromWorldToProjected(@NotNull Vector3d point) {
		return orientation.transform(point.sub(rotationPoint).mul(scale)).add(position);
	}


	/**
	 * Projects from "world space," or coordinates in the shell to coordinates in the
	 * shell projection.
	 *
	 * @param point The point in local space of the shell
	 */
	public @NotNull Vec3d fromWorldToProjected(@NotNull Vec3d point) {
		return JOMLConversions.toMinecraft(orientation.transform(JOMLConversions.toJOML(point).sub(rotationPoint).mul(scale)).add(position));
	}


	/**
	 * Projects from coordinates in the shell projection to "world space,"
	 * or coordinates in the shell.
	 *
	 * @param point The point in global space, relative to the world origin
	 */
	public @NotNull Vector3d fromProjectedToWorld(@NotNull Vector3d point) {
		return orientation.transformInverse(point.sub(position).div(scale)).add(rotationPoint);
	}

	/**
	 * Projects from coordinates in the shell projection to "world space,"
	 * or coordinates in the shell.
	 *
	 * @param point The point in global space, relative to the world origin
	 */
	public @NotNull Vec3d fromProjectedToWorld(@NotNull Vec3d point) {
		return JOMLConversions.toMinecraft(orientation.transformInverse(JOMLConversions.toJOML(point).sub(position).div(scale)).add(rotationPoint));
	}

	/**
	 * Serializes this pose to a {@link net.minecraft.nbt.NbtCompound}
	 *
	 * @return The serialized tag
	 */
	public NbtCompound write() {
		NbtCompound compound = new NbtCompound();

		compound.put("Orientation", NbtUtils.writeQuaternion(this.getOrientation()));
		compound.put("COR", NbtUtils.writeVector3d(this.getRotationPoint()));
		compound.put("Position", NbtUtils.writeVector3d(this.getPosition()));
		compound.put("Scale", NbtUtils.writeVector3d(this.getScale()));

		return compound;
	}

	/**
	 * Derializes this pose from a {@link net.minecraft.nbt.NbtCompound}
	 *
	 * @return The deserialized pose
	 */
	public Pose read(NbtCompound tag) {
		this.setOrientation(NbtUtils.readQuaternion(tag.getCompound("Orientation")));
		this.setRotationPoint(NbtUtils.readVector3d(tag.getCompound("COR")));
		this.setPosition(NbtUtils.readVector3d(tag.getCompound("Position")));
		this.setScale(NbtUtils.readVector3d(tag.getCompound("Scale")));

		return this;
	}
}

