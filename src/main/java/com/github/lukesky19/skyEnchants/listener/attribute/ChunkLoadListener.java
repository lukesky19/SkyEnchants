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
import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.*;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Listens to when a chunk is loaded and for all entities in that chunk, updates the attributes on the entity's equipment if it has the reach, speed, and or health enchantment.
 */
public class ChunkLoadListener implements Listener {
    private final @NonNull AttributeManager attributeManager;

    /**
     * Constructor
     * @param attributeManager An {@link AttributeManager} instance.
     */
    public ChunkLoadListener(@NonNull AttributeManager attributeManager) {
        this.attributeManager = attributeManager;
    }

    /**
     * Listens to when a chunk is loaded and for all entities in that chunk, updates the attributes on the entity's equipment if it has the reach, speed, and or health enchantment.
     * @param chunkLoadEvent A {@link ChunkLoadEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        Chunk chunk = chunkLoadEvent.getChunk();

        Arrays.stream(chunk.getEntities())
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                .map(entity -> (LivingEntity) entity)
                .forEach(livingEntity -> {
                    EntityEquipment entityEquipment = livingEntity.getEquipment();

                    if(entityEquipment != null) attributeManager.applyAttributes(entityEquipment);
                });
    }
}
