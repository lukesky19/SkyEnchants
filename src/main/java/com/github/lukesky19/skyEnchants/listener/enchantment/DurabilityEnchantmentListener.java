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

import com.github.lukesky19.skyEnchants.config.data.enchantment.Durability;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DurabilityConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.processor.MultibreakProcessor;
import com.github.lukesky19.skyEnchants.processor.TreeProcessor;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import io.papermc.paper.event.entity.EntityDamageItemEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Listens for when an item is damaged that contains the durability enchantment and prevents the tool from breaking.
 * Also cancels many events to prevent infinite tool usage.
 * @apiNote The durability enchantment is handled in {@link TreeProcessor} and {@link MultibreakProcessor}
 * for the tree feller and multibreak enchantment.
 */
public class DurabilityEnchantmentListener implements Listener {
    private final @NonNull DurabilityConfigManager durabilityConfigManager;
    private final @NonNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param durabilityConfigManager A {@link DurabilityConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public DurabilityEnchantmentListener(
            @NonNull DurabilityConfigManager durabilityConfigManager,
            @NonNull EnchantmentManager enchantmentManager) {
        this.durabilityConfigManager = durabilityConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * If the ItemStack used to break the block has the durability enchantment and is about to break, prevent the block from breaking.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Check if the event should be cancelled
        if(shouldCancel(mainHandItem, EquipmentSlot.HAND)) {
            // Cancel the event
            blockBreakEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(offHandItem, EquipmentSlot.OFF_HAND)) {
            // Cancel the event
            blockBreakEvent.setCancelled(true);
        }
    }

    /**
     * If the ItemStack used to interact has the durability enchantment and is about to break, prevent the interaction.
     * @param entityInteractEvent An {@link EntityInteractEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockInteract(EntityInteractEvent entityInteractEvent) {
        Entity entity = entityInteractEvent.getEntity();
        if(!(entity instanceof LivingEntity livingEntity)) return;
        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if(entityEquipment == null) return;
        ItemStack mainHandItem = entityEquipment.getItem(EquipmentSlot.HAND);
        ItemStack offHandItem = entityEquipment.getItem(EquipmentSlot.OFF_HAND);

        // Check if the event should be cancelled
        if(shouldCancel(mainHandItem, EquipmentSlot.HAND)) {
            // Cancel the event
            entityInteractEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(offHandItem, EquipmentSlot.OFF_HAND)) {
            // Cancel the event
            entityInteractEvent.setCancelled(true);
        }
    }

    /**
     * If the ItemStack used to change the block has the durability enchantment and is about to break, prevent the change.
     * @param entityChangeBlockEvent An {@link EntityChangeBlockEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent entityChangeBlockEvent) {
        Entity entity = entityChangeBlockEvent.getEntity();
        if(!(entity instanceof LivingEntity livingEntity)) return;
        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if(entityEquipment == null) return;
        ItemStack mainHandItem = entityEquipment.getItem(EquipmentSlot.HAND);
        ItemStack offHandItem = entityEquipment.getItem(EquipmentSlot.OFF_HAND);

        // Check if the event should be cancelled
        if(shouldCancel(mainHandItem, EquipmentSlot.HAND)) {
            // Cancel the event
            entityChangeBlockEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(offHandItem, EquipmentSlot.OFF_HAND)) {
            // Cancel the event
            entityChangeBlockEvent.setCancelled(true);
        }
    }

    /**
     * If the ItemStack used to cause the block to drop items has the durability enchantment and is about to break, prevent the interaction.
     * @param blockDropItemEvent An {@link BlockDropItemEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent blockDropItemEvent) {
        Player player = blockDropItemEvent.getPlayer();
        BlockState blockStateSnapshot = blockDropItemEvent.getBlockState();
        Block block = blockStateSnapshot.getWorld().getBlockAt(blockStateSnapshot.getLocation());
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Check if the event should be cancelled
        if(shouldCancel(mainHandItem, EquipmentSlot.HAND)) {
            // Cancel the event
            blockDropItemEvent.setCancelled(true);

            // Revert the block
            block.setType(blockStateSnapshot.getType());
            block.setBlockData(blockStateSnapshot.getBlockData());

            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(offHandItem, EquipmentSlot.OFF_HAND)) {
            // Cancel the event
            blockDropItemEvent.setCancelled(true);

            // Revert the block
            block.setType(blockStateSnapshot.getType());
            block.setBlockData(blockStateSnapshot.getBlockData());
        }
    }

    /**
     * If the ItemStack used to cause the damage has the durability enchantment and is about to break, prevent the damage.
     * @param entityDamageByEntityEvent An {@link EntityDamageByEntityEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Entity entity = entityDamageByEntityEvent.getDamageSource().getCausingEntity();
        if(!(entity instanceof LivingEntity livingEntity)) return;
        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if(entityEquipment == null) return;
        ItemStack mainHandItem = entityEquipment.getItem(EquipmentSlot.HAND);
        ItemStack offHandItem = entityEquipment.getItem(EquipmentSlot.OFF_HAND);

        // Check if the event should be cancelled
        if(shouldCancel(mainHandItem, EquipmentSlot.HAND)) {
            // Cancel the event
            entityDamageByEntityEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(offHandItem, EquipmentSlot.OFF_HAND)) {
            // Cancel the event
            entityDamageByEntityEvent.setCancelled(true);
        }
    }

    /**
     * If the entity is damaged by a block and an item (armor) the durability enchantment and is about to break, prevent the damage.
     * @param entityDamageByBlockEvent An {@link EntityDamageByBlockEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent entityDamageByBlockEvent) {
        Entity entity = entityDamageByBlockEvent.getDamageSource().getCausingEntity();
        if(!(entity instanceof LivingEntity livingEntity)) return;
        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if(entityEquipment == null) return;

        ItemStack helmetItem = entityEquipment.getItem(EquipmentSlot.HEAD);
        ItemStack chestplateItem = entityEquipment.getItem(EquipmentSlot.CHEST);
        ItemStack leggingsItem = entityEquipment.getItem(EquipmentSlot.LEGS);
        ItemStack bootsItem = entityEquipment.getItem(EquipmentSlot.FEET);
        ItemStack bodyItem = entityEquipment.getItem(EquipmentSlot.BODY);

        // Check if the event should be cancelled
        if(shouldCancel(helmetItem, EquipmentSlot.HEAD)) {
            // Cancel the event
            entityDamageByBlockEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(chestplateItem, EquipmentSlot.CHEST)) {
            // Cancel the event
            entityDamageByBlockEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(leggingsItem, EquipmentSlot.LEGS)) {
            // Cancel the event
            entityDamageByBlockEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(bootsItem, EquipmentSlot.FEET)) {
            // Cancel the event
            entityDamageByBlockEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(bodyItem, EquipmentSlot.BODY)) {
            // Cancel the event
            entityDamageByBlockEvent.setCancelled(true);
        }
    }

    /**
     * If the ItemStack used to launch the projectile has the durability enchantment and is about to break, prevent the projectile launch.
     * @apiNote The projectile will still be consumed even if the event is cancelled.
     * There is an open <a href="https://github.com/PaperMC/Paper/issues/12123">Issue</a> and <a href="https://github.com/PaperMC/Paper/pull/12124">PR</a> with paper for this.
     * @param projectileLaunchEvent An {@link ProjectileLaunchEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileFire(ProjectileLaunchEvent projectileLaunchEvent) {
        Projectile projectile = projectileLaunchEvent.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();
        if(!(projectileSource instanceof Entity entity)) return;
        if(!(entity instanceof LivingEntity livingEntity)) return;
        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if(entityEquipment == null) return;

        ItemStack mainHandItem = entityEquipment.getItem(EquipmentSlot.HAND);
        ItemStack offHandItem = entityEquipment.getItem(EquipmentSlot.OFF_HAND);

        // Check if the event should be cancelled
        if(shouldCancel(mainHandItem, EquipmentSlot.HAND)) {
            // Cancel the event
            projectileLaunchEvent.setCancelled(true);
            return;
        }

        // Check if the event should be cancelled
        if(shouldCancel(offHandItem, EquipmentSlot.OFF_HAND)) {
            // Cancel the event
            projectileLaunchEvent.setCancelled(true);
        }
    }

    /**
     * When an item with the durability enchantment is damaged, check the durability and protect the tool from breaking.
     * @param entityDamageItemEvent A {@link EntityDamageItemEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageItemEvent entityDamageItemEvent) {
        Entity entity = entityDamageItemEvent.getEntity();
        if(!(entity instanceof LivingEntity livingEntity)) return;
        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if(entityEquipment == null) return;
        ItemStack itemStack = entityDamageItemEvent.getItem();

        // Get the EquipmentSlot
        EquipmentSlot itemStackEquipmentSlot = null;
        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if(entityEquipment.getItem(equipmentSlot).equals(itemStack)) {
                itemStackEquipmentSlot = equipmentSlot;
            }
        }
        if(itemStackEquipmentSlot == null) return;

        // Check if the event should be cancelled
        if(shouldCancel(itemStack, itemStackEquipmentSlot)) {
            // Cancel the event to protect the item from breaking
            entityDamageItemEvent.setCancelled(true);
        }
    }

    /**
     * When an item with the durability enchantment is damaged, check the durability and protect the tool from breaking.
     * @param playerItemDamageEvent A {@link PlayerItemDamageEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent playerItemDamageEvent) {
        Player player = playerItemDamageEvent.getPlayer();
        EntityEquipment entityEquipment = player.getEquipment();
        ItemStack itemStack = playerItemDamageEvent.getItem();

        // Get the EquipmentSlot
        EquipmentSlot itemStackEquipmentSlot = null;
        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if(entityEquipment.getItem(equipmentSlot).equals(itemStack)) {
                itemStackEquipmentSlot = equipmentSlot;
            }
        }
        if(itemStackEquipmentSlot == null) return;
        // Check if the event should be cancelled
        if(shouldCancel(itemStack, itemStackEquipmentSlot)) {
            // Cancel the event to protect the item from breaking
            playerItemDamageEvent.setCancelled(true);
        }
    }

    /**
     * Should the event be cancelled because of the durability enchantment?
     * @param itemStack The {@link ItemStack}.
     * @param equipmentSlot The {@link EquipmentSlot} the {@link ItemStack} is in.
     * @return true if cancelled, false if not.
     */
    private boolean shouldCancel(@NonNull ItemStack itemStack, @NonNull EquipmentSlot equipmentSlot) {
        if(itemStack.isEmpty()) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return false;
        if(!(itemMeta instanceof Damageable damageable)) return false;
        if(!damageable.hasDamage()) return false;
        int maxDurability = damageable.hasMaxDamage()
                ? damageable.getMaxDamage()
                : itemStack.getType().getMaxDurability();
        if((maxDurability - damageable.getDamage()) > 1) return false;

        // Get the durability enchantment configuration and if null, return
        Durability durability = durabilityConfigManager.getConfiguration();
        if(durability == null) return false;
        // If the durability enchantment is disabled, return
        if(!durability.isEnabled()) return false;
        // If the durability enchantment is null, return
        Enchantment durabilityEnchantment = enchantmentManager.getDurabilityEnchantment();
        if(durabilityEnchantment == null) return false;

        // Get the EquipmentSlots
        List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(durability.getRegistrationConfig().equipmentSlots());
        // If the equipment slot is not configured to be affected by durability, return false
        if(!equipmentSlots.contains(equipmentSlot)) return false;

        // If the ItemStack doesn't contain the durability enchantment, return false
        if(!itemStack.getEnchantments().containsKey(durabilityEnchantment)) return false;

        // Get the max level of the durability enchantment
        int maxLevel = durability.getRegistrationConfig().maxLevel();

        // Get the enchantment level of the durability enchantment
        int enchantmentLevel = itemStack.getEnchantmentLevel(durabilityEnchantment);

        // If the enchantment level is below or equal to the max level, the event should be cancelled.
        return enchantmentLevel <= maxLevel;
    }
}