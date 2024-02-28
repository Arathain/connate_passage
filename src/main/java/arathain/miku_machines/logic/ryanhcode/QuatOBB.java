package arathain.miku_machines.logic.ryanhcode;

import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.Objects;

/**
 * Represents an oriented bounding box with extents, orientation, and positioning.
 * The box is expected to be centered on the position.
 * @author ryanhcode/ryanh6n
 */
public class QuatOBB {
	private static final Vector3d RIGHT = new Vector3d(1, 0, 0);
	private static final Vector3d UP = new Vector3d(0, 1, 0);
	private static final Vector3d FORWARD = new Vector3d(0, 0, 1);

	private final Vector3d position = new Vector3d();
	private final Vector3d dimensions = new Vector3d();
	private final Quaterniond orientation = new Quaterniond();
	private final ProjectionContext context;


	/**
	 * Creates a new oriented bounding box.
	 *
	 * @param position The center in global space
	 * @param dimensions The total dimensions
	 * @param orientation The unit quaternion rotation
	 */
	public QuatOBB(@NotNull Vector3dc position,
				   @NotNull Vector3dc dimensions,
				   @NotNull Quaterniondc orientation,
				   ProjectionContext context) {
		this.position.set(position);
		this.dimensions.set(dimensions);
		this.orientation.set(orientation);
		this.context = context;
	}

	public void set(Vector3dc position, Vector3dc dimensions, Quaterniondc orientation) {
		this.position.set(position);
		this.dimensions.set(dimensions);
		this.orientation.set(orientation);
	}

	public QuatOBB setPosition(Vector3dc position) {
		this.position.set(position);
		return this;
	}

	public QuatOBB setDimensions(Vector3dc dimensions) {
		this.dimensions.set(dimensions);
		return this;
	}

	public QuatOBB setOrientation(Quaterniondc orientation) {
		this.orientation.set(orientation);
		return this;
	}

	public Quaterniond getOrientation() {
		return orientation;
	}

	public Vector3d getPosition() {
		return position;
	}

	public Vector3d getDimensions() {
		return dimensions;
	}

	/**
	 * Computes all global vertices of this box.
	 */
	public Vector3d @NotNull [] vertices(Vector3d[] result) {
		this.dimensions.mul(0.5, context.tempmin);
		this.dimensions.mul(-0.5, context.tempmax);

		this.orientation.transform(context.tempmin, result[0]).add(this.position);
		this.orientation.transform(context.tempVert1.set(context.tempmax.x, context.tempmin.y, context.tempmin.z), result[1]).add(this.position);
		this.orientation.transform(context.tempVert2.set(context.tempmin.x, context.tempmax.y, context.tempmin.z), result[2]).add(this.position);
		this.orientation.transform(context.tempVert3.set(context.tempmax.x, context.tempmax.y, context.tempmin.z), result[3]).add(this.position);
		this.orientation.transform(context.tempVert4.set(context.tempmin.x, context.tempmin.y, context.tempmax.z), result[4]).add(this.position);
		this.orientation.transform(context.tempVert5.set(context.tempmax.x, context.tempmin.y, context.tempmax.z), result[5]).add(this.position);
		this.orientation.transform(context.tempVert6.set(context.tempmin.x, context.tempmax.y, context.tempmax.z), result[6]).add(this.position);
		this.orientation.transform(context.tempmax, result[7]).add(this.position);

		return result;
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
		if (!QuatOBB.doesOverlap(a, b)) {
			return 0.f;
		}

		return Math.min(a.y, b.y) - Math.max(a.x, b.x);
	}

	/**
	 * Computes the MTV, or Minimum Translation Vector between the vertices of two OBBs.
	 */
	public static @NotNull Vector3dc sat(@NotNull QuatOBB obbA,
										 @NotNull QuatOBB obbB) {
		Objects.requireNonNull(obbA, "obbA");
		Objects.requireNonNull(obbB, "obbB");

		ProjectionContext context = obbA.context;

		Vector3d[] verticesA = obbA.vertices(context.a);
		Vector3d[] verticesB = obbB.vertices(context.b);

		Vector3d checker = obbA.position.sub(obbB.position, new Vector3d()).normalize();

		Vector3d aRight = obbA.rotate(context.obbARight.set(RIGHT));
		Vector3d aUp = obbA.rotate(context.obbAUp.set(UP));
		Vector3d aForward = obbA.rotate(context.obbAForward.set(FORWARD));

		Vector3d bRight = obbB.rotate(context.obbBRight.set(RIGHT));
		Vector3d bUp = obbB.rotate(context.obbBUp.set(UP));
		Vector3d bForward = obbB.rotate(context.obbBForward.set(FORWARD));

		Vector3d mtv = new Vector3d(Double.MAX_VALUE);

		genChecks(aRight, aUp, aForward, bRight, bUp, bForward, context.checks);

		double minOverlap = Double.MAX_VALUE;

		for (Vector3d check : context.checks) {
			if (check.equals(0, 0, 0)) {
				continue;
			}

			check.normalize();

			QuatOBB.checkSeparation(verticesA, check, context.proj1);
			QuatOBB.checkSeparation(verticesB, check, context.proj2);

			if (check.dot(checker) > 0) {
				check.mul(-1.0);
			}

			double overlap = QuatOBB.getOverlap(context.proj1, context.proj2);

			if (overlap == 0.f) { // shapes are not overlapping
				return context.zero;
			} else {
				if (overlap < minOverlap) {
					minOverlap = overlap;
					mtv = check.mul(minOverlap);
				}
			}
		}

		boolean facingOpposite = obbA.position.sub(obbB.position, context.oppo).dot(mtv) < 0;

		if (facingOpposite) {
			mtv.mul(-1);
		}

		return mtv;
	}

