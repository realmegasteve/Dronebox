package net.Neomoon.dronebox.LUA.LUAObjects;

import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilteredLUAObject extends LuaUserdata {
	protected final List<String> ForbiddenMethods = new ArrayList<>();

	public FilteredLUAObject(LuaValue original) {
		super(((LuaUserdata) original).m_instance, ((LuaUserdata) original).m_metatable);
		initializeForbiddenMethods();
	}

	private void initializeForbiddenMethods() {
		ForbiddenMethods.add("getclass");
		ForbiddenMethods.add("getclassloader");
		ForbiddenMethods.add("getdeclaringclass");
		ForbiddenMethods.add("getsuperclass");
		ForbiddenMethods.add("getenclosingclass");
		ForbiddenMethods.add("getcomponenttype");
		ForbiddenMethods.add("getfields");
		ForbiddenMethods.add("getfield");
		ForbiddenMethods.add("getdeclaredfields");
		ForbiddenMethods.add("getdeclaredfield");
		ForbiddenMethods.add("getmethods");
		ForbiddenMethods.add("getmethod");
		ForbiddenMethods.add("getdeclaredmethods");
		ForbiddenMethods.add("getdeclaredmethod");
		ForbiddenMethods.add("getconstructors");
		ForbiddenMethods.add("getconstructor");
		ForbiddenMethods.add("getdeclaredconstructors");
		ForbiddenMethods.add("getdeclaredconstructor");
		ForbiddenMethods.add("getannotations");
		ForbiddenMethods.add("getannotation");
		ForbiddenMethods.add("getdeclaredannotations");
		ForbiddenMethods.add("getdeclaredannotation");
		ForbiddenMethods.add("getannotationsbytype");
		ForbiddenMethods.add("getdeclaredannotationsbytype");
		ForbiddenMethods.add("getmodifiers");
		ForbiddenMethods.add("getinterfaces");
		ForbiddenMethods.add("getgenericinterfaces");
		ForbiddenMethods.add("getgenericsuperclass");
		ForbiddenMethods.add("gettypeparameters");
		ForbiddenMethods.add("getpackage");
		ForbiddenMethods.add("getprotectiondomain");
		ForbiddenMethods.add("getsigners");
		ForbiddenMethods.add("getresource");
		ForbiddenMethods.add("getresources");
		ForbiddenMethods.add("getresourceasstream");
		ForbiddenMethods.add("getsystemresource");
		ForbiddenMethods.add("getsystemresources");
		ForbiddenMethods.add("getsystemresourceasstream");
		ForbiddenMethods.add("getsystemclassloader");
		ForbiddenMethods.add("clone");
		ForbiddenMethods.add("finalize");
		ForbiddenMethods.add("notify");
		ForbiddenMethods.add("notifyall");
		ForbiddenMethods.add("wait");
		ForbiddenMethods.add("forname");
		ForbiddenMethods.add("loadclass");
		ForbiddenMethods.add("findclass");
		ForbiddenMethods.add("defineclass");
		ForbiddenMethods.add("findloadedclass");
		ForbiddenMethods.add("findresource");
		ForbiddenMethods.add("findresources");
		ForbiddenMethods.add("findsystemclass");
		ForbiddenMethods.add("getunsafe");
		ForbiddenMethods.add("allocateinstance");
		ForbiddenMethods.add("objectfieldoffset");
		ForbiddenMethods.add("staticfieldoffset");
		ForbiddenMethods.add("staticfieldbase");
		ForbiddenMethods.add("exit");
		ForbiddenMethods.add("gc");
		ForbiddenMethods.add("runfinalization");
		ForbiddenMethods.add("load");
		ForbiddenMethods.add("loadlibrary");
		ForbiddenMethods.add("maplibraryname");
		ForbiddenMethods.add("getproperty");
		ForbiddenMethods.add("getproperties");
		ForbiddenMethods.add("setproperty");
		ForbiddenMethods.add("clearProperty");
		ForbiddenMethods.add("getenv");
		ForbiddenMethods.add("getenvs");
		ForbiddenMethods.add("console");
		ForbiddenMethods.add("inheritedchannel");
		ForbiddenMethods.add("setSecuritymanager");
		ForbiddenMethods.add("getsecuritymanager");
		ForbiddenMethods.add("getruntime");
		ForbiddenMethods.add("exec");
		ForbiddenMethods.add("addsshutdownhook");
		ForbiddenMethods.add("removeshutdownhook");
		ForbiddenMethods.add("halt");
		ForbiddenMethods.add("currentthread");
		ForbiddenMethods.add("activecount");
		ForbiddenMethods.add("enumerate");
		ForbiddenMethods.add("holdslock");
		ForbiddenMethods.add("dumpstack");
		ForbiddenMethods.add("getallstacktraces");
		ForbiddenMethods.add("getdefaultuncaughtexceptionhandler");
		ForbiddenMethods.add("setdefaultuncaughtexceptionhandler");
		ForbiddenMethods.add("stop");
		ForbiddenMethods.add("suspend");
		ForbiddenMethods.add("resume");
		ForbiddenMethods.add("destroy");
		ForbiddenMethods.add("listroots");
		ForbiddenMethods.add("createtempfile");
		ForbiddenMethods.add("delete");
		ForbiddenMethods.add("deleteonexit");
		ForbiddenMethods.add("mkdir");
		ForbiddenMethods.add("mkdirs");
		ForbiddenMethods.add("renameto");
		ForbiddenMethods.add("setlastmodified");
		ForbiddenMethods.add("setreadable");
		ForbiddenMethods.add("setwritable");
		ForbiddenMethods.add("setexecutable");
		ForbiddenMethods.add("getlocalhost");
		ForbiddenMethods.add("getbyname");
		ForbiddenMethods.add("getallbyname");
		ForbiddenMethods.add("getbyaddress");
		ForbiddenMethods.add("getmodule");
		ForbiddenMethods.add("getunnamedmodule");
		ForbiddenMethods.add("readobject");
		ForbiddenMethods.add("writeobject");
		ForbiddenMethods.add("readresolve");
		ForbiddenMethods.add("writereplace");
		ForbiddenMethods.add("readobjectnodata");
		ForbiddenMethods.add("getbeaninfo");
		ForbiddenMethods.add("getpropertydescriptors");
		ForbiddenMethods.add("getmethoddescriptors");
		ForbiddenMethods.add("getmethodhandles");
		ForbiddenMethods.add("lookup");
		ForbiddenMethods.add("findvirtual");
		ForbiddenMethods.add("findstatic");
		ForbiddenMethods.add("findspecial");
		ForbiddenMethods.add("findconstructor");
		ForbiddenMethods.add("unreflect");
		ForbiddenMethods.add("unreflectspecial");
		ForbiddenMethods.add("unreflectconstructor");
		ForbiddenMethods.add("unreflectgetter");
		ForbiddenMethods.add("unreflectsetter");
		ForbiddenMethods.add("getmxbean");
		ForbiddenMethods.add("getmanagementfactory");
		ForbiddenMethods.add("getplatformmxbean");
		ForbiddenMethods.add("getplatformmxbeans");
		ForbiddenMethods.add("getworld");
		ForbiddenMethods.add("getserver");
		ForbiddenMethods.add("getminecraftserver");
		ForbiddenMethods.add("getgameprofile");
		ForbiddenMethods.add("getplayermanager");
		ForbiddenMethods.add("getcommandmanager");
		ForbiddenMethods.add("getdatamanager");
		ForbiddenMethods.add("getentitymanager");
		ForbiddenMethods.add("getsavehandler");
		ForbiddenMethods.add("getlevelproperties");
		Collections.sort(ForbiddenMethods);
	}

	@Override
	public LuaValue get(LuaValue key) {
		String keyStr = key.tojstring().toLowerCase();
		if (ForbiddenMethods.contains(keyStr)) {
			return LuaValue.NIL;
		}
		return m_metatable!=null? gettable(this,key): NIL;
	}
}
