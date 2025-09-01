package net.Neomoon.dronebox.client;

import net.Neomoon.dronebox.network.DroneBatchC2SPayload;
import net.Neomoon.dronebox.network.DroneStateC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class DroneStatePayloadBatchesDispatcher {
	private static final List<DroneStateC2SPayload> pendingPayloads = new ArrayList<>();

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(DroneStatePayloadBatchesDispatcher::onEndTick);
	}

	public static void queuePayload(DroneStateC2SPayload payload) {
		synchronized (pendingPayloads) {
			pendingPayloads.add(payload);
		}
	}

	public static void flush() {
		List<DroneStateC2SPayload> toSend;
		synchronized (pendingPayloads) {
			if (pendingPayloads.isEmpty()) return;

			toSend = new ArrayList<>(pendingPayloads);
			pendingPayloads.clear();
		}

		// Send the batch
		if (ClientPlayNetworking.canSend(DroneBatchC2SPayload.ID)) {
			DroneBatchC2SPayload batchPayload = new DroneBatchC2SPayload(toSend);
			ClientPlayNetworking.send(batchPayload);
		}
	}

	private static void onEndTick(MinecraftClient client) {
			flush();
	}
}
