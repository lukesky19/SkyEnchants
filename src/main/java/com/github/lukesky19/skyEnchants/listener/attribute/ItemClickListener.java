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
package com.github.lukesky19.skyEnchants.listener.attribute;

import com.github.lukesky19.skyEnchants.manager.attribute.AttributeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * Listens to when an ItemStack is clicked and updates the attributes on the item if it has the reach, speed, and or health enchantment.
 */
public class ItemClickListener implements Listener {
    private final @NonNull AttributeManager attributeManager;

    /**
     * Constructor
     * @param attributeManager An {@link AttributeManager} instance.
     */
    public ItemClickListener(@NonNull AttributeManager attributeManager) {
        this.attributeManager = attributeManager;
    }

    /**
     * Listens to when an ItemStack is clicked and updates the attributes on the item if it has the reach, speed, and or health enchantment.
     * @param inventoryClickEvent A {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemClick(InventoryClickEvent inventoryClickEvent) {
        ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
        ItemStack cursorItem = inventoryClickEvent.getCursor();

        if(clickedItem != null && !clickedItem.isEmpty()) {
            attributeManager.applyAttributes(clickedItem);
        }

        if(!cursorItem.isEmpty()) {
            attributeManager.applyAttributes(cursorItem);
        }
    }
}
