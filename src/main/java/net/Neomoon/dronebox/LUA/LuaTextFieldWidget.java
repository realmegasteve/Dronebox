package net.Neomoon.dronebox.LUA;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

/**
 * PythonTextFieldWidget — multiline + pixel-smooth scrolling + per-string coloring + improved cursor column handling
 */
@Environment(EnvType.CLIENT)
public class LuaTextFieldWidget extends ClickableWidget {
	private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted"));
	private static final int VERTICAL_CURSOR_COLOR = -3092272;

	private final TextRenderer textRenderer;
	private String text;
	private int maxLength;
	private boolean drawsBackground;
	private boolean focusUnlocked;
	private boolean editable;
	private boolean centered;
	private boolean textShadow;
	private int firstCharacterIndex;
	private int firstLogicalIndex;
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

	private int[] origToLogical;
	private List<Integer> logicalToOrig;
	private String logicalText;

	private List<VisualSegment> allSegmentsCache;
	private double firstVisibleScrollY = 0.0;
	private int maxVisibleLines = 0;
	private final int lineHeight = 10;
	private final int lineSpacing = this.lineHeight + 1;

	private static final int SCROLLBAR_WIDTH = 8;
	private static final int SCROLLBAR_PADDING = 4;
	private boolean isDraggingScrollbar = false;
	private double dragMouseY = 0.0;
	private double dragStartScrollY = 0.0;

	private int desiredCursorX = -1;

