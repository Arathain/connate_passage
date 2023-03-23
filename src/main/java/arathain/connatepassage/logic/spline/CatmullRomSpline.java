package arathain.connatepassage.logic.spline;

import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4d;
import org.joml.Matrix4x3d;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;

public class CatmullRomSpline {
	public static Vec3d interpolate(float delta, Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3) {
		Vector4d cubic = new Vector4d(delta*delta*delta, delta*delta, delta, 1);
		Matrix4d transform = new Matrix4d( 0, 2, 0, 0,
				                          -1, 0, 1, 0,
				                           2,-5, 4, -1,
				                          -1, 3, -3, 1);
		Matrix4x3d vectors = new Matrix4x3d(p0.x, p0.y, p0.z,
				                            p1.x, p1.y, p1.z,
				                            p2.x, p2.y, p2.z,
				                            p3.x, p3.y, p3.z);
		cubic.mul(0.5);
		cubic.mul(transform);
		cubic.mul(vectors);
		return new Vec3d(cubic.x, cubic.y, cubic.z);
	}
	public static List<float[]> generateLUT(CatmullRomSplineCurve curve, int steps) {
		List<float[]> LUT = new ArrayList<>();
		List<Vec3d> vecs = curve.getPoints();
		int segments = vecs.size()-3;
		for(int i = 0; i < segments; i++) {
			Vec3d p0 = vecs.get(i);
			Vec3d p1 = vecs.get(i+1);
			Vec3d p2 = vecs.get(i+2);
			Vec3d p3 = vecs.get(i+3);
			LUT.add(composeSegmentLookup(p0, p1, p2, p3, steps));
		}
		return LUT;
	}
	private static float[] composeSegmentLookup(Vec3d a, Vec3d b, Vec3d c, Vec3d d, int precision) {
		float[] yeag = new float[]{};
		float buffer = 0;
		int index = 0;
		Vec3d prev = null;
		Vec3d current;
		for(int i = 0; i <= precision; i++) {
			current = interpolate((float)i/precision, a, b, c, d);
			if(prev == null) {
				prev = current;
			} else {
				buffer += (float) prev.distanceTo(current);
				yeag[index++] = buffer;
			}
		}
		return yeag;
	}
}
