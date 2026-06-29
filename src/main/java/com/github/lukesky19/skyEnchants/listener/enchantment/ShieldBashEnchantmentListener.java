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

import com.github.lukesky19.skyEnchants.config.data.enchantment.ShieldBash;
import com.github.lukesky19.skyEnchants.config.data.misc.ParticleConfig;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.ShieldBashConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.registry.RegistryUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Listens for when an attack is blocked with a shield that has the shield bash enchantment and triggers a shield bash.
 */
public class ShieldBashEnchantmentListener implements Listener {
    private final @NonNull ComponentLogger logger;
    private final @NonNull ShieldBashConfigManager shieldBashConfigManager;
    private final @NonNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}
     * @param shieldBashConfigManager A {@link ShieldBashConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public ShieldBashEnchantmentListener(
            @NonNull ComponentLogger logger,
            @NonNull ShieldBashConfigManager shieldBashConfigManager,
            @NonNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.shieldBashConfigManager = shieldBashConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens for when an attack is blocked with a shield that has the shield bash enchantment and triggers a shield bash.
     * @param entityDamageByEntityEvent An {@link EntityDamageByEntityEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAttackBlocked(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        ShieldBash shieldBash = shieldBashConfigManager.getConfiguration();
        if(shieldBash == null) {
            logger.error(AdventureUtility.plain("Unable to apply shield bash due to an invalid settings."));
            return;
        }
        // If shield bash isn't enabled, return
        if(!shieldBash.isEnabled()) return;
        // Get the shield bash Enchantment
        Enchantment shieldBashEnchantment = enchantmentManager.getShieldBashEnchantment();
        // If the shield bash enchantment is null, return
        if(shieldBashEnchantment == null) return;

        // Get the damage and knockback mapping
        Map<Integer, Double> damagePerLevel = shieldBash.damagePerLevel();
        Map<Integer, Double> knockbackPerLevel = shieldBash.knockbackPerLevel();

        // If there is no damage mapping, log an error and return
        if(damagePerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply shield bash enchantment effect due to invalid plugin settings (No damage mapping)."));
            return;
        }

        // If there is no knockback mapping, log an error and return
        if(knockbackPerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply shield bash enchantment effect due to invalid plugin settings (No knockback mapping)."));
            return;
        }

        // Get the damage source
        DamageSource damageSource = entityDamageByEntityEvent.getDamageSource();
        // The entity that caused the damage
        Entity causingEntity = damageSource.getCausingEntity();
        if(causingEntity == null) return;
        if(!(causingEntity instanceof LivingEntity causingLivingEntity)) return;
        // The entity that the damage is being applied to
        Entity targetEntity = entityDamageByEntityEvent.getEntity();
        // If the target entity is not a player, return
        if(!(targetEntity instanceof Player targetPlayer)) return;
        // If the target player isn't blocking, return
        if(!targetPlayer.isBlocking()) return;

        // Get the target player's EntityEquipment
        EntityEquipment targetPlayerEquipment = targetPlayer.getEquipment();
        // Get the EquipmentSlots that the shield bash enchantment can activate in
        List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(shieldBash.registration().equipmentSlots());
        // Get the max level for the shield bash enchantment.
        int maxLevel = shieldBash.registration().maxLevel();

        // Loop through the possible EquipmentSlots that the shield bash enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            ItemStack itemStack = targetPlayerEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the shield bash enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(shieldBashEnchantment)) return;

            // Get the enchantment level of the shield bash enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(shieldBashEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            Double damageAmount = damagePerLevel.get(enchantmentLevel);
            if(damageAmount == null) {
                logger.error(AdventureUtility.plain("Unable to apply the shield bash enchantment effect due to invalid plugin settings (No damage mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            Double knockbackAmount = knockbackPerLevel.get(enchantmentLevel);
            if(knockbackAmount == null) {
                logger.error(AdventureUtility.plain("Unable to apply the shield bash enchantment effect due to invalid plugin settings (No damage mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Apply the shield bash action
            applyShieldBash(shieldBash, targetPlayer, causingLivingEntity, damageAmount, knockbackAmount);
        }
    }

    /**
     * Applies the shield bash action.
     * @param shieldBash The {@link ShieldBash} config.
     * @param targetPlayer The player blocking the attack.
     * @param causingEntity The entity attacking the player.
     * @param damageAmount The damage amount.
     * @param knockbackAmount The knockback amount.
     */
    private void applyShieldBash(
            @NonNull ShieldBash shieldBash,
            @NonNull Player targetPlayer,
            @NonNull LivingEntity causingEntity,
            double damageAmount,
            double knockbackAmount) {
        Location targetPlayerLocation = targetPlayer.getLocation();
        Location causingEntityLocation = causingEntity.getLocation();

        // Get the Particle
        ParticleConfig particleConfig = shieldBash.particle();
        String particleName = particleConfig.particleType();
        if(particleName == null) {
            logger.error(AdventureUtility.plain("Unable to apply shield bash due to an invalid particle name."));
            return;
        }
        Optional<Particle> optionalParticle = RegistryUtil.getParticle(logger, particleName);
        if(optionalParticle.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply shield bash due to an no particle found for: " + particleName + "."));
            return;
        }
        Particle particle = optionalParticle.get();

        // Get the target player's location to spawn the particle at
        Location particleLocation = targetPlayer.getLocation();
        // Spawn a particle
        targetPlayer.getWorld().spawnParticle(particle, particleLocation, particleConfig.count(), particleConfig.offsetX(), particleConfig.offsetY(), particleConfig.offsetZ(), particleConfig.extra());

        // Apply damage to the causing entity
        causingEntity.damage(damageAmount, targetPlayer);

        // Calculate knockback direction
        double knockbackX = causingEntityLocation.getX() - targetPlayerLocation.getX();
        double knockbackZ = causingEntityLocation.getZ() - targetPlayerLocation.getZ();

        // Apply the knockback
        causingEntity.setVelocity(causingEntity.getVelocity().add(new Vector(knockbackX, 0.5, knockbackZ).normalize().multiply(knockbackAmount)));
    }
}
