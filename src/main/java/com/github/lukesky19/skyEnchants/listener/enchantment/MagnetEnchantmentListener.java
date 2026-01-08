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

import com.github.lukesky19.skyEnchants.config.data.enchantment.Magnet;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.MagnetConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * Listens for when a tool that contains the magnet enchantment causes a block to drop items.
 */
public class MagnetEnchantmentListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull MagnetConfigManager magnetConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger} instance.
     * @param magnetConfigManager A {@link MagnetConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public MagnetEnchantmentListener(
            @NotNull ComponentLogger logger,
            @NotNull MagnetConfigManager magnetConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.magnetConfigManager = magnetConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * When a block drops items, check if the tool has the magnet enchantment, and give the items to the player as necessary according to the configuration.
     * @param blockDropItemEvent A {@link BlockDropItemEvent}.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent blockDropItemEvent) {
        @NotNull Player player = blockDropItemEvent.getPlayer();
        @NotNull Inventory playerInventory = player.getInventory();
        @NotNull Location playerLocation = player.getLocation();
        @NotNull EntityEquipment entityEquipment = player.getEquipment();
        @NotNull ItemStack toolStack = player.getInventory().getItemInMainHand();
        if(toolStack.isEmpty()) return;

        @Nullable Magnet magnet = magnetConfigManager.getConfiguration();
        if(magnet == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate the magnet enchantment due to invalid settings."));
            return;
        }

        // If the magnet enchantment is disabled, return
        if(!magnet.isEnabled()) return;
        // If the magnet enchantment is null, return
        @Nullable Enchantment magnetEnchantment = enchantmentManager.getMagnetEnchantment();
        if(magnetEnchantment == null) return;

        // Only check distance-level mappings if guaranteed pickup is false
        if(!magnet.guaranteedPickup()) {
            // If no distance-level values are defined, log an error and return
            if(magnet.pickupDistanceSquaredPerLevel().isEmpty()) {
                logger.error(AdventureUtil.deserialize("Unable to apply durability enchantment effect due to invalid settings (No distance to levels mappings are configured)."));
                return;
            }
        }

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(magnet.getRegistrationConfig().equipmentSlots());
        // Get the max level of the magnet enchantment
        int maxLevel = magnet.getRegistrationConfig().maxLevel();

        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the magnet enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(magnetEnchantment)) continue;

            // Get the enchantment level of the magnet enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(magnetEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;
            // If there is no distance mapping for the level, move to the next EquipmentSlot
            @Nullable Double distanceSquaredForLevel = magnet.pickupDistanceSquaredPerLevel().get(enchantmentLevel);
            if(distanceSquaredForLevel == null) continue;

            // Get the ItemStacks being dropped
            @NotNull List<Item> droppedItems = blockDropItemEvent.getItems();

            if(magnet.guaranteedPickup()) {
                droppedItems.stream()
                        .map(Item::getItemStack)
                        .forEach(droppedItem -> PlayerUtil.giveItem(playerInventory, droppedItem, droppedItem.getAmount(), playerLocation));
            } else {
                Iterator<Item> itemIterator = droppedItems.iterator();
                while(itemIterator.hasNext()) {
                    Item item = itemIterator.next();
                    Location itemLocation = item.getLocation();

                    // If the dropped item is within the proper distance, give the item to the player and remove the item from the iterator
                    if(itemLocation.distanceSquared(playerLocation) <= distanceSquaredForLevel) {
                        ItemStack droppedItem = item.getItemStack();

                        PlayerUtil.giveItem(playerInventory, droppedItem, droppedItem.getAmount(), playerLocation);

                        itemIterator.remove();
                    }
                }
            }

            return;
        }
    }
}