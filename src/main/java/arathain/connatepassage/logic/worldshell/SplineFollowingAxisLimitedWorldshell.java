package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.logic.spline.CatmullRomCurveSpline;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Map;

public class SplineFollowingAxisLimitedWorldshell extends AxisLimitedWorldshell {
	private static CatmullRomCurveSpline spline;
	public SplineFollowingAxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot, initialAxis);
	}

	public void constructSpline(Vec3d... points) {
		spline = new CatmullRomCurveSpline(points);
	}

	@Override
	public void tick() {
		super.tick();
		this.pos = spline.getPos(1);
		if(this.prevPos != null) {
			this.axis =	this.pos.subtract(prevPos).normalize().toVector3f();
		}
	}
}
