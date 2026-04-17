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

import com.github.lukesky19.skyEnchants.config.data.enchantment.Explosive;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.ExplosiveConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.damage.DamageSource;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Listens to when an entity deals damage to another entity, and if the player has an explosive enchantment, attempts to apply the explosion effect.
 */
public class ExplosiveEnchantmentListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull ExplosiveConfigManager explosiveConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;
    private final @NotNull Set<UUID> entitiesToIgnore = new HashSet<>();

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}
     * @param explosiveConfigManager An {@link ExplosiveConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public ExplosiveEnchantmentListener(
            @NotNull ComponentLogger logger,
            @NotNull ExplosiveConfigManager explosiveConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.explosiveConfigManager = explosiveConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens to when an entity deals damage to another entity, checks to see if any custom enchantment effects need applied, and then applies those effects.
     * @param entityDamageByEntityEvent An {@link EntityDamageByEntityEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamaged(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        // Get the damage source
        DamageSource damageSource = entityDamageByEntityEvent.getDamageSource();
        // The entity that caused the damage
        Entity causingEntity = damageSource.getCausingEntity();
        if(causingEntity == null) return;
        // Get the entity's unique id
        UUID entityId = causingEntity.getUniqueId();

        // If the event is cancelled, remove the entity id from the entities to ignore (if any) and return
        if(entityDamageByEntityEvent.isCancelled()) {
            entitiesToIgnore.remove(entityId);
            return;
        }

        // If the entity id should be ignored, remove the entity id from the entities to ignore and return
        // This is to prevent an infinite loop of the explosive enchantment activating.
        if(entitiesToIgnore.contains(entityId)) {
            entitiesToIgnore.remove(entityId);
            return;
        }

        // Get the explosive configuration, and if null, log an error and return
        @Nullable Explosive explosive = explosiveConfigManager.getConfiguration();
        if(explosive == null) {
            logger.warn(AdventureUtility.plain("Unable to apply the explosive enchantment due to invalid settings."));
            return;
        }

        // If explosive isn't enabled, return
        if(!explosive.isEnabled()) return;
        // Get the explosive Enchantment
        @Nullable Enchantment explosiveEnchantment = enchantmentManager.getExplosiveEnchantment();
        // If the explosive enchantment is null, return
        if(explosiveEnchantment == null) return;

        if(!(causingEntity instanceof LivingEntity causingLivingEntity)) return;
        // The causing entity's equipment
        @Nullable EntityEquipment causingEntityEquipment = causingLivingEntity.getEquipment();
        if(causingEntityEquipment == null) return;
        // The entity that the damage is being applied to
        Entity targetEntity = entityDamageByEntityEvent.getEntity();
        if(!(targetEntity instanceof LivingEntity targetLivingEntity)) return;

        // Get the EquipmentSlots that the explosive enchantment can activate in.
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(explosive.getRegistrationConfig().equipmentSlots());
        // Get the explosive enchantment's max level
        int maxLevel = explosive.getRegistrationConfig().maxLevel();

        // Get the power mapping
        @NotNull Map<Integer, Integer> explosivePowerPerLevel = explosive.explosionPowerPerLevel();
        // Log an error if the power mapping is empty
        if(explosivePowerPerLevel.isEmpty()) {
            logger.warn(AdventureUtility.plain("Unable to apply the explosive enchantment effect due to invalid plugin settings (No power mapping)."));
            return;
        }

        // Loop through the equipment slots the explosive enchantment can activate in
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = causingEntityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the explosive enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(explosiveEnchantment)) continue;

            // Get the enchantment level of the explosive enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(explosiveEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the power for the explosion
            @Nullable Integer power = explosivePowerPerLevel.get(enchantmentLevel);
            // If there is no power mapping for the enchantment level, log an error and move to the next EquipmentSlot
            if(power == null) {
                logger.warn(AdventureUtility.plain("Unable to apply the explosive enchantment effect due to invalid plugin settings (No power mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Add the entity id to the ignored entity ids to prevent infinite activation of the explosive enchantment
            entitiesToIgnore.add(entityId);

            // Spawn the explosion
            targetLivingEntity.getLocation().createExplosion(causingEntity, power, false, false);

            return;
        }
    }
}