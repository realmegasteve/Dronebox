package net.Neomoon.dronebox.LUA;

import net.Neomoon.dronebox.Drone;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.Objects;
import java.util.regex.Pattern;

public class LuaIDE extends Screen {

	private final ItemStack pendrive;
	private MinecraftLuaInterpreter lua;
	private Drone drone;



	private String output = "*Empty*";
	private MultilineTextWidget codeOutputLog;
	private boolean closeFr = false;
	private boolean drawConsole = true;
	private boolean saved = true;
	private boolean subScreenOpen = false;

	// Widgets
	private ButtonWidget renameOkButton;
	private LuaTextFieldWidget codeInput;
	private TextFieldWidget renameBox;
	private ButtonWidget simulateCodeButton;
	private ButtonWidget saveCodeButton;
	private ButtonWidget renameButton;
	private ButtonWidget saveAndCloseButton;
	private ButtonWidget closeAndNotSaveButton;
	private ButtonWidget cancelButton;
	private ButtonWidget helpButton;
	private MultilineTextWidget help;
	private ButtonWidget closeHelpButton;








	private String lastText;

	public LuaIDE(Text title, ItemStack pendrive, Drone drone) {
		super(title);
		this.pendrive = pendrive;
		this.lua = new MinecraftLuaInterpreter().init();
		this.drone = drone;
	}

	public void setPythonOutputText(String s) {
		output = s;
	}

