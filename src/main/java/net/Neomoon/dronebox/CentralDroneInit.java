package net.Neomoon.dronebox;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class CentralDroneInit implements ModInitializer {
	public static EntityType<Drone> DRONE_ENTITY_TYPE;
	public static final RegistryKey<EntityType<?>> DRONE_KEY =
		RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(DroneboxMain.MOD_ID, "drone"));


	@Override
	public void onInitialize() {

		DRONE_ENTITY_TYPE = EntityType.Builder.create(Drone::new, SpawnGroup.MISC)
			.dimensions(0.75f, 0.75f)
			.maxTrackingRange(5)
			.trackingTickInterval(3)
			.build(DRONE_KEY);
		Registry.register(Registries.ENTITY_TYPE, DRONE_KEY.getValue(), DRONE_ENTITY_TYPE);
		FabricDefaultAttributeRegistry.register(DRONE_ENTITY_TYPE, Drone.createDroneAttributes());
		DroneNetworking.register();
	}

}
