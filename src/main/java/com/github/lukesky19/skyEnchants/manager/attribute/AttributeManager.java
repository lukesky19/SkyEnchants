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
package com.github.lukesky19.skyEnchants.manager.attribute;

import com.github.lukesky19.skyEnchants.config.data.enchantment.Health;
import com.github.lukesky19.skyEnchants.config.data.enchantment.Reach;
import com.github.lukesky19.skyEnchants.config.data.enchantment.Speed;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.HealthConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.ReachConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.SpeedConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * This class manages the application of attributes to ItemStacks with the speed, health, and or reach enchantments.
 */
public class AttributeManager {
    private final @NotNull ComponentLogger logger;

    private final @NotNull HealthConfigManager healthConfigManager;
    private final @NotNull ReachConfigManager reachConfigManager;
    private final @NotNull SpeedConfigManager speedConfigManager;

    private final @NotNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}
     * @param healthConfigManager A {@link HealthConfigManager} instance.
     * @param reachConfigManager A {@link ReachConfigManager} instance.
     * @param speedConfigManager A {@link SpeedConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public AttributeManager(
            @NotNull ComponentLogger logger,
            @NotNull HealthConfigManager healthConfigManager,
            @NotNull ReachConfigManager reachConfigManager,
            @NotNull SpeedConfigManager speedConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.healthConfigManager = healthConfigManager;
        this.reachConfigManager = reachConfigManager;
        this.speedConfigManager = speedConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Loop through the {@link EntityEquipment} provided and update any attributes for the speed, health, and reach enchantments that are enabled.
     * @param entityEquipment The {@link EntityEquipment} to loop through.
     */
    public void applyAttributes(@NotNull EntityEquipment entityEquipment) {
        @Nullable Health health = healthConfigManager.getConfiguration();
        @Nullable Reach reach = reachConfigManager.getConfiguration();
        @Nullable Speed speed = speedConfigManager.getConfiguration();

        @Nullable Enchantment speedEnchantment = enchantmentManager.getSpeedEnchantment();
        @Nullable Enchantment reachEnchantment = enchantmentManager.getReachEnchantment();
        @Nullable Enchantment healthEnchantment = enchantmentManager.getHealthEnchantment();

        // If the health enchantment is enabled, apply the health attribute
        if(health != null && health.isEnabled() && healthEnchantment != null) {
            // Get the EquipmentSlots that the health enchantment can apply to.
            @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(health.getRegistrationConfig().equipmentSlots());
            // Get the max level for the health enchantment
            int maxLevel = health.getRegistrationConfig().maxLevel();
            // Get the health mapping per level
            @NotNull Map<Integer, Integer> healthPerLevel = health.healthPerLevel();

            // Check if the health mapping is not empty
            if(!healthPerLevel.isEmpty()) {
                // Loop through the possible EquipmentSlots that the health enchantment can activate in.
                for(EquipmentSlot equipmentSlot : equipmentSlots) {
                    // Get the ItemStack in the EquipmentSlot
                    @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
                    // If the ItemStack is empty (air), move to the next EquipmentSlot
                    if(itemStack.isEmpty()) continue;
                    // If the ItemStack doesn't contain the health enchantment, move to the next EquipmentSlot
                    if(!itemStack.getEnchantments().containsKey(healthEnchantment)) continue;

                    // Get the enchantment level of the health enchantment
                    int enchantmentLevel = itemStack.getEnchantmentLevel(healthEnchantment);
                    // If the enchantment level is over the max level, move to the next EquipmentSlot
                    if(enchantmentLevel > maxLevel) continue;

                    // Get the health amount to use for the attribute
                    @Nullable Integer healthAmount = healthPerLevel.get(enchantmentLevel);
                    // Log an error if there is no health amount configured for the enchantment level and move to the next EquipmentSlot
                    if(healthAmount == null) {
                        logger.error(AdventureUtility.plain("Unable to apply health enchantment's attribute due to invalid plugin settings (No health mapping for enchantment level: " + enchantmentLevel + ")."));
                        continue;
                    }

                    // Apply the health attribute to the item
                    applyHealthAttribute(itemStack, equipmentSlot, healthAmount);
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to apply the health enchantment attribute due to invalid plugin settings (No health mapping)."));
            }
        }

        // If the reach enchantment is enabled, apply the reach attribute
        if(reach != null && reach.isEnabled() && reachEnchantment != null) {
            // Get the EquipmentSlots that the reach enchantment can apply to.
            @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(reach.getRegistrationConfig().equipmentSlots());
            // Get the max level for the reach enchantment
            int maxLevel = reach.getRegistrationConfig().maxLevel();
            // Get the reach distance mapping per level
            @NotNull Map<Integer, Double> reachDistancePerLevel = reach.reachDistancePerLevel();

            // Check if the reach distance mapping is not empty
            if(!reachDistancePerLevel.isEmpty()) {
                // Loop through the possible EquipmentSlots that the reach enchantment can activate in.
                for(EquipmentSlot equipmentSlot : equipmentSlots) {
                    // Get the ItemStack in the EquipmentSlot
                    @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
                    // If the ItemStack is empty (air), move to the next EquipmentSlot
                    if(itemStack.isEmpty()) continue;
                    // If the ItemStack doesn't contain the reach enchantment, move to the next EquipmentSlot
                    if(!itemStack.getEnchantments().containsKey(reachEnchantment)) continue;

                    // Get the enchantment level of the reach enchantment
                    int enchantmentLevel = itemStack.getEnchantmentLevel(reachEnchantment);
                    // If the enchantment level is over the max level, move to the next EquipmentSlot
                    if(enchantmentLevel > maxLevel) continue;

                    // Get the reach distance amount to use for the attribute
                    @Nullable Double reachDistanceAmount = reachDistancePerLevel.get(enchantmentLevel);
                    // Log an error if there is no reach distance amount configured for the enchantment level and move to the next EquipmentSlot
                    if(reachDistanceAmount == null) {
                        logger.error(AdventureUtility.plain("Unable to apply reach enchantment's attribute due to invalid plugin settings (No reach distance mapping for enchantment level: " + enchantmentLevel + ")."));
                        continue;
                    }

                    // Apply the reach attribute to the item
                    applyReachAttribute(itemStack, equipmentSlot, reachDistanceAmount);
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to apply the reach enchantment attribute due to invalid plugin settings (No reach distance mapping)."));
            }
        }

        // If the speed enchantment is enabled, apply the speed attribute
        if(speed != null && speed.isEnabled() && speedEnchantment != null) {
            // Get the EquipmentSlots that the speed enchantment can apply to.
            @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(speed.getRegistrationConfig().equipmentSlots());
            // Get the max level for the speed enchantment
            int maxLevel = speed.getRegistrationConfig().maxLevel();
            // Get the speed mapping per level
            @NotNull Map<Integer, Double> speedPerLevel = speed.speedPerLevel();

            // Check if the speed mapping is not empty
            if(!speedPerLevel.isEmpty()) {
                // Loop through the possible EquipmentSlots that the speed enchantment can activate in.
                for(EquipmentSlot equipmentSlot : equipmentSlots) {
                    // Get the ItemStack in the EquipmentSlot
                    @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
                    // If the ItemStack is empty (air), move to the next EquipmentSlot
                    if(itemStack.isEmpty()) continue;
                    // If the ItemStack doesn't contain the speed enchantment, move to the next EquipmentSlot
                    if(!itemStack.getEnchantments().containsKey(speedEnchantment)) continue;

                    // Get the enchantment level of the speed enchantment
                    int enchantmentLevel = itemStack.getEnchantmentLevel(speedEnchantment);
                    // If the enchantment level is over the max level, move to the next EquipmentSlot
                    if(enchantmentLevel > maxLevel) continue;

                    // Get the speed amount to use for the attribute
                    @Nullable Double speedAmount = speedPerLevel.get(enchantmentLevel);
                    // Log an error if there is no speed amount configured for the enchantment level and move to the next EquipmentSlot
                    if(speedAmount == null) {
                        logger.error(AdventureUtility.plain("Unable to apply speed enchantment's attribute due to invalid plugin settings (No speed mapping for enchantment level: " + enchantmentLevel + ")."));
                        continue;
                    }

                    // Apply the speed attribute to the item
                    applySpeedAttribute(itemStack, equipmentSlot, speedAmount);
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to apply the speed enchantment attribute due to invalid plugin settings (No speed mapping)."));
            }
        }
    }

    /**
     * For the ItemStack provided, update any attributes for the speed, health, and reach enchantments that are enabled.
     * @param itemStack The {@link ItemStack} to update attributes for.
     */
    public void applyAttributes(@NotNull ItemStack itemStack) {
        @Nullable Health health = healthConfigManager.getConfiguration();
        @Nullable Reach reach = reachConfigManager.getConfiguration();
        @Nullable Speed speed = speedConfigManager.getConfiguration();

        @Nullable Enchantment speedEnchantment = enchantmentManager.getSpeedEnchantment();
        @Nullable Enchantment reachEnchantment = enchantmentManager.getReachEnchantment();
        @Nullable Enchantment healthEnchantment = enchantmentManager.getHealthEnchantment();

        // If the health enchantment is enabled, apply the health attribute
        if(health != null && health.isEnabled() && healthEnchantment != null) {
            // Get the EquipmentSlots that the health enchantment can apply to.
            @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(health.getRegistrationConfig().equipmentSlots());
            // Get the max level for the health enchantment
            int maxLevel = health.getRegistrationConfig().maxLevel();
            // Get the health mapping per level
            @NotNull Map<Integer, Integer> healthPerLevel = health.healthPerLevel();

            // Check if the health mapping is not empty
            if(!healthPerLevel.isEmpty()) {
                // Loop through the possible EquipmentSlots that the health enchantment can activate in.
                for(EquipmentSlot equipmentSlot : equipmentSlots) {
                    // If the ItemStack is empty (air), move to the next EquipmentSlot
                    if(itemStack.isEmpty()) continue;
                    // If the ItemStack doesn't contain the health enchantment, move to the next EquipmentSlot
                    if(!itemStack.getEnchantments().containsKey(healthEnchantment)) continue;

                    // Get the enchantment level of the health enchantment
                    int enchantmentLevel = itemStack.getEnchantmentLevel(healthEnchantment);
                    // If the enchantment level is over the max level, move to the next EquipmentSlot
                    if(enchantmentLevel > maxLevel) continue;

                    // Get the health amount to use for the attribute
                    @Nullable Integer healthAmount = healthPerLevel.get(enchantmentLevel);
                    // Log an error if there is no health amount configured for the enchantment level and move to the next EquipmentSlot
                    if(healthAmount == null) {
                        logger.error(AdventureUtility.plain("Unable to apply health enchantment's attribute due to invalid plugin settings (No health mapping for enchantment level: " + enchantmentLevel + ")."));
                        continue;
                    }

                    // Apply the health attribute to the item
                    applyHealthAttribute(itemStack, equipmentSlot, healthAmount);
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to apply the health enchantment attribute due to invalid plugin settings (No health mapping)."));
            }
        }

        // If the reach enchantment is enabled, apply the reach attribute
        if(reach != null && reach.isEnabled() && reachEnchantment != null) {
            // Get the EquipmentSlots that the reach enchantment can apply to.
            @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(reach.getRegistrationConfig().equipmentSlots());
            // Get the max level for the reach enchantment
            int maxLevel = reach.getRegistrationConfig().maxLevel();
            // Get the reach distance mapping per level
            @NotNull Map<Integer, Double> reachDistancePerLevel = reach.reachDistancePerLevel();

            // Check if the reach distance mapping is not empty
            if(!reachDistancePerLevel.isEmpty()) {
                // Loop through the possible EquipmentSlots that the reach enchantment can activate in.
                for(EquipmentSlot equipmentSlot : equipmentSlots) {
                    // If the ItemStack is empty (air), move to the next EquipmentSlot
                    if(itemStack.isEmpty()) continue;
                    // If the ItemStack doesn't contain the reach enchantment, move to the next EquipmentSlot
                    if(!itemStack.getEnchantments().containsKey(reachEnchantment)) continue;

                    // Get the enchantment level of the reach enchantment
                    int enchantmentLevel = itemStack.getEnchantmentLevel(reachEnchantment);
                    // If the enchantment level is over the max level, move to the next EquipmentSlot
                    if(enchantmentLevel > maxLevel) continue;

                    // Get the reach distance amount to use for the attribute
                    @Nullable Double reachDistanceAmount = reachDistancePerLevel.get(enchantmentLevel);
                    // Log an error if there is no reach distance amount configured for the enchantment level and move to the next EquipmentSlot
                    if(reachDistanceAmount == null) {
                        logger.error(AdventureUtility.plain("Unable to apply reach enchantment's attribute due to invalid plugin settings (No reach distance mapping for enchantment level: " + enchantmentLevel + ")."));
                        continue;
                    }

                    // Apply the reach attribute to the item
                    applyReachAttribute(itemStack, equipmentSlot, reachDistanceAmount);
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to apply the reach enchantment attribute due to invalid plugin settings (No reach distance mapping)."));
            }
        }

        // If the speed enchantment is enabled, apply the speed attribute
        if(speed != null && speed.isEnabled() && speedEnchantment != null) {
            // Get the EquipmentSlots that the speed enchantment can apply to.
            @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(speed.getRegistrationConfig().equipmentSlots());
            // Get the max level for the speed enchantment
            int maxLevel = speed.getRegistrationConfig().maxLevel();
            // Get the speed mapping per level
            @NotNull Map<Integer, Double> speedPerLevel = speed.speedPerLevel();

            // Check if the speed mapping is not empty
            if(!speedPerLevel.isEmpty()) {
                // Loop through the possible EquipmentSlots that the speed enchantment can activate in.
                for(EquipmentSlot equipmentSlot : equipmentSlots) {
                    // If the ItemStack is empty (air), move to the next EquipmentSlot
                    if(itemStack.isEmpty()) continue;
                    // If the ItemStack doesn't contain the speed enchantment, move to the next EquipmentSlot
                    if(!itemStack.getEnchantments().containsKey(speedEnchantment)) continue;

                    // Get the enchantment level of the speed enchantment
                    int enchantmentLevel = itemStack.getEnchantmentLevel(speedEnchantment);
                    // If the enchantment level is over the max level, move to the next EquipmentSlot
                    if(enchantmentLevel > maxLevel) continue;

                    // Get the speed amount to use for the attribute
                    @Nullable Double speedAmount = speedPerLevel.get(enchantmentLevel);
                    // Log an error if there is no speed amount configured for the enchantment level and move to the next EquipmentSlot
                    if(speedAmount == null) {
                        logger.error(AdventureUtility.plain("Unable to apply speed enchantment's attribute due to invalid plugin settings (No speed mapping for enchantment level: " + enchantmentLevel + ")."));
                        continue;
                    }

                    // Apply the speed attribute to the item
                    applySpeedAttribute(itemStack, equipmentSlot, speedAmount);
                }
            } else {
                logger.error(AdventureUtility.plain("Unable to apply the speed enchantment attribute due to invalid plugin settings (No speed mapping)."));
            }
        }
    }

    /**
     * Applies the speed attribute using the amount provided.
     * @param itemStack The {@link ItemStack} to apply the speed attribute to.
     * @param equipmentSlot The {@link EquipmentSlot} the item is in.
     * @param amount The amount of speed to add for the attribute.
     */
    private void applySpeedAttribute(@NotNull ItemStack itemStack, @NotNull EquipmentSlot equipmentSlot, double amount) {
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the default modifiers
        itemMeta.setAttributeModifiers(itemType.getDefaultAttributeModifiers());

        // Remove any existing modifiers
        itemMeta.removeAttributeModifier(Attribute.MOVEMENT_SPEED);

        // Create the new modifier
        AttributeModifier speedModifier = new AttributeModifier(Attribute.MOVEMENT_SPEED.getKey(), amount, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot.getGroup());
        // Add the new modifier
        itemMeta.addAttributeModifier(Attribute.MOVEMENT_SPEED, speedModifier);

        // Update item's item meta
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Applies the reach attribute using the amount provided.
     * @param itemStack The {@link ItemStack} to apply the reach attribute to.
     * @param equipmentSlot The {@link EquipmentSlot} the item is in.
     * @param amount The amount of reach to add for the attribute.
     */
    private void applyReachAttribute(@NotNull ItemStack itemStack, @NotNull EquipmentSlot equipmentSlot, double amount) {
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the default modifiers
        itemMeta.setAttributeModifiers(itemType.getDefaultAttributeModifiers());

        // Remove any existing modifiers
        itemMeta.removeAttributeModifier(Attribute.BLOCK_INTERACTION_RANGE);
        itemMeta.removeAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE);

        // Create the new modifiers
        AttributeModifier blockInteractionModifier = new AttributeModifier(Attribute.BLOCK_INTERACTION_RANGE.getKey(), amount, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot.getGroup());
        AttributeModifier entityInteractionModifier = new AttributeModifier(Attribute.ENTITY_INTERACTION_RANGE.getKey(), amount, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot.getGroup());
        // Add the new modifiers
        itemMeta.addAttributeModifier(Attribute.BLOCK_INTERACTION_RANGE, blockInteractionModifier);
        itemMeta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, entityInteractionModifier);

        // Update item's item meta
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Applies the health attribute using the amount provided.
     * @param itemStack The {@link ItemStack} to apply the health attribute to.
     * @param equipmentSlot The {@link EquipmentSlot} the item is in.
     * @param amount The amount of health to add for the attribute.
     */
    private void applyHealthAttribute(@NotNull ItemStack itemStack, @NotNull EquipmentSlot equipmentSlot, double amount) {
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the default modifiers
        itemMeta.setAttributeModifiers(itemType.getDefaultAttributeModifiers());

        // Remove any existing modifiers
        itemMeta.removeAttributeModifier(Attribute.MAX_HEALTH);

        // Create the modifier
        AttributeModifier healthModifier = new AttributeModifier(Attribute.MAX_HEALTH.getKey(), amount, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot.getGroup());
        // Add the new modifier
        itemMeta.addAttributeModifier(Attribute.MAX_HEALTH, healthModifier);

        // Update item's item meta
        itemStack.setItemMeta(itemMeta);
    }
}
