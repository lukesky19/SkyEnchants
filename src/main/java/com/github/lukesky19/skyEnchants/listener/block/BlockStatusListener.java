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

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skyEnchants.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyEnchants.manager.block.BlockManager;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * This class listens for when a block is placed and marks it as player-placed.
 */
public class BlockStatusListener implements Listener {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull BlockManager blockManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param blockManager A {@link BlockManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public BlockStatusListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull BlockManager blockManager,
            @NonNull HookManager hookManager) {
        this.skyEnchants = skyEnchants;
        this.blockManager = blockManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens for when a block is broken and marks it as not player-placed 1 tick later.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if(blockBreakEvent.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        // This is done 1 tick later so that the value can be read by other events
        skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, () -> {
            Block block = blockBreakEvent.getBlock();

            // Don't remove player-placed status if the block is still stacked
            RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
            if(roseStackerHook.isHooked()) {
                if(roseStackerHook.isStackedBlock(block)) return;
            }

            // Don't remove player-placed status if there is a non-air block still there.
            if(!block.isEmpty()) return;

            blockManager.removeBlockPlayerPlaced(block);
        }, 1L);
    }

    /**
     * Listens for when a block is placed and marks it as player-placed.
     * @param blockPlaceEvent A {@link BlockPlaceEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        if(blockPlaceEvent.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        blockManager.setBlockPlayerPlaced(blockPlaceEvent.getBlock());
    }

    /**
     * Listens for when a block reaches it's maximum age and removes its player-placed status.
     * @param blockGrowEvent A {@link BlockGrowEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCropGrowth(BlockGrowEvent blockGrowEvent) {
        BlockState blockState = blockGrowEvent.getNewState();
        if(!(blockState.getBlockData() instanceof Ageable ageable)) return;
        if(ageable.getAge() != ageable.getMaximumAge()) return;

        blockManager.removeBlockPlayerPlaced(blockState.getBlock());
    }
}
