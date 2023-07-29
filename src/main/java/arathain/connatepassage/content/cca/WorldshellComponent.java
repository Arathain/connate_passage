package arathain.connatepassage.content.cca;

import arathain.connatepassage.init.ConnateWorldshells;
import arathain.connatepassage.logic.worldshell.Worldshell;
import arathain.connatepassage.logic.worldshell.WorldshellUpdatePacket;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WorldshellComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
	private final List<Worldshell> worldshells = new ArrayList<>();
	private final World obj;
	public WorldshellComponent(World world) {
		this.obj = world;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readFromNbt(NbtCompound nbt) {
		worldshells.clear();
		NbtList list = nbt.getList("worldshells", 10);
		list.forEach(wNbt -> {
			NbtCompound worldshellNbt = (NbtCompound) wNbt;
			Worldshell shell = ConnateWorldshells.WORLDSHELLS.get(new Identifier(worldshellNbt.getString("id"))).create(Worldshell.getBlocksFromNbt(worldshellNbt), Vec3d.ZERO, Worldshell.getBlockPosFromNbt(worldshellNbt));
			shell.readNbt(worldshellNbt);
			worldshells.add(shell);
		});
	}

	public List<Worldshell> getWorldshells() {
		return worldshells;
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtList list = new NbtList();
		worldshells.forEach((shell) -> {
			NbtCompound compound = new NbtCompound();
			shell.writeNbt(compound);
			list.add(compound);
		});
		tag.put("worldshells", list);
	}

	@Override
	public void clientTick() {
		worldshells.forEach(Worldshell::tick);
	}

	@Override
	public void serverTick() {
		worldshells.forEach(Worldshell::tick);
		if(obj instanceof ServerWorld s /*&& s.getTime() % 20 == 0*/) {
			for (int i = 0; i < worldshells.size(); i++) {
				WorldshellUpdatePacket.send(s.getPlayers(), worldshells, i);
			}
		}
	}

	public boolean snapWorldshell(Worldshell w) {
		BlockPos origin = new BlockPos(MathHelper.floor(w.getPos().x), MathHelper.floor(w.getPos().y), MathHelper.floor(w.getPos().z));
		w.getContained().forEach((b, s) -> {
			obj.setBlockState(origin.add(b.subtract(w.getPivot())), s);
		});
		return true;
	}
}
