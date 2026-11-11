# Dramen Staff Alert

A RuneLite plugin that highlights fairy ring tiles based on whether you have a Dramen or Lunar staff ready, and reminds you when you click a ring without one equipped.

## Fairy ring highlights

| Default color | Player status |
| --- | --- |
| Green | A Dramen or Lunar staff is equipped, or the Lumbridge & Draynor Elite Diary is complete. |
| Yellow | A staff is required and is in your inventory, but is not equipped. |
| Red | A staff is required and is neither equipped nor in your inventory. |

The plugin marks each ring's center tile with an outline and translucent fill. It also supports player-owned house fairy rings, including the combined spirit tree & fairy ring.

Each state has its own checkbox and color picker. All three highlights are enabled by default. Diary completion takes priority over equipment and inventory status.

## Overhead reminders

Clicking on a fairy ring interaction can display a reminder above your player:

- **Equip staff warning:** shown when you have a staff in your inventory but have not equipped it.
- **Missing staff warning:** shown when you have neither staff equipped nor in your inventory.

Both warnings are enabled by default and have separate checkboxes and custom-message fields. They work independently of the tile highlight settings. [Chat effects](https://oldschool.runescape.wiki/w/Chat_Interface#Chat_effects) are supported in custom messages.

Warnings appear only after a ring interaction is selected, not simply from walking nearby or removing a staff. Examine does not trigger them. A warning lasts about three seconds; clicking a ring again restarts the timer. Equipping a staff or completing the elite diary suppresses both warnings.

These messages are local overlays visible only to you. They do not send chat messages or replace your real overhead chat.
