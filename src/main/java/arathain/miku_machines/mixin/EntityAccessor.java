package arathain.miku_machines.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(Entity.class)
public interface EntityAccessor {
	@Invoker("adjustMovementForCollisions")
    Vec3d miku$invokeAdjustMovementForCollisions(Vec3d in);
}
