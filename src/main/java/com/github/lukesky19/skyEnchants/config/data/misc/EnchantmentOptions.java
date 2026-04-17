package com.github.lukesky19.skyEnchants.config.data.misc;

import org.jetbrains.annotations.NotNull;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

/**
 * This record stores configuration options for different enchantments.
 * @param disableAnvilUse Should the use of anvils to apply this enchantment be disabled?
 * @param disableEnchanterGuiUse Should the use of the enchanter GUI to apply this enchantment be disabled?
 * @param costByLevel The {@link Map} mapping enchantment levels to {@link ApplicationCost}s.
 */
@ConfigSerializable
public record EnchantmentOptions(
        boolean disableAnvilUse,
        boolean disableEnchanterGuiUse,
        @NotNull Map<Integer, ApplicationCost> costByLevel) {}
