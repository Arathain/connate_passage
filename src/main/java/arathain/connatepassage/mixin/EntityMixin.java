package arathain.connatepassage.mixin;

import arathain.connatepassage.logic.ryanhcode.WorldshellCollisionPass;
import arathain.connatepassage.logic.worldshell.WorldshellWrapper;
import arathain.connatepassage.logic.worldshell.WorldshellWrapperHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
