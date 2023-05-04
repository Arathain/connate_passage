package arathain.connatepassage.init;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.logic.worldshell.*;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ConnateWorldshells {
	Map<Identifier, WorldshellSupplier> WORLDSHELLS = new LinkedHashMap<>();

	WorldshellSupplier<AxisLimitedWorldshell> AXIS_LIMITED = register("axis_limited", (map, pos, pivot) -> new ConstantAxisLimitedWorldshell(map, pos, pivot, new Vector3f(0, 0, 1)));
	WorldshellSupplier<FreeWorldshell> FREE = register("unbound", FreeWorldshell::new);

	static <T extends WorldshellSupplier> T register(String id, T shell) {
		WORLDSHELLS.put(new Identifier(ConnatePassage.MODID, id), shell);
		return shell;
	}
}
