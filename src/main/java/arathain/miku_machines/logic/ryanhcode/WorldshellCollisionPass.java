package arathain.miku_machines.logic.ryanhcode;

import arathain.miku_machines.init.ConnateWorldComponents;
import arathain.miku_machines.logic.worldshell.Worldshell;
import arathain.miku_machines.logic.worldshell.WorldshellWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class responsible for handling entity collision with worldshells.
 * <pre>
 * Proprietary code used with express permission from Ryan, adapted for unique codebase.
 * </pre>
 * @author ryanhcode/ryanh6n, arathain
 */
public class WorldshellCollisionPass {
	public record WorldshellCollisionResult(Vec3d collision, boolean hasCollided) {

	}
	/**
	 * Main part not made by Ryan; iterates over all worldshells to check for collision.
	 **/
	public static Vec3d collideWithWorldshells(World world, WorldshellWrapper velocityShell, Entity e, Vec3d movement) {
		if(velocityShell.shell != null) {
			if(e.getVelocity().y == -0.0784000015258789 && movement.y == 0) {
				velocityShell.shell = null;
				velocityShell.hasCollided = false;
			}
		}
		WorldshellCollisionPass.WorldshellCollisionResult r = new WorldshellCollisionPass.WorldshellCollisionResult(movement, false);
		Vector3d original = new Vector3d(movement.x, movement.y, movement.z);
		for(Worldshell w : world.getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells()) {
			if(WorldshellCollisionPass.boxCollidesSphere(e.getBoundingBox(), w.getPos(), w.maxDistance)) {
				r = collide(e, r.collision(), w, original);
				if(r.hasCollided()) {
					velocityShell.shell = w;
				}
			}
		}
		if(velocityShell.shell != null) {
			if (r.hasCollided()) {
				velocityShell.hasCollided = true;
				velocityShell.isColliding = true;
				r = new WorldshellCollisionPass.WorldshellCollisionResult(r.collision().subtract(velocityShell.shell.getRotationalVelocity(e.getPos())), true);
			} else {
				velocityShell.isColliding = false;
			}
			if(r.hasCollided()) {
				e.setYaw(e.getYaw() + velocityShell.shell.getYawVelocity(1));
				if (e instanceof LivingEntity l) {
					l.setBodyYaw(l.bodyYaw + velocityShell.shell.getYawVelocity(1));
				} else {
					velocityShell.hasCollided = false;
				}
			}
		}


		return r.hasCollided() ? r.collision() : movement;
	}

	/**
	 * @author ryanhcode/ryanh6n
	 * **/
	public static WorldshellCollisionResult collide(Entity e, Vec3d movement, Worldshell shell, Vector3d backup) {
		Vec3d yeag = shell.getVelocity();
		Vector3d shellDir = new Vector3d(movement.x, movement.y, movement.z);
		List<VoxelShape> shapes = new ArrayList<>();
		boolean hasCollided = false;
		Iterable<? extends VoxelShape> collisionIterator = shell.getContained().entrySet().stream().map(en -> en.getValue().getCollisionShape(shell, en.getKey()).offset(en.getKey().getX()-shell.getPivot().getX(), en.getKey().getY()-shell.getPivot().getY(), en.getKey().getZ()-shell.getPivot().getZ())).filter(cull -> !cull.isEmpty() && shell.getLocalPos(cull.getBoundingBox().getCenter()).distanceTo(e.getPos()) < 5).collect(Collectors.toList());
		Box box = e.getBoundingBox();
		Vec3d cent = box.getCenter();
		Vector3d pos = new Vector3d(cent.x, cent.y, cent.z);
		Vector3d collisionEffect = new Vector3d();

		double xztolerance = 0.06;
		double ytolerance = 0.01;

		QuaternionOrientedBoundingBox entityBox = new QuaternionOrientedBoundingBox(
			pos,
			new Vector3d(box.getXLength(), box.getYLength(), box.getZLength()),
			new Quaterniond().identity()
		);

		pos.add(0, shellDir.y, 0);
		collisionPass(shapes, shell, collisionIterator, entityBox, pos, collisionEffect);
		tolerance(shellDir, backup, xztolerance, ytolerance);

		pos.add(0, 0, shellDir.z);
		collisionPass(shapes, shell, collisionIterator, entityBox, pos, collisionEffect);
		tolerance(shellDir, backup, xztolerance, ytolerance);

		pos.add(shellDir.x, 0, 0);
		collisionPass(shapes, shell, collisionIterator, entityBox, pos, collisionEffect);
		tolerance(shellDir, backup, xztolerance, ytolerance);

		if(collisionEffect.lengthSquared() > 0.0) {
			shellDir.set(pos.sub(new Vector3d(cent.x, cent.y, cent.z).sub(yeag.x, yeag.y, yeag.z), new Vector3d()));
			hasCollided = true;
		}

		return new WorldshellCollisionResult(new Vec3d(shellDir.x, shellDir.y, shellDir.z), hasCollided);
	}

