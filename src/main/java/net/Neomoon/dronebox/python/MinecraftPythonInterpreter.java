package net.Neomoon.dronebox.python;

import net.Neomoon.dronebox.python.PythonObjects.Drone;
import net.Neomoon.dronebox.python.PythonObjects.Logger;
import net.minecraft.client.MinecraftClient;
import org.python.antlr.ast.Str;
import org.python.util.PythonInterpreter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.*;

public class MinecraftPythonInterpreter {
	public PythonInterpreter py = new PythonInterpreter();
	public StringWriter outputBuffer = new StringWriter();

	public MinecraftPythonInterpreter init() {
		Drone drone = new Drone();
		Logger logger = new Logger();

		py.setOut(new PrintWriter(outputBuffer));

		py.set("drone", drone);
		py.set("console", logger);
		py.set("MinecraftClient", MinecraftClient.getInstance());
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

	public String console(){
		return outputBuffer.toString();
	}
}
