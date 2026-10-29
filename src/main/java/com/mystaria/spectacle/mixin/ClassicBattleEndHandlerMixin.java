package com.mystaria.spectacle.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.net.battle.BattleEndHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket;

import net.minecraft.client.Minecraft;

import com.mystaria.spectacle.classic.ClassicBattles;

/** The battle is over: camera, set pieces and music stop ({@link ClassicBattles}), once Cobblemon's own handler has run. */
@Mixin(value = BattleEndHandler.class, remap = false)
public abstract class ClassicBattleEndHandlerMixin {

	@Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/client/battle/BattleEndPacket;Lnet/minecraft/client/Minecraft;)V", at = @At("RETURN"))
	private void spectacle$stage(BattleEndPacket packet, Minecraft client, CallbackInfo ci) {
		ClassicBattles.onEnd();
	}
}
