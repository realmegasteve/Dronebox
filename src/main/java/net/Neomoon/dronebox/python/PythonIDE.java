package net.Neomoon.dronebox.python;

import com.mojang.datafixers.kinds.IdF;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.regex.Pattern;

public class PythonIDE extends Screen {
	public PythonIDE(Text title) {
		super(title);
	}

	String output = "*Empty*";
	MinecraftPythonInterpreter python = new MinecraftPythonInterpreter().init();
	MultilineTextWidget codeOutputLog;

	public void setPythonOutputText(String s){
		output = s;
	}

	@Override
	protected void init() {

		PythonTextFieldWidget codeInput = new PythonTextFieldWidget(
			this.textRenderer, 40, 70, 200, 200, Text.of("Python Code")
		);

		codeInput.setMaxLength(999999);

		MultilineTextWidget codeInput2= new MultilineTextWidget(
			Text.of("""
        Test!!
        This seems to be working.
    """),
			this.getTextRenderer()
		);

		this.addDrawableChild(codeInput);


		ButtonWidget buttonWidget = ButtonWidget.builder(Text.of("Run code"), (btn) -> {
			try {
				python.run(codeInput.getText().replaceAll(Pattern.quote(CustomRegexMarkersPython.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersPython.returnMarker), "\n"));
			} catch (Exception e) {
				setPythonOutputText(e.getMessage());
			}
			}).dimensions(40, 40, 120, 20).build();

		this.addDrawableChild(buttonWidget);

		codeOutputLog = new MultilineTextWidget(
			Text.of(output),
			this.getTextRenderer()
		);
		this.addDrawableChild(codeOutputLog);

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		// Minecraft doesn't have a "label" widget, so we'll have to draw our own text.
		// We'll subtract the font height from the Y position to make the text appear above the button.
		// Subtracting an extra 10 pixels will give the text some padding.
		// textRenderer, text, x, y, color, hasShadow
		context.drawText(this.textRenderer, "Python IDE", 40, 10 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);

		codeOutputLog.setMessage(Text.of(output));

		MultilineText console = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of(python.console()));

		console.drawWithShadow(context, 250,70, 10, Colors.ALTERNATE_WHITE);
	}
}
