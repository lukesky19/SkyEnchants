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
package com.github.lukesky19.skyEnchants.listener.enchantment;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.config.data.enchantment.Haste;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.HasteConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Listens for when a block is broken by a tool that contains the haste enchantment and
 * applies the necessary effects or actions. Also removes the unbreakable setting if necessary.
 */
public class HasteEnchantmentListener implements Listener {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull ComponentLogger logger;
    private final @NotNull HasteConfigManager hasteConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param hasteConfigManager A {@link HasteConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public HasteEnchantmentListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull HasteConfigManager hasteConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.skyEnchants = skyEnchants;
        this.logger = skyEnchants.getComponentLogger();
        this.hasteConfigManager = hasteConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens for when a block is broken with a tool that has the custom haste enchantment.
     * If the calculated changes is higher than the configured chance for that level, haste will be applied to the player based on the duration and amplifier mapping.
     * If the unbreakable setting is enabled, the tool will be unbreakable based on the duration mapping.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreakHasteEnchantment(BlockBreakEvent blockBreakEvent) {
        @Nullable Haste haste = hasteConfigManager.getConfiguration();
        if(haste == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate a haste enchantment due to invalid settings."));
            return;
        }

        // If the haste enchantment isn't enabled or is null, return
        if(!haste.isEnabled()) return;
        @Nullable Enchantment hasteEnchantment = enchantmentManager.getHasteEnchantment();
        if(hasteEnchantment == null) return;

        @NotNull Map<Integer, Long> unbreakableDurationPerLevel = haste.unbreakableDurationPerLevel();
        @NotNull Map<Integer, Integer> effectDurationPerLevel = haste.effectDurationPerLevel();
        @NotNull Map<Integer, Integer> effectAmplifierPerLevel = haste.effectAmplifierPerLevel();
        @NotNull Map<Integer, Double> chancePerLevel = haste.chancePerLevel();

        // If unbreakable with haste is enabled and there is no unbreakable duration mapping, log an error and return
        if(haste.temporaryUnbreakable()) {
            if(unbreakableDurationPerLevel.isEmpty()) {
                logger.error(AdventureUtil.deserialize("Unable to apply haste enchantment effect due to invalid settings (No unbreakable duration mapping)."));
                return;
            }
        }

        // If there is no effect duration mapping, log an error and return
        if(effectDurationPerLevel.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to apply haste enchantment effect due to invalid settings (No effect duration mapping)."));
            return;
        }

        // If there is no effect amplifier mapping, log an error and return
        if(effectAmplifierPerLevel.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to apply haste enchantment effect due to invalid settings (No effect amplifier mapping)."));
            return;
        }

        // If there is no chance mapping, log an error and return
        if(chancePerLevel.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to apply haste enchantment effect due to invalid settings (No chance mapping)."));
            return;
        }

        // Get the Player involved in the event
        Player player = blockBreakEvent.getPlayer();
        // Get the Player's EntityEquipment
        EntityEquipment entityEquipment = player.getEquipment();
        // Get the EquipmentSlots that the haste enchantment can apply to.
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(haste.getRegistrationConfig().equipmentSlots());
        // Get the max level for the haste enchantment.
        int maxLevel = haste.getRegistrationConfig().maxLevel();

        // Create a new Random
        Random random = new Random();

        // Loop through the possible EquipmentSlots that the haste enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the Haste enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(hasteEnchantment)) continue;

            // Get the enchantment level of the haste enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(hasteEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the chance that haste is applied
            @Nullable Double hasteChance = chancePerLevel.get(enchantmentLevel);
            // Log an error if there is no chance configured for the enchantment level
            if(hasteChance == null) {
                logger.error(AdventureUtil.deserialize("Unable to apply haste enchantment effects due to invalid settings (No chance to activate for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            if(hasteChance < 1.0) {
                // Calculate the random chance
                double randomChance = random.nextDouble(0, 1);

                // If the random chance is greater than or equal to the replant chance, move to the next EquipmentSlot
                if(randomChance >= hasteChance) continue;
            }

            // Get the duration for the haste effect
            @Nullable Integer duration = effectDurationPerLevel.get(enchantmentLevel);
            // Log an error if there is no duration configured for the enchantment level
            if(duration == null) {
                logger.error(AdventureUtil.deserialize("Unable to apply the haste enchantment effect due to invalid settings (No effect duration mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Get the amplifier for the haste effect
            @Nullable Integer amplifier = effectAmplifierPerLevel.get(enchantmentLevel);
            // Log an error if there is no amplifier configured for the enchantment level
            if(amplifier == null) {
                logger.error(AdventureUtil.deserialize("Unable to apply the haste enchantment effect due to invalid settings (No effect amplifier mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Create the PotionEffect
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.HASTE, duration, amplifier, false, true, true, null);
            // Add the PotionEffect to the Player
            player.addPotionEffect(potionEffect);

            // If unbreakable with haste is enabled, attempt to set the ItemStack's unbreakable option
            if(haste.temporaryUnbreakable()) {
                // Get the ItemMeta for the ItemStack
                ItemMeta itemMeta = itemStack.getItemMeta();
                // Get the PersistentDataContainer from the ItemMeta
                PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

                // Create the NamespacedKey that stores the timestamp when the unbreakable setting expires
                NamespacedKey timestampKey = NamespacedKey.fromString("timestamp", skyEnchants);
                if(timestampKey == null) continue;

                // Get the unbreakable duration
                @Nullable Long unbreakableDuration = unbreakableDurationPerLevel.get(enchantmentLevel);
                // Log an error if there is no duration for the enchantment level
                if(unbreakableDuration == null) {
                    logger.error(AdventureUtil.deserialize("Unable to apply the haste enchantment effect due to invalid settings (No unbreakable duration mapping for enchantment level: " + enchantmentLevel + ")."));
                    continue;
                }

                // Store the timestamp when the unbreakable setting expires
                pdc.set(timestampKey, PersistentDataType.LONG, (System.currentTimeMillis() + unbreakableDuration));

                // Set the unbreakable setting
                itemMeta.setUnbreakable(true);

                // Set the ItemStack's ItemMeta
                itemStack.setItemMeta(itemMeta);
            }
        }
    }

    /**
     * Listens for when a block is broken and checks if the unbreakable setting should be removed.
     * @param blockBreakEvent A {@link BlockBreakEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreakUnbreakableCheck(BlockBreakEvent blockBreakEvent) {
        @Nullable Haste haste = hasteConfigManager.getConfiguration();
        if(haste == null) {
            logger.error(AdventureUtil.deserialize("Unable to remove a unbreakable tool from the haste enchantment due to invalid settings."));
            return;
        }

        // Get the Player involved in the event
        Player player = blockBreakEvent.getPlayer();
        // Get the Player's EntityEquipment
        EntityEquipment entityEquipment = player.getEquipment();

        // Create the NamespacedKey that stores the timestamp when the unbreakable setting expires
        NamespacedKey timestampKey = NamespacedKey.fromString("timestamp", skyEnchants);
        if(timestampKey == null) return;

        // Loop through all possible EquipmentSlots and check for the timestamp stored on the ItemStack's PDC.
        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            // Get the ItemStack for the EquipmentSlot
            ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (i.e., AIR), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // Get the ItemStack's ItemMeta
            ItemMeta itemMeta = itemStack.getItemMeta();
            // If the ItemMeta is null, move to the next EquipmentSlot
            if(itemMeta == null) continue;
            // Get the PersistentDataContainer from the ItemMeta
            PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

            // If the PDC doesn't contain the timestamp key, move to the next EquipmentSlot
            if(!pdc.has(timestampKey)) continue;

            // Get the timestamp stored on the PDC
            @Nullable Long timeStamp = pdc.get(timestampKey, PersistentDataType.LONG);
            // If the timestamp is null, move to the next EquipmentSlot
            if(timeStamp == null) continue;

            // If the current system time is greater than or equal to the stored timestamp, remove the unbreakable setting
            if(System.currentTimeMillis() > timeStamp) {
                // Set the unbreakable setting to false
                itemMeta.setUnbreakable(false);

                // Remove the stored timestamp
                pdc.remove(timestampKey);

                // Set the ItemStack's ItemMeta
                itemStack.setItemMeta(itemMeta);
            }
        }
    }
}
