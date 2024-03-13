package arathain.miku_machines.logic.ryanhcode;

import arathain.miku_machines.init.ConnateWorldComponents;
import arathain.miku_machines.logic.ConnateMathUtil;
import arathain.miku_machines.logic.worldshell.Worldshell;
import arathain.miku_machines.mixin.EntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class WorldshellCollision {

	public final static WorldshellCollision SERVER = new WorldshellCollision();
	public final static WorldshellCollision CLIENT = new WorldshellCollision();

	public static WorldshellCollision get(World level) {
		return level.isClient ? CLIENT : SERVER;
	}

	public static ProjectionContext getContext(World level) {
		return get(level).context;
	}

	private final Vector3d preDim = new Vector3d();
	private final Vector3d col = new Vector3d();
	private final Vector3d up = new Vector3d(0, 1, 0);
	private final Vector3d stickVelo = new Vector3d();
	private final Vector3d blockExtents = new Vector3d();
	private final Vector3d backup = new Vector3d();
	private final Vector3d offsetPosition = new Vector3d();
	private final Vector3d normalizedSAT = new Vector3d();
	private final Vector3d lastMTV = new Vector3d();
	private final Vector3d local = new Vector3d();
	private final Vector3d projectedLocal = new Vector3d();
	private final Vector3d velo = new Vector3d();
	private final Vector3d playerExtents = new Vector3d();
	private final Vector3d delta = new Vector3d();
	private final Vector3d jomlAABBCenter = new Vector3d();
	private final Vector3d globalPosition = new Vector3d();

	public final ProjectionContext context = new ProjectionContext();

	private final Pose lastPoseSubstep = new Pose();
	private final Pose currentPoseSubstep = new Pose();

	private final QuatOBB playerCollider = new QuatOBB(new Vector3d(), new Vector3d(), new Quaterniond(), context);
	private final QuatOBB shellCollider = new QuatOBB(new Vector3d(), new Vector3d(), new Quaterniond(), context);

	/**
	 * It's really quite shrimple. Not very clamplicated if you think about it.
	 * You just have to go into it at the right angler.
	 *
	 * @param entity The entity to collide with shells
	 * @param motion Desired motion vector
	 * @return Clamped motion vector
	 */
	public CollisionResult collide(Entity entity, Vec3d motion) {
		World level = entity.getWorld();
		double stepHeight = entity.getStepHeight();

		// Player collision matters a lot more to us than actual entities.
		// We're willing to spend more performance on it.
		int substeps = (entity instanceof PlayerEntity && entity.getWorld().isClient()) ? 30 : 1;

		// Backup to compare to by the end.
		// If the difference is extremely negligible, just return the original motion.
		Vec3d originalMotion = motion;

		Vec3d substeppedMotion = motion.multiply(1.0 / substeps);

		Box expandedBounding = entity.getBoundingBox().expand(40.0);

        Collection<Worldshell> allShells = new ArrayList<>(level.getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells().stream().filter(w -> boxCollidesSphere(expandedBounding, w.getPos(), w.maxDistance)).toList());

		if (allShells.isEmpty() || entity.isSpectator()) return new CollisionResult(motion, Vec3d.ZERO, Vec3d.ZERO, false);

		// Keeping track of the entities global position across substeps
		globalPosition.set(entity.getX(), entity.getY(), entity.getZ());

		// How much the entity should move by "sticking" to the shell
		stickVelo.set(0,0,0);

		// Seperation Impulse
		boolean impulse = false;

		for (int i = 0; i < substeps; i++) {
			Iterator<Worldshell> shells = allShells.iterator();

			while (shells.hasNext()) {
				Worldshell shell = shells.next();

				// If the shell doesn't contain the entity, we don't care about it anymore.
//				if (!shell.getShellBoundingBox().intersects(expandedBounding)) {
//					shells.remove();
//					continue;
//				}

				Pose lastPose = shell.getPrevPose();
				Pose currentPose = level.isClient ? shell.getSmoothedPose(MinecraftClient.getInstance().getTickDelta()) : shell.getPose();

				lastPose.lerp(currentPose, (i - 1) / (float) substeps, lastPoseSubstep);
				lastPose.lerp(currentPose, (i) / (float) substeps, currentPoseSubstep);

				Box boundingBox = entity.getBoundingBox();
				boundingBox = boundingBox.offset(JOMLConversions.toMinecraft(globalPosition).subtract(boundingBox.getCenter().subtract(0.0, boundingBox.getYLength() / 2, 0.0)));

				Collection<Box> shellBoxes = new ArrayList<>();

				for (BlockPos pos : shell.getContained().keySet()) {

					BlockState state = shell.getContained().get(pos);

					if (entity instanceof LivingEntity le && state.getBlock() instanceof ScaffoldingBlock) {
						if (le.isSneaking())
							shellBoxes.add(new Box(0, 0, 0, 0, 0, 0));
						else if (le.getY() > currentPoseSubstep.fromWorldToProjected(Vec3d.ofCenter(pos.subtract(shell.getPivot()))).y + 0.4)
							shellBoxes.add(new Box(0, 15 / 16f, 0, 1f, 1f, 1f).offset(
								pos.getX() - 0.5 - shell.getPivot().getX(),
								pos.getY() - 0.5 - shell.getPivot().getY(),
								pos.getZ() - 0.5 - shell.getPivot().getZ()));

						continue;
					}

					if (state.isAir()) continue;

					VoxelShape shape = state.getCollisionShape(level, pos);

					if (shape == VoxelShapes.empty() || shape.isEmpty()) continue;

					List<Box> aabbs = shape.getBoundingBoxes();

					for (Box aabb : aabbs) {
						shellBoxes.add(aabb.offset(pos.getX()-0.5-shell.getPivot().getX(), pos.getY()-0.5-shell.getPivot().getY(), pos.getZ()-0.5-shell.getPivot().getZ()));
					}
				}

				// Player box!
				playerExtents.set(boundingBox.getXLength(), boundingBox.getYLength(), boundingBox.getZLength());

				double offset = playerExtents.y / 2;

				// Backup used to compute normal!
				globalPosition.add(0, offset, 0);

				backup.set(globalPosition);

				Quaterniond current = currentPoseSubstep.getOrientation();
				double angleDiff = 2 * current.y / current.w;

				playerCollider.set(globalPosition, playerExtents, new Quaterniond().rotateY(angleDiff));

				boolean close = false;
				double proximityRequirement = level.isClient ? 0.5 : 0.45;

				if (!entity.isOnGround()) stepHeight = 0.0;

				globalPosition.add(0, substeppedMotion.y, 0);

				close = pass(shellBoxes, globalPosition, playerCollider, proximityRequirement, stepHeight) || close;

				globalPosition.add(0, 0, substeppedMotion.z);

				close = pass(shellBoxes, globalPosition, playerCollider, proximityRequirement, stepHeight) || close;

				globalPosition.add(substeppedMotion.x, 0, 0);

				close = pass(shellBoxes, globalPosition, playerCollider, proximityRequirement, stepHeight) || close;

				globalPosition.add(0, -offset, 0);

				if (close) {
					currentPoseSubstep.fromProjectedToWorld(local.set(globalPosition));
					globalPosition.sub(lastPoseSubstep.fromWorldToProjected(projectedLocal.set(local)), velo);

					impulse = true;

					stickVelo.add(velo);
					globalPosition.add(velo);
//					if (shell.putEntityBuffer(entity, 2) == null) {
//						entity.hasImpulse = true;
//					}
				}

				// remove the actual effects of the motion
				globalPosition.sub(substeppedMotion.x, substeppedMotion.y, substeppedMotion.z);
			}

			// Add them back. Lmao
			globalPosition.add(substeppedMotion.x, substeppedMotion.y, substeppedMotion.z);
		}

		globalPosition.sub(stickVelo);
		Vec3d newMotion = JOMLConversions.toMinecraft(globalPosition).subtract(entity.getPos());

		// If the difference is extremely negligible, just return the original motion.
		// Tolerance every axis individually
		double tolerance = 0.01;

		if (Math.abs(newMotion.x - originalMotion.x) < tolerance) {
			newMotion = new Vec3d(originalMotion.x, newMotion.y, newMotion.z);
		}

		if (Math.abs(newMotion.y - originalMotion.y) < tolerance) {
			newMotion = new Vec3d(newMotion.x, originalMotion.y, newMotion.z);
		}

		if (Math.abs(newMotion.z - originalMotion.z) < tolerance) {
			newMotion = new Vec3d(newMotion.x, newMotion.y, originalMotion.z);
		}

		return new CollisionResult(newMotion, JOMLConversions.toMinecraft(stickVelo), JOMLConversions.toMinecraft(stickVelo), impulse);
	}

	public QuatOBB transform(Box aabb, Pose pose) {
		Vec3d aabbcenter = aabb.getCenter();
		jomlAABBCenter.set(aabbcenter.x, aabbcenter.y, aabbcenter.z);

		pose.fromWorldToProjected(jomlAABBCenter);
		blockExtents.set(aabb.getXLength(), aabb.getYLength(), aabb.getZLength());

		//CreateClient.OUTLINER.showAABB("quatobb" + aabb, new AABB(jomlAABBCenter.x, jomlAABBCenter.y, jomlAABBCenter.z, jomlAABBCenter.x, jomlAABBCenter.y, jomlAABBCenter.z).inflate(0.5));

		shellCollider.set(jomlAABBCenter, blockExtents, pose.getOrientation());
		return shellCollider;
	}


	private boolean pass(Collection<Box> shellBoxes,
						 Vector3d position,
						 QuatOBB playerCollider, double proximityRequirement, double stepHeight) {
		boolean close = false;

		col.set(0, 0, 0);
		boolean anyHorizontalNormals = false;

		for (Box aabb : shellBoxes) {
			QuatOBB shipCollider = transform(aabb, currentPoseSubstep);
			playerCollider.setPosition(position);
			if(playerCollider.getPosition().distance(shipCollider.getPosition()) > 2)
				continue;
			Vector3dc sat = QuatOBB.satToleranced(playerCollider, shipCollider, 0.1);

			if (sat.lengthSquared() > 1E-7) {
				close = true;
			}

			if (Math.abs(sat.normalize(normalizedSAT).dot(0, 1, 0)) < 0.1) anyHorizontalNormals = true;

			position.add(sat);
			col.add(sat);
		}

		if (stepHeight > 1E-7 && col.y >= 0.0 && col.lengthSquared() > 1E-7) {
			if (anyHorizontalNormals/*Math.abs(col.normalize().dot(new Vector3d(0, 1, 0))) < 0.2*/) {

				int substeps = 16;
				lastMTV.set(0,0,0);
				for (int i = 0; i <= substeps; i++) {
					double yOffset = i / (double) substeps * stepHeight;
					playerCollider.setPosition(position.add(0, yOffset, 0, offsetPosition).sub(col.mul(0.99 + ((double) i / substeps) * 5.0, new Vector3d())));

					// collide the player against every collider...
					boolean anyCollision = false;

					for (Box aabb : shellBoxes) {
						QuatOBB shipCollider = transform(aabb, currentPoseSubstep);

						Vector3dc sat = QuatOBB.sat(playerCollider, shipCollider);

						if (sat.lengthSquared() > 1E-7) {
							lastMTV.set(sat);
							anyCollision = true;
						}
					}

					if (!anyCollision) {
						// check if the last normal was upwards
						double d = lastMTV.normalize().dot(up);

						if (d > 0.75 && i > 0)
							position.add(0, yOffset, 0).add(col.mul(-0.09));

						break;
					}
				}
			}
		}

		if (!close) {
			preDim.set(playerCollider.getDimensions());

			playerCollider.getDimensions().add(proximityRequirement, proximityRequirement, proximityRequirement);

			for (Box aabb : shellBoxes) {
				QuatOBB shipCollider = transform(aabb, currentPoseSubstep);

				playerCollider.setPosition(position);
				Vector3dc sat = QuatOBB.sat(playerCollider, shipCollider);

				if (sat.lengthSquared() > 1E-7) {
					close = true;
				}
			}

			playerCollider.setDimensions(preDim);
		}

		return close;
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

	public Vec3d collidePost(Entity entity, CollisionResult col, Vec3d shellVelocity) {
		if (shellVelocity.lengthSquared() > 1e-12) {
			Vec3d shellVelo = ((EntityAccessor) entity).miku$invokeAdjustMovementForCollisions(shellVelocity);
			entity.setPosition(entity.getPos().add(shellVelo));

			return shellVelo;
		}

		return Vec3d.ZERO;
	}

	public record CollisionResult(Vec3d motion, Vec3d stickMotion, Vec3d impulse, boolean hasImpulse) {
	}
}
