package com.dramenstaffalert;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class StaffWarningOverlay extends Overlay
{
	// Normal player overhead chat lasts 150 client ticks (about three seconds).
	private static final int WARNING_DURATION_CYCLES = 150;

	private final Client client;
	private final DramenStaffAlertPlugin plugin;
	private final DramenStaffAlertConfig config;
	private boolean warningTriggered;
	private int warningStartCycle;

	@Inject
	StaffWarningOverlay(Client client, DramenStaffAlertPlugin plugin, DramenStaffAlertConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		GameState gameState = client.getGameState();
		if (gameState != GameState.LOGGED_IN)
		{
			if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.HOPPING)
			{
				resetWarning();
			}
			return null;
		}
		if (!config.showOverheadWarning() || plugin.meetsFairyRingStaffRequirement())
		{
			resetWarning();
			return null;
		}

		Player player = client.getLocalPlayer();
		String message = config.overheadWarningMessage();
		if (player == null || message == null || message.trim().isEmpty())
		{
			resetWarning();
			return null;
		}

		if (!warningTriggered)
		{
			warningTriggered = true;
			warningStartCycle = client.getGameCycle();
		}
		if (client.getGameCycle() - warningStartCycle >= WARNING_DURATION_CYCLES)
		{
			return null;
		}

		// Draw locally without replacing the player's real overhead chat or sending chat.
		graphics.setFont(FontManager.getRunescapeBoldFont());
		Point location = player.getCanvasTextLocation(graphics, message, player.getLogicalHeight() + 40);
		if (location != null)
		{
			OverlayUtil.renderTextLocation(graphics, location, message, Color.YELLOW);
		}
		return null;
	}

	void resetWarning()
	{
		warningTriggered = false;
		warningStartCycle = 0;
	}
}
