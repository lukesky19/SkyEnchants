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
package com.github.lukesky19.skyEnchants.manager.enchantment;

import com.github.lukesky19.skylib.paper.api.registry.RegistryUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class stores the {@link Enchantment}s for the custom enchantments loaded.
 * Enchantments that are disabled or not loaded will be null.
 */
public class EnchantmentManager {
    private final @Nullable Enchantment doubleDropEnchantment;
    private final @Nullable Enchantment doubleJumpEnchantment;
    private final @Nullable Enchantment durabilityEnchantment;
    private final @Nullable Enchantment explosiveEnchantment;
    private final @Nullable Enchantment hasteEnchantment;
    private final @Nullable Enchantment healthEnchantment;
    private final @Nullable Enchantment magnetEnchantment;
    private final @Nullable Enchantment multibreakEnchantment;
    private final @Nullable Enchantment poisonEnchantment;
    private final @Nullable Enchantment reachEnchantment;
    private final @Nullable Enchantment replantEnchantment;
    private final @Nullable Enchantment shieldBashEnchantment;
    private final @Nullable Enchantment smeltEnchantment;
    private final @Nullable Enchantment speedEnchantment;
    private final @Nullable Enchantment treeFellerEnchantment;
    private final @Nullable Enchantment witherEnchantment;

    /**
     * Constructor
     * Retrieves the {@link Enchantment}s for all custom enchantments loaded and stores them in memory.
     * Any enchantments not found (not enabled or loaded) will be null.
     * @apiNote Changing the enabled option in the enchantment's settings after the server has started will not change the data stored here.
     * @param logger A {@link ComponentLogger} instance.
     */
    public EnchantmentManager(@NonNull ComponentLogger logger) {
        doubleDropEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:double_drop").orElse(null);
        doubleJumpEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:double_jump").orElse(null);
        durabilityEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:durability").orElse(null);
        explosiveEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:explosive").orElse(null);
        hasteEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:haste").orElse(null);
        healthEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:health").orElse(null);
        magnetEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:magnet").orElse(null);
        multibreakEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:multibreak").orElse(null);
        poisonEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:poison").orElse(null);
        reachEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:reach").orElse(null);
        replantEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:replant").orElse(null);
        shieldBashEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:shield_bash").orElse(null);
        smeltEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:smelt").orElse(null);
        speedEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:speed").orElse(null);
        treeFellerEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:tree_feller").orElse(null);
        witherEnchantment = RegistryUtil.getEnchantment(logger, "skyenchants:wither").orElse(null);
    }

    /**
     * Get the {@link Enchantment} for the custom double drop enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getDoubleDropEnchantment() {
        return doubleDropEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom double jump enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getDoubleJumpEnchantment() {
        return doubleJumpEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom durability enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getDurabilityEnchantment() {
        return durabilityEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom explosive enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getExplosiveEnchantment() {
        return explosiveEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom haste enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getHasteEnchantment() {
        return hasteEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom health enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getHealthEnchantment() {
        return healthEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom magnet enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getMagnetEnchantment() {
        return magnetEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom multibreak enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getMultibreakEnchantment() {
        return multibreakEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom poison enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getPoisonEnchantment() {
        return poisonEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom reach enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getReachEnchantment() {
        return reachEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom replant enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getReplantEnchantment() {
        return replantEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom shield bash enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getShieldBashEnchantment() {
        return shieldBashEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom speed enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getSpeedEnchantment() {
        return speedEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom smelt enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getSmeltEnchantment() {
        return smeltEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom tree feller enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getTreeFellerEnchantment() {
        return treeFellerEnchantment;
    }

    /**
     * Get the {@link Enchantment} for the custom wither enchantment.
     * @return An {@link Enchantment} or null.
     */
    public @Nullable Enchantment getWitherEnchantment() {
        return witherEnchantment;
    }
}