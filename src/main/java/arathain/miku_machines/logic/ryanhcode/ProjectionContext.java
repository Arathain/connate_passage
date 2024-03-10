package arathain.miku_machines.logic.ryanhcode;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * There's a lot of vectors to do SAT, wow.
 */
public class ProjectionContext {
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