	private static Vector3d[] genChecks(Vector3d aRight, Vector3d aUp, Vector3d aForward, Vector3d bRight, Vector3d bUp, Vector3d bForward, Vector3d[] checks) {
		checks[0].set(aRight);
		checks[1].set(aUp);
		checks[2].set(aForward);
		checks[3].set(bRight);
		checks[4].set(bUp);
		checks[5].set(bForward);
		aRight.cross(bRight, checks[6]);
		aRight.cross(bUp, checks[7]);
		aRight.cross(bForward, checks[8]);
		aUp.cross(bRight, checks[9]);
		aUp.cross(bUp, checks[10]);
		aUp.cross(bForward, checks[11]);
		aForward.cross(bRight, checks[12]);
		aForward.cross(bUp, checks[13]);
		aForward.cross(bForward, checks[14]);
		return checks;
	}

	public static Vector3dc satToleranced(QuatOBB a, QuatOBB b, double tolerance) {
		// Start out with a normal SAT check
		Vector3dc mtv = QuatOBB.sat(a, b);

		Vector3d check = new Vector3d(0, 1, 0);

		if (mtv.lengthSquared() > 0.0 && mtv.normalize(a.context.normalizedMTV).dot(check) > 1.0 - tolerance) {
			return mtv.mul(0.0, 1.0, 0.0, a.context.verticalMTV);
		} else {
			return mtv;
		}
	}

	/**
	 * Check separation along an axis for Separating Axis Theorem.
	 *
	 * @return a 2d vector with the first component representing minimum and second component maximum
	 */
	public static @NotNull Vector2d checkSeparation(Vector3d @NotNull [] self, @NotNull Vector3d axis, Vector2d result) {
		if (axis.equals(0, 0, 0)) {
			return result.set(0,0);
		}

		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		for (var i = 0; i < self.length; i++) {
			double dot = self[i].dot(axis);

			min = Math.min(dot, min);
			max = Math.max(dot, max);
		}

		return result.set(min, max);
	}

	/**
	 * There's a lot of vectors to do SAT, wow.
	 */
	public static class ProjectionContext {
		public static final Vector3dc ZERO = new Vector3d();
		public final Vector3d tempVert8 = new Vector3d();
		public final Vector3d tempVert7 = new Vector3d();
		public final Vector3d tempVert6 = new Vector3d();
		public final Vector3d tempVert5 = new Vector3d();
		public final Vector3d tempVert4 = new Vector3d();
		public final Vector3d tempVert3 = new Vector3d();
		public final Vector3d tempVert2 = new Vector3d();
		public final Vector3d tempVert1 = new Vector3d();

		public final Vector3d[] tempVerts = new Vector3d[]{
				tempVert1,
				tempVert2,
				tempVert3,
				tempVert4,
				tempVert5,
				tempVert6,
				tempVert7,
				tempVert8
		};

		protected  final Vector3dc zero = new Vector3d();
		protected  final Vector3d tempmin = new Vector3d();
		protected  final Vector3d tempmax = new Vector3d();
		protected  final Vector2d proj1 = new Vector2d();
		protected  final Vector2d proj2 = new Vector2d();
		protected  final Vector3d oppo = new Vector3d();
		protected  final Vector3d verticalMTV = new Vector3d();
		protected  final Vector3d normalizedMTV = new Vector3d();
		protected  final Vector3d obbARight = new Vector3d();
		protected  final Vector3d obbAForward = new Vector3d();
		protected  final Vector3d obbAUp = new Vector3d();
		protected  final Vector3d obbBRight = new Vector3d();
		protected  final Vector3d obbBForward = new Vector3d();
		protected  final Vector3d obbBUp = new Vector3d();
		protected  final Vector3d[] a = new Vector3d[] {
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),

				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d()
		};
		protected  final Vector3d[] b = new Vector3d[] {
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),

				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d()
		};
		protected final Vector3d[] checks = new Vector3d[] {
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),

				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),

				new Vector3d(),
				new Vector3d(),
				new Vector3d(),
				new Vector3d(),

				new Vector3d(),
				new Vector3d(),
				new Vector3d()
		};
		public final Vector3d transformMin = new Vector3d();
		public final Vector3d transformMax = new Vector3d();
		public final Vector3d colPointA = new Vector3d();
		public final Vector3d closestPointA = new Vector3d();
		public final Vector3d manifoldNormal = new Vector3d();
	}
}
