package arathain.connatepassage.logic.spline;

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

	public CatmullRomSplineCurve(int lookupTablePrecision, Vec3d... points) {
		List<Vec3d> l = Arrays.stream(points).toList();
		this.points = new ArrayList<>();
		this.points.add(l.get(0).add(l.get(0).subtract(l.get(1))));
		this.points.addAll(l);
		int s = l.size()-1;
		this.points.add(l.get(s).add(l.get(s).subtract(l.get(s-1))));

		regenerateLookupTable(lookupTablePrecision);
	}
	public void regenerateLookupTable(int lookupTablePrecision) {
		this.lookupTable = CatmullRomSpline.generateLUT(this, lookupTablePrecision);

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
