package net.Neomoon.dronebox.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.Neomoon.dronebox.Drone;
import net.Neomoon.dronebox.client.DroneStatePayloadBatchesDispatcher;
import net.Neomoon.dronebox.network.DroneStateC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Environment(EnvType.CLIENT)
@Mixin(Drone.class)
public abstract class DroneMixin extends MobEntity {

	@Shadow
	public PlayerEntity pendriveOwner;

	@Shadow(remap = false)
	public boolean accessoryState;

	@Shadow(remap = false)
	protected abstract void runPython();

	@Shadow(remap = false)
	public abstract void controllerMovementInput(double forward, double strafe, double up, double yaw);

	@Shadow(remap = false)
	protected abstract void physics();

	protected DroneMixin(EntityType<? extends MobEntity> entityType, World world) {
		super(entityType, world);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;tick()V", ordinal = 0))
	private void checkOwnerAndDoClientThings(Drone instance, Operation<Void> original) {
		if (!Objects.equals(this.pendriveOwner, MinecraftClient.getInstance().player)) {
			return;
		}
		Vec3d velocity = this.getVelocity();
		original.call(instance);
		this.setVelocity(velocity);
		runPython();
		controllerMovementInput(0, 0, 0, 0);
		physics();
		DroneStateC2SPayload p = new DroneStateC2SPayload(uuid.toString(), getX(), getY(), getZ(), getVelocity().x, getVelocity().y, getVelocity().z, this.accessoryState);
		DroneStatePayloadBatchesDispatcher.queuePayload(p);
	}
}
