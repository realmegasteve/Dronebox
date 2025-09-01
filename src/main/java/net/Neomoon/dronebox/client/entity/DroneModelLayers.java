package net.Neomoon.dronebox.client.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DroneModelLayers {
	public static final EntityModelLayer DRONE =
		new EntityModelLayer(Identifier.of("dronebox", "drone"), "main");

	public static void init() {
		EntityModelLayerRegistry.registerModelLayer(DRONE, DroneEntityModel::getTexturedModelData);
	}
}
