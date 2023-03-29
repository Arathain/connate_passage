package arathain.connatepassage;

import arathain.connatepassage.init.ConnateBlocks;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.init.ConnateWorldshells;
import com.google.common.reflect.Reflection;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
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
	}
	public static Identifier id(String name) {
		return new Identifier(MODID, name);
	}
}
