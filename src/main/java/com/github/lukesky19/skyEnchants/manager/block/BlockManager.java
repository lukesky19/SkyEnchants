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
package com.github.lukesky19.skyEnchants.manager.block;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to process player-placed blocks.
 */
public class BlockManager {
    private final @NotNull SkyEnchants skyEnchants;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     */
    public BlockManager(@NotNull SkyEnchants skyEnchants) {
        this.skyEnchants = skyEnchants;
    }

    /**
     * Was the block placed by a player?
     * @param block The {@link Block}.
     * @return true if player-placed, or false if not.
     */
    public boolean isBlockPlayerPlaced(@NotNull Block block) {
        return isLocationPlayerPlaced(block.getChunk(), block.getLocation());
    }

    /**
     * Was the block at the location provided placed by a player?
     * @param chunk The {@link Chunk} the location is in.
     * @param location The {@link Location}.
     * @return true if player-placed, or false if not.
     */
    public boolean isLocationPlayerPlaced(@NotNull Chunk chunk, @NotNull Location location) {
        return chunk.getPersistentDataContainer().has(getNamespacedKey(location));
    }

    /**
     * Store the block's location as player-placed.
     * @param block The {@link Block}.
     */
    public void setBlockPlayerPlaced(@NotNull Block block) {
        setLocationPlayerPlaced(block.getChunk(), block.getLocation());
    }

    /**
     * Store the location as player-placed.
     * @param chunk The {@link Chunk} the location is in.
     * @param location The {@link Location}.
     */
    public void setLocationPlayerPlaced(@NotNull Chunk chunk, @NotNull Location location) {
        chunk.getPersistentDataContainer().set(getNamespacedKey(location), PersistentDataType.INTEGER, 1);
    }

    /**
     * Remove the block's location as player-placed.
     * @param block The {@link Block}.
     */
    public void removeBlockPlayerPlaced(@NotNull Block block) {
        removeLocationPlayerPlaced(block.getChunk(), block.getLocation());
    }

    /**
     * Remove the location as player-placed.
     * @param chunk The {@link Chunk} the location is in.
     * @param location The {@link Location}.
     */
    public void removeLocationPlayerPlaced(@NotNull Chunk chunk, @NotNull Location location) {
        removeLocationPlayerPlaced(chunk, location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Remove the location as player-placed.
     * @param chunk The {@link Chunk} the location is in.
     * @param world The {@link World}.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    public void removeLocationPlayerPlaced(
            @NotNull Chunk chunk,
            @NotNull World world,
            int x,
            int y,
            int z) {
        PersistentDataContainer chunkPDC = chunk.getPersistentDataContainer();

        NamespacedKey key = getNamespacedKey(world, x, y, z);

        if(chunkPDC.has(key)) {
            chunkPDC.remove(key);
        }
    }

    /**
     * Get the {@link NamespacedKey} for the location provided.
     * @param location The {@link Location}.
     * @return The {@link NamespacedKey}.
     */
    private @NotNull NamespacedKey getNamespacedKey(@NotNull Location location) {
        return getNamespacedKey(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Get the {@link NamespacedKey} for the world and coordinates provided.
     * @param world The {@link World}.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The {@link NamespacedKey}.
     */
    private @NotNull NamespacedKey getNamespacedKey(
            @NotNull World world,
            int x, int y, int z) {
        return new NamespacedKey(skyEnchants, world.getName() + "." + x + "." + y + "." + z);
    }
}
