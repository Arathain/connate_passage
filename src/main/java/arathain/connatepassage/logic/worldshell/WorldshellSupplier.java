package arathain.connatepassage.logic.worldshell;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * A worldshell supplier that takes in the basic immutable components and returns a specified type of worldshell.
 **/
@FunctionalInterface
public interface WorldshellSupplier<T extends Worldshell> {
	T create(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot);
}
