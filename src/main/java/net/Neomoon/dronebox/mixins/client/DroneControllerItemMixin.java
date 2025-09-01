package net.Neomoon.dronebox.mixins.client;

import net.Neomoon.dronebox.client.gui.DroneControlScreen;
import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.network.ToggleC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DroneControllerItem.class)
public abstract class DroneControllerItemMixin {

	@Inject(method = "use", at = @At("HEAD"))
	private void onUseClient(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient) {
			return;
		}
		if (player.isSneaking()) {
			MinecraftClient.getInstance().execute(() ->
				MinecraftClient.getInstance().setScreen(new DroneControlScreen(stack))
			);

		} else {
			List<String> linkedDrones = DroneControllerItem.getLinkedDroneUUIDs(stack);
			if (!linkedDrones.isEmpty()) {
				for (String droneUUID : linkedDrones) {
					ToggleC2SPayload payload = new ToggleC2SPayload(droneUUID);
					ClientPlayNetworking.send(payload);
				}
				player.sendMessage(Text.literal("Toggled all linked drones."), true);
			} else {
				player.sendMessage(Text.literal("No linked drones to toggle."), true);
			}
		}
	}
}
