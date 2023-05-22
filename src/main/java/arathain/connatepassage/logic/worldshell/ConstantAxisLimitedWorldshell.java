package arathain.connatepassage.logic.worldshell;

import arathain.connatepassage.ConnatePassage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Map;

public class ConstantAxisLimitedWorldshell extends AxisLimitedWorldshell {
	public ConstantAxisLimitedWorldshell(Map<BlockPos, BlockState> contained, Vec3d initialPos, BlockPos pivot, Vector3f initialAxis) {
		super(contained, initialPos, pivot, initialAxis);
	}
	@Override
	public void readUpdateNbt(NbtCompound nbt) {
		super.readUpdateNbt(nbt);
		this.putAxis(new Vector3f(nbt.getFloat("axisX"), nbt.getFloat("axisY"), nbt.getFloat("axisZ")));
	}

	@Override
	public void tick() {
		//TODO temp
		super.tick();
		this.prevRotation = this.getRotation();
		this.rotation.rotateAxis(0.05f, this.axis);
	}

	@Override
	public Identifier getId() {
		return new Identifier(ConnatePassage.MODID, "axis_limited");
	}

	@Override
	public NbtCompound writeUpdateNbt(NbtCompound nbt) {
		nbt.putFloat("axisX", axis.x);
		nbt.putFloat("axisY", axis.y);
		nbt.putFloat("axisZ", axis.z);
		return super.writeUpdateNbt(nbt);
	}
}
