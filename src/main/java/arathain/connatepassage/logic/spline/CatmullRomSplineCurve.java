package arathain.connatepassage.logic.spline;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CatmullRomSplineCurve {
	private final List<Vec3d> points;
	private List<float[]> lookupTable;
	private final List<Float> distances = new ArrayList<>();
	public float pos = 0;
	public float prevPos = 0;
	private final int lookupTablePrecision;

	public CatmullRomSplineCurve(int lookupTablePrecision, Vec3d... points) {
		List<Vec3d> l = Arrays.stream(points).toList();
		this.points = new ArrayList<>();
		this.points.add(l.get(0).add(l.get(0).subtract(l.get(1))));
		this.points.addAll(l);
		int s = l.size()-1;
		this.points.add(l.get(s).add(l.get(s).subtract(l.get(s-1))));
		this.lookupTablePrecision = lookupTablePrecision;

		regenerateLookupTable(lookupTablePrecision);
	}
	public void move(float dist) {
		this.prevPos = this.pos;
		this.pos += dist;
	}
	public void moveLoop(float dist) {
		move(dist);
		if(this.pos > distances.get(distances.size()-1)) {
			this.prevPos = 0;
			this.pos = 0;
		}
	}
	public Vec3d getPos(float tickDelta) {
		float distance = MathHelper.lerp(tickDelta, prevPos, pos);
		int index = 0;
		for(int target = 0; target < distances.size(); target++) {
			if(distances.get(target) > distance && distances.get(index) < distances.get(target)) {
				index = target;
			}
		}
		float[] distances = this.lookupTable.get(index);
		int innerIndex = distances.length-1;
		for(int target = 0; target < distances.length-1; target++) {
			if(distances[target] < distance && distances[innerIndex] > distances[target]) {
				innerIndex = target;
			}
		}
		float delta = distances[innerIndex+1]-distances[innerIndex];
		delta = MathHelper.lerp((distance-distances[innerIndex])/delta, innerIndex/lookupTablePrecision, (innerIndex+1)/lookupTablePrecision);
		return CatmullRomSpline.interpolate(delta, this.points.get(index), this.points.get(index+1), this.points.get(index+2), this.points.get(index+3));
	}
	public void regenerateLookupTable(int lookupTablePrecision) {
		this.lookupTable = CatmullRomSpline.generateLUT(this, lookupTablePrecision+1);

		constructDistances();
	}
	private void constructDistances() {
		float buffer = 0;
		this.distances.clear();
		for(float[] array : lookupTable) {
			buffer += array[array.length-1];
			distances.add(buffer);
		}
	}

	public List<Vec3d> getPoints() {
		return points;
	}
	public List<float[]> getLUT() {
		return lookupTable;
	}
}
