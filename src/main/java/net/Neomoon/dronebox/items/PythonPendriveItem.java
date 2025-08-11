package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.python.MinecraftPythonInterpreter;
import net.Neomoon.dronebox.python.PythonIDE;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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

		MinecraftClient.getInstance().setScreen(new PythonIDE(Text.empty(), player.getStackInHand(hand)));

		return ActionResult.SUCCESS;
	}

}
