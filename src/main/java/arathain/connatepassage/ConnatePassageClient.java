package arathain.connatepassage;

import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.logic.worldshell.Worldshell;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import java.util.List;

public class ConnatePassageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.AFTER_ENTITIES.register((a) -> renderWorldshells(a.world(), a.matrixStack(), a.consumers(), a.world().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells()));
	}
	private static void renderWorldshells(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, List<Worldshell> shells) {

	}
}
