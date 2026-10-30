package com.mystaria.spectacle;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import com.mystaria.spectacle.audio.BattleMusic;
import com.mystaria.spectacle.battle.BattleCameraDirector;
import com.mystaria.spectacle.battle.BattleCinematic;
import com.mystaria.spectacle.battle.BattleScreenButtons;
import com.mystaria.spectacle.battle.BattleSpectacle;
import com.mystaria.spectacle.classic.ClassicBattles;

/**
 * Cobblemon Spectacle: the staging of battles (camera, intros, set pieces, music), on its own or for Phantasmon. Wires
 * the tick, the render passes, the battle screen's buttons, the trainers' emotes and {@code /spectacle preview}.
 */
public class CobblemonSpectacle implements ClientModInitializer {

	private static final List<String> TYPES = List.of("normal", "fire", "water", "grass", "electric", "ice", "fighting",
			"poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy", "stellar");

	/**
	 * Spectacle's only key (N, Adrien 2026-10-30: the modpack has keys enough): its menu — the trainer's animation
	 * profile, the music (Phantasmon's own music menu when it is installed) and the settings.
	 */
	private static final net.minecraft.client.KeyMapping MENU_KEY = new net.minecraft.client.KeyMapping(
			"key.cobblemon_spectacle.menu", org.lwjgl.glfw.GLFW.GLFW_KEY_N, "key.categories.cobblemon_spectacle");

	@Override
	public void onInitializeClient() {
		net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(MENU_KEY);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (MENU_KEY.consumeClick()) {
				client.setScreen(new com.mystaria.spectacle.gui.SettingsScreen(null));
			}
			BattleCinematic.tick();
			BattleSpectacle.tick();
			BattleMusic.tick();
			ClassicBattles.tick();
			com.mystaria.spectacle.emote.TrainerEmotes.tick();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClassicBattles.reset();
			com.mystaria.spectacle.emote.TrainerEmotes.reset();
			BattleMusic.stop();
			BattleCameraDirector.stop();
			BattleSpectacle.clear();
			BattleCinematic.stop();
		});
		WorldRenderEvents.AFTER_ENTITIES.register(BattleCinematic::renderWorld);
		HudRenderCallback.EVENT.register((graphics, tickCounter) -> BattleCinematic.renderHud(graphics));
		BattleScreenButtons.register();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("spectacle").then(ClientCommandManager.literal("preview")
						.then(ClientCommandManager.literal("emote")
								.then(ClientCommandManager.argument("moment", StringArgumentType.word())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(
												com.mystaria.spectacle.emote.TrainerEmotes.Moment.values()).map(m -> m.id()), builder))
										.executes(context -> emote(context.getSource(), StringArgumentType.getString(context, "moment")))))
						.then(ClientCommandManager.literal("intro")
								.then(ClientCommandManager.argument("intro", StringArgumentType.word())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(BattleCinematic.Intro.values())
												.map(intro -> intro.name().toLowerCase(Locale.ROOT)), builder))
										.executes(context -> intro(context.getSource(), StringArgumentType.getString(context, "intro")))))
						.then(ClientCommandManager.argument("kind", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("mega", "primal", "zmove", "tera"), builder))
								.executes(context -> setPiece(context.getSource(), StringArgumentType.getString(context, "kind"), null))
								.then(ClientCommandManager.argument("type", StringArgumentType.word())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(TYPES, builder))
										.executes(context -> setPiece(context.getSource(), StringArgumentType.getString(context, "kind"),
												StringArgumentType.getString(context, "type"))))))));
	}

	/** {@code /spectacle preview emote <moment>}: a trainer's animation on the player themselves (third person). */
	private static int emote(FabricClientCommandSource source, String name) {
		var moment = Arrays.stream(com.mystaria.spectacle.emote.TrainerEmotes.Moment.values())
				.filter(m -> m.id().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (moment == null) {
			source.sendError(Component.translatable("cobblemon_spectacle.preview.unknown_emote", name));
			return 0;
		}
		if (!com.mystaria.spectacle.emote.TrainerEmotes.available()) {
			source.sendError(Component.translatable("cobblemon_spectacle.preview.no_emotes"));
			return 0;
		}
		Minecraft.getInstance().tell(() -> com.mystaria.spectacle.emote.TrainerEmotes.preview(moment));
		return Command.SINGLE_SUCCESS;
	}

	/** {@code /spectacle preview intro <name>}: one of the battle intros alone. */
	private static int intro(FabricClientCommandSource source, String name) {
		BattleCinematic.Intro intro = Arrays.stream(BattleCinematic.Intro.values())
				.filter(i -> i.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (intro == null) {
			source.sendError(Component.translatable("cobblemon_spectacle.preview.unknown_intro", name));
			return 0;
		}
		// Run from the chat screen: the intro's screen opens once the chat has closed.
		Minecraft.getInstance().tell(() -> BattleCinematic.preview(intro));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * {@code /spectacle preview <mega|primal|zmove|tera> [type]}: a set piece on the nearest Pokémon in the world,
	 * nothing changes on it (no Mega model).
	 */
	private static int setPiece(FabricClientCommandSource source, String kindName, String type) {
		BattleSpectacle.Kind kind = switch (kindName.toLowerCase(Locale.ROOT)) {
			case "mega" -> BattleSpectacle.Kind.MEGA;
			case "primal" -> BattleSpectacle.Kind.PRIMAL;
			case "zmove", "z" -> BattleSpectacle.Kind.Z_MOVE;
			case "tera" -> BattleSpectacle.Kind.TERA;
			default -> null;
		};
		if (kind == null) {
			source.sendError(Component.translatable("cobblemon_spectacle.preview.unknown_effect", kindName));
			return 0;
		}
		Minecraft mc = Minecraft.getInstance();
		PokemonEntity target = mc.level == null || mc.player == null ? null
				: mc.level.getEntitiesOfClass(PokemonEntity.class, mc.player.getBoundingBox().inflate(24))
						.stream().min(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(mc.player))).orElse(null);
		if (target == null) {
			source.sendError(Component.translatable("cobblemon_spectacle.preview.no_pokemon"));
			return 0;
		}
		// Run from the chat screen: once it has closed.
		mc.tell(() -> BattleSpectacle.preview(kind, target, type == null ? null : type.toLowerCase(Locale.ROOT)));
		return Command.SINGLE_SUCCESS;
	}
}
