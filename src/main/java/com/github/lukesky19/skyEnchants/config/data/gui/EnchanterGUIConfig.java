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
package com.github.lukesky19.skyEnchants.config.data.gui;

import com.github.lukesky19.skyEnchants.gui.EnchanterGUI;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the config to create the {@link EnchanterGUI}.
 * @param version The version of the config.
 * @param guiName The GUI's name.
 * @param guiType The {@link GUIType}.
 * @param filler The {@link ItemStackConfig} for the filler buttons.
 * @param playerInfoButton The {@link ButtonConfig} for the player info button.
 * @param costButton The {@link ButtonConfig} for the costs button.
 * @param inputItem The {@link ButtonConfig} for the placeholder of where the player inputs their item to enchant.
 * @param inputEnchantment The {@link ButtonConfig} for the placeholder of where the player inputs their enchanted book.
 * @param output The {@link ButtonConfig} for the placeholder of where the output item is displayed
 * @param dummyButtons The {@link List} of {@link ButtonConfig}s for the dummy buttons.
 */
@ConfigSerializable
public record EnchanterGUIConfig(
        int version,
        @Nullable String guiName,
        @Nullable GUIType guiType,
        @NotNull ItemStackConfig filler,
        @NotNull ButtonConfig playerInfoButton,
        @NotNull ButtonConfig costButton,
        @NotNull ButtonConfig inputItem,
        @NotNull ButtonConfig inputEnchantment,
        @NotNull ButtonConfig output,
        @NotNull List<ButtonConfig> dummyButtons) {}
