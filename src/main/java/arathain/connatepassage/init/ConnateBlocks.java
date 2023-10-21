package arathain.connatepassage.init;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.content.block.*;
import arathain.connatepassage.content.block.entity.WorldshellBlockEntity;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Initialisation interface used for registering in-game blocks.
 **/
public interface ConnateBlocks {
	Map<Identifier, Block> BLOCKS = new LinkedHashMap<>();
	Map<Identifier, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new LinkedHashMap<>();
	static <T extends Block> T register(String id, T block) {
		return register(id, block, true);
	}
	Block HINGE = register("hinge", new HingeBlock(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).nonOpaque()));
	Block SPLINE = register("spline_carriage", new SplineBlock(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK)));
	Block PULSE_NODE = register("pulse_node", new ConnatePulseNode(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).luminance(createLightLevelFromLitBlockState(10)).nonOpaque()));
	Block DERESONATOR = register("deresonator", new ConnateDeresonator(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).nonOpaque()));
	Block BATTERY = register("battery", new ConnateBatteryBlock(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).luminance(createLightLevelFromLitBlockState(10)), false));
	Block BATTERY_INVERSE = register("battery_inverse", new ConnateBatteryBlock(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).luminance(createLightLevelFromLitBlockState(2)), true));
	Block CHASSIS = register("chassis", new Block(QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK)));

	BlockEntityType<WorldshellBlockEntity> HINGE_BLOCK_ENTITY = register("hinge", QuiltBlockEntityTypeBuilder.create(WorldshellBlockEntity::hinge, HINGE).build());
	BlockEntityType<WorldshellBlockEntity> SPLINE_BLOCK_ENTITY = register("spline", QuiltBlockEntityTypeBuilder.create(WorldshellBlockEntity::spline, SPLINE).build());

	static <T extends Block> T register(String id, T block, boolean createItem) {
		Identifier identity = new Identifier(ConnatePassage.MODID, id);
		BLOCKS.put(identity, block);
		if(createItem) {
			ConnateItems.ITEMS.put(identity, new BlockItem(block, new Item.Settings()));
		}
		return block;
	}
	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
		BLOCK_ENTITY_TYPES.put(new Identifier(ConnatePassage.MODID, name), type);
		return type;
	}

	private static ToIntFunction<BlockState> createLightLevelFromLitBlockState(int litLevel) {
		return (state) -> (Boolean)state.get(ConnateBatteryBlock.TRIGGERED) ? litLevel : 0;
	}
	static void init() {
		BLOCKS.forEach((id, block) -> Registry.register(Registries.BLOCK, id, block));
	}
}
