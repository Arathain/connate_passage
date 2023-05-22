package arathain.connatepassage.logic.spline;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.util.math.MathHelper.catmullRom;

public abstract class CatmullRomSpline {

	//for remembering the godsforsaken maths behind it all
	private static final Matrix4d IDENTITY_MATRIX = new Matrix4d( -0.5, 1.5, -1.5, 0.5,
			1, -2.5, 2, -0.5,
			-0.5, 0, 0.5, 0,
			0, 1, 0, 0);
	/**
	 * @author Alexey Karamyshev
	 * <a href="https://medium.com/@ommand/movement-along-the-curve-with-constant-speed-4fa383941507">...</a>
	 **/
	private static final Vec2f[] cubicQuadrature = new Vec2f[]{new Vec2f(-0.7745966F, 0.5555556F), new Vec2f(0, 0.8888889F), new Vec2f(0.7745966F, 0.5555556F)};
	/**
	 * @author Alexey Karamyshev
	 * <a href="https://medium.com/@ommand/movement-along-the-curve-with-constant-speed-4fa383941507">...</a>
	 **/
	public static float integrate(Function<Float, Float> f, float min, float max) {
		float sum = 0f;
		for(Vec2f vec : cubicQuadrature) {
			sum += vec.y * f.apply(MathHelper.lerp(MathHelper.inverseLerp(vec.x, -1, 1), min, max));
		}
		return sum * (max - min) / 2;
	}
	public static Vec3d interpolate(float delta, Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3) {
		return p1
			.add(p0.multiply(-0.5).add(p2.multiply(0.5)).multiply(delta))
			.add(p0.add(p1.multiply(-2.5)).add(p2.multiply(2)).add(p3.multiply(-0.5)).multiply(delta*delta))
			.add(p0.multiply(-0.5).add(p1.multiply(1.5)).add(p2.multiply(-1.5)).add(p3.multiply(0.5)).multiply(delta*delta*delta));
	}

	public static Vec3d interpolateDerivative(float delta, Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3) {
		return p0.multiply(-0.5).add(p2.multiply(0.5))
				.add(p0.add(p1.multiply(-2.5)).add(p2.multiply(2)).add(p3.multiply(-0.5)).multiply(2*delta))
				.add(p0.multiply(-0.5).add(p1.multiply(1.5)).add(p2.multiply(-1.5)).add(p3.multiply(0.5)).multiply(3*delta*delta));
	}

}
