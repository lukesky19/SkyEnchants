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
import com.github.lukesky19.skyEnchants.config.data.enchantment.Durability;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DurabilityConfigManager;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skyEnchants.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.BlockTypeUtils;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.lukesky19.skyEnchants.util.PluginUtils.compact;

/**
 * This class is used to collect the blocks of trees and break those blocks.
 */
public class TreeProcessor extends BlockProcessor {
    // Options
    private final int minLeavesRequired;
    private final boolean includeLeaves;
    private final boolean includeMangroveRoots;
    private final boolean includeFoliage;
    private final boolean preventBreaking;

    // Processing Data
    private int leafCount = 0;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param durabilityConfigManager A {@link DurabilityConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param startingBlock The starting {@link Block}.
     * @param minLeavesRequired The number of leaves required to be a tree.
     * @param includeLeaves If leaves should be broken.
     * @param includeMangroveRoots If mangrove roots should be broken.
     * @param includeFoliage If other tree foliage should be broken.
     * @param preventBreaking Whether the tool should be prevented from breaking or not.
     * @param player The {@link Player}.
     * @param tool The {@link ItemStack} used.
     * @param toolSlotNumber The slot number of the tool.
     */
    public TreeProcessor(
            @NotNull SkyEnchants skyEnchants,
            @NotNull DurabilityConfigManager durabilityConfigManager,
            @NotNull EnchantmentManager enchantmentManager,
            @NotNull HookManager hookManager,
            @NotNull Block startingBlock,
            int minLeavesRequired,
            boolean includeLeaves,
            boolean includeMangroveRoots,
            boolean includeFoliage,
            boolean preventBreaking,
            @NotNull Player player,
            @NotNull ItemStack tool,
            int toolSlotNumber) {
        super(skyEnchants, durabilityConfigManager, enchantmentManager, hookManager, startingBlock, player, tool, toolSlotNumber);

        // Settings
        this.minLeavesRequired = minLeavesRequired;
        this.includeLeaves = includeLeaves;
        this.includeMangroveRoots = includeMangroveRoots;
        this.includeFoliage = includeFoliage;
        this.preventBreaking = preventBreaking;

        // Queue starting location
        queueLocations(startingLocation);

        // Start the process
        processLocations();
    }

    /**
     * Queue the 26 adjacent locations around the provided location.
     * @apiNote Ignores locations in chunks not loaded.
     * @param center The starting {@link Location}.
     */
    @Override
    protected void queueLocations(@NotNull Location center) {
        int baseX = center.getBlockX();
        int baseY = center.getBlockY();
        int baseZ = center.getBlockZ();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    Location newLoc = new Location(world, baseX + dx, baseY + dy, baseZ + dz);
                    if(!newLoc.isChunkLoaded()) continue;
                    long packedLocation = pack(newLoc);
                    if(processedLocations.contains(packedLocation)) continue;

                    locationQueue.add(newLoc);
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
            Location location = locationQueue.poll();
            if(location == null) continue;
            long packedLocation = pack(location);
            if(processedLocations.contains(packedLocation)) continue;
            Block block = location.getBlock();
            // Ignore stacked blocks or spawners
            if(roseStackerHook.isHooked() && roseStackerHook.isStackedBlock(block)) continue;
            @Nullable BlockType blockType = block.getType().asBlockType();
            if(blockType == null) continue;

            if(BlockTypeUtils.isLogOrWoodBlock(block)
                    || (includeMangroveRoots && blockType.equals(BlockType.MANGROVE_ROOTS))
                    || (includeFoliage && BlockTypeUtils.isFoliageBlock(block))) {
                processedLocations.add(packedLocation);

                blockQueue.add(block);

                queueLocations(location);
            } else if(BlockTypeUtils.isLeafOrWartBlock(block)) {
                processedLocations.add(packedLocation);

                if(includeLeaves) {
                    blockQueue.add(block);
                }

                queueLocations(location);

                leafCount++;
            }
        }

        if(leafCount < minLeavesRequired) {
            cleanup();
            return;
        }

        if(blockQueue.isEmpty()) {
            cleanup();
            return;
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
     * Clear any data.
     */
    @Override
    protected void cleanup() {
        locationQueue.clear();
        processedLocations.clear();
        blockQueue.clear();
    }

    @Override
    protected boolean isProtected(@NotNull ItemStack tool, @NotNull Damageable damageable) {
        if(!damageable.hasDamage()) return false;
        int maxDurability = damageable.hasMaxDamage()
                ? damageable.getMaxDamage()
                : tool.getType().getMaxDurability();

        if(preventBreaking) {
            // We use 2 here instead of 1 because the BlockBreakEvent itself that triggered the tree processor will remove 1 durability.
            // If we used 1, the tool would break.
            return (maxDurability - damageable.getDamage()) <= 2;
        } else {
            if((maxDurability - damageable.getDamage()) > 1) return false;

            // Get the durability enchantment configuration and if null, return
            @Nullable Durability durability = durabilityConfigManager.getConfiguration();
            if(durability == null) return false;
            // If the durability enchantment is disabled, return
            if(!durability.isEnabled()) return false;
            // If the durability enchantment is null, return
            @Nullable Enchantment durabilityEnchantment = enchantmentManager.getDurabilityEnchantment();
            if(durabilityEnchantment == null) return false;

            // Get the EquipmentSlots
            List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(durability.getRegistrationConfig().equipmentSlots());
            // If the equipment slot is not configured to be affected by durability, return false
            if(!equipmentSlots.contains(EquipmentSlot.HAND)) return false;

            // If the ItemStack doesn't contain the durability enchantment, return false
            if(!tool.getEnchantments().containsKey(durabilityEnchantment)) return false;

            // Get the max level of the durability enchantment
            int maxLevel = durability.getRegistrationConfig().maxLevel();

            // Get the enchantment level of the durability enchantment
            int enchantmentLevel = tool.getEnchantmentLevel(durabilityEnchantment);

            // If the enchantment level is below or equal to the max level, the event should be cancelled.
            return enchantmentLevel <= maxLevel;
        }
    }
}