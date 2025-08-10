package net.Neomoon.dronebox.python.PythonObjects;

public class Drone {
    public void setYawAcceleration(double Acceleration) {
        System.out.println("yaw accel: " + Acceleration);
    }
	public void setPitchAcceleration(double Acceleration) {
		System.out.println("pitch accel: " + Acceleration);
	}
	public void setRollAcceleration(double Acceleration) {
		System.out.println("roll accel: " + Acceleration);
	}

	public void setLocalXAcceleration(double Acceleration) {
		System.out.println("x accel: " + Acceleration);
	}
	public void setLocalYAcceleration(double Acceleration) {
		System.out.println("y accel: " + Acceleration);
	}
	public void setLocalZAcceleration(double Acceleration) { System.out.println("z accel: " + Acceleration); }

    public double getX() {
        return 42.0;
    }
	public double getY() {
		return 42.0;
	}
	public double getZ() { return 42.0; }
	public double getYaw() {
		return 42.0;
	}
	public double getPitch() {
		return 42.0;
	}
	public double getRol() {
		return 42.0;
	}

	public double getXSpeed() {
		return 42.0;
	}
	public double getYSpeed() {
		return 42.0;
	}
	public double getZSpeed() { return 42.0; }
	public double getYawSpeed() {
		return 42.0;
	}
	public double getPitchSpeed() {
		return 42.0;
	}
	public double getRolSpeed() {
		return 42.0;
	}
}
