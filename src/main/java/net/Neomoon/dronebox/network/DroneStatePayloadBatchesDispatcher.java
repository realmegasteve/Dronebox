package net.Neomoon.dronebox.network;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class DroneStatePayloadBatchesDispatcher {
	private static final List<DroneStatePayload> pendingPayloads = new ArrayList<>();

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(DroneStatePayloadBatchesDispatcher::onEndTick);
	}

	public static void queuePayload(DroneStatePayload payload) {
		synchronized (pendingPayloads) {
			pendingPayloads.add(payload);
		}
	}

	public static void flush() {
		List<DroneStatePayload> toSend;
		synchronized (pendingPayloads) {
			if (pendingPayloads.isEmpty()) return;

			toSend = new ArrayList<>(pendingPayloads);
			pendingPayloads.clear();
		}

		// Send the batch
		if (ClientPlayNetworking.canSend(DroneBatchPayload.ID)) {
			DroneBatchPayload batchPayload = new DroneBatchPayload(toSend);
			ClientPlayNetworking.send(batchPayload);
		}
	}

	private static void onEndTick(MinecraftClient client) {
			flush();
	}
}