	@Override
	protected void init() {
		// Load code from pendrive
		NbtComponent comp = pendrive.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		String loadedCode = root.getString("code", LUADefaults.presetPythonCode.replaceAll(Pattern.quote("\t"), CustomRegexMarkersLUA.tabMarker)
			.replaceAll(Pattern.quote("    "), CustomRegexMarkersLUA.tabMarker)
			.replaceAll(Pattern.quote("\n"), CustomRegexMarkersLUA.returnMarker));
		if (loadedCode.isEmpty()) {
			loadedCode = LUADefaults.presetPythonCode.replaceAll(Pattern.quote("\t"), CustomRegexMarkersLUA.tabMarker)
				.replaceAll(Pattern.quote("    "), CustomRegexMarkersLUA.tabMarker)
				.replaceAll(Pattern.quote("\n"), CustomRegexMarkersLUA.returnMarker);
		}

		// Code editor
		codeInput = new LuaTextFieldWidget(this.textRenderer, 40, 70, 500, 200, Text.of("Python Code"));
		codeInput.setMaxLength(Integer.MAX_VALUE);
		codeInput.setText(loadedCode);

		// Rename dialog
		renameBox = new TextFieldWidget(this.textRenderer, 100, 100, 100, 20, Text.literal("name"));
		renameBox.setVisible(false);
		renameOkButton = ButtonWidget.builder(Text.of("Ok"), (btn) -> {
			pendrive.set(DataComponentTypes.CUSTOM_NAME, Text.literal(renameBox.getText()));
			resetVisibility();
		}).dimensions(210, 100, 20, 20).build();
		renameOkButton.visible = false;

		// Action buttons
		simulateCodeButton = ButtonWidget.builder(Text.of("Run Test"), (btn) -> {
			try {
				drone.loadPythonScript(codeInput.getText().replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), "\t")
					.replaceAll(Pattern.quote(CustomRegexMarkersLUA.returnMarker), "\n"), MinecraftClient.getInstance().player);
			} catch (Exception e) {
				setPythonOutputText(e.getMessage());
			}
		}).dimensions(40, 40, 80, 20).build();

		saveCodeButton = ButtonWidget.builder(Text.of("Save"), (btn) -> saveCode(pendrive, codeInput.getText()))
			.dimensions(130, 40, 60, 20).build();

		renameButton = ButtonWidget.builder(Text.of("Rename"), (btn) -> {
			renameBox.setVisible(true);
			renameOkButton.visible = true;
			btn.setFocused(false);
			renameBox.setFocused(true);
			renameBox.active = true;
			codeInput.setVisible(false);
			btn.visible = false;
			saveCodeButton.visible = false;
			simulateCodeButton.visible = false;
			drawConsole = false;
			subScreenOpen = true;
			closeHelpButton.visible = true;
		}).dimensions(200, 40, 70, 20).build();

		helpButton = ButtonWidget.builder(Text.of("Help"), (btn) -> {
			openHelpDialog();
		}).dimensions(280, 40, 40, 20).build();

		closeHelpButton = ButtonWidget.builder(Text.of("X"), (btn) -> {
			resetVisibility();
		}).dimensions(40, 40, 20, 20).build();

		help = new MultilineTextWidget(80, 60, LUADefaults.guide, textRenderer);
		help.visible = false;

		closeHelpButton.visible = false;
		// Confirm close dialog
		saveAndCloseButton = ButtonWidget.builder(Text.of("Save code and close"), (btn) -> {
			saveCode(pendrive, codeInput.getText());
			this.close();
		}).dimensions(200, 140, 120, 20).build();
		saveAndCloseButton.visible = false;

		closeAndNotSaveButton = ButtonWidget.builder(Text.of("Close without saving"), (btn) -> this.close())
			.dimensions(330, 140, 120, 20).build();
		closeAndNotSaveButton.visible = false;

		cancelButton = ButtonWidget.builder(Text.of("Cancel"), (btn) -> {
			closeFr = false;
			resetVisibility();
		}).dimensions(200, 165, 250, 20).build();
		cancelButton.visible = false;

		// Code output log
		codeOutputLog = new MultilineTextWidget(Text.of(output), this.textRenderer);

		// Add widgets
		addDrawableChild(codeInput);
		addDrawableChild(simulateCodeButton);
		addDrawableChild(saveCodeButton);
		addDrawableChild(renameButton);
		addDrawableChild(codeOutputLog);
		addDrawableChild(renameOkButton);
		addDrawableChild(renameBox);
		addDrawableChild(closeAndNotSaveButton);
		addDrawableChild(saveAndCloseButton);
		addDrawableChild(cancelButton);
		addDrawableChild(help);
		addDrawableChild(closeHelpButton);
		addDrawableChild(helpButton);

		lastText = codeInput.getText();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		codeInput.setWidth(context.getScaledWindowWidth() - 40);

		if (!Objects.equals(lastText, codeInput.getText())) {
			lastText = codeInput.getText();
			saved = false;
		}

		context.drawText(this.textRenderer, "Python IDE", 40, -this.textRenderer.fontHeight, 0xFFFFFFFF, true);

		try {
			codeOutputLog.setMessage(Text.of(output));
		} catch (RuntimeException ignored) {}

		if (drawConsole) {
			MultilineText console = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of(lua.console()));
			console.drawWithShadow(context, 390, 70, 10, Colors.ALTERNATE_WHITE);
		}

		try {
			help.setY((int) Math.round(mouseY * -0.1) + 40);
		} catch (Exception e) {
			// TODO: exception handing
		}
	}

	private void saveCode(ItemStack drive, String code) {
		NbtComponent comp = drive.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		root.put("code", NbtString.of(code));
		drive.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		saved = true;
	}

	private void resetVisibility() {
		saveAndCloseButton.visible = false;
		closeAndNotSaveButton.visible = false;
		codeInput.setVisible(true);
		renameButton.visible = true;
		saveCodeButton.visible = true;
		simulateCodeButton.visible = true;
		renameOkButton.visible = false;
		helpButton.visible = true;
		renameBox.setVisible(false);
		cancelButton.visible = false;
		drawConsole = true;
		help.visible = false;
		closeHelpButton.visible = false;
		subScreenOpen = false;
		closeHelpButton.visible = false;
	}

	private void openCloseDialog() {
		saveAndCloseButton.visible = true;
		closeAndNotSaveButton.visible = true;
		codeInput.setVisible(false);
		renameButton.visible = false;
		saveCodeButton.visible = false;
		simulateCodeButton.visible = false;
		renameOkButton.visible = false;
		renameBox.setVisible(false);
		cancelButton.visible = true;
		drawConsole = false;
	}

	private void openHelpDialog() {
		saveAndCloseButton.visible = false;
		closeAndNotSaveButton.visible = false;
		codeInput.setVisible(false);
		renameButton.visible = false;
		saveCodeButton.visible = false;
		simulateCodeButton.visible = false;
		renameOkButton.visible = false;
		renameBox.setVisible(false);
		cancelButton.visible = false;
		help.visible = true;
		drawConsole = false;
		help.visible = true;
		helpButton.visible = false;
		subScreenOpen = true;
		closeHelpButton.visible = true;
	}



	@Override
	public void close() {
		if (subScreenOpen) {
			resetVisibility();
			subScreenOpen = false;
		} else {
			if (closeFr || saved) {
				MinecraftClient mc = MinecraftClient.getInstance();
				if (mc != null) {
					mc.execute(() -> mc.setScreen(null));
				}
			}
			openCloseDialog();
			closeFr = true;
		}
	}
}
