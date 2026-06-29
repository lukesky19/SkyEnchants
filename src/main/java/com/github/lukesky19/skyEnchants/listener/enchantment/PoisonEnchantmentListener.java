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

import com.github.lukesky19.skyEnchants.config.data.enchantment.Poison;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.PoisonConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.github.lukesky19.skyEnchants.util.EnchantmentUtils.doesArmorContainEnchantment;

/**
 * Listens to when an entity deals damage to another entity, and if the player has a poison enchantment, attempts to apply the poison effect.
 */
public class PoisonEnchantmentListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull PoisonConfigManager poisonConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}
     * @param poisonConfigManager A {@link PoisonConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public PoisonEnchantmentListener(
            @NotNull ComponentLogger logger,
            @NotNull PoisonConfigManager poisonConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.poisonConfigManager = poisonConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens to when an entity deals damage to another entity, checks to see if any custom enchantment effects need applied, and then applies those effects.
     * @param entityDamageByEntityEvent An {@link EntityDamageByEntityEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        @Nullable Poison poison = poisonConfigManager.getConfiguration();
        if(poison == null) {
            logger.error(AdventureUtility.plain("Unable to apply the poison enchantment's effects due to invalid settings."));
            return;
        }

        // If poison isn't enabled, return
        if(!poison.isEnabled()) return;
        // Get the poison Enchantment
        @Nullable Enchantment poisonEnchantment = enchantmentManager.getPoisonEnchantment();
        // If the poison enchantment is null, return
        if(poisonEnchantment == null) return;

        // Get the damage source
        DamageSource damageSource = entityDamageByEntityEvent.getDamageSource();
        // The entity that caused the damage
        Entity causingEntity = damageSource.getCausingEntity();
        if(causingEntity == null) return;
        if(!(causingEntity instanceof LivingEntity causingLivingEntity)) return;
        // The causing entity's equipment
        @Nullable EntityEquipment causingEntityEquipment = causingLivingEntity.getEquipment();
        if(causingEntityEquipment == null) return;
        // The entity that the damage is being applied to
        Entity targetEntity = entityDamageByEntityEvent.getEntity();
        if(!(targetEntity instanceof LivingEntity targetLivingEntity)) return;

        // Get the EquipmentSlots that the poison enchantment can activate in.
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(poison.getRegistrationConfig().equipmentSlots());
        // Get the poison enchantment's max level
        int maxLevel = poison.getRegistrationConfig().maxLevel();

        // Get the effect duration and amplifier mapping
        @NotNull Map<Integer, Integer> effectDurationPerLevel = poison.effectDurationPerLevel();
        @NotNull Map<Integer, Integer> effectAmplifierPerLevel = poison.effectAmplifierPerLevel();
        // Log an error if the effect duration mapping is empty
        if(effectDurationPerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply the poison enchantment effect due to invalid plugin settings (No effect duration mapping)."));
            return;
        }

        // Log an error if the amplifier duration mapping is empty
        if(effectAmplifierPerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply the poison enchantment effect due to invalid plugin settings (No effect amplifier mapping)."));
            return;
        }

        // Thorns handling
        if(damageSource.getDamageType().equals(DamageType.THORNS)) {
            // Ignore thorns damage from activating if armor doesn't contain the enchantment.
            if(!doesArmorContainEnchantment(causingEntityEquipment, poisonEnchantment)) return;
        }

        // Loop through the equipment slots the poison enchantment can activate in
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = causingEntityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the poison enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(poisonEnchantment)) continue;

            // Get the enchantment level of the poison enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(poisonEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the duration of the effect
            @Nullable Integer duration = effectDurationPerLevel.get(enchantmentLevel);
            // Get the amplifier of the effect
            @Nullable Integer amplifier = effectAmplifierPerLevel.get(enchantmentLevel);

            // Log an error if there is no duration mapping for the enchantment level and move to the next EquipmentSlot
            if(duration == null) {
                logger.error(AdventureUtility.plain("Unable to apply the poison enchantment effect due to invalid plugin settings (No effect duration mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Log an error if there is no amplifier mapping for the enchantment level and move to the next EquipmentSlot
            if(amplifier == null) {
                logger.error(AdventureUtility.plain("Unable to apply the poison enchantment effect due to invalid plugin settings (No effect amplifier mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Apply the potion effect to the target entity
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.POISON, duration, amplifier, false, true, true, null);
            targetLivingEntity.addPotionEffect(potionEffect);

            // Only apply the effect once
            return;
        }
    }
}