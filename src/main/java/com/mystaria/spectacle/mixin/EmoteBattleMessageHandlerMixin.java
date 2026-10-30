package com.mystaria.spectacle.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.net.battle.BattleMessageHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket;

import net.minecraft.client.Minecraft;

import com.mystaria.spectacle.classic.ClassicEmotes;

/**
 * The battle's messages (moves ordered, winners) for the trainers' emotes, read <b>before</b> Cobblemon's handler:
 * DeltaClient may cancel it on the Cobblemon Academy server. Applied after DeltaClient's (higher priority), so this
 * head injection comes first.
 */
@Mixin(value = BattleMessageHandler.class, remap = false, priority = 1500)
public abstract class EmoteBattleMessageHandlerMixin {

	@Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/client/battle/BattleMessagePacket;Lnet/minecraft/client/Minecraft;)V", at = @At("HEAD"))
	private void spectacle$emotes(BattleMessagePacket packet, Minecraft client, CallbackInfo ci) {
		ClassicEmotes.onMessages(packet.getMessages());
	}
}
