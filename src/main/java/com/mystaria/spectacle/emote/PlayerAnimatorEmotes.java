package com.mystaria.spectacle.emote;

import java.util.Map;
import java.util.WeakHashMap;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * The trainers' animations on playerAnimator (KosmX, brought by Emotecraft): one layer of Spectacle's per player, above
 * Emotecraft's own, faded in and out. Loaded by name only when playerAnimator is installed ({@link TrainerEmotes}).
 */
final class PlayerAnimatorEmotes implements EmotePlayer {

	/** Above Emotecraft's layer (1000): a trainer in battle acts the battle. */
	private static final int LAYER_PRIORITY = 1500;
	private static final int FADE_TICKS = 4;

	private final Map<AbstractClientPlayer, ModifierLayer<IAnimation>> layers = new WeakHashMap<>();

	@Override
	public boolean play(Player player, ResourceLocation id) {
		if (!(player instanceof AbstractClientPlayer client)) {
			return false;
		}
		IPlayable animation = PlayerAnimationRegistry.getAnimation(id);
		if (animation == null) {
			return false;
		}
		layer(client).replaceAnimationWithFade(fade(), animation.playAnimation());
		return true;
	}

	@Override
	public java.util.Set<String> names(String namespace) {
		return PlayerAnimationRegistry.getModAnimations(namespace).keySet();
	}

	@Override
	public boolean playing(Player player) {
		ModifierLayer<IAnimation> layer = player instanceof AbstractClientPlayer client ? layers.get(client) : null;
		return layer != null && layer.getAnimation() != null && layer.getAnimation().isActive();
	}

	@Override
	public void stop(Player player) {
		ModifierLayer<IAnimation> layer = player instanceof AbstractClientPlayer client ? layers.get(client) : null;
		if (layer != null && layer.getAnimation() != null) {
			layer.replaceAnimationWithFade(fade(), null);
		}
	}

	private ModifierLayer<IAnimation> layer(AbstractClientPlayer player) {
		return layers.computeIfAbsent(player, ignored -> {
			ModifierLayer<IAnimation> layer = new ModifierLayer<>();
			PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(LAYER_PRIORITY, layer);
			return layer;
		});
	}

	private static AbstractFadeModifier fade() {
		return AbstractFadeModifier.standardFadeIn(FADE_TICKS, Ease.INOUTSINE);
	}
}
