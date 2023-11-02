package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.init.ConnateWorldComponents;
import arathain.miku_machines.content.cca.WorldshellComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.List;

/**
 * A packet responsible for updating all trivial parameters of a worldshell.
 **/
public class WorldshellUpdatePacket {
	public static final Identifier ID = MikuMachines.id("update_wshells");

	public static void send(List<ServerPlayerEntity> players, List<Worldshell> shells, int i) {
		if(shells.size() > 0) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeNbt(shells.get(i).writeUpdateNbt(new NbtCompound()));
			buf.writeInt(i);
			ServerPlayNetworking.send(players, ID, buf);
		}
	}

	public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf payload, PacketSender responseSender) {
		WorldshellComponent c = handler.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS);
		NbtCompound toRead = payload.readUnlimitedNbt();
		int i = payload.readInt();
		if(c.getWorldshells().size() > i){
			client.execute(() -> c.getWorldshells().get(i).readUpdateNbt(toRead));
		}
	}
}
