/*
    SkyEnchants adds custom enchantments.
    Copyright (C) 2026 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyEnchants.listener.block;

import com.github.lukesky19.skyEnchants.manager.block.BlockManager;
import com.github.lukesky19.skyTools.buildTool.event.BuildToolPlaceBlockEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens for when a block is placed by a build tool and marks it as player-placed.
 */
public class BuildToolListener implements Listener {
    private final @NotNull BlockManager blockManager;

    /**
     * Constructor
     * @param blockManager A {@link BlockManager} instance.
     */
    public BuildToolListener(@NotNull BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    /**
     * Listens for when a block is placed by the build tool and marks it as player-placed.
     * @param buildToolPlaceBlockEvent A {@link BuildToolPlaceBlockEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBuildToolPlaceBlock(BuildToolPlaceBlockEvent buildToolPlaceBlockEvent) {
        blockManager.setBlockPlayerPlaced(buildToolPlaceBlockEvent.getBlock());
    }
}
