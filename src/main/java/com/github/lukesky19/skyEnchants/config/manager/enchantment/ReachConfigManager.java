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

import com.github.lukesky19.skyEnchants.config.data.enchantment.Reach;
import com.github.lukesky19.skyEnchants.config.manager.abstracts.EnchantmentConfigManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;

/**
 * This class manages the {@link Reach} configuration.
 */
public class ReachConfigManager extends EnchantmentConfigManager<Reach> {
    /**
     * Constructor
     * @param dataFolder The plugin's data folder.
     * @param logger The plugin's {@link ComponentLogger}.
     */
    public ReachConfigManager(@NonNull File dataFolder, @NonNull ComponentLogger logger) {
        super(dataFolder, logger, "reach.yml", Reach.class);
    }

    /**
     * There is currently no migration required so the value is just returned.
     * @param reach The {@link Reach} configuration.
     * @return The original {@link Reach} configuration.
     */
    @Override
    public @Nullable Reach migrateConfiguration(@NonNull Reach reach) {
        return reach;
    }

    /**
     * Assumes configuration is valid.
     * @param reach The configuration.
     * @return Always true.
     */
    @Override
    public boolean validateConfiguration(@Nullable Reach reach) {
        return true;
    }
}