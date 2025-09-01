package net.Neomoon.dronebox;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.UUID;

public final class DroneNetworking {
	private DroneNetworking() {}

	@SuppressWarnings("resource")
	public static void register() {

		PayloadTypeRegistry.playC2S().register(MoveC2SPayload.ID, MoveC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleC2SPayload.ID, ToggleC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestCameraC2SPayload.ID, RequestCameraC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ViewToggleC2SPayload.ID, ViewToggleC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DroneStateC2SPayload.ID, DroneStateC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DroneBatchC2SPayload.ID, DroneBatchC2SPayload.CODEC);

		//PayloadTypeRegistry.playS2C().register(CameraFrameS2CPayload.ID, CameraFrameS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ViewUpdateS2CPayload.ID, ViewUpdateS2CPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(DroneBatchC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			MinecraftServer server = context.server();
			for (DroneStateC2SPayload droneState : payload.droneStates()) {
				server.execute(() -> handleDroneStatePayload(player, droneState));
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(MoveC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleMovePayload(player, payload));
		});

		ServerPlayNetworking.registerGlobalReceiver(ToggleC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleTogglePayload(player, payload));
		});

		ServerPlayNetworking.registerGlobalReceiver(RequestCameraC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleCameraPayload(player, payload));
		});

		ServerPlayNetworking.registerGlobalReceiver(ViewToggleC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> handleViewtogglePayload(player, payload));
		});
	}

	private static void handleMovePayload(ServerPlayerEntity player, MoveC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof Drone drone) {
			double forward = payload.forward();
			double strafe = payload.strafe();
			double up = (payload.up()? 1 : 0) + (payload.down()? -1 : 0);
			drone.controllerMovementInput(forward, strafe, up, payload.yawInput());
		}
	}

	private static void handleDroneStatePayload(ServerPlayerEntity player, DroneStateC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.drone()));
		if (e instanceof Drone drone) {
			drone.payload = payload;
		}
	}

	private static void handleTogglePayload(ServerPlayerEntity player, ToggleC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof Drone) {
			DroneControllerItem.setDroneEnabled(player.getStackInHand(Hand.MAIN_HAND), payload.droneUuid(), !DroneControllerItem.isDroneEnabled(player.getStackInHand(Hand.MAIN_HAND), payload.droneUuid()));
		}
	}

	private static void handleViewtogglePayload(ServerPlayerEntity player, ViewToggleC2SPayload payload) {
		ServerPlayNetworking.send(player, new ViewUpdateS2CPayload(payload.target()));
	}

	private static void handleCameraPayload(ServerPlayerEntity player, RequestCameraC2SPayload payload) {
		Entity e = findEntityByUUID(player, UUID.fromString(payload.droneUuid()));
		if (e instanceof Drone drone) {

			byte[] imageData = drone.renderCameraToBytes();

			CameraFrameS2CPayload frame = new CameraFrameS2CPayload(drone.getUuidAsString(), imageData);
			ServerPlayNetworking.send(player, frame);
		}
	}


	private static Entity findEntityByUUID(ServerPlayerEntity player, UUID uuid) {
		return player.getWorld().getEntity(uuid);
	}
}
