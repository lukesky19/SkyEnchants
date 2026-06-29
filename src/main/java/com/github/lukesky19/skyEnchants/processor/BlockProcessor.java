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
package com.github.lukesky19.skyEnchants.processor;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.config.data.enchantment.Durability;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DurabilityConfigManager;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.Damageable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * This class can be extended to create a block processor.
 */
public abstract class BlockProcessor {
    /**
     * A {@link SkyEnchants} instance.
     */
    protected final @NonNull SkyEnchants skyEnchants;
    /**
     * A {@link DurabilityConfigManager} instance.
     */
    protected final @NonNull DurabilityConfigManager durabilityConfigManager;
    /**
     * An {@link EnchantmentManager} instance.
     */
    protected final @NonNull EnchantmentManager enchantmentManager;
    /**
     * A {@link HookManager} instance.
     */
    protected final @NonNull HookManager hookManager;

    // Player Data
    /**
     * The {@link Player} to attribute the broken blocks to.
     */
    protected final @NonNull Player player;
    /**
     * The {@link ItemStack} to use.
     */
    protected @Nullable ItemStack tool;
    /**
     * The slot number of where the tool is in the player's inventory.
     */
    protected final int toolSlotNumber;

    // Processing Data
    /**
     * The starting {@link Block}.
     */
    protected final @NonNull Block startingBlock;
    /**
     * The starting location. This is the location of the starting block.
     */
    protected final @NonNull Location startingLocation;
    /**
     * The {@link World} the processing is occurring in.
     */
    protected final @NonNull World world;
    /**
     * The {@link Deque} of {@link Location}s to be processed.
     */
    protected final @NonNull Deque<Location> locationQueue = new ArrayDeque<>();
    /**
     * The {@link Deque} of {@link Block}s to be processed.
     */
    protected final @NonNull Deque<Block> blockQueue = new ArrayDeque<>();
    /**
     * The locations already processed. See {@link #pack(Location)} and {@link #pack(int, int, int)} to convert coordinates to a single long.
     */
    protected final @NonNull LongOpenHashSet processedLocations = new LongOpenHashSet();

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param durabilityConfigManager A {@link DurabilityConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param startingBlock The starting {@link Block}.
     * @param player The {@link Player}.
     * @param tool The {@link ItemStack} used.
     * @param toolSlotNumber The slot number of the tool.
     */
    public BlockProcessor(
            @NonNull SkyEnchants skyEnchants,
            @NonNull DurabilityConfigManager durabilityConfigManager,
            @NonNull EnchantmentManager enchantmentManager,
            @NonNull HookManager hookManager,
            @NonNull Block startingBlock,
            @NonNull Player player,
            @NonNull ItemStack tool,
            int toolSlotNumber) {
        this.skyEnchants = skyEnchants;
        this.durabilityConfigManager = durabilityConfigManager;
        this.enchantmentManager = enchantmentManager;
        this.hookManager = hookManager;

        // Player, tool, and slot number
        this.player = player;
        this.tool = tool;
        this.toolSlotNumber = toolSlotNumber;

        // Starting Block, Location and World
        this.startingBlock = startingBlock;
        this.startingLocation = startingBlock.getLocation();
        this.world = startingLocation.getWorld();
    }

    /**
     * Queue the location or locations based on the location. Locations should be added to {@link #locationQueue}.
     * @param location The {@link Location} to process.
     */
    protected abstract void queueLocations(@NonNull Location location);

    /**
     * Process the locations in the {@link #locationQueue}.
     */
    protected abstract void processLocations();

    /**
     * Break the blocks in the {@link #blockQueue}.
     */
    protected abstract void breakBlocks();

    /**
     * Clear any data.
     */
    protected void cleanup() {
        locationQueue.clear();
        blockQueue.clear();
        processedLocations.clear();
    }

    /**
     * Convert a {@link Location}'s x, y, and z coordinates to a single long.
     * @param location The {@link Location}
     * @return A long.
     */
    protected long pack(@NonNull Location location) {
        return pack(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Convert the x, y, and z coordinates to a single long.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return A long.
     */
    protected long pack(int x, int y, int z) {
        return (((long) x & 0x3FFFFFFL) << 38) | (((long) z & 0x3FFFFFFL) << 12) | ((long) y & 0xFFFL);
    }

    /**
     * Check if the tool should take durability based on the unbreaking enchantment.
     * @param tool The {@link ItemStack}.
     * @return true if durability should be removed, false if not.
     */
    protected boolean checkUnbreaking(@NonNull ItemStack tool) {
        if(tool.isEmpty()) return false;

        int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        if(unbreakingLevel > 0) {
            double chance = 1.0 / (unbreakingLevel + 1);
            return Math.random() < chance;
        }

        return true;
    }

    /**
     * Check if the durability enchantment is activated preventing the tool from breaking.
     * @param tool The {@link ItemStack}.
     * @param damageable The {@link Damageable}.
     * @return true if the durability enchantment is activated, false if not.
     */
    protected boolean isProtected(@NonNull ItemStack tool, @NonNull Damageable damageable) {
        if(!damageable.hasDamage()) return false;
        int maxDurability = damageable.hasMaxDamage()
                ? damageable.getMaxDamage()
                : tool.getType().getMaxDurability();
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
        if(!equipmentSlots.contains(EquipmentSlot.HAND)) return false;

        // If the ItemStack doesn't contain the durability enchantment, return false
        if(!tool.getEnchantments().containsKey(durabilityEnchantment)) return false;

        // Get the max level of the durability enchantment
        int maxLevel = durability.getRegistrationConfig().maxLevel();

        // Get the enchantment level of the durability enchantment
        int enchantmentLevel = tool.getEnchantmentLevel(durabilityEnchantment);

        // If the enchantment level is below or equal to the max level, the event should be cancelled.
        return enchantmentLevel <= maxLevel;
    }

    /**
     * Attempt to update the tool's durability.
     * @apiNote Checks for unbreaking and the durability enchantment.
     * Sets the tool to null if broken.
     * @param damageable The {@link Damageable} meta.
     */
    protected void updateDurability(@NonNull Damageable damageable) {
        if(tool == null) return;
        if(!checkUnbreaking(tool)) return;
        if(isProtected(tool, damageable)) return;
        int maxDurability = damageable.hasMaxDamage()
                ? damageable.getMaxDamage()
                : tool.getType().getMaxDurability();

        int updatedDurability = damageable.getDamage() + 1;
        if(updatedDurability == maxDurability) {
            tool = null;
            player.getInventory().setItem(toolSlotNumber, ItemType.AIR.createItemStack());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        } else {
            damageable.setDamage(updatedDurability);
        }
    }
}