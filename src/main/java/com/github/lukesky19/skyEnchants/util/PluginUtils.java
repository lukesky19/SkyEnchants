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
package com.github.lukesky19.skyEnchants.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains utilities used throughout the plugin.
 */
public class PluginUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public PluginUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Get an array of {@link EquipmentSlotGroup}s from the group names provided.
     * Invalid or null values are removed.
     * @param groupNames A {@link List} of {@link String}s for the group names.
     * @return An array of {@link EquipmentSlotGroup}s.
     */
    public static @NotNull EquipmentSlotGroup @NotNull[] getEquipmentSlotGroups(@NotNull List<String> groupNames) {
        return groupNames.stream()
                .map(EquipmentSlotGroup::getByName)
                .filter(Objects::nonNull)
                .toArray(EquipmentSlotGroup[]::new);
    }

    /**
     * Get a {@link List} of {@link EquipmentSlot}s for the slot names provided.
     * This uses the same names for {@link EquipmentSlotGroup}s, so the name "mainhand" and "offhand" are converted to the appropriate enum names.
     * Invalid or null values are removed.
     * @param slotNames The {@link List} of {@link String}s for slot names.
     * @return A {@link List} of {@link EquipmentSlot}s.
     */
    public static @NotNull List<EquipmentSlot> getEquipmentSlots(@NotNull List<String> slotNames) {
        return slotNames.stream()
                .map(slotName -> {
                    try {
                        if(slotName.equalsIgnoreCase("mainhand")) slotName = "HAND";
                        if(slotName.equalsIgnoreCase("offhand")) slotName = "OFF_HAND";

                        return EquipmentSlot.valueOf(slotName);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Get a {@link List} of {@link TypedKey}s of type {@link ItemType} that an enchantment can apply to based on the list of item names provided.
     * Invalid or null values are removed.
     * @param itemNames A {@link List} of {@link NamespacedKey}s as a {@link String} for {@link TypedKey}s of type {@link ItemType}
     * @return A {@link List} of {@link TypedKey}s of type {@link ItemType}.
     */
    public static @NotNull List<@NotNull TypedKey<@NotNull ItemType>> getSupportedItemTypeTypedKeys(@NotNull List<String> itemNames) {
        return itemNames.stream()
                .map(itemKeyName -> {
                    try {
                        if(!supportsItemType(itemKeyName)) return null;

                        //noinspection PatternValidation
                        return TypedKey.create(RegistryKey.ITEM, Key.key(itemKeyName));
                    } catch (InvalidKeyException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Does the server support an ItemType οf the key name provided as a String?
     * @param itemTypeKeyName The ItemType key name.
     * @return true if supported, false if not.
     */
    private static boolean supportsItemType(@NotNull String itemTypeKeyName) {
        Registry<@NotNull ItemType> itemTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);

        // Create the NamespacedKey using the name normalized to lowercase.
        @NotNull Optional<NamespacedKey> optionalNamespacedKey = createNamespacedKey(itemTypeKeyName.toLowerCase());
        if(optionalNamespacedKey.isPresent()) {
            NamespacedKey key = optionalNamespacedKey.get();

            // Does the registry have a valid ItemType for the key?
            return itemTypeRegistry.get(key) != null;
        }

        return false;
    }

    /**
     * Creates a {@link NamespacedKey} from the given name.
     * If a namespace is not provided, the default minecraft: namespace will be used. Otherwise, the one provided will be used.
     * @param name The name of the key or the name with a namespace and key following the namespace:key format.
     * @return An {@link Optional} containing a {@link NamespacedKey} if one was created successfully. May be empty if the NamespacedKey fails to be created.
     */
    private static @NotNull Optional<NamespacedKey> createNamespacedKey(@NotNull String name) {
        if(name.contains(":")) {
            return Optional.ofNullable(NamespacedKey.fromString(name));
        } else {
            return Optional.of(NamespacedKey.minecraft(name));
        }
    }

    /**
     * Get a {@link RegistryKeySet} of type {@link Enchantment} that can be used to be exclusive with another enchantment.
     * @param enchantmentNames A {@link List} of {@link NamespacedKey}s as a {@link String} for enchantments.
     * @return A {@link RegistryKeySet} of type {@link Enchantment}.
     */
    public static @NotNull RegistryKeySet<@NotNull Enchantment> getExclusiveEnchantments(@NotNull List<String> enchantmentNames) {
        List<@NotNull TypedKey<@NotNull Enchantment>> keyList = enchantmentNames.stream()
                .map(enchantmentName -> {
                    try {
                        //noinspection PatternValidation
                        return TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(enchantmentName));
                    } catch (InvalidKeyException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return RegistrySet.keySet(RegistryKey.ENCHANTMENT, keyList);
    }
}
