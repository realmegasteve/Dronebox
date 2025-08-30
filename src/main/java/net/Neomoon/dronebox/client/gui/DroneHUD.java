package net.Neomoon.dronebox.client.gui;

import net.Neomoon.dronebox.client.CameraManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DroneHUD {
	private static boolean toggle = false;
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final Map<Integer, NativeImageBackedTexture> droneCams = new HashMap<>();

	private static Identifier getDroneIdentifier(int slot) {
		return Identifier.of("dronebox", "drone_cam_" + slot);
	}

	public static void renderHUD(DrawContext ctx) {
		if (CameraManager.DroneCamera) {
			return;
		}
		if (!toggle) return;
		int SCREEN_WIDTH = 70;
		int SCREEN_HEIGHT = 70;

		int GAP_X = 50;
		int GAP_Y = 20;

		int ROW_SPACING = SCREEN_HEIGHT + 20;

		int[][] SCREEN_POSITIONS = {
			{10, 10}, {400, 10},
			{10, 10 + ROW_SPACING}, {400, 10 + ROW_SPACING},
			{10, 10 + ROW_SPACING * 2}, {400, 10 + ROW_SPACING * 2}
		};

		for (int i = 0; i < 6; i++) {
			int x = SCREEN_POSITIONS[i][0];
			int y = SCREEN_POSITIONS[i][1];

			NativeImageBackedTexture tex = droneCams.get(i);
			if (tex == null) {
				ctx.fill(x, y, x + SCREEN_WIDTH, y + SCREEN_HEIGHT, 0xFF000000);
				ctx.drawText(mc.textRenderer, Text.literal("No signal found"),
					x + 10, y + SCREEN_HEIGHT / 2 - 5, 0xFFFFFFFF, true);
			} else {
				Identifier texId = getDroneIdentifier(i);
				registerTextureIfMissing(texId, tex);
				drawTextureCompat(ctx, texId, x, y, SCREEN_WIDTH, SCREEN_HEIGHT,
					tex.getImage().getWidth(), tex.getImage().getHeight());
			}
			ctx.drawBorder(x, y, SCREEN_WIDTH, SCREEN_HEIGHT, 0xFFFFFFFF);
		}
	}

	private static void registerTextureIfMissing(Identifier id, NativeImageBackedTexture tex) {
		TextureManager manager = mc.getTextureManager();
		if (manager.getTexture(id) == null) {
			NativeImageBackedTexture wrapped = new NativeImageBackedTexture(id::toString, tex.getImage());
			manager.registerTexture(id, wrapped);
		}
	}

	public static void drawTextureCompat(DrawContext ctx, Identifier texId,
										 int x, int y, int width, int height,
										 int textureWidth, int textureHeight) {
		// TODO: find and use the correct pipeline. GUI_TEXTURED is likely what you want
		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, texId, x, y, 0f, 0f, width, height, textureWidth, textureHeight);
	}

	public static NativeImageBackedTexture loadTextureFromResource(Identifier id) {
		try {
			Optional<Resource> opt = mc.getResourceManager().getResource(id);
			if (opt.isPresent()) {
				Resource res = opt.get();
				try (InputStream is = res.getInputStream()) {
					NativeImage img = NativeImage.read(is);
					return new NativeImageBackedTexture(id::toString, img);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void updateDroneImage(int slot, NativeImage newImage) {
		if (newImage == null) return;
		droneCams.put(slot, new NativeImageBackedTexture(() -> "drone_cam_" + slot, newImage));
	}
}
