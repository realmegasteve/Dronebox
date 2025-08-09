package net.Neomoon.dronebox;

import net.Neomoon.dronebox.items.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneboxMain implements ModInitializer {
	public static final String MOD_ID = "dronebox";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
	}
}
