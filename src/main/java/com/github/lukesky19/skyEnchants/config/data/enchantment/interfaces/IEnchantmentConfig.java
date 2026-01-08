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
package com.github.lukesky19.skyEnchants.config.data.enchantment.interfaces;

import com.github.lukesky19.skyEnchants.config.data.misc.Registration;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

/**
 * This interface acts as a way to get specific configuration across multiple configuration classes.
 */
public interface IEnchantmentConfig {
    /**
     * Is the enchantment enabled?
     * @return true if enabled, false if not.
     */
    boolean isEnabled();

    /**
     * The enchantment name.
     * @return The enchantment name as a {@link String}.
     */
    @NotNull String getName();

    /**
     * Get the {@link TypedKey} of type {@link Enchantment}.
     * @return A {@link TypedKey} of type {@link Enchantment}.
     */
    @NotNull TypedKey<@NotNull Enchantment> getTypedKey();

    /**
     * Get the {@link Registration} config.
     * @return The {@link Registration}
     */
    @NotNull Registration getRegistrationConfig();
}
