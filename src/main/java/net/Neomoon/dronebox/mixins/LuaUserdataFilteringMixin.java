package net.Neomoon.dronebox.mixins;

import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(targets = "org.luaj.vm2.LuaUserdata", remap = false)
public class LuaUserdataFilteringMixin {

	@Inject(
		method = "get(Lorg/luaj/vm2/LuaValue;)Lorg/luaj/vm2/LuaValue;",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	private void FilteredGetSafeThisTimeUwU(LuaValue key, CallbackInfoReturnable<LuaValue> cir) {
		if (key != null && key.isstring()) {
			String methodName = key.tojstring().toLowerCase();
			if (FORBIDDEN_METHODS_No_Gud_UwU.contains(methodName)) {
				cir.setReturnValue(LuaValue.NIL);
				cir.cancel();
			}
		}
	}

    private static final Set<String> FORBIDDEN_METHODS_No_Gud_UwU = new HashSet<>();
    
    static {
        initializeForbiddenMethods();
    }
    
    private static void initializeForbiddenMethods() {
        FORBIDDEN_METHODS_No_Gud_UwU.add("getclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getclassloader");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaringclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsuperclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getenclosingclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getcomponenttype");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getfields");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getfield");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredfields");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredfield");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmethods");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmethod");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredmethods");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredmethod");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getconstructors");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getconstructor");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredconstructors");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredconstructor");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getannotations");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getannotation");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredannotations");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredannotation");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getannotationsbytype");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdeclaredannotationsbytype");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmodifiers");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getinterfaces");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getgenericinterfaces");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getgenericsuperclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("gettypeparameters");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getpackage");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getprotectiondomain");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsigners");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getresource");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getresources");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getresourceasstream");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemresource");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemresources");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemresourceasstream");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsystemclassloader");
        FORBIDDEN_METHODS_No_Gud_UwU.add("clone");
        FORBIDDEN_METHODS_No_Gud_UwU.add("finalize");
        FORBIDDEN_METHODS_No_Gud_UwU.add("notify");
        FORBIDDEN_METHODS_No_Gud_UwU.add("notifyall");
        FORBIDDEN_METHODS_No_Gud_UwU.add("wait");
        FORBIDDEN_METHODS_No_Gud_UwU.add("forname");
        FORBIDDEN_METHODS_No_Gud_UwU.add("loadclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("defineclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findloadedclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findresource");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findresources");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findsystemclass");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getunsafe");
        FORBIDDEN_METHODS_No_Gud_UwU.add("allocateinstance");
        FORBIDDEN_METHODS_No_Gud_UwU.add("objectfieldoffset");
        FORBIDDEN_METHODS_No_Gud_UwU.add("staticfieldoffset");
        FORBIDDEN_METHODS_No_Gud_UwU.add("staticfieldbase");
        FORBIDDEN_METHODS_No_Gud_UwU.add("exit");
        FORBIDDEN_METHODS_No_Gud_UwU.add("gc");
        FORBIDDEN_METHODS_No_Gud_UwU.add("runfinalization");
        FORBIDDEN_METHODS_No_Gud_UwU.add("load");
        FORBIDDEN_METHODS_No_Gud_UwU.add("loadlibrary");
        FORBIDDEN_METHODS_No_Gud_UwU.add("maplibraryname");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getproperty");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getproperties");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setproperty");
        FORBIDDEN_METHODS_No_Gud_UwU.add("clearproperty");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getenv");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getenvs");
        FORBIDDEN_METHODS_No_Gud_UwU.add("console");
        FORBIDDEN_METHODS_No_Gud_UwU.add("inheritedchannel");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setsecuritymanager");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsecuritymanager");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getruntime");
        FORBIDDEN_METHODS_No_Gud_UwU.add("exec");
        FORBIDDEN_METHODS_No_Gud_UwU.add("addshutdownhook");
        FORBIDDEN_METHODS_No_Gud_UwU.add("removeshutdownhook");
        FORBIDDEN_METHODS_No_Gud_UwU.add("halt");
        FORBIDDEN_METHODS_No_Gud_UwU.add("currentthread");
        FORBIDDEN_METHODS_No_Gud_UwU.add("activecount");
        FORBIDDEN_METHODS_No_Gud_UwU.add("enumerate");
        FORBIDDEN_METHODS_No_Gud_UwU.add("holdslock");
        FORBIDDEN_METHODS_No_Gud_UwU.add("dumpstack");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getallstacktraces");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdefaultuncaughtexceptionhandler");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setdefaultuncaughtexceptionhandler");
        FORBIDDEN_METHODS_No_Gud_UwU.add("stop");
        FORBIDDEN_METHODS_No_Gud_UwU.add("suspend");
        FORBIDDEN_METHODS_No_Gud_UwU.add("resume");
        FORBIDDEN_METHODS_No_Gud_UwU.add("destroy");
        FORBIDDEN_METHODS_No_Gud_UwU.add("listroots");
        FORBIDDEN_METHODS_No_Gud_UwU.add("createtempfile");
        FORBIDDEN_METHODS_No_Gud_UwU.add("delete");
        FORBIDDEN_METHODS_No_Gud_UwU.add("deleteonexit");
        FORBIDDEN_METHODS_No_Gud_UwU.add("mkdir");
        FORBIDDEN_METHODS_No_Gud_UwU.add("mkdirs");
        FORBIDDEN_METHODS_No_Gud_UwU.add("renameto");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setlastmodified");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setreadable");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setwritable");
        FORBIDDEN_METHODS_No_Gud_UwU.add("setexecutable");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getlocalhost");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getbyname");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getallbyname");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getbyaddress");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmodule");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getunnamedmodule");
        FORBIDDEN_METHODS_No_Gud_UwU.add("readobject");
        FORBIDDEN_METHODS_No_Gud_UwU.add("writeobject");
        FORBIDDEN_METHODS_No_Gud_UwU.add("readresolve");
        FORBIDDEN_METHODS_No_Gud_UwU.add("writereplace");
        FORBIDDEN_METHODS_No_Gud_UwU.add("readobjectnodata");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmethodhandles");
        FORBIDDEN_METHODS_No_Gud_UwU.add("lookup");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findvirtual");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findstatic");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findspecial");
        FORBIDDEN_METHODS_No_Gud_UwU.add("findconstructor");
        FORBIDDEN_METHODS_No_Gud_UwU.add("unreflect");
        FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectspecial");
        FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectconstructor");
        FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectgetter");
        FORBIDDEN_METHODS_No_Gud_UwU.add("unreflectsetter");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmxbean");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getmanagementfactory");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getplatformmxbean");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getplatformmxbeans");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getworld");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getserver");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getminecraftserver");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getgameprofile");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getplayermanager");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getcommandmanager");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getdatamanager");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getentitymanager");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getsavehandler");
        FORBIDDEN_METHODS_No_Gud_UwU.add("getlevelproperties");
    }
}
