package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.CentralDroneInit;
import net.Neomoon.dronebox.Drone;
import net.Neomoon.dronebox.gui.DroneControlScreen;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.Neomoon.dronebox.items.ModItems.CONTROLLER_COMPONENT;


public class DroneControllerItem extends Item {
	protected static final String LINKED_LIST_KEY = "linked_drones";
	protected static final String CONTROL_STATES_KEY = "control_states";
	protected static final String CAMERA_STATES_KEY = "camera_states";

	protected static final String ENABLED_STATES_KEY = "enabled_states";
	protected static final String NAMES_KEY = "drone_names";

	public DroneControllerItem(Settings settings) {
		super(settings);
	}

	public void addDrone(ItemStack controllerStack, LivingEntity drone, PlayerEntity player) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();

		List<String> linkedDrones = controller.linkedDrones();

		String uuidStr = drone.getUuidAsString();

		if (linkedDrones.contains(uuidStr)) {
			linkedDrones.remove(uuidStr);
			controller.controlStates().remove(uuidStr);
			controller.cameraStates().remove(uuidStr);
			controller.enabledStates().remove(uuidStr);
			controller.droneNames().remove(uuidStr);

			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone removed from controller!"), true);
			}
		} else {
			if (linkedDrones.size() >= 6) {
				if (!player.getWorld().isClient) {
					player.sendMessage(Text.literal("Maximum amount of Drones reached!"), true);
				}
				return;
			}
			linkedDrones.add(uuidStr);

			controller.controlStates().put(uuidStr, true);
			controller.cameraStates().put(uuidStr, false);
			controller.enabledStates().put(uuidStr, true);

			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone added to controller!"), true);
			}
		}
		controllerStack.set(CONTROLLER_COMPONENT, controller);
	}

	public static List<String> getLinkedDroneUUIDs(ItemStack controllerStack) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY);

		List<String> linked_drones = new ArrayList<>();
		List<String> controllerDrones = controller.linkedDrones();

		boolean modified = false;
		for (String string : controllerDrones) {
			String raw = string.trim();

			if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() > 2) {
				raw = raw.substring(1, raw.length() - 1);
			}

			try {
				UUID.fromString(raw);
				linked_drones.add(raw);
			} catch (IllegalArgumentException e) {
				System.err.println("[DroneControllerItem] Skipping invalid stored UUID: '" + raw + "'");
				modified = true;
			}
		}

		if (modified) {
			controllerDrones.clear();
			controllerDrones.addAll(linked_drones);
			controllerStack.set(CONTROLLER_COMPONENT, controller.copy());
		}

		return linked_drones;
	}

	public static void removeDroneByUUID(ItemStack controllerStack, String uuidStr) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		controller.linkedDrones().remove(uuidStr);
		controller.controlStates().remove(uuidStr);
		controller.cameraStates().remove(uuidStr);
		controller.enabledStates().remove(uuidStr);
		controller.droneNames().remove(uuidStr);
		controllerStack.set(CONTROLLER_COMPONENT, controller);
	}

	public static boolean isDroneControlEnabled(ItemStack controllerStack, String uuidStr) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY);
		return controller.controlStates().getOrDefault(uuidStr, true);
	}

	public static void setDroneControlEnabled(ItemStack controllerStack, String uuidStr, boolean enabled) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		controller.controlStates().put(uuidStr, enabled);
		controllerStack.set(CONTROLLER_COMPONENT, controller);
	}

	public static boolean isDroneCameraEnabled(ItemStack controllerStack, String uuidStr) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY);
		return controller.cameraStates().getOrDefault(uuidStr, false);
	}

	public static void setDroneCameraEnabled(ItemStack controllerStack, String uuidStr, boolean enabled) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		controller.cameraStates().put(uuidStr, enabled);
		controllerStack.set(CONTROLLER_COMPONENT, controller);
	}

	public static boolean isDroneEnabled(ItemStack controllerStack, String uuidStr) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		return controller.enabledStates().getOrDefault(uuidStr, true);
	}

	public static void setDroneEnabled(ItemStack controllerStack, String uuidStr, boolean enabled) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		controller.enabledStates().put(uuidStr, enabled);
		controllerStack.set(CONTROLLER_COMPONENT, controller);
	}

	public static String getStoredDroneName(ItemStack controllerStack, String uuidStr) {
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		return controller.droneNames().get(uuidStr);
	}

	public static void setStoredDroneName(ItemStack controllerStack, String uuidStr, String name) {
		if (name == null) name = "";
		ControllerComponent controller = controllerStack.getOrDefault(CONTROLLER_COMPONENT, ControllerComponent.EMPTY).copy();
		controller.droneNames().put(uuidStr, name);
		controllerStack.set(CONTROLLER_COMPONENT, controller);
	}

	public static Text getDroneDisplayName(ItemStack controllerStack, String uuidStr) {
		String stored = getStoredDroneName(controllerStack, uuidStr);
		if (stored != null && !stored.isEmpty()) {
			return Text.literal(stored);
		}


		Entity ent = getDroneEntityByUUID(controllerStack, uuidStr);
		if (ent != null && ent.hasCustomName()) {
			return ent.getCustomName();
		}


		String shortUuid;
		try {
			shortUuid = uuidStr.substring(0, Math.min(8, uuidStr.length()));
		} catch (Exception e) {
			shortUuid = uuidStr;
		}
		return Text.literal(shortUuid);
	}


	public static void setDroneName(ItemStack controllerStack, String uuidStr, String newName, World world) {
		if (newName == null) newName = "";


		setStoredDroneName(controllerStack, uuidStr, newName);


		if (world != null && !world.isClient) {
			try {
				UUID uuid = UUID.fromString(uuidStr);
				Entity entity = null;

				if (world instanceof ServerWorld serverWorld) {

					Box worldBox = new Box(
						Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
					);


					for (Entity e : serverWorld.getEntitiesByType(CentralDroneInit.DRONE_ENTITY_TYPE, worldBox, e -> true)) {
						if (e.getUuid().equals(uuid)) {
							entity = e;
							break;
						}
					}
				}



				if (entity != null && entity.getType().isSaveable() && entity.isAlive()) {
					Text text = Text.literal(newName);
					entity.setCustomName(text);
					if (entity instanceof MobEntity mob) {
						mob.setPersistent();
					}
				}
			} catch (Exception ignored) {

			}
		}
	}

	public static Entity getDroneEntityByUUID(ItemStack controllerStack, String uuidStr) {
		try {
			UUID uuid = UUID.fromString(uuidStr);
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc != null && mc.world != null) {
				try {
					try {
						Method m = mc.world.getClass().getMethod("getEntityByUuid", UUID.class);
						Entity byUuid = (Entity) m.invoke(mc.world, uuid);
						if (byUuid != null) return byUuid;
					} catch (NoSuchMethodException ignored) { }

					try {
						Method m2 = mc.world.getClass().getMethod("getEntity", UUID.class);
						Entity byUuid2 = (Entity) m2.invoke(mc.world, uuid);
						if (byUuid2 != null) return byUuid2;
					} catch (NoSuchMethodException ignored) { }


					try {
						Method entitiesMethod = mc.world.getClass().getMethod("getEntities");
						Object listObj = entitiesMethod.invoke(mc.world);
						if (listObj instanceof Iterable<?>) {
							for (Object o : (Iterable<?>) listObj) {
								if (o instanceof Entity e && e.getUuid().equals(uuid)) return e;
							}
						}
					} catch (NoSuchMethodException ignored) { }
				} catch (IllegalAccessException | InvocationTargetException ignored) { }
			}
		} catch (Exception ignored) { }

		return null;
	}



	@Override
	public Text getName(ItemStack stack) {
		return Text.literal("Drone Controller");
	}

	@Override
	public net.minecraft.util.ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, net.minecraft.util.Hand hand) {
		if (entity instanceof Drone drone) {
			ItemStack stack2 = player.getStackInHand(hand);
			addDrone(stack2, drone, player);
			return net.minecraft.util.ActionResult.SUCCESS;
		}
		return net.minecraft.util.ActionResult.PASS;
	}
	public static List<LivingEntity> getActiveCameraDrones(ItemStack controllerStack, ClientWorld world) {
		List<LivingEntity> activeDrones = new ArrayList<>();
		List<String> linkedUUIDs = getLinkedDroneUUIDs(controllerStack);

		for (String uuidStr : linkedUUIDs) {
			if (isDroneCameraEnabled(controllerStack, uuidStr)) {
				try {
					UUID uuid = UUID.fromString(uuidStr);
					Entity entity = world.getEntity(uuid);
					if (entity instanceof LivingEntity drone) {
						activeDrones.add(drone);
					}
				} catch (IllegalArgumentException e) {
					System.err.println("[DroneControllerItem] Invalid UUID while fetching camera drones: " + uuidStr);
				}
			}
		}

		return activeDrones;
	}

	public static List<UUID> getActiveCameraDroneUUID(ItemStack controllerStack, ClientWorld world) {
		List<UUID> activeCameraDrones = new ArrayList<>();
		List<String> linkedUUIDs = getLinkedDroneUUIDs(controllerStack);

		for (String uuidStr : linkedUUIDs) {
			if (isDroneCameraEnabled(controllerStack, uuidStr)) {
				activeCameraDrones.add(UUID.fromString(uuidStr));
			}
		}

		return activeCameraDrones;
	}

	@Override
	public net.minecraft.util.ActionResult use(net.minecraft.world.World world, PlayerEntity player, net.minecraft.util.Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (world.isClient) {
			if (player.isSneaking()) {
				MinecraftClient.getInstance().execute(() -> {
					MinecraftClient.getInstance().setScreen(new DroneControlScreen(stack));
				});

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
		return net.minecraft.util.ActionResult.SUCCESS;
	}


	private void sendTogglePacket(String droneUuid) {
		ToggleC2SPayload payload = new ToggleC2SPayload(droneUuid);
		ClientPlayNetworking.send(payload);
	}
}
