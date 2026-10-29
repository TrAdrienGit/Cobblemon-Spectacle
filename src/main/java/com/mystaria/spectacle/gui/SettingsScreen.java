package com.mystaria.spectacle.gui;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.mystaria.spectacle.api.SpectacleHooks;
import com.mystaria.spectacle.config.SpectacleConfig;

/**
 * Spectacle's settings (phase S3), opened by Mod Menu: per kind of battle — Ghost (Phantasmon), PvP, NPC trainer —
 * the camera staged or free at the start, the intro (not for Ghost battles: their intro keeps both players in step),
 * our music; the music volume and the music menu. Saved on close.
 */
public final class SettingsScreen extends Screen {

	private final Screen parent;

	public SettingsScreen(Screen parent) {
		super(Component.translatable("cobblemon_spectacle.settings.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int center = width / 2;
		int colW = 100;
		int labelW = 110;
		int x0 = center - (labelW + 3 * (colW + 4)) / 2 + labelW;
		int y = 56;
		for (SpectacleConfig.Kind kind : SpectacleConfig.Kind.values()) {
			SpectacleConfig.Battle battle = SpectacleConfig.battle(kind);
			addRenderableWidget(CycleButton.onOffBuilder(battle.stagedCamera)
					.create(x0, y, colW, 20, Component.translatable("cobblemon_spectacle.settings.camera"),
							(button, value) -> battle.stagedCamera = value));
			if (kind != SpectacleConfig.Kind.GHOST) {
				addRenderableWidget(CycleButton.onOffBuilder(battle.intro)
						.create(x0 + colW + 4, y, colW, 20, Component.translatable("cobblemon_spectacle.settings.intro"),
								(button, value) -> battle.intro = value));
			}
			addRenderableWidget(CycleButton.onOffBuilder(battle.music)
					.create(x0 + 2 * (colW + 4), y, colW, 20, Component.translatable("cobblemon_spectacle.settings.music"),
							(button, value) -> battle.music = value));
			y += 24;
		}
		addRenderableWidget(new MusicScreen.VolumeSlider(center - 154, y + 8, 150, 20));
		addRenderableWidget(Button.builder(Component.translatable("cobblemon_spectacle.settings.tracks"),
				b -> minecraft.setScreen(SpectacleHooks.musicScreen(this))).bounds(center + 4, y + 8, 150, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
				.bounds(center - 50, y + 40, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		g.drawCenteredString(font, title, width / 2, 16, 0xFFFFFFFF);
		int colW = 100;
		int labelW = 110;
		int x0 = width / 2 - (labelW + 3 * (colW + 4)) / 2;
		int y = 62;
		for (SpectacleConfig.Kind kind : SpectacleConfig.Kind.values()) {
			g.drawString(font, Component.translatable("cobblemon_spectacle.settings.kind." + kind.name().toLowerCase(Locale.ROOT)),
					x0, y, 0xFFFFFFFF, true);
			y += 24;
		}
	}

	@Override
	public void onClose() {
		SpectacleConfig.save();
		minecraft.setScreen(parent);
	}
}
