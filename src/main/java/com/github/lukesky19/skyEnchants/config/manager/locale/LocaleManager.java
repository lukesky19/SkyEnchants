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
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NotNull SimpleConfigManager<Settings> settingsManager;
    private final @NotNull Locale DEFAULT_LOCALE = new Locale(
            "1.0.0.0",
                    "<dark_purple><bold>SkyEnchants</bold></dark_purple><gray> ▪ </gray>",
            List.of(
                    "<dark_purple>SkyEnchants is developed by <white><bold>lukeskywlker19</bold></white>.</dark_purple>",
                    "<dark_purple>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></dark_purple>",
                    " ",
                    "<dark_purple><bold>List of Commands:</bold></dark_purple>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple>",
                    "<white>/</white><dark_purple>skyenchants</dark_purple> <yellow>help</yellow>",
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
    public LocaleManager(@NotNull SkyPlugin plugin, @NotNull SimpleConfigManager<Settings> settingsManager) {
        super(plugin, Locale.class);
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    @Override
    public @NotNull Locale getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load plugin's locale due to plugin settings being null.</red>"));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load plugin's locale to use in settings.yml is null.</red>"));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));
        setConfigurationPath(path);

        super.loadConfiguration();
    }

    @Override
    public void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
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
    public @Nullable Locale migrateConfiguration(@NotNull Locale locale) {
        return locale;
    }

    /**
     * Validates if the locale is missing any strings.
     */
    @Override
    public boolean validateConfiguration(@Nullable Locale configuration) {
        if(configuration == null) return false;

        if(configuration.configVersion()  == null
                || configuration.prefix()  == null
                || configuration.reload()  == null
                || configuration.guiOpenError()  == null
                || configuration.insufficientFunds()  == null
                || configuration.insufficientExpLevels()  == null
                || configuration.insufficientPoints()  == null
                || configuration.enchanterError()  == null
                || configuration.anvilUseNotAllowed()  == null) {
            this.configuration = null;

            logger.error(AdventureUtil.deserialize("Your locale is missing one of the plugin's messages. The default locale will be used."));
            logger.info(AdventureUtil.deserialize("You can regenerate your locale file by deleting it or adding the missing messages to resolve the issue."));
        }

        return true;
    }
}
