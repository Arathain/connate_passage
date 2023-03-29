package arathain.connatepassage.content.cca;

import arathain.connatepassage.init.ConnateWorldshells;
import arathain.connatepassage.logic.worldshell.Worldshell;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WorldshellComponent implements AutoSyncedComponent {
	private final List<Worldshell> worldshells = new ArrayList<>();
	private final World obj;
	public WorldshellComponent(World world) {
		this.obj = world;
	}
	@Override
	public void readFromNbt(NbtCompound nbt) {
		worldshells.clear();
		NbtList list = nbt.getList("worldshells", 10);
		list.forEach(wNbt -> {
			NbtCompound worldshellNbt = (NbtCompound) wNbt;
			Worldshell shell = ConnateWorldshells.WORLDSHELLS.get(new Identifier(worldshellNbt.getString("id"))).create(Worldshell.getBlocksFromNbt(worldshellNbt), Vec3d.ZERO);
			shell.readNbt(worldshellNbt);
			worldshells.add(shell);
		});
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
}
