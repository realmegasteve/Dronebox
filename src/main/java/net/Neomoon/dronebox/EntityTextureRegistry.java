package net.Neomoon.dronebox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityTextureRegistry {
	private static final Map<EntityType<?>, Map<Integer, Identifier>> REGISTERED = new HashMap<>();

	public static void register(EntityType<?> type, int id, Identifier texture) {
		REGISTERED.computeIfAbsent(type, t -> new HashMap<>()).put(id, texture);
	}

	public static void setTexture(ServerWorld world, UUID uuid, EntityType<?> type, int id) {
		if (world == null) return;
		Entity e = world.getEntity(uuid);
		if (e != null && e.getType() == type) {
			if (e instanceof Drone d) {
				d.getDataTracker().set(Drone.TEXTURE_ID, id);
			}
		}
	}

	public static Identifier getTexture(Entity entity, Identifier defaultTex) {
		if (entity == null) return defaultTex;
		Map<Integer, Identifier> map = REGISTERED.get(entity.getType());
		if (map == null) return defaultTex;

		int id = 0;
		if (entity instanceof Drone d) {
			id = d.getDataTracker().get(Drone.TEXTURE_ID);
		} else {

			try {
				var field = entity.getClass().getField("TEXTURE_ID");
				Object val = field.get(null);
				if (val instanceof net.minecraft.entity.data.TrackedData) {
					id = entity.getDataTracker().get((net.minecraft.entity.data.TrackedData<Integer>) val);
				}
			} catch (Exception ignored) {}
		}

		return map.getOrDefault(id, defaultTex);
	}
}
