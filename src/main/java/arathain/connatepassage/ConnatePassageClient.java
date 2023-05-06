package arathain.connatepassage;

import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.logic.worldshell.Worldshell;
import arathain.connatepassage.logic.worldshell.WorldshellUpdatePacket;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.List;
import java.util.Map;

public class ConnatePassageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.AFTER_ENTITIES.register((a) -> renderWorldshells(a.world(), a.matrixStack(), a.consumers(), a.world().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells(), a.camera()));
		ClientPlayNetworking.registerGlobalReceiver(WorldshellUpdatePacket.ID, WorldshellUpdatePacket::apply);
	}
	private static void renderWorldshells(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, List<Worldshell> shells, Camera camera) {
		MinecraftClient c = MinecraftClient.getInstance();
		BlockRenderManager blockRenderManager = c.getBlockRenderManager();
		RandomGenerator r = RandomGenerator.createLegacy();
		for(Worldshell shell : shells) {
			matrices.push();
			Vec3d pos = shell.getPos();
			matrices.translate(-camera.getPos().getX(), -camera.getPos().getY(), -camera.getPos().getZ());
			matrices.translate(pos.x, pos.y, pos.z);
			matrices.multiply(shell.getRotation(c.getTickDelta()));
			for(Map.Entry<BlockPos, BlockState> entry : shell.getContained().entrySet()) {
				BlockPos blockPos = entry.getKey().subtract(shell.getPivot());
				BlockState state = entry.getValue();
				VertexConsumer drawer = consumer.getBuffer(RenderLayers.getBlockLayer(state));
				BakedModel model = c.getBakedModelManager().getBlockModels().getModel(state);

				if(!state.isAir()) {
					matrices.push();

					matrices.translate(blockPos.getX()-0.5, blockPos.getY()-0.5, blockPos.getZ()-0.5);

					blockRenderManager.getModelRenderer().render(world, model, state, blockPos, matrices, drawer, true, r, state.getRenderingSeed(blockPos), OverlayTexture.DEFAULT_UV);
					matrices.pop();
				}
			}
			matrices.pop();

		}
	}
}
