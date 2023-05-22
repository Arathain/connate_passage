package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.logic.spline.CatmullRomCurveSpline;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class SplineFollowingAxisLimitedWorldshell extends AxisLimitedWorldshell {
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
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "spline_following");
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.spline = CatmullRomCurveSpline.readNbt(nbt);
	}

	public SplineFollowingAxisLimitedWorldshell constructSpline(Vec3d... points) {
		this.spline = new CatmullRomCurveSpline(points);
		return this;
	}

	@Override
	public void tick() {
		super.tick();
		this.spline.moveLoop(0.25f);
		this.prevPos = this.pos;
		this.pos = this.spline.getPos(1);
		Vec3d prod = this.spline.getVelocity(1).normalize();
		//float angleChange = (float) (MathHelper.atan2(prod.x, prod.z) - MathHelper.atan2(this.axis.x, this.axis.z)) * 20;
		if(rotation == null) {
			rotation = new Quaternionf();
		}
		this.prevRotation = rotation;
		this.rotation = new Quaternionf().rotateTo(new Vector3f(1, 0,0), axis);
		this.axis = prod.toVector3f();
	}
}
