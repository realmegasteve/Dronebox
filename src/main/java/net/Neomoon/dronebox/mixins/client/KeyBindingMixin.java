package net.Neomoon.dronebox.mixins.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.Neomoon.dronebox.client.KeyInterceptor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

	@Shadow
	@Final
	private static Map<String, KeyBinding> KEYS_BY_ID;

	@ModifyReturnValue(method = "equals", at = @At("RETURN"))
	private boolean multiKeyBindEquals(boolean original, KeyBinding other) {
		if ((KeyBinding) (Object) this instanceof KeyInterceptor.MultiKeyBinding) {
			if (!(other instanceof KeyInterceptor.MultiKeyBinding)) {
				return false;
			}
			return original;
		}
		return original;
	}

	@WrapOperation(method = "updateKeysByCode", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
	private static Object multiKeyBindPriority(Map<?, ?> instance, Object k, Object v, Operation<Object> original) {
		if (!(k instanceof InputUtil.Key key) || !(v instanceof KeyBinding keyBinding)) {
			return original.call(instance, k, v);
		}
		if (keyBinding instanceof KeyInterceptor.MultiKeyBinding multiKeyBinding) {
			multiKeyBinding.clearOthers();
			KEYS_BY_ID.values().forEach(other -> {
				if (other instanceof KeyInterceptor.MultiKeyBinding) {
					return;
				}
				if (
					((KeyBindingAccessor) multiKeyBinding).dronebox$getBoundKey()
					.equals(
						((KeyBindingAccessor) other).dronebox$getBoundKey()
					)
				) {
					multiKeyBinding.addOther(other);
				}
			});
		}
		if (!(instance.get(key) instanceof KeyInterceptor.MultiKeyBinding)) {
			return original.call(instance, key, keyBinding);
		}
		return null;
	}
}
