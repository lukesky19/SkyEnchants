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
import com.github.lukesky19.skyEnchants.listener.enchantment.MultibreakEnchantmentListener;
import com.github.lukesky19.skyEnchants.util.Direction;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class is used to collect the blocks around the starting block and break those blocks.
 * The collection and breaking process is distributed over time for performance reasons.
 */
public class MultibreakProcessor {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull List<Location> multibreakLocationsToIgnore;
    private final @NotNull Player player;

    // Starting data
    private final @NotNull BlockType startingBlockType;
    private final @NotNull Location startingLocation;
    private final @NotNull Direction direction;

    // Settings
    private final boolean breakSimilarOnly;
    private final int sectionMaxBlocksToProcess;
    private final int totalMaxBlocksToProcess;
    private final long locationProcessingDelayTicks;
    private final long blockBreakDelayTicks;

    private final int widthMin;
    private final int widthMax;
    private final int heightMin;
    private final int heightMax;
    private final int depth;

    // Data
    private final @NotNull Deque<Block> blockQueue = new ArrayDeque<>();

    private int totalProcessed = 0;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param direction The {@link Direction} the player is facing.
     * @param startingBlock The starting {@link Block} the player broke.
     * @param startingBlockType The starting {@link BlockType}.
     * @param depth The depth of the area to break.
     * @param width The width of the area to break.
     * @param height The height of the area to break.
     * @param breakSimilarOnly Whether only similar blocks should be broken.
     * @param preventBelow Whether when breaking blocks horizontally, if the blocks below the player's feet should not be broken.
     * @param sectionMaxBlocksToProcess The maximum number of blocks to process per section.
     * @param totalMaxBlocksToProcess The maximum number of blocks to process across all sections.
     * @param locationProcessingDelayTicks The delay in ticks between location collection processing.
     * @param blockBreakDelayTicks The delay in ticks between block breaking processing.
     * @param multibreakLocationsToIgnore The {@link List} of {@link Location}s of blocks to ignore for the {@link MultibreakEnchantmentListener}.
     * @param player The {@link Player} who initiated the process and will break the collected blocks.
     */
    public MultibreakProcessor(
            @NotNull SkyEnchants skyEnchants,
            @NotNull Direction direction,
            @NotNull Block startingBlock,
            @NotNull BlockType startingBlockType,
            int depth,
            int width,
            int height,
            boolean breakSimilarOnly,
            boolean preventBelow,
            int sectionMaxBlocksToProcess,
            int totalMaxBlocksToProcess,
            long locationProcessingDelayTicks,
            long blockBreakDelayTicks,
            @NotNull List<Location> multibreakLocationsToIgnore,
            @NotNull Player player) {
        this.skyEnchants = skyEnchants;
        // Starting data
        this.direction = direction;
        this.startingBlockType = startingBlockType;
        this.startingLocation = startingBlock.getLocation().clone();
        this.depth = depth;
        // Settings
        this.breakSimilarOnly = breakSimilarOnly;
        this.sectionMaxBlocksToProcess = sectionMaxBlocksToProcess;
        this.totalMaxBlocksToProcess = totalMaxBlocksToProcess;

        this.locationProcessingDelayTicks = locationProcessingDelayTicks;
        this.blockBreakDelayTicks = blockBreakDelayTicks;

        // This is the list in MultibreakEnchantmentListener so that blocks broken here don't trigger an infinite loop
        this.multibreakLocationsToIgnore = multibreakLocationsToIgnore;
        // The Player involved
        this.player = player;

        // Calculate the width radius of the area to break.
        boolean widthEven = (width % 2 == 0);
        int widthRadius = widthEven ? (width / 2) : ((width - 1) / 2);
        // Calculate the min and max points for the width to break.
        widthMin = -widthRadius;
        widthMax = widthEven ? widthRadius - 1 : widthRadius;

        // Calculate the height radius of the area to break.
        boolean heightEven = (height % 2 == 0);
        int heightRadius = heightEven ? (height / 2) : ((height - 1) / 2);
        // Calculate the min and max points for the height to break.
        heightMin = preventBelow && !direction.isY ? -1 : -heightRadius;
        int heightMax = heightEven ? heightRadius - 1 : heightRadius;
        // If the prevent below option is true, shift the height to not break the floor the player is on.
        if(preventBelow && !direction.isY) heightMax = heightMax + (heightEven ? heightRadius - 1 : heightRadius) - 1;
        this.heightMax = heightMax;

        // Start the collection process
        skyEnchants.getServer().getScheduler().runTask(skyEnchants, () -> collectLocations(0, widthMin, heightMin));
    }

