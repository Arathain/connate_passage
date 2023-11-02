package arathain.miku_machines.init;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.logic.worldshell.*;
import net.minecraft.block.FacingBlock;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Initialisation interface used for storing all worldshell types.
 **/
public interface ConnateWorldshells {
	Map<Identifier, WorldshellSupplier<?>> WORLDSHELLS = new LinkedHashMap<>();

	WorldshellSupplier<AxisLimitedWorldshell> AXIS_LIMITED = register("axis_limited", (map, pos, pivot) -> new ConstantAxisLimitedWorldshell(map, pos, pivot, new Vector3f(0, 0, 1)));
	WorldshellSupplier<FreeWorldshell> FREE = register("unbound", FreeWorldshell::new);
	WorldshellSupplier<SplineFollowingAxisLimitedWorldshell> SPLINE = register("spline_following", (map, pos, pivot) -> new SplineFollowingAxisLimitedWorldshell(map, pos, pivot, map.get(pivot).get(FacingBlock.FACING).getUnitVector()));


	static <T extends WorldshellSupplier<?>> T register(String id, T shell) {
		WORLDSHELLS.put(new Identifier(MikuMachines.MODID, id), shell);
		return shell;
	}
}
