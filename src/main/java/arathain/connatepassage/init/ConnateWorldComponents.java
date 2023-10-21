package arathain.connatepassage.init;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.content.cca.WorldshellComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;

/**
 * API-specific component registration class
 **/
public class ConnateWorldComponents implements WorldComponentInitializer {
	public static final ComponentKey<WorldshellComponent> WORLDSHELLS = ComponentRegistry.getOrCreate(ConnatePassage.id("worldshells"), WorldshellComponent.class);
	@Override
	public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
		registry.register(WORLDSHELLS, WorldshellComponent::new);
	}
}
