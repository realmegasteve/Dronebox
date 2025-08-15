package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.CentralDroneInit;
import net.Neomoon.dronebox.gui.DroneControlScreen;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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


public class DroneControllerItem extends Item {
	private static final String LINKED_LIST_KEY = "linked_drones";
	private static final String CONTROL_STATES_KEY = "control_states";
	private static final String CAMERA_STATES_KEY = "camera_states";


	private static final String ENABLED_STATES_KEY = "enabled_states";
	private static final String NAMES_KEY = "drone_names";

	public DroneControllerItem(Settings settings) {
		super(settings);
	}


	public void addDrone(ItemStack controllerStack, LivingEntity drone, PlayerEntity player) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		NbtList linkedDrones;
		if (!root.contains(LINKED_LIST_KEY)) {
			linkedDrones = new NbtList();
			root.put(LINKED_LIST_KEY, linkedDrones);
		} else {
			linkedDrones = root.getListOrEmpty(LINKED_LIST_KEY);
		}

		String uuidStr = drone.getUuidAsString();
		NbtString uuidNbt = NbtString.of(uuidStr);

		if (linkedDrones.contains(uuidNbt)) {

			linkedDrones.remove(uuidNbt);

			if (root.contains(CONTROL_STATES_KEY)) {
				root.getCompound(CONTROL_STATES_KEY).ifPresent(compound -> compound.remove(uuidStr));
			}
			if (root.contains(CAMERA_STATES_KEY)) {
				root.getCompound(CAMERA_STATES_KEY).ifPresent(compound -> compound.remove(uuidStr));
			}
			if (root.contains(ENABLED_STATES_KEY)) {
				root.getCompound(ENABLED_STATES_KEY).ifPresent(compound -> compound.remove(uuidStr));
			}
			if (root.contains(NAMES_KEY)) {
				root.getCompound(NAMES_KEY).ifPresent(compound -> compound.remove(uuidStr));
			}

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
			linkedDrones.add(uuidNbt);


			NbtCompound controlStates = root.contains(CONTROL_STATES_KEY) ? root.getCompoundOrEmpty(CONTROL_STATES_KEY) : new NbtCompound();
			NbtCompound cameraStates = root.contains(CAMERA_STATES_KEY) ? root.getCompoundOrEmpty(CAMERA_STATES_KEY) : new NbtCompound();
			NbtCompound enabledStates = root.contains(ENABLED_STATES_KEY) ? root.getCompoundOrEmpty(ENABLED_STATES_KEY) : new NbtCompound();

			controlStates.putBoolean(uuidStr, true);
			cameraStates.putBoolean(uuidStr, false);
			enabledStates.putBoolean(uuidStr, true);

			root.put(CONTROL_STATES_KEY, controlStates);
			root.put(CAMERA_STATES_KEY, cameraStates);
			root.put(ENABLED_STATES_KEY, enabledStates);

			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone added to controller!"), true);
			}
		}

		root.put(LINKED_LIST_KEY, linkedDrones);
		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}


	public static List<String> getLinkedDroneUUIDs(ItemStack controllerStack) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		List<String> linked_drones = new ArrayList<>();
		NbtList listTag = root.getListOrEmpty(LINKED_LIST_KEY);

		boolean modified = false;
		for (int i = 0; i < listTag.size(); i++) {
			if (listTag.get(i) instanceof NbtString nbtStr) {
				String raw = nbtStr.asString().orElse("").trim();


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
			} else {
				modified = true;
			}
		}


		if (modified) {
			NbtList cleanList = new NbtList();
			for (String s : linked_drones) cleanList.add(NbtString.of(s));
			root.put(LINKED_LIST_KEY, cleanList);
			controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		}

		return linked_drones;
	}


	public static void removeDroneByUUID(ItemStack controllerStack, String uuidStr) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		NbtList linkedDrones = root.getListOrEmpty(LINKED_LIST_KEY);

		for (int i = 0; i < linkedDrones.size(); i++) {
			if (linkedDrones.get(i) instanceof NbtString nbtStr &&
				uuidStr.equals(nbtStr.asString().orElse(""))) {
				linkedDrones.remove(i);
				break;
			}
		}

		if (root.contains(CONTROL_STATES_KEY)) {
			NbtCompound cs = root.getCompoundOrEmpty(CONTROL_STATES_KEY);
			cs.remove(uuidStr);
			root.put(CONTROL_STATES_KEY, cs);
		}
		if (root.contains(CAMERA_STATES_KEY)) {
			NbtCompound cam = root.getCompoundOrEmpty(CAMERA_STATES_KEY);
			cam.remove(uuidStr);
			root.put(CAMERA_STATES_KEY, cam);
		}
		if (root.contains(ENABLED_STATES_KEY)) {
			NbtCompound en = root.getCompoundOrEmpty(ENABLED_STATES_KEY);
			en.remove(uuidStr);
			root.put(ENABLED_STATES_KEY, en);
		}
		if (root.contains(NAMES_KEY)) {
			NbtCompound nm = root.getCompoundOrEmpty(NAMES_KEY);
			nm.remove(uuidStr);
			root.put(NAMES_KEY, nm);
		}

		root.put(LINKED_LIST_KEY, linkedDrones);
		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}


	public static boolean isDroneControlEnabled(ItemStack controllerStack, String uuidStr) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		if (!root.contains(CONTROL_STATES_KEY)) return true;
		NbtCompound cs = root.getCompoundOrEmpty(CONTROL_STATES_KEY);
		if (!cs.contains(uuidStr)) return true;
		return cs.getBoolean(uuidStr, false);
	}

	public static void setDroneControlEnabled(ItemStack controllerStack, String uuidStr, boolean enabled) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		NbtCompound cs = root.contains(CONTROL_STATES_KEY) ? root.getCompoundOrEmpty(CONTROL_STATES_KEY) : new NbtCompound();
		cs.putBoolean(uuidStr, enabled);
		root.put(CONTROL_STATES_KEY, cs);
		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}


	public static boolean isDroneCameraEnabled(ItemStack controllerStack, String uuidStr) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		if (!root.contains(CAMERA_STATES_KEY)) return false;
		NbtCompound cs = root.getCompoundOrEmpty(CAMERA_STATES_KEY);
		if (!cs.contains(uuidStr)) return false;
		return cs.getBoolean(uuidStr, false);
	}

	public static void setDroneCameraEnabled(ItemStack controllerStack, String uuidStr, boolean enabled) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		NbtCompound cs = root.contains(CAMERA_STATES_KEY) ? root.getCompoundOrEmpty(CAMERA_STATES_KEY) : new NbtCompound();
		cs.putBoolean(uuidStr, enabled);
		root.put(CAMERA_STATES_KEY, cs);
		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}


	public static boolean isDroneEnabled(ItemStack controllerStack, String uuidStr) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		if (!root.contains(ENABLED_STATES_KEY)) return true;
		NbtCompound es = root.getCompoundOrEmpty(ENABLED_STATES_KEY);
		return es.contains(uuidStr) ? es.getBoolean(uuidStr, false) : true;
	}

	public static void setDroneEnabled(ItemStack controllerStack, String uuidStr, boolean enabled) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		NbtCompound es = root.contains(ENABLED_STATES_KEY) ? root.getCompoundOrEmpty(ENABLED_STATES_KEY) : new NbtCompound();
		es.putBoolean(uuidStr, enabled);
		root.put(ENABLED_STATES_KEY, es);
		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}


	public static String getStoredDroneName(ItemStack controllerStack, String uuidStr) {
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		if (!root.contains(NAMES_KEY)) return null;
		NbtCompound names = root.getCompoundOrEmpty(NAMES_KEY);
		if (!names.contains(uuidStr)) return null;
		return names.getString(uuidStr, "");
	}


	public static void setStoredDroneName(ItemStack controllerStack, String uuidStr, String name) {
		if (name == null) name = "";
		NbtComponent comp = controllerStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		NbtCompound names = root.contains(NAMES_KEY) ? root.getCompoundOrEmpty(NAMES_KEY) : new NbtCompound();
		names.putString(uuidStr, name);
		root.put(NAMES_KEY, names);
		controllerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
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
		if (entity instanceof CentralDroneInit.Drone drone) {
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
