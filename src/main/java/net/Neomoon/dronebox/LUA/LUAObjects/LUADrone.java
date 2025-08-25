package net.Neomoon.dronebox.LUA.LUAObjects;

import net.Neomoon.dronebox.Drone;
import net.minecraft.util.math.Vec3d;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LUADrone {
	Drone drone;
	public double max = 0.6;
	public LUADrone(Drone inDrone) {
		drone = inDrone;
	}

	public void accelerate(double X, double Y, double Z){
		drone.setVelocity(drone.getVelocity().add(new Vec3d(Math.min(Math.max(X, -max),max), Math.min(Math.max(Y, -max),max), Math.min(Math.max(Z, -max),max))));
	}

	public void setSpeed(double X, double Y, double Z){
		drone.setVelocity(new Vec3d(X, Y, Z));
	}

	public void setAccessory(boolean state){
		drone.setAcessory(state);
	}

	public void accelerateTurning(double yaw, double pitch, double roll){
		drone.yawRate += yaw;
		drone.rollRate += roll;
		drone.pitchRate += pitch;
	}

	//Position getters
	public double getX(){ return drone.getX(); }
	public double getY(){ return drone.getY(); }
	public double getZ(){ return drone.getZ(); }
	public LuaTable getPos() {
		LuaTable table = LuaValue.tableOf();
		table.set(1, CoerceJavaToLua.coerce(drone.getPos().x));
		table.set(2, CoerceJavaToLua.coerce(drone.getPos().y));
		table.set(3, CoerceJavaToLua.coerce(drone.getPos().z));
		return table;
	}
	//Speed getters
	public double getXSpeed(){ return drone.getVelocity().x; }
	public double getYSpeed(){ return drone.getVelocity().y; }
	public double getZSpeed(){ return drone.getVelocity().z; }
	public LuaTable getSpeed() {
		LuaTable table = LuaValue.tableOf();
		table.set(1, CoerceJavaToLua.coerce(drone.getVelocity().x));
		table.set(2, CoerceJavaToLua.coerce(drone.getVelocity().y));
		table.set(3, CoerceJavaToLua.coerce(drone.getVelocity().z));
		return table;
	}
	//Rotation getters
	public double getPitch(){ return drone.getPitch(); }
	public double getYaw(){ return drone.getYaw(); }
	public double getRoll(){ return drone.getRoll(); }
	public LuaTable getRotation() {
		LuaTable table = LuaValue.tableOf();
		table.set(1, CoerceJavaToLua.coerce(drone.getPitch()));
		table.set(2, CoerceJavaToLua.coerce(drone.getPitch()));
		table.set(3, CoerceJavaToLua.coerce(drone.getPitch()));
		return table;
	}
	//Rotation Rate getters
	public double getYawRate(){ return drone.yawRate; }
	public double getPitchRate(){ return drone.pitchRate; }
	public double getRollRate(){ return drone.rollRate; }
	public LuaTable getRotationRate() {
		LuaTable table = LuaValue.tableOf();
		table.set(1, CoerceJavaToLua.coerce(drone.yawRate));
		table.set(2, CoerceJavaToLua.coerce(drone.pitchRate));
		table.set(3, CoerceJavaToLua.coerce(drone.rollRate));
		return table;
	}
}
