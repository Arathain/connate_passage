package arathain.miku_machines.content.block;

import arathain.miku_machines.MikuMachines;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * Server-to-client packet used for {@link ConnateDeresonator} and {@link ConnatePulseNode} visual effects.
 **/
public record ResonanceVFXPacket(Vec3d position, boolean b) implements Packet<ClientPlayPacketListener> {
	public static final Identifier ID = new Identifier(MikuMachines.MODID, "resonance_fx");


	@Override
	public void write(PacketByteBuf buf) {
		buf.writeDouble(position.x);
		buf.writeDouble(position.y);
		buf.writeDouble(position.z);
		buf.writeBoolean(b);
	}

	@Override
	public void apply(ClientPlayPacketListener listener) {

	}

	public static ResonanceVFXPacket fromBuf(PacketByteBuf buf) {
		Vec3d vec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new ResonanceVFXPacket(vec, buf.readBoolean());
	}
}
