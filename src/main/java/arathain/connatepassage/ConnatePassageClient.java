package arathain.connatepassage;

import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.content.item.ConnateBracerItem;
import arathain.connatepassage.init.ConnateBlocks;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.logic.worldshell.Worldshell;
import arathain.connatepassage.logic.worldshell.WorldshellUpdatePacket;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sammy.lodestone.setup.LodestoneRenderLayers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
		WorldRenderEvents.AFTER_ENTITIES.register((a) -> {
			renderSelected(a.world(), a.matrixStack(), a.consumers(), a.camera());
			renderWorldshells(a.world(), a.matrixStack(), a.consumers(), a.world().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells(), a.camera(), a.tickDelta());
		});
		ClientPlayNetworking.registerGlobalReceiver(WorldshellUpdatePacket.ID, WorldshellUpdatePacket::apply);
	}
	private static void renderSelected(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, Camera camera) {
		MinecraftClient c = MinecraftClient.getInstance();
		matrices.push();
		matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
		if(c.player != null) {
			ItemStack s = c.player.getMainHandStack();
			if (s.isOf(ConnateItems.CONNATE_BRACER)) {
				List<Box> positions = ConnateBracerItem.getTrailBlocks(s).stream().map(b -> new Box(b.getX(), b.getY(), b.getZ(), b.getX() + 1, b.getY() + 1, b.getZ() + 1)).toList();;
				List<Box> boxes = ConnateBracerItem.getBlockBoxes(s).stream().map(b -> new Box(b.getMinX(), b.getMinY(), b.getMinZ(), b.getMaxX() + 1, b.getMaxY() + 1, b.getMaxZ() + 1)).toList();
				for (Box b : positions) {
					WorldRenderer.drawBox(matrices, consumer.getBuffer(RenderLayer.getLines()), b, 0.6f, 0.9f, 1, 0.6f);
				}
				for (Box b : boxes) {
					WorldRenderer.drawBox(matrices, consumer.getBuffer(RenderLayer.getLines()), b, 0.7f, 0.9f, 1, 0.8f);
				}
			}
		}
		matrices.pop();
		//WorldRenderer.drawBox();
	}
	private static void renderWorldshells(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, List<Worldshell> shells, Camera camera, float tickDelta) {
		MinecraftClient c = MinecraftClient.getInstance();
		BlockRenderManager blockRenderManager = c.getBlockRenderManager();
		RandomGenerator r = RandomGenerator.createLegacy();
		for(Worldshell shell : shells) {
			matrices.push();
			Vec3d pos = shell.getPos(tickDelta);
			matrices.translate(-camera.getPos().getX(), -camera.getPos().getY(), -camera.getPos().getZ());
			matrices.translate(pos.x, pos.y, pos.z);
			matrices.multiply(shell.getRotation(tickDelta));
			for(Map.Entry<BlockPos, BlockState> entry : shell.getContained().entrySet()) {
				BlockPos blockPos = entry.getKey().subtract(shell.getPivot());
				BlockState state = entry.getValue();
				VertexConsumer drawer = consumer.getBuffer(RenderLayers.getBlockLayer(state));
				BakedModel model = c.getBakedModelManager().getBlockModels().getModel(state);

				if(!state.isAir()) {
					matrices.push();

					matrices.translate(blockPos.getX()-0.5, blockPos.getY()-0.5, blockPos.getZ()-0.5);

					blockRenderManager.getModelRenderer().render(world, model, state, entry.getKey(), matrices, drawer, true, r, state.getRenderingSeed(entry.getKey()), OverlayTexture.DEFAULT_UV);
					matrices.pop();
				}
			}
			matrices.pop();

		}
	}
}