	/**
	 * @author ryanhcode/ryanh6n
	 * **/
	protected static void collisionPass(@NotNull List<VoxelShape> shapes,
										@NotNull Worldshell shell,
										@NotNull Iterable<? extends VoxelShape> shellCollisionIterator,
										@NotNull QuaternionOrientedBoundingBox entityBox,
										@NotNull Vector3d pos,
										@NotNull Vector3d collisionEffect) {

		for(VoxelShape shape : shellCollisionIterator) {
			shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
				Vector3d min = new Vector3d(x1, y1, z1);
				Vector3d max = new Vector3d(x2, y2, z2);

				Vector3d obbDim = max.sub(min, new Vector3d());
				Vector3d obbPos = min.add(max, new Vector3d()).mul(0.5).add(-0.5, -0.5, -0.5);
				Quaterniond obbRot = shell.getRotation().get(new Quaterniond());

				obbPos = shell.getLocalPos(obbPos);
				if(pos.distanceSquared(obbPos) > 36) {
					return;
				}

				QuaternionOrientedBoundingBox shellBox = new QuaternionOrientedBoundingBox(
					obbPos,
					obbDim,
					obbRot
				);

				entityBox.setPosition(pos);

				Vector3d mtv = QuaternionOrientedBoundingBox.sat(entityBox, shellBox);
				pos.add(mtv);
				collisionEffect.add(mtv);
				shapes.add(shape);
			});
		}

	}

	/**
	 * @author ryanhcode/ryanh6n
	 * **/
	protected static void tolerance(@NotNull Vector3d vec1,
									@NotNull Vector3d vec2,
									double xztolerance,
									double ytolerance) {
		if (Math.abs(vec1.x - vec2.x) < xztolerance) {
			vec1.set(vec2.x, vec1.y, vec1.z);
		}

		if (Math.abs(vec1.y - vec2.y) < ytolerance) {
			vec1.set(vec1.x, vec2.y, vec1.z);
		}

		if (Math.abs(vec1.z - vec2.z) < xztolerance) {
			vec1.set(vec1.x, vec1.y, vec2.z);
		}
	}

	/**
	 * @author arathain
	 * **/
	public static boolean boxCollidesSphere(Box b, Vec3d vec, double radius) {
		double distance = closestAABBPointSquareDistance(new Vector3d(vec.x, vec.y, vec.z), b);
		return distance <= radius * radius;
	}

	/**
	 * @author arathain
	 * **/
	public static double closestAABBPointSquareDistance(Vector3d vec, Box b) {
		double sqDist = 0.0f;
		for(int i = 0; i < 3; i++ ){
			double v = vec.get(i);
			Direction.Axis a = null;
			switch (i) {
				case 0 -> a = Direction.Axis.X;
				case 1 -> a = Direction.Axis.Y;
				case 2 -> a = Direction.Axis.Z;
			}
			if(v < b.getMin(a)) {
				sqDist += (b.getMin(a) - v) * (b.getMin(a) - v);
			}
			if(v > b.getMax(a)) {
				sqDist += (v - b.getMax(a)) * (v - b.getMax(a));
			}
		}
		return sqDist;
	}
}
