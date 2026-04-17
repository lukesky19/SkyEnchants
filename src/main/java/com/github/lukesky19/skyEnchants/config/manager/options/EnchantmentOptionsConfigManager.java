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
package com.github.lukesky19.skyEnchants.config.manager.options;

import com.github.lukesky19.skyEnchants.config.data.misc.ApplicationCost;
import com.github.lukesky19.skyEnchants.config.data.misc.EnchantmentOptions;
import com.github.lukesky19.skyEnchants.config.data.options.EnchantmentOptionsConfig;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the {@link EnchantmentOptionsConfig} configuration.
 */
public class EnchantmentOptionsConfigManager extends SimpleConfigManager<EnchantmentOptionsConfig> {
    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public EnchantmentOptionsConfigManager(@NotNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDirectoryFile() + File.separator + "enchantment_options.yml"), EnchantmentOptionsConfig.class);
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        saveMissingEnchantmentOptions();
    }

    @Override
    public void saveDefaultConfiguration() {
        plugin.saveResource("enchantment_options.yml", false);
    }

    /**
     * There is currently no migration required so the value is just returned.
     * @param enchantmentOptionsConfig The {@link EnchantmentOptionsConfig} configuration.
     * @return The original {@link EnchantmentOptionsConfig} configuration.
     */
    @Override
    public @Nullable EnchantmentOptionsConfig migrateConfiguration(@NotNull EnchantmentOptionsConfig enchantmentOptionsConfig) {
        return enchantmentOptionsConfig;
    }

    /**
     * Assumes configuration is valid.
     * @return Always true.
     */
    @Override
    public boolean validateConfiguration(@Nullable EnchantmentOptionsConfig configuration) {
        return true;
    }

    /**
     * Save enchantment options for enchantments that are missing.
     * This is for other plugins that add custom enchantments or if new vanilla enchantments get added.
     */
    private void saveMissingEnchantmentOptions() {
        if(configuration == null) return;

        Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Map<String, EnchantmentOptions> enchantmentOptionsMap = configuration.enchantmentOptions();

        enchantmentRegistry.forEach(enchantment -> {
            NamespacedKey namespacedKey = enchantment.getKey();
            String enchantmentKeyName = namespacedKey.toString();

            EnchantmentOptions enchantmentConfig = enchantmentOptionsMap.getOrDefault(enchantmentKeyName, new EnchantmentOptions(false, false, new HashMap<>()));
            enchantmentOptionsMap.put(enchantmentKeyName, enchantmentConfig);

            Map<Integer, ApplicationCost> innerMap = enchantmentConfig.costByLevel();

            for(int level = enchantment.getStartLevel(); level <= enchantment.getMaxLevel(); level++) {
                if(!innerMap.containsKey(level)) {
                    innerMap.put(level, new ApplicationCost(100 * level, 10 * level, 10 * level));
                }
            }
        });

        configuration = new EnchantmentOptionsConfig(
                configuration.version(),
                enchantmentOptionsMap);

        saveConfiguration(configuration);
    }
}
