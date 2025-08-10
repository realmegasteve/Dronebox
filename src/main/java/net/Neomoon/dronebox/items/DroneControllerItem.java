package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.CentralDroneInit;
import net.Neomoon.dronebox.DroneNetworking;
import net.Neomoon.dronebox.DroneboxMain;
import net.Neomoon.dronebox.gui.DroneControlScreen;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.python.antlr.ast.Str;

import java.lang.reflect.Method;
import java.util.*;
	import java.util.stream.Collectors;


public class DroneControllerItem extends Item {
	private static final String SUBNBT = "dronebox";
	private static final String DRONE_LIST_TAG = "Drones";

	public void addDrone(ItemStack controllerStack, LivingEntity drone, PlayerEntity player){
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		System.out.println(root.toString());

		NbtList linkedDrones;

		if (!root.contains("linked_drones")) {
			linkedDrones = new NbtList();
			root.put("linked_drones", linkedDrones);
		} else {
			linkedDrones = root.getListOrEmpty("linked_drones");
		}

		NbtString uuid = NbtString.of(drone.getUuidAsString());

		if (linkedDrones.contains(uuid)){
			linkedDrones.remove(uuid);
			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone removed to controller!"), true);
			}
		} else {
			linkedDrones.add(uuid);
			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone added to controller!"), true);
			}
		}

		root.put("linked_drones", linkedDrones);

		linkedDrones = root.getListOrEmpty("linked_drones");

		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}

	public static List<String> getLinkedDroneUUIDs(ItemStack controllerStack){
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		List<String> linked_drones = new ArrayList<>();
		NbtList listTag = root.getListOrEmpty("linked_drones");

		for (int i = 0; i < listTag.size(); i++) {
			linked_drones.add(listTag.get(i).toString());
		}

		return linked_drones;
	}

	public DroneControllerItem(Settings settings) {
		super(settings);
	}

	@Override
	public Text getName(ItemStack stack) {
		return Text.literal("Drone Controller");
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof CentralDroneInit.Drone drone) {
			ItemStack stack2 = player.getStackInHand(hand);
			addDrone(stack2, drone, player);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (world.isClient) {
			if (player.isSneaking()) {

				net.minecraft.client.MinecraftClient.getInstance().setScreen(new DroneControlScreen(stack));
			} else {

				List<String> linkedDrones = getLinkedDroneUUIDs(stack);
				if (!linkedDrones.isEmpty()) {
					for (String droneUUID : linkedDrones) {
						sendTogglePacket(droneUUID);
					}
					player.sendMessage(Text.literal("Toggled all linked drones."), true);
				} else {
					player.sendMessage(Text.literal("No linked drones to toggle."), true);
				}
			}
		}
		return ActionResult.SUCCESS;
	}


	private static boolean logged = false;
	private static void logOnce(String msg) {
		if (logged) return;
		logged = true;
		try {
			System.err.println("[DroneControllerItem] " + msg);
		} catch (Throwable ignore) {}
	}
	private void sendTogglePacket(String droneUuid) {

		ToggleC2SPayload payload = new ToggleC2SPayload(droneUuid);
		ClientPlayNetworking.send(payload);

	}
}
