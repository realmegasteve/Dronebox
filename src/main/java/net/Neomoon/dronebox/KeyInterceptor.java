package net.Neomoon.dronebox;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.MoveC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// TODO: prevent this from interfering with default vanilla keybinds
public class KeyInterceptor {

	public static final Set<KeyBinding> BINDINGS = new HashSet<>();

	private static KeyBinding keyForward;
	private static KeyBinding keyBack;
	private static KeyBinding keyLeft;
	private static KeyBinding keyRight;
	private static KeyBinding keyJump;
	private static KeyBinding keySneak;
	private static KeyBinding keyYawLeft;
	private static KeyBinding keyYawRight;

	public static void register() {
		keyForward = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.forward", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_W, "category.dronecontrol"));
		keyBack = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.back", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_S, "category.dronecontrol"));
		keyLeft = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_A, "category.dronecontrol"));
		keyRight = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_D, "category.dronecontrol"));
		keyJump = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.jump", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, "category.dronecontrol"));
		keySneak = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.sneak", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, "category.dronecontrol"));
		keyYawLeft = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.yaw_left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Q, "category.dronecontrol"));
		keyYawRight = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding("key.dronecontrol.yaw_right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, "category.dronecontrol"));

		BINDINGS.add(keyForward);
		BINDINGS.add(keyBack);
		BINDINGS.add(keyLeft);
		BINDINGS.add(keyRight);
		BINDINGS.add(keyJump);
		BINDINGS.add(keySneak);
		BINDINGS.add(keyYawLeft);
		BINDINGS.add(keyYawRight);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) return;
			var held = client.player.getMainHandStack();
			if (!(held.getItem() instanceof DroneControllerItem)) return;

			float forward = 0f;
			float strafe = 0f;
			boolean jump = false;
			boolean sneak = false;
			float yawDelta = 0f;

			if (keyForward.isPressed()) forward += 1f;
			if (keyBack.isPressed()) forward -= 1f;
			if (keyLeft.isPressed()) strafe += 1f;
			if (keyRight.isPressed()) strafe -= 1f;
			if (keyJump.isPressed()) jump = true;
			if (keySneak.isPressed()) sneak = true;
			if (keyYawLeft.isPressed()) yawDelta += 0.5f;
			if (keyYawRight.isPressed()) yawDelta -= 0.5f;

			if (forward != 0 || strafe != 0 || jump || sneak || yawDelta != 0) {
				List<String> droneUUIDs = DroneControllerItem.getLinkedDroneUUIDs(held);
				for (String uuidStr : droneUUIDs) {
					if (!DroneControllerItem.isDroneControlEnabled(held, uuidStr)) continue;

					UUID uuid;
					try {
						uuid = UUID.fromString(uuidStr);
					} catch (IllegalArgumentException e) {
						continue;
					}

					MoveC2SPayload p = new MoveC2SPayload(uuid.toString(), forward, strafe, jump, sneak, yawDelta);
					ClientPlayNetworking.send(p);

					Entity e = client.world.getEntity(uuid);
					if (e instanceof Drone drone) {
						double up2 = (jump? 1 : 0) + (sneak? -1 : 0);
						drone.controllerMovementInput(forward, strafe, up2, yawDelta);
					}
				}
			}
		});
	}

	public static class MultiKeyBinding extends KeyBinding {

		protected final Set<KeyBinding> others = new HashSet<>();

		@SuppressWarnings("unused")
		public MultiKeyBinding(String translationKey, int code, String category) {
			super(translationKey, code, category);
		}

		public MultiKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
			super(translationKey, type, code, category);
		}

		@Override
		public void setPressed(boolean pressed) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null) {
				super.setPressed(pressed);
				this.others.forEach(keyBinding -> keyBinding.setPressed(pressed));
				return;
			}
			var held = client.player.getMainHandStack();
			if (!(held.getItem() instanceof DroneControllerItem)) {
				this.others.forEach(keyBinding -> keyBinding.setPressed(pressed));
				return;
			}
			super.setPressed(pressed);
		}

		public void addOther(KeyBinding keyBinding) {
			this.others.add(keyBinding);
		}

		public void clearOthers() {
			this.others.clear();
		}

		public void addAll(Collection<? extends KeyBinding> keyBindings) {
			this.others.addAll(keyBindings);
		}
	}
}
