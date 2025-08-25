package net.Neomoon.dronebox.LUA.LUAObjects;

import net.Neomoon.dronebox.Drone;
import net.minecraft.util.math.Vec3d;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LUADrone {
	Drone drone;

	public LUADrone(Drone inDrone) {
		drone = inDrone;
	}

	public void accelerate(double X, double Y, double Z){
		drone.setVelocity(drone.getVelocity().add(new Vec3d(X, Y, Z)));
	}

	public void accelerateTurning(double yaw, double pitch, double roll){
		drone.yawRate += yaw;
		drone.rollRate += roll;
		drone.pitchRate += pitch;
	}

	public void accelerateUpward(double acceleration) {
		double yawRad = Math.toRadians(drone.getYaw());
		double pitchRad = Math.toRadians(drone.getPitch());
		double rollRad = Math.toRadians(drone.getRoll());

		Vec3d localUp = new Vec3d(0, 1, 0);

		double cosYaw = Math.cos(yawRad), sinYaw = Math.sin(yawRad);
		double cosPitch = Math.cos(pitchRad), sinPitch = Math.sin(pitchRad);
		double cosRoll = Math.cos(rollRad), sinRoll = Math.sin(rollRad);

		double m00 = cosYaw * cosRoll + sinYaw * sinPitch * sinRoll;
		double m01 = -cosYaw * sinRoll + sinYaw * sinPitch * cosRoll;
		double m02 = sinYaw * cosPitch;

		double m10 = cosPitch * sinRoll;
		double m11 = cosPitch * cosRoll;
		double m12 = -sinPitch;

		double m20 = -sinYaw * cosRoll + cosYaw * sinPitch * sinRoll;
		double m21 = sinRoll * sinYaw + cosYaw * sinPitch * cosRoll;
		double m22 = cosYaw * cosPitch;

		Vec3d worldUp = new Vec3d(m01, m11, m21);

		worldUp = worldUp.multiply(acceleration);

		drone.setVelocity(drone.getVelocity().add(worldUp));
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
