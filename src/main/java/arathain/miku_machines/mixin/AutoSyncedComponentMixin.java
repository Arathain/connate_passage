package arathain.miku_machines.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(AutoSyncedComponent.class)
public interface AutoSyncedComponentMixin {

	@WrapOperation(method = "applySyncPacket(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt()Lnet/minecraft/nbt/NbtCompound;") )
	private NbtCompound connate$readNbt(PacketByteBuf instance, Operation<NbtCompound> og) {
		return instance.readUnlimitedNbt();
	}
}
