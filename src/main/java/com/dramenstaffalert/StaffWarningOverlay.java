package com.dramenstaffalert;

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

public class StaffWarningOverlay extends Overlay
{
	// Normal player overhead chat lasts 150 client ticks (about three seconds).
	private static final int WARNING_DURATION_CYCLES = 150;

	private final Client client;
	private final DramenStaffAlertPlugin plugin;
	private final DramenStaffAlertConfig config;
	private boolean warningTriggered;
	private boolean missingStaffWarning;
	private int warningStartCycle;
	private String cachedMessage;
	private OverheadMessage styledMessage;

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
		if (!warningTriggered)
		{
			return null;
		}
		boolean warningEnabled = missingStaffWarning ? config.showMissingStaffWarning() : config.showOverheadWarning();
		if (!warningEnabled || plugin.meetsFairyRingStaffRequirement()
			|| missingStaffWarning == plugin.hasStaffInInventory())
		{
			resetWarning();
			return null;
		}

		Player player = client.getLocalPlayer();
		String message = missingStaffWarning ? config.missingStaffWarningMessage() : config.overheadWarningMessage();
		if (player == null || message == null || message.trim().isEmpty())
		{
			resetWarning();
			return null;
		}

		int elapsed = client.getGameCycle() - warningStartCycle;
		if (elapsed >= WARNING_DURATION_CYCLES)
		{
			return null;
		}

		// Draw locally without replacing the player's real overhead chat or sending chat.
		if (!message.equals(cachedMessage))
		{
			cachedMessage = message;
			styledMessage = OverheadMessage.parse(message);
		}
		graphics.setFont(FontManager.getRunescapeBoldFont());
		Point location = player.getCanvasTextLocation(graphics, styledMessage.text, player.getLogicalHeight() + 40);
		if (location != null)
		{
			int centerX = location.getX() + graphics.getFontMetrics().stringWidth(styledMessage.text) / 2;
			styledMessage.render(graphics, centerX, location.getY(), elapsed, client.getGameCycle());
		}
		return null;
	}

	void triggerWarning()
	{
		resetWarning();
		if (client.getGameState() != GameState.LOGGED_IN || plugin.meetsFairyRingStaffRequirement())
		{
			return;
		}

		missingStaffWarning = !plugin.hasStaffInInventory();
		boolean warningEnabled = missingStaffWarning ? config.showMissingStaffWarning() : config.showOverheadWarning();
		if (warningEnabled)
		{
			warningTriggered = true;
			warningStartCycle = client.getGameCycle();
		}
	}

	void resetWarning()
	{
		warningTriggered = false;
		missingStaffWarning = false;
		warningStartCycle = 0;
		cachedMessage = null;
		styledMessage = null;
	}
}
