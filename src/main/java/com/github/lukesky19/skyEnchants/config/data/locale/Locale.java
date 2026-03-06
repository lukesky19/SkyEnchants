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
package com.github.lukesky19.skyEnchants.config.data.locale;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * The plugin's locale configuration.
 * @param version The config version of the locale.
 * @param prefix The plugin's prefix.
 * @param help The plugin's help messages.
 * @param reload The plugin's reload message.
 * @param guiOpenError The message sent when a GUI fails to open due to an error.
 * @param insufficientFunds The message sent when a player lacks the funds to apply enchantments.
 * @param insufficientExpLevels The message sent when a player lacks the experience levels to apply enchantments.
 * @param insufficientPoints The message sent when a player lacks the player points to apply enchantments.
 * @param enchanterError The message sent when an enchantment cannot be applied due to an error.
 * @param anvilUseNotAllowed The message sent when an enchantment cannot be used in an anvil.
 */
@ConfigSerializable
public record Locale(
        int version,
        String prefix,
        List<String> help,
        String reload,
        String guiOpenError,
        String insufficientFunds,
        String insufficientExpLevels,
        String insufficientPoints,
        String enchanterError,
        String anvilUseNotAllowed) {}
