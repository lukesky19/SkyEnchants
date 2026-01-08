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
import com.github.lukesky19.skyEnchants.config.data.enchantment.Replant;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.ReplantConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Listens for when a block is broken by a tool that contains the replant enchantment and applies the necessary actions.
 */
public class ReplantEnchantmentListener implements Listener {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull ComponentLogger logger;
    private final @NotNull ReplantConfigManager replantConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param replantConfigManager A {@link ReplantConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public ReplantEnchantmentListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull ReplantConfigManager replantConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.skyEnchants = skyEnchants;
        this.logger = skyEnchants.getComponentLogger();
        this.replantConfigManager = replantConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens for when a block is broken by a tool that contains the replant enchantment and applies the necessary actions.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreakReplantEnchantment(BlockBreakEvent blockBreakEvent) {
        @Nullable Replant replant = replantConfigManager.getConfiguration();
        if(replant == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate a replant enchantment due to invalid plugin settings."));
            return;
        }

        // If the multibreak enchantment is disabled, return
        if(!replant.isEnabled()) return;
        // If the multibreak enchantment is null, return
        @Nullable Enchantment replantEnchantment = enchantmentManager.getReplantEnchantment();
        if(replantEnchantment == null) return;

        @NotNull Map<Integer, Double> chancePerLevel = replant.chancePerLevel();

        // If there is no chance mapping, log an error and return
        if(chancePerLevel.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to activate the replant enchantment due to invalid plugin settings (No chance mapping)."));
            return;
        }

        Block block = blockBreakEvent.getBlock();
        if(!(block.getBlockData() instanceof Ageable ageable)) return;
        if(ageable.getAge() < ageable.getMaximumAge()) return;
        Material blockMaterial = block.getType();
        BlockType blockType = blockMaterial.asBlockType();
        if(blockType == null) return;
        ItemType seedType = getSeedItemTypeFromBlockType(blockType);
        if(seedType == null) return;

        // Get the Player that broke the Block
        Player player = blockBreakEvent.getPlayer();
        // Get the player's equipment
        EntityEquipment entityEquipment = player.getEquipment();

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(replant.getRegistrationConfig().equipmentSlots());
        // Get the max level of the replant enchantment
        int maxLevel = replant.getRegistrationConfig().maxLevel();

        // Create a new Random
        Random random = new Random();

        // Loop through the possible EquipmentSlots that the replant enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the replant enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(replantEnchantment)) continue;

            // Get the enchantment level of the replant enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(replantEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the chance that replant is applied
            @Nullable Double replantChance = chancePerLevel.get(enchantmentLevel);
            // Log an error if there is no chance configured for the enchantment level
            if(replantChance == null) {
                logger.error(AdventureUtil.deserialize("Unable to apply the replant enchantment due to invalid plugin settings (No chance to activate for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            if(replantChance < 1.0) {
                // Calculate the random chance
                double randomChance = random.nextDouble(0, 1);

                // If the random chance is greater than or equal to the replant chance, move to the next EquipmentSlot
                if(randomChance >= replantChance) continue;
            }

            // Get the player's Inventory
            Inventory inventory = player.getInventory();
            // Get the Inventory size
            int size = player.getInventory().getSize();
            // Loop through the inventory contents to find a seed for the crop broken
            for(int i = 0; i < size; i++) {
                ItemStack invItem = inventory.getItem(i);
                if(invItem == null || invItem.isEmpty()) continue;
                ItemType invType = invItem.getType().asItemType();
                if(invType == null) continue;
                if(!invType.equals(seedType)) continue;
                // Calculate the inventory ItemStack's updated amount
                int updatedAmount = invItem.getAmount() - 1;

                if(updatedAmount <= 0) {
                    // If less than or equal to 0, clear the item
                    inventory.setItem(i, ItemType.AIR.createItemStack());
                } else {
                    // Otherwise set the inventory ItemStack to the updated amount
                    invItem.setAmount(updatedAmount);
                }

                skyEnchants.getServer().getScheduler().runTaskLater(skyEnchants, () -> block.setType(blockMaterial), 1L);

                // Exit the method as we only need to replant once
                return;
            }
        }
    }

    /**
     * Get the {@link ItemType} that corresponds to seed of the {@link BlockType} provided.
     * @param blockType A {@link BlockType}.
     * @return An {@link ItemType} or null.
     */
    private @Nullable ItemType getSeedItemTypeFromBlockType(@NotNull BlockType blockType) {
        if(blockType.equals(BlockType.WHEAT)) {
            return ItemType.WHEAT_SEEDS;
        } else if(blockType.equals(BlockType.CARROTS)) {
            return ItemType.CARROT;
        } else if(blockType.equals(BlockType.POTATOES)) {
            return ItemType.POTATO;
        } else if(blockType.equals(BlockType.BEETROOTS)) {
            return ItemType.BEETROOT_SEEDS;
        } else if(blockType.equals(BlockType.TORCHFLOWER_CROP)) {
            return ItemType.TORCHFLOWER_SEEDS;
        } else {
            return null;
        }
    }
}
