package net.Neomoon.dronebox;

import net.Neomoon.dronebox.gui.DroneHUD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DroneCameraManager {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Map<Integer, Integer> slotToEntityId = new HashMap<>();
	private static final int CAM_WIDTH  = 128;
	private static final int CAM_HEIGHT = 128;

	private DroneCameraManager() {}

	public static void init() {}

	public static void startCamera(int slot, Entity drone) {
		if (drone == null) return;
		slotToEntityId.put(slot, drone.getId());
	}

	public static void stopCamera(int slot) {
		slotToEntityId.remove(slot);
		DroneHUD.updateDroneImage(slot, null);
	}

	public static void onClientTick() {
		if (client.world == null) return;

		Iterator<Map.Entry<Integer, Integer>> it = slotToEntityId.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> e = it.next();
			int slot = e.getKey();
			int entityId = e.getValue();

			Entity target = client.world.getEntityById(entityId);
			if (target == null || target.isRemoved()) {
				it.remove();
				DroneHUD.updateDroneImage(slot, null);
			}
		}
	}
}
