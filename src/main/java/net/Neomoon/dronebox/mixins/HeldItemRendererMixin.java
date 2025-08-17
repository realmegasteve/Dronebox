package net.Neomoon.dronebox.mixins;

import net.Neomoon.dronebox.CameraManager;
import net.Neomoon.dronebox.DroneCameraManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.item.HeldItemRenderer.class)
public class HeldItemRendererMixin {
	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
	private void disableFirstPersonItem(CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (CameraManager.DroneCamera) {
			ci.cancel();
		}
	}
}
