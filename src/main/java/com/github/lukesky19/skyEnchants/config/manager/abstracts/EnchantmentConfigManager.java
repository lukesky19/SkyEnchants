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
package com.github.lukesky19.skyEnchants.config.manager.abstracts;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.interfaces.config.ISimpleConfigManager;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;

/**
 * This class can be extended to create a configuration manager class.
 * @param <C> The configuration object.
 */
public abstract class EnchantmentConfigManager<C> implements ISimpleConfigManager<C> {
    /**
     * The plugin's data directory as a {@link File}.
     */
    protected final @NotNull File dataDirectory;
    /**
     * The {@link ComponentLogger} of the plugin.
     */
    protected final @NotNull ComponentLogger logger;
    /**
     * The file name of the configuration.
     */
    protected @NotNull String fileName;
    /**
     * The {@link Path} the configuration is loaded from and saved to.
     */
    protected @NotNull Path configurationPath;
    /**
     * The path as a {@link String} to the embedded default configuration.
     */
    protected @NotNull String resourcePath;
    /**
     * The class of the configuration being loaded.
     */
    protected final @NotNull Class<C> configClass;

    /**
     * The configuration object.
     */
    protected @Nullable C configuration;

    /**
     * Constructor
     * @param dataDirectory The plugin's data directory as a {@link File}.
     * @param logger The plugin's {@link ComponentLogger}.
     * @param fileName The file name of the configuration.
     * @param configClass The {@link Class} of the {@link C} configuration object.
     */
    public EnchantmentConfigManager(
            @NotNull File dataDirectory,
            @NotNull ComponentLogger logger,
            @NotNull String fileName,
            @NotNull Class<C> configClass) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.fileName = fileName;
        this.configurationPath = Path.of(dataDirectory + File.separator + "enchantments" + File.separator + fileName);
        this.resourcePath = File.separator + "enchantments" + File.separator + fileName;
        this.configClass = configClass;
    }

    /**
     * This method does nothing.
     * @param configurationPath A {@link Path}.
     */
    @Override
    public void setConfigurationPath(@NotNull Path configurationPath) {}

    /**
     * Get the configuration. May be null.
     * @return The configuration or null.
     */
    @Override
    public @Nullable C getConfiguration() {
        return configuration;
    }

    /**
     * A method to reload the configuration.
     */
    @Override
    public void loadConfiguration() {
        configuration = null;

        if(!configurationPath.toFile().exists()) {
            saveBundledConfig();
        }

        YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            configuration = yamlConfigurationLoader.load().get(configClass);
            if(configuration == null) {
                logger.warn(AdventureUtil.deserialize("Failed to load configuration. Class name: " + this.getClass().getName()));
                return;
            }
            @NotNull C preMigrationConfiguration = configuration;

            // Migrate configuration
            configuration = migrateConfiguration(configuration);
            // If migration failed, return
            if(configuration == null) {
                logger.warn(AdventureUtil.deserialize("Migrated configuration is invalid. Class name: " + this.getClass().getName()));
                return;
            }

            // Check if the configuration is invalid
            if(!validateConfiguration()) {
                logger.warn(AdventureUtil.deserialize("Configuration validation failed. Class name: " + this.getClass().getName()));
                configuration = null;
                return;
            }

            // Save the migrated configuration if different
            if(configuration != preMigrationConfiguration) {
                saveConfiguration(configuration);
            }
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the configuration.
     */
    @Override
    public void saveConfiguration(@NotNull C configuration) {
        try {
            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(configClass, configuration);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.deserialize("Failed to save settings config file. Error: " + e.getMessage()));
        }
    }

    /**
     * Save the default bundled configuration.
     */
    protected void saveBundledConfig() {
        // Open the input stream
        try(InputStream inputStream = EnchantmentConfigManager.class.getResourceAsStream(resourcePath)) {
            // Display an error if the input stream is invalid.
            if(inputStream == null) {
                logger.error(AdventureUtil.deserialize("Failed to create the input stream. Resource Path: " + resourcePath));
                return;
            }

            // Create the File for the enchantments directory
            File enchantmentDirectory = new File(dataDirectory + File.separator + "enchantments");

            // Create the parent directories if they do not exist
            if (!enchantmentDirectory.exists() && !enchantmentDirectory.mkdirs()) {
                logger.error(AdventureUtil.deserialize("Failed to create directories for data directory: " + enchantmentDirectory));
                return;
            }

            // Create the output file
            File outputFile = new File(enchantmentDirectory, fileName);

            // Prevent overwriting existing file
            if(outputFile.exists()) return;

            // Write the data to disk
            try(OutputStream out = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int length;

                while((length = inputStream.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (IOException e) {
                logger.error(AdventureUtil.deserialize("Error saving default configuration " + outputFile.getName() + " to " + enchantmentDirectory + ". Resource path: " + resourcePath + ". Error: " + e.getMessage()));
            }
        } catch (IOException e) {
            logger.error(AdventureUtil.deserialize("Error saving default configuration. Resource path: " + resourcePath + ". Error: " + e.getMessage()));
        }
    }

    /**
     * Migrate the configuration.
     * @param configuration The configuration to migrate.
     * @return V the migrated configuration.
     */
    public abstract @Nullable C migrateConfiguration(@NotNull C configuration);

    /**
     * Validate the configuration in the class.
     * This is a convenience method and calls {@link #validateConfiguration(Object)} which should be favored instead.
     * @return true if valid, or false.
     */
    public boolean validateConfiguration() {
        return validateConfiguration(configuration);
    }

    /**
     * Validate the configuration.
     * @return true if valid, or false.
     */
    public abstract boolean validateConfiguration(@Nullable C configuration);
}
