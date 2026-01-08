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
import com.github.lukesky19.skyEnchants.config.data.enchantment.Multibreak;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.MultibreakConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.processor.MultibreakProcessor;
import com.github.lukesky19.skyEnchants.util.Direction;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Listens for when a block is broken by a tool that contains the multibreak enchantment and queues the multibreak.
 */
public class MultibreakEnchantmentListener implements Listener {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull ComponentLogger logger;
    private final @NotNull MultibreakConfigManager multibreakConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;
    private final @NotNull List<Location> multibreakLocationsToIgnore = new ArrayList<>();

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param multibreakConfigManager A {@link MultibreakConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public MultibreakEnchantmentListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull MultibreakConfigManager multibreakConfigManager,
            @NotNull EnchantmentManager enchantmentManager) {
        this.skyEnchants = skyEnchants;
        this.logger = skyEnchants.getComponentLogger();
        this.multibreakConfigManager = multibreakConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * When a block is broken by a tool with the Multibreak enchantment, attempt to get the nearby blocks that should be broken and break those blocks.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockMultibreak(BlockBreakEvent blockBreakEvent) {
        Block block = blockBreakEvent.getBlock();
        Location blockLocation = block.getLocation();

        // If the event was cancelled, remove the location from the multibreakLocationsToIgnore if it contains it and return
        if(blockBreakEvent.isCancelled()) {
            multibreakLocationsToIgnore.remove(blockLocation);
            return;
        }

        @Nullable Multibreak multibreak = multibreakConfigManager.getConfiguration();
        if(multibreak == null) {
            logger.error(AdventureUtil.deserialize("Unable to activate a multibreak enchantment due to invalid settings."));
            return;
        }

        // If the multibreak enchantment is disabled, return
        if(!multibreak.isEnabled()) return;
        // If the multibreak enchantment is null, return
        @Nullable Enchantment multibreakEnchantment = enchantmentManager.getMultibreakEnchantment();
        if(multibreakEnchantment == null) return;

        // If no break areas are defined, log an error and return
        if(multibreak.breakAreas().isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to apply multibreak enchantment effect due to invalid plugin settings (No break areas configured)."));
            return;
        }

        // If the block location was broken by the plugin as a result of multibreak, remove the location and return
        if(multibreakLocationsToIgnore.contains(blockLocation)) {
            multibreakLocationsToIgnore.remove(blockLocation);
            return;
        }

        // Get the block's BlockType
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;
        // Get the Player that broke the Block
        Player player = blockBreakEvent.getPlayer();
        // Get the Direction the player is facing
        Direction direction = Direction.getDirectionFacing(player);
        // Get the player's equipment
        EntityEquipment entityEquipment = player.getEquipment();

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(multibreak.getRegistrationConfig().equipmentSlots());
        // Get the max level of the multibreak enchantment
        int maxLevel = multibreak.getRegistrationConfig().maxLevel();

        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the multibreak enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(multibreakEnchantment)) continue;

            // Get the enchantment level of the multibreak enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(multibreakEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the configured area dimensions as a String
            @Nullable String areaDimensionsString = multibreak.breakAreas().get(enchantmentLevel);
            // If there is no break area mapping for the enchantment level, log an error and move to the next EquipmentSlot
            if(areaDimensionsString == null) {
                logger.error(AdventureUtil.deserialize("No dimensions for the area to break for enchantment level " + enchantmentLevel));
                continue;
            }

            // Split the area dimensions into 3 Strings
            String[] splitDimensionStrings = areaDimensionsString.split("x");
            // If the array doesn't contain 3 Strings, log and error and move to the next EquipmentSlot
            if(splitDimensionStrings.length != 3) {
                logger.error(AdventureUtil.deserialize("Invalid dimensions for the area to break for enchantment level " + enchantmentLevel));
                continue;
            }

            // Get ints for based on the 3 Strings from the above array
            int width = Integer.parseInt(splitDimensionStrings[0]);
            int height = Integer.parseInt(splitDimensionStrings[1]);
            int depth = Integer.parseInt(splitDimensionStrings[2]);

            // Create a new MultibreakProcessor to attempt to process the multibreak
            new MultibreakProcessor(
                    skyEnchants,
                    direction,
                    block, blockType,
                    depth, width, height,
                    multibreak.breakSimilarOnly(),
                    multibreak.preventBelowPlayer(),
                    multibreak.maxBlocksPerSection(),
                    multibreak.totalMaxBlocks(),
                    multibreak.locationProcessingDelayTicks(),
                    multibreak.blockBreakDelayTicks(),
                    multibreakLocationsToIgnore,
                    player);

            return;
        }
    }
}
