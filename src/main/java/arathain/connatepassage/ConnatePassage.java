package arathain.connatepassage;

import arathain.connatepassage.init.ConnateBlocks;
import arathain.connatepassage.init.ConnateItems;
import arathain.connatepassage.init.ConnateWorldshells;
import arathain.connatepassage.logic.spline.CatmullRomSpline;
import arathain.connatepassage.logic.spline.CatmullRomSplineCurve;
import com.google.common.reflect.Reflection;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnatePassage implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("Connate Passage");
	public static String MODID = " ";
	public static float funStuff = 0;
	private static final CatmullRomSplineCurve c = new CatmullRomSplineCurve(new Vec3d(-10, -50, -10), new Vec3d(-5, -50, 15), new Vec3d(-10, -50, 30), new Vec3d(10, -50, 20), new Vec3d(15, -50, 0), new Vec3d(20, -60, -10), new Vec3d(30, -60, -10), new Vec3d(12.5, -55, 0), new Vec3d(-10, -50, 30), new Vec3d(-5, -50, 15), new Vec3d(-10, -50, -10));

	@Override
	public void onInitialize(ModContainer mod) {
		MODID = mod.metadata().id();
		ConnateBlocks.init();
		ConnateItems.init();
		if(QuiltLoader.isDevelopmentEnvironment()) {
			ClientTickEvents.START.register((client -> {
				if(client.world != null) {
					if(client.options.sprintKey.isPressed()) {
						funStuff += 0.03f;
					} else if(client.options.sneakKey.isPressed()) {
						funStuff -= 0.03f;
					}
					c.moveLoop(funStuff);
					Vec3d vec = c.getPos(1);
					client.world.addParticle(ParticleTypes.EXPLOSION, vec.x, vec.y, vec.z, 0, 0, 0);
				}
			}));
		}
		Reflection.initialize(ConnateWorldshells.class);
	}
	public static Identifier id(String name) {
		return new Identifier(MODID, name);
	}
	public static BlockBox makeBlockBoxIndiscriminate(BlockPos one, BlockPos two) {
		int minX, maxX, minY, maxY, minZ, maxZ;
		if(one.getX() > two.getX()) {
			minX = two.getX();
			maxX = one.getX();
		} else {
			maxX = two.getX();
			minX = one.getX();
		}
		if(one.getY() > two.getY()) {
			minY = two.getY();
			maxY = one.getY();
		} else {
			maxY = two.getY();
			minY = one.getY();
		}
		if(one.getZ() > two.getZ()) {
			minZ = two.getZ();
			maxZ = one.getZ();
		} else {
			maxZ = two.getZ();
			minZ = one.getZ();
		}
		return new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
