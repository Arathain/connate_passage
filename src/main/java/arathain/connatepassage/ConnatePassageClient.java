package arathain.connatepassage;

import arathain.connatepassage.config.ConnateConfig;
import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.content.cca.WorldshellComponent;
import arathain.connatepassage.content.item.ConnateBracerItem;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.logic.ConnateMathUtil;
import arathain.connatepassage.logic.worldshell.ConstantAxisLimitedWorldshell;
import arathain.connatepassage.logic.worldshell.ScrollableWorldshell;
import arathain.connatepassage.logic.worldshell.Worldshell;
import arathain.connatepassage.logic.worldshell.WorldshellUpdatePacket;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
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
		HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
			PlayerEntity player = MinecraftClient.getInstance().player;

			if(player != null && !player.isSpectator()) {

				if(player.getMainHandStack().getItem() instanceof ConnateBracerItem) {
					WorldshellComponent c = player.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS);
					Worldshell worldshell = null;
					for (int i = 0; i < c.getWorldshells().size(); i++) {
						Worldshell shell = c.getWorldshells().get(i);
						if (shell instanceof ScrollableWorldshell s ) {
							Vec3d diff = shell.getPos().subtract(player.getPos());
							float length = (float) diff.length();
							if(length < 128) {
								diff = diff.normalize();
								if (diff.dotProduct(player.getRotationVecClient()) > 1 - (0.1 / Math.pow(length, 1 / 2f))) {
									worldshell = shell;
								}
							}
						}
					}
					if(worldshell instanceof ConstantAxisLimitedWorldshell constShell) {
						TextRenderer r = MinecraftClient.getInstance().textRenderer;
						int x  = guiGraphics.getScaledWindowWidth()/2;
						int y  = guiGraphics.getScaledWindowHeight()/2;
						guiGraphics.drawText(r, constShell.getSpeedHz() + "Hz", x+10, y, 0x99E6FF, true);
					}
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(WorldshellUpdatePacket.ID, WorldshellUpdatePacket::apply);
		MidnightConfig.init("connatepassage", ConnateConfig.class);
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
	}


	private static void renderWorldshell(MinecraftClient c, BlockRenderManager b, ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, Worldshell shell, RandomGenerator  r, float tickDelta) {
		matrices.push();
		Vec3d pos = shell.getPos(tickDelta);
		matrices.translate(pos.x, pos.y, pos.z);
		matrices.multiply(shell.getRotation(tickDelta));
		for(Map.Entry<BlockPos, BlockState> entry : shell.getContained().entrySet()) {
			BlockPos blockPos = entry.getKey().subtract(shell.getPivot());
			BlockState state = entry.getValue();

			if(state.getRenderType() != BlockRenderType.INVISIBLE) {
				matrices.push();
				matrices.translate(blockPos.getX()-0.5, blockPos.getY()-0.5, blockPos.getZ()-0.5);

				b.renderBlock(state, blockPos, shell, matrices, consumer.getBuffer(RenderLayers.getBlockLayer(state)), true, r);
				matrices.pop();
			}
		}
		matrices.pop();
	}
	private static void renderWorldshells(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, List<Worldshell> shells, Camera camera, float tickDelta) {
		MinecraftClient c = MinecraftClient.getInstance();
		BlockRenderManager blockRenderManager = c.getBlockRenderManager();
		RandomGenerator r = RandomGenerator.createLegacy();
		matrices.push();
		Vec3d vec = camera.getPos();
		matrices.translate(-vec.getX(), -vec.getY(), -vec.getZ());
		for(Worldshell shell : shells) {
			renderWorldshell(c, blockRenderManager, world, matrices, consumer, shell, r, tickDelta);
		}
		matrices.pop();
	}
}
