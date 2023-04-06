package arathain.connatepassage.content.block;

import arathain.connatepassage.init.ConnateWorldshells;
import arathain.connatepassage.logic.worldshell.Worldshell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public abstract class WorldshellCreationBlock extends Block {
	public WorldshellCreationBlock(Settings settings) {
		super(settings);
	}
	public abstract Worldshell createWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot);
}
