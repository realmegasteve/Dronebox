package net.Neomoon.dronebox;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.Neomoon.dronebox.items.MainModItemw;
import net.Neomoon.dronebox.items.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class DroneboxMain implements ModInitializer {
	public static final String MOD_ID = "dronebox";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ModItems.registerModItems();
			EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 0, Identifier.of(MOD_ID, "textures/entity/drone.png"));
			EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 1, Identifier.of(MOD_ID, "textures/entity/drone_eyes.png"));
			EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 2, Identifier.of(MOD_ID, "textures/entity/drone_boosters.png"));
			EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 3, Identifier.of(MOD_ID, "textures/entity/drone_lamp.png"));
			EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 4, Identifier.of(MOD_ID, "textures/entity/drone_spot.png"));

			// Cthulhu accessory
			Drone.registerAccessory(ModItems.EYE_ACCESSORY,
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 1);
					}
				},
				(world, uuid, drone) -> {

				},
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
				(world, uuid, drone) -> {},
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
						FlareRenderManager.toggle(uuid, false);
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
				(world, uuid, drone) -> {},
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
					}
				}
			);

		} else {
			MainModItemw.registerModItems();

			// Cthulhu accessory
			Drone.registerAccessory(MainModItemw.EYE_ACCESSORY,
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 1);
					}
				},
				(world, uuid, drone) -> {

				},
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
					}
				}
			);

// Spotlight accessory
			Drone.registerAccessory(MainModItemw.SPOTLIGHT_ACCESSORY,
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 4);
						FlareRenderManager.toggle(uuid, true);
					}
				},
				(world, uuid, drone) -> {},
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
						FlareRenderManager.toggle(uuid, false);
					}
				}
			);

// Toplight accessory
			Drone.registerAccessory(MainModItemw.TOPLIGHT_ACCESSORY,
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 3);
					}
				},
				(world, uuid, drone) -> {},
				(world, uuid, drone) -> {
					if (world instanceof ServerWorld serverWorld) {
						EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
					}
				}
			);
		}



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
