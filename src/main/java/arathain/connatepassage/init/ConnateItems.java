package arathain.connatepassage.init;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ConnateItems {
	Map<Identifier, Item> ITEMS = new LinkedHashMap<>();

	static <T extends Item> T register(String id, T item) {
		ITEMS.put(new Identifier(ConnatePassage.MODID, id), item);
		return item;
	}

	static void init() {
		ITEMS.forEach((id, item) -> Registry.register(Registries.ITEM, id, item));
	}

}
