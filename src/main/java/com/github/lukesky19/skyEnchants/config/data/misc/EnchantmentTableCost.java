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

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * This record contains the experience cost when the enchantment is available in enchanting tables.
 * @param baseCost The base costs.
 * @param additionalCostPerLevel The additional cost per level.
 */
@ConfigSerializable
public record EnchantmentTableCost(
        int baseCost,
        int additionalCostPerLevel) {}
