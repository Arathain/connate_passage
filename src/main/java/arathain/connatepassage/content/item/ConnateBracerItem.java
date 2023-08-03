package arathain.connatepassage.content.item;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.content.block.HingeBlock;
import arathain.connatepassage.content.block.SplineBlock;
import arathain.connatepassage.content.cca.ConnateWorldComponents;
import arathain.connatepassage.content.cca.WorldshellComponent;
import arathain.connatepassage.init.ConnateBlocks;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.init.ConnateWorldshells;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.*;
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
		if(user.isSneaking()) {
			WorldshellComponent w = world.getComponent(ConnateWorldComponents.WORLDSHELLS);
			boolean[] bl = new boolean[1];
			w.getWorldshells().removeIf(shell -> {
				if(shell != null && shell.getPos().distanceTo(user.getPos()) < 8) {
					bl[0] = true;
					return w.snapWorldshell(shell);
				}
				return false;
			});
			if(!bl[0]) {
				Text msg = Text.literal("Block selection mode activated").formatted(Formatting.AQUA);
				if(switchMode(s)) {
					msg = Text.literal("Trail selection mode activated").formatted(Formatting.AQUA);
				}
				user.sendMessage(msg, true);
			}
			ConnateWorldComponents.WORLDSHELLS.sync(world);
			return TypedActionResult.consume(s);
		}
		if(isPositionMode(s)) {
			List<BlockPos> list = getTrailBlocks(s);
			if(!list.isEmpty() && user.getBlockPos().equals(list.get(0)) && !(list.stream().filter(b -> b.equals(list.get(0))).toList().size() == 1)) {
				list.remove(user.getBlockPos());
				s.getNbt().remove("trailBlocks");
				putTrailBlock(s, list.toArray(new BlockPos[]{}));
			} else {
				putTrailBlock(s, user.getBlockPos());
			}
		}
		return TypedActionResult.pass(s);
	}
	private static boolean switchMode(ItemStack s) {
		if(isPositionMode(s)) {
			s.getOrCreateNbt().remove("mode");
			return false;
		} else {
			s.getOrCreateNbt().putBoolean("mode", true);
			return true;
		}
	}
	public static boolean isPositionMode(ItemStack s) {
		return s.hasNbt() && s.getNbt().contains("mode");
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
			if(!isPositionMode(s))
				putBlock(s, context.getBlockPos());
		} else {
			if(context.getWorld().getBlockState(context.getBlockPos()).getBlock().equals(ConnateBlocks.CHASSIS)) {
				HashMap<BlockPos, BlockState> stateMap = new HashMap<>();
				for(BlockBox b : getBlockBoxes(s)) {
					forEachBlockPos(b, blockPos -> {
						if(!stateMap.containsKey(blockPos)) {
							BlockState st = context.getWorld().getBlockState(blockPos);
							if(!st.isAir()) {
								stateMap.put(blockPos, st);
								context.getWorld().setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.UPDATE_NONE);
							}
						}
					});
				}
				if(!stateMap.containsKey(pos)) {
					return ActionResult.FAIL;
				}
				s.getNbt().remove("first");
				s.getNbt().remove("boxes");
				context.getPlayer().setStackInHand(context.getHand(), new ItemStack(ConnateItems.CONNATE_BRACER));
				context.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS).addWorldshell(ConnateWorldshells.FREE.create(stateMap, Vec3d.ofCenter(pos), pos));
				ConnateWorldComponents.WORLDSHELLS.sync(context.getWorld());
				return ActionResult.CONSUME;
			} else if(context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof HingeBlock) {
				HashMap<BlockPos, BlockState> stateMap = new HashMap<>();
				AtomicReference<BlockState> state = new AtomicReference<>();
				for(BlockBox b : getBlockBoxes(s)) {
					forEachBlockPos(b, blockPos -> {
						if(!stateMap.containsKey(blockPos)) {
							BlockState st = context.getWorld().getBlockState(blockPos);
							if(!st.isAir()) {
								stateMap.put(blockPos, st);
								context.getWorld().removeBlock(blockPos, false);
								if (blockPos.equals(pos)) {
									state.set(st);
								}
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
				s.getNbt().remove("first");
				s.getNbt().remove("boxes");
				context.getPlayer().setStackInHand(context.getHand(), new ItemStack(ConnateItems.CONNATE_BRACER));
				context.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS).addWorldshell(ConnateWorldshells.AXIS_LIMITED.create(stateMap, Vec3d.ofCenter(pos), pos).putAxis(axis));
				ConnateWorldComponents.WORLDSHELLS.sync(context.getWorld());
				return ActionResult.CONSUME;
			} else if(context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof SplineBlock) {
				HashMap<BlockPos, BlockState> stateMap = new HashMap<>();
				AtomicReference<BlockState> state = new AtomicReference<>();
				for(BlockBox b : getBlockBoxes(s)) {
					forEachBlockPos(b, blockPos -> {
						if(!stateMap.containsKey(blockPos)) {
							BlockState st = context.getWorld().getBlockState(blockPos);
							if(!st.isAir()) {
								stateMap.put(blockPos, st);
								context.getWorld().removeBlock(blockPos, false);
								if (blockPos.equals(pos)) {
									state.set(st);
								}
							}
						}
					});
				}
				if(!stateMap.containsKey(pos)) {
					return ActionResult.FAIL;
				}

				Vector3f axis = state.get().get(SplineBlock.FACING).getUnitVector();
				s.getNbt().remove("first");
				s.getNbt().remove("boxes");
				context.getPlayer().setStackInHand(context.getHand(), new ItemStack(ConnateItems.CONNATE_BRACER));
				context.getWorld().getComponent(ConnateWorldComponents.WORLDSHELLS).addWorldshell(ConnateWorldshells.SPLINE.create(stateMap, Vec3d.ofCenter(pos), pos).constructSpline(getTrailBlocks(s).stream().map(Vec3d::ofCenter).toList().toArray(new Vec3d[]{})).putAxis(axis));
				ConnateWorldComponents.WORLDSHELLS.sync(context.getWorld());
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
	public static List<BlockBox> getBlockBoxes(NbtCompound comp) {
		NbtList n = getBoxList(comp);
		List<BlockBox> b = new ArrayList<>();
		if(n == null) {
			return b;
		}
		n.forEach(nbt -> b.add(getBlockBox((NbtCompound) nbt)));
		return b;
	}
	public static List<BlockPos> getTrailBlocks(ItemStack stack) {
		NbtList n = getBlockList(stack);
		List<BlockPos> b = new ArrayList<>();
		if(n == null) {
			return b;
		}
		n.forEach(nbt -> b.add(NbtHelper.toBlockPos((NbtCompound) nbt)));
		return b;
	}
	public static List<BlockPos> getTrailBlocks(NbtCompound comp) {
		NbtList n = getBlockList(comp);
		List<BlockPos> b = new ArrayList<>();
		if(n == null) {
			return b;
		}
		n.forEach(nbt -> b.add(NbtHelper.toBlockPos((NbtCompound) nbt)));
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
			nbt.remove("first");
			return stack;
		}
	}
	public static void putTrailBlock(ItemStack stack, BlockPos... pos) {
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtList l = getBlockList(stack);
		if(l == null) {
			l = new NbtList();
		}
		for(BlockPos position : pos) {
			l.add(NbtHelper.fromBlockPos(position));
		}
		putBlockList(nbt, l);
	}

	public static NbtList getBoxList(ItemStack stack) {
		if(stack.hasNbt()) {
			if(stack.getNbt().contains("boxes")) {
				return stack.getNbt().getList("boxes", 10);
			}
		}
		return null;
	}
	public static NbtList getBoxList(NbtCompound nbt) {
		if(nbt.contains("boxes")) {
			return nbt.getList("boxes", 10);
		}
		return null;
	}
	public static void putBoxList(NbtCompound nbt, NbtList list) {
		nbt.put("boxes", list);
	}
	public static NbtList getBlockList(ItemStack stack) {
		if(stack.hasNbt()) {
			if(stack.getNbt().contains("trailBlocks")) {
				return stack.getNbt().getList("trailBlocks", 10);
			}
		}
		return null;
	}
	public static NbtList getBlockList(NbtCompound nbt) {
		if(nbt.contains("trailBlocks")) {
			return nbt.getList("trailBlocks", 10);
		}
		return null;
	}
	public static void putBlockList(NbtCompound nbt, NbtList list) {
		nbt.put("trailBlocks", list);
	}

	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
}
