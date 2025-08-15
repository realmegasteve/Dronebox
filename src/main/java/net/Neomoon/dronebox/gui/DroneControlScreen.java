package net.Neomoon.dronebox.gui;

import net.Neomoon.dronebox.items.DroneControllerItem;
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

	private static final int ENTRY_HEIGHT = 30;
	private static final int ENTRY_SPACING = 8;
	private static final int BUTTON_WIDTH = 75;
	private static final int BUTTON_HEIGHT = 20;
	private static final int PANEL_PADDING = 6;
	private static final int NAME_FIELD_WIDTH = 140;
	private static final int MAX_COLUMNS = 2;

	public DroneControlScreen(ItemStack controller) {
		super(Text.literal("Drone Control"));
		this.controllerStack = controller;
	}

	@Override
	protected void init() {
		super.init();
		droneEntries.clear();
		panels.clear();
		buttonTooltips.clear();

		int centerX = this.width / 2;
		int startY = 40;

		List<String> linked = DroneControllerItem.getLinkedDroneUUIDs(controllerStack);

		if (linked.isEmpty()) {
			addDrawableChild(ButtonWidget.builder(
				Text.literal("No linked drones").formatted(Formatting.GRAY),
				b -> close()
			).position(centerX - 100, startY).size(200, BUTTON_HEIGHT).build());
		} else {
			int displayed = 0;
			int row = 0;

			for (String uuidStr : linked) {
				if (displayed >= linked.size()) break;

				final String droneUuid = uuidStr;
				boolean isEnabled = DroneControllerItem.isDroneEnabled(controllerStack, droneUuid);

				int col = displayed % MAX_COLUMNS;
				if (col == 0 && displayed > 0) row++;

				int xOffset = centerX - 160 + col * 180;
				int yOffset = startY + row * (ENTRY_HEIGHT + ENTRY_SPACING);

				panels.add(new PanelRect(
					xOffset - PANEL_PADDING / 2,
					yOffset - PANEL_PADDING / 2,
					170,
					ENTRY_HEIGHT + PANEL_PADDING,
					isEnabled
				));

				TextFieldWidget nameField = new TextFieldWidget(
					this.textRenderer,
					xOffset,
					yOffset,
					NAME_FIELD_WIDTH,
					BUTTON_HEIGHT,
					Text.literal("Drone Name")
				);
				nameField.setText(DroneControllerItem.getDroneDisplayName(controllerStack, droneUuid).getString());
				nameField.setEditable(isEnabled);
				addSelectableChild(nameField);

				droneEntries.add(new DroneEntry(droneUuid, nameField));

				ButtonWidget controlBtn = ButtonWidget.builder(
					Text.literal("Control: " + (DroneControllerItem.isDroneControlEnabled(controllerStack, droneUuid) ? "ON" : "OFF")),
					btn -> {
						boolean newState = !DroneControllerItem.isDroneControlEnabled(controllerStack, droneUuid);
						DroneControllerItem.setDroneControlEnabled(controllerStack, droneUuid, newState);
						btn.setMessage(Text.literal("Control: " + (newState ? "ON" : "OFF")));
					}
				).position(xOffset - (BUTTON_WIDTH + 4), yOffset + BUTTON_HEIGHT + 2).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();

				buttonTooltips.put(controlBtn, Text.literal("Toggle control state"));
				addDrawableChild(controlBtn);

				ButtonWidget cameraBtn = ButtonWidget.builder(
					Text.literal("Camera: " + (DroneControllerItem.isDroneCameraEnabled(controllerStack, droneUuid) ? "ON" : "OFF")),
					btn -> {
						boolean newState = !DroneControllerItem.isDroneCameraEnabled(controllerStack, droneUuid);
						DroneControllerItem.setDroneCameraEnabled(controllerStack, droneUuid, newState);
						btn.setMessage(Text.literal("Camera: " + (newState ? "ON" : "OFF")));
					}
				).position(xOffset + BUTTON_WIDTH + 4, yOffset + BUTTON_HEIGHT + 2).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();

				buttonTooltips.put(cameraBtn, Text.literal("Toggle camera feed"));
				addDrawableChild(cameraBtn);

				displayed++;
			}
		}

		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
			saveNames();
			close();
		}).position(this.width / 2 - 100, this.height - 35).size(200, 25).build());
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		for (PanelRect p : panels) {
			ctx.fill(p.x, p.y, p.x + p.w, p.y + p.h, 0xFF222233);
			ctx.fill(p.x + 1, p.y + 1, p.x + p.w - 1, p.y + p.h - 1, 0xFF444466);
			if (!p.enabled) {
				ctx.fill(p.x + 2, p.y + 2, p.x + p.w - 2, p.y + p.h - 2, 0x88000000);
			}
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
