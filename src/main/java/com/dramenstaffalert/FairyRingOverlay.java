package com.dramenstaffalert;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class FairyRingOverlay extends Overlay
{
	private static final Color RED_FILL = new Color(255, 0, 0, 50);
	private static final Color GREEN_FILL = new Color(0, 255, 0, 50);
	private static final Color YELLOW_FILL = new Color(255, 255, 0, 50);

	private final Client client;
	private final DramenStaffAlertPlugin plugin;
	private final DramenStaffAlertConfig config;

	@Inject
	FairyRingOverlay(Client client, DramenStaffAlertPlugin plugin, DramenStaffAlertConfig config)
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
		Color outline;
		Color fill;
		if (plugin.meetsFairyRingStaffRequirement())
		{
			if (!config.highlightEquipped())
			{
				return null;
			}
			outline = Color.GREEN;
			fill = GREEN_FILL;
		}
		else if (plugin.hasStaffInInventory())
		{
			if (!config.highlightInventory())
			{
				return null;
			}
			outline = Color.YELLOW;
			fill = YELLOW_FILL;
		}
		else
		{
			if (!config.highlightMissing())
			{
				return null;
			}
			outline = Color.RED;
			fill = RED_FILL;
		}

		for (GameObject ring : plugin.getFairyRings())
		{
			if (ring.getWorldView() != client.getTopLevelWorldView()
				|| ring.getPlane() != client.getTopLevelWorldView().getPlane())
			{
				continue;
			}

			Polygon tile = Perspective.getCanvasTilePoly(client, ring.getLocalLocation());
			if (tile != null)
			{
				OverlayUtil.renderPolygon(graphics, tile, outline);
				graphics.setColor(fill);
				graphics.fillPolygon(tile);
			}
		}
		return null;
	}
}
