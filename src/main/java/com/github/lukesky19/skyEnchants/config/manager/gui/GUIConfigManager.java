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
package com.github.lukesky19.skyEnchants.config.manager.gui;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.config.data.gui.EnchanterGUIConfig;
import com.github.lukesky19.skyEnchants.config.data.gui.PreviewGUIConfig;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the plugin's gui configurations.
 */
public class GUIConfigManager {
    private final @NotNull SkyEnchants skyEnchants;
    private @Nullable PreviewGUIConfig previewGUIConfig;
    private @Nullable EnchanterGUIConfig enchanterGUIConfig;

    private final @NotNull Path previewGUIConfigPath;
    private final @NotNull Path enchanterGUIConfigPath;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     */
    public GUIConfigManager(@NotNull SkyEnchants skyEnchants) {
        this.skyEnchants = skyEnchants;

        previewGUIConfigPath = Path.of(skyEnchants.getDataFolder() + File.separator + "gui" + File.separator + "preview.yml");
        enchanterGUIConfigPath = Path.of(skyEnchants.getDataFolder() + File.separator + "gui" + File.separator + "enchanter.yml");
    }

    /**
     * Get the {@link PreviewGUIConfig},
     * @return The {@link PreviewGUIConfig} or null.
     */
    public @Nullable PreviewGUIConfig getPreviewGUIConfig() {
        return previewGUIConfig;
    }

    /**
     * Get the {@link EnchanterGUIConfig},
     * @return The {@link EnchanterGUIConfig} or null.
     */
    public @Nullable EnchanterGUIConfig getEnchanterGUIConfig() {
        return enchanterGUIConfig;
    }

    /**
     * (Re-)load the GUI configurations.
     */
    public void reload() {
        ComponentLogger logger = skyEnchants.getComponentLogger();
        previewGUIConfig = null;
        enchanterGUIConfig = null;

        saveDefaultConfig();

        YamlConfigurationLoader previewLoader = createLoader(previewGUIConfigPath);
        YamlConfigurationLoader applyLoader = createLoader(enchanterGUIConfigPath);

        try {
            previewGUIConfig = previewLoader.load().get(PreviewGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load the preview GUI config. Error:" + configurateException.getMessage()));
        }

        try {
            enchanterGUIConfig = applyLoader.load().get(EnchanterGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load the enchanter GUI config. Error:" + configurateException.getMessage()));
        }
    }

    /**
     * Save the default config files if they don't exist.
     */
    private void saveDefaultConfig() {
        if(!previewGUIConfigPath.toFile().exists()) {
            skyEnchants.saveResource("gui" + File.separator + "preview.yml", false);
        }
        if(!enchanterGUIConfigPath.toFile().exists()) {
            skyEnchants.saveResource("gui" + File.separator + "enchanter.yml", false);
        }
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .defaultOptions(configurationOptions ->
                        configurationOptions.serializers(builder ->
                                builder.registerAll(PlatformUtils.getSerializers())))
                .build();
    }
}