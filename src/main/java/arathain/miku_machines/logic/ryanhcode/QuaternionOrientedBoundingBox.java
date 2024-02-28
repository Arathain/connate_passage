package arathain.miku_machines.logic.ryanhcode;

import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.Objects;

/**
 * Represents an oriented bounding box with extents, orientation, and positioning.
 * The box is expected to be centered on the position.
 * <pre>
 * Used with express permission from Ryan.
 * </pre>
 * @author ryanhcode/ryanh6n
 */
public class QuaternionOrientedBoundingBox {
	private static final Vector3d RIGHT = new Vector3d(1, 0, 0);
	private static final Vector3d UP = new Vector3d(0, 1, 0);
	private static final Vector3d FORWARD = new Vector3d(0, 0, 1);

	private final Vector3d position = new Vector3d();
	private final Vector3d dimensions = new Vector3d();
	private final Quaterniond orientation = new Quaterniond();

	/**
	 * Creates a new oriented bounding box.
	 *
	 * @param position The center in global space
	 * @param dimensions The total dimensions
	 * @param orientation The unit quaternion rotation
	 */
	public QuaternionOrientedBoundingBox(@NotNull Vector3dc position,
										 @NotNull Vector3dc dimensions,
										 @NotNull Quaterniondc orientation) {
		this.position.set(Objects.requireNonNull(position));
		this.dimensions.set(Objects.requireNonNull(dimensions));
		this.orientation.set(Objects.requireNonNull(orientation));
	}

	public QuaternionOrientedBoundingBox setPosition(Vector3dc position) {
		this.position.set(position);
		return this;
	}

	public QuaternionOrientedBoundingBox setExtents(Vector3dc dimensions) {
		this.dimensions.set(dimensions);
		return this;
	}

	/**
	 * Computes all global vertices of this box.
	 */
	public Vector3d @NotNull [] vertices() {
		Vector3d min = this.dimensions.mul(0.5, new Vector3d());
		Vector3d max = this.dimensions.mul(-0.5, new Vector3d());

		return new Vector3d[] {
			this.orientation.transform(min, new Vector3d()).add(this.position),
			this.orientation.transform(new Vector3d(max.x, min.y, min.z)).add(this.position),
			this.orientation.transform(new Vector3d(min.x, max.y, min.z)).add(this.position),
			this.orientation.transform(new Vector3d(max.x, max.y, min.z)).add(this.position),
			this.orientation.transform(new Vector3d(min.x, min.y, max.z)).add(this.position),
			this.orientation.transform(new Vector3d(max.x, min.y, max.z)).add(this.position),
			this.orientation.transform(new Vector3d(min.x, max.y, max.z)).add(this.position),
			this.orientation.transform(max, new Vector3d()).add(this.position)
		};
	}

	/**
	 * Rotates a vector from local space in this OBB to global space.
	 */
	public Vector3d rotate(@NotNull Vector3d vec) {
		return this.orientation.transform(vec);
	}

	/**
	 * Checks if two intervals intersect.
	 */
	private static boolean doesOverlap(@NotNull Vector2d a, @NotNull Vector2d b) {
		return a.x <= b.y && a.y >= b.x;
	}

	/**
	 * @return The overlap of the two intervals.
	 */
	private static double getOverlap(@NotNull Vector2d a, @NotNull Vector2d b) {
		if (!QuaternionOrientedBoundingBox.doesOverlap(a, b)) {
			return 0.f;
		}

		return Math.min(a.y, b.y) - Math.max(a.x, b.x);
	}

	public static Vector3d satToleranced(QuaternionOrientedBoundingBox a, QuaternionOrientedBoundingBox b, double tolerance) {
		// Start out with a normal SAT check
		Vector3d mtv = QuaternionOrientedBoundingBox.sat(a, b);

		Vector3d check = new Vector3d(0, 1, 0);

		if (mtv.lengthSquared() > 0.0 && mtv.normalize(new Vector3d()).dot(check) > 1.0 - tolerance) {
			return mtv.mul(0.0, 1.0, 0.0, new Vector3d());
		} else {
			return mtv;
		}
	}

	/**
	 * Computes the MTV, or Minimum Translation Vector between the vertices of two OBBs.
	 */
	public static @NotNull Vector3d sat(@NotNull QuaternionOrientedBoundingBox obbA,
										@NotNull QuaternionOrientedBoundingBox obbB) {
		Objects.requireNonNull(obbA, "obbA");
		Objects.requireNonNull(obbB, "obbB");

		Vector3d[] verticesA = obbA.vertices();
		Vector3d[] verticesB = obbB.vertices();

		Vector3d checker = obbA.position.sub(obbB.position, new Vector3d()).normalize();

		Vector3d aRight = obbA.rotate(new Vector3d(RIGHT));
		Vector3d aUp = obbA.rotate(new Vector3d(UP));
		Vector3d aForward = obbA.rotate(new Vector3d(FORWARD));

		Vector3d bRight = obbB.rotate(new Vector3d(RIGHT));
		Vector3d bUp = obbB.rotate(new Vector3d(UP));
		Vector3d bForward = obbB.rotate(new Vector3d(FORWARD));

		Vector3d mtv = new Vector3d(Double.MAX_VALUE);

		Vector3d[] checks = new Vector3d[]{
			aRight,
			aUp,
			aForward,
			bRight,
			bUp,
			bForward,
			aRight.cross(bRight, new Vector3d()),
			aRight.cross(bUp, new Vector3d()),
			aRight.cross(bForward, new Vector3d()),
			aUp.cross(bRight, new Vector3d()),
			aUp.cross(bUp, new Vector3d()),
			aUp.cross(bForward, new Vector3d()),
			aForward.cross(bRight, new Vector3d()),
			aForward.cross(bUp, new Vector3d()),
			aForward.cross(bForward, new Vector3d())
		};

		double minOverlap = Double.MAX_VALUE;

		for (Vector3d check : checks) {
			if (check.lengthSquared() <= 0) {
				continue;
			}

			check.normalize();

			Vector2d proj1 = QuaternionOrientedBoundingBox.checkSeparation(verticesA, check);
			Vector2d proj2 = QuaternionOrientedBoundingBox.checkSeparation(verticesB, check);

			if (check.dot(checker) > 0) {
				check.mul(-1.0);
			}

			double overlap = QuaternionOrientedBoundingBox.getOverlap(proj1, proj2);

			if (overlap == 0.f) { // shapes are not overlapping
				return new Vector3d(0);
			} else {
				if (overlap < minOverlap) {
					minOverlap = overlap;
					mtv = check.mul(minOverlap);
				}
			}
		}

		boolean facingOpposite = obbA.position.sub(obbB.position, new Vector3d()).dot(mtv) < 0;

		if (facingOpposite) {
			mtv.mul(-1);
		}

		return mtv;
	}

	/**
	 * Check separation along an axis for Separating Axis Theorem.
	 *
	 * @return a 2d vector with the first component representing minimum and second component maximum
	 */
	public static @NotNull Vector2d checkSeparation(Vector3d @NotNull [] self, @NotNull Vector3d axis) {
		if (axis.equals(0, 0, 0)) {
			return new Vector2d();
		}

		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		for (var i = 0; i < self.length; i++) {
			double dot = self[i].dot(axis);

			min = Math.min(dot, min);
			max = Math.max(dot, max);
		}

		return new Vector2d(min, max);
	}
}
