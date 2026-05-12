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
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * This record contains the configuration for the tree feller enchantment.
 * @param version The config version.
 * @param enabled Is the enchantment enabled?
 * @param registration The {@link Registration} config for the enchantment.
 * @param minLeafCount The minimum number of leaves required to be considered a tree.
 * @param includeLeaves Should leaves be removed when felling a tree?
 * @param includeMangroveRoots Should mangrove roots be removed when felling a tree?
 * @param includeFoliage Should other tree foliage be removed when felling a tree?
 * @param preventToolBreaking Should the tool be prevented from breaking when activated?
 */
@ConfigSerializable
public record TreeFeller(
        int version,
        boolean enabled,
        @NotNull Registration registration,
        int minLeafCount,
        boolean includeLeaves,
        boolean includeMangroveRoots,
        boolean includeFoliage,
        boolean preventToolBreaking) implements IEnchantmentConfig {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull String getName() {
        return "Tree Feller";
    }

    @Override
    public @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
        return EnchantmentKeys.create(Key.key("skyenchants", "tree_feller"));
    }

    @Override
    public @NotNull Registration getRegistrationConfig() {
        return registration;
    }
}