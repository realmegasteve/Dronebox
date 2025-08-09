package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.DroneboxMain;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
	public static final Item DRONE_CONTROLLER = registerItem("drone_controller", new DroneControllerItem(new Item.Settings().maxCount(1).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID,"drone_controller")))));

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(DroneboxMain.MOD_ID, name), item);
	}


	public static void registerModItems() {
		DroneboxMain.LOGGER.info("Registering Mod Items for " + DroneboxMain.MOD_ID);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(DRONE_CONTROLLER);
		});
	}
}
