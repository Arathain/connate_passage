package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.logic.ConnateMathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.color.biome.BiomeColorProvider;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Worldshell implements BlockRenderView {
	protected boolean invertedMotion;
	public int shutdownTickCountdown = 0;
	private Supplier<World> worldGetter = null;
	protected final Map<BlockPos, BlockState> contained;
	protected Quaternionf rotation, prevRotation = new Quaternionf();
	protected Vec3d prevPos, pos;
	protected final BlockPos pivot;

	public Worldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot) {
		this.contained = contained;
		this.pos = initialPos;
		this.pivot = pivot;
		this.prevPos = pos;
	}
	public void setWorld(World world) {
		worldGetter = () -> world;
	}

	public abstract Identifier getId();

	public void tick() {
		this.prevPos = pos;
	}
	public void outerTick() {
		this.tick();
	}
	public void activate(int countdown, boolean invert) {
		this.invertedMotion = invert;
		this.shutdownTickCountdown = countdown;
	}
	public Quaternionf getRotation() {
		return getRotation(1);
	}
	public Quaternionf getRotation(float tickDelta) {
		checkRotation();
		return prevRotation.slerp(rotation, tickDelta);
	}

	public BlockPos getPivot() {
		return pivot;
	}

	public void setPos(Vec3d pos) {
		this.pos = pos;
	}
	public void rotate(float pitch, float yaw, float roll) {
		rotation.rotateZYX(roll, yaw, pitch);
	}
	public void setRotation(Quaternionf quat) {
		rotation.set(quat);
	}

	public Vec3d getPos() {
		return pos;
	}
	public Vec3d getPos(float tickDelta) {
		return prevPos.lerp(pos, tickDelta);
	}

	public Map<BlockPos, BlockState> getContained() {
		return contained;
	}

	public void writeNbt(NbtCompound nbt) {
		writeUpdateNbt(nbt);

		NbtList list = new NbtList();
		contained.forEach((key, value) -> {
			NbtCompound compound = NbtHelper.fromBlockPos(key);
			compound.put("state", NbtHelper.fromBlockState(value));
			list.add(compound);
		});
		nbt.putString("id", getId().toString());
		nbt.put("containedBlocks", list);
		nbt.put("pivot", NbtHelper.fromBlockPos(pivot));
	}
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		checkRotation();
		nbt.putFloat("qX", rotation.x);
		nbt.putFloat("qY", rotation.y);
		nbt.putFloat("qZ", rotation.z);
		nbt.putFloat("qW", rotation.w);

		nbt.putDouble("pX", pos.x);
		nbt.putDouble("pY", pos.y);
		nbt.putDouble("pZ", pos.z);

		nbt.putInt("sCd", shutdownTickCountdown);
		nbt.putBoolean("invM", invertedMotion);
		return nbt;
	}
	protected void checkRotation() {
		if(rotation == null) {
			rotation = new Quaternionf();
		}
		if(prevRotation == null) {
			prevRotation = new Quaternionf();
		}
	}
	public void readUpdateNbt(NbtCompound nbt) {
		this.rotation = new Quaternionf(nbt.getFloat("qX"), nbt.getFloat("qY"), nbt.getFloat("qZ"), nbt.getFloat("qW"));
		this.pos = new Vec3d(nbt.getDouble("pX"), nbt.getDouble("pY"), nbt.getDouble("pZ"));
		this.shutdownTickCountdown = nbt.getInt("sCd");
		this.invertedMotion = nbt.getBoolean("invM");
	}
	public void readNbt(NbtCompound nbt) {
		readUpdateNbt(nbt);
	}
	public static BlockPos getBlockPosFromNbt(NbtCompound nbt) {
		return NbtHelper.toBlockPos(nbt.getCompound("pivot"));
	}

	public static Map<BlockPos, BlockState> getBlocksFromNbt(NbtCompound nbt) {
		NbtList list = nbt.getList("containedBlocks", 10);
		Map<BlockPos, BlockState> map = new HashMap<>();
		list.forEach(bNbt -> {
			map.put(NbtHelper.toBlockPos((NbtCompound) bNbt), NbtHelper.toBlockState(Registries.BLOCK.asLookup(), ((NbtCompound) bNbt).getCompound("state")));
		});
		return map;
	}

	//the janky zone

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		return worldGetter == null ? 0 : worldGetter.get().getBrightness(direction, shaded);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return worldGetter == null ? null : worldGetter.get().getLightingProvider();
	}

	@Override
	public int getColor(BlockPos pos, BiomeColorProvider biomeColorProvider) {
		return worldGetter == null ? -64 : worldGetter.get().getColor(getLocalPos(pos), biomeColorProvider);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return contained.getOrDefault(pos, Blocks.AIR.getDefaultState());
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return contained.containsKey(pos) ? contained.get(pos).getFluidState() : Fluids.EMPTY.getDefaultState();
	}

	@Override
	public int getHeight() {
		return worldGetter == null ? 256 : worldGetter.get().getHeight();
	}

	@Override
	public int getBottomY() {
		return worldGetter == null ? -64 : worldGetter.get().getBottomY();
	}

	private BlockPos getLocalPos(BlockPos bPos) {
		Vec3d vec = new Vec3d(pos.x, pos.y, pos.z).add(ConnateMathUtil.rotateViaQuat(Vec3d.ofCenter(bPos), rotation));
		return new BlockPos(MathHelper.ceil(vec.getX()), MathHelper.ceil(vec.getY()), MathHelper.ceil(vec.getZ())).add(-1, -1, 0);
	}

	@Override
	public int getLightLevel(LightType type, BlockPos pos) {
		return worldGetter == null ? 0 : worldGetter.get().getLightLevel(type, getLocalPos(pos));
	}

	@Override
	public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
		return worldGetter == null ? 0 : worldGetter.get().getBaseLightLevel(getLocalPos(pos), ambientDarkness);
	}

}
