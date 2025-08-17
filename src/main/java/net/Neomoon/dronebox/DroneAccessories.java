package net.Neomoon.dronebox;

import net.Neomoon.dronebox.Drone;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class DroneAccessories {

	private static final Map<Item, BiConsumer<Drone, ItemStack>> TICK_HANDLERS = new HashMap<>();

	public static void registerAccessory(Item item, BiConsumer<Drone, ItemStack> tickHandler) {
		TICK_HANDLERS.put(item, tickHandler);
	}

	public static void tickAccessory(Drone drone, ItemStack stack) {
		BiConsumer<Drone, ItemStack> handler = TICK_HANDLERS.get(stack.getItem());
		if (handler != null) {
			handler.accept(drone, stack);
		}
	}
}
