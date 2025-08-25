package net.Neomoon.dronebox.LUA.LUAObjects;

import net.Neomoon.dronebox.Radio;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaString;

public class LUARadio {
	 public void sendSignal(LuaString channel, LuaDouble value){
		Radio.sendSignal(channel.tojstring(), value.todouble());
	}
	 public double readOrDefault(LuaString channel, LuaDouble value){
		return Radio.readOrDefault(channel.tojstring(), value.todouble());
	}
	 public double read(LuaString channel){
		return Radio.read(channel.tojstring());
	}
	 public boolean signalExist(LuaString channel){
		return Radio.signalExist(channel.tojstring());
	}
}
