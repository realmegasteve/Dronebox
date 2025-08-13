package net.Neomoon.dronebox.python;

import net.Neomoon.dronebox.python.PythonObjects.Drone;
import net.Neomoon.dronebox.python.PythonObjects.Logger;
import net.minecraft.client.MinecraftClient;
import org.python.antlr.ast.Str;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.*;

public class MinecraftPythonInterpreter {
	public PythonInterpreter py = new PythonInterpreter();
	public StringWriter outputBuffer = new StringWriter();

	public MinecraftPythonInterpreter init() {

		py.setOut(new PrintWriter(outputBuffer));

		py.set("MinecraftClient", MinecraftClient.getInstance());
		return this;
	}

	public MinecraftPythonInterpreter set(Object o, String name) {
		py.set(name, o);
		return this;
	}

	private final ExecutorService exec = Executors.newSingleThreadExecutor();

	public void run(String code) throws ExecutionException, InterruptedException {
		outputBuffer = new StringWriter();
		py.setOut(new PrintWriter(outputBuffer));

		Future<?> f = exec.submit(() -> py.exec(code));
		try {
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			f.cancel(true);
			py.close();
		} catch (InterruptedException | ExecutionException e) {
			throw e;
		}
	}

	public void runSetup() throws ExecutionException, InterruptedException {
		PyObject func = py.getLocals().__getitem__(Py.newString("setup"));
		Future<?> f = exec.submit(() -> func.__call__());

		try {
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			f.cancel(true);
			py.close();
		} catch (InterruptedException | ExecutionException e) {
			throw e;
		}
	}

	public void runTick() throws ExecutionException, InterruptedException {
		PyObject func = py.getLocals().__getitem__(Py.newString("tick"));
		Future<?> f = exec.submit(() -> func.__call__());
		try {
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			f.cancel(true);
			py.close();
		} catch (InterruptedException | ExecutionException e) {
			throw e;
		}
	}

	public String console(){
		return outputBuffer.toString();
	}
}
