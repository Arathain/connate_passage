package arathain.connatepassage.content.item;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.content.block.HingeBlock;
import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.init.ConnateWorldshells;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static arathain.connatepassage.content.block.HingeBlock.AXIS;

public class ConnateBracerItem extends Item {
	public ConnateBracerItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack s = user.getStackInHand(hand);
		return TypedActionResult.consume(s);
	}

//	@Override
//	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
//		super.onStoppedUsing(stack, world, user, remainingUseTicks);
//		if(!user.isSneaking()) {
//			HitResult r = user.raycast(64, 1, false);
//			if (r.getType().equals(HitResult.Type.BLOCK)) {
//				putBlock(stack, ((BlockHitResult) r).getBlockPos());
//			}
//		}
//	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ItemStack s = context.getStack();
		BlockPos pos = context.getBlockPos();
		if(!context.getPlayer().isSneaking()) {
			if (!s.hasNbt() || s.getNbt().contains("first")) {
				putBlock(s, context.getBlockPos());
			}
		} else {
			if(context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof HingeBlock) {
				HashMap<BlockPos, BlockState> stateMap = new HashMap<>();
				AtomicReference<BlockState> state = new AtomicReference<>();
				for(BlockBox b : getBlockBoxes(s)) {
					forEachBlockPos(b, blockPos -> {
						if(!stateMap.containsKey(blockPos)) {
							BlockState st = context.getWorld().getBlockState(blockPos);
							stateMap.put(blockPos, st);
							context.getWorld().removeBlock(blockPos, false);
							if(blockPos.equals(pos)) {
								state.set(st);
							}
						}
					});
				}
				if(!stateMap.containsKey(pos)) {
					return ActionResult.FAIL;
				}

				Vector3f axis;
				switch (state.get().get(AXIS)) {
					case X -> axis = new Vector3f(1, 0, 0);
					case Y -> axis = new Vector3f(0, 1, 0);
					default -> axis = new Vector3f(0, 0, 1);
				}
				context.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS).getWorldshells().add(ConnateWorldshells.AXIS_LIMITED.create(stateMap, Vec3d.ofCenter(pos), pos).putAxis(axis));
				ConnateWorldComponents.WORLDSHELLS.sync(context.getWorld());
				context.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
				return ActionResult.CONSUME;
			}
		}
		return super.useOnBlock(context);
	}
	private static void forEachBlockPos(BlockBox b, Consumer<BlockPos> hungy) {
		for(int x = b.getMinX(); x <= b.getMaxX(); x++) {
			for(int y = b.getMinY(); y <= b.getMaxY(); y++) {
				for(int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
					hungy.accept(new BlockPos(x, y, z));
				}
			}
		}
	}
	public static List<BlockBox> getBlockBoxes(ItemStack stack) {
		NbtList n = getBoxList(stack);
		List<BlockBox> b = new ArrayList<>();
		if(n == null) {
			return b;
		}
		n.forEach(nbt -> b.add(getBlockBox((NbtCompound) nbt)));
		return b;
	}
	private static BlockBox getBlockBox(NbtCompound nbt) {
		return ConnatePassage.makeBlockBoxIndiscriminate(NbtHelper.toBlockPos(nbt.getCompound("first")), NbtHelper.toBlockPos(nbt.getCompound("second")));
	}
	public static ItemStack putBlock(ItemStack stack, BlockPos pos) {
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtList l = getBoxList(stack);
		if(l == null) {
			l = new NbtList();
		}
		if(!nbt.contains("first")) {
			nbt.put("first", NbtHelper.fromBlockPos(pos));
			return stack;
		} else {
			NbtCompound blocks = new NbtCompound();
			blocks.put("first", nbt.getCompound("first"));
			blocks.put("second", NbtHelper.fromBlockPos(pos));
			l.add(blocks);
			putBoxList(nbt, l);
			return stack;
		}
	}

	public static NbtList getBoxList(ItemStack stack) {
		if(stack.hasNbt()) {
			if(stack.getNbt().contains("boxes")) {
				return stack.getNbt().getList("boxes", 10);
			}
		}
		return null;
	}
	public static void putBoxList(NbtCompound nbt, NbtList list) {
		nbt.put("boxes", list);
	}

	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
}
