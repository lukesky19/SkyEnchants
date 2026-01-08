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
package com.github.lukesky19.skyEnchants.config.data.options;

import com.github.lukesky19.skyEnchants.config.data.misc.EnchantmentOptions;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This record contains the configuration options related to enchantments in anvils and the enchanter GUI.
 * @param configVersion The config version
 * @param enchantmentOptions The {@link Map} mapping enchantment keys to {@link EnchantmentOptions}.
 */
@ConfigSerializable
public record EnchantmentOptionsConfig(
        @Nullable String configVersion,
        @NotNull Map<String, EnchantmentOptions> enchantmentOptions) {}
