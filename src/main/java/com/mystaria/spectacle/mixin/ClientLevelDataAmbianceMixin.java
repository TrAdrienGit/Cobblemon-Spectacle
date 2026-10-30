package com.mystaria.spectacle.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientLevel;

import com.mystaria.spectacle.ambiance.BattleAmbiance;

/** A battle ambiance's time of day and its speed, on this client only: sun, moon, stars and light follow it. */
@Mixin(ClientLevel.ClientLevelData.class)
abstract class ClientLevelDataAmbianceMixin {

	@Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true)
	private void spectacle$ambianceTime(CallbackInfoReturnable<Long> cir) {
		cir.setReturnValue(BattleAmbiance.dayTime(cir.getReturnValueJ()));
	}
}
