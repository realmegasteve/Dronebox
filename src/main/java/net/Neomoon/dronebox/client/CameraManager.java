package net.Neomoon.dronebox.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class CameraManager {
	public static boolean DroneCamera = false;
	public static UUID targetEntityUUID = null;

	public static void update(UUID uuid, boolean newstate){
		DroneCamera = newstate;
		targetEntityUUID = uuid;
	}

	public static void resetCamera() {
		DroneCamera = false;
		targetEntityUUID = null;
	}

	public static Entity getTargetEntity(MinecraftClient client) {
		if (targetEntityUUID == null) return null;
		ClientWorld world = client.world;
		if (world == null) return null;
		return world.getEntity(targetEntityUUID);
	}

	public static void updateCameraTarget(MinecraftClient client) {
		if (DroneCamera) {
			Entity target = getTargetEntity(client);
			if (target != null) {
				client.cameraEntity = target;
			}
		}
	}
}
