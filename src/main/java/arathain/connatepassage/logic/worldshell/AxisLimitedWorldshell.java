package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Map;

public abstract class AxisLimitedWorldshell extends Worldshell {
	protected Vector3f axis;
	public AxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot);
		this.axis = initialAxis;
	}

	public AxisLimitedWorldshell putAxis(Vector3f axis) {
		this.axis = axis;
		return this;
	}

	public Vector3f getAxis() {
		return axis;
	}
}
