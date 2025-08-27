package net.Neomoon.dronebox.LUA;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LUADefaults {
	public  static String presetPythonCode = """
        -- Write your LUA code here!
        -- Shift-click a drone to link it
        -- and allow running code with run
        -- test!
        
        counter = 0 -- Test variable!!
        function setup()
            -- Called once when loading code!
            logger:log("Meow! Only the pendrive owner receives the message!")
        end
        
        function tick()
            -- Called once per tick!
            counter = counter + 1
            if counter % 20 == 0 then
               logger:log("running for " .. tostring(counter / 20) .. " seconds!")
            end
        end
            
        """;


	public static Text guide = Text.of("""
    Mini Lua Methods Guide by Azura Nishio

    Drone object
        drone:getPos() → table {x, y, z}
        drone:getSpeed() → table {x, y, z}
        drone:getRotation() → table {pitch, yaw, roll}
        drone:getRotationRate() → table {pitchRate, yawRate, rollRate}
        drone:setSpeed(X, Y, Z)
        drone:accelerate(X, Y, Z)
        drone:accelerateTurning(yaw, pitch, roll)
        drone:setAccessory(state)

    Controller object
        controller:yaw() → yaw input
        controller:forward() → forward input
        controller:strafe() → strafe input
        controller:up() → up input

    Logger object
        logger:print(msg)
        logger:log(msg)
        logger:warn(msg)
        logger:error(msg)

    Radio object
        radio:sendSignal(channel, value)
        radio:read(channel) → value
        radio:readOrDefault(channel, default) → value
        radio:signalExist(channel) → boolean
""");



	//Coloring shenanigansssssssssssssssssssss
	public static final int COLOR_DEFAULT        = 0xFFD4D4D4;
	public static final int SELECTION_COLOR      = 0x8855AAFF;

	public static Pattern syntaxPattern;
	public static final List<Integer> groupColors = new ArrayList<>();

	static {
		syntaxPattern = null;
		groupColors.clear();

		// Triple-bracketed strings
		addRule("\\[\\[.*?\\]\\]", 0xFFE06D5C);

		// Double-quoted strings
		addRule("\"(?:\\\\.|[^\"\\\\])*\"", 0xFFFFA680);

		// Single-quoted strings
		addRule("'(?:\\\\.|[^'\\\\])*'", 0xFFD47050);

		// Comments - multi-line first
		addRule("--\\[\\[.*?\\]\\]", 0xFF9999AA);

		// Comments - single line
		addRule("--.*", 0xFF9999AA);

		// Decorators - slightly more saturated
		addRule("@\\w+", 0xFFE0E0AA);

		// Keywords - function keyword (must come before function name capture)
		addRule("\\bfunction\\b", 0xFFBB66BB);

		// Function declaration with name capture (comes after function keyword)
		addRule("\\bfunction\\s+(\\w+)", 0xFFFFCC88);

		// Keywords - logical operators
		addRule("\\b(?:and|not|or)\\b", 0xFFBB66BB);

		// Keywords - control flow
		addRule("\\b(?:break|do|else|elseif|end|for|goto|if|repeat|return|then|until|while)\\b", 0xFFBB66BB);

		// Keywords - local
		addRule("\\blocal\\b", 0xFFBB66BB);

		// Keywords - loop control
		addRule("\\bin\\b", 0xFFBB66BB);

		// Keywords - literals
		addRule("\\b(?:false|nil|true)\\b", 0xFFBB66BB);

		// Numbers
		addRule("\\b\\d+(?:\\.\\d+)?\\b", 0xFF00FFAA);

		// Object and method call pattern - captures both parts
		addRule("(\\w+)\\s*([:.])\\s*(\\w+)\\s*(?=\\()", 0xFFAAAAEE);

		// Tab marker ⟹
		addRule("⟹", 0xFF444444);
	}

	public static void addRule(String regex, int color) {
		String combined = "(" + regex + ")";
		if (syntaxPattern == null) {
			syntaxPattern = Pattern.compile(combined, Pattern.MULTILINE | Pattern.DOTALL);
		} else {
			syntaxPattern = Pattern.compile(
				syntaxPattern.pattern() + "|" + combined,
				Pattern.MULTILINE | Pattern.DOTALL
			);
		}
		groupColors.add(color);
	}

	public static int pickColorForMatch(Matcher m, String token) {
		if (syntaxPattern == null) return COLOR_DEFAULT;

		Matcher matcher = syntaxPattern.matcher(token);
		if (!matcher.find()) return COLOR_DEFAULT;

		for (int i = 1; i <= groupColors.size(); i++) {
			if (matcher.start(i) != -1) {
				String matchedText = matcher.group(i);
				if (matchedText != null && matchedText.matches("\\w+\\s*[:.]+\\s*\\w+\\s*")) {
					if (token.contains(":") || token.contains(".")) {
						if (token.matches("\\w+") && matcher.group(i).endsWith(token)) {
							return 0xFFDDEEFF;
						} else {
							return groupColors.get(i - 1);
						}
					}
				}
				return groupColors.get(i - 1);
			}
		}
		return COLOR_DEFAULT;
	}

	public static int getMethodCallColor(String fullMatch, String token) {
		if (fullMatch.matches("\\w+\\s*[:.]+\\s*\\w+\\s*")) {
			String[] parts = fullMatch.split("\\s*[:.]+\\s*");
			if (parts.length >= 2) {
				if (token.equals(parts[0].trim())) {
					return 0xFFAAAAEE;
				} else if (token.equals(parts[1].trim())) {
					return 0xFFDDEEFF;
				}
			}
		}
		return COLOR_DEFAULT;
	}
}
