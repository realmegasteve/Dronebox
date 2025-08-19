package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.DroneboxMain;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public class ModItems {

	public static final Item DRONE = registerItem("drone",
		new DroneItem(new Item.Settings().maxCount(1)
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID, "drone")))));

	public static final Item DRONE_CONTROLLER = registerItem("controller",
		new DroneControllerItem(new Item.Settings().maxCount(1)
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID,"controller")))));

	public static final Item DRONE_REMOTE = registerItem("remote",
		new DroneRemoteItem(new Item.Settings().maxCount(1)
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID, "remote")))));

	public static final Item EYE_ACCESSORY = registerItem("googly",
		new Item(new Item.Settings().maxCount(1)
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID,"googly")))));

	public static final Item SPOTLIGHT_ACCESSORY = registerItem("spotlight",
		new Item(new Item.Settings().maxCount(1)
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID,"spotlight")))));

	public static final Item TOPLIGHT_ACCESSORY = registerItem("toplight",
		new Item(new Item.Settings().maxCount(1)
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(DroneboxMain.MOD_ID,"toplight")))));


	public static final RegistryKey<ItemGroup> DRONEBOX_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP,
		Identifier.of(DroneboxMain.MOD_ID, "dronebox"));

	public static final ItemGroup DRONEBOX_GROUP = FabricItemGroup.builder()
		.icon(() -> new ItemStack(DRONE_CONTROLLER))
		.displayName(Text.translatable("itemGroup.dronebox.dronebox"))
		.entries((context, entries) -> {
			entries.add(DRONE);
			entries.add(DRONE_CONTROLLER);
			entries.add(DRONE_REMOTE);
			entries.add(EYE_ACCESSORY);
			entries.add(SPOTLIGHT_ACCESSORY);
			entries.add(TOPLIGHT_ACCESSORY);
		})
		.build();

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(DroneboxMain.MOD_ID, name), item);
	}

	public static void registerModItems() {
		DroneboxMain.LOGGER.info("Registering Mod Items for " + DroneboxMain.MOD_ID);

		Registry.register(Registries.ITEM_GROUP, DRONEBOX_GROUP_KEY, DRONEBOX_GROUP);
	}
}
