package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.content.cca.WorldshellComponent;
import arathain.miku_machines.init.ConnateWorldComponents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;

public class WorldshellBlockUpdatePacket {
	public static final Identifier ID = MikuMachines.id("wshell_set_block");

	public static void send(int ordinal, BlockState state, BlockPos... positions) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(ordinal);
		if(positions.length == 1) {
			buf.writeFromIterable(Block.STATE_IDS, state);
			buf.writeBlockPos(positions[0]);
			ClientPlayNetworking.send(ID, buf);
			return;
		}
		buf.writeFromIterable(Block.STATE_IDS, state);
		for(BlockPos p : positions)
			buf.writeBlockPos(p);
		ClientPlayNetworking.send(ID, buf);

	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf payload, PacketSender responseSender) {
		int ordinal = payload.isReadable() ? payload.readInt() : -1;
		BlockState state = payload.readFromIterable(Block.STATE_IDS);
		List<BlockPos> positions = new ArrayList<>();
		while(payload.isReadable()) {
			positions.add(payload.readBlockPos());
		}

		server.execute(() -> {
			WorldshellComponent c = player.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS);
			if(c.getWorldshells().size() > 0 && ordinal != -1){
				Worldshell s = c.getWorldshells().get(ordinal);
				for(BlockPos pos : positions)
					s.setBlockState(pos, state);
			}
		});

	}
}
