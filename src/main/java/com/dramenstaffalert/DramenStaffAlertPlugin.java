package com.dramenstaffalert;

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Dramen Staff Alert",
	description = "Alerts you when you need your Dramen/Lunar Staff equipped",
	tags = {"fairy", "ring", "dramen", "lunar", "staff", "alert", "tile"}
)
public class DramenStaffAlertPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FairyRingOverlay overlay;

	@Inject
	private StaffWarningOverlay staffWarningOverlay;

	private final Set<GameObject> fairyRings = new HashSet<>();
	private boolean active;

	@Provides
	@SuppressWarnings("unused") // Called by Guice to provide the plugin configuration.
	DramenStaffAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DramenStaffAlertConfig.class);
	}

	@Override
	protected void startUp()
	{
		active = true;
		staffWarningOverlay.resetWarning();
		overlayManager.add(overlay);
		overlayManager.add(staffWarningOverlay);
		clientThread.invoke(() ->
		{
			if (active && client.getGameState() == GameState.LOGGED_IN)
			{
				// Scan once when enabling the plugin; subsequent updates use object events.
				for (Tile[][] plane : client.getTopLevelWorldView().getScene().getTiles())
				{
					for (Tile[] column : plane)
					{
						for (Tile tile : column)
						{
							if (tile != null)
							{
								for (GameObject object : tile.getGameObjects())
								{
									trackFairyRing(object);
								}
							}
						}
					}
				}
			}
		});
	}

	@Override
	protected void shutDown()
	{
		active = false;
		overlayManager.remove(overlay);
		overlayManager.remove(staffWarningOverlay);
		staffWarningOverlay.resetWarning();
		fairyRings.clear();
	}

	@Subscribe
	@SuppressWarnings("unused") // Called by RuneLite's event bus.
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		trackFairyRing(event.getGameObject());
	}

	@Subscribe
	@SuppressWarnings("unused") // Called by RuneLite's event bus.
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		fairyRings.remove(event.getGameObject());
	}

	@Subscribe
	@SuppressWarnings("unused") // Called by RuneLite's event bus.
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		MenuAction action = event.getMenuAction();
		if (action != MenuAction.GAME_OBJECT_FIRST_OPTION
			&& action != MenuAction.GAME_OBJECT_SECOND_OPTION
			&& action != MenuAction.GAME_OBJECT_THIRD_OPTION
			&& action != MenuAction.GAME_OBJECT_FOURTH_OPTION
			&& action != MenuAction.GAME_OBJECT_FIFTH_OPTION)
		{
			return;
		}

		if (isFairyRing(event.getId()))
		{
			staffWarningOverlay.triggerWarning();
		}
	}

	@Subscribe
	@SuppressWarnings("unused") // Called by RuneLite's event bus.
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING
			|| event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING)
		{
			fairyRings.clear();
		}
	}

	private void trackFairyRing(GameObject object)
	{
		if (object == null)
		{
			return;
		}

		if (isFairyRing(object.getId()))
		{
			fairyRings.add(object);
		}
	}

	private boolean isFairyRing(int objectId)
	{
		ObjectComposition definition = client.getObjectDefinition(objectId);
		if (definition.getImpostorIds() != null)
		{
			definition = definition.getImpostor();
		}
		// Match the named center object, excluding decorative ring edges and corners.
		return definition != null && ("Fairy ring".equalsIgnoreCase(definition.getName())
			|| definition.getId() == ObjectID.POH_FAIRY_RING
			|| definition.getId() == ObjectID.POH_SPIRIT_RING);
	}

	Set<GameObject> getFairyRings()
	{
		return fairyRings;
	}

	boolean meetsFairyRingStaffRequirement()
	{
		return client.getVarbitValue(VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE) == 1
			|| hasStaffEquipped();
	}

	private boolean hasStaffEquipped()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null)
		{
			return false;
		}

		Item weapon = equipment.getItem(KitType.WEAPON.getIndex());
		return weapon != null && (weapon.getId() == ItemID.DRAMEN_STAFF
			|| weapon.getId() == ItemID.LUNAR_MOONCLAN_LIMINAL_STAFF);
	}

	boolean hasStaffInInventory()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		return inventory != null && (inventory.contains(ItemID.DRAMEN_STAFF)
			|| inventory.contains(ItemID.LUNAR_MOONCLAN_LIMINAL_STAFF));
	}
}
