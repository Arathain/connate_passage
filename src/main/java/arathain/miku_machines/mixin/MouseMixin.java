package arathain.miku_machines.mixin;

import arathain.miku_machines.init.ConnateWorldComponents;
import arathain.miku_machines.content.cca.WorldshellComponent;
import arathain.miku_machines.init.ConnateItems;
import arathain.miku_machines.logic.worldshell.ScrollableWorldshell;
import arathain.miku_machines.logic.worldshell.WorldshellAddSpeedPacket;
import arathain.miku_machines.logic.worldshell.Worldshell;
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

	/**
	 * Scrolling logic for adjusting worldshell speed.
	 * */
	@Inject(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void connate$bracerScroll(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci, double d, int yea) {
		if(client.player.getMainHandStack().isOf(ConnateItems.CONNATE_BRACER)) {
			WorldshellComponent c = client.world.getComponent(ConnateWorldComponents.WORLDSHELLS);
			int select = -1;
			for (int i = 0; i < c.getWorldshells().size(); i++) {
				Worldshell shell = c.getWorldshells().get(i);
				if (shell instanceof ScrollableWorldshell) {
					Vec3d diff = shell.getPos().subtract(client.player.getPos());
					float length = (float) diff.length();
					diff = diff.normalize();
					if (diff.dotProduct(client.player.getRotationVecClient()) > 1 - (0.1 / Math.pow(length, 1 / 2f))) {
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
