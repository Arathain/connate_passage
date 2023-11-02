package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.MikuMachines;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * A blank implementation of {@link Worldshell} - unused by default.
 **/

public class FreeWorldshell extends Worldshell {
	public FreeWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot) {
		super(contained, initialPos, pivot);
	}

	@Override
	public Identifier getId() {
		return MikuMachines.id("unbound");
	}
}
