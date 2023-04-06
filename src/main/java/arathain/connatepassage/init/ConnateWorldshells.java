package arathain.connatepassage.init;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.logic.worldshell.AxisLimitedWorldshell;
import arathain.connatepassage.logic.worldshell.FreeWorldshell;
import arathain.connatepassage.logic.worldshell.WorldshellSupplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ConnateWorldshells {
	Map<Identifier, WorldshellSupplier> WORLDSHELLS = new LinkedHashMap<>();

	WorldshellSupplier AXIS_LIMITED = register("axis_limited", (map, pos, pivot) -> new AxisLimitedWorldshell(map, pos, pivot, Direction.Axis.Z));
	WorldshellSupplier FREE = register("unbound", FreeWorldshell::new);

	static <T extends WorldshellSupplier> T register(String id, T shell) {
		WORLDSHELLS.put(new Identifier(ConnatePassage.MODID, id), shell);
		return shell;
	}
}
