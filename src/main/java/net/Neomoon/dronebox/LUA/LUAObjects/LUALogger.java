package net.Neomoon.dronebox.LUA.LUAObjects;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.luaj.vm2.ast.Str;

public class LUALogger {
	private final PlayerEntity target;
	public LUALogger(PlayerEntity player) {
		target = player;
	}

	public void print(String t){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(" [Drone]")), false);
		}
	}

	public void print(String t, String t2){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(t2).concat(" [Drone]")), false);
		}
	}

	public void print(String t, String t2, String t3){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(t2).concat(t3).concat(" [Drone]")), false);
		}
	}

	public void print(double n){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(String.valueOf(n).concat(" [Drone]")), false);
		}
	}

	public void log(String t){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(" [Drone]")), false);
		}
	}

	public void log(String t, String t2){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(t2).concat(" [Drone]")), false);
		}
	}

	public void log(String t, String t2, String t3){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(t2).concat(t3).concat(" [Drone]")), false);
		}
	}

	public void log(double n){
		if (target.getWorld().isClient) {
		target.sendMessage(Text.of(String.valueOf(n).concat(" [Drone]")), false);
		}
	}

	public void warn(String t){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(" [Drone WARNING]")), false);
		}
	}

	public void warn(String t, String t2){
		if (target.getWorld().isClient) {
		target.sendMessage(Text.of(t.concat(t2).concat(" [Drone WARNING]")), false);
		}
	}

	public void warn(String t, String t2, String t3){
		if (target.getWorld().isClient) {		target.sendMessage(Text.of(t.concat(t2).concat(t3).concat(" [Drone WARNING]")), false);
}}

	public void warn(double n){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(String.valueOf(n).concat(" [Drone WARNING]")), false);
		}
	}

	public void error(String t){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(" [Drone ERROR]")), false);
		}
	}

	public void error(String t, String t2){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(t2).concat(" [Drone ERROR]")), false);
		}
	}

	public void error(String t, String t2, String t3){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(t.concat(t2).concat(t3).concat(" [Drone ERROR]")), false);
		}
	}

	public void error(double n){
		if (target.getWorld().isClient) {
			target.sendMessage(Text.of(String.valueOf(n).concat(" [Drone ERROR]")), false);
		}
	}
}
