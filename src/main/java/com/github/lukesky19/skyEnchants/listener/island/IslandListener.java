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
package com.github.lukesky19.skyEnchants.listener.island;

import com.github.lukesky19.skyEnchants.manager.block.BlockManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.events.island.IslandDeletedEvent;
import world.bentobox.bentobox.database.objects.IslandDeletion;

import java.util.concurrent.CompletableFuture;

/**
 * Listens for when an island is deleted and removes any player-placed block statuses.
 */
public class IslandListener implements Listener {
    private final @NotNull BlockManager blockManager;

    /**
     * Constructor
     * @param blockManager A {@link BlockManager} instance.
     */
    public IslandListener(@NotNull BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    /**
     * Listens to when an island is deleted and removes any player-placed statuses.
     * @param islandDeletedEvent A {@link IslandDeletedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(@NotNull IslandDeletedEvent islandDeletedEvent) {
        @NotNull IslandDeletion islandDeletion = islandDeletedEvent.getDeletedIslandInfo();
        @NotNull World world = islandDeletion.getWorld();
        int islandMinX = islandDeletion.getMinX();
        int islandMaxX = islandDeletion.getMaxX();
        int islandMinZ = islandDeletion.getMinZ();
        int islandMaxZ = islandDeletion.getMaxZ();

        int minX = Math.min(islandMinX, islandMaxX);
        int maxX = Math.max(islandMinX, islandMaxX);
        int minZ = Math.min(islandMinZ, islandMaxZ);
        int maxZ = Math.max(islandMinZ, islandMaxZ);

        for(int x = minX; x <= maxX; x++) {
            int chunkX = x >> 4;

            for(int z = minZ; z <= maxZ; z++) {
                int chunkZ = z >> 4;

                CompletableFuture<Chunk> chunkFuture;
                if(world.isChunkLoaded(chunkX, chunkZ)) {
                    chunkFuture = CompletableFuture.completedFuture(world.getChunkAt(chunkX, chunkZ));
                } else {
                    chunkFuture = world.getChunkAtAsync(chunkX, chunkZ, false);
                }

                int finalX = x;
                int finalZ = z;
                chunkFuture.thenAccept(chunk -> {
                    for(int y = 0; y < world.getMaxHeight(); y++) {
                        blockManager.removeLocationPlayerPlaced(chunk, world, finalX, y, finalZ);
                    }
                });
            }
        }
    }
}
