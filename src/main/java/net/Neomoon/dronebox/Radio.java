package net.Neomoon.dronebox;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.HashMap;
import java.util.Map;

public class Radio {
	public static Map<String, Double> Channel1 = new HashMap<>();
	public static Map<String, Double> BufferChannel1 = new HashMap<>();
	public static void register() {
		ClientTickEvents.START_WORLD_TICK.register(server -> {
			Channel1 = BufferChannel1;
			BufferChannel1.clear();
		});
	}

	public static void sendSignal(String channel, double value){
		BufferChannel1.put(channel, value);
	}

	public static double readOrDefault(String channel, double value){
		return Channel1.getOrDefault(channel, value);
	}

	public static double read(String channel){
		return Channel1.get(channel);
	}

	static public boolean signalExist(String channel){
		return Channel1.containsKey(channel);
	}
}
