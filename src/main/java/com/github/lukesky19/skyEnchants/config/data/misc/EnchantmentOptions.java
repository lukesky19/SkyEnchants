package com.github.lukesky19.skyEnchants.config.data.misc;

import org.jspecify.annotations.NonNull;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

/**
 * This record stores configuration options for different enchantments.
 * @param disableAnvilUse Should the use of anvils to apply this enchantment be disabled?
 * @param disableEnchanterGuiUse Should the use of the enchanter GUI to apply this enchantment be disabled?
 * @param allowEnchantmentOverwrite Allow incompatible enchantments to be removed to add the other enchantment.
 * @param costByLevel The {@link Map} mapping enchantment levels to {@link ApplicationCost}s that are applied for adding enchantments.
 * @param penaltyByLevel The {@link Map} mapping enchantment levels to {@link ApplicationCost}s that are applied for penalties.
 */
@ConfigSerializable
public record EnchantmentOptions(
        boolean disableAnvilUse,
        boolean disableEnchanterGuiUse,
        boolean allowEnchantmentOverwrite,
        @NonNull Map<Integer, ApplicationCost> costByLevel,
        @NonNull Map<Integer, ApplicationCost> penaltyByLevel) {}
