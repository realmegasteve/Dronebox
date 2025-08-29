package net.Neomoon.dronebox;

import net.Neomoon.dronebox.gui.DroneHUD;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.items.ModItems;
import net.Neomoon.dronebox.network.DroneBatchPayload;
import net.Neomoon.dronebox.network.DroneStatePayloadBatchesDispatcher;
import net.Neomoon.dronebox.network.RequestCameraPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static net.Neomoon.dronebox.DroneboxMain.MOD_ID;

public class CentralDroneClient implements ClientModInitializer {

	private static final long REQUEST_THROTTLE_MS = 200L;
	private static final AtomicLong lastRequestMs = new AtomicLong(0);

	@Override
	public void onInitializeClient() {
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 0, Identifier.of(MOD_ID, "textures/entity/drone.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 1, Identifier.of(MOD_ID, "textures/entity/drone_eyes.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 2, Identifier.of(MOD_ID, "textures/entity/drone_boosters.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 3, Identifier.of(MOD_ID, "textures/entity/drone_lamp.png"));
		EntityTextureRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, 4, Identifier.of(MOD_ID, "textures/entity/drone_spot.png"));

		DroneStatePayloadBatchesDispatcher.initialize();
		DroneNetworking.registerclient();
		DroneModelLayers.init();
		Radio.register();


		EntityRendererRegistry.register(CentralDroneInit.DRONE_ENTITY_TYPE, DroneRenderer::new);

		ClientWorld world = MinecraftClient.getInstance().world;

		HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc == null || mc.player == null || mc.world == null) return;

			ItemStack held = getHeldControllerStack(mc);
			if (held == null) return;


			List<UUID> registered = parseRegisteredDronesFromController(held, world);
			if (!registered.isEmpty()) {
				long now = System.currentTimeMillis();
				long last = lastRequestMs.get();
				if (now - last >= REQUEST_THROTTLE_MS && lastRequestMs.compareAndSet(last, now)) {
					requestDroneFramesFromServer(registered);
				}
			}


			try {
				DroneHUD.renderHUD(ctx.getMatrices(), ctx);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});


		KeyInterceptor.register();
	}


	private static void requestDroneFramesFromServer(List<UUID> drones) {
		try {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(drones.size());
			for (UUID u : drones) {
				ClientPlayNetworking.send(new RequestCameraPayload(u.toString()));
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	private static List<UUID> parseRegisteredDronesFromController(ItemStack controller, ClientWorld world) {
		if (controller == null || controller.isEmpty()) return Collections.emptyList();

		return DroneControllerItem.getActiveCameraDroneUUID(controller, world);

	}


	private static ItemStack getHeldControllerStack(MinecraftClient mc) {
		try {
			ItemStack main = mc.player.getMainHandStack();
			if (isControllerStack(main)) return main;
			ItemStack off = mc.player.getOffHandStack();
			if (isControllerStack(off)) return off;
			return null;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	private static boolean isControllerStack(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		try {
			return stack.getItem() == ModItems.DRONE_CONTROLLER;
		} catch (Throwable t) {
			return false;
		}
	}

	public static void openScreen(Screen screen) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc == null) return;
		mc.execute(() -> {
			try {
				mc.setScreen(screen);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}
}
