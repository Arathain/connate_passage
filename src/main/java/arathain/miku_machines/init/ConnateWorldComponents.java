package arathain.miku_machines.init;

import arathain.miku_machines.MikuMachines;
import arathain.miku_machines.content.cca.WorldshellComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;

/**
 * API-specific component registration class
 **/
public class ConnateWorldComponents implements WorldComponentInitializer {
	public static final ComponentKey<WorldshellComponent> WORLDSHELLS = ComponentRegistry.getOrCreate(MikuMachines.id("worldshells"), WorldshellComponent.class);
	@Override
	public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
		registry.register(WORLDSHELLS, WorldshellComponent::new);
	}
}
