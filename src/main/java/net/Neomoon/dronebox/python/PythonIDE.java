package net.Neomoon.dronebox.python;

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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("removal")
public class PythonIDE extends Screen {
	public PythonIDE(Text title, ItemStack pendrive) {super(title); this.pendrive = pendrive;}

	String presetPythonCode = """
		import math
		
		# Called once when the script starts
		def setup():
			# Example: initialize variables
			global counter
			counter = 0
		
		
		# Called every game tick (pass in time in seconds or ticks)
		def tick():
			#Example: fly in circles
			global counter
			counter = counter + 1
			time = counter / 20
			drone.setVelocity(math.sin(time) * 0.1, 0, math.cos(time) * 0.1)
		""";

	ItemStack pendrive;
	String output = "*Empty*";
	MinecraftPythonInterpreter python = new MinecraftPythonInterpreter().init();
	MultilineTextWidget codeOutputLog;
	boolean closeFr = false;
	boolean drawConsole = true;
	boolean saved = true;

	//Widgets
	ButtonWidget renameOkButton;
	PythonTextFieldWidget codeInput;
	TextFieldWidget renameBox;
	ButtonWidget simulateCodeButton;
	ButtonWidget saveCodeButton;
	ButtonWidget renameButton;
	ButtonWidget saveAndCloseButton;
	ButtonWidget closeAndNotSaveButton;
	ButtonWidget cancelButton;


	public void setPythonOutputText(String s){
		output = s;
	}

	@Override
	protected void init() {
		//Saved code retrieving from NBT <================================================================================================================================================================================================================================================================================
		NbtComponent comp2 = pendrive.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root2 = comp2.copyNbt();
		String loadedCode = root2.getString("code", presetPythonCode.replaceAll(Pattern.quote("\t"), CustomRegexMarkersPython.tabMarker).replaceAll(Pattern.quote( "    "), CustomRegexMarkersPython.tabMarker).replaceAll(Pattern.quote("\n"), CustomRegexMarkersPython.returnMarker));
		if (loadedCode.length() == 0){loadedCode = presetPythonCode.replaceAll(Pattern.quote("\t"), CustomRegexMarkersPython.tabMarker).replaceAll(Pattern.quote( "    "), CustomRegexMarkersPython.tabMarker).replaceAll(Pattern.quote("\n"), CustomRegexMarkersPython.returnMarker);}





		//Setting up widgets <================================================================================================================================================================================================================================================================================
		codeInput = new PythonTextFieldWidget(
			this.textRenderer, 40, 70, 340, 200, Text.of("Python Code")
		);
		codeInput.setMaxLength(Integer.MAX_VALUE);
		codeInput.setText(loadedCode); //load code into editor




		//Rename dialog widgets <================================================================================================================================================================================================================================================================================
		renameBox = new TextFieldWidget(this.getTextRenderer(),100, 100, 100, 20, Text.literal("name"));
		renameBox.setVisible(false);
		renameOkButton = ButtonWidget.builder(Text.of("Ok"), (btn) -> {
			pendrive.set(DataComponentTypes.CUSTOM_NAME, Text.literal(renameBox.getText()));
			resetVisibility();
		}).dimensions(210, 100, 20, 20).build();
		renameOkButton.visible = false;





		//Action bar widgets <================================================================================================================================================================================================================================================================================
		simulateCodeButton = ButtonWidget.builder(Text.of("Run Test"), (btn) -> {
			try {
				python.run(codeInput.getText().replaceAll(Pattern.quote(CustomRegexMarkersPython.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersPython.returnMarker), "\n"));
			} catch (Exception e) {
				setPythonOutputText(e.getMessage());
			}
		}).dimensions(40, 40, 80, 20).build();

		saveCodeButton = ButtonWidget.builder(Text.of("Save"), (btn) -> {
			saveCode(pendrive, codeInput.getText());
		}).dimensions(130, 40, 60, 20).build();

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
		}).dimensions(200, 40, 70, 20).build();



		//Confirm close screen widgets <================================================================================================================================================================================================================================================================================
		saveAndCloseButton = ButtonWidget.builder(Text.of("Save code and close"), (btn) -> {
			saveCode(pendrive, codeInput.getText());
			this.close();
		}).dimensions(200, 140, 120, 20).build();
		saveAndCloseButton.visible = false;


		closeAndNotSaveButton = ButtonWidget.builder(Text.of("Close without saving"), (btn) -> {
			this.close();
		}).dimensions(330, 140, 120, 20).build();
		closeAndNotSaveButton.visible = false;

		cancelButton = ButtonWidget.builder(Text.of("Cancel"), (btn) -> {
			closeFr = false;
			resetVisibility();
		}).dimensions(200, 165, 250, 20).build();
		cancelButton.visible = false;


		//Code error log <================================================================================================================================================================================================================================================================================
		codeOutputLog = new MultilineTextWidget(
			Text.of(output),
			this.getTextRenderer()
		);





		//Adding all widgets <================================================================================================================================================================================================================================================================================
		this.addDrawableChild(codeInput);
		this.addDrawableChild(simulateCodeButton);
		this.addDrawableChild(saveCodeButton);
		this.addDrawableChild(renameButton);
		this.addDrawableChild(codeOutputLog);
		this.addDrawableChild(renameOkButton);
		this.addDrawableChild(renameBox);
		this.addDrawableChild(closeAndNotSaveButton);
		this.addDrawableChild(saveAndCloseButton);
		this.addDrawableChild(cancelButton);

		lastText = codeInput.getText();
	}

	String lastText;

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		if (!Objects.equals(lastText, codeInput.getText())) {
			lastText = codeInput.getText();
			saved = false;
		}
		context.drawText(this.textRenderer, "Python IDE", 40, 10 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);

		codeOutputLog.setMessage(Text.of(output));

		MultilineText console = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of(python.console()));

		if (drawConsole) {
			console.drawWithShadow(context, 390, 70, 10, Colors.ALTERNATE_WHITE);
		}
	}


	private void saveCode(ItemStack drive, String code){
		//Save code nbt shenanigans
		NbtComponent comp = drive.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		NbtElement code2 = NbtString.of(code);
		root.put("code", code2);

		drive.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		saved = true;
	}

	private void openCloseDialog(){
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

	private void resetVisibility(){
		saveAndCloseButton.visible = false;
		closeAndNotSaveButton.visible = false;
		codeInput.setVisible(true);
		renameButton.visible = true;
		saveCodeButton.visible = true;
		simulateCodeButton.visible = true;
		renameOkButton.visible = false;
		renameBox.setVisible(false);
		cancelButton.visible = false;
		drawConsole = true;
	}

	@Override
	public void close() {
		if (closeFr || saved) {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc != null) {
				mc.execute(() -> {
					try {
						mc.setScreen(null);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				});
			}
			}
		openCloseDialog();
		closeFr = true;
	}
}
