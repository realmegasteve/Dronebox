package net.Neomoon.dronebox.python.PythonObjects;

import net.Neomoon.dronebox.Radio;

public class PYRadio {
	static public void sendSignal(String channel, double value){
		Radio.sendSignal(channel, value);
	}
	static public double readOrDefault(String channel, double value){
		return Radio.readOrDefault(channel, value);
	}
	static public double read(String channel){
		return Radio.read(channel);
	}
	static public boolean signalExist(String channel){
		return Radio.signalExist(channel);
	}
}
