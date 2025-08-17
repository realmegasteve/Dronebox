package net.Neomoon.dronebox;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.UUID;

public final class DroneNetworking {
	private DroneNetworking() {}

	public static void register() {

		PayloadTypeRegistry.playC2S().register(MoveC2SPayload.ID, MoveC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleC2SPayload.ID, ToggleC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestCameraPayload.ID, RequestCameraPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CameraFramePayload.ID, CameraFramePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ViewTogglePayload.ID, ViewTogglePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ViewUpdatePayload.ID, ViewUpdatePayload.CODEC);


		ServerPlayNetworking.registerGlobalReceiver(MoveC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleMovePayload(player, payload));
		});

		ServerPlayNetworking.registerGlobalReceiver(ToggleC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleTogglePayload(player, payload));
		});

		ServerPlayNetworking.registerGlobalReceiver(RequestCameraPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleCameraPayload(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(ViewTogglePayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleViewtogglePayload(player, payload));
		});
		ClientPlayNetworking.registerGlobalReceiver(ViewUpdatePayload.ID, (payload, context) -> {
			ClientPlayerEntity player = context.player();
			context.client().execute(() -> handleViewupdatePayload(player, payload));
		});
	}

	private static void handleMovePayload(ServerPlayerEntity player, MoveC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof Drone drone) {
			double yawRad = Math.toRadians(drone.getYaw());
			double forward = payload.forward();
			double strafe = payload.strafe();
			double moveSpeed = 0.35;
			double vx = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * moveSpeed;
			double vz = (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * moveSpeed;
			double vy = (payload.up() ? moveSpeed : 0) + (payload.down() ? -moveSpeed : 0);
			drone.setManualVelocity(vx, vy, vz);

			double yawRate = payload.yawInput() * 3.5;
			drone.setRotationVelocity(yawRate, 0.0, 0.0);
		}
	}

	private static void handleTogglePayload(ServerPlayerEntity player, ToggleC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof Drone drone) {
			DroneControllerItem.setDroneEnabled(player.getStackInHand(Hand.MAIN_HAND), payload.droneUuid().toString(), !DroneControllerItem.isDroneEnabled(player.getStackInHand(Hand.MAIN_HAND), payload.droneUuid().toString()));
		}
	}

	private static void handleViewtogglePayload(ServerPlayerEntity player, ViewTogglePayload payload) {

		ServerPlayNetworking.send(player, new ViewUpdatePayload(payload.target()));

	}
	private static void handleViewupdatePayload(ClientPlayerEntity player, ViewUpdatePayload payload) {

		CameraManager.update(UUID.fromString(payload.target()), !CameraManager.DroneCamera);

	}

	private static void handleCameraPayload(ServerPlayerEntity player, RequestCameraPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof Drone drone) {

			byte[] imageData = drone.renderCameraToBytes();


			CameraFramePayload frame = new CameraFramePayload(drone.getUuidAsString(), imageData);
			ServerPlayNetworking.send(player, frame);
		}
	}


	private static Entity findEntityByUUID(ServerPlayerEntity player, UUID uuid) {
		return player.getWorld().getEntity(uuid);
	}
}
