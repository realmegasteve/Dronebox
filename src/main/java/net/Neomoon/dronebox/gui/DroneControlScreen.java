package net.Neomoon.dronebox.gui;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.ViewTogglePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class DroneControlScreen extends Screen {

	private final ItemStack controllerStack;
	private final List<DroneEntry> droneEntries = new ArrayList<>();
	private final List<PanelRect> panels = new ArrayList<>();
	private final Map<ButtonWidget, Text> buttonTooltips = new HashMap<>();

	public DroneControlScreen(ItemStack controller) {
		super(Text.literal("Drone Control"));
		this.controllerStack = controller;
	}

	@Override
	protected void init() {
		super.init();
		this.clearChildren();
		panels.clear();
		droneEntries.clear();
		buttonTooltips.clear();

		int panelWidth = 240;
		int panelHeight = 50;
		int panelSpacingX = 15;
		int panelSpacingY = 10;
		int buttonWidth = 50;
		int buttonHeight = 20;

		List<String> droneUUIDs = DroneControllerItem.getLinkedDroneUUIDs(controllerStack);
		int columns = 2;
		int totalPanelsWidth = columns * panelWidth + (columns - 1) * panelSpacingX;
		int startX = (this.width - totalPanelsWidth) / 2;
		int startY = 20;

		for (int i = 0; i < droneUUIDs.size(); i++) {
			UUID droneUuid = UUID.fromString(droneUUIDs.get(i));

			int col = i % columns;
			int row = i / columns;
			int panelX = startX + col * (panelWidth + panelSpacingX);
			int panelY = startY + row * (panelHeight + panelSpacingY);

			panels.add(new PanelRect(panelX, panelY, panelWidth, panelHeight, true));

			int textFieldWidth = panelWidth - 32;
			TextFieldWidget nameField = new TextFieldWidget(
				this.textRenderer,
				panelX + 4,
				panelY + 4,
				textFieldWidth,
				20,
				Text.literal("Drone Name")
			);
			String currentName = String.valueOf(DroneControllerItem.getDroneDisplayName(controllerStack, droneUuid.toString()));
			nameField.setText(currentName != null ? currentName : "");
			this.addDrawableChild(nameField);
			droneEntries.add(new DroneEntry(droneUuid.toString(), nameField));

			ButtonWidget checkmarkBtn = ButtonWidget.builder(
					Text.literal("✔"),
					btn -> {
						String name = nameField.getText();
						DroneControllerItem.setDroneName(controllerStack, droneUuid.toString(), name, MinecraftClient.getInstance().world);
					}
				).position(panelX + panelWidth - buttonWidth - 4, panelY + 4)
				.size(buttonWidth, buttonHeight)
				.build();
			addDrawableChild(checkmarkBtn);

			int btnY = panelY + panelHeight - buttonHeight - 4;
			int totalBtnWidth = (buttonWidth * 4) + (4 * 3);
			int btnStartX = panelX + (panelWidth - totalBtnWidth) / 2;

			ButtonWidget controlBtn = ButtonWidget.builder(
				Text.literal("Control: " + (DroneControllerItem.isDroneControlEnabled(controllerStack, String.valueOf(droneUuid)) ? "ON" : "OFF")),
				btn -> {
					boolean newState = !DroneControllerItem.isDroneControlEnabled(controllerStack, String.valueOf(droneUuid));
					DroneControllerItem.setDroneControlEnabled(controllerStack, String.valueOf(droneUuid), newState);
					btn.setMessage(Text.literal("Control: " + (newState ? "ON" : "OFF")));
				}
			).position(btnStartX, btnY).size(buttonWidth, buttonHeight).build();
			buttonTooltips.put(controlBtn, Text.literal("Toggle control state"));
			addDrawableChild(controlBtn);

			ButtonWidget cameraBtn = ButtonWidget.builder(
				Text.literal("Camera: " + (DroneControllerItem.isDroneCameraEnabled(controllerStack, String.valueOf(droneUuid)) ? "ON" : "OFF")),
				btn -> {
					boolean newState = !DroneControllerItem.isDroneCameraEnabled(controllerStack, String.valueOf(droneUuid));
					DroneControllerItem.setDroneCameraEnabled(controllerStack, String.valueOf(droneUuid), newState);
					btn.setMessage(Text.literal("Camera: " + (newState ? "ON" : "OFF")));
				}
			).position(btnStartX + buttonWidth + 4, btnY).size(buttonWidth, buttonHeight).build();
			buttonTooltips.put(cameraBtn, Text.literal("Toggle camera feed"));
			addDrawableChild(cameraBtn);

			ButtonWidget removeBtn = ButtonWidget.builder(
				Text.literal("Remove").formatted(Formatting.RED),
				btn -> {
					DroneControllerItem.removeDroneByUUID(controllerStack, String.valueOf(droneUuid));
					init();
				}
			).position(btnStartX + (buttonWidth + 4) * 2, btnY).size(buttonWidth, buttonHeight).build();
			buttonTooltips.put(removeBtn, Text.literal("Remove this drone"));
			addDrawableChild(removeBtn);

			ButtonWidget extraBtn = ButtonWidget.builder(
				Text.literal("View"),
				btn -> {
					ClientPlayNetworking.send(new ViewTogglePayload(droneUuid.toString()));
				}
			).position(btnStartX + (buttonWidth + 4) * 3, btnY).size(buttonWidth, buttonHeight).build();
			buttonTooltips.put(extraBtn, Text.literal("Take the Drone's Vision"));
			addDrawableChild(extraBtn);
		}
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		for (PanelRect p : panels) {
			ctx.fill(p.x, p.y, p.x + p.w, p.y + p.h, 0xFF222233);
			ctx.fill(p.x + 1, p.y + 1, p.x + p.w - 1, p.y + p.h - 1, 0xFF444466);
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFAAAAFF);
		super.render(ctx, mouseX, mouseY, delta);

		for (Map.Entry<ButtonWidget, Text> e : buttonTooltips.entrySet()) {
			if (e.getKey().isMouseOver(mouseX, mouseY)) {
				renderTooltip(ctx, List.of(e.getValue()), mouseX, mouseY);
			}
		}
	}

	private void saveNames() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.world == null) return;

		for (DroneEntry entry : droneEntries) {
			String newName = entry.nameField.getText().trim();
			if (!newName.isEmpty()) {
				DroneControllerItem.setDroneName(controllerStack, entry.droneUuid, newName, client.world);
			}
		}
	}

	private void renderTooltip(DrawContext ctx, List<Text> lines, int mouseX, int mouseY) {
		if (lines == null || lines.isEmpty()) return;
		ctx.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
	}

	private static class DroneEntry {
		final String droneUuid;
		final TextFieldWidget nameField;
		DroneEntry(String uuid, TextFieldWidget field) {
			this.droneUuid = uuid;
			this.nameField = field;
		}
	}

	private static class PanelRect {
		final int x, y, w, h;
		final boolean enabled;
		PanelRect(int x, int y, int w, int h, boolean enabled) {
			this.x = x; this.y = y; this.w = w; this.h = h; this.enabled = enabled;
		}
	}
}
