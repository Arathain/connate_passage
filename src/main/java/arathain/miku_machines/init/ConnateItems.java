package arathain.miku_machines.init;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.content.item.ConnateBracerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Initialisation interface used for registering in-game items.
 **/
public interface ConnateItems {
	Map<Identifier, Item> ITEMS = new LinkedHashMap<>();
	Item CONNATE_BRACER = register("copper_bracers", new ConnateBracerItem(new QuiltItemSettings().maxCount(1).rarity(Rarity.RARE)));

	static <T extends Item> T register(String id, T item) {
		ITEMS.put(new Identifier(MikuMachines.MODID, id), item);
		return item;
	}

	static void init() {
		ITEMS.forEach((id, item) -> Registry.register(Registries.ITEM, id, item));
	}

}
