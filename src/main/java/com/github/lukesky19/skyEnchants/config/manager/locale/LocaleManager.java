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
package com.github.lukesky19.skyEnchants.config.manager.locale;

import com.github.lukesky19.skyEnchants.config.data.locale.Locale;
import com.github.lukesky19.skyEnchants.config.data.settings.Settings;
import com.github.lukesky19.skyEnchants.config.manager.settings.SettingsManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NonNull SimpleConfigManager<Settings> settingsManager;
    private final @NonNull Locale DEFAULT_LOCALE = new Locale(
            1,
                    "<dark_purple><bold>SkyEnchants</bold></dark_purple><gray> ▪ </gray>",
            List.of(
                    "<dark_purple>SkyEnchants is developed by <white><bold>lukeskywlker19</bold></white>.</dark_purple>",
                    "<dark_purple>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></dark_purple>",
                    " ",
                    "<dark_purple><bold>List of Commands:</bold></dark_purple>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>help</yellow>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>enchanter</yellow>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>info</yellow>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>preview</yellow>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>gui <enchanter | preview></yellow>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>reload</yellow>"),
            "<green>The plugin has reloaded successfully.</green>",
                    "<red>Unable to open this GUI because of a configuration error.</red>",
                    "Unable to add enchantments because you lack the funds to pay the cost to apply.",
                    "Unable to add the enchantments because you lack the experience levels to pay the cost to apply.",
                    "Unable to add enchantments because you lack the points to pay the cost to apply.",
                    "<red>The enchantment(s) could not be applied due a error.</red>",
                    "<red>The enchantment <enchantment> is not allowed to be used in anvils. Use <white>/skyenchants gui enchanter</white> instead.</red>");

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NonNull SkyPlugin plugin, @NonNull SimpleConfigManager<Settings> settingsManager) {
        super(plugin, Locale.class);
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    @Override
    public @NonNull Locale getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtility.plain("Failed to load plugin's locale due to plugin settings being null."));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtility.plain("Failed to load plugin's locale to use in settings.yml is null."));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + (localeString + ".yml"));
        setConfigurationPath(path);

        super.loadConfiguration();
    }

    @Override
    public void saveDefaultConfiguration() {
        Path path = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * There is currently no migration required so the value is just returned.
     * @param locale The {@link Locale} configuration.
     * @return The original {@link Locale} configuration.
     */
    @Override
    public @Nullable Locale migrateConfiguration(@NonNull Locale locale) {
        return locale;
    }

    /**
     * Validates if the locale is missing any strings.
     */
    @Override
    public boolean validateConfiguration(@Nullable Locale configuration) {
        if(configuration == null) return false;

        if(configuration.prefix()  == null
                || configuration.reload()  == null
                || configuration.guiOpenError()  == null
                || configuration.insufficientFunds()  == null
                || configuration.insufficientExpLevels()  == null
                || configuration.insufficientPoints()  == null
                || configuration.enchanterError()  == null
                || configuration.anvilUseNotAllowed()  == null) {
            this.configuration = null;

            logger.error(AdventureUtility.plain("Your locale is missing one of the plugin's messages. The default locale will be used."));
            logger.info(AdventureUtility.plain("You can regenerate your locale file by deleting it or adding the missing messages to resolve the issue."));
        }

        return true;
    }
}