	private static final Pattern TOKEN_PATTERN = LUADefaults.syntaxPattern;


	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		firstVisibleScrollY -= (int) (verticalAmount * 20);
		firstVisibleScrollY = Math.max(0, firstVisibleScrollY);
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	public LuaTextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
		this(textRenderer, 0, 0, width, height, null, text);
	}

	public LuaTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		this(textRenderer, x, y, width, height, (LuaTextFieldWidget) null, text);
	}

	public LuaTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable LuaTextFieldWidget copyFrom, Text text) {
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
		this.firstCharacterIndex = 0;
		this.firstLogicalIndex = 0;
		this.allSegmentsCache = new ArrayList<>();

		if (copyFrom != null) {
			this.setText(copyFrom.getText());
		}

		this.buildLogicalMappings();
		this.updateTextPosition();
	}



	public void setChangedListener(Consumer<String> changedListener) { this.changedListener = changedListener; }

	public void setRenderTextProvider(BiFunction<String, Integer, OrderedText> renderTextProvider) { this.renderTextProvider = renderTextProvider; }
	protected MutableText getNarrationMessage() {
		Text t = this.getMessage();
		return Text.translatable("gui.narrate.editBox", t, this.text);
	}

	public void setText(String text) {
		if (this.textPredicate.test(text)) {
			if (text.length() > this.maxLength) this.text = text.substring(0, this.maxLength); else this.text = text;
			this.buildLogicalMappings();
			this.setCursorToEnd(false);
			this.setSelectionEnd(this.selectionStart);
			this.onChanged(text);
		}
	}

	public String getText() { return this.text; }

	public String getSelectedText() {
		int i = Math.min(this.selectionStart, this.selectionEnd);
		int j = Math.max(this.selectionStart, this.selectionEnd);
		return this.text.substring(i, j);
	}

	public void setX(int x) { super.setX(x); this.updateTextPosition(); }
	public void setY(int y) { super.setY(y); this.updateTextPosition(); }
	public void setTextPredicate(Predicate<String> textPredicate) { this.textPredicate = textPredicate; }



	public void write(String text) {
		int i = Math.min(this.selectionStart, this.selectionEnd);
		int j = Math.max(this.selectionStart, this.selectionEnd);
		int k = this.maxLength - this.text.length() - (i - j);
		if (k > 0) {
			String string = StringHelper.stripInvalidChars(text);
			int l = string.length();
			if (k < l) {
				if (Character.isHighSurrogate(string.charAt(k - 1))) --k;
				string = string.substring(0, k);
				l = k;
			}

			String newText = (new StringBuilder(this.text)).replace(i, j, string).toString();
			if (this.textPredicate.test(newText)) {
				this.text = newText;
				if (string.contains(CustomRegexMarkersLUA.returnMarker)) {
					this.desiredCursorX = 0;
				}
				this.buildLogicalMappings();
				this.setSelectionStart(i + l);
				this.setSelectionEnd(this.selectionStart);
				this.onChanged(this.text);
			}
		}
	}

	private void onChanged(String newText) {
		if (this.changedListener != null) this.changedListener.accept(newText);
		this.buildLogicalMappings();
		this.updateTextPosition();
	}



	private void erase(int offset) {
		if (Screen.hasControlDown()) this.eraseWords(offset); else this.eraseCharacters(offset);
	}

	public void eraseWords(int wordOffset) {
		if (!this.text.isEmpty()) {
			if (this.selectionEnd != this.selectionStart) this.write("");
			else this.eraseCharactersTo(this.getWordSkipPosition(wordOffset));
		}
	}

	public void eraseCharacters(int characterOffset) { this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset)); }

	public void eraseCharactersTo(int position) {
		if (!this.text.isEmpty()) {
			if (this.selectionEnd != this.selectionStart) this.write("");
			else {
				int i = Math.min(position, this.selectionStart);
				int j = Math.max(position, this.selectionStart);
				if (i != j) {
					String s = (new StringBuilder(this.text)).delete(i, j).toString();
					if (this.textPredicate.test(s)) {
						this.text = s;
						this.buildLogicalMappings();
						this.setCursor(i, false);
					}
				}
			}
		}
	}

	public int getWordSkipPosition(int wordOffset) { return this.getWordSkipPosition(wordOffset, this.getCursor()); }
	private int getWordSkipPosition(int wordOffset, int cursorPosition) { return this.getWordSkipPosition(wordOffset, cursorPosition, true); }
	private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
		int i = cursorPosition;
		boolean back = wordOffset < 0;
		int times = Math.abs(wordOffset);
		for (int k = 0; k < times; ++k) {
			if (!back) {
				int l = this.text.length();
				i = this.text.indexOf(32, i);
				if (i == -1) i = l; else {
					while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') ++i;
				}
			} else {
				while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') --i;
				while (i > 0 && this.text.charAt(i - 1) != ' ') --i;
			}
		}
		return i;
	}

	public void moveCursor(int offset, boolean shiftKeyPressed) { this.setCursor(this.getCursorPosWithOffset(offset), shiftKeyPressed); }
	private int getCursorPosWithOffset(int offset) { return Util.moveCursor(this.text, this.selectionStart, offset); }

	public void setCursor(int cursor, boolean shiftKeyPressed) {
		this.setSelectionStart(cursor);
		if (!shiftKeyPressed) this.setSelectionEnd(this.selectionStart);
		this.updateTextPosition();
		this.onChanged(this.text);
		this.ensureCursorVisible();
	}

	public void setSelectionStart(int cursor) {
		this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
		this.updateFirstCharacterIndex(this.selectionStart);
		this.ensureCursorVisible();
	}

	public void setCursorToStart(boolean shiftKeyPressed) { this.setCursor(0, shiftKeyPressed); }
	public void setCursorToEnd(boolean shiftKeyPressed) { this.setCursor(this.text.length(), shiftKeyPressed); }



	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!this.isNarratable() || !this.isFocused()) return false;
		switch (keyCode) {
			case 258:
				if (this.editable) this.write(CustomRegexMarkersLUA.tabMarker);
				return true;
			case 257:
				if (this.editable) {
					this.write(CustomRegexMarkersLUA.returnMarker);
					this.desiredCursorX = 0;
					this.ensureCursorVisible();
				}
				return true;
			case 259:
				if (this.editable) this.erase(-1);
				return true;
			case 261:
				if (this.editable) this.erase(1);
				return true;
			case 262:
				if (Screen.hasControlDown()) this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown());
				else this.moveCursor(1, Screen.hasShiftDown());

				this.updateDesiredCursorXFromCursor();
				return true;
			case 263:
				if (Screen.hasControlDown()) this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown());
				else this.moveCursor(-1, Screen.hasShiftDown());
				this.updateDesiredCursorXFromCursor();
				return true;
			case 265:
				if (Screen.hasControlDown()) this.setCursorToStart(Screen.hasShiftDown());
				else this.moveCursorUp(Screen.hasShiftDown());
				return true;
			case 264:
				if (Screen.hasControlDown()) this.setCursorToEnd(Screen.hasShiftDown());
				else this.moveCursorDown(Screen.hasShiftDown());
				return true;
			case 268:
				this.setCursorToStart(Screen.hasShiftDown());
				return true;
			case 269:
				this.setCursorToEnd(Screen.hasShiftDown());
				return true;
			default:
				if (Screen.isSelectAll(keyCode)) { this.setCursorToEnd(false); this.setSelectionEnd(0); return true; }
				if (Screen.isCopy(keyCode)) { MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText().replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersLUA.returnMarker), "\n")); return true; }
				if (Screen.isPaste(keyCode)) {
					if (this.isEditable()) {
						this.write(MinecraftClient.getInstance().keyboard.getClipboard().replaceAll(Pattern.quote("\t"), CustomRegexMarkersLUA.tabMarker).replaceAll(Pattern.quote("    "), CustomRegexMarkersLUA.tabMarker).replaceAll(Pattern.quote("\n"), CustomRegexMarkersLUA.returnMarker));
					}
					return true;
				}
				if (Screen.isCut(keyCode)) {
					MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
					if (this.isEditable()) this.write("");
					return true;
				}
				return false;
		}
	}

	public boolean isActive() { return this.isNarratable() && this.isFocused() && this.isEditable(); }

	public boolean charTyped(char chr, int modifiers) {
		if (!this.isActive()) return false;
		if (StringHelper.isValidChar(chr)) {
			if (this.editable) this.write(Character.toString(chr));

			if (this.desiredCursorX < 0) this.updateDesiredCursorXFromCursor();
			return true;
		}
		return false;
	}


	public void onClick(double mouseX, double mouseY) {

		int trackX = this.getX() + this.getWidth() - SCROLLBAR_PADDING - SCROLLBAR_WIDTH;
		int trackY = this.getY() + 6;
		int trackH = this.getInnerHeight();
		if (mouseX >= trackX && mouseX <= trackX + SCROLLBAR_WIDTH && mouseY >= trackY && mouseY <= trackY + trackH) {
			if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
			int totalLines = this.allSegmentsCache.size();
			if (totalLines <= this.maxVisibleLines) return;
			int thumbMin = 8;
			int thumbH = Math.max(thumbMin, (int)((float)this.maxVisibleLines / (float)totalLines * trackH));
			int maxPosRange = trackH - thumbH;
			double scrollRange = Math.max(0.0, (double)(totalLines * this.lineSpacing - this.maxVisibleLines * this.lineSpacing));
			int thumbY = trackY + (int)((this.firstVisibleScrollY / Math.max(1.0, scrollRange)) * (double)maxPosRange);
			if (mouseY < thumbY) {

				this.firstVisibleScrollY = Math.max(0.0, this.firstVisibleScrollY - (double)this.maxVisibleLines * this.lineSpacing);
			} else if (mouseY > thumbY + thumbH) {

				this.firstVisibleScrollY = Math.min(scrollRange, this.firstVisibleScrollY + (double)this.maxVisibleLines * this.lineSpacing);
			} else {

				this.isDraggingScrollbar = true;
				this.dragMouseY = mouseY;
				this.dragStartScrollY = this.firstVisibleScrollY;
			}

			this.clampScrollToBounds();
			return;
		}


		int lineY = this.getY() + 6;
		double scrolledY = this.firstVisibleScrollY;
		int clickedIndex = (int)Math.floor((mouseY - lineY + scrolledY) / (double)this.lineSpacing);
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		if (clickedIndex < 0) clickedIndex = 0;
		if (clickedIndex >= this.allSegmentsCache.size()) clickedIndex = this.allSegmentsCache.size() - 1;
		VisualSegment seg = this.allSegmentsCache.get(clickedIndex);
		int localX = MathHelper.floor(mouseX) - this.textX;
		int clickedLogicalOffset = this.getLogicalIndexAtPixel(seg.content, Math.max(0, localX));
		int clickedLogical = seg.logicalStart + clickedLogicalOffset;
		if (this.logicalToOrig == null || this.logicalToOrig.isEmpty()) this.buildLogicalMappings();
		clickedLogical = MathHelper.clamp(clickedLogical, 0, Math.max(0, this.logicalToOrig.size() - 1));
		int origIndex = this.logicalToOrig.get(clickedLogical);
		this.setCursor(origIndex, Screen.hasShiftDown());


		this.desiredCursorX = Math.max(0, MathHelper.floor(mouseX) - this.textX);
	}

	public boolean mouseDragged(double mouseX, double mouseY, double delta) {
		if (this.isDraggingScrollbar) {
			if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
			int totalLines = this.allSegmentsCache.size();
			int trackY = this.getY() + 6;
			int trackH = this.getInnerHeight();
			if (totalLines <= this.maxVisibleLines) { this.isDraggingScrollbar = false; return true; }
			int thumbMin = 8;
			int thumbH = Math.max(thumbMin, (int)((float)this.maxVisibleLines / (float)totalLines * trackH));
			int maxPosRange = trackH - thumbH;
			double scrollRange = Math.max(0.0, (double)(totalLines * this.lineSpacing - this.maxVisibleLines * this.lineSpacing));
			double dy = mouseY - this.dragMouseY;
			double newThumbPos = (double)(trackY) + ((this.dragStartScrollY / Math.max(1.0, scrollRange)) * (double)maxPosRange) + dy;
			double rel = MathHelper.clamp((float)(newThumbPos - trackY), 0f, (float)maxPosRange);
			double frac = (maxPosRange == 0) ? 0.0 : rel / (double)maxPosRange;
			this.firstVisibleScrollY = frac * scrollRange;
			this.clampScrollToBounds();
			return true;
		}
		return false;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.isDraggingScrollbar) { this.isDraggingScrollbar = false; return true; }
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (!this.isVisible()) return false;
		if (mouseX < this.getX() || mouseX > this.getX() + this.getWidth() || mouseY < this.getY() || mouseY > this.getY() + this.getHeight()) return false;

		double deltaPixels = amount * this.lineSpacing * 3.0;
		this.firstVisibleScrollY = MathHelper.clamp((float)(this.firstVisibleScrollY - deltaPixels), 0f, (float)Math.max(0.0, this.allSegmentsCache.size() * this.lineSpacing - this.maxVisibleLines * this.lineSpacing));
		return true;
	}



	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (!this.isVisible()) return;
		if (this.drawsBackground()) {
			Identifier identifier = TEXTURES.get(this.isNarratable(), this.isFocused());
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}

		int lineY = this.getY() + 6;
		if (this.logicalText == null) this.buildLogicalMappings();
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();

		this.maxVisibleLines = Math.max(1, this.getInnerHeight() / this.lineSpacing);
		this.clampScrollToBounds();


		int startIdx = Math.max(0, (int)Math.floor(this.firstVisibleScrollY / (double)this.lineSpacing));
		double offsetPixels = this.firstVisibleScrollY - (double)startIdx * (double)this.lineSpacing;
		int endIdx = Math.min(this.allSegmentsCache.size(), startIdx + this.maxVisibleLines + 1);
		int logicalSelStart = mapOrigToLogical(this.selectionStart);
		int logicalSelEnd = mapOrigToLogical(this.selectionEnd);
		int logicalCursor = mapOrigToLogical(this.selectionStart);

		int y = lineY - MathHelper.floor(offsetPixels);
		for (int idx = startIdx; idx < endIdx; idx++) {

			VisualSegment seg = this.allSegmentsCache.get(idx);
			int segStart = seg.logicalStart;
			int segEnd = seg.logicalStart + seg.content.length();

			int selStart = Math.max(Math.min(logicalSelStart, logicalSelEnd), segStart);
			int selEnd = Math.min(Math.max(logicalSelStart, logicalSelEnd), segEnd);
			if (selStart < selEnd) {
				int startOffset = selStart - segStart;
				int endOffset = selEnd - segStart;
				int xStart = this.textX + getTextWidth(seg.content.substring(0, Math.max(0, Math.min(startOffset, seg.content.length()))));
				int xEnd = this.textX + getTextWidth(seg.content.substring(0, Math.max(0, Math.min(endOffset, seg.content.length()))));
				context.fill(xStart, y - 1, xEnd, y + this.lineHeight - 1, LUADefaults.SELECTION_COLOR);
			}


			boolean showCursor = this.isFocused() && ((Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) % 1000L < 700);
			if (logicalCursor >= segStart && logicalCursor <= segEnd) {
				int cursorOffset = logicalCursor - segStart;
				int safeOffset = Math.max(0, Math.min(cursorOffset, seg.content.length()));
				int cursorX = this.textX + getTextWidth(seg.content.substring(0, safeOffset));
				if(showCursor) {
					context.fill(cursorX - 1, y - 1, cursorX + 1, y + this.lineHeight - 1, Colors.WHITE);
				} else {
					context.fill(cursorX - 1, y - 1, cursorX + 1, y + this.lineHeight - 1, Colors.GRAY);
				}
			}

			y += this.lineSpacing;
		}


		y = lineY - MathHelper.floor(offsetPixels);
		for (int idx = startIdx; idx < endIdx; idx++) {
			VisualSegment seg = this.allSegmentsCache.get(idx);
			drawSegmentWithHighlighting(context, seg, this.textX, y);
			y += this.lineSpacing;
		}


		int totalLines = this.allSegmentsCache.size();
		int trackX = this.getX() + this.getWidth() - SCROLLBAR_PADDING - SCROLLBAR_WIDTH;
		int trackY = this.getY() + 6;
		int trackH = this.getInnerHeight();
		context.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackH, 0xFF1F1F1F);
		if (totalLines > this.maxVisibleLines) {
			int thumbMin = 8;
			double totalHeight = (double)totalLines * this.lineSpacing;
			int thumbH = Math.max(thumbMin, (int)((double)this.maxVisibleLines / (double)totalLines * trackH));
			double scrollRange = Math.max(0.0, totalHeight - (double)this.maxVisibleLines * this.lineSpacing);
			int maxPosRange = trackH - thumbH;
			int thumbY = trackY;
			if (scrollRange > 0.0) {
				thumbY = trackY + (int)((this.firstVisibleScrollY / scrollRange) * (double)maxPosRange);
			}
			context.fill(trackX + 1, thumbY, trackX + SCROLLBAR_WIDTH - 1, thumbY + thumbH, 0xFFA0A0A0);
			context.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF3F3F3F);
		} else {
			int thumbH = Math.max(8, trackH / 6);
			int thumbY = trackY + (trackH - thumbH) / 2;
			context.fill(trackX + 1, thumbY, trackX + SCROLLBAR_WIDTH - 1, thumbY + thumbH, 0xFF2E2E2E);
		}


		if (this.placeholder != null && this.text.isEmpty() && !this.isFocused()) {
			int phColor = 0xFF777777;
			context.drawTextWithShadow(this.textRenderer, OrderedText.styledForwardsVisitedString(this.placeholder.getString(), Style.EMPTY), this.textX, this.getY() + 6, phColor);
		}
	}

	private record VisualSegment(int logicalStart, String content) {
	}

	private void buildVisualSegmentsFull() {
		this.allSegmentsCache = new ArrayList<>();
		if (this.logicalText == null) this.buildLogicalMappings();
		if (this.logicalText == null) return;

		int maxWidth = getInnerWidth();
		String visibleLogical = this.logicalText;

		int pos = 0;
		int logicalPos = 0;
		while (pos < visibleLogical.length()) {
			int nextNewline = visibleLogical.indexOf('\n', pos);
			int lineEnd = (nextNewline == -1) ? visibleLogical.length() : nextNewline;
			String logicalLine = visibleLogical.substring(pos, lineEnd);
			int segOffset = 0;
			if (logicalLine.isEmpty()) {
				this.allSegmentsCache.add(new VisualSegment(logicalPos, ""));
			} else {
				while (segOffset < logicalLine.length()) {
					String remain = logicalLine.substring(segOffset);
					String fit = this.textRenderer.trimToWidth(remain, maxWidth);
					if (fit.isEmpty() && !remain.isEmpty()) fit = remain.substring(0, 1);
					this.allSegmentsCache.add(new VisualSegment(logicalPos + segOffset, fit));
					segOffset += fit.length();
				}
			}
			int consumed = (lineEnd - pos);
			pos = lineEnd;
			logicalPos += consumed;
			if (nextNewline != -1) {
				pos += 1;
				logicalPos += 1;
			}
		}

		if (this.allSegmentsCache.isEmpty()) this.allSegmentsCache.add(new VisualSegment(0, ""));
		this.clampScrollToBounds();
	}

	private void drawSegmentWithHighlighting(DrawContext context, VisualSegment seg, int baseX, int baseY) {
		if (this.logicalText == null) { this.buildLogicalMappings(); }
		if (this.logicalText == null) return;

		int segStartLogical = seg.logicalStart;
		int segEndLogical = seg.logicalStart + seg.content.length();

		segStartLogical = Math.max(0, Math.min(segStartLogical, this.logicalText.length()));
		segEndLogical = Math.max(0, Math.min(segEndLogical, this.logicalText.length()));

		int lineStartIdx = this.logicalText.lastIndexOf('\n', Math.max(0, segStartLogical - 1));
		if (lineStartIdx == -1) lineStartIdx = 0; else lineStartIdx = lineStartIdx + 1;
		int lineEndIdx = this.logicalText.indexOf('\n', segStartLogical);
		if (lineEndIdx == -1) lineEndIdx = this.logicalText.length();

		if (lineStartIdx > lineEndIdx) {
			int t = lineStartIdx;
			lineStartIdx = lineEndIdx;
			lineEndIdx = t;
		}
		lineStartIdx = Math.max(0, Math.min(lineStartIdx, this.logicalText.length()));
		lineEndIdx = Math.max(0, Math.min(lineEndIdx, this.logicalText.length()));

		String fullLine = (lineStartIdx < lineEndIdx) ? this.logicalText.substring(lineStartIdx, lineEndIdx) : "";
		fullLine = fullLine.replaceAll(Pattern.quote("\t"), "⟹");

		int segLocalStart = seg.logicalStart - lineStartIdx;
		int segLocalEnd = segLocalStart + seg.content.length();
		segLocalStart = Math.max(0, Math.min(segLocalStart, fullLine.length()));
		segLocalEnd = Math.max(0, Math.min(segLocalEnd, fullLine.length()));

		Matcher m = TOKEN_PATTERN.matcher(fullLine);
		int last = 0;
		int drawX = baseX;

		while (m.find()) {

			if (m.start() > last) {
				String plain = fullLine.substring(last, m.start());
				int overlapStart = Math.max(last, segLocalStart);
				int overlapEnd = Math.min(m.start(), segLocalEnd);
				if (overlapStart < overlapEnd) {
					int from = overlapStart - last;
					int to = overlapEnd - last;
					int xOffset = drawX + getTextWidth(plain.substring(0, from));
					String slice = plain.substring(from, to);
					context.drawTextWithShadow(this.textRenderer, OrderedText.styledForwardsVisitedString(slice, Style.EMPTY), xOffset, baseY, LUADefaults.COLOR_DEFAULT);
				}
				drawX += getTextWidth(plain);
			}

			int matchedGroupIndex = -1;
			String matchedText = null;
			for (int gi = 1; gi <= m.groupCount(); gi++) {
				try {
					String g = m.group(gi);
					if (g != null) { matchedGroupIndex = gi; matchedText = g; break; }
				} catch (IllegalStateException ignored) { /* defensive */ }
			}


			String token = fullLine.substring(m.start(), m.end());


			int color = LUADefaults.COLOR_DEFAULT;


			try {
				if (LUADefaults.runtimeColors != null) {
					Integer rt = LUADefaults.runtimeColors.get(matchedText);
					if (rt == null) rt = LUADefaults.runtimeColors.get(token);
					if (rt != null) color = rt;
				}
			} catch (Throwable ignored) {}


			if (color == LUADefaults.COLOR_DEFAULT && matchedText != null && matchedText.matches("\\w+\\s*[:.]+\\s*\\w+\\s*")) {
				color = LUADefaults.getMethodCallColor(matchedText, token);
			}


			if (color == LUADefaults.COLOR_DEFAULT && matchedGroupIndex != -1) {
				int idx = matchedGroupIndex - 1;
				try {
					if (LUADefaults.groupColors != null && idx >= 0 && idx < LUADefaults.groupColors.size()) {
						color = LUADefaults.groupColors.get(idx);
					}
				} catch (Throwable ignored) {}
			}

			if (color == 0) color = LUADefaults.COLOR_DEFAULT;

			int overlapStartToken = Math.max(m.start(), segLocalStart);
			int overlapEndToken = Math.min(m.end(), segLocalEnd);
			if (overlapStartToken < overlapEndToken) {
				int from = overlapStartToken - m.start();
				int xOffset = drawX + getTextWidth(token.substring(0, from));
				String slice = token.substring(from, overlapEndToken - m.start());
				context.drawTextWithShadow(this.textRenderer, OrderedText.styledForwardsVisitedString(slice, Style.EMPTY), xOffset, baseY, color);
			}
			drawX += getTextWidth(token);
			last = m.end();
		}

		
		if (last < fullLine.length()) {
			String trailing = fullLine.substring(last);
			int overlapStart = Math.max(last, segLocalStart);
			int overlapEnd = Math.min(fullLine.length(), segLocalEnd);
			if (overlapStart < overlapEnd) {
				int from = overlapStart - last;
				int xOffset = drawX + getTextWidth(trailing.substring(0, from));
				String slice = trailing.substring(from, overlapEnd - last);
				context.drawTextWithShadow(this.textRenderer, OrderedText.styledForwardsVisitedString(slice , Style.EMPTY), xOffset, baseY, LUADefaults.COLOR_DEFAULT);
			}
		}
	}


	private void updateTextPosition() {
		if (this.textRenderer != null) {
			if (this.logicalText == null) this.buildLogicalMappings();
			if (this.firstLogicalIndex > this.logicalText.length()) this.firstLogicalIndex = Math.max(0, this.logicalText.length());
			this.textX = this.getX() + (this.isCentered() ? (this.getWidth() - this.textRenderer.getWidth("")) / 2 : (this.drawsBackground ? 4 : 0));
			this.textY = this.drawsBackground ? this.getY() + 6 : this.getY();
			if (this.logicalToOrig != null && this.firstLogicalIndex >= 0 && this.firstLogicalIndex < this.logicalToOrig.size()) {
				this.firstCharacterIndex = this.logicalToOrig.get(this.firstLogicalIndex);
			} else if (this.logicalToOrig != null && !this.logicalToOrig.isEmpty()) {
				this.firstCharacterIndex = this.logicalToOrig.getLast();
			} else this.firstCharacterIndex = 0;
		}
	}

	public void setMaxLength(int maxLength) { this.maxLength = maxLength; if (this.text.length() > maxLength) { this.text = this.text.substring(0, maxLength); this.onChanged(this.text); } }
	private int getMaxLength() { return this.maxLength; }
	public int getCursor() { return this.selectionStart; }
	public boolean drawsBackground() { return this.drawsBackground; }
	public void setDrawsBackground(boolean drawsBackground) { this.drawsBackground = drawsBackground; this.updateTextPosition(); }
	public void setEditableColor(int editableColor) { this.editableColor = editableColor; }
	public void setUneditableColor(int uneditableColor) { this.uneditableColor = uneditableColor; }
	public void setFocused(boolean focused) { if (this.focusUnlocked || focused) { super.setFocused(focused); if (focused) this.lastSwitchFocusTime = Util.getMeasuringTimeMs(); } }
	private boolean isEditable() { return this.editable; }
	public void setEditable(boolean editable) { this.editable = editable; }
	private boolean isCentered() { return this.centered; }
	public void setCentered(boolean centered) { this.centered = centered; this.updateTextPosition(); }
	public void setTextShadow(boolean textShadow) { this.textShadow = textShadow; }


	public int getInnerWidth() {
		int base = this.drawsBackground() ? this.width - 8 : this.width;
		return Math.max(16, base - (SCROLLBAR_WIDTH + SCROLLBAR_PADDING));
	}

	public int getInnerHeight() { return this.drawsBackground() ? Math.max(1, this.height - 12) : this.height; }

	public void setSelectionEnd(int index) {
		this.selectionEnd = MathHelper.clamp(index, 0, this.text.length());
		this.updateFirstCharacterIndex(this.selectionEnd);
		this.ensureCursorVisible();
	}

	private void updateFirstCharacterIndex(int cursorOrig) {
		if (this.logicalText == null) this.buildLogicalMappings();
		int logicalCursor = mapOrigToLogical(MathHelper.clamp(cursorOrig, 0, this.text.length()));
		if (this.logicalToOrig != null && this.firstLogicalIndex < this.logicalToOrig.size()) {
			this.firstCharacterIndex = this.logicalToOrig.get(Math.max(0, Math.min(this.firstLogicalIndex, this.logicalToOrig.size() - 1)));
		} else if (this.logicalToOrig != null && !this.logicalToOrig.isEmpty()) {
			this.firstCharacterIndex = this.logicalToOrig.getLast();
		} else this.firstCharacterIndex = 0;
	}

	public void setFocusUnlocked(boolean focusUnlocked) { this.focusUnlocked = focusUnlocked; }

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isVisible() { return this.visible; }

	public void setVisible(boolean visible) { this.visible = visible; }

	public void setSuggestion(@Nullable String suggestion) { this.suggestion = suggestion; }

	public int getCharacterX(int index) {
		if (index > this.text.length()) return this.getX();
		int logical = mapOrigToLogical(index);
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		int segIndex = getSegmentIndexForLogical(logical);
		if (segIndex < 0) return this.getX();
		VisualSegment seg = this.allSegmentsCache.get(segIndex);
		int offset = logical - seg.logicalStart;
		if (offset <= 0) return this.textX;
		String sub = seg.content.substring(0, MathHelper.clamp(offset, 0, seg.content.length()));
		return this.getX() + this.textRenderer.getWidth(sub) + (this.isCentered() ? (this.getWidth() - this.textRenderer.getWidth(sub)) / 2 : (this.drawsBackground ? 4 : 0));
	}

	public void appendClickableNarrations(NarrationMessageBuilder builder) { builder.put(NarrationPart.TITLE, this.getNarrationMessage()); }
	public void setPlaceholder(Text placeholder) { this.placeholder = placeholder; }


	private void buildLogicalMappings() {
		String tab = CustomRegexMarkersLUA.tabMarker;
		String ret = CustomRegexMarkersLUA.returnMarker;
		int n = this.text.length();
		this.origToLogical = new int[n + 1];
		this.logicalToOrig = new ArrayList<>();
		StringBuilder b = new StringBuilder();
		int i = 0;
		int logicalIndex = 0;
		while (i < n) {
			if (tab != null && tab.length() > 0 && i + tab.length() <= n && this.text.startsWith(tab, i)) {
				this.origToLogical[i] = logicalIndex;
				for (int k = 1; k < tab.length(); k++) this.origToLogical[i + k] = logicalIndex;
				b.append('\t');
				this.logicalToOrig.add(i);
				i += tab.length();
				logicalIndex++;
			} else if (ret != null && ret.length() > 0 && i + ret.length() <= n && this.text.startsWith(ret, i)) {
				this.origToLogical[i] = logicalIndex;
				for (int k = 1; k < ret.length(); k++) this.origToLogical[i + k] = logicalIndex;
				b.append('\n');
				this.logicalToOrig.add(i);
				i += ret.length();
				logicalIndex++;
			} else {
				this.origToLogical[i] = logicalIndex;
				b.append(this.text.charAt(i));
				this.logicalToOrig.add(i);
				i++;
				logicalIndex++;
			}
		}
		this.origToLogical[n] = logicalIndex;
		this.logicalToOrig.add(n);
		this.logicalText = b.toString();
		this.firstLogicalIndex = MathHelper.clamp(this.firstLogicalIndex, 0, Math.max(0, this.logicalText.length()));
		this.buildVisualSegmentsFull();
	}

	private int mapOrigToLogical(int origIndex) {
		if (this.origToLogical == null || this.origToLogical.length < this.text.length() + 1 || this.logicalToOrig == null) this.buildLogicalMappings();
		if (origIndex < 0) return 0;
		if (origIndex > this.text.length()) return this.logicalText == null ? 0 : this.logicalText.length();
		int idx = MathHelper.clamp(origIndex, 0, this.text.length());
		if (idx >= this.origToLogical.length) idx = this.origToLogical.length - 1;
		if (idx < 0) idx = 0;
		return this.origToLogical[idx];
	}

	private int getLogicalIndexAtPixel(String s, int pixelX) {
		if (pixelX <= 0) return 0;
		int low = 0;
		int high = s.length();
		while (low < high) {
			int mid = (low + high + 1) / 2;
			int w = this.textRenderer.getWidth(s.substring(0, mid));
			if (w <= pixelX) low = mid; else high = mid - 1;
		}
		return low;
	}

	private int getSegmentIndexForLogical(int logicalIndex) {
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		for (int i = 0; i < this.allSegmentsCache.size(); i++) {
			VisualSegment seg = this.allSegmentsCache.get(i);
			if (logicalIndex >= seg.logicalStart && logicalIndex <= seg.logicalStart + seg.content.length()) return i;
		}
		return Math.max(0, this.allSegmentsCache.size() - 1);
	}


	private void ensureCursorVisible() {
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		int logicalCursor = mapOrigToLogical(this.selectionStart);
		int cursorSeg = getSegmentIndexForLogical(logicalCursor);
		double segTop = (double)cursorSeg * (double)this.lineSpacing;
		double segBottom = segTop + this.lineSpacing;
		double viewTop = this.firstVisibleScrollY;
		double viewBottom = this.firstVisibleScrollY + (double)this.maxVisibleLines * this.lineSpacing;
		if (segTop < viewTop) this.firstVisibleScrollY = segTop;
		else if (segBottom > viewBottom) this.firstVisibleScrollY = Math.max(0.0, segBottom - (double)this.maxVisibleLines * this.lineSpacing);
		this.clampScrollToBounds();
	}


	private void updateDesiredCursorXFromCursor() {
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		int logicalCursor = mapOrigToLogical(this.selectionStart);
		int segIdx = getSegmentIndexForLogical(logicalCursor);
		VisualSegment seg = this.allSegmentsCache.get(segIdx);
		int offset = logicalCursor - seg.logicalStart;
		int cursorX = this.textX + getTextWidth(seg.content.substring(0, Math.max(0, Math.min(offset, seg.content.length()))));
		this.desiredCursorX = Math.max(0, cursorX - this.textX);
	}

	private void moveCursorUp(boolean shiftKey) {
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		int logicalCursor = mapOrigToLogical(this.selectionStart);
		int segIdx = getSegmentIndexForLogical(logicalCursor);
		if (segIdx <= 0) { this.setCursorToStart(shiftKey); return; }
		VisualSegment cur = this.allSegmentsCache.get(segIdx);
		int cursorOffsetInSeg = logicalCursor - cur.logicalStart;
		int pixelColumn = (this.desiredCursorX >= 0) ? this.desiredCursorX : getTextWidth(cur.content.substring(0, Math.max(0, Math.min(cursorOffsetInSeg, cur.content.length()))));
		VisualSegment target = this.allSegmentsCache.get(segIdx - 1);
		int targetOffset = this.getLogicalIndexAtPixel(target.content, pixelColumn);
		int targetLogical = target.logicalStart + targetOffset;
		targetLogical = MathHelper.clamp(targetLogical, 0, Math.max(0, this.logicalText.length()));
		int origIndex = this.logicalToOrig.get(Math.min(targetLogical, this.logicalToOrig.size() - 1));
		this.setCursor(origIndex, shiftKey);
		this.ensureCursorVisible();
	}

	private void moveCursorDown(boolean shiftKey) {
		if (this.allSegmentsCache == null || this.allSegmentsCache.isEmpty()) this.buildVisualSegmentsFull();
		int logicalCursor = mapOrigToLogical(this.selectionStart);
		int segIdx = getSegmentIndexForLogical(logicalCursor);
		if (segIdx >= this.allSegmentsCache.size() - 1) { this.setCursorToEnd(shiftKey); return; }
		VisualSegment cur = this.allSegmentsCache.get(segIdx);
		int cursorOffsetInSeg = logicalCursor - cur.logicalStart;
		int pixelColumn = (this.desiredCursorX >= 0) ? this.desiredCursorX : getTextWidth(cur.content.substring(0, Math.max(0, Math.min(cursorOffsetInSeg, cur.content.length()))));
		VisualSegment target = this.allSegmentsCache.get(segIdx + 1);
		int targetOffset = this.getLogicalIndexAtPixel(target.content, pixelColumn);
		int targetLogical = target.logicalStart + targetOffset;
		targetLogical = MathHelper.clamp(targetLogical, 0, Math.max(0, this.logicalText.length()));
		int origIndex = this.logicalToOrig.get(Math.min(targetLogical, this.logicalToOrig.size() - 1));
		this.setCursor(origIndex, shiftKey);
		this.ensureCursorVisible();
	}

	private void clampScrollToBounds() {
		if (this.allSegmentsCache == null) return;
		double totalHeight = (double)this.allSegmentsCache.size() * this.lineSpacing;
		double maxScroll = Math.max(0.0, totalHeight - (double)this.maxVisibleLines * this.lineSpacing);
		if (this.firstVisibleScrollY < 0.0) this.firstVisibleScrollY = 0.0;
		if (this.firstVisibleScrollY > maxScroll) this.firstVisibleScrollY = maxScroll;
	}

	private int getTextWidth(String txt) { return this.textRenderer.getWidth(txt); }

}
