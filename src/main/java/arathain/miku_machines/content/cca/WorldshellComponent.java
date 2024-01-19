package arathain.miku_machines.content.cca;

import arathain.miku_machines.init.ConnateWorldshells;
import arathain.miku_machines.logic.worldshell.Worldshell;
import arathain.miku_machines.logic.worldshell.WorldshellUpdatePacket;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an attached world component used for storing, updating, and synchronising worldshells.
 * @author Arathain
 * **/
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
			shell.setWorld(obj);
			shell.readNbt(worldshellNbt);
			worldshells.add(shell);
		});
	}

	public List<Worldshell> getWorldshells() {
		return worldshells;
	}

	/**
	 * Places a worldshell in the world, and updates the worldshell's stored parent world to match.
	 **/
	public void addWorldshell(Worldshell shell) {
		shell.setWorld(obj);
		worldshells.add(shell);
		shell.tick();
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

	/**
	 * Updates the worldshell at a rate of 20 Hz on the client side, applying worldshell logic.
	 * @see arathain.miku_machines.logic.worldshell.Worldshell
	 * **/
	@Override
	public void clientTick() {
		worldshells.forEach(Worldshell::tick);
	}

	/**
	 * Updates the worldshell at a rate of 20 Hz on the server side, applying worldshell logic and synchronising the worldshell data every other frame.
	 * @see arathain.miku_machines.logic.worldshell.Worldshell
	 * **/
	@Override
	public void serverTick() {
		worldshells.forEach(Worldshell::outerTick);
		if(obj instanceof ServerWorld s && s.getTime() % 2 == 0) {
			for (int i = 0; i < worldshells.size(); i++) {
				WorldshellUpdatePacket.send(s.getPlayers(), worldshells, i);
			}
		}
	}

	/**
	 * This method 'snaps' a worldshell's components to the world it's in, and returns a boolean value indicating whether the worldshell should be removed
	 * @author Arathain
	 * **/
	public boolean snapWorldshell(Worldshell w) {
		BlockPos origin = new BlockPos(MathHelper.floor(w.getPos().x), MathHelper.floor(w.getPos().y), MathHelper.floor(w.getPos().z));
		if(obj instanceof ServerWorld sWorld) {
			LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(sWorld).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(origin))
				.add(LootContextParameters.TOOL, new ItemStack(Items.NETHERITE_PICKAXE));
			w.getContained().forEach((b, s) -> {
				BlockPos pos = origin.add(b.subtract(w.getPivot()));
				if (obj.getBlockState(pos).isAir()) {
					obj.setBlockState(pos, s);
				} else {
					ItemScatterer.spawn(obj, pos, DefaultedList.copyOf(ItemStack.EMPTY, s.getDroppedStacks(builder).toArray(ItemStack[]::new)));
				}
			});
			return true;
		}
		return false;
	}
}
