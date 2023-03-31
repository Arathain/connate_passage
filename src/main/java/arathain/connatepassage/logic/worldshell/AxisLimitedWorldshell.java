package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Map;

public class AxisLimitedWorldshell extends Worldshell {
	private Vector3f axis;
	public AxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot);
		this.axis = initialAxis;
	}

	public Vector3f getAxis() {
		return axis;
	}

	@Override
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "axis_limited");
	}
}
