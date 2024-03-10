package arathain.miku_machines.logic.worldshell;

import arathain.miku_machines.MikuMachines;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Map;
/**
 * An implementation of a {@link Worldshell} that rotates the worldshell along a {@link Vector3f} axis at a set speed, without movement.
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
		this.putAxis(new Vector3d(nbt.getDouble("axisX"), nbt.getDouble("axisY"), nbt.getDouble("axisZ")));
		this.speed = nbt.getInt("speed");
	}

	@Override
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		nbt.putInt("speed", speed);
		nbt.putDouble("axisX", axis.x);
		nbt.putDouble("axisY", axis.y);
		nbt.putDouble("axisZ", axis.z);
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

		if(this.shutdownTickCountdown > 0 || this.shutdownTickCountdown == -666) {
			if(this.shutdownTickCountdown != -666)
				this.shutdownTickCountdown--;
			this.pose.getOrientation().rotateAxis(getSpeedHz() * (this.invertedMotion ? -1 : 1) * MathHelper.PI / 20f, this.axis).normalize();
		}

		if(this.speed == 0) {
			this.pose = this.prevPose;
		}
	}

	@Override
	public Identifier getId() {
		return new Identifier(MikuMachines.MODID, "axis_limited");
	}
}
