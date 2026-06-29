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

import com.github.lukesky19.skyEnchants.config.data.enchantment.Wither;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.WitherConfigManager;
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
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

import static com.github.lukesky19.skyEnchants.util.EnchantmentUtils.doesArmorContainEnchantment;

/**
 * Listens to when an entity deals damage to another entity, and if the player has a wither enchantment, attempts to apply the wither effect.
 */
public class WitherEnchantmentListener implements Listener {
    private final @NonNull ComponentLogger logger;
    private final @NonNull WitherConfigManager witherConfigManager;
    private final @NonNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}
     * @param witherConfigManager A {@link WitherConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public WitherEnchantmentListener(
            @NonNull ComponentLogger logger,
            @NonNull WitherConfigManager witherConfigManager,
            @NonNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.witherConfigManager = witherConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens to when an entity deals damage to another entity, checks to see if any custom enchantment effects need applied, and then applies those effects.
     * @param entityDamageByEntityEvent An {@link EntityDamageByEntityEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Wither wither = witherConfigManager.getConfiguration();
        if(wither == null) {
            logger.error(AdventureUtility.plain("Unable to apply the wither enchantment's effects due to invalid settings."));
            return;
        }

        // If wither isn't enabled, return
        if(!wither.isEnabled()) return;
        // Get the wither Enchantment
        Enchantment witherEnchantment = enchantmentManager.getWitherEnchantment();
        // If the wither enchantment is null, return
        if(witherEnchantment == null) return;

        // Get the damage source
        DamageSource damageSource = entityDamageByEntityEvent.getDamageSource();
        // The entity that caused the damage
        Entity causingEntity = damageSource.getCausingEntity();
        if(causingEntity == null) return;
        if(!(causingEntity instanceof LivingEntity causingLivingEntity)) return;
        // The causing entity's equipment
        EntityEquipment causingEntityEquipment = causingLivingEntity.getEquipment();
        if(causingEntityEquipment == null) return;
        // The entity that the damage is being applied to
        Entity targetEntity = entityDamageByEntityEvent.getEntity();
        if(!(targetEntity instanceof LivingEntity targetLivingEntity)) return;

        // Get the EquipmentSlots that the explosive enchantment can activate in.
        List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(wither.getRegistrationConfig().equipmentSlots());
        // Get the explosive enchantment's max level
        int maxLevel = wither.getRegistrationConfig().maxLevel();

        // Get the effect duration and amplifier mapping
        Map<Integer, Integer> effectDurationPerLevel = wither.effectDurationPerLevel();
        Map<Integer, Integer> effectAmplifierPerLevel = wither.effectAmplifierPerLevel();
        // Log an error if the effect duration mapping is empty
        if(effectDurationPerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply the wither enchantment effect due to invalid plugin settings (No effect duration mapping)."));
            return;
        }

        // Log an error if the amplifier duration mapping is empty
        if(effectAmplifierPerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply the wither enchantment effect due to invalid plugin settings (No effect amplifier mapping)."));
            return;
        }

        // Thorns handling
        if(damageSource.getDamageType().equals(DamageType.THORNS)) {
            // Ignore thorns damage from activating if armor doesn't contain the enchantment.
            if(doesArmorContainEnchantment(causingEntityEquipment, witherEnchantment)) return;
        }

        // Loop through the equipment slots the wither enchantment can activate in
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            ItemStack itemStack = causingEntityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the wither enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(witherEnchantment)) continue;

            // Get the enchantment level of the wither enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(witherEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the duration of the effect
            Integer duration = effectDurationPerLevel.get(enchantmentLevel);
            // Get the amplifier of the effect
            Integer amplifier = effectAmplifierPerLevel.get(enchantmentLevel);

            // Log an error if there is no duration mapping for the enchantment level and move to the next EquipmentSlot
            if(duration == null) {
                logger.error(AdventureUtility.plain("Unable to apply wither enchantment effect due to invalid plugin settings (No effect duration mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Log an error if there is no amplifier mapping for the enchantment level and move to the next EquipmentSlot
            if(amplifier == null) {
                logger.error(AdventureUtility.plain("Unable to apply wither enchantment effect due to invalid plugin settings (No effect amplifier mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Apply the potion effect to the target entity
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.WITHER, duration, amplifier, false, true, true, null);
            targetLivingEntity.addPotionEffect(potionEffect);

            // Only apply the effect once
            return;
        }
    }
}