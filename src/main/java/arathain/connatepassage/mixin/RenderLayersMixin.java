package arathain.connatepassage.mixin;

import arathain.connatepassage.ConnatePassage;
import arathain.connatepassage.init.ConnateItems;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class RenderLayersMixin {
	private static final Identifier tex = ConnatePassage.id("textures/item/bracer.png");
	@Inject(method = "getItemLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"), cancellable = true)
	private static void connate$fullBracers(ItemStack stack, boolean direct, CallbackInfoReturnable<RenderLayer> cir) {
		if(stack.isOf(ConnateItems.CONNATE_BRACER)) {
			cir.setReturnValue(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
		}
	}
}
