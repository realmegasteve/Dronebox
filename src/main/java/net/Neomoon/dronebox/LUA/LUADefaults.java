package net.Neomoon.dronebox.LUA;

import net.Neomoon.dronebox.ClientConfig;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LUADefaults {
	public static String presetPythonCode = """
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
    Chat object
        chat:last() → last message string
        chat:getMessage(index) → message index counting backyards string
		
""");


	public static final int COLOR_DEFAULT   = 0xFFD4D4D4;
	public static final int SELECTION_COLOR = 0x8855AAFF;


	public static Pattern syntaxPattern;
	private static final List<String> rulePatterns = new ArrayList<>();
	static final List<String> ruleNames = new ArrayList<>();
	public static final List<Integer> groupColors = new ArrayList<>();
	public static final Map<String, Integer> runtimeColors = new LinkedHashMap<>();

	private static final Map<String, Integer> defaultGroupColors = new LinkedHashMap<>();

	static {
		syntaxPattern = null;
		rulePatterns.clear();
		groupColors.clear();
		runtimeColors.clear();
		ruleNames.clear();
		defaultGroupColors.clear();




		addNamedRuleWithDotAll("multiline_lua_comment", "--\\[\\[.*?\\]\\]", ClientConfig.getInt("lua.syntax.multiline_lua_comment", 0xFF99AABB));
		addNamedRuleWithDotAll("multiline_bracket_string", "\\[\\[.*?\\]\\]", ClientConfig.getInt("lua.syntax.multiline_bracket_string", 0xFFFF7F50));
		addNamedRule("double_quoted_string", "\"(?:\\\\.|[^\"\\\\\\r\\n])*\"", ClientConfig.getInt("lua.syntax.double_quoted_string", 0xFFFFB347));
		addNamedRule("single_quoted_string", "'(?:\\\\.|[^'\\\\\\r\\n])*'", ClientConfig.getInt("lua.syntax.single_quoted_string", 0xFFFF8C69));
		addNamedRule("single_line_comment", "--[^\\n]*", ClientConfig.getInt("lua.syntax.single_line_comment", 0xFF99AABB));
		addNamedRule("decorator", "@\\w+", ClientConfig.getInt("lua.syntax.decorator", 0xFFE0E0AA));
		addNamedRule("function_keyword", "\\bfunction\\b", ClientConfig.getInt("lua.syntax.function_keyword", 0xFFBB66BB));
		addNamedRule("function_name", "\\bfunction\\s+(\\w+)", ClientConfig.getInt("lua.syntax.function_name", 0xFFFFCC88));
		addNamedRule("logical_operator", "\\b(?:and|not|or)\\b", ClientConfig.getInt("lua.syntax.logical_operator", 0xFFBB66BB));
		addNamedRule("control_flow_keyword", "\\b(?:break|do|else|elseif|end|for|goto|if|repeat|return|then|until|while)\\b", ClientConfig.getInt("lua.syntax.control_flow_keyword", 0xFFBB66BB));
		addNamedRule("local_keyword", "\\blocal\\b", ClientConfig.getInt("lua.syntax.local_keyword", 0xFFBB66BB));
		addNamedRule("in_keyword", "\\bin\\b", ClientConfig.getInt("lua.syntax.in_keyword", 0xFFBB66BB));
		addNamedRule("literal", "\\b(?:false|nil|true)\\b", ClientConfig.getInt("lua.syntax.literal", 0xFFBB66BB));
		addNamedRule("hex_number", "\\b0x[0-9A-Fa-f]+\\b", ClientConfig.getInt("lua.syntax.hex_number", 0xFF6EE7B7));
		addNamedRule("number", "\\b\\d+(?:\\.\\d+)?\\b", ClientConfig.getInt("lua.syntax.number", 0xFF00FFAA));
		addNamedRule("object_method_call", "(\\w+)\\s*([:.])\\s*(\\w+)\\s*(?=\\()", ClientConfig.getInt("lua.syntax.object_method_call", 0xFFAAAAEE));
		addNamedRule("tab_marker", "⟹", ClientConfig.getInt("lua.syntax.tab_marker", 0xFF444444));

		rebuildCombinedPattern();
	}


	public static void addRule(String regex, int color) {
		addNamedRuleInternal(UUID.randomUUID().toString(), regex, color, false);
	}

	public static void addRuleWithDotAll(String regex, int color) {
		addNamedRuleInternal(UUID.randomUUID().toString(), regex, color, true);
	}


	public static void addNamedRule(String name, String regex, int color) {
		addNamedRuleInternal(name, regex, color, false);
	}

	public static void addNamedRuleWithDotAll(String name, String regex, int color) {
		addNamedRuleInternal(name, regex, color, true);
	}

	private static void addNamedRuleInternal(String name, String regex, int color, boolean dotAll) {
		if (!defaultGroupColors.containsKey(name)) {
			defaultGroupColors.put(name, color);
		}

		String wrapped = dotAll ? "(?s:" + regex + ")" : "(?:" + regex + ")";
		String capturing = "(" + wrapped + ")";
		rulePatterns.add(capturing);
		groupColors.add(color);
		ruleNames.add(name);
		rebuildCombinedPattern();
	}
	
	private static void rebuildCombinedPattern() {
		if (rulePatterns.isEmpty()) {
			syntaxPattern = null;
			return;
		}
		StringJoiner join = new StringJoiner("|");
		for (String p : rulePatterns) join.add(p);
		syntaxPattern = Pattern.compile(join.toString(), Pattern.MULTILINE);
	}

	
	public static void setColor(String token, int color) {
		runtimeColors.put(Objects.requireNonNull(token), color);
	}

	public static void clearColor(String token) {
		runtimeColors.remove(token);
	}

	public static void clearAllRuntimeColors() {
		runtimeColors.clear();
	}


	public static void setColorByRuleName(String name, int color) {
		for (int i = 0; i < ruleNames.size(); i++) {
			if (ruleNames.get(i).equals(name)) {
				groupColors.set(i, color);
				break;
			}
		}
	}

	/**
	 * Restore groupColors to the originally-registered defaults.
	 * This does not touch ClientConfig (caller may want to clear saved overrides first).
	 */
	public static void resetToDefaults() {
		for (int i = 0; i < ruleNames.size(); i++) {
			String rn = ruleNames.get(i);
			Integer def = defaultGroupColors.get(rn);
			if (def != null) {
				if (i < groupColors.size()) groupColors.set(i, def);
				else groupColors.add(def);
			}
		}
	}


	public static List<String> getRuleNames() {
		return Collections.unmodifiableList(ruleNames);
	}

	public static List<Integer> getGroupColors() {
		return Collections.unmodifiableList(groupColors);
	}

	public static int pickColorForMatch(Matcher m, String token) {
		if (token == null || token.isEmpty()) return COLOR_DEFAULT;

		Integer rt = runtimeColors.get(token);
		if (rt != null) return rt;

		if (syntaxPattern == null || m == null) return COLOR_DEFAULT;

		for (int gi = 1; gi <= m.groupCount(); gi++) {
			String g;
			try { g = m.group(gi); } catch (IllegalStateException ex) { g = null; }
			if (g != null) {
				Integer rt2 = runtimeColors.get(g);
				if (rt2 != null) return rt2;

				if (g.matches("\\w+\\s*[:.]+\\s*\\w+\\s*")) {
					int mc = getMethodCallColor(g, token);
					if (mc != COLOR_DEFAULT) return mc;
				}

				int idx = gi - 1;
				if (idx >= 0 && idx < groupColors.size()) {
					return groupColors.get(idx);
				} else {
					return COLOR_DEFAULT;
				}
			}
		}
		return COLOR_DEFAULT;
	}

	public static int getMethodCallColor(String fullMatch, String token) {
		String[] parts = fullMatch.split("\\s*[:.]+\\s*");
		if (parts.length >= 2) {
			String obj = parts[0].trim();
			String meth = parts[1].trim();
			Integer rObj = runtimeColors.get(obj);
			if (rObj != null) return rObj;
			Integer rMeth = runtimeColors.get(meth);
			if (rMeth != null) return rMeth;

			if (token.equals(obj)) return 0xFFAAAAEE;
			if (token.equals(meth)) return 0xFFDDEEFF;
			return 0xFFAAAAEE;
		}
		return COLOR_DEFAULT;
	}
}
