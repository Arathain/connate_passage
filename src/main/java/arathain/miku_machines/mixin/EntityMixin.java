package arathain.miku_machines.mixin;

import arathain.miku_machines.logic.ryanhcode.WorldshellCollision;
import arathain.miku_machines.logic.worldshell.WorldshellWrapper;
import arathain.miku_machines.logic.worldshell.WorldshellWrapperHolder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements WorldshellWrapperHolder {
	@Unique
	private WorldshellCollision.CollisionResult tempCollisionResult = null;

	@Unique
	private Vec3d shellVelocity = new Vec3d(0, 0, 0);
	@Unique
	private final WorldshellWrapper shell = new WorldshellWrapper();

	@Override
	public WorldshellWrapper getWorldshell() {
		return shell;
	}

	@Shadow
	public abstract World getWorld();

	@Shadow
	private World world;

	/**
	 * Universal worldshell collision hook.
	 * */
	@ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"), index = 0)
	private Vec3d connate$collideWorldshells(Vec3d movement) {
		WorldshellCollision.CollisionResult result = WorldshellCollision.get(world).collide(((Entity)(Object) this), movement);
		tempCollisionResult = result;
		return result.motion();
	}

	@Inject(method = "move", at = @At("TAIL"))
	public void move(CallbackInfo ci) {
		if (tempCollisionResult != null) {
			if (tempCollisionResult.hasImpulse())
				shellVelocity = tempCollisionResult.stickMotion();

			shellVelocity = WorldshellCollision.get(world).collidePost((Entity) (Object) this, tempCollisionResult, shellVelocity);

			tempCollisionResult = null;
		}
	}


}
