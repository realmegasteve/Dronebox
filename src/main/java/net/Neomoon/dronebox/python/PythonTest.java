package net.Neomoon.dronebox.python;

import net.Neomoon.dronebox.python.PythonObjects.Drone;
import org.python.util.PythonInterpreter;

public class PythonTest {
	public static void main() {
		Drone drone = new Drone();

		PythonInterpreter py = new PythonInterpreter();
		py.set("drone", drone);

		String code = """
		for i in range(50):
			drone.setYawAcceleration(i)
		""";

		py.exec(code);


	}
}
