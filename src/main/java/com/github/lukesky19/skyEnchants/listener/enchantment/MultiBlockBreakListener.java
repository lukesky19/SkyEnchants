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
import com.github.lukesky19.skyEnchants.api.event.MultiBlockBreakEvent;
import com.github.lukesky19.skyEnchants.config.data.enchantment.DoubleDrop;
import com.github.lukesky19.skyEnchants.config.data.enchantment.Magnet;
import com.github.lukesky19.skyEnchants.config.data.enchantment.Smelt;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.DoubleDropConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.MagnetConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.SmeltConfigManager;
import com.github.lukesky19.skyEnchants.manager.block.BlockManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.util.PluginUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.lukesky19.skyEnchants.util.PluginUtils.compact;

/**
 * Listens for a {@link MultiBlockBreakEvent} and attempts to process enchantment effects.
 */
public class MultiBlockBreakListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull DoubleDropConfigManager doubleDropConfigManager;
    private final @NotNull SmeltConfigManager smeltConfigManager;
    private final @NotNull MagnetConfigManager magnetConfigManager;
    private final @NotNull EnchantmentManager enchantmentManager;
    private final @NotNull BlockManager blockManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param doubleDropConfigManager A {@link DoubleDropConfigManager} instance.
     * @param smeltConfigManager A {@link SmeltConfigManager} instance.
     * @param magnetConfigManager A {@link MagnetConfigManager} instance.
     * @param enchantmentManager An {@link EnchantmentManager} instance.
     * @param blockManager A {@link BlockManager} instance.
     */
    public MultiBlockBreakListener(
            @NotNull SkyEnchants skyEnchants,
            @NotNull DoubleDropConfigManager doubleDropConfigManager,
            @NotNull SmeltConfigManager smeltConfigManager,
            @NotNull MagnetConfigManager magnetConfigManager,
            @NotNull EnchantmentManager enchantmentManager,
            @NotNull BlockManager blockManager) {
        this.logger = skyEnchants.getComponentLogger();
        this.doubleDropConfigManager = doubleDropConfigManager;
        this.smeltConfigManager = smeltConfigManager;
        this.magnetConfigManager = magnetConfigManager;
        this.enchantmentManager = enchantmentManager;
        this.blockManager = blockManager;
    }

    /**
     * Attempts to apply enchantment effects.
     * @param multiBlockBreakEvent  A {@link MultiBlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMultiBlockBreak(MultiBlockBreakEvent multiBlockBreakEvent) {
        Player player = multiBlockBreakEvent.getPlayer();
        EntityEquipment playerEquipment = player.getEquipment();
        Map<BlockState, Collection<ItemStack>> blockStateItemStackMap = multiBlockBreakEvent.getBlockStateToItemStackMap();
        Collection<ItemStack> itemList = multiBlockBreakEvent.getItems();
        Location location = multiBlockBreakEvent.getLocation();

        processDoubleDrop(playerEquipment, blockStateItemStackMap, itemList);

        processSmelt(playerEquipment, blockStateItemStackMap, itemList);

        processMagnet(player, playerEquipment, itemList, location);
    }

    /**
     * Attempt to process the double drop enchantment.
     * @param playerEquipment The {@link EntityEquipment} for the player.
     * @param blockStateItemStackMap A {@link Map} mapping {@link BlockState}s to a {@link Collection} of {@link ItemStack}s.
     * @param itemStackList The {@link Collection} of {@link ItemStack}s.
     */
    private void processDoubleDrop(
            @NotNull EntityEquipment playerEquipment,
            @NotNull Map<BlockState, Collection<ItemStack>> blockStateItemStackMap,
            @NotNull Collection<ItemStack> itemStackList) {
        @Nullable DoubleDrop doubleDrop = doubleDropConfigManager.getConfiguration();
        if(doubleDrop == null) {
            logger.error(AdventureUtility.plain("Unable to activate a double drop enchantment due to an invalid settings."));
            return;
        }

        // If the double drop enchantment isn't enabled or is null, return
        if(!doubleDrop.isEnabled()) return;
        @Nullable Enchantment doubleDropEnchantment = enchantmentManager.getDoubleDropEnchantment();
        if(doubleDropEnchantment == null) return;

        @NotNull Map<Integer, Double> chancePerLevel = doubleDrop.chancePerLevel();
        // If there is no chance mapping, log an error and return
        if(chancePerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply the double drop enchantment due to invalid plugin settings (No chance mapping)."));
            return;
        }

        // Get the EquipmentSlots that the haste enchantment can apply to.
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(doubleDrop.getRegistrationConfig().equipmentSlots());
        // Get the max level for the haste enchantment.
        int maxLevel = doubleDrop.getRegistrationConfig().maxLevel();

        // If the player doesn't have equipment with the enchantment, return
        if(lacksEquipment(playerEquipment, equipmentSlots, chancePerLevel, doubleDropEnchantment, maxLevel)) return;

        // Attempt to double the items
        final Collection<ItemStack> result = new ArrayList<>();
        blockStateItemStackMap.forEach((blockState, itemStackCollection) -> {
            // Add the items once, for the existing amount
            result.addAll(itemStackCollection);

            // If the block was not player-placed,  double the items.
            if(!blockManager.isBlockPlayerPlaced(blockState.getBlock())) {
                result.addAll(itemStackCollection);
            }
        });

        Collection<ItemStack> compactedCollection = compact(result);

        itemStackList.clear();

        itemStackList.addAll(compactedCollection);
    }

    /**
     * Attempt to process the smelt enchantment.
     * @param playerEquipment The {@link EntityEquipment} for the player.
     * @param blockStateItemStackMap A {@link Map} mapping {@link BlockState}s to a {@link Collection} of {@link ItemStack}s.
     * @param itemStackList The {@link Collection} of {@link ItemStack}s.
     */
    private void processSmelt(
            @NotNull EntityEquipment playerEquipment,
            @NotNull Map<BlockState, Collection<ItemStack>> blockStateItemStackMap,
            @NotNull Collection<ItemStack> itemStackList) {
        @Nullable Smelt smelt = smeltConfigManager.getConfiguration();
        if(smelt == null) {
            logger.error(AdventureUtility.plain("Unable to activate the smelt enchantment due to invalid settings."));
            return;
        }

        // If the smelt enchantment is disabled, return
        if(!smelt.enabled()) return;
        // If the smelt enchantment is null, return
        @Nullable Enchantment smeltEnchantment = enchantmentManager.getSmeltEnchantment();
        if(smeltEnchantment == null) return;

        @NotNull Map<Integer, Double> chancePerLevel = smelt.chancePerLevel();
        // If there is no chance mapping, log an error and return
        if(chancePerLevel.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to apply the smelt enchantment due to invalid settings (No chance mapping)."));
            return;
        }

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(smelt.getRegistrationConfig().equipmentSlots());
        // Get the max level of the smelt enchantment
        int maxLevel = smelt.getRegistrationConfig().maxLevel();

        // If the player doesn't have equipment with the enchantment, return
        if(lacksEquipment(playerEquipment, equipmentSlots, chancePerLevel, smeltEnchantment, maxLevel)) return;

        // Smelt the items
        final Collection<ItemStack> result = new ArrayList<>();
        blockStateItemStackMap.forEach((blockState, itemStackCollection) -> {
            if(!blockManager.isBlockPlayerPlaced(blockState.getBlock())) {
                itemStackCollection.forEach(itemStack -> {
                    @Nullable ItemStack smeltedItem = getSmeltedItemStack(itemStack);
                    result.add(Objects.requireNonNullElse(smeltedItem, itemStack));
                });
            } else {
                result.addAll(itemStackCollection);
            }
        });

        Collection<ItemStack> compactedCollection = compact(result);

        itemStackList.clear();

        itemStackList.addAll(compactedCollection);
    }

    /**
     * Attempt to process the magnet enchantment.
     * @param player The {@link Player}.
     * @param playerEquipment The {@link EntityEquipment} for the player.
     * @param itemStackList The {@link Collection} of {@link ItemStack}s or null.
     * @param itemLocation The {@link Location} the {@link List} of {@link ItemStack}s will be dropped at, or null.
     */
    private void processMagnet(
            @NotNull Player player,
            @NotNull EntityEquipment playerEquipment,
            @NotNull Collection<ItemStack> itemStackList,
            @NotNull Location itemLocation) {
        @NotNull Location playerLocation = player.getLocation();
        @NotNull PlayerInventory playerInventory = player.getInventory();
        @NotNull ItemStack toolStack = player.getInventory().getItemInMainHand();
        if(toolStack.isEmpty()) return;

        @Nullable Magnet magnet = magnetConfigManager.getConfiguration();
        if(magnet == null) {
            logger.error(AdventureUtility.plain("Unable to activate the magnet enchantment due to invalid settings."));
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
                logger.error(AdventureUtility.plain("Unable to apply magnet enchantment effect due to invalid settings (No distance to levels mappings are configured)."));
                return;
            }
        }

        // Get the configured slots the enchantment can activate in
        @NotNull List<EquipmentSlot> equipmentSlots = PluginUtils.getEquipmentSlots(magnet.getRegistrationConfig().equipmentSlots());
        // Get the max level of the magnet enchantment
        int maxLevel = magnet.getRegistrationConfig().maxLevel();

        @Nullable Double distance = null;
        // Loop through the possible EquipmentSlots that the enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = playerEquipment.getItem(equipmentSlot);

            // If the ItemStack is not valid, move to the next EquipmentSlot
            if(isItemStackInvalid(itemStack, magnetEnchantment, maxLevel)) continue;

            distance = magnet.pickupDistanceSquaredPerLevel().get(itemStack.getEnchantmentLevel(magnetEnchantment));
            if(distance == null) continue;

            break;
        }

        // Add the items to the player's inventory if possible
        final Collection<ItemStack> result = new ArrayList<>();
        if(magnet.guaranteedPickup()) {
            itemStackList.forEach(itemStack -> {
                List<ItemStack> leftover = addToInventory(playerInventory, itemStack);

                result.addAll(leftover);
            });
        } else {
            // If no distance value was found, return
            if(distance == null) return;
            double distanceSquared = itemLocation.distanceSquared(playerLocation);
            if(distanceSquared <= distance) return;

            itemStackList.forEach(itemStack -> {
                List<ItemStack> leftover = addToInventory(playerInventory, itemStack);
                result.addAll(leftover);
            });
        }

        Collection<ItemStack> compactedCollection = compact(result);

        itemStackList.clear();

        itemStackList.addAll(compactedCollection);
    }

    /**
     * Does the player have equipment with a valid enchantment to activate the effect?
     * @param playerEquipment The {@link EntityEquipment} for the player.
     * @param equipmentSlots The {@link List} of {@link EquipmentSlot}s the enchantment can activate in.
     * @param chancePerLevel The {@link Map} mapping enchantment levels to chances to activate. May be null.
     * @param enchantment The enchantment.
     * @param maxLevel The max level.
     * @return true if the player lacks equipment with a valid enchantment, or false.
     */
    private boolean lacksEquipment(
            @NotNull EntityEquipment playerEquipment,
            @NotNull List<EquipmentSlot> equipmentSlots,
            @NotNull Map<Integer, Double> chancePerLevel,
            @NotNull Enchantment enchantment,
            int maxLevel) {
        // Loop through the possible EquipmentSlots that the enchantment can activate in.
        for(EquipmentSlot equipmentSlot : equipmentSlots) {
            // Get the ItemStack in the EquipmentSlot
            @NotNull ItemStack itemStack = playerEquipment.getItem(equipmentSlot);

            // If the ItemStack is not valid, move to the next EquipmentSlot
            if(isItemStackInvalid(itemStack, enchantment, maxLevel)) continue;

            // Get the enchantment level
            int enchantmentLevel = itemStack.getEnchantmentLevel(enchantment);
            // Get the chance that the enchantment is applied
            @Nullable Double chance = chancePerLevel.get(enchantmentLevel);
            // Log an error if there is no chance configured for the enchantment level
            if (chance == null) {
                logger.error(AdventureUtility.plain("Unable to apply the " + enchantment.getKey() + " enchantment due to invalid plugin settings (No chance to activate for enchantment level: " + enchantmentLevel + ")."));
                continue;
            }

            // If the chance to activate fails, move to the next EquipmentSlot
            if (!isActivated(chance)) continue;

            return false;
        }

        return true;
    }

    /**
     * Checks if the item is invalid based on the ItemStack and Enchantment.
     * @param itemStack The {@link ItemStack}.
     * @param enchantment The {@link Enchantment}.
     * @return true if invalid, false if not.
     */
    private boolean isItemStackInvalid(@NotNull ItemStack itemStack, @NotNull Enchantment enchantment, int maxLevel) {
        // If the ItemStack is empty (air), the item is not valid.
        if(itemStack.isEmpty()) return true;
        // If the ItemStack doesn't contain the enchantment, the item is not valid.
        if(!itemStack.getEnchantments().containsKey(enchantment)) return true;

        // Get the enchantment level the item has for the enchantment
        int enchantmentLevel = itemStack.getEnchantmentLevel(enchantment);
        // If the enchantment level is over the max level, the item is not valid.
        return enchantmentLevel > maxLevel;
    }

    /**
     * Based ont he configured enchantment chance provided, can the enchantment be activated?
     * @param chance The enchantment chance.
     * @return true if allowed, false if not.
     */
    private boolean isActivated(double chance) {
        if(chance >= 1.0) return true;
        if(chance <= 0.0) return false;
        Random random = ThreadLocalRandom.current();

        // Calculate the random chance
        double randomChance = random.nextDouble(0, 1);
        // If the chance is greater than or equal to the random chance, the enchantment is allowed.
        return chance >= randomChance;
    }

    /**
     * Attempt to get the {@link ItemStack} that would result from smelting the provided {@link ItemStack}.
     * @param item The {@link ItemStack} to get the smelted result for.
     * @return The {@link ItemStack} or null.
     */
    private @Nullable ItemStack getSmeltedItemStack(@NotNull ItemStack item) {
        if(item.isEmpty()) return null;

        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while(recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if(!(recipe instanceof FurnaceRecipe furnaceRecipe)) continue;
            if(!furnaceRecipe.getInputChoice().test(item)) continue;

            ItemStack result = furnaceRecipe.getResult();

            // Get the output amount per input item
            int outputPerInput = result.getAmount();
            // Calculate total output based on input amount
            int totalOutput = (item.getAmount() * outputPerInput);

            // Create and return the new ItemStack with calculated amount
            return new ItemStack(result.getType(), totalOutput);
        }

        return null;
    }

    /**
     * Attempt the {@link ItemStack} to the {@link Inventory}.
     * @param inventory The Inventory to add the item to.
     * @param itemStack The ItemStack to add.
     * @return A {@link List} of {@link ItemStack}s that were leftover because the full ItemStack didn't fit in the Inventory.
     */
    private @NotNull List<ItemStack> addToInventory(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        Map<Integer, ItemStack> leftover = inventory.addItem(itemStack);
        if(leftover.isEmpty()) return new ArrayList<>();

        return leftover.values().stream()
                .filter(leftoverItemStack -> !leftoverItemStack.isEmpty())
                .toList();
    }
}