package arathain.connatepassage.content.block;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import team.lodestar.lodestone.systems.rendering.particle.Easing;
import team.lodestar.lodestone.systems.rendering.particle.WorldParticleBuilder;
import team.lodestar.lodestone.setup.LodestoneParticles;
import team.lodestar.lodestone.systems.rendering.particle.data.ColorParticleData;
import team.lodestar.lodestone.systems.rendering.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.rendering.particle.data.SpinParticleData;

import java.awt.*;

/**
 * Server-to-client packet used for {@link ConnateDeresonator} and {@link ConnatePulseNode} visual effects.
 **/
public record ResonanceVFXPacket(Vec3d position, boolean b) implements Packet<ClientPlayPacketListener> {
	public static final Identifier ID = new Identifier(ConnatePassage.MODID, "resonance_fx");
	private static final Color parryStart = Color.CYAN;
	private static final Color parryEnd = new Color(60, 200, 220);

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeDouble(position.x);
		buf.writeDouble(position.y);
		buf.writeDouble(position.z);
		buf.writeBoolean(b);
	}

	public static ResonanceVFXPacket fromBuf(PacketByteBuf buf) {
		Vec3d vec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new ResonanceVFXPacket(vec, buf.readBoolean());
	}

	@ClientOnly
	@Override
	public void apply(ClientPlayPacketListener listener) {
		if (listener instanceof ClientPlayNetworkHandler handler) {
			PlayerEntity player = MinecraftClient.getInstance().player;

			WorldParticleBuilder.create(LodestoneParticles.STAR_PARTICLE)
					.setScaleData(GenericParticleData.create(b ? 0.01f : 3f, b ? 3f : 0.01f).build())
					.setLifetime(8)
					.setColorData(ColorParticleData.create(parryStart, parryEnd).setCoefficient(0.9f).setEasing(Easing.QUAD_IN).build())
					.setTransparencyData(GenericParticleData.create(b ? 0.8f : 0, b ? 0f : 0.8f).setEasing(Easing.QUAD_OUT).build())
					.enableNoClip()
					.setSpinData(SpinParticleData.create(player.getRandom().nextFloat()* MathHelper.PI*2).build())
					.spawn(player.getWorld(), position.x, position.y, position.z);

		}
	}
}
