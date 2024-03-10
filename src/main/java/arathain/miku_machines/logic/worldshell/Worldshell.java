package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.init.ConnateWorldshells;
import arathain.miku_machines.logic.ConnateMathUtil;
import arathain.miku_machines.logic.ryanhcode.JOMLConversions;
import arathain.miku_machines.logic.ryanhcode.Pose;
import net.minecraft.block.BlockEntityProvider;
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
import org.quiltmc.loader.api.minecraft.ClientOnly;

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
	protected Pose prevPose = new Pose(), pose = new Pose();
	protected final BlockPos pivot;
	public final double maxDistance;

	@ClientOnly
	protected WorldshellRenderCache cache;

	@Nullable
	private Worldshell parent;

	private List<Worldshell> children;

	public Worldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot) {
		this.contained = contained;
		containedEntities = new HashMap<>();
		this.pivot = pivot;
		this.pose.setPosition(JOMLConversions.toJOML(initialPos));
		this.prevPose.setPosition(pose.getPosition());
		this.maxDistance = computeSize();
	}

	public WorldshellRenderCache getCache() {
		if(cache == null && this.worldGetter.get().isClient) {
	 		cache = new WorldshellRenderCache();
		}
		return this.cache;
	}

	public Worldshell uploadBlockEntities(Map<BlockPos, BlockEntity> contained) {
		containedEntities.putAll(contained);
		return this;
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
		prevPose = pose.copy();
	}

	/**
	 * Returns the {@link Identifier} of the worldshell to reconstruct from during reload, as taken from {@link ConnateWorldshells}
	 **/
	public abstract Identifier getId();

	public void tick() {
		prevPose = pose.copy();
	}
	public void outerTick() {
		this.tick();
	}
	public void activate(int countdown, boolean invert) {
		this.invertedMotion = invert;
		this.shutdownTickCountdown = countdown;
	}
	public Quaterniond getRotation() {
		return getRotation(1).normalize();
	}
	public Quaterniond getRotation(float tickDelta) {
		return prevPose.getOrientation().slerp(pose.getOrientation(), tickDelta, new Quaterniond()).normalize();
	}

	public BlockPos getPivot() {
		return pivot;
	}

	public void setPos(Vec3d pos) {
		this.pose.setPosition(JOMLConversions.toJOML(pos));
	}
	public void rotate(float pitch, float yaw, float roll) {
		this.pose.getOrientation().rotateZYX(roll, yaw, pitch);
	}
	public void setRotation(Quaterniond quat) {
		this.pose.setOrientation(quat);
	}

	public Vec3d getPos() {
		return JOMLConversions.toMinecraft(this.pose.getPosition());
	}
	public Vec3d getPos(float tickDelta) {
		if(prevPose.getPosition().equals(pose.getPosition())) {
			return JOMLConversions.toMinecraft(pose.getPosition());
		}
		return JOMLConversions.toMinecraft(prevPose.getPosition().lerp(pose.getPosition(), tickDelta, new Vector3d()));
	}

	public Map<BlockPos, BlockState> getContained() {
		return contained;
	}
	public Map<BlockPos, BlockEntity> getContainedEntities() {
		return containedEntities;
	}

	public Pose getPose() {
		return pose;
	}
	public Pose getPrevPose() {
		return prevPose;
	}
	public Pose getSmoothedPose(float delta) {
		return prevPose.lerp(pose, delta, new Pose());
	}

	public void writeNbt(NbtCompound nbt) {
		writeUpdateNbt(nbt);

		NbtList list = new NbtList();
		contained.forEach((key, value) -> {
			NbtCompound compound = NbtHelper.fromBlockPos(key);
			compound.put("s", NbtHelper.fromBlockState(value));
			list.add(compound);
		});
		NbtList listE = new NbtList();
		containedEntities.forEach((key, value) -> {
			if(value != null) {
				NbtCompound compound = NbtHelper.fromBlockPos(key);
				compound.put("e", value.toIdentifiedNbt());
				listE.add(compound);
			}
		});
		nbt.putString("id", getId().toString());
		nbt.put("containedBlocks", list);
		nbt.put("containedEntities", listE);
		nbt.put("pivot", NbtHelper.fromBlockPos(pivot));


	}
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		nbt.put("pose", pose.write());

		nbt.putInt("sCd", shutdownTickCountdown);
		nbt.putBoolean("invM", invertedMotion);
		return nbt;
	}
	public void readUpdateNbt(NbtCompound nbt) {
		this.pose.read(nbt.getCompound("pose"));
		this.shutdownTickCountdown = nbt.getInt("sCd");
		this.invertedMotion = nbt.getBoolean("invM");
	}
	public void readNbt(NbtCompound nbt) {
		this.containedEntities.clear();
		NbtList list = nbt.getList("containedEntities", 10);
		Map<BlockPos, BlockEntity> map = new HashMap<>();
		list.forEach(bNbt -> {
			BlockPos p = NbtHelper.toBlockPos((NbtCompound) bNbt);
			BlockEntity blockEntity = ((BlockEntityProvider)contained.get(p).getBlock()).createBlockEntity(p, contained.get(p));
			blockEntity.readNbt((NbtCompound) bNbt);
			blockEntity.setWorld(this.worldGetter.get());
			blockEntity.setCachedState(contained.get(p));
			map.put(p, blockEntity);
		});
		this.containedEntities.putAll(map);
		readUpdateNbt(nbt);
	}
	public static BlockPos getBlockPosFromNbt(NbtCompound nbt) {
		return NbtHelper.toBlockPos(nbt.getCompound("pivot"));
	}

	public static Map<BlockPos, BlockState> getBlocksFromNbt(NbtCompound nbt) {
		NbtList list = nbt.getList("containedBlocks", 10);
		Map<BlockPos, BlockState> map = new HashMap<>();
		list.forEach(bNbt -> {
			map.put(NbtHelper.toBlockPos((NbtCompound) bNbt), NbtHelper.toBlockState(Registries.BLOCK.asLookup(), ((NbtCompound) bNbt).getCompound("s")));
		});
		return map;
	}

	/**
	 * Naive worldshell velocity implementation, not taking rotation into account.
	 **/
	public Vec3d getVelocity() {
		return JOMLConversions.toMinecraft(pose.getPosition().sub(prevPose.getPosition(), new Vector3d()));
	}
	/**
	 * Rotational velocity implementation.
	 **/
	public Vec3d getRotationalVelocity(Vec3d entityPos) {
		entityPos = entityPos.subtract(JOMLConversions.toMinecraft(this.pose.getPosition()));
		Quaterniond c = new Quaterniond().identity().mul(pose.getOrientation().invert(new Quaterniond()));
		Quaterniond p = new Quaterniond().identity().mul(prevPose.getOrientation().invert(new Quaterniond()));
		p.invert().mul(c).normalize();
		return ConnateMathUtil.rotateViaQuat(entityPos, p).subtract(entityPos);
	}

	public float getYawVelocity(float delta) {
		Quaterniond c = new Quaterniond().identity().mul(prevPose.getOrientation().slerp(pose.getOrientation(), delta, new Quaterniond()).invert(new Quaterniond()));
		Quaterniond p = new Quaterniond().identity().mul(prevPose.getOrientation().invert(new Quaterniond()));
		p.invert().mul(c).normalize();
		Vector3d f = p.getEulerAnglesZYX(new Vector3d());
		return (float) f.y * 180f / MathHelper.PI;
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
				brightness += Math.max(dir.getUnitVector().dot(properDirection), 0) * Math.max(dir.getUnitVector().dot(properDirection), 0) * getDirectionBrightness(dir, bl);
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
	private static final Quaternionf jank = new Quaternionf();

	private Direction getLocal(Direction dir) {
		Vector3f v = dir.getUnitVector();
		v.rotate(this.getRotation().get(jank));
		return Direction.getFacing(v.x, v.y, v.z);
	}
	private Vector3f getLocalVec(Direction dir) {
		Vector3f v = dir.getUnitVector();
		v.rotate(this.getRotation().get(jank));
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
		return containedEntities.getOrDefault(pos, null);
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
		return new Vec3d(prevPose.getPosition().x, prevPose.getPosition().y, prevPose.getPosition().z).add(ConnateMathUtil.rotateViaQuat(bPos, pose.getOrientation()));
	}
	public Vec3d getLocalPos(Vec3d bPos, float tickDelta) {
		return new Vec3d(prevPose.getPosition().x, prevPose.getPosition().y, prevPose.getPosition().z).add(ConnateMathUtil.rotateViaQuat(bPos, getRotation(tickDelta)));
	}
	public Vector3d getLocalPos(Vector3d bPos) {
		return new Vector3d(prevPose.getPosition().x, prevPose.getPosition().y, prevPose.getPosition().z).add(bPos.rotate(pose.getOrientation().get(new Quaterniond())));
	}
	public Vector3d getLocalPos(Vector3d bPos, float tickDelta) {
		return new Vector3d(prevPose.getPosition().x, prevPose.getPosition().y, prevPose.getPosition().z).add(bPos.rotate(getRotation(tickDelta).get(new Quaterniond())));
	}

	private BlockPos getLocalPos(BlockPos bPos) {
		Vec3d vec = new Vec3d(prevPose.getPosition().x, prevPose.getPosition().y, prevPose.getPosition().z).add(ConnateMathUtil.rotateViaQuat(new Vec3d(bPos.getX(), bPos.getY(), bPos.getZ()), prevPose.getOrientation()));
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

	public void setBlockState(BlockPos pos, BlockState state) {
		contained.replace(pos, state);
	}

}
