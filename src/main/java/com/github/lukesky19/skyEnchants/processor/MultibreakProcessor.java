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
import com.github.lukesky19.skyEnchants.api.event.MultiBlockBreakEvent;
import com.github.lukesky19.skyEnchants.api.event.PreMultiBlockBreakEvent;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DurabilityConfigManager;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skyEnchants.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.Direction;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.lukesky19.skyEnchants.util.PluginUtils.compact;

/**
 * This class is used to collect the blocks in an area and break those blocks.
 */
public class MultibreakProcessor extends BlockProcessor {
    // Options
    private final boolean breakSimilarOnly;

    // Data
    private final @NotNull BlockType startingBlockType;
    private final @NotNull Direction direction;
    private final int widthMin;
    private final int widthMax;
    private final int heightMin;
    private final int heightMax;
    private final int depth;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param durabilityConfigManager A {@link DurabilityConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param direction The {@link Direction} the player is facing.
     * @param startingBlock The starting {@link Block} the player broke.
     * @param startingBlockType The starting {@link BlockType}.
     * @param depth The depth of the area to break.
     * @param width The width of the area to break.
     * @param height The height of the area to break.
     * @param breakSimilarOnly Whether only similar blocks should be broken.
     * @param preventBelow Whether when breaking blocks horizontally, if the blocks below the player's feet should not be broken.
     * @param player The {@link Player} who initiated the process and will break the collected blocks.
     * @param tool The {@link ItemStack} used.
     * @param toolSlotNumber The slot number of the tool.
     */
    public MultibreakProcessor(
            @NotNull SkyEnchants skyEnchants,
            @NotNull DurabilityConfigManager durabilityConfigManager,
            @NotNull EnchantmentManager enchantmentManager,
            @NotNull HookManager hookManager,
            @NotNull Direction direction,
            @NotNull Block startingBlock,
            @NotNull BlockType startingBlockType,
            int depth,
            int width,
            int height,
            boolean breakSimilarOnly,
            boolean preventBelow,
            @NotNull Player player,
            @NotNull ItemStack tool,
            int toolSlotNumber) {
        super(skyEnchants, durabilityConfigManager, enchantmentManager, hookManager, startingBlock, player, tool, toolSlotNumber);

        // Settings
        this.breakSimilarOnly = breakSimilarOnly;

        // Data
        this.startingBlockType = startingBlockType;
        this.direction = direction;
        this.depth = depth;

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

        // Queue starting location
        queueLocations(startingLocation);

        // Start the process
        processLocations();
    }

    /**
     * Collect the locations of blocks to break within the provided current depth, width, and height and the max depth, width, and height.
     * Current width and height are reset to the min width and min height at each new depth.
     * @param location The starting {@link Location}.
     */
    @Override
    protected void queueLocations(@NotNull Location location) {
        for(int currentDepth = 0; currentDepth < depth; currentDepth++) {
            Location depthStartingLocation = location.clone().add(direction.vector.clone().multiply(currentDepth));

            for(int currentWidth = widthMin; currentWidth <= widthMax; currentWidth++) {
                for(int currentHeight = heightMin; currentHeight <= heightMax; currentHeight++) {
                    locationQueue.add(calculateBlockLocation(depthStartingLocation, currentWidth, currentHeight));
                }
            }
        }
    }

    /**
     * Process the location queue.
     */
    @Override
    protected void processLocations() {
        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
        while(!locationQueue.isEmpty()) {
            // Get the location from the queue
            Location location = locationQueue.poll();
            // Get the Block at the location
            @NotNull Block collectedBlock = location.getBlock();
            // Ignore stacked blocks or spawners
            if(roseStackerHook.isHooked() && roseStackerHook.isStackedBlock(collectedBlock)) return;
            // Get the BlockType for the Block
            @Nullable BlockType collectedBlockType = collectedBlock.getType().asBlockType();
            // If the BlockType is null, then move to the next block
            if(collectedBlockType == null) continue;

            // If the break similar only option is true, and the block type doesn't match, then move to the next block
            if(breakSimilarOnly && !collectedBlockType.equals(startingBlockType)) continue;

            // Store the block to break
            blockQueue.add(collectedBlock);
        }

        breakBlocks();
    }

    /**
     * Break the blocks.
     */
    @Override
    protected void breakBlocks() {
        PluginManager pluginManager = skyEnchants.getServer().getPluginManager();

        assert tool != null;  // Tool is only set to null during the block breaking process
        PreMultiBlockBreakEvent preMultiBlockBreakEvent = new PreMultiBlockBreakEvent(player, blockQueue);
        pluginManager.callEvent(preMultiBlockBreakEvent);
        if(preMultiBlockBreakEvent.isCancelled()) {
            cleanup();
            return;
        }

        @Nullable ItemMeta itemMeta = tool.getItemMeta();
        @Nullable Damageable durabilityMeta = null;
        if(itemMeta != null) {
            if(itemMeta instanceof Damageable damageable) {
                durabilityMeta = damageable;
            }
        }

        List<BlockState> blockStateList = new ArrayList<>();
        Collection<ItemStack> itemStackCollection = new ArrayList<>();
        Map<BlockState, Collection<ItemStack>> blockStateItemStackMap = new HashMap<>();

        while(!blockQueue.isEmpty()) {
            Block block = blockQueue.poll();
            BlockState blockState = block.getState(true);

            blockStateList.add(blockState);

            if(preMultiBlockBreakEvent.isDropItems()) {
                Collection<ItemStack> drops = block.getDrops(tool);
                itemStackCollection.addAll(drops);
                blockStateItemStackMap.put(blockState, drops);
            }

            block.setBlockData(BlockType.AIR.createBlockData(), false);

            if(durabilityMeta != null) {
                updateDurability(durabilityMeta);

                // If the tool broke or is protected by the durability enchantment, stop the loop
                if(tool == null || isProtected(tool, durabilityMeta)) break;
            }
        }

        // Update the tool's ItemMeta if not broken
        if(tool != null && durabilityMeta != null) tool.setItemMeta(durabilityMeta);

        Collection<ItemStack> compactedItemStacks = compact(itemStackCollection);
        MultiBlockBreakEvent multiBlockBreakEvent = new MultiBlockBreakEvent(player, blockStateList, compactedItemStacks, blockStateItemStackMap, startingLocation);
        pluginManager.callEvent(multiBlockBreakEvent);
        if(multiBlockBreakEvent.isCancelled()) {
            blockStateList.forEach(blockState -> blockState.update(true, false));
            cleanup();
            return;
        }

        compactedItemStacks.forEach(itemStack -> world.dropItemNaturally(startingLocation, itemStack));

        cleanup();
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
}