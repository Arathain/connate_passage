package arathain.connatepassage.init;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ConnateBlocks {
	Map<Identifier, Block> BLOCKS = new LinkedHashMap<>();
	static <T extends Block> T register(String id, T block) {
		return register(id, block, true);
	}

	static <T extends Block> T register(String id, T block, boolean createItem) {
		Identifier identity = new Identifier(ConnatePassage.MODID, id);
		BLOCKS.put(identity, block);
		if(createItem) {
			ConnateItems.ITEMS.put(identity, new BlockItem(block, new Item.Settings()));
		}
		return block;
	}
	static void init() {
		BLOCKS.forEach((id, block) -> Registry.register(Registries.BLOCK, id, block));
	}
}
