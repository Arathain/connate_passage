package arathain.miku_machines.mixin;

import arathain.miku_machines.logic.ryanhcode.WorldshellCollisionPass;
import arathain.miku_machines.logic.worldshell.Worldshell;
import arathain.miku_machines.logic.worldshell.WorldshellWrapper;
import arathain.miku_machines.logic.worldshell.WorldshellWrapperHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public abstract  class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

	@Shadow
	public float lastRenderPitch;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	/**
	 * Rotates the player across the Y-axis in accordance with collided worldshell.
	 * */
	@ModifyReturnValue(method = "getYaw(F)F", at = @At(value = "RETURN") )
	private float connate$collideWorldshells(float in, @Local float delta) {
		WorldshellWrapper w = ((WorldshellWrapperHolder) this).getWorldshell();
		Worldshell s = w.shell;
		if(s != null && w.hasCollided && w.isColliding) {
			in += s.getYawVelocity(delta);
		}
		return in;
	}
}
