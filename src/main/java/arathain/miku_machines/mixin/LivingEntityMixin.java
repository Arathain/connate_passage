package arathain.miku_machines.mixin;

import arathain.miku_machines.logic.worldshell.Worldshell;
import arathain.miku_machines.logic.worldshell.WorldshellWrapperHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> variant, World world) {
		super(variant, world);
	}

	@ModifyArgs(method = "updateLimbs(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;magnitude(DDD)D"))
	private void connate$cancelVelocity(Args arg) {
		Worldshell s = ((WorldshellWrapperHolder) this).getWorldshell().shell;
		if(s != null) {
			Vec3d vel = s.getVelocity(this.getPos());
			arg.set(0, (double) arg.get(0) - vel.x);
			arg.set(1, (double) arg.get(1) - vel.y);
			arg.set(2, (double) arg.get(2) - vel.z);
		}
	}
	@ModifyVariable(method = "tick()V", at = @At("STORE"), ordinal = 0)
	private double connate$worldshellRotationX(double og) {
		Worldshell s = ((WorldshellWrapperHolder) this).getWorldshell().shell;
		if(s != null) {
			Vec3d vel = s.getVelocity(this.getPos());
			og -= vel.x;
		}
		return og;
	}
	@ModifyVariable(method = "tick()V", at = @At("STORE"), ordinal = 1)
	private double connate$worldshellRotationZ(double og) {
		Worldshell s = ((WorldshellWrapperHolder) this).getWorldshell().shell;
		if(s != null) {
			Vec3d vel = s.getVelocity(this.getPos());
			og -= vel.z;
		}
		return og;
	}
}
