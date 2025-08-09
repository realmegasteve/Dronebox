package net.Neomoon.dronebox.gui;

import net.Neomoon.dronebox.DroneNetworking;
import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class DroneControlScreen extends Screen {
	private final ItemStack controllerStack;

	public DroneControlScreen(ItemStack controller) {
		super(Text.literal("Drone Control"));
		this.controllerStack = controller;
	}

	@Override
	protected void init() {
		super.init();
		int y = 20;
		int buttonWidth = 200;
		int buttonHeight = 20;
		List<String> linked = DroneControllerItem.getLinkedDroneUUIDs(controllerStack);

		if (linked.isEmpty()) {
			this.addDrawableChild(ButtonWidget.builder(Text.literal("No linked drones"), b -> this.close())
				.position(this.width / 2 - buttonWidth / 2, y)
				.size(buttonWidth, buttonHeight)
				.build());
			y += 24;
		} else {
			for (String uuidStr : linked) {
				String label = "Drone " + uuidStr.substring(0, Math.min(8, uuidStr.length()));


				final String droneUuid = uuidStr;

				this.addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {

					sendTogglePacket(droneUuid);

				}).position(this.width / 2 - buttonWidth / 2, y).size(buttonWidth, buttonHeight).build());
				y += 24;
			}
		}


		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), (b) -> this.close())
			.position(this.width / 2 - buttonWidth / 2, this.height - 30)
			.size(buttonWidth, 20)
			.build());
	}

	private void sendTogglePacket(String droneUuid) {

		ToggleC2SPayload payload = new ToggleC2SPayload(droneUuid);
		ClientPlayNetworking.send(payload);

	}
}
