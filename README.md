# SkyEnchants
## Description
* SkyEnchants adds custom enchantments.

## Features
* Extensive configuration options for enchantments.
* A custom enchanter GUI to apply enchantments. 
* A preview GUI to preview custom enchantments.
* Custom cost configuration for the enchanter GUI using money, exp levels, and player points.
* Ability to prevent anvil usage with enchantments.

## Enchantments
* Double Drop - Causes non-player placed block drops to be doubled.
  * Containers (i.e., chests) are excluded.
  * Player-placed blocks are ignored, but are only tracked after the plugin was added.
* Double Jump - Allows the player to do one extra jump in the air.
* Durability - Prevents an item from breaking.
  * Also cancels the action that would break the item.
* Explosive - Triggers a small explosion when attacking a player.
* Haste - Gives the haste effect when mining blocks.
* Health - Gives the player more health.
* Magnet - Gives the player the items dropped when breaking a block.
* Multibreak - Breaks a configured area.
* Poison - Gives the poison effect when attacking a player.
* Reach - Gives the player extra reach distance.
* Replant - Automatically replant crops using seeds from the player's inventory.
* Shield Bash - Apply knockback to a player when you block their attack with a shield.
* Smelt - Automatically smelt the items a block drops.
* Speed - Give the player extra speed.
* Tree Feller - Automatically chop down a tree.
* Wither - Give the wither effect when attacking a player.

## Dependencies
* [SkyLib](https://github.com/lukesky19/SkyLib)

## Soft Dependencies
* BentoBox
* SkyTools
* PlayerPoints
* Vault

## Commands
* /skyenchants - The base command.
  * Alias: /enchants
* /skyenchants help - View the plugin's help message.
* /skyenchants reload - Reloads the plugin.
* /skyenchants gui enchanter - Open the enchanter GUI.
* /skyenchants gui preview - Open the preview GUI.

## Permissions
* `skyenchants.command.skyenchants` - Base Command Permission
* `skyenchants.command.skyenchants.help` - The permission to view the plugin's help message.
* `skyenchants.command.skyenchants.reload` - The permission to use the `/skyenchants reload` command.
* `skyenchants.command.skyenchants.gui` - The permission to use the `/skyenchants gui` command.
* `skyenchants.command.skyenchants.gui.enchanter` - The permission to use the `/skyenchants gui enchanter` command.
* `skyenchants.command.skyenchants.gui.preview` - The permission to use the `/skyenchants gui preview` command.

## FAQ
Q: How do I get the enchanted books?

A: They can be found in the creative inventory.

Q: The enchantments don't show up in the /enchant command?

A: You likely have a plugin that overrides the vanilla enchantment command. You can use `/minecraft:enchant`.

Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1, 26.1.1, and 26.1.2.
Note: Java 25 is required even on versions older than 26.1.

Q: I get the following error: "SkyEnchants has been compiled by a more recent version of the Java Runtime
(class file version 69.0), this version of the Java Runtime only recognizes class file versions up to 65.0"

A: SkyEnchants is compiled using Java 25 as part of it's support of 26.1 and beyond. SkyEnchants still works on older version as long as Java 25 is used.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot and Paper?

A: Only Paper is supported. There are no plans to support any other server software (i.e., Spigot, Folia).

## Issues, Bugs, or Suggestions
* Please create a new [GitHub Issue](https://github.com/lukesky19/SkyEnchants/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyEnchants and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```
* Go to [SkyTools](https://github.com/lukesky19/SkyTools) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

### Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
