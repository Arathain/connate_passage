package arathain.miku_machines.mixin.iris;

import arathain.miku_machines.MikuMachinesClient;
import arathain.miku_machines.init.ConnateWorldComponents;
import com.llamalad7.mixinextras.sugar.Local;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShadowRenderer.class)
public class ShadowRendererMixin {
	/**
	 * Iris shader mod compatibility; re-renders worldshells in the shadow pass.
	 * */
	@Inject(method = "renderShadows", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/pipeline/ShadowRenderer;renderEntities(Lnet/coderbot/iris/mixin/LevelRendererAccessor;Lnet/minecraft/client/render/entity/EntityRenderDispatcher;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/util/math/MatrixStack;FLnet/minecraft/client/render/Frustum;DDD)I", shift = At.Shift.BEFORE))
	private void connate$irisShadows(LevelRendererAccessor par1, Camera par2, CallbackInfo ci, @Local(ordinal = 0) MatrixStack stack, @Local(ordinal = 0) VertexConsumerProvider.Immediate i, @Local(ordinal = 0) float delta) {
		MikuMachinesClient.renderWorldshells(par1.getLevel(), stack, i, par1.getLevel().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells(), par2, delta);
	}
}
