package net.Neomoon.dronebox.mixins.client;

import net.Neomoon.dronebox.CameraManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
	private void disableFirstPersonItem(CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (CameraManager.DroneCamera) {
			ci.cancel();
		}
	}
}
