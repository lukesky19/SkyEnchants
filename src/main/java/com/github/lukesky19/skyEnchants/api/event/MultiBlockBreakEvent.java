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
package com.github.lukesky19.skyEnchants.api.event;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This event should be fired after all blocks have been broken.
 */
public class MultiBlockBreakEvent extends Event implements Cancellable {
    private static final @NonNull HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;
    private final @NonNull Player player;
    private final @NonNull List<BlockState> blocks;
    private final @NonNull Collection<ItemStack> items;
    private final @NonNull Map<BlockState, Collection<ItemStack>> getBlockStateToItemStackMap;
    private final @NonNull Location location;

    /**
     * Constructor
     * @param player The {@link Player} to attribute the broken blocks to.
     * @param blocks The {@link List} of {@link BlockState}s that were broken.
     * @param items The {@link Collection} of {@link ItemStack}s that will be dropped from breaking the blocks.
     * @param getBlockStateToItemStackMap A {@link Map} mapping {@link BlockState}s to a {@link Collection} of {@link ItemStack}s.
     * @param location The {@link Location} the {@link ItemStack}s will be dropped at.
     */
    public MultiBlockBreakEvent(
            @NonNull Player player,
            @NonNull List<BlockState> blocks,
            @NonNull Collection<ItemStack> items,
            @NonNull Map<BlockState, Collection<ItemStack>> getBlockStateToItemStackMap,
            @NonNull Location location) {
        this.player = player;
        this.blocks = blocks;
        this.items = items;
        this.getBlockStateToItemStackMap = getBlockStateToItemStackMap;
        this.location = location;
    }

    /**
     * The player who initiated the block breaking.
     * @return A {@link Player}.
     */
    public @NonNull Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link List} of {@link BlockState}s that were broken.
     * @return A {@link List} of {@link BlockState}s.
     */
    public @NonNull List<BlockState> getBlocks() {
        return blocks;
    }

    /**
     * Get the {@link Collection} of {@link ItemStack}s to be dropped from the blocks being broken.
     * @apiNote If {@link PreMultiBlockBreakEvent#isDropItems()} was set to false, the list will be empty.
     * @return A {@link Collection} of {@link ItemStack}.
     */
    public @NonNull Collection<ItemStack> getItems() {
        return items;
    }

    /**
     * Get the {@link Map} mapping {@link BlockState}s to {@link Collection} of {@link ItemStack}s.
     * @apiNote {@link #getItems()} is the authoritative collection of items dropped. Make sure any changes modify that list.
     * @return A {@link Map} mapping {@link BlockState}s to a {@link Collection} of {@link ItemStack}s.
     */
    public @NonNull Map<BlockState, Collection<ItemStack>> getBlockStateToItemStackMap() {
        return getBlockStateToItemStackMap;
    }

    /**
     * Get the {@link Location} the items will be dropped at.
     * @return A {@link Location}.
     */
    public @NonNull Location getLocation() {
        return location;
    }

    /**
     * Get the {@link HandlerList} for this event.
     * @return A {@link HandlerList}.
     */
    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the {@link HandlerList} for this event.
     * @return A {@link HandlerList}.
     */
    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Checks if the event is cancelled.
     * @return true if cancelled, otherwise false.
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Set if this event should be cancelled.
     * @param isCancelled {@code true} if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}