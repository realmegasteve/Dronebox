package net.Neomoon.dronebox.python.PythonObjects;

import net.Neomoon.dronebox.Drone;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

public class PYDrone {
	Drone drone;

	public PYDrone(Drone inDrone) {
		drone = inDrone;
	}

	public void setVelocity(double X, double Y, double Z){
		drone.setVelocity(X, Y, Z);
	}

	//Position getters
	public double getX(){ return drone.getX(); }
	public double getY(){ return drone.getY(); }
	public double getZ(){ return drone.getZ(); }
	public PyTuple getPos(){ return new PyTuple(new PyFloat(drone.getX()), new PyFloat(drone.getY()), new PyFloat(drone.getZ()));}


}
