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
package com.github.lukesky19.skyEnchants.config.data.enchantment;

import com.github.lukesky19.skyEnchants.config.data.enchantment.interfaces.IEnchantmentConfig;
import com.github.lukesky19.skyEnchants.config.data.misc.Registration;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NonNull;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

/**
 * This record contains the configuration for the haste enchantment.
 * @param version The config version.
 * @param enabled Is the enchantment enabled?
 * @param registration The {@link Registration} config for the enchantment.
 * @param temporaryUnbreakable Should the tool be made unbreakable for a temporary amount of time?
 * @param unbreakableDurationPerLevel The {@link Map} mapping enchantment levels to unbreakable durations.
 * @param effectDurationPerLevel The {@link Map} mapping enchantment levels to effect durations.
 * @param effectAmplifierPerLevel The {@link Map} mapping enchantment levels to effect amplifiers.
 * @param chancePerLevel The {@link Map} mapping enchantment levels to the chance to activate.
 */
@ConfigSerializable
public record Haste(
        int version,
        boolean enabled,
        @NonNull Registration registration,
        boolean temporaryUnbreakable,
        @NonNull Map<Integer, Long> unbreakableDurationPerLevel,
        @NonNull Map<Integer, Integer> effectDurationPerLevel,
        @NonNull Map<Integer, Integer> effectAmplifierPerLevel,
        @NonNull Map<Integer, Double> chancePerLevel) implements IEnchantmentConfig {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NonNull String getName() {
        return "Haste";
    }

    @Override
    public @NonNull TypedKey<@NonNull Enchantment> getTypedKey() {
        return EnchantmentKeys.create(Key.key("skyenchants", "haste"));
    }

    @Override
    public @NonNull Registration getRegistrationConfig() {
        return registration;
    }
}
