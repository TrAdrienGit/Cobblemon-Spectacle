package com.mystaria.spectacle.emote;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Plays the trainers' animations: {@link PlayerAnimatorEmotes} when playerAnimator is installed. */
interface EmotePlayer {

	/** Fades into animation {@code id} on {@code player}; false if there is no such animation. */
	boolean play(Player player, ResourceLocation id);

	/** Whether the animation last played on {@code player} is still running (a loop always is). */
	boolean playing(Player player);

	/** Fades back to the player's own movements. */
	void stop(Player player);
}
