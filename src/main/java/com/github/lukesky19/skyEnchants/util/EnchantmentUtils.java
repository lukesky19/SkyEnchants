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
package com.github.lukesky19.skyEnchants.util;

import com.github.lukesky19.skyEnchants.config.data.misc.EnchantmentOptions;
import com.github.lukesky19.skyEnchants.config.data.options.EnchantmentOptionsConfig;
import com.github.lukesky19.skyEnchants.config.data.settings.Settings;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.data.EnchantmentData;
import com.github.lukesky19.skyEnchants.data.EnchantmentLevel;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains utility methods for parsing enchantment data.
 */
public class EnchantmentUtils {
    /**
     * Constructor. Use static methods instead.
     * @throws IllegalAccessException if used.
     */
    public EnchantmentUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Use the static methods instead.");
    }

    /**
     * Calculate the {@link EnchantmentData} for the two input stacks.
     * @param logger A {@link ComponentLogger} instance.
     * @param enchantmentOptionsConfigManager An {@link EnchantmentOptionsConfigManager} instance.
     * @param input1ItemStack The first {@link ItemStack} to use as an input. May be null.
     * @param input2ItemStack The second {@link ItemStack} to use as an input. May be null.
     * @return The {@link EnchantmentData}.
     */
    public static @NonNull EnchantmentData getEnchantmentData(
            @NonNull ComponentLogger logger,
            @NonNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager,
            @Nullable ItemStack input1ItemStack,
            @Nullable ItemStack input2ItemStack) {
        Map<Enchantment, Integer> toAdd = new HashMap<>();
        Map<Enchantment, Integer> toReturn = new HashMap<>();
        Map<Enchantment, Integer> conflicts = new HashMap<>();
        Map<EnchantmentLevel, EnchantmentLevel> conflictToAdd = new HashMap<>();

        EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) return new EnchantmentData(toAdd, toReturn, conflicts, conflictToAdd);

        if(input1ItemStack == null || input2ItemStack == null) return new EnchantmentData(toAdd, toReturn, conflicts, conflictToAdd);

        ItemMeta meta1 = input1ItemStack.getItemMeta();
        ItemMeta meta2 = input2ItemStack.getItemMeta();

        Map<Enchantment, Integer> enchants1 = getEnchants(meta1);
        Map<Enchantment, Integer> enchants2 = getEnchants(meta2);

        for(Map.Entry<Enchantment, Integer> entry : enchants2.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            int maxLevel = enchantment.getMaxLevel();

            EnchantmentOptions enchantmentOptions = enchantmentOptionsConfig.enchantmentOptions().get(enchantment.getKey().toString());
            if(enchantmentOptions == null) {
                logger.warn(AdventureUtility.plain("No enchantment options configured for enchantment " + enchantment.getKey() + ". Assuming enchantment conflict overwrite is not allowed."));
            }

            boolean allowConflictOverwrite = enchantmentOptions != null && enchantmentOptions.allowEnchantmentOverwrite();
            boolean hasEnchant = hasEnchant(meta1, enchantment);
            boolean isConflicting = hasConflict(meta1, enchantment);
            int currentLevel = getLevel(meta1, enchantment);

            if(hasEnchant) {
                if(currentLevel < level) {
                    toAdd.put(enchantment, level);
                } else if(currentLevel == level && level < maxLevel) {
                    toAdd.put(enchantment, level + 1);
                } else {
                    toReturn.put(enchantment, level);
                }
            } else {
                if(isConflicting) {
                    if(allowConflictOverwrite) {
                        toAdd.put(enchantment, level);
                    } else {
                        toReturn.put(enchantment, level);
                    }
                } else {
                    toAdd.put(enchantment, level);
                }
            }
        }

        for(Map.Entry<Enchantment, Integer> e1 : enchants1.entrySet()) {
            for(Map.Entry<Enchantment, Integer> e2 : toAdd.entrySet()) {
                Enchantment enchantment1 = e1.getKey();
                Enchantment enchantment2 = e2.getKey();
                int level1 = e1.getValue();
                int level2 = e2.getValue();

                // Don't consider the same enchantment a conflict because the enchanter will simply combine without penalty.
                // I.e., Efficiency 1 + Efficiency 1 = Efficiency 2 (Combine Logic) vs Efficiency 1 -> Efficiency 1 (Conflict logic)
                if(enchantment1.equals(enchantment2)) continue;

                if(enchantment1.conflictsWith(enchantment2)) {
                    EnchantmentLevel conflictingEnchantment = new EnchantmentLevel(enchantment1, level1);
                    EnchantmentLevel enchantmentToAdd = new EnchantmentLevel(enchantment2, level2);
                    conflictToAdd.put(conflictingEnchantment, enchantmentToAdd);
                    conflicts.put(enchantment1, level1);
                    break;
                }
            }
        }

        return new EnchantmentData(toAdd, toReturn, conflicts, conflictToAdd);
    }

    /**
     * Give the unadded enchantments back to the player if configured to do so and the map contains any enchantments.
     * @param settings The plugin's {@link Settings}.
     * @param player The {@link Player}.
     * @param enchantmentData The {@link EnchantmentData}.
     */
    public static void returnUnappliedEnchantments(@NotNull Settings settings, @NonNull Player player, @NonNull EnchantmentData enchantmentData) {
        // If not configured to return unapplied enchantments, return
        if(!settings.giveEnchantedBookForUnappliedEnchantments()) return;
        // If the enchantments to return is empty, return
        if(enchantmentData.enchantmentsToReturn().isEmpty()) return;

        // Return the unapplied enchantments as either one book or multiple
        if(settings.giveUnappliedEnchantmentsAsOneBook()) {
            // Create the Enchanted Book ItemStack
            ItemStack returnStack = ItemType.ENCHANTED_BOOK.createItemStack();
            // Get the EnchantmentStorageMeta
            if(!(returnStack.getItemMeta() instanceof EnchantmentStorageMeta returnItemEnchantmentMeta)) return;

            // Add the enchantments to the enchanted book
            enchantmentData.enchantmentsToReturn().forEach((enchantment, level) ->
                    returnItemEnchantmentMeta.addStoredEnchant(enchantment, level, false));

            // Set the item meta of the ItemStack
            returnStack.setItemMeta(returnItemEnchantmentMeta);

            // Give the player the ItemStack
            PlayerUtil.giveItem(player.getInventory(), returnStack, returnStack.getAmount(), player.getLocation());
        } else {
            enchantmentData.enchantmentsToReturn().forEach((enchantment, level) -> {
                // Create the Enchanted Book ItemStack
                ItemStack returnStack = ItemType.ENCHANTED_BOOK.createItemStack();
                // Get the EnchantmentStorageMeta
                if(!(returnStack.getItemMeta() instanceof EnchantmentStorageMeta returnEnchantmentStorageMeta)) return;

                // Add the enchantment enchanted book
                returnEnchantmentStorageMeta.addStoredEnchant(enchantment, level, false);

                // Set the item meta of the ItemStack
                returnStack.setItemMeta(returnEnchantmentStorageMeta);

                // Give the player the ItemStack
                PlayerUtil.giveItem(player.getInventory(), returnStack, returnStack.getAmount(), player.getLocation());
            });
        }
    }

    /**
     * Get the {@link Map} of {@link Enchantment}s to levels as an {@link Integer}.
     * @param meta The {@link ItemMeta}.
     * @return The {@link Map} of {@link Enchantment}s to levels as an {@link Integer}.
     */
    public static @NonNull Map<Enchantment, Integer> getEnchants(@NonNull ItemMeta meta) {
        return meta instanceof EnchantmentStorageMeta storage ? storage.getStoredEnchants() : meta.getEnchants();
    }

    /**
     * Does the meta have the enchantment?
     * @param meta The {@link ItemMeta}.
     * @param enchantment The {@link Enchantment}.
     * @return true if the meta has the enchantment, otherwise false.
     */
    public static boolean hasEnchant(@NonNull ItemMeta meta, @NonNull Enchantment enchantment) {
        return meta instanceof EnchantmentStorageMeta storage
                ? storage.hasStoredEnchant(enchantment)
                : meta.hasEnchant(enchantment);
    }

    /**
     * Does the enchantment conflict with another enchantment?
     * @param meta The {@link ItemMeta}.
     * @param enchantment The {@link Enchantment}.
     * @return true if there is a conflict, false if not.
     */
    public static boolean hasConflict(@NonNull ItemMeta meta, @NonNull Enchantment enchantment) {
        return meta instanceof EnchantmentStorageMeta storage ? storage.hasConflictingStoredEnchant(enchantment) : meta.hasConflictingEnchant(enchantment);
    }

    /**
     * Get the enchantment level for the enchantment.
     * @param meta The {@link ItemMeta}.
     * @param enchantment The {@link Enchantment}.
     * @return The level.
     */
    public static int getLevel(@NonNull ItemMeta meta, @NonNull Enchantment enchantment) {
        return meta instanceof EnchantmentStorageMeta storage ? storage.getStoredEnchantLevel(enchantment) : meta.getEnchantLevel(enchantment);
    }
}
