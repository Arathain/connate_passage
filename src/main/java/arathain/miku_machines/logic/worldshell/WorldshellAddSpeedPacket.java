package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.init.ConnateWorldComponents;
import arathain.miku_machines.content.cca.WorldshellComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

/**
 * A packet responsible for updating worldshell speed.
 **/
public class WorldshellAddSpeedPacket {
	public static final Identifier ID = MikuMachines.id("wshell_add_speed");

	public static void send(int ordinal, double speedoru) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(ordinal);
		buf.writeDouble(speedoru);
		ClientPlayNetworking.send(ID, buf);
	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf payload, PacketSender responseSender) {
		int ordinal = payload.isReadable() ? payload.readInt() : -1;
		double speed = payload.isReadable() ? payload.readDouble() : 0;
		server.execute(() -> {
			WorldshellComponent c = player.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS);
			if(c.getWorldshells().size() > 0 && ordinal != -1){
				Worldshell s = c.getWorldshells().get(ordinal);
				if(s instanceof ScrollableWorldshell scr) {
					scr.addSpeed(speed);
				}
			}
		});

	}
}
