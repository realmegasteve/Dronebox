package net.Neomoon.dronebox.mixins;

import net.Neomoon.dronebox.CameraManager;
import net.Neomoon.dronebox.DroneCameraManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void tickCamera(CallbackInfo ci) {
		MinecraftClient client = (MinecraftClient) (Object) this;


		CameraManager.updateCameraTarget(client);

		if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			CameraManager.resetCamera();
			if (client.player != null) client.cameraEntity = client.player;
		}
	}
}
