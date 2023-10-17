package arathain.connatepassage;

import arathain.connatepassage.init.ConnateBlocks;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.init.ConnateWorldshells;
import arathain.connatepassage.logic.worldshell.WorldshellAddSpeedPacket;
import com.google.common.reflect.Reflection;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnatePassage implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("Connate Passage");
	public static String MODID = " ";

	@Override
	public void onInitialize(ModContainer mod) {
		MODID = mod.metadata().id();
		ConnateBlocks.init();
		ConnateItems.init();
		Reflection.initialize(ConnateWorldshells.class);
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
