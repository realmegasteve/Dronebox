package net.Neomoon.dronebox.LUA;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface LazuliMultilineText {

	int minX = 0;
	int minY = 0;
	int maxX = 999999;
	int maxY = 999999;

	LazuliMultilineText EMPTY = new LazuliMultilineText() {
		@Override
		public void drawCenterWithShadow(DrawContext context, int x, int y) {
		}

		@Override
		public void setCursor(int cursor){}

		@Override
		public void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
		}

		@Override
		public void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
		}

		@Override
		public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
			return y;
		}

		@Nullable
		@Override
		public Style getStyleAtCentered(int x, int y, int i, double mouseX, double mouseY) {
			return null;
		}

		@Override
		public void setCropping(int MinX, int MinY, int MaxX, int MaxY){

		}

		@Nullable
		@Override
		public Style getStyleAtLeftAligned(int x, int y, int i, double mouseX, double mouseY) {
			return null;
		}

		@Override
		public int count() {
			return 0;
		}

		@Override
		public int getMaxWidth() {
			return 0;
		}
	};

	static LazuliMultilineText create(TextRenderer renderer, Text... texts) {
		return create(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, texts);
	}

	static LazuliMultilineText create(TextRenderer renderer, int maxWidth, Text... texts) {
		return create(renderer, maxWidth, Integer.MAX_VALUE, texts);
	}

	static LazuliMultilineText create(TextRenderer renderer, Text text, int maxWidth) {
		return create(renderer, maxWidth, Integer.MAX_VALUE, text);
	}

	static LazuliMultilineText create(TextRenderer renderer, int maxWidth, int maxLines, Text... texts) {
		return texts.length == 0 ? EMPTY : new LazuliMultilineText() {
			@Nullable
			private List<LazuliMultilineText.Line> lines;
			@Nullable
			private Language language;
			private int minX = 0;
			private int minY = 0;
			private int maxX = 999999;
			private int maxY = 999999;

			private int cursor;

			public void setCursor(int Cursor){
				cursor = Cursor;
			}


			@Override
			public void drawCenterWithShadow(DrawContext context, int x, int y) {
				this.drawCenterWithShadow(context, x, y, 9, -1);
			}

			@Override
			public void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
				int i = y;

				for (LazuliMultilineText.Line line : this.getLines()) {
					context.drawTextWithShadow(renderer, line.text, x - line.width / 2, i, color);
					i += lineHeight;
				}
			}

			@Override
			public void setCropping(int MinX, int MinY, int MaxX, int MaxY){
				minX = MinX;
				minY = MinY;
				maxX = MaxX;
				maxY = MaxY;
			}

			@Override
			public void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
				int i = y;
				int count = 0;
				int lineStart = 0;

				for (LazuliMultilineText.Line line : this.getLines()) {

					StringBuilder sb = new StringBuilder();
					line.text.accept((charIndex, style, codePoint) -> {
						sb.appendCodePoint(codePoint);
						return true;
					});
					String plain = sb.toString();

					//Shennanigans to keep cursor pos
					String preCursor = plain.substring(0, Math.max(Math.min(cursor, plain.length() - 1), 0));
					preCursor = preCursor.replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), " ┃ ");
					cursor = preCursor.length();

					plain = plain.replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), " ┃ ");





					String[] parts = plain.split(Pattern.quote(CustomRegexMarkersLUA.returnMarker));

					for (String thisString : parts) {
						//draw the text itself
						String trimmedString = renderer.trimToWidth(thisString, maxX - x);
						OrderedText ot = OrderedText.styledForwardsVisitedString(trimmedString, Style.EMPTY);
						if (i > minY && i + lineHeight < maxY) {
							context.drawTextWithShadow(renderer, ot, x, i, color);
						}

						if (cursor >= lineStart && cursor <= lineStart + thisString.length()) {
							//draw the cursor
							int cursorXOffset = renderer.getWidth(OrderedText.styledForwardsVisitedString(thisString.substring(0, cursor - lineStart), Style.EMPTY));
							context.fill(x + cursorXOffset - 1, i - 1, x + cursorXOffset + 1, i + 9, Colors.WHITE);
						}


						i += lineHeight;
						lineStart += thisString.length();
						cursor--;
					}
				}

				//System.out.println(log);

			}

			@Override
			public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
				int i = y;

				for (LazuliMultilineText.Line line : this.getLines()) {
					context.drawText(renderer, line.text, x, i, color, false);
					i += lineHeight;
				}

				return i;
			}

			@Nullable
			@Override
			public Style getStyleAtCentered(int x, int y, int i, double mouseX, double mouseY) {
				List<LazuliMultilineText.Line> list = this.getLines();
				int j = MathHelper.floor((mouseY - y) / i);
				if (j >= 0 && j < list.size()) {
					LazuliMultilineText.Line line = list.get(j);
					int k = x - line.width / 2;
					if (mouseX < k) {
						return null;
					} else {
						int l = MathHelper.floor(mouseX - k);
						return renderer.getTextHandler().getStyleAt(line.text, l);
					}
				} else {
					return null;
				}
			}

			@Nullable
			@Override
			public Style getStyleAtLeftAligned(int x, int y, int i, double mouseX, double mouseY) {
				if (mouseX < x) {
					return null;
				} else {
					List<LazuliMultilineText.Line> list = this.getLines();
					int j = MathHelper.floor((mouseY - y) / i);
					if (j >= 0 && j < list.size()) {
						LazuliMultilineText.Line line = list.get(j);
						int k = MathHelper.floor(mouseX - x);
						return renderer.getTextHandler().getStyleAt(line.text, k);
					} else {
						return null;
					}
				}
			}

			private List<LazuliMultilineText.Line> getLines() {
				Language language = Language.getInstance();
				if (this.lines != null && language == this.language) { return this.lines;}

				this.language = language;
				List<StringVisitable> list = new ArrayList();

				for (Text text : texts) {
					list.addAll(renderer.wrapLinesWithoutLanguage(text, maxWidth));
				}

				this.lines = new ArrayList();
				int i = Math.min(list.size(), maxLines);
				List<StringVisitable> list2 = list.subList(0, i);

				for (int j = 0; j < list2.size(); j++) {
					StringVisitable stringVisitable = list2.get(j);
					OrderedText orderedText = Language.getInstance().reorder(stringVisitable);
					if (j == list2.size() - 1 && i == maxLines && i != list.size()) {
						StringVisitable stringVisitable2 = renderer.trimToWidth(stringVisitable, renderer.getWidth(stringVisitable) - renderer.getWidth(ScreenTexts.ELLIPSIS));
						StringVisitable stringVisitable3 = StringVisitable.concat(stringVisitable2, ScreenTexts.ELLIPSIS);
						this.lines.add(new LazuliMultilineText.Line(Language.getInstance().reorder(stringVisitable3), renderer.getWidth(stringVisitable3)));
					} else {
						this.lines.add(new LazuliMultilineText.Line(orderedText, renderer.getWidth(orderedText)));
					}
				}
					return this.lines;
			}

			@Override
			public int count() {
				return this.getLines().size();
			}

			@Override
			public int getMaxWidth() {
				return Math.min(maxWidth, this.getLines().stream().mapToInt(LazuliMultilineText.Line::width).max().orElse(0));
			}
		};
	}

	void drawCenterWithShadow(DrawContext context, int x, int y);

	void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color);

	void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color);

	int draw(DrawContext context, int x, int y, int lineHeight, int color);

	@Nullable
	Style getStyleAtCentered(int x, int y, int i, double mouseX, double mouseY);

	@Nullable
	Style getStyleAtLeftAligned(int x, int y, int i, double mouseX, double mouseY);

	int count();

	int getMaxWidth();

	public void setCropping(int MinX, int MinY, int MaxX, int MaxY);

	void setCursor(int cursor);

	@Environment(EnvType.CLIENT)
	record Line(OrderedText text, int width) {
	}


}
