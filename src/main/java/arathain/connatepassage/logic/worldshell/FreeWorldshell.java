package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class FreeWorldshell extends Worldshell {
	public FreeWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot) {
		super(contained, initialPos, pivot);
	}

	@Override
	public Identifier getId() {
		return ConnatePassage.id("unbound");
	}
}
