package net.Neomoon.dronebox.python;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.python.jline.internal.Nullable;

@Environment(EnvType.CLIENT)
public class PythonTextFieldWidget extends ClickableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted"));
    public static final int field_32194 = -1;
    public static final int field_32195 = 1;
    private static final int field_32197 = 1;
    private static final int VERTICAL_CURSOR_COLOR = -3092272;
    private static final String HORIZONTAL_CURSOR = "_";
    public static final int DEFAULT_EDITABLE_COLOR = -2039584;
    private static final int field_45354 = 300;
    private final TextRenderer textRenderer;
    private String text;
    private int maxLength;
    private boolean drawsBackground;
    private boolean focusUnlocked;
    private boolean editable;
    private boolean centered;
    private boolean textShadow;
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private int editableColor;
    private int uneditableColor;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    private BiFunction<String, Integer, OrderedText> renderTextProvider;
    @Nullable
    private Text placeholder;
    private long lastSwitchFocusTime;
    private int textX;
    private int textY;

    public PythonTextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        this(textRenderer, 0, 0, width, height, text);
    }

    public PythonTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        this(textRenderer, x, y, width, height, (PythonTextFieldWidget)null, text);
    }

    public PythonTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable PythonTextFieldWidget copyFrom, Text text) {
        super(x, y, width, height, text);
        this.text = "";
        this.maxLength = 32;
        this.drawsBackground = true;
        this.focusUnlocked = true;
        this.editable = true;
        this.centered = false;
        this.textShadow = true;
        this.editableColor = -2039584;
        this.uneditableColor = -9408400;
        this.textPredicate = Objects::nonNull;
        this.renderTextProvider = (string, firstCharacterIndex) -> OrderedText.styledForwardsVisitedString(string, Style.EMPTY);
        this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
        this.textRenderer = textRenderer;
        if (copyFrom != null) {
            this.setText(copyFrom.getText());
        }

        this.updateTextPosition();
    }

    public void setChangedListener(Consumer<String> changedListener) {
        this.changedListener = changedListener;
    }

    public void setRenderTextProvider(BiFunction<String, Integer, OrderedText> renderTextProvider) {
        this.renderTextProvider = renderTextProvider;
    }

    protected MutableText getNarrationMessage() {
        Text text = this.getMessage();
        return Text.translatable("gui.narrate.editBox", new Object[]{text, this.text});
    }

    public void setText(String text) {
        if (this.textPredicate.test(text)) {
            if (text.length() > this.maxLength) {
                this.text = text.substring(0, this.maxLength);
            } else {
                this.text = text;
            }

            this.setCursorToEnd(false);
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(text);
        }
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setX(int x) {
        super.setX(x);
        this.updateTextPosition();
    }

    public void setY(int y) {
        super.setY(y);
        this.updateTextPosition();
    }

    public void setTextPredicate(Predicate<String> textPredicate) {
        this.textPredicate = textPredicate;
    }

    public void write(String text) {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        if (k > 0) {
            String string = StringHelper.stripInvalidChars(text);
            int l = string.length();
            if (k < l) {
                if (Character.isHighSurrogate(string.charAt(k - 1))) {
                    --k;
                }

                string = string.substring(0, k);
                l = k;
            }

            String string2 = (new StringBuilder(this.text)).replace(i, j, string).toString();
            if (this.textPredicate.test(string2)) {
                this.text = string2;
                this.setSelectionStart(i + l);
                this.setSelectionEnd(this.selectionStart);
                this.onChanged(this.text);
            }
        }
    }

    private void onChanged(String newText) {
        if (this.changedListener != null) {
            this.changedListener.accept(newText);
        }

        this.updateTextPosition();
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }

    }

    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharactersTo(this.getWordSkipPosition(wordOffset));
            }
        }
    }

    public void eraseCharacters(int characterOffset) {
        this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset));
    }

    public void eraseCharactersTo(int position) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                int i = Math.min(position, this.selectionStart);
                int j = Math.max(position, this.selectionStart);
                if (i != j) {
                    String string = (new StringBuilder(this.text)).delete(i, j).toString();
                    if (this.textPredicate.test(string)) {
                        this.text = string;
                        this.setCursor(i, false);
                    }
                }
            }
        }
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);

        for(int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int offset, boolean shiftKeyPressed) {
        this.setCursor(this.getCursorPosWithOffset(offset), shiftKeyPressed);
    }

    private int getCursorPosWithOffset(int offset) {
        return Util.moveCursor(this.text, this.selectionStart, offset);
    }

    public void setCursor(int cursor, boolean shiftKeyPressed) {
        this.setSelectionStart(cursor);
        if (!shiftKeyPressed) {
            this.setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionStart);
    }

    public void setCursorToStart(boolean shiftKeyPressed) {
        this.setCursor(0, shiftKeyPressed);
    }

    public void setCursorToEnd(boolean shiftKeyPressed) {
        this.setCursor(this.text.length(), shiftKeyPressed);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isNarratable() && this.isFocused()) {
            switch (keyCode) {
				case 258: //tab
					if (this.editable) {
						System.out.println(CustomRegexMarkersPython.tabMarker);
						this.write(CustomRegexMarkersPython.tabMarker); ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					}
					return true;
				case 257: //enter
					if (this.editable) {
						System.out.println("ENTER");
						this.write(CustomRegexMarkersPython.returnMarker);
					}
					return true;
                case 259: //backspace
                    if (this.editable) {
                        this.erase(-1);
                    }

                    return true;
                case 260:
                case 264:
                case 265:
                case 266:
                case 267:
                default:
                    if (Screen.isSelectAll(keyCode)) {
                        this.setCursorToEnd(false);
                        this.setSelectionEnd(0);
                        return true;
                    } else if (Screen.isCopy(keyCode)) {
                        MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText().replaceAll(Pattern.quote(CustomRegexMarkersPython.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersPython.returnMarker), "\n"));
                        return true;
                    } else if (Screen.isPaste(keyCode)) {
                        if (this.isEditable()) {
                            this.write(MinecraftClient.getInstance().keyboard.getClipboard().replaceAll(Pattern.quote("\t"), CustomRegexMarkersPython.tabMarker).replaceAll(Pattern.quote( "    "), CustomRegexMarkersPython.tabMarker).replaceAll(Pattern.quote("\n"), CustomRegexMarkersPython.returnMarker));
                        }

                        return true;
                    } else {
                        if (Screen.isCut(keyCode)) {
                            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                            if (this.isEditable()) {
                                this.write("");
                            }

                            return true;
                        }

                        return false;
                    }
                case 261:
                    if (this.editable) { //backspace
                        this.erase(1);
                    }

                    return true;
                case 262: //right arrow
                    if (Screen.hasControlDown()) {
                        this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(1, Screen.hasShiftDown());
                    }

                    return true;
                case 263: //left arrow
                    if (Screen.hasControlDown()) {
                        this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(-1, Screen.hasShiftDown());
                    }

                    return true;
                case 268:
                    this.setCursorToStart(Screen.hasShiftDown());
                    return true;
                case 269:
                    this.setCursorToEnd(Screen.hasShiftDown());
                    return true;

            }
        } else {
            return false;
        }
    }

    public boolean isActive() {
        return this.isNarratable() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!this.isActive()) {
            return false;
        } else if (StringHelper.isValidChar(chr)) {
            if (this.editable) {
                this.write(Character.toString(chr));
            }

            return true;
        } else {
            return false;
        }
    }

    public void onClick(double mouseX, double mouseY) {
        int i = MathHelper.floor(mouseX) - this.textX;
        String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        this.setCursor(this.textRenderer.trimToWidth(string, i).length() + this.firstCharacterIndex, Screen.hasShiftDown());
    }

    public void playDownSound(SoundManager soundManager) {
    }

	public int getMaxWidth(){
		return this.width - 6;
	}
	private int getTextWidth(String txt) {
		return this.textRenderer.getWidth(txt);
	}

	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (this.isVisible()) {
			//System.out.println("Init: " + selectionStart + ", end: "+ selectionEnd);
			if (this.drawsBackground()) {
				Identifier identifier = TEXTURES.get(this.isNarratable(), this.isFocused());
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.getWidth(), this.getHeight());
			}

			int lineY = this.getY() + 6;
			int maxWidth = getMaxWidth();
			int lineHeight = 10;


			int drawingPenIndex = 0;

			List<String> lines = this.textRenderer.wrapLines(Text.of(this.text), maxWidth).stream()
				.map(OrderedText::toString)
				.toList();

			int highlightLineY = lineY;

			for (String line : lines) {
				int lineLength = line.length();

				int lineStart = drawingPenIndex;
				int lineEnd = drawingPenIndex + lineLength;

				int selStart = Math.max(selectionStart, lineStart);
				int selEnd = Math.min(selectionEnd, lineEnd);

				if (selectionStart != selectionEnd) {
					//draw selection
					if (selStart < selEnd) {
						int startOffset = selStart - lineStart;
						int endOffset = selEnd - lineStart;

						int xStart = this.textX + getTextWidth(line.substring(0, startOffset));
						int xEnd = this.textX +getTextWidth(line.substring(0, endOffset));

						context.fill(xStart, highlightLineY - 1, xEnd, highlightLineY + 9, 0x8855AAFF);
					}
				}

				if (lineStart < getCursor() && lineEnd > getCursor()){
					//draw cursor
					int cursorX = this.textX + getTextWidth(line.substring(0, getCursor() - lineStart));
					context.fill(cursorX -1, highlightLineY - 1, cursorX + 1, highlightLineY + 9, Colors.WHITE);
				}

				drawingPenIndex += lineLength + 1; // +1 for the newline character
				highlightLineY += 11; // line height
			}



			int textColor = this.editable ? this.editableColor : this.uneditableColor;

			LazuliMultilineText multilineText = LazuliMultilineText.create(this.textRenderer, Text.of(this.getText()), 999999999);
			multilineText.drawWithShadow(context, this.textX, lineY, lineHeight, textColor);


			if (this.placeholder != null && this.text.isEmpty() && !this.isFocused()) {
				multilineText.drawWithShadow(context, this.textX, lineY, lineHeight, textColor);
			}
		}
	}


	private void updateTextPosition() {
        if (this.textRenderer != null) {
            String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
            this.textX = this.getX() + (this.isCentered() ? (this.getWidth() - this.textRenderer.getWidth(string)) / 2 : (this.drawsBackground ? 4 : 0));
            this.textY = this.drawsBackground ? this.getY() + (this.height - 8) / 2 : this.getY();
        }
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
            this.onChanged(this.text);
        }

    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursor() {
        return this.selectionStart;
    }

    public boolean drawsBackground() {
        return this.drawsBackground;
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
        this.updateTextPosition();
    }

    public void setEditableColor(int editableColor) {
        this.editableColor = editableColor;
    }

    public void setUneditableColor(int uneditableColor) {
        this.uneditableColor = uneditableColor;
    }

    public void setFocused(boolean focused) {
        if (this.focusUnlocked || focused) {
            super.setFocused(focused);
            if (focused) {
                this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
            }

        }
    }

    private boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    private boolean isCentered() {
        return this.centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
        this.updateTextPosition();
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public int getInnerWidth() {
        return this.drawsBackground() ? this.width - 8 : this.width;
    }

    public void setSelectionEnd(int index) {
        this.selectionEnd = MathHelper.clamp(index, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionEnd);
    }

    private void updateFirstCharacterIndex(int cursor) {
        if (this.textRenderer != null) {
            this.firstCharacterIndex = Math.min(this.firstCharacterIndex, this.text.length());
            int i = this.getInnerWidth();
            String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), i);
            int j = string.length() + this.firstCharacterIndex;
            if (cursor == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, i, true).length();
            }

            if (cursor > j) {
                this.firstCharacterIndex += cursor - j;
            } else if (cursor <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - cursor;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, this.text.length());
        }
    }

    public void setFocusUnlocked(boolean focusUnlocked) {
        this.focusUnlocked = focusUnlocked;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getCharacterX(int index) {
        return index > this.text.length() ? this.getX() : this.getX() + this.textRenderer.getWidth(this.text.substring(0, index));
    }

    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
    }

    public void setPlaceholder(Text placeholder) {
        this.placeholder = placeholder;
    }
}
