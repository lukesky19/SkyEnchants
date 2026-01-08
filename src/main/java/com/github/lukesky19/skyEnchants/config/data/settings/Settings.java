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
package com.github.lukesky19.skyEnchants.config.data.settings;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the plugin's settings and enchantment configuration.
 * @param configVersion The config version of the file.
 * @param locale The locale to use.
 * @param giveEnchantedBookForUnappliedEnchantments Whether enchantments not applied to the item in the enchanter GUI should be returned to the player.
 * @param giveUnappliedEnchantmentsAsOneBook Whether unapplied enchantments should be given as one book or multiple.
 */
@ConfigSerializable
public record Settings(
        @Nullable String configVersion,
        @Nullable String locale,
        boolean giveEnchantedBookForUnappliedEnchantments,
        boolean giveUnappliedEnchantmentsAsOneBook) {}