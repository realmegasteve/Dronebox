package net.Neomoon.dronebox;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlareRenderManager {
	private static final Set<UUID> enabled = new HashSet<>();

	public static void toggle(UUID uuid, boolean enable) {
		if (enable) enabled.add(uuid);
		else enabled.remove(uuid);
	}

	public static boolean isEnabled(UUID uuid) {
		return enabled.contains(uuid);
	}
}
