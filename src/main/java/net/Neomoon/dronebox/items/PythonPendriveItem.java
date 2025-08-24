package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.Drone;
import net.Neomoon.dronebox.LUA.LuaIDE;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PythonPendriveItem extends Item {

	public PythonPendriveItem(Settings settings) {
		super(settings);
	}
	private Drone linkedDrone = null;

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (player.isSneaking()) {
			ItemStack stack = player.getStackInHand(hand);
			MinecraftClient.getInstance().setScreen(new LuaIDE(Text.empty(), stack, linkedDrone));
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof Drone drone) {
			if (player.isSneaking()) {
				player.sendMessage(Text.literal("Pendrive linked to drone."), true);
				linkedDrone = drone;
			} else {
				player.sendMessage(Text.literal("Code written to drone."), true);
				writeCode(stack, drone);
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	private void writeCode(ItemStack drive, Drone drone) {
		// Load code from the drive
		NbtComponent comp = drive.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		String loadedCode = root.getString("code", "");
		drone.loadPythonScript(loadedCode);
	}
}
