package net.Neomoon.dronebox.mixins.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.Neomoon.dronebox.KeyInterceptor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.class)
public class ControlsListWidgetMixin {

	@Mixin(ControlsListWidget.KeyBindingEntry.class)
	public static class KeyBindingEntryMixin {

		@Unique
		private boolean dronebox$duplicate = false;

		@Shadow
		@Final
		private KeyBinding binding;

		@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
		private void duplicateDroneKeybindRendering(DrawContext instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
			if (this.dronebox$duplicate) {
				color = Colors.CYAN;
			}
			original.call(instance, x1, y1, x2, y2, color);
		}

		@Inject(method = "update", at = @At("HEAD"))
		private void updateSpecial(CallbackInfo ci) {
			this.dronebox$duplicate = KeyInterceptor.BINDINGS.contains(this.binding);
		}

		@ModifyExpressionValue(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/util/Formatting;RED:Lnet/minecraft/util/Formatting;"))
		private Formatting itIsNotWrong(Formatting original) {
			return this.dronebox$duplicate ? Formatting.AQUA : original;
		}

		@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;getTranslationKey()Ljava/lang/String;"))
		private void alsoAccountForTheDuplicate(CallbackInfo ci, @Local KeyBinding keyBinding) {
			if (KeyInterceptor.BINDINGS.contains(keyBinding)) {
				this.dronebox$duplicate = true;
			}
		}
	}
}
