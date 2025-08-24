package net.Neomoon.dronebox.LUA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.StringWriter;
import java.util.concurrent.*;

public class MinecraftLuaInterpreter {
	private Globals globals = JsePlatform.standardGlobals();
	private StringWriter outputBuffer = new StringWriter();
	private final ExecutorService exec = Executors.newSingleThreadExecutor();

	public MinecraftLuaInterpreter init() {
		// Override print to write into our buffer
		globals.set("print", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				outputBuffer.write("\n");
				return NIL;
			}
			@Override
			public Varargs invoke(Varargs args) {
				for (int i = 1; i <= args.narg(); i++) {
					outputBuffer.write(args.arg(i).tojstring());
					if (i < args.narg()) outputBuffer.write(" ");
				}
				outputBuffer.write("\n");
				return NIL;
			}
		});

		globals.set("MinecraftClient", CoerceJavaToLua.coerce(MinecraftClient.getInstance()));
		return this;
	}

	public MinecraftLuaInterpreter set(Object o, String name) {
		globals.set(name, CoerceJavaToLua.coerce(o));
		return this;
	}

	public MinecraftLuaInterpreter set(LuaValue o, String name) {
		globals.set(name, CoerceJavaToLua.coerce(o));
		return this;
	}

	public void run(String code) throws ExecutionException, InterruptedException {
		outputBuffer = new StringWriter();
		Future<?> f = exec.submit(() -> {
			LuaValue chunk = globals.load(code, "script");
			chunk.call();
		});
		try {
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			f.cancel(true);
			throw new RuntimeException("Lua script timed out", e);
		}
	}

	public void runSetup() throws ExecutionException, InterruptedException {
		runFunction("setup");
	}

	public void runTick() throws ExecutionException, InterruptedException {
		runFunction("tick");
	}

	public void runFunction(String name) throws ExecutionException, InterruptedException {
		LuaValue func = globals.get(name);
		if (func.isnil()) {
			var mc = MinecraftClient.getInstance();
			if (mc != null && mc.player != null) {
				mc.player.sendMessage(Text.of(name + " function not found"), true);
			}
			return;
		}
		Future<?> f = exec.submit(() -> func.call());  // <-- FIXED
		try {
			f.get(3, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			f.cancel(true);
			throw new RuntimeException("Lua " + name + " timed out", e);
		}
	}


	public String console() {
		return outputBuffer.toString();
	}
}
