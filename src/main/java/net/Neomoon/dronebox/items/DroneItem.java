package net.Neomoon.dronebox.items;

import net.Neomoon.dronebox.CentralDroneInit;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DroneItem extends Item {
	public DroneItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			BlockHitResult hit = (BlockHitResult) user.raycast(5.0D, 0, false);
			BlockPos spawnPos = hit.getBlockPos().up();

			CentralDroneInit.DRONE_ENTITY_TYPE.spawn(
				(ServerWorld) world,
				spawnPos,
				SpawnReason.SPAWN_ITEM_USE
			);

			if (!user.getAbilities().creativeMode) {
				stack.decrement(1);
			}
		}
		return ActionResult.SUCCESS;
	}
}
