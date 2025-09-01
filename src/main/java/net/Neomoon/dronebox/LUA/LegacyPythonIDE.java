package net.Neomoon.dronebox.LUA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.regex.Pattern;

public class LegacyPythonIDE extends Screen {
	public LegacyPythonIDE(Text title, ItemStack pendrive) {super(title); this.pendrive = pendrive;}


	ItemStack pendrive;
	String output = "*Empty*";
	MinecraftLuaInterpreter python = new MinecraftLuaInterpreter().init();
	MultilineTextWidget codeOutputLog;

	public void setPythonOutputText(String s){
		output = s;
	}

	@Override
	protected void init() {

		LuaTextFieldWidget codeInput = new LuaTextFieldWidget(
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
				python.run(codeInput.getText().replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersLUA.returnMarker), "\n"));
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

		context.drawText(this.textRenderer, "Python IDE", 40, -this.textRenderer.fontHeight, 0xFFFFFFFF, true);

		codeOutputLog.setMessage(Text.of(output));

		MultilineText console = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of(python.console()));

		console.drawWithShadow(context, 250,70, 10, Colors.ALTERNATE_WHITE);
	}
}
