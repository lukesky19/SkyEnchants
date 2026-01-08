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
package com.github.lukesky19.skyEnchants.processor;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.listener.enchantment.TreeFellerEnchantmentListener;
import com.github.lukesky19.skyEnchants.util.BlockTypeUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class is used to collect the blocks of a tree and break those blocks.
 * The collection and breaking process is distributed over time for performance reasons.
 */
public class TreeProcessor {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull List<Location> treeFellerLocationsToIgnore;
    private final @NotNull Player player;

    // Settings
    private final int minLeavesRequired;
    private final int sectionMaxBlocksToProcess;
    private final int totalMaxBlocksToProcess;
    private final long treeProcessingDelayTicks;
    private final long blockBreakDelayTicks;
    private final boolean includeLeaves;
    private final boolean includeMangroveRoots;

    // Data
    private final @NotNull Deque<Location> locationQueue = new ArrayDeque<>();
    private final @NotNull List<Location> processedLocations = new ArrayList<>();
    private final @NotNull Deque<Block> logBlocks = new ArrayDeque<>();
    private final @NotNull Deque<Block> leafBlocks = new ArrayDeque<>();

    private int totalProcessed = 0;
    private int leafCount = 0;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param treeFellerLocationsToIgnore The {@link List} of {@link Location}s of blocks to ignore for the {@link TreeFellerEnchantmentListener}.
     * @param startingBlock The {@link Block} the player broke to initiate the tree felling.
     * @param minLeavesRequired The minimum leaf count to be considered a tree.
     * @param sectionMaxBlocksToProcess The maximum number of blocks to process per section.
     * @param totalMaxBlocksToProcess The maximum number of blocks to process across all sections.
     * @param includeLeaves Should leaves be broken when the tree is felled?
     * @param includeMangroveRoots Should mangrove roots be broken when the tree is felled?
     * @param treeProcessingDelayTicks The delay in ticks between tree detection processing.
     * @param blockBreakDelayTicks The delay in ticks between tree breaking processing.
     * @param player The {@link Player} who initiated the process and will break the collected blocks.
     */
    public TreeProcessor(
            @NotNull SkyEnchants skyEnchants,
            @NotNull List<Location> treeFellerLocationsToIgnore,
            @NotNull Block startingBlock,
            int minLeavesRequired,
            int sectionMaxBlocksToProcess,
            int totalMaxBlocksToProcess,
            boolean includeLeaves,
            boolean includeMangroveRoots,
            long treeProcessingDelayTicks,
            long blockBreakDelayTicks,
            @NotNull Player player) {
        this.skyEnchants = skyEnchants;

        // Settings
        this.minLeavesRequired = minLeavesRequired;
        this.sectionMaxBlocksToProcess = sectionMaxBlocksToProcess;
        this.totalMaxBlocksToProcess = totalMaxBlocksToProcess;
        this.includeLeaves = includeLeaves;
        this.includeMangroveRoots = includeMangroveRoots;
        this.treeProcessingDelayTicks = treeProcessingDelayTicks;
        this.blockBreakDelayTicks = blockBreakDelayTicks;

        // This is the list in TreeFellerEnchantmentListener so that blocks broken here don't trigger an infinite loop
        this.treeFellerLocationsToIgnore = treeFellerLocationsToIgnore;

        // The Player Involved
        this.player = player;

        // Queue locations adjacent to starting location
        queueAdjacentLocations(startingBlock.getLocation());

        // Start the tree processor collection
        skyEnchants.getServer().getScheduler().runTask(skyEnchants, this::collectLocations);
    }

