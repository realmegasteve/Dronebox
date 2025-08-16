package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.Drone;
import net.Neomoon.dronebox.python.MinecraftPythonInterpreter;
import net.Neomoon.dronebox.python.PythonIDE;
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
	MinecraftPythonInterpreter py = new MinecraftPythonInterpreter().init();

	public PythonPendriveItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
			if (player.isSneaking()) {
				MinecraftClient.getInstance().setScreen(new PythonIDE(Text.empty(), player.getStackInHand(hand)));
			}
		return ActionResult.SUCCESS;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof Drone drone) {
			ItemStack stack2 = player.getStackInHand(hand);
			writeCode(stack2, drone);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	private void writeCode(ItemStack drive, Drone drone){
		//load drive code
		NbtComponent comp2 = drive.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root2 = comp2.copyNbt();
		String loadedCode = root2.getString("code", "");

		drone.loadPythonScript(loadedCode);
	}
}
