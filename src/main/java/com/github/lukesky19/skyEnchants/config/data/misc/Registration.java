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
package com.github.lukesky19.skyEnchants.config.data.misc;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Range;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * This record contains the configuration to register an enchantment to the Enchantment registry during server startup.
 * @param maxLevel The enchantment's max level.
 * @param showInEnchantmentTable Should the enchantment be available in enchantment tables?
 * @param anvilCost The cost to apply the enchantment using an anvil.
 * @param weight The weight of the enchantment. Related to the enchantment in an enchantment table.
 * @param exclusive The {@link List} of enchantment {@link NamespacedKey} as a {@link String} that are exclusive with this one. Think infinity and mending
 * @param minimumCost The minimum {@link EnchantmentTableCost} for the enchantment in the enchantment table.
 * @param maximumCost The maximum {@link EnchantmentTableCost} for the enchantment in the enchantment table.
 * @param equipmentSlots The {@link List} of {@link EquipmentSlot} and or {@link EquipmentSlotGroup} names that the enchantment can activate in.
 * @param supportedItems The {@link List} of {@link ItemType} {@link NamespacedKey}s as a {@link String} that the enchantment can be applied to.
 */
@ConfigSerializable
public record Registration(
        @Range(from = 1, to = 255) int maxLevel,
        boolean showInEnchantmentTable,
        @Range(from = 0, to = Integer.MAX_VALUE) int anvilCost,
        @Range(from = 1, to = 1024) int weight,
        @NonNull List<String> exclusive,
        @NonNull EnchantmentTableCost minimumCost,
        @NonNull EnchantmentTableCost maximumCost,
        @NonNull List<String> equipmentSlots,
        @NonNull List<String> supportedItems) {}
