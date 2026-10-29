package com.mystaria.spectacle.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

/**
 * What another mod (Phantasmon) plugs into Spectacle, set once at its startup. The rest of the public surface is the
 * public static methods of {@code BattleCinematic}, {@code BattleCameraDirector} and {@code BattleSpectacle}.
 * Client thread only.
 */
public final class SpectacleHooks {

	private static BiFunction<ClientLevel, UUID, Player> playerResolver = (level, uuid) -> null;
	private static IntPredicate glowKeeper = entityId -> false;
	private static BooleanSupplier foreignPacketCheck = () -> false;
	private static final List<BattleButton> buttons = new ArrayList<>();

	private SpectacleHooks() {
	}

	/**
	 * Who stands for a player that this world doesn't have (the opponent of the intro, the trainer of a battle):
	 * Phantasmon gives its Hub avatars. Asked only when the world has no such player.
	 */
	public static void setPlayerResolver(BiFunction<ClientLevel, UUID, Player> resolver) {
		playerResolver = resolver;
	}

	/** The player {@code uuid} of this world, else the resolver's stand-in, else null. */
	public static Player player(ClientLevel level, UUID uuid) {
		if (level == null || uuid == null) {
			return null;
		}
		Player player = level.getPlayerByUUID(uuid);
		return player != null ? player : playerResolver.apply(level, uuid);
	}

	/**
	 * Entities whose Tera glow survives the end of the filmed battle: Phantasmon's battles seen from afar keep
	 * theirs until their own end.
	 */
	public static void setGlowKeeper(IntPredicate keeper) {
		glowKeeper = keeper;
	}

	public static boolean keepsGlow(int entityId) {
		return glowKeeper.test(entityId);
	}

	/**
	 * Whether the battle packet Cobblemon's client handlers are running right now was handed to them by another mod
	 * for a battle it stages itself (Phantasmon's Ghost battles): Spectacle's own staging of Cobblemon battles leaves
	 * it alone.
	 */
	public static void setForeignPacketCheck(BooleanSupplier check) {
		foreignPacketCheck = check;
	}

	public static boolean handlingForeignPacket() {
		return foreignPacketCheck.getAsBoolean();
	}

	public static void addButton(BattleButton button) {
		buttons.add(button);
		buttons.sort(Comparator.comparingInt(BattleButton::order));
	}

	public static List<BattleButton> buttons() {
		return buttons;
	}
}
