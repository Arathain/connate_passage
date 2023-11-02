package arathain.miku_machines.mixin;

import arathain.miku_machines.logic.ryanhcode.WorldshellCollisionPass;
import arathain.miku_machines.logic.worldshell.WorldshellWrapper;
import arathain.miku_machines.logic.worldshell.WorldshellWrapperHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin implements WorldshellWrapperHolder {
	@Unique
	private final WorldshellWrapper shell = new WorldshellWrapper();

	@Override
	public WorldshellWrapper getWorldshell() {
		return shell;
	}

	@Shadow
	public abstract World getWorld();

	@Shadow
	public abstract Box getBoundingBox();

	@Shadow
	public abstract boolean isOnGround();

	@ModifyReturnValue(method = "adjustMovementForCollisions", at = @At(value = "RETURN") )
	private Vec3d connate$collideWorldshells(Vec3d movement) {
		return WorldshellCollisionPass.collideWithWorldshells(this.getWorld(), shell, (Entity) (Object) this, movement);
	}

}
