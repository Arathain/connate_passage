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
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import team.lodestar.lodestone.setup.LodestoneParticles;
import team.lodestar.lodestone.setup.LodestoneRenderLayers;
import team.lodestar.lodestone.systems.rendering.VFXBuilders;
import team.lodestar.lodestone.systems.rendering.particle.Easing;
import team.lodestar.lodestone.systems.rendering.particle.WorldParticleBuilder;
import team.lodestar.lodestone.systems.rendering.particle.data.ColorParticleData;
import team.lodestar.lodestone.systems.rendering.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.rendering.particle.data.SpinParticleData;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static team.lodestar.lodestone.handlers.RenderHandler.DELAYED_RENDER;

public class MikuMachinesClient implements ClientModInitializer {
	private static final Color parryStart = Color.CYAN;
	private static final Color parryEnd = new Color(60, 200, 220);
	private static final Identifier LIGHT_TRAIL = new Identifier(MikuMachines.MODID, "textures/vfx/sammy_trail.png");
	private static final RenderLayer LIGHT_TYPE = LodestoneRenderLayers.SCROLLING_TEXTURE.apply(LIGHT_TRAIL);
	public static boolean isIrisInstalled = QuiltLoader.isModLoaded("iris");


	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.BEFORE_ENTITIES.register((a) -> {
			renderWorldshells(a.world(), a.matrixStack(), a.consumers(), a.world().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells(), a.camera(), a.tickDelta());
			renderSelected(a.world(), a.matrixStack(), a.consumers(), a.camera());
		});
		ClientPlayNetworking.registerGlobalReceiver(ResonanceVFXPacket.ID, (client, handler, buf, responseSender) -> applyResonance(client, ResonanceVFXPacket.fromBuf(buf), handler));
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
		ClientPlayNetworking.registerGlobalReceiver(WorldshellUpdatePacket.ID, MikuMachinesClient::applyWorldshellUpdate);
		MidnightConfig.init("miku_machines", ConnateConfig.class);
	}

	/**
	 * Displays VFX based off of a {@link ResonanceVFXPacket}.
	 **/
	public static void applyResonance(MinecraftClient client, ResonanceVFXPacket p, ClientPlayPacketListener listener) {
		client.executeTask(() -> {
			if (listener instanceof ClientPlayNetworkHandler handler) {
				ClientPlayerEntity player = MinecraftClient.getInstance().player;

				WorldParticleBuilder.create(LodestoneParticles.STAR_PARTICLE)
					.setScaleData(GenericParticleData.create(p.b() ? 0.01f : 3.5f, p.b() ? 3.5f : 0.01f).build())
					.setLifetime(8)
					.setColorData(ColorParticleData.create(parryStart, parryEnd).setCoefficient(0.9f).setEasing(Easing.QUAD_IN).build())
					.setTransparencyData(GenericParticleData.create(p.b() ? 0.8f : 0, p.b() ? 0f : 0.8f).setEasing(Easing.QUAD_OUT).build())
					.enableNoClip()
					.setSpinData(SpinParticleData.create(player.getRandom().nextFloat() * MathHelper.PI * 2).build())
					.spawn(player.getWorld(), p.position().x, p.position().y, p.position().z);

			}

		});
	}
	/**
	 * Receives and applies a {@link WorldshellUpdatePacket}
	 **/
	public static void applyWorldshellUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf payload, PacketSender responseSender) {
		World world = handler.getWorld() == null ? client.world : handler.getWorld();
		if(world != null) {
			WorldshellComponent c = world.getComponent(ConnateWorldComponents.WORLDSHELLS);
			NbtCompound toRead = payload.readUnlimitedNbt();
			int i = payload.readInt();
			if (c.getWorldshells().size() > i) {
				client.execute(() -> c.getWorldshells().get(i).readUpdateNbt(toRead));
			}
		}
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
		BlockEntityRenderDispatcher d = c.getBlockEntityRenderDispatcher();
		WorldshellRenderCache cache = shell.getCache();
		if(!cache.isRendered()) {
			cache.reset();
			drawToCache(c, b, world, consumer, shell, r, tickDelta);
		}
		cache.draw(matrices);
		for(Map.Entry<BlockPos, BlockState> entry : shell.getContained().entrySet()) {
			BlockPos blockPos = entry.getKey().subtract(shell.getPivot());
			matrices.push();
			matrices.translate(blockPos.getX()-0.5, blockPos.getY()-0.5, blockPos.getZ()-0.5);
			if(shell.getContainedEntities().containsKey(entry.getKey())) {
				d.render(shell.getContainedEntities().get(entry.getKey()), tickDelta, matrices, consumer);
			}
			matrices.pop();
		}
		matrices.pop();
	}

	private static void drawToCache(MinecraftClient c, BlockRenderManager b, ClientWorld world, VertexConsumerProvider consumer, Worldshell shell, RandomGenerator  r, float tickDelta) {
		MatrixStack matrices = new MatrixStack();
		WorldshellRenderCache cache = shell.getCache();
		for(Map.Entry<BlockPos, BlockState> entry : shell.getContained().entrySet()) {
			BlockPos blockPos = entry.getKey().subtract(shell.getPivot());
			BlockState state = entry.getValue();
			matrices.push();
			matrices.translate(blockPos.getX()-0.5, blockPos.getY()-0.5, blockPos.getZ()-0.5);
			if(state.getRenderType() != BlockRenderType.INVISIBLE) {
				b.renderBlock(state, blockPos, shell, matrices, cache.get(RenderLayers.getBlockLayer(state)), true, r);
			}
			matrices.pop();
		}
		cache.upload();
	}

	/**
	 * Renders all in-game {@link Worldshell}s
	 **/
	public static void renderWorldshells(ClientWorld world, MatrixStack matrices, VertexConsumerProvider consumer, List<Worldshell> shells, Camera camera, float tickDelta) {
		MinecraftClient c = MinecraftClient.getInstance();
		BlockRenderManager blockRenderManager = c.getBlockRenderManager();
		RandomGenerator r = RandomGenerator.createLegacy();
		matrices.push();
		Vec3d vec = camera.getPos();
		matrices.translate(-vec.getX(), -vec.getY(), -vec.getZ());
		for(Worldshell shell : shells) {
			if(shell.getPos().distanceTo(vec) < c.options.getViewDistance().get()*16+shell.maxDistance)
				renderWorldshell(c, blockRenderManager, world, matrices, consumer, shell, r, tickDelta);
		}
		matrices.pop();
	}
}
