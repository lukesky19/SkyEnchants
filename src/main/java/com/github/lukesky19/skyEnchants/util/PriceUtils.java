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

import com.github.lukesky19.skyEnchants.config.data.locale.Locale;
import com.github.lukesky19.skyEnchants.config.data.misc.ApplicationCost;
import com.github.lukesky19.skyEnchants.config.data.misc.EnchantmentOptions;
import com.github.lukesky19.skyEnchants.config.data.options.EnchantmentOptionsConfig;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.data.EnchantmentData;
import com.github.lukesky19.skyEnchants.data.EnchantmentLevel;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skyEnchants.integration.hooks.EconomyHook;
import com.github.lukesky19.skyEnchants.integration.hooks.PlayerPointsHook;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.google.common.util.concurrent.AtomicDouble;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class contains utilities related to calculating the requirements to apply enchantments.
 */
public class PriceUtils {
    /**
     * Constructor. Use static methods instead.
     * @throws IllegalAccessException if used.
     */
    public PriceUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Use the static methods instead.");
    }

    /**
     * Calculate the costs required to apply the enchantments.
     * @param logger A {@link ComponentLogger} instance.
     * @param enchantmentOptionsConfigManager A {@link EnchantmentOptionsConfigManager} instance.
     * @param enchantmentData The {@link EnchantmentData}.
     * @return The {@link ApplicationCost} or null.
     */
    public static @Nullable ApplicationCost calculateCosts(
            @NonNull ComponentLogger logger,
            @NonNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager,
            @Nullable EnchantmentData enchantmentData) {
        @Nullable EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.error(AdventureUtility.plain("Unable to calculate application costs due to invalid enchantment options configuration."));
            return null;
        }

        if(enchantmentData == null) {
            logger.error(AdventureUtility.plain("Unable to calculate application costs due to invalid enchantment data."));
            return null;
        }

        double requiredMoney = 0.0;
        int requiredExpLevels = 0;
        int requiredPoints = 0;

        for(Map.Entry<Enchantment, Integer> entry : enchantmentData.enchantmentsToAdd().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            @Nullable ApplicationCost costs = getCost(enchantmentOptionsConfig, enchantment, level);
            if(costs == null) {
                logger.error(AdventureUtility.plain("Unable to calculate application costs due to missing enchantment configuration for " + enchantment.getKey() + " and level " + level));
                return null;
            }

            requiredMoney += costs.money();
            requiredExpLevels += costs.exp();
            requiredPoints += costs.points();
        }

        return new ApplicationCost(requiredMoney, requiredExpLevels, requiredPoints);
    }

    /**
     * Calculate the penalties to apply the enchantments.
     * @param logger A {@link ComponentLogger} instance.
     * @param enchantmentOptionsConfigManager A {@link EnchantmentOptionsConfigManager} instance.
     * @param enchantmentData The {@link EnchantmentData}.
     * @return The {@link ApplicationCost} or null.
     */
    public static @Nullable ApplicationCost calculatePenalties(
            @NonNull ComponentLogger logger,
            @NonNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager,
            @Nullable EnchantmentData enchantmentData) {
        @Nullable EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.error(AdventureUtility.plain("Unable to calculate application costs due to invalid enchantment options configuration."));
            return null;
        }

        if(enchantmentData == null) {
            logger.error(AdventureUtility.plain("Unable to calculate application costs due to invalid enchantment data."));
            return null;
        }

        AtomicDouble requiredMoney = new AtomicDouble();
        AtomicInteger requiredExpLevels = new AtomicInteger();
        AtomicInteger requiredPoints = new AtomicInteger();

        Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantmentData.enchantmentConflicts().entrySet().iterator();
        iterator.forEachRemaining(entry -> {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            @Nullable EnchantmentOptions enchantmentOptions = enchantmentOptionsConfig.enchantmentOptions().get(enchantment.getKey().toString());
            if(enchantmentOptions != null) {
                if(enchantmentOptions.allowEnchantmentOverwrite()) {
                    @Nullable ApplicationCost enchantmentPenalties = getPenalty(enchantmentOptionsConfig, enchantment, level);
                    if(enchantmentPenalties != null) {
                        requiredMoney.addAndGet(enchantmentPenalties.money());
                        requiredExpLevels.addAndGet(enchantmentPenalties.exp());
                        requiredPoints.addAndGet(enchantmentPenalties.points());

                    } else {
                        logger.warn(AdventureUtility.plain("Missing penalty configuration for " + enchantment.getKey() + " and level " + level));
                    }
                } else { // Remove if overwrite isn't allowed
                    // Remove the conflicting enchantment from the map of enchantment conflicts (via iterator)
                    iterator.remove();

                    // Get the enchantment and level that would be added if the conflict was removed.
                    EnchantmentLevel enchantmentLevelToAdd = enchantmentData.conflictToAdd().get(new EnchantmentLevel(enchantment, level));
                    if(enchantmentLevelToAdd != null) {
                        Enchantment addEnchantment = enchantmentLevelToAdd.enchantment();
                        int addLevel = enchantmentLevelToAdd.level();

                        // Remove the enchantment that would be added if the conflict was removed from the enchants to add map
                        enchantmentData.enchantmentsToAdd().remove(addEnchantment);

                        // Add the enchantment that would be added if the conflict was removed to the enchants to return
                        enchantmentData.enchantmentsToReturn().put(addEnchantment, addLevel);
                    }
                }
            } else { // Remove if invalid enchantment options
                // Remove the conflicting enchantment from the map of enchantment conflicts (via iterator)
                iterator.remove();

                // Get the enchantment and level that would be added if the conflict was removed.
                EnchantmentLevel enchantmentLevelToAdd = enchantmentData.conflictToAdd().get(new EnchantmentLevel(enchantment, level));
                if(enchantmentLevelToAdd != null) {
                    Enchantment addEnchantment = enchantmentLevelToAdd.enchantment();
                    int addLevel = enchantmentLevelToAdd.level();

                    // Remove the enchantment that would be added if the conflict was removed from the enchants to add map
                    enchantmentData.enchantmentsToAdd().remove(addEnchantment);

                    // Add the enchantment that would be added if the conflict was removed to the enchants to return
                    enchantmentData.enchantmentsToReturn().put(addEnchantment, addLevel);
                }
            }
        });

        return new ApplicationCost(requiredMoney.get(), requiredExpLevels.get(), requiredPoints.get());
    }

    /**
     * Get the {@link ApplicationCost} for the cost for the enchantment and enchantment level.
     * @param enchantmentOptionsConfig The plugin's {@link EnchantmentOptionsConfig}.
     * @param enchantment The {@link Enchantment}.
     * @param level The enchantment level.
     * @return The {@link ApplicationCost} or null.
     */
    public static @Nullable ApplicationCost getCost(@NonNull EnchantmentOptionsConfig enchantmentOptionsConfig, @NonNull Enchantment enchantment, int level) {
        @Nullable EnchantmentOptions enchantmentOptions = enchantmentOptionsConfig.enchantmentOptions().get(enchantment.getKey().toString());
        if(enchantmentOptions == null) return null;

        return enchantmentOptions.costByLevel().get(level);
    }

    /**
     * Get the {@link ApplicationCost} for the penalty for the enchantment and enchantment level.
     * @param enchantmentOptionsConfig The plugin's {@link EnchantmentOptionsConfig}.
     * @param enchantment The {@link Enchantment}.
     * @param level The enchantment level.
     * @return The {@link ApplicationCost} or null.
     */
    public static @Nullable ApplicationCost getPenalty(@NonNull EnchantmentOptionsConfig enchantmentOptionsConfig, @NonNull Enchantment enchantment, int level) {
        @Nullable EnchantmentOptions enchantmentOptions = enchantmentOptionsConfig.enchantmentOptions().get(enchantment.getKey().toString());
        if(enchantmentOptions == null) return null;

        return enchantmentOptions.penaltyByLevel().get(level);
    }

    /**
     * Check if the player has the required money, exp levels, and points.
     * @apiNote Also cancels the {@link InventoryClickEvent} and sends any error messages to the player or console as necessary.
     * @param logger A {@link ComponentLogger} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param player The {@link Player}.
     * @param costs The {@link ApplicationCost} for the costs.
     * @param penalties The {@link ApplicationCost} for the penalties.
     * @param inventoryClickEvent The {@link InventoryClickEvent}.
     * @return true if the player has the required amounts, otherwise false.
     */
    public static boolean hasRequiredAmounts(
            @NonNull ComponentLogger logger,
            @NonNull LocaleManager localeManager,
            @NonNull HookManager hookManager,
            @NonNull Player player,
            @Nullable ApplicationCost costs,
            @Nullable ApplicationCost penalties,
            @NonNull InventoryClickEvent inventoryClickEvent) {
        Locale locale = localeManager.getConfiguration();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Required amounts
        double money = 0.0;
        int exp = 0;
        int points = 0;

        // Add costs
        if(costs != null) {
            money += costs.money();
            exp += costs.exp();
            points += costs.points();
        } else {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.enchanterError()));
            inventoryClickEvent.setCancelled(true);
            return false;
        }

        // Add penalties
        if(penalties != null) {
            money += penalties.money();
            exp += penalties.exp();
            points += penalties.points();
        }

        // Check if the player has the required money
        if(money > 0) {
            if(economyHook.isHooked()) {
                if(economyHook.getBalance(player) < money) {
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.insufficientFunds()));
                    inventoryClickEvent.setCancelled(true);
                    return false;
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to remove the required money because the economy was not hooked into."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.enchanterError()));
                inventoryClickEvent.setCancelled(true);
                return false;
            }
        }

        // Check if the player has the required experience levels
        if(exp > 0) {
            if(player.getLevel() < exp) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.insufficientExpLevels()));
                inventoryClickEvent.setCancelled(true);
                return false;
            }
        }

        // Check if the player has the required points
        if(points > 0) {
            if(playerPointsHook.isHooked()) {
                if(playerPointsHook.getBalance(player) < points) {
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.insufficientPoints()));
                    inventoryClickEvent.setCancelled(true);
                    return false;
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to remove the required money because the PlayerPoints plugin was not hooked into."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.enchanterError()));
                inventoryClickEvent.setCancelled(true);
                return false;
            }
        }

        return true;
    }

    /**
     * Remove the required money, exp levels, and points from the player.
     * @param hookManager A {@link HookManager} instance.
     * @param player The {@link Player}.
     * @param costs The {@link ApplicationCost} for the costs.
     * @param penalties The {@link ApplicationCost} for the penalties.
     */
    public static void removeRequiredAmounts(
            @NonNull HookManager hookManager,
            @NonNull Player player,
            @Nullable ApplicationCost costs,
            @Nullable ApplicationCost penalties) {
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Required amounts
        double money = 0.0;
        int exp = 0;
        int points = 0;

        // Add costs
        if(costs != null) {
            money += costs.money();
            exp += costs.exp();
            points += costs.points();
        }

        // Add penalties
        if(penalties != null) {
            money += penalties.money();
            exp += penalties.exp();
            points += penalties.points();
        }

        if(money > 0 && economyHook.isHooked()) {
            economyHook.removeFromBalance(player, money);
        }

        if(exp > 0) {
            player.setLevel(player.getLevel() - exp);
        }

        if(points > 0 && playerPointsHook.isHooked()) {
            playerPointsHook.removeFromBalance(player, points);
        }
    }
}