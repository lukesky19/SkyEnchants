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

import com.github.lukesky19.skyEnchants.config.data.enchantment.DoubleJump;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DoubleJumpConfigManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Input;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Listens for when a player attempts to jump while in the air and sets their velocity to jump again.
 * Also listens for when a player walks on solid ground and or logs out to reset their jump count.
 */
public class DoubleJumpEnchantmentListener implements Listener {
    private final @NonNull ComponentLogger logger;
    private final @NonNull DoubleJumpConfigManager doubleJumpConfigManager;
    private final @NonNull EnchantmentManager enchantmentManager;
    private final @NonNull Map<UUID, Integer> jumpCounts = new HashMap<>();

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}
     * @param doubleJumpConfigManager A {@link DoubleJumpConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     */
    public DoubleJumpEnchantmentListener(
            @NonNull ComponentLogger logger,
            @NonNull DoubleJumpConfigManager doubleJumpConfigManager,
            @NonNull EnchantmentManager enchantmentManager) {
        this.logger = logger;
        this.doubleJumpConfigManager = doubleJumpConfigManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * Listens for when a player attempts to jump while in the air and sets their velocity to jump again.
     * @param playerInputEvent A {@link PlayerInputEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJumpInput(PlayerInputEvent playerInputEvent) {
        // Get the Player
        Player player = playerInputEvent.getPlayer();
        // Get the player's unique id
        UUID uuid = player.getUniqueId();
        // Get the equipment for the player
        EntityEquipment entityEquipment = player.getEquipment();
        // Get the input
        Input input = playerInputEvent.getInput();
        // If the input is not a jump, return
        if(!input.isJump()) return;

        DoubleJump doubleJump = doubleJumpConfigManager.getConfiguration();
        if(doubleJump == null) {
            logger.error(AdventureUtility.plain("Unable to activate a double jump enchantment due to an invalid settings."));
            return;
        }

        // If the double jump enchantment isn't enabled, return
        if(!doubleJump.enabled()) return;
        // Get the double jump enchantment
        Enchantment doubleJumpEnchantment = enchantmentManager.getDoubleJumpEnchantment();
        // If the double jump enchantment is null, return
        if(doubleJumpEnchantment == null) return;

        // Get the jump velocity mapping
        Map<Integer, Double> jumpVelocityPerLevel = doubleJump.velocityPerLevel();
        // If the jump velocity is empty, log an error and return
        if(jumpVelocityPerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply double jump enchantment action due to invalid plugin settings (No velocity mapping)."));
            return;
        }

        // If the player's jump count is greater than or equal to 1, return
        if(jumpCounts.getOrDefault(uuid, 0) >= 1) return;

        // Insert the jump count for the player
        jumpCounts.put(uuid, jumpCounts.getOrDefault(uuid, 0) + 1);

        // Check the block directly below the player is empty (air)
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
        // If the block below the player is solid, return
        if(blockBelow.getType().isSolid()) return;

        // Get the EquipmentSlots that the double jump enchantment can activate in
        List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(doubleJump.getRegistrationConfig().equipmentSlots());
        // Get the max level for the double jump enchantment
        int maxLevel = doubleJump.getRegistrationConfig().maxLevel();

        // Loop through the possible EquipmentSlots that the double jump enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            ItemStack itemStack = entityEquipment.getItem(equipmentSlot);
            // If the ItemStack is empty (air), move to the next EquipmentSlot
            if(itemStack.isEmpty()) continue;
            // If the ItemStack doesn't contain the double jump enchantment, move to the next EquipmentSlot
            if(!itemStack.getEnchantments().containsKey(doubleJumpEnchantment)) continue;

            // Get the enchantment level of the double jump enchantment
            int enchantmentLevel = itemStack.getEnchantmentLevel(doubleJumpEnchantment);
            // If the enchantment level is over the max level, move to the next EquipmentSlot
            if(enchantmentLevel > maxLevel) continue;

            // Get the Player's Velocity
            Vector playerVelocity = player.getVelocity();
            // Set the y velocity to 0
            playerVelocity.setY(0);

            // Zero the player's velocity if configured to do so
            if(doubleJump.ignoreCurrentVelocity()) {
                // Set the y velocity to 0
                playerVelocity.setY(0);
                // Update the player's velocity
                player.setVelocity(playerVelocity);
            }

            // Get the jump velocity for the enchantment level
            Double jumpVelocity = jumpVelocityPerLevel.get(enchantmentLevel);
            // Log an error if no jump velocity is configured
            if(jumpVelocity == null) {
                logger.error(AdventureUtility.plain("Unable to apply the double jump enchantment effect due to invalid plugin settings (No jump velocity mapping for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // Add the jump velocity
            playerVelocity.add(new Vector(0, jumpVelocity, 0));

            // Update the player's velocity
            player.setVelocity(playerVelocity);
        }
    }

    /**
     * Listens for when a player moves on solid ground and resets their jump count
     * @param playerMoveEvent A {@link PlayerMoveEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        DoubleJump doubleJump = doubleJumpConfigManager.getConfiguration();
        if(doubleJump == null) {
            logger.error(AdventureUtility.plain("Unable to reset jump count due to invalid settings."));
            return;
        }

        // If the double jump enchantment isn't enabled, return
        if(!doubleJump.enabled()) return;

        // Get the player and the player's unique id
        Player player = playerMoveEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check the block directly below the player
        Block blockBelow = player.getLocation().clone().subtract(0, 1, 0).getBlock();

        // Check if the block is solid
        if(blockBelow.getType().isSolid()) {
            // Reset jump count when on ground
            jumpCounts.remove(uuid);
        }
    }

    /**
     * Removes any jump counts stored for the player when they disconnect.
     * @param playerQuitEvent A {@link PlayerQuitEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        jumpCounts.remove(playerQuitEvent.getPlayer().getUniqueId());
    }
}