    /**
     * Collect the locations of blocks to break within the provided current depth, width, and height and the max depth, width, and height.
     * Current width and height are reset to the min width and min height at each new depth.
     * The process is limited to the section's max block count and a total max processed block count across all sections.
     */
    private void collectLocations(int currentDepth, int currentWidth, int currentHeight) {
        if(!player.isOnline() && !player.isConnected()) {
            cleanup();
            return;
        }

        int processed = 0;

        // Loop through the current and max depth while the section and total blocks processed are less than their limits
        while(currentDepth < depth && processed <= sectionMaxBlocksToProcess && totalProcessed <= totalMaxBlocksToProcess) {
            // Get the depth's starting location
            Location depthStartingLocation = startingLocation.clone().add(direction.vector.clone().multiply(currentDepth));
            // Set the current width to the minimum width for the new depth layer
            currentWidth = widthMin;

            // Loop through the min and max width while the section and total blocks processed are less than their limits
            while(currentWidth <= widthMax && processed <= sectionMaxBlocksToProcess && totalProcessed <= totalMaxBlocksToProcess) {
                // Set the current height to the minimum height for the new width layer
                currentHeight = heightMin;

                // Loop through the min and max height while the section and total blocks processed are less than their limits
                while(currentHeight <= heightMax && processed <= sectionMaxBlocksToProcess && totalProcessed <= totalMaxBlocksToProcess) {
                    // Get the location of the block based on the depth starting location, current width, and current height
                    @NotNull Location collectedBlockLocation = calculateBlockLocation(depthStartingLocation, currentWidth, currentHeight);
                    // Get the Block at the location
                    @NotNull Block collectedBlock = collectedBlockLocation.getBlock();
                    // Get the BlockType for the Block
                    @Nullable BlockType collectedBlockType = collectedBlock.getType().asBlockType();
                    // If the BlockType is null, increment the processed counters and current height then move to the next block if the conditions allow it
                    if(collectedBlockType == null) {
                        // Increment the blocks processed for this section
                        processed++;
                        // Increment the total blocks processed
                        totalProcessed++;
                        // Increment the current height
                        currentHeight++;
                        // Move to the next block in the loop if the conditions allow it
                        continue;
                    }

                    // If the break similar only option is true, and the block type doesn't match, increment the
                    // processed counters and current height then move to the next block if the conditions allow it
                    if(breakSimilarOnly && !collectedBlockType.equals(startingBlockType)) {
                        // Increment the blocks processed for this section
                        processed++;
                        // Increment the total blocks processed
                        totalProcessed++;
                        // Increment the current height
                        currentHeight++;
                        // Move to the next block in the loop if the conditions allow it
                        continue;
                    }

                    // Store the block to break
                    blockQueue.add(collectedBlock);

                    // Increment the blocks processed for this section
                    processed++;
                    // Increment the total blocks processed
                    totalProcessed++;
                    // Increment the current height
                    currentHeight++;
                }

                // If the process limits have not been reached, increment the current width
                if(processed <= sectionMaxBlocksToProcess && totalProcessed <= totalMaxBlocksToProcess) {
                    currentWidth++;
                }
            }

            // If the process limits have not been reached, increment the current depth
            if(processed <= sectionMaxBlocksToProcess && totalProcessed <= totalMaxBlocksToProcess) {
                currentDepth++;
            }
        }

        // If the max depth has not been reached and the total processed blocks hasn't hit the limit, schedule the next section
        // Otherwise start the block breaking process
        if(currentDepth < depth && totalProcessed <= totalMaxBlocksToProcess) {
            int finalCurrentDepth = currentDepth;
            int finalCurrentWidth = currentWidth;
            int finalCurrentHeight = currentHeight;

            // Schedule the next collection
            skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, () -> collectLocations(finalCurrentDepth, finalCurrentWidth, finalCurrentHeight), locationProcessingDelayTicks);
        } else {
            // If no blocks were collected, return
            if(blockQueue.isEmpty()) return;

            // Start block breaking process
            skyEnchants.getServer().getScheduler().runTask(skyEnchants, this::processBlockBreaks);
        }
    }

    /**
     * Calculates the {@link Location} of the block to collect.
     * @param depthStartingLocation The starting {@link Location} of the depth layer.
     * @param currentWidth The current width being processed.
     * @param currentHeight The current height being processed.
     * @return A {@link Location}
     */
    private @NotNull Location calculateBlockLocation(@NotNull Location depthStartingLocation, int currentWidth, int currentHeight) {
        if(direction.isX) {
            return depthStartingLocation.clone().add(0, currentHeight, currentWidth);
        } else if(direction.isY) {
            return depthStartingLocation.clone().add(currentWidth, 0, currentHeight);
        } else { // direction.isZ
            return depthStartingLocation.clone().add(currentWidth, currentHeight, 0);
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

        int blocksProcessed = 0;

        // While the collection queue isn't empty, and the processed counts haven't been exceeded, process the queue
        while(!blockQueue.isEmpty() && blocksProcessed <= sectionMaxBlocksToProcess) {
            // Get the next entry to process
            Block currentBlock = blockQueue.poll();
            // Get the BlockType for the current block
            @Nullable BlockType currentBlockType = currentBlock.getType().asBlockType();
            // If the BlockType is null, increment the processed counter and move to the next entry in the queue
            if(currentBlockType == null) {
                blocksProcessed++;
                continue;
            }

            // If the block has changed since it was collected, increment the processed counter and move to the next entry in the queue
            if(!currentBlockType.equals(startingBlockType)) {
                blocksProcessed++;
                continue;
            }

            // Add the block's location to be ignored for the multibreak enchantment listener to prevent an infinite loop
            multibreakLocationsToIgnore.add(currentBlock.getLocation());

            // Have the player break the block
            player.breakBlock(currentBlock);
        }

        // While the queue is not empty, queue the next section to be broken
        if(!blockQueue.isEmpty()) {
            skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, this::processBlockBreaks, blockBreakDelayTicks);
        }
    }

    /**
     * Cleanup any data stored.
     */
    private void cleanup() {
        blockQueue.clear();
    }
}
