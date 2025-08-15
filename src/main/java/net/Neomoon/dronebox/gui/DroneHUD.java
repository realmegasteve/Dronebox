package net.Neomoon.dronebox.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.List;

public class DroneHUD {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final Map<Integer, NativeImageBackedTexture> droneCams = new HashMap<>();

	private static Identifier getDroneIdentifier(int slot) {
		return Identifier.of("dronebox", "drone_cam_" + slot);
	}

	public static void renderHUD(MatrixStack matrices, DrawContext ctx) {
		int[][] SCREEN_POSITIONS = {
			{10, 10}, {148, 10}, {286, 10},
			{10, 148}, {148, 148}, {286, 148}
		};
		int SCREEN_WIDTH = 128;
		int SCREEN_HEIGHT = 128;

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
			NativeImageBackedTexture wrapped = new NativeImageBackedTexture((Supplier<String>) () -> id.toString(), tex.getImage());
			manager.registerTexture(id, wrapped);
		}
	}

	public static void drawTextureCompat(DrawContext ctx, Identifier texId,
										 int x, int y, int width, int height,
										 int textureWidth, int textureHeight) {
		try {
			Method[] methods = ctx.getClass().getMethods();
			for (Method m : methods) {
				if (!m.getName().equals("drawTexture")) continue;
				Class<?>[] params = m.getParameterTypes();
				Object[] args = new Object[params.length];
				boolean ok = true;
				int[] ints = new int[]{x, y, width, height, textureWidth, textureHeight};
				int intIdx = 0;
				float[] floats = new float[]{0f, 0f};
				int floatIdx = 0;

				for (int pi = 0; pi < params.length; pi++) {
					Class<?> p = params[pi];
					if (pi == 0 && p.getName().contains("RenderPipeline")) {
						try {
							Class<?> renderPipelinesClass = Class.forName("com.mojang.blaze3d.pipeline.RenderPipelines");
							Method getAll = renderPipelinesClass.getMethod("getAll");
							Object listObj = getAll.invoke(null);
							@SuppressWarnings("unchecked")
							List<?> pipelines = (List<?>) listObj;
							Object pipeline = pipelines.isEmpty() ? null : pipelines.get(0);
							args[pi] = pipeline;
							continue;
						} catch (Throwable t) {
							args[pi] = null;
							continue;
						}
					}
					if (Identifier.class.isAssignableFrom(p)) {
						args[pi] = texId;
						continue;
					}
					if (p == float.class || p == Float.class) {
						if (floatIdx < floats.length) {
							args[pi] = floats[floatIdx++];
						} else {
							args[pi] = 0f;
						}
						continue;
					}
					if (p == int.class || p == Integer.class) {
						if (intIdx < ints.length) {
							args[pi] = ints[intIdx++];
						} else {
							args[pi] = 0;
						}
						continue;
					}
					if (p == boolean.class || p == Boolean.class) {
						args[pi] = false;
						continue;
					}
					try {
						Class<?> matrixStackClass = Class.forName("net.minecraft.client.util.math.MatrixStack");
						if (matrixStackClass.isAssignableFrom(p)) {
							try {
								Method gm = ctx.getClass().getMethod("getMatrices");
								Object ms = gm.invoke(ctx);
								args[pi] = ms;
								continue;
							} catch (Throwable ignored) {}
						}
					} catch (ClassNotFoundException ignored) {}
					args[pi] = null;
				}
				try {
					m.invoke(ctx, args);
					return;
				} catch (IllegalArgumentException iae) {
					continue;
				} catch (Throwable t) {
					continue;
				}
			}
		} catch (Throwable ex) {}
	}

	public static NativeImageBackedTexture loadTextureFromResource(Identifier id) {
		try {
			Optional<Resource> opt = mc.getResourceManager().getResource(id);
			if (opt.isPresent()) {
				Resource res = opt.get();
				try (InputStream is = res.getInputStream()) {
					NativeImage img = NativeImage.read(is);
					return new NativeImageBackedTexture((Supplier<String>) () -> id.toString(), img);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void updateDroneImage(int slot, NativeImage newImage) {
		if (newImage == null) return;
		droneCams.put(slot, new NativeImageBackedTexture((Supplier<String>) () -> "drone_cam_" + slot, newImage));
	}
}
