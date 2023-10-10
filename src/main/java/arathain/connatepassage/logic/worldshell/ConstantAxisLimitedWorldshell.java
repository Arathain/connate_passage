package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.logic.spline.CatmullRomCurveSpline;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndRodBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Map;
/**
 * An implementation of a {@link Worldshell} that rotates the worldshell along a {@link Vector3f} axis at a set speed.
 **/
public class ConstantAxisLimitedWorldshell extends AxisLimitedWorldshell implements ScrollableWorldshell {
	//show on hover
	private int speed = 1;
	public ConstantAxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot, initialAxis);
	}
	@Override
	public void readUpdateNbt(NbtCompound nbt) {
		super.readUpdateNbt(nbt);
		this.putAxis(new Vector3f(nbt.getFloat("axisX"), nbt.getFloat("axisY"), nbt.getFloat("axisZ")));
		this.speed = nbt.getInt("speed");
	}

	@Override
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		nbt.putInt("speed", speed);
		nbt.putFloat("axisX", axis.x);
		nbt.putFloat("axisY", axis.y);
		nbt.putFloat("axisZ", axis.z);
		return super.writeUpdateNbt(nbt);
	}

	public void setSpeed(float spd) {
		speed = MathHelper.clamp((int)spd, -32, 32);
	}
	public void addSpeed(double speed) {
		if(speed > 0)
			speed = 1;
		else
			speed = -1;
		setSpeed(getSpeed()+(float)speed);
	}

	public float getSpeed() {
		return speed;
	}

	/**
	 * Gets the rotation speed value of the worldshell in Hertz (rotations per second)
	 **/
	public float getSpeedHz() {
		return speed/8f;
	}

	@Override
	public void tick() {
		super.tick();

		this.prevRotation = this.getRotation();
		if(this.shutdownTickCountdown > 0 || this.shutdownTickCountdown == -666) {
			if(this.shutdownTickCountdown != -666)
				this.shutdownTickCountdown--;
			this.rotation.rotateAxis(getSpeedHz() * (this.invertedMotion ? -1 : 1) * MathHelper.PI / 20f, this.axis);
		}

		if(this.speed == 0) {
			this.rotation = this.prevRotation;
		}

	}

	@Override
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "axis_limited");
	}
}
