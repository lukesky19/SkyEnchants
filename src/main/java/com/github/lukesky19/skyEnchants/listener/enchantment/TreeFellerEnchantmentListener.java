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
package com.github.lukesky19.skyEnchants.listener.enchantment;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.config.data.enchantment.TreeFeller;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.TreeFellerConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.processor.TreeProcessor;
import com.github.lukesky19.skyEnchants.util.BlockTypeUtils;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Listens for when a block is broken by a tool that contains the tree feller enchantment and applies the necessary actions.
 */
public class TreeFellerEnchantmentListener implements Listener {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull ComponentLogger logger;
    private final @NotNull TreeFellerConfigManager treeFellerConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;
    private final @NotNull List<Location> treeFellerLocationsToIgnore = new ArrayList<>();

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param treeFellerConfigManager A {@link TreeFellerConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public TreeFellerEnchantmentListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull TreeFellerConfigManager treeFellerConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.skyEnchants = skyEnchants;
        this.logger = skyEnchants.getComponentLogger();
        this.treeFellerConfigManager = treeFellerConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens for when a block is broken by a tool that contains the tree feller enchantment and applies the necessary actions.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreakTreeFeller(BlockBreakEvent blockBreakEvent) {
        Block block = blockBreakEvent.getBlock();
        Location blockLocation = block.getLocation();

        // If the event was cancelled, remove the location from the treeFellerLocationsToIgnore if it contains it and return
        if(blockBreakEvent.isCancelled()) {
            treeFellerLocationsToIgnore.remove(blockLocation);
            return;
        }

        // If the tree feller's settings are null, log and error and return
        @Nullable TreeFeller treeFeller = treeFellerConfigManager.getConfiguration();
        if(treeFeller == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate a tree feller enchantment due to an invalid settings."));
            return;
        }

        // If the tree feller enchantment is disabled, return
        if(!treeFeller.isEnabled()) return;
        // If the tree feller enchantment is null, return
        @Nullable Enchantment treeFellerEnchantment = enchantmentManager.getTreeFellerEnchantment();
        if(treeFellerEnchantment == null) return;

        // If the block location was broken by the plugin as a result of tree feller, remove the location and return
        if(treeFellerLocationsToIgnore.contains(blockLocation)) {
            treeFellerLocationsToIgnore.remove(blockLocation);
            return;
        }

        // Get the block's BlockType
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;
        if(!BlockTypeUtils.isLogOrWoodBlock(block)) return;
        // Get the Player that broke the Block
        Player player = blockBreakEvent.getPlayer();
        // Get the player's equipment
        EntityEquipment entityEquipment = player.getEquipment();

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(treeFeller.getRegistrationConfig().equipmentSlots());
        // Get the max level of the tree feller enchantment
        int maxLevel = treeFeller.getRegistrationConfig().maxLevel();

        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the tree feller enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(treeFellerEnchantment)) continue;

            // Get the enchantment level of the tree feller enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(treeFellerEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Create a new TreeProcessor to attempt to process the tree
            new TreeProcessor(
                    skyEnchants,
                    treeFellerLocationsToIgnore,
                    block,
                    treeFeller.minLeafCount(),
                    treeFeller.maxBlocksPerSection(),
                    treeFeller.totalMaxBlocks(),
                    treeFeller.includeLeaves(),
                    treeFeller.includeMangroveRoots(),
                    treeFeller.treeDetectionDelayTicks(),
                    treeFeller.blockBreakDelayTicks(),
                    player);

            return;
        }
    }
}
