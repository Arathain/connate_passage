package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.logic.spline.CatmullRomCurveSpline;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Map;

public class ConstantAxisLimitedWorldshell extends AxisLimitedWorldshell implements ScrollableWorldshell {
	private float speed = 0.1f;
	public ConstantAxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot, initialAxis);
	}
	@Override
	public void readUpdateNbt(NbtCompound nbt) {
		super.readUpdateNbt(nbt);
		this.putAxis(new Vector3f(nbt.getFloat("axisX"), nbt.getFloat("axisY"), nbt.getFloat("axisZ")));
		this.speed = nbt.getFloat("speed");
	}
	@Override
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		nbt.putFloat("speed", speed);
		nbt.putFloat("axisX", axis.x);
		nbt.putFloat("axisY", axis.y);
		nbt.putFloat("axisZ", axis.z);
		return super.writeUpdateNbt(nbt);
	}

	public void setSpeed(float spd) {
		speed = MathHelper.clamp(spd, -1, 1);
	}

	public float getSpeed() {
		return speed;
	}

	@Override
	public void tick() {
		super.tick();

		this.prevRotation = this.getRotation();
		this.rotation.rotateAxis(speed, this.axis);

		if(this.speed == 0) {
			this.rotation = this.prevRotation;
		}

	}

	@Override
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "axis_limited");
	}
}
