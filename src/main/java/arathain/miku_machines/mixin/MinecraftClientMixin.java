package arathain.miku_machines.mixin;

import arathain.miku_machines.content.item.ConnateBracerUpdateNBTPacket;
import arathain.miku_machines.init.ConnateItems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	@Nullable
	public HitResult crosshairTarget;

	/**
	 * Redirects block-breaking mechanic to give {@link arathain.miku_machines.content.item.ConnateBracerItem}s an additional input.
	 * */
	@ModifyExpressionValue(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEnabled(Lnet/minecraft/feature_flags/FeatureFlagBitSet;)Z", shift = At.Shift.BY))
	private boolean connate$fullBracers(boolean og, @Local ItemStack stack) {
		if(stack.isOf(ConnateItems.CONNATE_BRACER) && crosshairTarget.getType() == HitResult.Type.BLOCK) {
			ConnateBracerUpdateNBTPacket.send(((BlockHitResult) crosshairTarget).getBlockPos());
			return false;
		}
		return og;
	}
}