    /**
     * Collect the locations of blocks to break.
     */
    private void collectLocations() {
        if(!player.isOnline() && !player.isConnected()) {
            cleanup();
            return;
        }

        int processed = 0;

        while(!locationQueue.isEmpty() && processed <= sectionMaxBlocksToProcess && totalProcessed <= totalMaxBlocksToProcess) {
            Location location = locationQueue.poll();
            if(processedLocations.contains(location)) continue;
            Block block = location.getBlock();

            // Verify the BlockType
            @Nullable BlockType blockType = block.getType().asBlockType();
            if(blockType == null) continue;

            // Add the block to the appropriate queue
            if(BlockTypeUtils.isLogOrWoodBlock(block)) {
                logBlocks.add(block);

                // Queue adjacent locations
                queueAdjacentLocations(location);

                // Increment processed counters
                processed++;
                totalProcessed++;
                processedLocations.add(location);
            } else if(includeMangroveRoots && blockType.equals(BlockType.MANGROVE_ROOTS)) {
                logBlocks.add(block);

                // Queue adjacent locations
                queueAdjacentLocations(location);

                // Increment processed counters
                processed++;
                totalProcessed++;
                processedLocations.add(location);
            } else if(BlockTypeUtils.isLeafOrWartBlock(block)) {
                leafCount++;

                if(includeLeaves) {
                    leafBlocks.add(block);
                }

                // Queue adjacent locations
                queueAdjacentLocations(location);

                // Increment processed counters
                processed++;
                totalProcessed++;
                processedLocations.add(location);
            }
        }

        // If the location queue isn't empty and the max process count hasn't been reached, queue the next collection
        if(!locationQueue.isEmpty() && totalProcessed <= totalMaxBlocksToProcess) {
            skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, this::collectLocations, treeProcessingDelayTicks);
            return;
        }

        // If the leaf count is less than the minimum required leaves, return
        if(leafCount < minLeavesRequired) {
            cleanup();
            return;
        }

        // If no blocks were collected, return
        if(logBlocks.isEmpty() && leafBlocks.isEmpty()) {
            cleanup();
            return;
        }

        // Process block breaking process
        skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, this::processBlockBreaks, blockBreakDelayTicks);
    }

    /**
     * Queue the adjacent locations around the location provided for processing.
     * @apiNote Excludes locations already processed.
     * @param location The {@link Location}.
     */
    private void queueAdjacentLocations(@NotNull Location location) {
        for(int y = -1; y <= 1; y++) {
            for(int x = -1; x <= 1; x++) {
                for(int z = -1; z <= 1; z++) {
                    if(x == 0 && y == 0 && z == 0) continue;
                    Location newLocation = location.clone().add(x, y, z);
                    if(processedLocations.contains(newLocation)) continue;

                    locationQueue.add(newLocation);
                }
            }
        }
    }

    /**
     * Breaks the blocks that were collected and added to the breaking queue
     */
    private void processBlockBreaks() {
        if(!player.isOnline() && !player.isConnected()) {
            cleanup();
            return;
        }

        int processed = 0;

        while(!logBlocks.isEmpty() && processed <= sectionMaxBlocksToProcess) {
            Block block = logBlocks.poll();
            @Nullable BlockType blockType = block.getType().asBlockType();
            if(blockType == null) continue;
            if(!BlockTypeUtils.isLogOrWoodBlock(block) && !blockType.equals(BlockType.MANGROVE_ROOTS)) continue;

            // Add the block's location to be ignored for the tree feller enchantment listener to prevent an infinite loop
            treeFellerLocationsToIgnore.add(block.getLocation());

            // Have the player break the block
            player.breakBlock(block);

            processed++;
        }

        if(logBlocks.isEmpty() && leafBlocks.isEmpty()) {
            cleanup();
            return;
        }

        if(processed >= sectionMaxBlocksToProcess) {
            skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, this::processBlockBreaks, blockBreakDelayTicks);
        }

        while(!leafBlocks.isEmpty() && processed <= sectionMaxBlocksToProcess) {
            Block block = leafBlocks.poll();

            @Nullable BlockType blockType = block.getType().asBlockType();
            if(blockType == null) continue;
            if(!BlockTypeUtils.isLeafOrWartBlock(block)) continue;

            // Add the block's location to be ignored for the tree feller enchantment listener to prevent an infinite loop
            treeFellerLocationsToIgnore.add(block.getLocation());

            // Have the player break the block
            player.breakBlock(block);
        }

        if(leafBlocks.isEmpty()) {
            cleanup();
            return;
        }

        skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, this::processBlockBreaks, blockBreakDelayTicks);
    }

    /**
     * Cleanup any data stored.
     */
    private void cleanup() {
        locationQueue.clear();
        processedLocations.clear();
        logBlocks.clear();
        leafBlocks.clear();
    }
}
