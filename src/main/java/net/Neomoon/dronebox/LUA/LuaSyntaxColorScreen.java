package net.Neomoon.dronebox.LUA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LuaSyntaxColorScreen extends Screen {
	private final Screen parent;
	private final MinecraftClient client = MinecraftClient.getInstance();

	private final List<RuleEntry> entries = new ArrayList<>();
	private final List<RuleEntry> filtered = new ArrayList<>();
	private int scroll = 0;

	private final int padding = 12;
	private final int entryHeight = 76;

	private int draggingEntry = -1;
	private DragMode draggingMode = DragMode.NONE;

	private boolean draggingScrollbar = false;
	private int scrollbarDragOffset = 0;

	private static final boolean DEBUG = false;

	private GLFWScrollCallback glfwScrollCallback = null;
	private GLFWScrollCallback prevGlfwScrollCallback = null;

	private String searchQuery = "";
	private boolean searchFocused = false;
	private boolean searchOpen = false;
	private int caretIndex = 0;
	private long caretBlinkStartMs = System.currentTimeMillis();
	private int hoveredEntry = -1;

	private int suggestionSelected = -1;

	private float doneHoverProgress = 0f;

	private enum DragMode { NONE, HUE, SAT, VAL }

	public LuaSyntaxColorScreen(Screen parent) {
		super(Text.literal("Lua Syntax Colors"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		entries.clear();
		filtered.clear();

		List<String> names = null;
		List<Integer> colors = null;

		try {
			Field fn = findFieldIgnoreCase(LUADefaults.class, "ruleNames");
			Field fc = findFieldIgnoreCase(LUADefaults.class, "groupColors");
			if (fn != null && fc != null) {
				fn.setAccessible(true);
				fc.setAccessible(true);
				Object on = fn.get(null);
				Object oc = fc.get(null);
				if (on instanceof List && oc instanceof List) {
					names = (List<String>) on;
					colors = (List<Integer>) oc;
				}
			}
		} catch (Throwable ignored) {}

		if (names == null || colors == null) {
			try {
				names = (List<String>) LUADefaults.class.getMethod("getRuleNames").invoke(null);
				colors = (List<Integer>) LUADefaults.class.getMethod("getGroupColors").invoke(null);
			} catch (Throwable ignored) {
				names = List.of("error_no_rules_found");
				colors = List.of(0xFFFF0000);
			}
		}

		Map<String, Integer> map = new LinkedHashMap<>();
		int count = Math.min(names.size(), colors.size());
		for (int i = 0; i < count; i++) {
			String n = names.get(i);
			Integer c = colors.get(i);
			if (!map.containsKey(n)) map.put(n, c);
		}

		for (Map.Entry<String, Integer> e : map.entrySet()) {
			entries.add(new RuleEntry(e.getKey(), e.getValue()));
		}

		recomputeFiltered();

		try {
			long handle = this.client.getWindow().getHandle();
			prevGlfwScrollCallback = GLFW.glfwSetScrollCallback(handle, glfwScrollCallback = new GLFWScrollCallback() {
				@Override
				public void invoke(long window, double xoffset, double yoffset) {
					int contentHeight = filtered.size() * entryHeight;
					int viewport = LuaSyntaxColorScreen.this.height - 160;
					int max = Math.max(0, contentHeight - viewport);
					int delta = (int) Math.signum(yoffset) * Math.max(1, (int) (Math.abs(yoffset) * 20.0));
					scroll -= delta;
					scroll = MathHelper.clamp(scroll, 0, max);
					if (prevGlfwScrollCallback != null) {
						prevGlfwScrollCallback.invoke(window, xoffset, yoffset);
					}
				}
			});
		} catch (Throwable ignored) {
		}
	}

	private static Field findFieldIgnoreCase(Class<?> c, String name) {
		for (Field f : c.getDeclaredFields()) {
			if (f.getName().equalsIgnoreCase(name)) return f;
		}
		return null;
	}

	private void handleMouseWheelReflection() {
		try {
			Mouse mouse = client.mouse;
			if (mouse == null) return;

			Field scrollerField = findFieldIgnoreCase(mouse.getClass(), "scroller");
			if (scrollerField == null) return;
			scrollerField.setAccessible(true);
			Object scroller = scrollerField.get(mouse);
			if (scroller == null) return;

			Field cumulV = findFieldIgnoreCase(scroller.getClass(), "cumulVertical");
			if (cumulV == null) return;
			cumulV.setAccessible(true);
			double cumul = ((Number) cumulV.get(scroller)).doubleValue();

			if (cumul != 0.0) {
				int contentHeight = filtered.size() * entryHeight;
				int viewport = this.height - 160;
				int max = Math.max(0, contentHeight - viewport);
				int delta = (int) Math.signum(cumul) * Math.max(1, (int) (Math.abs(cumul) * 20.0));
				scroll -= delta;
				scroll = MathHelper.clamp(scroll, 0, max);
				cumulV.setDouble(scroller, 0.0);
			}
		} catch (Throwable ignored) {}
	}

	private void recomputeFiltered() {
		filtered.clear();
		if (searchQuery == null || searchQuery.isEmpty()) {
			filtered.addAll(entries);
		} else {
			String q = searchQuery.toLowerCase();
			for (RuleEntry r : entries) {
				if (r.name.toLowerCase().contains(q)) filtered.add(r);
			}
		}
		int contentHeight = filtered.size() * entryHeight;
		int viewport = this.height - 160;
		int max = Math.max(0, contentHeight - viewport);
		scroll = MathHelper.clamp(scroll, 0, max);
		suggestionSelected = filtered.isEmpty() ? -1 : 0;
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		drawContext.fill(0, 0, this.width, this.height, 0x88000000);

		int left = padding;
		int right = this.width - padding;
		int yStart = 80;
		int viewportHeight = this.height - 160;
		int contentHeight = filtered.size() * entryHeight;

		handleMouseWheelReflection();

		int maxScroll = Math.max(0, contentHeight - viewportHeight);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);

		int contentRight = right - 16;
		int contentWidth = contentRight - left;

		hoveredEntry = -1;
		for (int i = 0; i < filtered.size(); i++) {
			int y = yStart + i * entryHeight - scroll;
			if (y + entryHeight < yStart || y > this.height - 40) continue;
			filtered.get(i).render(drawContext, left, y, contentWidth, entryHeight, mouseX, mouseY);
			if (mouseX >= left && mouseX <= left + contentWidth && mouseY >= y && mouseY <= y + entryHeight) {
				hoveredEntry = i;
			}
		}

		if (DEBUG) {
			for (int i = 0; i < filtered.size(); i++) {
				int y = yStart + i * entryHeight - scroll;
				int boxLeft = left;
				int boxRight = left + contentWidth;
				drawContext.fill(boxLeft, y, boxLeft + 1, y + 1, 0xFFFF0000);
				drawContext.drawText(textRenderer, Text.literal("i=" + i + " y=" + y), boxLeft + 4, y + 2, 0xFFFFAA00, false);
			}
		}

		int trackX = right - 12;
		int trackY = yStart;
		int trackW = 8;
		int trackH = viewportHeight;
		drawContext.fill(trackX - 1, trackY - 1, trackX + trackW + 1, trackY + trackH + 1, 0xFF1A1A1A);
		drawContext.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF2A2A2A);

		int thumbH = (int) (Math.max(16, (float) viewportHeight * (viewportHeight / (float) Math.max(1, contentHeight))));
		thumbH = Math.min(thumbH, trackH);
		int thumbRange = Math.max(1, trackH - thumbH);
		int thumbY = (contentHeight <= viewportHeight) ? trackY : trackY + (int) ((float) scroll / (contentHeight - viewportHeight) * thumbRange);

		boolean doneHover = false;
		{
			int btnW = 100, btnH = 20;
			int bx = (this.width / 2) - (btnW / 2);
			int by = this.height - 28;
			doneHover = (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH);
		}
		float hoverStep = 0.12f;
		doneHoverProgress += (doneHover ? hoverStep : -hoverStep);
		if (doneHoverProgress < 0f) doneHoverProgress = 0f;
		if (doneHoverProgress > 1f) doneHoverProgress = 1f;

		drawContext.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFF7F7F7F);

		int btnW = 100, btnH = 20;
		int bx = (this.width / 2) - (btnW / 2);
		int by = this.height - 28;
		int doneCol = lerpColor(0xFF2E2E2E, 0xFF3B3B3B, doneHoverProgress);
		drawContext.fill(bx - 1, by - 1, bx + btnW + 1, by + btnH + 1, 0xFF000000);
		drawContext.fill(bx, by, bx + btnW, by + btnH, doneCol);
		int doneTextY = by + (btnH - textRenderer.fontHeight) / 2;
		int doneTextW = textRenderer.getWidth("Done");
		int doneTextX = bx + (btnW - doneTextW) / 2;
		drawContext.drawText(this.textRenderer, Text.literal("Done"), doneTextX, doneTextY, 0xFFFFFFFF, false);

		String titleStr = this.title.getString();
		int titleW = textRenderer.getWidth(titleStr);
		int titleX = (this.width - titleW) / 2;
		int titleY = 8;
		drawContext.drawText(this.textRenderer, Text.literal(titleStr), titleX, titleY, 0xFFFFFFFF, false);

		int searchX = padding;
		int searchY = 28;
		int searchW = Math.max(180, this.width - padding * 2 - 200);
		int searchH = 18;
		boolean searchHover = (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH);

		if (!searchOpen) {
			int col = searchHover ? 0xFF2E2E2E : 0xFF222222;
			drawContext.fill(searchX - 1, searchY - 1, searchX + searchW + 1, searchY + searchH + 1, 0xFF000000);
			drawContext.fill(searchX, searchY, searchX + searchW, searchY + searchH, col);
			String label = searchQuery.isEmpty() ? "Search..." : searchQuery;
			int lx = searchX + 6;
			int ly = searchY + (searchH - textRenderer.fontHeight) / 2;
			drawContext.drawText(textRenderer, Text.literal(label), lx, ly, 0xFFDADADA, false);
		} else {
			drawContext.fill(searchX - 1, searchY - 1, searchX + searchW + 1, searchY + searchH + 1, 0xFF000000);
			drawContext.fill(searchX, searchY, searchX + searchW, searchY + searchH, searchFocused ? 0xFF2E2E2E : (searchHover ? 0xFF262626 : 0xFF222222));
			String display = searchQuery.isEmpty() ? "<search>" : searchQuery;
			int qx = searchX + 6;
			int qy = searchY + (searchH - textRenderer.fontHeight) / 2;
			drawContext.drawText(textRenderer, Text.literal(display), qx, qy, searchFocused ? 0xFFFFFFFF : 0xFFAAAAAA, false);

			if (searchFocused) {
				long elapsed = System.currentTimeMillis() - caretBlinkStartMs;
				boolean caretOn = ((elapsed / 500) % 2) == 0;
				if (caretOn) {
					int caretX = qx + textRenderer.getWidth(display.substring(0, Math.max(0, Math.min(display.length(), caretIndex))));
					int caretY1 = qy;
					int caretY2 = qy + textRenderer.fontHeight;
					drawContext.fill(caretX, caretY1, caretX + 1, caretY2, 0xFFFFFFFF);
				}
			}

			List<String> suggestions = new ArrayList<>();
			if (!searchQuery.isEmpty()) {
				String qlow = searchQuery.toLowerCase();
				for (RuleEntry r : entries) {
					if (r.name.toLowerCase().contains(qlow)) {
						suggestions.add(r.name);
						if (suggestions.size() >= 8) break;
					}
				}
			}
			if (!suggestions.isEmpty()) {
				int sugX = searchX;
				int sugY = searchY + searchH + 4;
				int sugW = searchW;
				int sugH = suggestions.size() * (textRenderer.fontHeight + 6) + 4;
				drawContext.fill(sugX - 1, sugY - 1, sugX + sugW + 1, sugY + sugH + 1, 0xFF000000);
				drawContext.fill(sugX, sugY, sugX + sugW, sugY + sugH, 0xFF1E1E1E);
				for (int i = 0; i < suggestions.size(); i++) {
					String s = suggestions.get(i);
					int sy = sugY + 4 + i * (textRenderer.fontHeight + 6);
					boolean sh = (mouseX >= sugX && mouseX <= sugX + sugW && mouseY >= sy && mouseY <= sy + textRenderer.fontHeight + 4);
					boolean sel = (i == suggestionSelected);
					if (sh || sel) drawContext.fill(sugX + 2, sy - 2, sugX + sugW - 2, sy + textRenderer.fontHeight + 4, 0xFF2A2A2A);
					drawContext.drawText(textRenderer, Text.literal(s), sugX + 6, sy, 0xFFFFFFFF, false);
				}
			}
		}

		super.render(drawContext, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int left = padding;
		int right = this.width - padding;
		int yStart = 80;
		int viewportHeight = this.height - 160;
		int contentHeight = filtered.size() * entryHeight;

		int searchX = left;
		int searchY = 28;
		int searchW = Math.max(180, this.width - padding * 2 - 200);
		int searchH = 18;

		if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH) {
			if (!searchOpen) {
				searchOpen = true;
				searchFocused = true;
				caretIndex = Math.max(0, Math.min(searchQuery.length(), searchQuery.length()));
				caretBlinkStartMs = System.currentTimeMillis();
				recomputeFiltered();
				return true;
			} else {
				searchFocused = true;
				int qx = searchX + 6;
				int localX = (int) (mouseX - qx);
				int pos = 0;
				for (; pos <= searchQuery.length(); pos++) {
					int w = textRenderer.getWidth(searchQuery.substring(0, pos));
					if (w >= localX) break;
				}
				caretIndex = Math.max(0, Math.min(searchQuery.length(), pos));
				caretBlinkStartMs = System.currentTimeMillis();
				return true;
			}
		} else {
			if (searchOpen && !searchQuery.isEmpty()) {
				List<String> suggestions = new ArrayList<>();
				String qlow = searchQuery.toLowerCase();
				for (RuleEntry r : entries) {
					if (r.name.toLowerCase().contains(qlow)) {
						suggestions.add(r.name);
						if (suggestions.size() >= 8) break;
					}
				}
				if (!suggestions.isEmpty()) {
					int sugX = searchX;
					int sugY = searchY + searchH + 4;
					int sugW = searchW;
					for (int i = 0; i < suggestions.size(); i++) {
						int sy = sugY + 4 + i * (textRenderer.fontHeight + 6);
						if (mouseX >= sugX && mouseX <= sugX + sugW && mouseY >= sy && mouseY <= sy + textRenderer.fontHeight + 4) {
							String chosen = suggestions.get(i);
							searchQuery = chosen;
							caretIndex = searchQuery.length();
							recomputeFiltered();
							for (int idx = 0; idx < filtered.size(); idx++) {
								if (filtered.get(idx).name.equals(chosen)) {
									scroll = idx * entryHeight;
									int contentH = filtered.size() * entryHeight;
									int viewport = this.height - 160;
									scroll = MathHelper.clamp(scroll, 0, Math.max(0, contentH - viewport));
									break;
								}
							}
							searchOpen = false;
							searchFocused = false;
							return true;
						}
					}
				}
			}

			if (searchOpen) {
				searchOpen = false;
				searchFocused = false;
			}
		}

		int btnW = 100, btnH = 20;
		int bx = (this.width / 2) - (btnW / 2);
		int by = this.height - 28;
		if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH) {
			this.client.setScreen(parent);
			return true;
		}

		int trackX = right - 12;
		int trackY = yStart;
		int trackW = 8;
		int trackH = viewportHeight;
		int thumbH = (int) (Math.max(16, (float) viewportHeight * (viewportHeight / (float) Math.max(1, contentHeight))));
		thumbH = Math.min(thumbH, trackH);
		int thumbRange = Math.max(1, trackH - thumbH);
		int thumbY = (contentHeight <= viewportHeight) ? trackY : trackY + (int) ((float) scroll / (contentHeight - viewportHeight) * thumbRange);

		if (mouseX >= trackX && mouseX <= trackX + trackW && mouseY >= thumbY && mouseY <= thumbY + thumbH) {
			draggingScrollbar = true;
			scrollbarDragOffset = (int) (mouseY - thumbY);
			return true;
		}
		if (mouseX >= trackX && mouseX <= trackX + trackW && mouseY >= trackY && mouseY <= trackY + trackH) {
			if (mouseY < thumbY) scroll -= viewportHeight - 20;
			else scroll += viewportHeight - 20;
			int maxScroll = Math.max(0, contentHeight - viewportHeight);
			scroll = MathHelper.clamp(scroll, 0, maxScroll);
			return true;
		}

		int contentRight = right - 16;
		int contentWidth = contentRight - left;

		for (int i = 0; i < filtered.size(); i++) {
			int y = yStart + i * entryHeight - scroll;
			if (mouseY < y || mouseY > y + entryHeight) continue;
			RuleEntry e = filtered.get(i);

			int previewSize = 52;
			int slidersX = left + previewSize + 10;
			int slidersW = contentWidth - previewSize - 8 - 120;

			int hueY = y + 8 + 12;
			int hueH = 10;
			int hueX = slidersX;
			if (mouseX >= hueX && mouseX <= hueX + slidersW && mouseY >= hueY && mouseY <= hueY + hueH) {
				draggingEntry = i;
				draggingMode = DragMode.HUE;
				updateHueFromMouse(e, mouseX, hueX, slidersW);
				return true;
			}

			int satY = hueY + 16;
			int satH = 10;
			if (mouseX >= hueX && mouseX <= hueX + slidersW && mouseY >= satY && mouseY <= satY + satH) {
				draggingEntry = i;
				draggingMode = DragMode.SAT;
				updateSatFromMouse(e, mouseX, hueX, slidersW);
				return true;
			}

			int valY = satY + 16;
			int valH = 10;
			if (mouseX >= hueX && mouseX <= hueX + slidersW && mouseY >= valY && mouseY <= valY + valH) {
				draggingEntry = i;
				draggingMode = DragMode.VAL;
				updateValFromMouse(e, mouseX, hueX, slidersW);
				return true;
			}

			int btnX = left + contentWidth - 100;
			int btnWSmall = 48;
			int applyY = y + 12;
			int resetY = applyY + 22;
			if (mouseX >= btnX && mouseX <= btnX + btnWSmall && mouseY >= applyY && mouseY <= applyY + 16) {
				int rgb = hsvToRgbInt(e.hue, e.sat, e.val);
				int argb = 0xFF000000 | (rgb & 0xFFFFFF);
				try { LUADefaults.setColorByRuleName(e.name, argb); } catch (Throwable t) {}
				e.currentColor = argb;
				return true;
			}
			if (mouseX >= btnX && mouseX <= btnX + btnWSmall && mouseY >= resetY && mouseY <= resetY + 16) {
				try {
					int base = getCurrentColorFromLUADefaults(e.name, e.currentColor);
					float[] hsv = rgbToHsv(base);
					e.hue = hsv[0]; e.sat = hsv[1]; e.val = hsv[2];
				} catch (Throwable t) {}
				return true;
			}

			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		draggingEntry = -1;
		draggingMode = DragMode.NONE;
		draggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (draggingScrollbar) {
			int left = padding;
			int right = this.width - padding;
			int yStart = 80;
			int viewportHeight = this.height - 160;
			int contentHeight = filtered.size() * entryHeight;

			int trackX = right - 12;
			int trackY = yStart;
			int trackH = viewportHeight;
			int thumbH = (int) (Math.max(16, (float) viewportHeight * (viewportHeight / (float) Math.max(1, contentHeight))));
			thumbH = Math.min(thumbH, trackH);
			int thumbRange = Math.max(1, trackH - thumbH);

			int mouseYInt = (int) mouseY;
			int newThumbY = mouseYInt - scrollbarDragOffset;
			newThumbY = MathHelper.clamp(newThumbY, trackY, trackY + trackH - thumbH);
			if (contentHeight > viewportHeight) {
				scroll = (int) ((float) (newThumbY - trackY) / (thumbRange) * (contentHeight - viewportHeight));
			} else {
				scroll = 0;
			}
			return true;
		}

		if (draggingEntry >= 0 && draggingEntry < filtered.size()) {
			RuleEntry e = filtered.get(draggingEntry);
			int left = padding;
			int right = this.width - padding;
			int previewSize = 52;
			int slidersX = left + previewSize + 10;
			int contentWidth = (right - 16) - left;
			int slidersW = contentWidth - previewSize - 8 - 120;
			if (draggingMode == DragMode.HUE) updateHueFromMouse(e, mouseX, slidersX, slidersW);
			else if (draggingMode == DragMode.SAT) updateSatFromMouse(e, mouseX, slidersX, slidersW);
			else if (draggingMode == DragMode.VAL) updateValFromMouse(e, mouseX, slidersX, slidersW);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			if (searchFocused && searchOpen && !getCurrentSuggestions().isEmpty()) {
				int idx = suggestionSelected >= 0 ? suggestionSelected : 0;
				List<String> suggest = getCurrentSuggestions();
				if (idx < suggest.size()) {
					String chosen = suggest.get(idx);
					searchQuery = chosen;
					caretIndex = chosen.length();
					recomputeFiltered();
					for (int i = 0; i < filtered.size(); i++) {
						if (filtered.get(i).name.equals(chosen)) {
							scroll = i * entryHeight;
							int contentH = filtered.size() * entryHeight;
							int viewport = this.height - 160;
							scroll = MathHelper.clamp(scroll, 0, Math.max(0, contentH - viewport));
							break;
						}
					}
				}
				searchOpen = false;
				searchFocused = false;
				return true;
			}
			if (!searchFocused) {
				if (hoveredEntry >= 0 && hoveredEntry < filtered.size()) {
					RuleEntry e = filtered.get(hoveredEntry);
					int rgb = hsvToRgbInt(e.hue, e.sat, e.val);
					int argb = 0xFF000000 | (rgb & 0xFFFFFF);
					try { LUADefaults.setColorByRuleName(e.name, argb); } catch (Throwable t) {}
					e.currentColor = argb;
					return true;
				}
			}
		}

		if (searchOpen && searchFocused) {
			if (keyCode == GLFW.GLFW_KEY_UP) {
				List<String> sug = getCurrentSuggestions();
				if (!sug.isEmpty()) {
					suggestionSelected = Math.max(0, (suggestionSelected <= 0) ? (sug.size() - 1) : (suggestionSelected - 1));
				}
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_DOWN) {
				List<String> sug = getCurrentSuggestions();
				if (!sug.isEmpty()) {
					suggestionSelected = Math.min(sug.size() - 1, suggestionSelected + 1);
				}
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_LEFT) {
				caretIndex = Math.max(0, caretIndex - 1);
				caretBlinkStartMs = System.currentTimeMillis();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_RIGHT) {
				caretIndex = Math.min(searchQuery.length(), caretIndex + 1);
				caretBlinkStartMs = System.currentTimeMillis();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_HOME) {
				caretIndex = 0; caretBlinkStartMs = System.currentTimeMillis(); return true;
			}
			if (keyCode == GLFW.GLFW_KEY_END) {
				caretIndex = searchQuery.length(); caretBlinkStartMs = System.currentTimeMillis(); return true;
			}
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				if (caretIndex > 0 && !searchQuery.isEmpty()) {
					searchQuery = searchQuery.substring(0, caretIndex - 1) + searchQuery.substring(caretIndex);
					caretIndex = Math.max(0, caretIndex - 1);
					recomputeFiltered();
					caretBlinkStartMs = System.currentTimeMillis();
				}
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_DELETE) {
				if (caretIndex < searchQuery.length()) {
					searchQuery = searchQuery.substring(0, caretIndex) + searchQuery.substring(caretIndex + 1);
					recomputeFiltered();
					caretBlinkStartMs = System.currentTimeMillis();
				}
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				searchFocused = false;
				searchOpen = false;
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (searchOpen && searchFocused) {
			if (chr >= 32 && chr != 127) {
				if (searchQuery.length() < 512) {
					searchQuery = searchQuery.substring(0, caretIndex) + chr + searchQuery.substring(caretIndex);
					caretIndex++;
					recomputeFiltered();
					caretBlinkStartMs = System.currentTimeMillis();
				}
			}
			return true;
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public void close() {
		try {
			long handle = this.client.getWindow().getHandle();
			if (glfwScrollCallback != null) {
				glfwScrollCallback.free();
				glfwScrollCallback = null;
			}
			if (prevGlfwScrollCallback != null) {
				GLFW.glfwSetScrollCallback(handle, prevGlfwScrollCallback);
				prevGlfwScrollCallback = null;
			}
		} catch (Throwable ignored) {}
		super.close();
	}

	private List<String> getCurrentSuggestions() {
		List<String> suggestions = new ArrayList<>();
		if (!searchQuery.isEmpty()) {
			String qlow = searchQuery.toLowerCase();
			for (RuleEntry r : entries) {
				if (r.name.toLowerCase().contains(qlow)) {
					suggestions.add(r.name);
					if (suggestions.size() >= 8) break;
				}
			}
		}
		return suggestions;
	}

	private void updateHueFromMouse(RuleEntry e, double mouseX, int hueX, int hueW) {
		float t = (float) ((mouseX - hueX) / (double) hueW);
		t = Math.max(0f, Math.min(1f, t));
		e.hue = t * 360f;
	}
	private void updateSatFromMouse(RuleEntry e, double mouseX, int satX, int satW) {
		float t = (float) ((mouseX - satX) / (double) satW);
		t = Math.max(0f, Math.min(1f, t));
		e.sat = t;
	}
	private void updateValFromMouse(RuleEntry e, double mouseX, int valX, int valW) {
		float t = (float) ((mouseX - valX) / (double) valW);
		t = Math.max(0f, Math.min(1f, t));
		e.val = t;
	}

	private int getCurrentColorFromLUADefaults(String name, int fallback) {
		try {
			Field fn = findFieldIgnoreCase(LUADefaults.class, "ruleNames");
			Field fc = findFieldIgnoreCase(LUADefaults.class, "groupColors");
			if (fn != null && fc != null) {
				fn.setAccessible(true);
				fc.setAccessible(true);
				List<String> names = (List<String>) fn.get(null);
				List<Integer> cols = (List<Integer>) fc.get(null);
				int idx = names.indexOf(name);
				if (idx >= 0 && idx < cols.size()) return cols.get(idx);
			}
		} catch (Throwable t) { }
		return fallback;
	}

	private class RuleEntry {
		final String name;
		int currentColor;
		float hue;
		float sat;
		float val;

		float applyHoverProg = 0f;
		float resetHoverProg = 0f;

		RuleEntry(String name, int argb) {
			this.name = name;
			this.currentColor = argb;
			float[] hsv = rgbToHsv(argb);
			this.hue = hsv[0]; this.sat = hsv[1]; this.val = hsv[2];
		}

		void render(DrawContext dc, int x, int y, int w, int h, int mouseX, int mouseY) {
			dc.fill(x, y, x + w, y + h - 6, 0xFF1B1B1B);
			dc.fill(x, y + h - 6, x + w, y + h - 4, 0xFF000000);

			int previewSize = 52;
			int px = x + 6;
			int py = y + 8;
			drawPreview(dc, px, py, previewSize, previewSize, 0xFF000000 | (hsvToRgbInt(hue, sat, val) & 0xFFFFFF));

			int nameX = px + previewSize + 8;
			int nameY = py + (previewSize - textRenderer.fontHeight) / 4 - 15;
			dc.drawText(textRenderer, Text.literal(this.name), nameX, nameY, 0xFFFFFFFF, false);

			int slidersX = px + previewSize + 8;
			int slidersW = w - previewSize - 8 - 120;

			int hueX = slidersX;
			int hueY = py + 12;
			int hueW = slidersW;
			int hueH = 10;
			renderHueGradient(dc, hueX, hueY, hueW, hueH);
			int knobX = hueX + MathHelper.floor((hue / 360f) * (Math.max(1, hueW - 1)));
			dc.fill(knobX - 2, hueY - 2, knobX + 3, hueY + hueH + 2, 0xFF000000);
			dc.fill(knobX - 1, hueY - 1, knobX + 2, hueY + hueH + 1, 0xFFFFFFFF);

			int satX = hueX;
			int satY = hueY + 16;
			int satH = 10;
			renderSaturationGradient(dc, satX, satY, hueW, satH, hue);
			int satKnobX = satX + MathHelper.floor(sat * (Math.max(1, hueW - 1)));
			dc.fill(satKnobX - 2, satY - 2, satKnobX + 3, satY + satH + 2, 0xFF000000);
			dc.fill(satKnobX - 1, satY - 1, satKnobX + 2, satY + satH + 1, 0xFFFFFFFF);

			int valX = hueX;
			int valY = satY + 16;
			int valH = 10;
			renderValueGradient(dc, valX, valY, hueW, valH, hue, sat);
			int valKnobX = valX + MathHelper.floor(val * (Math.max(1, hueW - 1)));
			dc.fill(valKnobX - 2, valY - 2, valKnobX + 3, valY + valH + 2, 0xFF000000);
			dc.fill(valKnobX - 1, valY - 1, valKnobX + 2, valY + valH + 1, 0xFFFFFFFF);

			int btnX = x + w - 100;
			int applyY = py + 12;
			int btnW = 48; int btnH = 16;
			boolean applyHover = (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= applyY && mouseY <= applyY + btnH);
			boolean resetHover = false;

			float step = 0.12f;
			applyHoverProg += (applyHover ? step : -step);
			if (applyHoverProg < 0f) applyHoverProg = 0f;
			if (applyHoverProg > 1f) applyHoverProg = 1f;

			int applyCol = lerpColor(0xFF3465A4, 0xFF3F7FC6, applyHoverProg);
			dc.fill(btnX - 1, applyY - 1, btnX + btnW + 1, applyY + btnH + 1, 0xFF000000);
			dc.fill(btnX, applyY, btnX + btnW, applyY + btnH, applyCol);
			int applyTextY = applyY + (btnH - textRenderer.fontHeight) / 2;
			int applyTextW = textRenderer.getWidth("Apply");
			int applyTextX = btnX + (btnW - applyTextW) / 2;
			dc.drawText(textRenderer, Text.literal("Apply"), applyTextX, applyTextY, 0xFFFFFFFF, false);

			int resetY = applyY + 22;
			resetHover = (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= resetY && mouseY <= resetY + btnH);
			resetHoverProg += (resetHover ? step : -step);
			if (resetHoverProg < 0f) resetHoverProg = 0f;
			if (resetHoverProg > 1f) resetHoverProg = 1f;
			int resetCol = lerpColor(0xFF555555, 0xFF6F6F6F, resetHoverProg);
			dc.fill(btnX - 1, resetY - 1, btnX + btnW + 1, resetY + btnH + 1, 0xFF000000);
			dc.fill(btnX, resetY, btnX + btnW, resetY + btnH, resetCol);
			int resetTextY = resetY + (btnH - textRenderer.fontHeight) / 2;
			int resetTextW = textRenderer.getWidth("Reset");
			int resetTextX = btnX + (btnW - resetTextW) / 2;
			dc.drawText(textRenderer, Text.literal("Reset"), resetTextX, resetTextY, 0xFFFFFFFF, false);

			int hex = 0xFF000000 | (hsvToRgbInt(hue, sat, val) & 0xFFFFFF);
			String hexStr = String.format("#%06X", hex & 0xFFFFFF);
			int hexW = textRenderer.getWidth(hexStr);
			int hexX = btnX - 6 - hexW;
			int hexY = py + 2;
			dc.drawText(textRenderer, Text.literal(hexStr), hexX, hexY, 0xFFFFFFFF, false);

			if (DEBUG) {
				dc.fill(nameX - 1, nameY - 1, nameX + 1, nameY + 1, 0xFFFF00FF);
				dc.drawText(textRenderer, Text.literal("x=" + nameX + " y=" + nameY), nameX + 6, nameY - 2, 0xFF00FF00, false);
			}
		}

		private void drawPreview(DrawContext dc, int x, int y, int w, int h, int argb) {
			dc.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
			for (int yy = y; yy < y + h; yy += 4) {
				for (int xx = x; xx < x + w; xx += 4) {
					int checker = ((xx + yy) / 4) % 2 == 0 ? 0xFFBBBBBB : 0xFFFFFFFF;
					dc.fill(xx, yy, xx + 4, yy + 4, checker);
				}
			}
			dc.fill(x, y, x + w, y + h, argb);
		}

		private void renderHueGradient(DrawContext dc, int x, int y, int w, int h) {
			for (int i = 0; i < w; i++) {
				float t = i / (float) Math.max(1, w - 1);
				int rgb = hsvToRgbInt(t * 360f, 1f, 1f);
				dc.fill(x + i, y, x + i + 1, y + h, 0xFF000000 | (rgb & 0xFFFFFF));
			}
		}

		private void renderSaturationGradient(DrawContext dc, int x, int y, int w, int h, float hue) {
			for (int i = 0; i < w; i++) {
				float t = i / (float) Math.max(1, w - 1);
				int rgb = hsvToRgbInt(hue, t, 1f);
				dc.fill(x + i, y, x + i + 1, y + h, 0xFF000000 | (rgb & 0xFFFFFF));
			}
		}

		private void renderValueGradient(DrawContext dc, int x, int y, int w, int h, float hue, float sat) {
			for (int i = 0; i < w; i++) {
				float t = i / (float) Math.max(1, w - 1);
				int rgb = hsvToRgbInt(hue, sat, t);
				dc.fill(x + i, y, x + i + 1, y + h, 0xFF000000 | (rgb & 0xFFFFFF));
			}
		}
	}

	private static int hsvToRgbInt(float h, float s, float v) {
		float c = v * s;
		float hp = h / 60f;
		float x = c * (1f - Math.abs(hp % 2f - 1f));
		float r=0,g=0,b=0;
		if (0 <= hp && hp < 1) { r = c; g = x; b = 0; }
		else if (1 <= hp && hp < 2) { r = x; g = c; b = 0; }
		else if (2 <= hp && hp < 3) { r = 0; g = c; b = x; }
		else if (3 <= hp && hp < 4) { r = 0; g = x; b = c; }
		else if (4 <= hp && hp < 5) { r = x; g = 0; b = c; }
		else { r = c; g = 0; b = x; }
		float m = v - c;
		int ri = Math.round((r + m) * 255f);
		int gi = Math.round((g + m) * 255f);
		int bi = Math.round((b + m) * 255f);
		ri = clamp8(ri); gi = clamp8(gi); bi = clamp8(bi);
		return (ri << 16) | (gi << 8) | bi;
	}

	private static float[] rgbToHsv(int argb) {
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		float rf = r / 255f, gf = g / 255f, bf = b / 255f;
		float max = Math.max(rf, Math.max(gf, bf));
		float min = Math.min(rf, Math.min(gf, bf));
		float d = max - min;
		float h = 0f;
		if (d != 0f) {
			if (max == rf) h = 60f * (((gf - bf) / d) % 6f);
			else if (max == gf) h = 60f * (((bf - rf) / d) + 2f);
			else h = 60f * (((rf - gf) / d) + 4f);
		}
		if (h < 0) h += 360f;
		float s = (max == 0f) ? 0f : d / max;
		float v = max;
		return new float[] { h, s, v };
	}

	private static int clamp8(int v) {
		return Math.max(0, Math.min(255, v));
	}

	private static int lerpColor(int a, int b, float t) {
		if (t <= 0f) return a;
		if (t >= 1f) return b;
		t = Math.max(0f, Math.min(1f, t));
		int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		int ra = Math.round(aa + (ba - aa) * t);
		int rr = Math.round(ar + (br - ar) * t);
		int rg = Math.round(ag + (bg - ag) * t);
		int rb = Math.round(ab + (bb - ab) * t);
		return (ra << 24) | (rr << 16) | (rg << 8) | rb;
	}
}
