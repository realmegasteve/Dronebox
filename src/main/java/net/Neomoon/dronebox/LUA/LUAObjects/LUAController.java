package net.Neomoon.dronebox.LUA.LUAObjects;

import net.Neomoon.dronebox.Drone;

public class LUAController {
	Drone drone;

	public LUAController(Drone inDrone) {
		drone = inDrone;
	}

	//Rotation Rate getters
	public double yaw(){ return drone.yawInput; }
	public double forward(){ return drone.forwardInput; }
	public double strafe(){ return drone.strafeInput; }
	public double up(){ return drone.upInput; }
}
