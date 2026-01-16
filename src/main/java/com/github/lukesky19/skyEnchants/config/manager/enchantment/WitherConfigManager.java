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
package com.github.lukesky19.skyEnchants.config.manager.enchantment;

import com.github.lukesky19.skyEnchants.config.data.enchantment.Wither;
import com.github.lukesky19.skyEnchants.config.manager.abstracts.EnchantmentConfigManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * This class manages the {@link Wither} configuration.
 */
public class WitherConfigManager extends EnchantmentConfigManager<Wither> {
    /**
     * Constructor
     * @param dataFolder The plugin's data folder.
     * @param logger The plugin's {@link ComponentLogger}.
     */
    public WitherConfigManager(@NotNull File dataFolder, @NotNull ComponentLogger logger) {
        super(dataFolder, logger, "wither.yml", Wither.class);
    }

    /**
     * There is currently no migration required so the value is just returned.
     * @param wither The {@link Wither} configuration.
     * @return The original {@link Wither} configuration.
     */
    @Override
    public @Nullable Wither migrateConfiguration(@NotNull Wither wither) {
        return wither;
    }

    /**
     * Assumes configuration is valid.
     * @param wither The configuration.
     * @return Always true.
     */
    @Override
    public boolean validateConfiguration(@Nullable Wither wither) {
        return true;
    }
}