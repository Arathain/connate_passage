package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.logic.spline.CatmullRomCurveSpline;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
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
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.spline = CatmullRomCurveSpline.readNbt(nbt);
	}

	public void constructSpline(Vec3d... points) {
		this.spline = new CatmullRomCurveSpline(points);
	}

	@Override
	public void tick() {
		super.tick();
		this.prevPos = this.pos;
		this.pos = this.spline.getPos(1);
		if(this.prevPos != null) {
			this.axis = this.spline.getVelocity(1).normalize().toVector3f();
		}
		this.spline.moveLoop(0.05f);
	}
}
