package net.Neomoon.dronebox.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.entity.EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void alwaysRenderPlayer(Entity entity, double x, double y, double z, float yaw, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (entity == client.player && client.getCameraEntity() != client.player) {
			entity.setInvisible(false);
		}
	}
}

