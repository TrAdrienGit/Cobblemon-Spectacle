package com.mystaria.spectacle.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.net.battle.BattleSwitchPokemonHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket;

import net.minecraft.client.Minecraft;

import com.mystaria.spectacle.classic.ClassicBattles;

/** A Pokémon comes in: a close-up ({@link ClassicBattles}), once Cobblemon's own handler has run. */
@Mixin(value = BattleSwitchPokemonHandler.class, remap = false)
public abstract class ClassicBattleSwitchPokemonHandlerMixin {

	@Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/client/battle/BattleSwitchPokemonPacket;Lnet/minecraft/client/Minecraft;)V", at = @At("RETURN"))
	private void spectacle$stage(BattleSwitchPokemonPacket packet, Minecraft client, CallbackInfo ci) {
		ClassicBattles.onFocus(packet.getPnx());
	}
}
