package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.init.ConnateWorldshells;
import arathain.miku_machines.logic.ConnateMathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.color.biome.BiomeColorProvider;
import net.minecraft.client.world.ClientWorld;
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
import org.joml.*;

import java.lang.Math;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * The base class used to represent abstract moving block structures. Also acts as a fake world to simulate block detection and rendering.
 * **/
public abstract class Worldshell implements BlockRenderView {
	protected boolean invertedMotion;
	public int shutdownTickCountdown = 0;
	private Supplier<World> worldGetter = null;
	protected final Map<BlockPos, BlockState> contained;
	protected final Map<BlockPos, BlockEntity> containedEntities;
	protected Quaternionf rotation, prevRotation = new Quaternionf();
	protected Vec3d prevPos, pos;
	protected final BlockPos pivot;
	public final double maxDistance;

	@Nullable
	private Worldshell parent;

	private List<Worldshell> children;

	public Worldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot) {
		this.contained = contained;
		containedEntities = new HashMap<>();
		this.pos = initialPos;
		this.pivot = pivot;
		this.prevPos = pos;
		this.maxDistance = computeSize();
	}

	/**
	 * Computes the maximum radius of a sphere that can encompass the entire worldshell - used for optimising collision checks
	 **/
	protected double computeSize() {
		AtomicReference<Double> size = new AtomicReference<>((double) 1);
		contained.keySet().forEach(c -> {
			double dist = Math.sqrt(c.getSquaredDistance(pivot));
			if(dist > size.get()) {
				size.set(dist);
			}
		});
		size.set(size.get() + 2);
		return size.get();
	}
	public void setWorld(World world) {
		worldGetter = () -> world;
	}

	/**
	 * Returns the {@link Identifier} of the worldshell to reconstruct from during reload, as taken from {@link ConnateWorldshells}
	 **/
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
		return prevRotation.slerp(rotation, tickDelta, new Quaternionf());
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

	/**
	 * Naive worldshell velocity implementation, not taking rotation into account.
	 **/
	public Vec3d getVelocity() {
		return pos.subtract(prevPos);
	}
	/**
	 * Rotational velocity implementation.
	 **/
	public Vec3d getRotationalVelocity(Vec3d entityPos) {
		entityPos = entityPos.subtract(this.pos);
		Quaternionf c = new Quaternionf().identity().mul(rotation.invert(new Quaternionf()));
		Quaternionf p = new Quaternionf().identity().mul(prevRotation.invert(new Quaternionf()));
		p.invert().mul(c).normalize();
		return ConnateMathUtil.rotateViaQuat(entityPos, p).subtract(entityPos);
	}

	public float getYawVelocity(float delta) {
		Quaternionf c = new Quaternionf().identity().mul(prevRotation.slerp(rotation, delta, new Quaternionf()).invert(new Quaternionf()));
		Quaternionf p = new Quaternionf().identity().mul(prevRotation.invert(new Quaternionf()));
		p.invert().mul(c).normalize();
		Vector3f f = p.getEulerAnglesZYX(new Vector3f());
		return f.y * 180f / MathHelper.PI;
	}

	/**
	 * Full worldshell velocity implementation, taking rotational velocity into account.
	 **/
	public Vec3d getVelocity(Vec3d entityPos) {
		Vec3d diff = getRotationalVelocity(entityPos);
		return getVelocity().subtract(diff);
	}

	//The code below is all 'world' implementation methods, used for Minecraft's underlying code to correctly interact with the worldshell.
	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		boolean bl = worldGetter.get() instanceof ClientWorld c && c.getSkyProperties().isDarkened();
		Vector3f properDirection = getLocalVec(direction);
		if (!shaded) {
			return bl ? 0.9F : 1.0F;
		} else {
			float brightness = 0;
			for(Direction dir : Direction.values()) {
				brightness += Math.max(dir.getUnitVector().dot(properDirection), 0) * getDirectionBrightness(dir, bl);
			}
			return MathHelper.clamp(brightness, 0, 1);
		}
	}
	private float getDirectionBrightness(Direction dir, boolean bl) {
		return switch (dir) {
			case DOWN -> bl ? 0.9F : 0.5F;
			case UP -> bl ? 0.9F : 1.0F;
			case NORTH, SOUTH -> 0.8F;
			case WEST, EAST -> 0.6F;
			default -> 1.0F;
		};
	}

	private Direction getLocal(Direction dir) {
		Vector3f v = dir.getUnitVector();
		v.rotate(this.getRotation());
		return Direction.getFacing(v.x, v.y, v.z);
	}
	private Vector3f getLocalVec(Direction dir) {
		Vector3f v = dir.getUnitVector();
		v.rotate(this.getRotation());
		return v;
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
		return contained.getOrDefault(pos.add(pivot), Blocks.AIR.getDefaultState());
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

	public Vec3d getLocalPos(Vec3d bPos) {
		return new Vec3d(pos.x, pos.y, pos.z).add(ConnateMathUtil.rotateViaQuat(bPos, rotation));
	}
	public Vec3d getLocalPos(Vec3d bPos, float tickDelta) {
		return new Vec3d(pos.x, pos.y, pos.z).add(ConnateMathUtil.rotateViaQuat(bPos, getRotation(tickDelta)));
	}
	public Vector3d getLocalPos(Vector3d bPos) {
		return new Vector3d(pos.x, pos.y, pos.z).add(bPos.rotate(rotation.get(new Quaterniond())));
	}
	public Vector3d getLocalPos(Vector3d bPos, float tickDelta) {
		return new Vector3d(pos.x, pos.y, pos.z).add(bPos.rotate(getRotation(tickDelta).get(new Quaterniond())));
	}

	private BlockPos getLocalPos(BlockPos bPos) {
		Vec3d vec = new Vec3d(pos.x, pos.y, pos.z).add(ConnateMathUtil.rotateViaQuat(new Vec3d(bPos.getX(), bPos.getY(), bPos.getZ()), rotation));
		return new BlockPos(MathHelper.floor(vec.getX()), MathHelper.floor(vec.getY()), MathHelper.floor(vec.getZ()));
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
