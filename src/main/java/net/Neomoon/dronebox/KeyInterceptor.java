package net.Neomoon.dronebox;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.MoveC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class KeyInterceptor {
	private static KeyBinding keyForward;
	private static KeyBinding keyBack;
	private static KeyBinding keyLeft;
	private static KeyBinding keyRight;
	private static KeyBinding keyJump;
	private static KeyBinding keySneak;
	private static KeyBinding keyYawLeft;
	private static KeyBinding keyYawRight;

	public static void register() {
		keyForward = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.forward", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_W, "category.dronecontrol"));
		keyBack = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.back", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_S, "category.dronecontrol"));
		keyLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_A, "category.dronecontrol"));
		keyRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_D, "category.dronecontrol"));
		keyJump = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.jump", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, "category.dronecontrol"));
		keySneak = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.sneak", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, "category.dronecontrol"));
		keyYawLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.yaw_left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Q, "category.dronecontrol"));
		keyYawRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.dronecontrol.yaw_right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, "category.dronecontrol"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			var held = client.player.getMainHandStack();
			if (!(held.getItem() instanceof DroneControllerItem)) return;

			float forward = 0f;
			float strafe = 0f;
			boolean jump = false;
			boolean sneak = false;
			float yawDelta = 0f;

			if (keyForward.isPressed()) forward += 1f;
			if (keyBack.isPressed()) forward -= 1f;
			if (keyLeft.isPressed()) strafe -= 1f;
			if (keyRight.isPressed()) strafe += 1f;
			if (keyJump.isPressed()) jump = true;
			if (keySneak.isPressed()) sneak = true;
			if (keyYawLeft.isPressed()) yawDelta -= 5f;
			if (keyYawRight.isPressed()) yawDelta += 5f;

			if (forward != 0 || strafe != 0 || jump || sneak || yawDelta != 0) {
				List<String> droneUUIDs = DroneControllerItem.getLinkedDroneUUIDs(held);
				for (String uuidStr : droneUUIDs) {
					UUID uuid = UUID.fromString(uuidStr);

					MoveC2SPayload p = new MoveC2SPayload(uuid.toString(), forward, strafe, jump, sneak, yawDelta);

					ClientPlayNetworking.send(p);
				}
			}
		});
	}
}
