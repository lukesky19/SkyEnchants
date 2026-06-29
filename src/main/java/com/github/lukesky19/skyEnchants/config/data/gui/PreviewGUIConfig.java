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

import com.github.lukesky19.skyEnchants.gui.PreviewGUI;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * This record contains the configuration for the {@link PreviewGUI}.
 * @param version The config version.
 * @param guiName The name for the GUI.
 * @param guiType The {@link GUIType}.
 * @param filler The {@link ItemStackConfig} for the filler buttons.
 * @param exit The {@link ButtonConfig} for the exit button.
 * @param dummyButtons The {@link List} of {@link ButtonConfig} for the dummy buttons. The custom enchantments are configured here to display preview items.
 */
@ConfigSerializable
public record PreviewGUIConfig(
        int version,
        @Nullable String guiName,
        @Nullable GUIType guiType,
        @NonNull ItemStackConfig filler,
        @NonNull ButtonConfig exit,
        @NonNull List<ButtonConfig> dummyButtons) {}
