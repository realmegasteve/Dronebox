package net.Neomoon.dronebox.python.PythonObjects;

import net.Neomoon.dronebox.Drone;
import net.minecraft.util.math.Vec3d;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

public class Controller {
	Drone drone;

	public Controller(Drone inDrone) {
		drone = inDrone;
	}

	//Rotation Rate getters
	public double yaw(){ return drone.yawInput; }
	public double forward(){ return drone.forwardInput; }
	public double strafe(){ return drone.strafeInput; }
	public double up(){ return drone.upInput; }
}
