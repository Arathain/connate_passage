package arathain.connatepassage.mixin;

import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.content.cca.WorldshellComponent;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.logic.worldshell.ScrollableWorldshell;
import arathain.connatepassage.logic.worldshell.WorldshellAddSpeedPacket;
import arathain.connatepassage.logic.worldshell.Worldshell;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public class MouseMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private double scrollDelta;

	@Inject(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void connate$bracerScroll(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci, double d, int yea) {
		if(client.player.getMainHandStack().isOf(ConnateItems.CONNATE_BRACER)) {
			WorldshellComponent c = client.world.getComponent(ConnateWorldComponents.WORLDSHELLS);
			int select = -1;
			for (int i = 0; i < c.getWorldshells().size(); i++) {
				Worldshell shell = c.getWorldshells().get(i);
				if (shell instanceof ScrollableWorldshell s) {
					Vec3d diff = shell.getPos().subtract(client.player.getPos());
					float length = (float) diff.length();
					diff = diff.normalize();
					if (diff.dotProduct(client.player.getRotationVecClient()) > 1 - 0.2 / Math.pow(length, 1 / 4f)) {
						select = i;
					}
				}
			}
			if (select != -1) {
				WorldshellAddSpeedPacket.send(select, yea / 64f);
				ci.cancel();
			}
		}
	}
}
