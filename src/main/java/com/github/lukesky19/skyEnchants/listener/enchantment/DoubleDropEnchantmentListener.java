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
import com.github.lukesky19.skyEnchants.config.data.enchantment.DoubleDrop;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DoubleDropConfigManager;
import com.github.lukesky19.skyEnchants.manager.block.BlockManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class listens to when a block drops an item, and applies double drop chances for the double drop enchantment.
 */
public class DoubleDropEnchantmentListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull DoubleDropConfigManager doubleDropConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;
    private final @NotNull BlockManager blockManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param doubleDropConfigManager A {@link DoubleDropConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     * @param blockManager A {@link BlockManager} instance.
     */
    public DoubleDropEnchantmentListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull DoubleDropConfigManager doubleDropConfigManager,
            @NotNull EnchantmentManager enchantmentManager,
            @NotNull BlockManager blockManager) {
        this.logger = skyEnchants.getComponentLogger();
        this.doubleDropConfigManager = doubleDropConfigManager;
        this.enchantmentManager = enchantmentManager;
        this.blockManager = blockManager;
    }

    /**
     * Listens to when a block drops an item, and applies double drop chances for the double drop enchantment.
     * @param blockDropItemEvent A {@link BlockDropItemEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockItemDrop(BlockDropItemEvent blockDropItemEvent) {
        // Ignore player-placed blocks
        if(blockManager.isBlockPlayerPlaced(blockDropItemEvent.getBlock())) return;
        // Ignore containers -- The container's inventory is also included in the drop event so to prevent duplication of chest contents, we ignore containers.
        if(blockDropItemEvent.getBlockState() instanceof Container) return;

        @Nullable DoubleDrop doubleDrop = doubleDropConfigManager.getConfiguration();
        if(doubleDrop == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate a double drop enchantment due to an invalid settings."));
            return;
        }

        // If the double drop enchantment isn't enabled or is null, return
        if(!doubleDrop.isEnabled()) return;
        @Nullable Enchantment doubleDropEnchantment = enchantmentManager.getDoubleDropEnchantment();
        if(doubleDropEnchantment == null) return;

        @NotNull Map<Integer, Double> chancePerLevel = doubleDrop.chancePerLevel();

        // If there is no chance mapping, log an error and return
        if(chancePerLevel.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to apply the double drop enchantment due to invalid plugin settings (No chance mapping)."));
            return;
        }

        @NotNull List<Item> eventItems = blockDropItemEvent.getItems();
        @NotNull List<Item> unmodifiedItems = new ArrayList<>(eventItems);

        // Get the Player involved in the event
        Player player = blockDropItemEvent.getPlayer();
        // Get the Player's EntityEquipment
        EntityEquipment entityEquipment = player.getEquipment();
        // Get the EquipmentSlots that the haste enchantment can apply to.
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(doubleDrop.getRegistrationConfig().equipmentSlots());
        // Get the max level for the haste enchantment.
        int maxLevel = doubleDrop.getRegistrationConfig().maxLevel();

        // Create a new Random
        Random random = new Random();

        // Loop through the possible EquipmentSlots that the double drop enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the double drop enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(doubleDropEnchantment)) continue;

            // Get the enchantment level of the double drop enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(doubleDropEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the chance that double drop is applied
            @Nullable Double doubleDropChance = chancePerLevel.get(enchantmentLevel);
            // Log an error if there is no chance configured for the enchantment level
            if(doubleDropChance == null) {
                logger.error(AdventureUtil.deserialize("Unable to apply the double drop enchantment due to invalid plugin settings (No chance to activate for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            if(doubleDropChance < 1.0) {
                // Calculate the random chance
                double randomChance = random.nextDouble(0, 1);

                // If the random chance is greater than or equal to the replant chance, move to the next EquipmentSlot
                if(randomChance >= doubleDropChance) continue;
            }

            eventItems.addAll(unmodifiedItems);

            // Only activate double drop once
            return;
        }
    }
}
