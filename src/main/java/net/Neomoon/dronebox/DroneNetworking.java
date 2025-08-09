package net.Neomoon.dronebox;

import net.Neomoon.dronebox.network.MoveC2SPayload;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;


public final class DroneNetworking {
	private DroneNetworking() {}

	public static void register() {

		PayloadTypeRegistry.playC2S().register(MoveC2SPayload.ID, MoveC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleC2SPayload.ID, ToggleC2SPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(MoveC2SPayload.ID, (payload, context) -> {

			ServerPlayerEntity player = context.player();

			context.server().execute(() -> {
				handleMovePayload(player, payload);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ToggleC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> {
				handleTogglePayload(player, payload);
			});
		});
	}

	private static void handleMovePayload(ServerPlayerEntity player, MoveC2SPayload payload) {

		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof CentralDroneInit.Drone drone) {

			double yawRad = Math.toRadians(drone.getYaw());
			double forward = payload.forward();
			double strafe  = payload.strafe();
			double moveSpeed = 0.35;
			double vx = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * moveSpeed;
			double vz = ( Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * moveSpeed;
			double vy = (payload.up() ? moveSpeed : 0) + (payload.down() ? -moveSpeed : 0);
			drone.setManualVelocity(vx, vy, vz);


			double yawRate = payload.yawInput() * 3.5;
			drone.setRotationVelocity(yawRate, 0.0, 0.0);
		}
	}

	private static void handleTogglePayload(ServerPlayerEntity player, ToggleC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof CentralDroneInit.Drone drone) {

			drone.setCustomName(Text.literal("Controlled by " + player.getName()));

		}
	}

	private static Entity findEntityByUUID(ServerPlayerEntity player, java.util.UUID uuid) {
		return player.getWorld().getEntity(uuid);
	}
}
