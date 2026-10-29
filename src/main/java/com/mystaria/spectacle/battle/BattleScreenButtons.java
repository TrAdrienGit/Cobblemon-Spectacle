package com.mystaria.spectacle.battle;

import java.util.List;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.mystaria.spectacle.api.BattleButton;
import com.mystaria.spectacle.api.SpectacleHooks;
import com.mystaria.spectacle.audio.BattleMusic;

/**
 * A column of buttons over Cobblemon's battle screen while a battle is filmed (Adrien 2026-10-05): Spectacle's next
 * music and staged / free camera, plus those other mods add ({@link SpectacleHooks#addButton}: Phantasmon's turn timer).
 * Drawn over the screen and clicked through Fabric's screen events rather than added as widgets — Cobblemon rebuilds
 * its own widgets at every sub-screen (moves, switch...). Right side, under the opponent's tile.
 */
public final class BattleScreenButtons {

	private static final int W = 124;
	private static final int H = 18;
	private static final int GAP = 3;
	private static final int RIGHT_INSET = 12;
	/** Under Cobblemon's opponent tile (inset 10, tile 40 high). */
	private static final int TOP = 58;

	private static final int BG_TOP = 0xE0123A55;
	private static final int BG_BOTTOM = 0xE00B2236;
	private static final int BG_HOVER_TOP = 0xF01C5274;
	private static final int BG_HOVER_BOTTOM = 0xF0123A55;

	private BattleScreenButtons() {
	}

	public static void register() {
		SpectacleHooks.addButton(new BattleButton(BattleButton.CAMERA_ORDER - 10, () -> true,
				() -> Component.translatable("cobblemon_spectacle.buttons.music").getString(),
				() -> BattleMusic.skippable() ? BattleButton.CYAN : BattleButton.DIM, () -> true, BattleMusic::skip));
		SpectacleHooks.addButton(new BattleButton(BattleButton.CAMERA_ORDER, () -> true,
				() -> Component.translatable(BattleCameraDirector.active() ? "cobblemon_spectacle.buttons.camera_staged"
						: "cobblemon_spectacle.buttons.camera_free").getString(),
				() -> BattleCameraDirector.active() ? BattleButton.GREEN : BattleButton.CYAN, () -> true, BattleCameraDirector::toggle));
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!"BattleGUI".equals(screen.getClass().getSimpleName())) {
				return;
			}
			ScreenEvents.afterRender(screen).register((current, graphics, mouseX, mouseY, tickDelta) -> {
				if (BattleCameraDirector.inBattle() && !BattleSpectacle.playing()) {
					render(current, graphics, mouseX, mouseY);
				}
			});
			ScreenMouseEvents.allowMouseClick(screen).register((current, mouseX, mouseY, button) -> {
				if (button != 0 || !BattleCameraDirector.inBattle()) {
					return true;
				}
				BattleButton clicked = buttonAt(current, mouseX, mouseY);
				if (clicked == null) {
					return true;
				}
				Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
						net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1f));
				if (clicked.clickable().getAsBoolean()) {
					clicked.onClick().run();
				}
				return false; // ours: Cobblemon's screen doesn't see this click
			});
		});
	}

	private static List<BattleButton> shown() {
		return SpectacleHooks.buttons().stream().filter(button -> button.visible().getAsBoolean()).toList();
	}

	private static int x(Screen screen) {
		return screen.width - RIGHT_INSET - W;
	}

	private static int y(int index) {
		return TOP + index * (H + GAP);
	}

	private static BattleButton buttonAt(Screen screen, double mouseX, double mouseY) {
		int x = x(screen);
		List<BattleButton> shown = shown();
		for (int i = 0; i < shown.size(); i++) {
			if (mouseX >= x && mouseX < x + W && mouseY >= y(i) && mouseY < y(i) + H) {
				return shown.get(i);
			}
		}
		return null;
	}

	private static void render(Screen screen, GuiGraphics g, int mouseX, int mouseY) {
		Font font = Minecraft.getInstance().font;
		int x = x(screen);
		BattleButton hovered = buttonAt(screen, mouseX, mouseY);
		List<BattleButton> shown = shown();
		g.pose().pushPose();
		g.pose().translate(0, 0, 400);
		for (int i = 0; i < shown.size(); i++) {
			BattleButton button = shown.get(i);
			button(g, font, x, y(i), button == hovered && button.clickable().getAsBoolean(), button.label().get(),
					button.color().getAsInt());
		}
		g.pose().popPose();
	}

	private static void button(GuiGraphics g, Font font, int x, int y, boolean hovered, String label, int accent) {
		g.fillGradient(x, y, x + W, y + H, hovered ? BG_HOVER_TOP : BG_TOP, hovered ? BG_HOVER_BOTTOM : BG_BOTTOM);
		g.fill(x, y, x + W, y + 1, accent);
		g.fill(x, y + H - 1, x + W, y + H, accent);
		g.fill(x, y, x + 1, y + H, accent);
		g.fill(x + W - 1, y, x + W, y + H, accent);
		String text = font.width(label) > W - 8 ? font.plainSubstrByWidth(label, W - 12) + "…" : label;
		g.drawString(font, text, x + (W - font.width(text)) / 2, y + (H - 8) / 2, accent, true);
	}
}
