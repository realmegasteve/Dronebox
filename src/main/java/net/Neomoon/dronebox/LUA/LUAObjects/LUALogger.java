package net.Neomoon.dronebox.LUA.LUAObjects;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class LUALogger {
	PlayerEntity target;
	public LUALogger(PlayerEntity player) {
		target = player;
	}

	public void print(String t){
		target.sendMessage(Text.of(t.concat(" [Drone]")), false);
	}
}
