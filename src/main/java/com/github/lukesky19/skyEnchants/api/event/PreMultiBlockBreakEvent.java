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

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

import java.util.Queue;

/**
 * This event should be fired before the blocks are broken.
 */
public class PreMultiBlockBreakEvent extends Event implements Cancellable {
    private static final @NonNull HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;
    private final @NonNull Player player;
    private final @NonNull Queue<Block> blocks;
    private boolean dropItems = true;

    /**
     * Constructor
     * @param player The {@link Player} to attribute the broken blocks to.
     * @param blocks The {@link Queue} of {@link Block}s to be broken.
     */
    public PreMultiBlockBreakEvent(
            @NonNull Player player,
            @NonNull Queue<Block> blocks) {
        this.player = player;
        this.blocks = blocks;
    }

    /**
     * The player who initiated the block breaking.
     * @return A {@link Player}.
     */
    public @NonNull Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link Queue} of {@link Block}s that are to be broken.
     * @apiNote Changes to the queue will reflect what blocks are broken.
     * @return A {@link Queue} of {@link Block}s.
     */
    public @NonNull Queue<Block> getBlocks() {
        return blocks;
    }

    /**
     * Set whether items should be dropped when the block is broken or not.
     * @param dropItems true to drop items, false if not.
     */
    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    /**
     * Should items be dropped when the blocks are broken?
     * @return true if items should be dropped, false if not.
     */
    public boolean isDropItems() {
        return dropItems;
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