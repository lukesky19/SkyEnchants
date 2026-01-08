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
import com.github.lukesky19.skyEnchants.config.data.enchantment.Smelt;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.SmeltConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Listens for when a block is broken by a tool that contains the smelt enchantment and applies the smelting action as necessary.
 */
public class SmeltEnchantmentListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SmeltConfigManager smeltConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param smeltConfigManager A {@link SmeltConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public SmeltEnchantmentListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull SmeltConfigManager smeltConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.logger = skyEnchants.getComponentLogger();
        this.smeltConfigManager = smeltConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens for when an item is dropped by a destroyed block by a tool that contains the smelt enchantment and applies the smelting action as necessary.
     * @param blockDropItemEvent A {@link BlockDropItemEvent}.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItemSmeltEnchantment(BlockDropItemEvent blockDropItemEvent) {
        Smelt smelt = smeltConfigManager.getConfiguration();
        if(smelt == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate the smelt enchantment due to invalid settings."));
            return;
        }

        // If the smelt enchantment is disabled, return
        if(!smelt.enabled()) return;
        // If the smelt enchantment is null, return
        @Nullable Enchantment smeltEnchantment = enchantmentManager.getSmeltEnchantment();
        if(smeltEnchantment == null) return;

        @NotNull Map<Integer, Double> chancePerLevel = smelt.chancePerLevel();

        // If there is no chance mapping, log an error and return
        if(chancePerLevel.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to apply the smelt enchantment due to invalid settings (No chance mapping)."));
            return;
        }

        @NotNull List<Item> eventItems = blockDropItemEvent.getItems();

        // Get the Player that broke the Block
        Player player = blockDropItemEvent.getPlayer();
        // Get the player's equipment
        EntityEquipment entityEquipment = player.getEquipment();

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(smelt.getRegistrationConfig().equipmentSlots());
        // Get the max level of the smelt enchantment
        int maxLevel = smelt.getRegistrationConfig().maxLevel();

        // Create a new Random
        Random random = new Random();

        // Loop through the possible EquipmentSlots that the smelt enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the smelt enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(smeltEnchantment)) continue;

            // Get the enchantment level of the smelt enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(smeltEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the chance that smelt is applied
            @Nullable Double smeltChance = chancePerLevel.get(enchantmentLevel);
            // Log an error if there is no chance configured for the enchantment level
            if(smeltChance == null) {
                logger.error(AdventureUtil.deserialize("Unable to apply the smelt enchantment due to invalid plugin settings (No chance to activate for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            if(smeltChance < 1.0) {
                // Calculate the random chance
                double randomChance = random.nextDouble(0, 1);

                // If the random chance is greater than or equal to the smelt chance, move to the next EquipmentSlot
                if(randomChance >= smeltChance) continue;
            }

            List<Item> smeltedItems = new ArrayList<>();

            for(Item item : eventItems) {
                ItemStack smeltedItem = getSmeltedItemStack(item.getItemStack());
                if(smeltedItem != null) {
                    Item itemEntity = (Item) item.getWorld().spawnEntity(item.getLocation(), EntityType.ITEM);
                    itemEntity.setItemStack(smeltedItem);

                    smeltedItems.add(itemEntity);
                } else {
                    smeltedItems.add(item);
                }
            }

            eventItems.clear();

            eventItems.addAll(smeltedItems);

            return;
        }
    }

    private @Nullable ItemStack getSmeltedItemStack(@NotNull ItemStack item) {
        if (item.isEmpty()) return null;

        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while(recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if(!(recipe instanceof FurnaceRecipe furnaceRecipe)) continue;
            if(!furnaceRecipe.getInputChoice().test(item)) continue;

            ItemStack result = furnaceRecipe.getResult();

            int outputPerInput = result.getAmount(); // Get the output amount per input item
            int totalOutput = (item.getAmount() * outputPerInput); // Calculate total output based on input amount

            // Create new ItemStack with calculated amount
            return new ItemStack(result.getType(), totalOutput); // Return the smelted item with the calculated quantity
        }

        return null; // Return null if no smelting recipe exists
    }
}
