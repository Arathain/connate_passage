package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class AxisLimitedWorldshell extends Worldshell {
	private Axis axis;
	public AxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Axis initialAxis) {
		super(contained, initialPos, pivot);
		this.axis = initialAxis;
	}
	public AxisLimitedWorldshell setAxis(Axis axis) {
		this.axis = axis;
		return this;
	}

	public Axis getAxis() {
		return axis;
	}

	@Override
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "axis_limited");
	}
}
