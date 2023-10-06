package arathain.connatepassage.logic.spline;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * This class acts as a representation of a Catmull-Rom curve spline, being able to return position and velocity {@link Vec3d} objects based off of the {@link CatmullRomCurveSpline#pos} and {@link CatmullRomCurveSpline#prevPos} float position values stored. Works by pre-calculating all curve lengths at initialisation, so as to calculate the correct target curve index to use for the actual interpolation.
 **/

public class CatmullRomCurveSpline {
	private final List<Vec3d> points;
	private final List<Float> distances = new ArrayList<>();
	public float pos = 0;
	public float prevPos = 0;

	public CatmullRomCurveSpline(Vec3d... points) {
		List<Vec3d> l = Arrays.stream(points).toList();
		this.points = new ArrayList<>();
		this.points.addAll(l);

		generateDistances();
	}
	public static CatmullRomCurveSpline fromExisting(Vec3d... points) {
		return new CatmullRomCurveSpline(points);
	}
	/**
	 * Creates a complete spline from a non-looping series of points, adding a point to each side in order to include the endpoints within the spline
	 **/
	public static CatmullRomCurveSpline fromRaw(Vec3d... points) {
		List<Vec3d> l = Arrays.stream(points).toList();
		List<Vec3d> pts = new ArrayList<>();
		pts.add(l.get(0).add(l.get(0).subtract(l.get(1))));
		pts.addAll(l);
		int s = l.size()-1;
		pts.add(l.get(s).add(l.get(s).subtract(l.get(s-1))));

		return new CatmullRomCurveSpline(pts.toArray(Vec3d[]::new));
	}
	public void move(float dist) {
		this.prevPos = this.pos;
		this.pos += dist;
	}
	public void writeNbt(NbtCompound nbt) {
		NbtList list = new NbtList();
		points.forEach(v -> {
			NbtCompound nbtC = new NbtCompound();
			nbtC.putDouble("posX", v.x);
			nbtC.putDouble("posY", v.y);
			nbtC.putDouble("posZ", v.z);
			list.add(nbtC);
		});
		nbt.put("points", list);
		nbt.putFloat("splinePos", pos);
		nbt.putFloat("prevSplinePos", prevPos);
	}
	public static CatmullRomCurveSpline readNbt(NbtCompound nbt) {
		List<Vec3d> points = new ArrayList<>();
		nbt.getList("points", 10).forEach(n -> {
					NbtCompound nbtC = (NbtCompound) n;
					points.add(new Vec3d(nbtC.getDouble("posX"), nbtC.getDouble("posY"), nbtC.getDouble("posZ")));
				}
		);
		CatmullRomCurveSpline s = fromExisting(points.toArray(new Vec3d[0]));
		s.pos = nbt.getFloat("splinePos");
		s.prevPos = nbt.getFloat("prevSplinePos");
		return s;
	}
	public void moveLoop(float dist) {
		move(dist);
		float h = distances.get(distances.size()-1);
		if(this.pos > h) {
			this.prevPos = 0;
			this.pos = 0;
		}
		if(this.pos < 0) {
			this.prevPos = h;
			this.pos = h;
		}
	}
	public void moveClamped(float dist) {
		move(dist);
		float h = distances.get(distances.size()-1);
		if(this.pos < 0) {
			this.prevPos = 0;
			this.pos = 0;
		}
		if(this.pos > h) {
			this.prevPos = h;
			this.pos = h;
		}
	}

	/**
	 * Returns a position vector of the current position of the spline based off of the curve delta value.
	 * @param index the curve index
	 * @param delta a delta value representing the position on the specified curve.
	 **/
	public Vec3d getPos(int index, float delta) {
		return CatmullRomSpline.interpolate(delta, this.points.get(index), this.points.get(index+1), this.points.get(index+2), this.points.get(index+3));
	}

	/**
	 * Returns a velocity vector based off of the provided curve index and curve delta value.
	 * @param index the curve index
	 * @param delta a delta value representing the position on the specified curve.
	 **/
	public Vec3d getVelocity(int index, float delta) {
		return CatmullRomSpline.interpolateDerivative(delta, this.points.get(index), this.points.get(index+1), this.points.get(index+2), this.points.get(index+3));
	}

	/**
	 * Returns a position vector of the current position of the spline, interpolating between {@link CatmullRomCurveSpline#pos} and {@link CatmullRomCurveSpline#prevPos} via the {@code tickDelta} method argument.
	 * @param tickDelta A delta value between 0 and 1, representing the progress between one Minecraft tick and another.
	 **/
	public Vec3d getPos(float tickDelta) {
		float distance = MathHelper.lerp(tickDelta, prevPos, pos);
		int index = 0;
		for(int target = 0; target < distances.size(); target++) {
			if(distances.get(target) < distance && distances.get(index) < distances.get(target)) {
				index = target;
			}
		}
		float delta = mapDistToDelta(distance-distances.get(index), index);
		return getPos(index, delta);
	}

	/**
	 * Returns a velocity vector based off of the delta value between the {@link CatmullRomCurveSpline#pos} & {@link CatmullRomCurveSpline#prevPos} values.
	 * @param tickDelta A delta value between 0 and 1, representing the progress between one Minecraft tick and another.
	 **/
	public Vec3d getVelocity(float tickDelta) {
		float distance = MathHelper.lerp(tickDelta, prevPos, pos);
		int index = 0;
		for(int target = 0; target < distances.size(); target++) {
			if(distances.get(target) < distance && distances.get(index) < distances.get(target)) {
				index = target;
			}
		}
		float delta = mapDistToDelta(distance-distances.get(index), index);
		return getVelocity(index, delta);
	}

	/**
	 * Uses Alexey Karamyshev's curve integration algorithm to calculate the curve length.
	 * @see CatmullRomSpline#integrate(Function, float, float)
	 **/
	private float arcLength(float t, int index) {
		return CatmullRomSpline.integrate((x) -> (float)getVelocity(index, x).length(), 0, t);
	}
	/**
	 * @author Alexey Karamyshev
	 * Maps a distance value to a corresponding 0-1 curve delta value. Used for interpolation.
	 * <a href="https://medium.com/@ommand/movement-along-the-curve-with-constant-speed-4fa383941507">...</a>
	 **/
	private float mapDistToDelta(float length, int index) {
		float t = 0 + length / arcLength(1, index);
		float min = 0;
		float max = 1;

		for (int i = 0; i < 16*16; ++i) {
			float f = arcLength(t, index) - length;

			if (MathHelper.abs(f) < 0.01f)
				break;

			float derivative = (float) getVelocity(index, t).length();
			float candidateT = t - f / derivative;

			if (f > 0) {
				max = t;
				if (candidateT <= 0)
					t = (max + min) / 2;
				else
					t = candidateT;
			} else {
				min = t;
				if (candidateT >= 1)
					t = (max + min) / 2;
				else
					t = candidateT;
			}
		}
		return t;
	}

	/**
	 * Generates the curve distance lengths for all provided spline positions. Should be called whenever the vector values of the spline are modified..
	 **/
	public void generateDistances() {
		float buffer = 0;
		this.distances.clear();
		this.distances.add(0f);
		List<Vec3d> vecs = this.getPoints();
		int segments = vecs.size()-3;
		for(int i = 0; i < segments; i++) {
			buffer += arcLength(1, i);
			distances.add(buffer);
		}
	}

	/**
	 * Checks whether the spline is able to loop.
	 **/
	public boolean lastPointsMatch() {
		return points.size() >= 4 && points.get(3).equals(points.get(points.size()-1)) && points.get(2).equals(points.get(points.size()-2)) && points.get(1).equals(points.get(points.size()-3));
	}
	public List<Vec3d> getPoints() {
		return points;
	}
}
