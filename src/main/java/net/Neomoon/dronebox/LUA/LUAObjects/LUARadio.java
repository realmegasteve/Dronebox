package net.Neomoon.dronebox.LUA.LUAObjects;

import net.Neomoon.dronebox.Radio;

public class LUARadio {
	 public void sendSignal(String channel, double value){
		Radio.sendSignal(channel, value);
	}
	 public double readOrDefault(String channel, double value){
		return Radio.readOrDefault(channel, value);
	}
	 public double read(String channel){
		return Radio.read(channel);
	}
	 public boolean signalExist(String channel){
		return Radio.signalExist(channel);
	}
}
