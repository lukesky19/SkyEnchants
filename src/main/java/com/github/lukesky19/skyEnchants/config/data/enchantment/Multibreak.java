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
package com.github.lukesky19.skyEnchants.config.data.enchantment;

import com.github.lukesky19.skyEnchants.config.data.enchantment.interfaces.IEnchantmentConfig;
import com.github.lukesky19.skyEnchants.config.data.misc.Registration;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This record contains the configuration for the multibreak enchantment.
 * @param version The config version.
 * @param enabled Is the enchantment enabled?
 * @param registration The {@link Registration} config for the enchantment.
 * @param breakSimilarOnly Whether the enchantment should only break similar blocks to the one broken.
 * @param preventBelowPlayer Whether the blocks broken should prevent breaking the area below the player's feet.
 * @param maxBlocksPerSection The maximum number of blocks to process at once.
 * @param totalMaxBlocks The maximum total number of blocks to process.
 * @param locationProcessingDelayTicks The delay between location processing.
 * @param blockBreakDelayTicks The delay between the breaking of the blocks.
 * @param breakAreas The {@link Map} mapping enchantment levels to break areas. Break areas follow a LENGTHxHEIGHTxDEPTH format, i.e., 3x3x3
 */
@ConfigSerializable
public record Multibreak(
        int version,
        boolean enabled,
        @NotNull Registration registration,
        boolean breakSimilarOnly,
        boolean preventBelowPlayer,
        int maxBlocksPerSection,
        int totalMaxBlocks,
        long locationProcessingDelayTicks,
        long blockBreakDelayTicks,
        @NotNull Map<Integer, String> breakAreas) implements IEnchantmentConfig {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull String getName() {
        return "Multibreak";
    }

    @Override
    public @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
        return EnchantmentKeys.create(Key.key("skyenchants", "multibreak"));
    }

    @Override
    public @NotNull Registration getRegistrationConfig() {
        return registration;
    }
}