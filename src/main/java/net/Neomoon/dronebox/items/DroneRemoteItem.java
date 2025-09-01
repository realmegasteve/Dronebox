package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.Drone;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DroneRemoteItem extends Item {
	public DroneRemoteItem(Settings settings) {
		super(settings);
	}
	private static final String LINKED_LIST_KEY = "linked_drones";

	@Override
	public net.minecraft.util.ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (world.isClient) return net.minecraft.util.ActionResult.SUCCESS;

		ItemStack stack = player.getStackInHand(hand);
		List<String> linked = getLinkedDroneUUIDs(stack);

		if (linked.isEmpty()) {
			player.sendMessage(Text.literal("No linked drones."), true);
			return net.minecraft.util.ActionResult.FAIL;
		}

		BlockHitResult hit = raycast(world, player, 64);
		if (hit == null) {
			player.sendMessage(Text.literal("No target block in sight."), true);
			return net.minecraft.util.ActionResult.FAIL;
		}

		BlockPos target = hit.getBlockPos().up();
		ServerWorld serverWorld = (ServerWorld) world;

		for (String uuidStr : linked) {
			try {
				UUID uuid = UUID.fromString(uuidStr);
				Entity e = serverWorld.getEntity(uuid);
				if (!(e instanceof LivingEntity ent)) continue;


				if (ent instanceof PathAwareEntity mob) {
					Path path = mob.getNavigation().findPathTo(target, 1);
					if (path != null) {
						mob.getNavigation().startMovingAlong(path, 1.2);
					} else {

						player.sendMessage(Text.literal("No path found for drone " + ent.getName().getString()), true);
					}
					continue;
				}


				if (ent instanceof Drone drone) {

					Vec3d targetCenter = new Vec3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);


					drone.remoteTime = 0;
					drone.remoteTarget = targetCenter;

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		player.sendMessage(Text.literal("Drones moving to " + target.toShortString()), true);
		return net.minecraft.util.ActionResult.SUCCESS;
	}

	private BlockHitResult raycast(World world, PlayerEntity player, double range) {
		Vec3d start = player.getCameraPosVec(1.0f);
		Vec3d end = start.add(player.getRotationVec(1.0f).multiply(range));
		return world.raycast(new RaycastContext(start, end,
			RaycastContext.ShapeType.OUTLINE,
			RaycastContext.FluidHandling.NONE,
			player));
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
		boolean alreadyLinked = false;
		int foundIndex = -1;

		for (int i = 0; i < linkedDrones.size(); i++) {
			if (linkedDrones.get(i) instanceof NbtString nbtStr) {
				String stored = nbtStr.asString().orElse("");
				if (uuidStr.equals(stored)) {
					alreadyLinked = true;
					foundIndex = i;
					break;
				}
			}
		}

		if (alreadyLinked) {
			linkedDrones.remove(foundIndex);
			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone removed from remote!"), true);
			}
		} else {
			if (linkedDrones.size() >= 6) {
				if (!player.getWorld().isClient) {
					player.sendMessage(Text.literal("Maximum amount of Drones reached!"), true);
				}
				return;
			}
			linkedDrones.add(NbtString.of(uuidStr));
			if (!player.getWorld().isClient) {
				player.sendMessage(Text.literal("Drone added to remote!"), true);
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
		for (NbtElement nbtElement : listTag) {
			if (nbtElement instanceof NbtString nbtStr) {
				String raw = nbtStr.asString().orElse("").trim();

				if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() > 2) {
					raw = raw.substring(1, raw.length() - 1);
				}

				try {
					UUID.fromString(raw);
					linked_drones.add(raw);
				} catch (IllegalArgumentException e) {
					System.err.println("[DroneRemoteItem] Skipping invalid stored UUID: '" + raw + "'");
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

	@Override
	public net.minecraft.util.ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof Drone drone) {
			ItemStack stack2 = player.getStackInHand(hand);
			addDrone(stack2, drone, player);
			return net.minecraft.util.ActionResult.SUCCESS;
		}
		return net.minecraft.util.ActionResult.PASS;
	}
}
