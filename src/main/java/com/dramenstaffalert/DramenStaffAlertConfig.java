package com.dramenstaffalert;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("dramenStaffAlert")
public interface DramenStaffAlertConfig extends Config
{
	@ConfigItem(
		keyName = "highlightEquipped",
		position = 0,
		name = "Highlight when equipped",
		description = "Highlight fairy ring tiles when a Dramen/Lunar Staff is equipped or the Lumbridge & Draynor Elite Diary is complete"
	)
	default boolean highlightEquipped()
	{
		return true;
	}

	@ConfigItem(
		keyName = "equippedColor",
		position = 1,
		name = "Equipped highlight color",
		description = "Tile color when a staff is equipped or the elite diary is complete"
	)
	default Color equippedColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "highlightInventory",
		position = 2,
		name = "Highlight when in inventory",
		description = "Highlight fairy ring tiles when a Dramen/Lunar Staff is in inventory and still needs to be equipped"
	)
	default boolean highlightInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "inventoryColor",
		position = 3,
		name = "Inventory highlight color",
		description = "Tile color when a staff is in inventory and still needs to be equipped"
	)
	default Color inventoryColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "highlightMissing",
		position = 4,
		name = "Highlight when missing",
		description = "Highlight fairy ring tiles when a Dramen/Lunar Staff is required but neither equipped nor in inventory"
	)
	default boolean highlightMissing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "missingColor",
		position = 5,
		name = "Missing highlight color",
		description = "Tile color when a staff is required but neither equipped nor in inventory"
	)
	default Color missingColor()
	{
		return Color.RED;
	}
}
