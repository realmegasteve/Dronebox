package net.Neomoon.dronebox;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.Neomoon.dronebox.items.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
		Radio.register();
		ModItems.registerModItems();

		DroneAccessories.registerAccessory(Items.FIREWORK_ROCKET, (drone, stack) -> {
			EntityTextureRegistry.setTexture((ServerWorld) drone.getWorld(), drone.getUuid(), CentralDroneInit.DRONE_ENTITY_TYPE, 2);
			drone.setVelocity(drone.getVelocity().multiply(1.5));
		});

		DroneAccessories.registerAccessory(ModItems.EYE_ACCESSORY, (drone, stack) -> {
			EntityTextureRegistry.setTexture((ServerWorld) drone.getWorld(), drone.getUuid(), CentralDroneInit.DRONE_ENTITY_TYPE, 1);
		});

		DroneAccessories.registerAccessory(ModItems.SPOTLIGHT_ACCESSORY, (drone, stack) -> {
			EntityTextureRegistry.setTexture((ServerWorld) drone.getWorld(), drone.getUuid(), CentralDroneInit.DRONE_ENTITY_TYPE, 4);
		});

		DroneAccessories.registerAccessory(ModItems.TOPLIGHT_ACCESSORY, (drone, stack) -> {
			EntityTextureRegistry.setTexture((ServerWorld) drone.getWorld(), drone.getUuid(), CentralDroneInit.DRONE_ENTITY_TYPE, 3);
		});
		DroneAccessories.registerAccessory(ItemStack.EMPTY.getItem(), (drone, stack) -> {
			EntityTextureRegistry.setTexture((ServerWorld) drone.getWorld(), drone.getUuid(), CentralDroneInit.DRONE_ENTITY_TYPE, 0);
		});



		registerCommands();
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 0, Identifier.of(MOD_ID, "textures/entity/drone.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 1, Identifier.of(MOD_ID, "textures/entity/drone_eyes.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 2, Identifier.of(MOD_ID, "textures/entity/drone_boosters.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 3, Identifier.of(MOD_ID, "textures/entity/drone_lamp.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 4, Identifier.of(MOD_ID, "textures/entity/drone_spot.png"));
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
