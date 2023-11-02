package arathain.miku_machines.content.item;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.init.ConnateItems;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.List;

public class ConnateBracerUpdateNBTPacket {

	public static final Identifier ID = MikuMachines.id("connate_bracer_update_nbt");

	public static void send(BlockPos target) {
		if(target != null) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeBlockPos(target);
			ClientPlayNetworking.send(ID, buf);
		}
	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf payload, PacketSender responseSender) {
		BlockPos target = payload.isReadable() ? payload.readBlockPos() : null;
		server.execute(() -> {
			ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
			if(stack.isOf(ConnateItems.CONNATE_BRACER) && target != null) {
				List<BlockBox> boxes = ConnateBracerItem.getBlockBoxes(stack);
				boxes.removeIf(b -> b.isInside(target));
				ConnateBracerItem.putBoxList(stack.getOrCreateNbt(), ConnateBracerItem.makeBlockList(boxes));
			}
		});

	}
}
