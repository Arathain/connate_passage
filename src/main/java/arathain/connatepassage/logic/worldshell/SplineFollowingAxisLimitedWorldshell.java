package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.logic.spline.CatmullRomCurveSpline;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class SplineFollowingAxisLimitedWorldshell extends AxisLimitedWorldshell implements ScrollableWorldshell {
	private float speed = 1f;
	private CatmullRomCurveSpline spline;
	public SplineFollowingAxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot, initialAxis);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		spline.writeNbt(nbt);
	}

	@Override
	public void readUpdateNbt(NbtCompound nbt) {
		//super.readUpdateNbt(nbt);
		this.spline.prevPos = nbt.getFloat("pos");
		this.speed = nbt.getFloat("speed");
	}
	@Override
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		nbt.putFloat("speed", speed);
		nbt.putFloat("pos", this.spline.pos);
		return nbt;
	}
	@Override
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "spline_following");
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.spline = CatmullRomCurveSpline.readNbt(nbt);
	}

	public void setSpeed(float spd) {
		speed = MathHelper.clamp(spd, -2, 2);
	}

	public float getSpeed() {
		return speed;
	}

	public SplineFollowingAxisLimitedWorldshell constructSpline(Vec3d... points) {
		this.spline = new CatmullRomCurveSpline(points);
		return this;
	}

	@Override
	public void tick() {
		super.tick();
		this.spline.moveLoop(speed);
		this.prevPos = this.pos;
		this.pos = this.spline.getPos(1);
		Vec3d prod = this.spline.getVelocity(1).normalize();
		checkRotation();
		this.prevRotation = rotation;
		this.axis = prod.toVector3f();
		this.rotation = new Quaternionf().rotateTo(new Vector3f(1, 0,0), axis);
	}
}
