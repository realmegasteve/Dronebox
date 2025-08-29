package net.Neomoon.dronebox.mixins;

import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Mixin(value = LuaUserdata.class, remap = false)
public class LuaUserdataFilteringMixin {

	@Inject(
		method = "get(Lorg/luaj/vm2/LuaValue;)Lorg/luaj/vm2/LuaValue;",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	private void FilteredGetSafeThisTimeUwU(LuaValue key, CallbackInfoReturnable<LuaValue> cir) {
		if (key != null && key.isstring()) {
			String methodName = key.tojstring().toLowerCase(Locale.ROOT);
			if (dronebox$FORBIDDEN_METHODS_No_Gud_UwU.contains(methodName)) {
				cir.setReturnValue(LuaValue.NIL);
				cir.cancel();
			}
		}
	}

    @Unique
	private static final Set<String> dronebox$FORBIDDEN_METHODS_No_Gud_UwU = new HashSet<>();
    
    static {
        dronebox$initializeForbiddenMethods();
    }
    
    @Unique
	private static void dronebox$initializeForbiddenMethods() {
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getclassloader");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaringclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsuperclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getenclosingclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getcomponenttype");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getfields");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getfield");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredfields");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredfield");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmethods");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmethod");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredmethods");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredmethod");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getconstructors");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getconstructor");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredconstructors");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredconstructor");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getannotations");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getannotation");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredannotations");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredannotation");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getannotationsbytype");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredannotationsbytype");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmodifiers");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getinterfaces");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getgenericinterfaces");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getgenericsuperclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("gettypeparameters");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getpackage");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getprotectiondomain");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsigners");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getresource");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getresources");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getresourceasstream");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemresource");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemresources");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemresourceasstream");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemclassloader");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("clone");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("finalize");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("notify");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("notifyall");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("wait");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("forname");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("loadclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("defineclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findloadedclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findresource");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findresources");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findsystemclass");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getunsafe");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("allocateinstance");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("objectfieldoffset");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("staticfieldoffset");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("staticfieldbase");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("exit");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("gc");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("runfinalization");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("load");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("loadlibrary");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("maplibraryname");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getproperty");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getproperties");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setproperty");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("clearproperty");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getenv");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getenvs");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("console");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("inheritedchannel");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setsecuritymanager");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsecuritymanager");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getruntime");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("exec");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("addshutdownhook");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("removeshutdownhook");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("halt");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("currentthread");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("activecount");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("enumerate");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("holdslock");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("dumpstack");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getallstacktraces");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdefaultuncaughtexceptionhandler");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setdefaultuncaughtexceptionhandler");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("stop");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("suspend");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("resume");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("destroy");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("listroots");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("createtempfile");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("delete");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("deleteonexit");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("mkdir");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("mkdirs");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("renameto");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setlastmodified");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setreadable");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setwritable");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("setexecutable");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getlocalhost");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getbyname");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getallbyname");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getbyaddress");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmodule");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getunnamedmodule");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("readobject");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("writeobject");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("readresolve");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("writereplace");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("readobjectnodata");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmethodhandles");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("lookup");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findvirtual");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findstatic");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findspecial");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("findconstructor");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("unreflect");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectspecial");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectconstructor");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectgetter");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectsetter");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmxbean");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getmanagementfactory");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getplatformmxbean");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getplatformmxbeans");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getworld");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getserver");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getminecraftserver");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getgameprofile");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getplayermanager");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getcommandmanager");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getdatamanager");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getentitymanager");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getsavehandler");
        dronebox$FORBIDDEN_METHODS_No_Gud_UwU.add("getlevelproperties");
    }
}
