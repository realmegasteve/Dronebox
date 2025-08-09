package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.CentralDroneInit;
import net.Neomoon.dronebox.DroneNetworking;
import net.Neomoon.dronebox.gui.DroneControlScreen;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.*;
	import java.util.stream.Collectors;


public class DroneControllerItem extends Item {
	private static final String SUBNBT = "dronebox";
	private static final String DRONE_LIST_TAG = "Drones";

	public DroneControllerItem(Settings settings) {
		super(settings);
	}


	public static List<String> getLinkedDroneUUIDs(ItemStack stack) {
		try {
			Optional<NbtCompound> sub = readSubNbt(stack, SUBNBT);
			if (sub.isEmpty()) return Collections.emptyList();
			String csv = String.valueOf(sub.get().getString(DRONE_LIST_TAG));
			if (csv == null || csv.isEmpty()) return Collections.emptyList();
			return Arrays.stream(csv.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
		} catch (Throwable t) {
			logOnce("getLinkedDroneUUIDs error: " + t);
			return Collections.emptyList();
		}
	}


	public static void addLinkedDrone(ItemStack stack, String uuid) {
		try {
			List<String> list = new ArrayList<>(getLinkedDroneUUIDs(stack));
			if (!list.contains(uuid)) {
				list.add(uuid);
				writeDroneListToStack(stack, list);
			}
		} catch (Throwable t) {
			logOnce("addLinkedDrone error: " + t);
		}
	}


	public static void removeLinkedDrone(ItemStack stack, String uuid) {
		try {
			List<String> list = new ArrayList<>(getLinkedDroneUUIDs(stack));
			if (list.remove(uuid)) {
				writeDroneListToStack(stack, list);
			}
		} catch (Throwable t) {
			logOnce("removeLinkedDrone error: " + t);
		}
	}


	public static void addDrone(ItemStack stack, CentralDroneInit.Drone drone) {
		addLinkedDrone(stack, drone.getUuidAsString());
	}


	@Override
	public Text getName(ItemStack stack) {
		return Text.literal("Drone Controller");
	}



	private static void writeDroneListToStack(ItemStack stack, List<String> list) throws Exception {
		String csv = String.join(",", list);
		Optional<NbtCompound> subOpt = readSubNbt(stack, SUBNBT);
		NbtCompound sub = subOpt.orElseGet(() -> new NbtCompound());
		sub.putString(DRONE_LIST_TAG, csv);
		writeSubNbt(stack, SUBNBT, sub);
	}

	private static Optional<NbtCompound> readSubNbt(ItemStack stack, String key) {
		try {
			Method m;
			try {
				m = ItemStack.class.getMethod("getSubNbt", String.class);
				Object res = m.invoke(stack, key);
				if (res instanceof NbtCompound nc && !nc.isEmpty()) return Optional.of(nc);
				return Optional.ofNullable((NbtCompound) res);
			} catch (NoSuchMethodException ignored) {}

			try {
				m = ItemStack.class.getMethod("getNbt");
				Object res = m.invoke(stack);
				if (res instanceof NbtCompound nc) {
					NbtCompound sub = nc;
					return Optional.ofNullable(sub.isEmpty() ? null : sub);
				}
			} catch (NoSuchMethodException ignored) {}


			try {
				m = ItemStack.class.getMethod("getOrCreateNbt");
				Object res = m.invoke(stack);
				if (res instanceof NbtCompound nc) {
					NbtCompound sub = nc;
					return Optional.ofNullable(sub.isEmpty() ? null : sub);
				}
			} catch (NoSuchMethodException ignored) {}


			return Optional.empty();
		} catch (Throwable t) {
			return Optional.empty();
		}
	}


	private static void writeSubNbt(ItemStack stack, String key, NbtCompound value) throws Exception {
		try {
			Method getOrCreate = ItemStack.class.getMethod("getOrCreateNbt");
			Object compound = getOrCreate.invoke(stack);
			if (compound instanceof NbtCompound nc) {
				nc.put(key, value);

				try {
					Method set = ItemStack.class.getMethod("setNbt", NbtCompound.class);
					set.invoke(stack, nc);
					return;
				} catch (NoSuchMethodException ignored) {}

				return;
			}
		} catch (NoSuchMethodException ignored) {}


		try {
			Method get = ItemStack.class.getMethod("getNbt");
			Object compound = get.invoke(stack);
			NbtCompound nc;
			if (compound instanceof NbtCompound existing) {
				nc = existing;
			} else {
				nc = new NbtCompound();
			}
			nc.put(key, value);
			try {
				Method set = ItemStack.class.getMethod("setNbt", NbtCompound.class);
				set.invoke(stack, nc);
				return;
			} catch (NoSuchMethodException ignored) {}
		} catch (NoSuchMethodException ignored) {}

	}
	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof CentralDroneInit.Drone drone) {
			addDrone(stack, drone);
			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone added to controller!"), true);

			}
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
