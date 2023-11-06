package arathain.miku_machines;

import arathain.miku_machines.config.ConnateConfig;
import arathain.miku_machines.content.block.ResonanceVFXPacket;
import arathain.miku_machines.content.cca.WorldshellComponent;
import arathain.miku_machines.content.item.ConnateBracerItem;
import arathain.miku_machines.init.ConnateBlocks;
import arathain.miku_machines.init.ConnateItems;
import arathain.miku_machines.init.ConnateWorldComponents;
import arathain.miku_machines.logic.worldshell.*;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import team.lodestar.lodestone.setup.LodestoneRenderLayers;
import team.lodestar.lodestone.systems.rendering.VFXBuilders;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static team.lodestar.lodestone.handlers.RenderHandler.DELAYED_RENDER;

public class MikuMachinesClient implements ClientModInitializer {
	private static final Identifier LIGHT_TRAIL = new Identifier(MikuMachines.MODID, "textures/vfx/sammy_trail.png");
	private static final RenderLayer LIGHT_TYPE = LodestoneRenderLayers.SCROLLING_TEXTURE.apply(LIGHT_TRAIL);
	public static boolean isIrisInstalled = QuiltLoader.isModLoaded("iris");

	//TODO annotate
	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.BEFORE_ENTITIES.register((a) -> {
			renderSelected(a.world(), a.matrixStack(), a.consumers(), a.camera());
			renderWorldshells(a.world(), a.matrixStack(), a.consumers(), a.world().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells(), a.camera(), a.tickDelta());
		});
		ClientPlayNetworking.registerGlobalReceiver(ResonanceVFXPacket.ID, (client, handler, buf, responseSender) -> ResonanceVFXPacket.fromBuf(buf).apply(handler));
		BlockRenderLayerMap.put(RenderLayer.getCutout(), ConnateBlocks.DERESONATOR);
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
					if(worldshell instanceof SplineFollowingAxisLimitedWorldshell hell) {
						TextRenderer r = MinecraftClient.getInstance().textRenderer;
						int x  = guiGraphics.getScaledWindowWidth()/2;
						int y  = guiGraphics.getScaledWindowHeight()/2;
						guiGraphics.drawText(r, hell.getSpeed() + " b/s", x+10, y, 0x99E6FF, true);
					}
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(WorldshellUpdatePacket.ID, WorldshellUpdatePacket::apply);
		MidnightConfig.init("miku_machines", ConnateConfig.class);
	}
	/**
	 * Renders all boxes selected via the {@link ConnateBracerItem}
	 **/
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


	/**
	 * Renders a {@link Worldshell} based off of its attributes
	 **/
	private static void renderWorldshell(MinecraftClient c, BlockRenderManager b, ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, Worldshell shell, RandomGenerator  r, float tickDelta) {
		matrices.push();
		Vec3d pos = shell.getPos(tickDelta);
		matrices.translate(pos.x, pos.y, pos.z);
		if(shell instanceof SplineFollowingAxisLimitedWorldshell sp && ConnateConfig.renderCarriage) {
			matrices.push();
			List<Vec3d> positions = sp.getPoints(tickDelta, 17, 0.6f);
			VFXBuilders.WorldVFXBuilder builder = VFXBuilders.createWorld().setPosColorTexLightmapDefaultFormat();

			float size = 0.3f;
			float alpha = 0.9f;

			builder.setColor(Color.CYAN).setOffset((float) -pos.x, (float) -pos.y, (float) -pos.z)
				.setAlpha(alpha)
				.renderTrail(
					DELAYED_RENDER.getBuffer(LIGHT_TYPE),
					matrices,
					positions.stream()
						.map(p -> new Vector4f((float) p.x, (float) p.y, (float) p.z, 1))
						.toList(),
					f -> MathHelper.sqrt(0.5f-MathHelper.abs(f - 0.5f)) * size,
					f -> builder.setAlpha(MathHelper.sqrt(0.5f-MathHelper.abs(f - 0.5f)) * alpha)
				)
				.renderTrail(
					DELAYED_RENDER.getBuffer(LIGHT_TYPE),
					matrices,
					positions.stream()
						.map(p -> new Vector4f((float) p.x, (float) p.y, (float) p.z, 1))
						.toList(),
					f -> MathHelper.sqrt(0.5f-MathHelper.abs(f - 0.5f)) * size,
					f -> builder.setAlpha(MathHelper.sqrt(0.5f-MathHelper.abs(f - 0.5f)) * alpha)
				);

			matrices.pop();
		}
		Quaternionf q = shell.getRotation(tickDelta);
		matrices.multiply(q);
		for(Map.Entry<BlockPos, BlockState> entry : shell.getContained().entrySet()) {
			BlockPos blockPos = entry.getKey().subtract(shell.getPivot());
			BlockState state = entry.getValue();

			if(state.getRenderType() != BlockRenderType.INVISIBLE) {
				matrices.push();
				matrices.translate(blockPos.getX()-0.5, blockPos.getY()-0.5, blockPos.getZ()-0.5);

				b.renderBlock(state, blockPos, shell, matrices, consumer.getBuffer(RenderLayers.getBlockLayer(state)), true, r);
				if(state.hasBlockEntity()) {

				}
				matrices.pop();
			}
		}
		matrices.pop();
	}
	public static void renderWorldshells(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, List<Worldshell> shells, Camera camera, float tickDelta) {
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
