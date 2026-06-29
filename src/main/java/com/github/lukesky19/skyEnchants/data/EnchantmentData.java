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
package com.github.lukesky19.skyEnchants.data;

import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * This record stores the enchantments to add and return.
 * @param enchantmentsToAdd The {@link Map} mapping {@link Enchantment}s to {@link Integer}s (levels) to add.
 * @param enchantmentsToReturn The {@link Map} mapping {@link Enchantment}s to {@link Integer}s (levels) to return.
 * @param enchantmentConflicts The {@link Map} mapping {@link Enchantment}s to {@link Integer}s (levels) to remove to add enchantments in the {@link #enchantmentsToAdd} map.
 * @param conflictToAdd The {@link Map} that maps the conflicting {@link EnchantmentLevel} to the {@link EnchantmentLevel} that would be in the {@link #enchantmentsToAdd} map.
 */
public record EnchantmentData(
        @NotNull Map<Enchantment, Integer> enchantmentsToAdd,
        @NotNull Map<Enchantment, Integer> enchantmentsToReturn,
        @NonNull Map<Enchantment, Integer> enchantmentConflicts,
        @NonNull Map<EnchantmentLevel, EnchantmentLevel> conflictToAdd) {}