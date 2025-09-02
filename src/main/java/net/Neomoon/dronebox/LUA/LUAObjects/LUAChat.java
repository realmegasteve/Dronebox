package net.Neomoon.dronebox.LUA.LUAObjects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.collection.ArrayListDeque;

import java.util.Arrays;

public class LUAChat {
	 public String last(){
		 ArrayListDeque<String> history = MinecraftClient.getInstance().inGameHud.getChatHud().getMessageHistory();
		 if (history.isEmpty()) {
			 return "";
		 }
		 return history.getLast();
	}

	public String getMessage(int index){
		ArrayListDeque<String> history = MinecraftClient.getInstance().inGameHud.getChatHud().getMessageHistory();
		if (history.isEmpty()) {
			return "";
		}
		return history.get(history.getArrayLength() - 1 - index);
	}
}
