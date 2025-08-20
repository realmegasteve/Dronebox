package net.Neomoon.dronebox.mixins;


import net.Neomoon.dronebox.FlareRenderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void onShouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (entity == null) return;

		if (!FlareRenderManager.isEnabled(entity.getUuid())) {
			return;
		}

		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return;

		final double MAX_DISTANCE = 9999.0D;
		final double maxSq = MAX_DISTANCE * MAX_DISTANCE;

		double dx = mc.player.getX() - entity.getX();
		double dy = mc.player.getY() - entity.getY();
		double dz = mc.player.getZ() - entity.getZ();
		double distSq = dx*dx + dy*dy + dz*dz;

		if (distSq <= maxSq) {
			cir.setReturnValue(true);
		}
	}
}
