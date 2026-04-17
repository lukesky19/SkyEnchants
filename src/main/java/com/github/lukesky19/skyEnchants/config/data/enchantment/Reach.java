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
import org.jetbrains.annotations.NotNull;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

/**
 * This record contains the configuration for the reach enchantment.
 * @param version The config version.
 * @param enabled Is the enchantment enabled?
 * @param registration The {@link Registration} config for the enchantment.
 * @param reachDistancePerLevel The {@link Map} mapping enchantment levels to reach distance amounts.
 */
@ConfigSerializable
public record Reach(
        int version,
        boolean enabled,
        @NotNull Registration registration,
        @NotNull Map<Integer, Double> reachDistancePerLevel) implements IEnchantmentConfig {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull String getName() {
        return "Reach";
    }

    @Override
    public @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
        return EnchantmentKeys.create(Key.key("skyenchants", "reach"));
    }

    @Override
    public @NotNull Registration getRegistrationConfig() {
        return registration;
    }
}