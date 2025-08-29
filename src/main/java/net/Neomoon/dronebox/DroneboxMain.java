package net.Neomoon.dronebox;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.Neomoon.dronebox.items.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneboxMain implements ModInitializer {
	public static final String MOD_ID = "dronebox";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		// Cthulhu accessory
		Drone.registerAccessory(ModItems.EYE_ACCESSORY,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 1);
				}
			},
			Drone.AccessoryTick.EMPTY,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
				}
			}
		);

		// Toplight accessory
		Drone.registerAccessory(ModItems.TOPLIGHT_ACCESSORY,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 3);
				}
			},
			Drone.AccessoryTick.EMPTY,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
				}
			}
		);

		// Spotlight accessory
		Drone.registerAccessory(ModItems.SPOTLIGHT_ACCESSORY,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 4);
					FlareRenderManager.toggle(uuid, true);
				}
			},
			Drone.AccessoryTick.EMPTY,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
					FlareRenderManager.toggle(uuid, false);
				}
			}
		);

		// Booster accessory
		Drone.registerAccessory(Items.FIREWORK_ROCKET,
			(world, uuid, drone) -> {
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 2);
				}
			},
			(world, uuid, drone) -> {
				drone.setVelocity(drone.getVelocity().multiply(1 + Math.abs(drone.forwardInput * 0.2)));
				if (drone.forwardInput != 0){
					if (!world.isClient) {
						ServerWorld serverWorld = (ServerWorld) world;
						serverWorld.spawnParticles(ParticleTypes.FIREWORK, drone.getX(), drone.getY(), drone.getZ(), 5, 0.1, 0.1, 0.1, 0.1);
					}
				}
			}, // tick
			(world, uuid, drone) -> {   // remove
				if (world instanceof ServerWorld serverWorld) {
					EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
				}
			}
		);
		registerCommands();
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("dronetex")
				.then(CommandManager.argument("ID", IntegerArgumentType.integer())
					.then(CommandManager.argument("target", EntityArgumentType.entity())
						.executes(DroneboxMain::runDroneboxCommand))));
		});
	}

	private static int runDroneboxCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		int ID = IntegerArgumentType.getInteger(context, "ID");
		Entity targetEntity = EntityArgumentType.getEntity(context, "target");

		ServerCommandSource source = context.getSource();

		EntityTextureRegistry.setTexture(source.getWorld(), targetEntity.getUuid(), CentralDroneInit.DRONE_ENTITY_TYPE, ID);

		return Command.SINGLE_SUCCESS;
	}
}
