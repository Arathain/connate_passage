package arathain.miku_machines;

import arathain.miku_machines.content.item.ConnateBracerUpdateNBTPacket;
import arathain.miku_machines.init.ConnateBlocks;
import arathain.miku_machines.init.ConnateItems;
import arathain.miku_machines.init.ConnateWorldshells;
import arathain.miku_machines.logic.worldshell.WorldshellAddSpeedPacket;
import com.google.common.reflect.Reflection;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class MikuMachines implements ModInitializer {
	public static String MODID = "miku_machines";

	@Override
	public void onInitialize(ModContainer mod) {
		MODID = mod.metadata().id();
		ConnateBlocks.init();
		ConnateItems.init();
		Reflection.initialize(ConnateWorldshells.class);
		ServerPlayNetworking.registerGlobalReceiver(ConnateBracerUpdateNBTPacket.ID, ConnateBracerUpdateNBTPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(WorldshellAddSpeedPacket.ID, WorldshellAddSpeedPacket::apply);
	}
	public static Identifier id(String name) {
		return new Identifier(MODID, name);
	}

	/**
	 * Creates a {@link BlockBox} with two block positions, automatically checking for correct order.
	 **/
	public static BlockBox makeBlockBoxIndiscriminate(BlockPos one, BlockPos two) {
		int minX, maxX, minY, maxY, minZ, maxZ;
		if(one.getX() > two.getX()) {
			minX = two.getX();
			maxX = one.getX();
		} else {
			maxX = two.getX();
			minX = one.getX();
		}
		if(one.getY() > two.getY()) {
			minY = two.getY();
			maxY = one.getY();
		} else {
			maxY = two.getY();
			minY = one.getY();
		}
		if(one.getZ() > two.getZ()) {
			minZ = two.getZ();
			maxZ = one.getZ();
		} else {
			maxZ = two.getZ();
			minZ = one.getZ();
		}
		return new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
