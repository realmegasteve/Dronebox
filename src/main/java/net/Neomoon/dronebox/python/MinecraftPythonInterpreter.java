package net.Neomoon.dronebox.python;

import net.Neomoon.dronebox.Drone;
import net.Neomoon.dronebox.python.PythonObjects.Logger;
import net.Neomoon.dronebox.python.PythonObjects.PYDrone;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.python.core.PySystemState;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.*;

public class MinecraftPythonInterpreter {
	private PythonInterpreter py;
	private StringWriter outputBuffer = new StringWriter();
	private final ExecutorService exec = Executors.newSingleThreadExecutor();

	public MinecraftPythonInterpreter init(Drone drone) {
		PySystemState sys = new PySystemState();

		PyStringMap builtins = (PyStringMap) sys.builtins;
		builtins.__delitem__("open");
		builtins.__delitem__("execfile");
		builtins.__delitem__("compile");
		builtins.__delitem__("reload");

		sys.path.clear();
		sys.setClassLoader(new ClassLoader() {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if (name.startsWith("java.") || name.startsWith("javax.")) {
					throw new ClassNotFoundException("Java classes are blocked");
				}
				return super.loadClass(name);
			}
		});

		py = new PythonInterpreter(null, sys);
		py.setOut(new PrintWriter(outputBuffer));
		py.setErr(new PrintWriter(outputBuffer));

		py.set("Logger", new Logger());
		py.set("Drone", new PYDrone(drone));

		return this;
	}

	public MinecraftPythonInterpreter set(Object o, String name) {
		py.set(name, o);
		return this;
	}

	public void run(String code) throws ExecutionException, InterruptedException {
		resetBuffer();

		Future<?> f = exec.submit(() -> py.exec(code));
		try {
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			f.cancel(true);
			resetInterpreter();
		}
	}

	public void runFunc(String funcName) throws ExecutionException, InterruptedException {
		try {
			PyObject func = py.get(funcName);
			if (func == null) {
				log("Function '" + funcName + "' not found");
				return;
			}

			Future<?> f = exec.submit(() -> {
				func.__call__();
				return null;
			});
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			resetInterpreter();
		}
	}

	public void runSetup() throws ExecutionException, InterruptedException {
		runFunc("setup");
	}

	public void runTick() throws ExecutionException, InterruptedException {
		runFunc("tick");
	}

	public String console() {
		return outputBuffer.toString();
	}

	private void resetBuffer() {
		outputBuffer = new StringWriter();
		py.setOut(new PrintWriter(outputBuffer));
		py.setErr(new PrintWriter(outputBuffer));
	}

	private void resetInterpreter() {
		// You’ll need to pass the Drone back in somehow.
		// Either store it in a field or refactor init() to reuse.
		log("Interpreter reset required (timeout or error).");
	}

	private void log(String msg) {
		outputBuffer.write(msg + "\n");
	}
}
