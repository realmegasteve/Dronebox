package net.Neomoon.dronebox;

import net.Neomoon.dronebox.network.MoveC2SPayload;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class CentralDroneClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, DroneRenderer::new);

		KeyInterceptor.register();
	}
}
