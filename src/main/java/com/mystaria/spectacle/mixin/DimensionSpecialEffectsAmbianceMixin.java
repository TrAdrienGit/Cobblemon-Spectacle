package com.mystaria.spectacle.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.DimensionSpecialEffects;

import com.mystaria.spectacle.ambiance.BattleAmbiance;

/** A battle ambiance's clouds: hidden, or at another height. */
@Mixin(DimensionSpecialEffects.class)
abstract class DimensionSpecialEffectsAmbianceMixin {

	@Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
	private void spectacle$cloudHeight(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(BattleAmbiance.cloudHeight(cir.getReturnValueF()));
	}
}
