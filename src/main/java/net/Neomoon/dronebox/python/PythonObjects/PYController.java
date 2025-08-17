package net.Neomoon.dronebox.python.PythonObjects;

import net.Neomoon.dronebox.Drone;

public class PYController {
	Drone drone;

	public PYController(Drone inDrone) {
		drone = inDrone;
	}

	//Rotation Rate getters
	public double yaw(){ return drone.yawInput; }
	public double forward(){ return drone.forwardInput; }
	public double strafe(){ return drone.strafeInput; }
	public double up(){ return drone.upInput; }
}
