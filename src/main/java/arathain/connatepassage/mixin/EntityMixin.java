package arathain.connatepassage.mixin;

import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.logic.ryanhcode.WorldshellCollisionPass;
import arathain.connatepassage.logic.worldshell.Worldshell;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract World getWorld();

	@Shadow
	public abstract Box getBoundingBox();

	@ModifyReturnValue(method = "adjustMovementForCollisions", at = @At(value = "RETURN") )
	private Vec3d connate$collideWorldshells(Vec3d movement) {
		World world = this.getWorld();
		WorldshellCollisionPass.WorldshellCollisionResult r = new WorldshellCollisionPass.WorldshellCollisionResult(Vec3d.ZERO, false);
		Vector3d original = new Vector3d(movement.x, movement.y, movement.z);
		for(Worldshell w : world.getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells()) {
			if(WorldshellCollisionPass.boxCollidesSphere(this.getBoundingBox(), w.getPos(), w.maxDistance)) {
				r = WorldshellCollisionPass.collide(((Entity)(Object)this), r.collision(), w, original);
			}
		}
		return r.hasCollided() ? r.collision() : movement;
	}
}
