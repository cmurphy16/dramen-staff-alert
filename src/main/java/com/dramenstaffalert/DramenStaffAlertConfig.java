package com.dramenstaffalert;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("dramenStaffAlert")
public interface DramenStaffAlertConfig extends Config
{
	@ConfigItem(
		keyName = "highlightEquipped",
		name = "Highlight when equipped",
		description = "Show green fairy ring tiles when a Dramen/Lunar Staff is equipped or the Lumbridge & Draynor Elite Diary is complete"
	)
	default boolean highlightEquipped()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightInventory",
		name = "Highlight when in inventory",
		description = "Show yellow fairy ring tiles when a Dramen/Lunar Staff is in inventory and still needs to be equipped"
	)
	default boolean highlightInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightMissing",
		name = "Highlight when missing",
		description = "Show red fairy ring tiles when a Dramen/Lunar Staff is required but neither equipped nor in inventory"
	)
	default boolean highlightMissing()
	{
		return true;
	}
}
