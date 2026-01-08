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
package com.github.lukesky19.skyEnchants.gui;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.config.data.options.EnchantmentOptionsConfig;
import com.github.lukesky19.skyEnchants.config.data.locale.Locale;
import com.github.lukesky19.skyEnchants.config.data.misc.EnchantmentOptions;
import com.github.lukesky19.skyEnchants.config.data.settings.Settings;
import com.github.lukesky19.skyEnchants.config.data.gui.EnchanterGUIConfig;
import com.github.lukesky19.skyEnchants.config.data.gui.ButtonConfig;
import com.github.lukesky19.skyEnchants.config.data.misc.ApplicationCost;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.integration.hooks.EconomyHook;
import com.github.lukesky19.skyEnchants.integration.hooks.PlayerPointsHook;
import com.github.lukesky19.skyEnchants.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyEnchants.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyEnchants.manager.hook.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class creates the GUI to access different shop categories.
 */
public class EnchanterGUI extends ChestGUI<UUID> {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager;
    private final @NotNull HookManager hookManager;
    private final @Nullable EnchanterGUIConfig enchanterGUIConfig;

    // The selected input item
    private @Nullable ItemStack input1ItemStack;
    // This is the slot where the item was in the player's inventory.
    private int input1Slot = -1;
    // The select input enchantment
    private @Nullable ItemStack input2ItemStack;
    // This is the slot where the item was in the player's inventory.
    private int input2Slot = -1;

    // This is the output ItemStack to give to the player.
    private @Nullable ItemStack outputItemStack;
    // This contains the costs to enchant the item.
    private @Nullable ApplicationCost applicationCost;
    // This contains the enchantments to add and return
    private @Nullable EnchantmentData enchantmentData;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param player The {@link Player} viewing the GUI/Inventory.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param enchantmentOptionsConfigManager An {@link EnchantmentOptionsConfigManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public EnchanterGUI(
            @NotNull SkyEnchants skyEnchants,
            @NotNull UUIDGUIManager guiManager,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HookManager hookManager) {
        super(skyEnchants, guiManager, player.getUniqueId(), player);

        this.skyEnchants = skyEnchants;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.enchantmentOptionsConfigManager = enchantmentOptionsConfigManager;
        this.hookManager = hookManager;
        this.enchanterGUIConfig = guiConfigManager.getEnchanterGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(enchanterGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the enchanter GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = enchanterGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the enchanter GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54 -> {}

            default -> {
                logger.error(AdventureUtil.deserialize("Unsupported GUI Type in enchanter GUI config. Allowed Types: CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
                return false;
            }
        }

        String guiName = Objects.requireNonNullElse(enchanterGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(enchanterGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the enchanter GUI due to invalid gui configuration."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the enchanter GUI as the InventoryView was not created."));
            return false;
        }

        createFillerButtons(inventoryView.getTopInventory().getSize());
        createDummyButtons();

        this.enchantmentData = getEnchantmentData();
        this.applicationCost = calculateApplicationCosts();
        createPlayerInfoButton();
        createCostsButton();
        createInput1Button();
        createInput2Button();
        createOutputButton();

        return super.update();
    }

    /**
     * Update the buttons that change based on user input.
     */
    private void updateDynamicButtons() {
        if(enchanterGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to update the dynamic buttons for the enchanter GUI due to invalid gui configuration."));
            return;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to update the dynamic buttons for the enchanter GUI as the InventoryView was not created."));
            return;
        }

        this.enchantmentData = getEnchantmentData();
        this.applicationCost = calculateApplicationCosts();
        createPlayerInfoButton();
        createCostsButton();
        createInput1Button();
        createInput2Button();
        createOutputButton();

        super.update();
    }

    /**
     * Close the GUI and return any input items back to the player.
     */
    @Override
    public void close() {
        Inventory playerInventory = player.getInventory();
        if(input1ItemStack != null && input1Slot != -1) {
            playerInventory.setItem(input1Slot, input1ItemStack);
        }

        if(input2ItemStack != null && input2Slot != -1) {
            playerInventory.setItem(input2Slot, input2ItemStack);
        }

        input1ItemStack = null;
        input1Slot = -1;
        input2ItemStack = null;
        input2Slot = -1;

        super.close();
    }

    /**
     * Handles when the inventory is closed. Ignores closures with reason UNLOADED and OPEN_NEW.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)
                || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        Inventory playerInventory = player.getInventory();
        if(input1ItemStack != null && input1Slot != -1) {
            playerInventory.setItem(input1Slot, input1ItemStack);
        }

        if(input2ItemStack != null && input2Slot != -1) {
            playerInventory.setItem(input2Slot, input2ItemStack);
        }

        input1ItemStack = null;
        input1Slot = -1;
        input2ItemStack = null;
        input2Slot = -1;
    }

    /**
     * Handles when items are dragged across the player's inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the entire inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        // If the InventoryView is null, return
        if(inventoryView == null) return;
        // If the GUI config is null, return
        if(enchanterGUIConfig == null) return;
        // If the GUI already has items in the GUI, return
        if(input1ItemStack != null && input2ItemStack != null) return;

        int clickedSlot = inventoryClickEvent.getSlot();
        skyEnchants.getServer().getScheduler().runTask(skyEnchants, () -> {
            if(!player.isOnline() && !player.isConnected()) return;
            @Nullable ItemStack clickedItemStack = player.getInventory().getItem(clickedSlot);
            if(clickedItemStack == null || clickedItemStack.isEmpty()) return;

            if(input1ItemStack == null) {
                input1ItemStack = clickedItemStack;
                input1Slot = clickedSlot;

                player.getInventory().setItem(clickedSlot, ItemType.AIR.createItemStack());

                updateDynamicButtons();
            } else if(input2ItemStack == null) {
                input2ItemStack = clickedItemStack;
                input2Slot = clickedSlot;

                player.getInventory().setItem(clickedSlot, ItemType.AIR.createItemStack());

                updateDynamicButtons();
            }

            super.update();
        });
    }

    /**
     * Handles when a click occurs in either inventory. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create the filler buttons for the GUI.
     * @param guiSize The size of the GUI.
     */
    private void createFillerButtons(int guiSize) {
        if(enchanterGUIConfig == null) return;

        ItemStackConfig fillerConfig = enchanterGUIConfig.filler();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyEnchants.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(fillerConfig, player, null, List.of());

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(itemStack);

            for(int i = 0; i <= guiSize - 1; i++) {
                setButton(i, builder.build());
            }
        });
    }

    /**
     * Create the costs button for the GUI
     */
    private void createCostsButton() {
        if(inventoryView == null) return;
        if(enchanterGUIConfig == null) return;
        @Nullable EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a the costs button to the enchanter GUI due to an invalid enchantment options config."));
            return;
        }

        ButtonConfig buttonConfig = enchanterGUIConfig.costButton();
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a the costs button to the enchanter GUI due to an invalid slot."));
            return;
        }

        // Get the required money, exp levels, and player points
        double requiredMoney = 0.0;
        int requiredExpLevels = 0;
        int requiredPoints = 0;
        if(applicationCost != null) {
            requiredMoney = applicationCost.money();
            requiredExpLevels = applicationCost.exp();
            requiredPoints = applicationCost.points();
        }

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("required_money", String.valueOf(requiredMoney)),
                Placeholder.parsed("required_levels", String.valueOf(requiredExpLevels)),
                Placeholder.parsed("required_points", String.valueOf(requiredPoints)));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, null, placeholders);

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(itemStack);

            setButton(buttonConfig.slot(), builder.build());
        });
    }

    /**
     * Create the player info button for the GUI
     */
    private void createPlayerInfoButton() {
        if(inventoryView == null) return;
        if(enchanterGUIConfig == null) return;

        ButtonConfig buttonConfig = enchanterGUIConfig.playerInfoButton();
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a the player info button to the enchanter GUI due to an invalid slot."));
            return;
        }

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        String balance = "0.00";
        if(economyHook.isHooked()) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            balance = df.format(bigBalance);
        }

        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);
        String points = "0";
        if(playerPointsHook.isHooked()) {
            points = String.valueOf(playerPointsHook.getBalance(player));
        }

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("money", balance),
                Placeholder.parsed("levels", String.valueOf(player.getLevel())),
                Placeholder.parsed("points", points));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, null, placeholders);

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(itemStack);

            setButton(buttonConfig.slot(), builder.build());
        });
    }

    /**
     * Create the button to either display the first input item or the placeholder button.
     */
    private void createInput1Button() {
        if(inventoryView == null) return;
        if(enchanterGUIConfig == null) return;

        ButtonConfig buttonConfig = enchanterGUIConfig.inputItem();
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the first input item button to the enchanter GUI due to an invalid slot."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        if(input1ItemStack != null && input1Slot != -1) {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(input1ItemStack);
            builder.setAction(inventoryClickEvent -> {
                if(input1Slot == -1 || input1ItemStack == null) return;

                player.getInventory().setItem(input1Slot, input1ItemStack);

                input1ItemStack = null;
                input1Slot = -1;

                updateDynamicButtons();

                super.update();
            });

            setButton(buttonConfig.slot(), builder.build());
        } else {
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, null, List.of());

            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();
                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        }
    }

    /**
     * Create the button to either display the second input item or the placeholder button.
     */
    private void createInput2Button() {
        if(inventoryView == null) return;
        if(enchanterGUIConfig == null) return;

        ButtonConfig buttonConfig = enchanterGUIConfig.inputEnchantment();
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a the input enchantment button to the enchanter GUI due to an invalid slot."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        if(input2ItemStack != null && input2Slot != -1) {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(input2ItemStack);
            builder.setAction(inventoryClickEvent -> {
                if(input2Slot == -1 || input2ItemStack == null) return;

                player.getInventory().setItem(input2Slot, input2ItemStack);

                input2ItemStack = null;
                input2Slot = -1;

                updateDynamicButtons();

                super.update();
            });

            setButton(buttonConfig.slot(), builder.build());
        } else {
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, null, List.of());

            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();
                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        }
    }

    /**
     * Create the button to either display the result item or the placeholder button.
     */
    private void createOutputButton() {
        if(inventoryView == null) return;
        if(enchanterGUIConfig == null) return;

        ButtonConfig buttonConfig = enchanterGUIConfig.output();
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the output button to the enchanter GUI due to an invalid slot."));
            return;
        }

        if(input1ItemStack != null && input2ItemStack != null) {
            EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
            if(enchantmentOptionsConfig == null) {
                logger.error(AdventureUtil.deserialize("Unable to create the output ItemStack due to invalid enchantment options settings."));
                createOutputButtonPlaceholder(buttonConfig.slot(), buttonConfig);
                return;
            }

            @Nullable ItemStack outputStack = createOutputItemStack();
            if(outputStack == null) {
                createOutputButtonPlaceholder(buttonConfig.slot(), buttonConfig);
                return;
            }
            this.outputItemStack = outputStack;

            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(outputStack);
            builder.setAction(inventoryClickEvent -> {
                @Nullable Settings settings = settingsManager.getConfiguration();
                if(settings == null) return;
                if(input1Slot == -1 || input1ItemStack == null) return;
                if(input2Slot == -1 || input2ItemStack == null) return;
                if(applicationCost == null) return;
                if(enchantmentData == null) return;

                // If the player does not have the required amounts to meet the costs, return
                if(!hasRequiredCosts(inventoryClickEvent)) return;

                // Remove the calculated costs from the player
                removeCosts();

                // Give the result item to the player
                PlayerUtil.giveItem(player.getInventory(), outputItemStack, outputItemStack.getAmount(), player.getLocation());

                // Clear any input data
                input1Slot = -1;
                input1ItemStack = null;
                input2Slot = -1;
                input2ItemStack = null;

                // Give any unadded enchantments if configured to do so
                returnUnappliedEnchantments(settings);

                // Update the GUI's buttons
                updateDynamicButtons();

                super.update();
            });

            // Set the result button
            setButton(buttonConfig.slot(), builder.build());
        } else {
            createOutputButtonPlaceholder(buttonConfig.slot(), buttonConfig);
        }
    }

    /**
     * Create the {@link ItemStack} to display in the GUI for the output button and to give to the player.
     * @apiNote Returns null if the enchantments can't be added or combined.
     * @return An {@link ItemStack} or null.
     */
    private @Nullable ItemStack createOutputItemStack() {
        if(input1ItemStack == null || input2ItemStack == null) {
            logger.error(AdventureUtil.deserialize("Unable to create output ItemStack for the enchanter GUI due to one input item being null."));
            return null;
        }
        ItemStack resultItemStack = input1ItemStack.clone();
        // Get the enchantments to add and return
        EnchantmentData enchantmentData = getEnchantmentData();
        // Return null if no enchantments to add
        if(enchantmentData.enchantmentsToAdd().isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to create output ItemStack for the enchanter GUI due to no enchantments to add."));
            return null;
        }

        if(resultItemStack.getItemMeta() instanceof EnchantmentStorageMeta resultEnchantmentStorage) {
            enchantmentData.enchantmentsToAdd().forEach((enchantment, level) -> {
                resultEnchantmentStorage.removeStoredEnchant(enchantment);

                resultEnchantmentStorage.addStoredEnchant(enchantment, level, false);

                resultItemStack.setItemMeta(resultEnchantmentStorage);
            });
        } else {
            enchantmentData.enchantmentsToAdd.forEach((enchantment, level) -> {
                resultItemStack.removeEnchantment(enchantment);

                resultItemStack.addEnchantment(enchantment, level);
            });
        }

        return resultItemStack;
    }

    /**
     * Calculate the enchantments to add and return.
     * @return The {@link EnchantmentData}.
     */
    private @NotNull EnchantmentData getEnchantmentData() {
        Map<Enchantment, Integer> enchantmentsToAdd = new HashMap<>();
        Map<Enchantment, Integer> enchantmentsToReturn = new HashMap<>();
        // Return the EnchantmentData with empty maps if either input stack is null
        if(input1ItemStack == null || input2ItemStack == null) return new EnchantmentData(enchantmentsToAdd, enchantmentsToReturn);

        ItemMeta input1ItemMeta = input1ItemStack.getItemMeta();
        ItemMeta input2ItemMeta = input2ItemStack.getItemMeta();

        if(input2ItemMeta instanceof EnchantmentStorageMeta input2EnchantmentStorage) {
            if(input1ItemMeta instanceof EnchantmentStorageMeta input1EnchantmentStorage) {
                for(Map.Entry<Enchantment, Integer> entry : input2EnchantmentStorage.getStoredEnchants().entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    int maxLevel = enchantment.getMaxLevel();

                    boolean hasEnchant = input1EnchantmentStorage.hasStoredEnchant(enchantment);
                    boolean isConflicting = input1EnchantmentStorage.hasConflictingStoredEnchant(enchantment);
                    int storedLevel = input1EnchantmentStorage.getStoredEnchantLevel(enchantment);

                    if(!hasEnchant && !isConflicting) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel < level) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel == level && level < maxLevel) {
                        enchantmentsToAdd.put(enchantment, level + 1);
                    } else {
                        enchantmentsToReturn.put(enchantment, level);
                    }
                }
            } else {
                for(Map.Entry<Enchantment, Integer> entry : input2EnchantmentStorage.getStoredEnchants().entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    int maxLevel = enchantment.getMaxLevel();

                    boolean hasEnchant = input1ItemMeta.hasEnchant(enchantment);
                    boolean isConflicting = input1ItemMeta.hasConflictingEnchant(enchantment);
                    int storedLevel = input1ItemMeta.getEnchantLevel(enchantment);

                    if(!hasEnchant && !isConflicting) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel < level) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel == level && level < maxLevel) {
                        enchantmentsToAdd.put(enchantment, level + 1);
                    } else {
                        enchantmentsToReturn.put(enchantment, level);
                    }
                }
            }
        } else {
            if(input1ItemMeta instanceof EnchantmentStorageMeta input1EnchantmentStorage) {
                for(Map.Entry<Enchantment, Integer> entry : input2ItemMeta.getEnchants().entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    int maxLevel = enchantment.getMaxLevel();

                    boolean hasEnchant = input1EnchantmentStorage.hasStoredEnchant(enchantment);
                    boolean isConflicting = input1EnchantmentStorage.hasConflictingStoredEnchant(enchantment);
                    int storedLevel = input1EnchantmentStorage.getStoredEnchantLevel(enchantment);

                    if(!hasEnchant && !isConflicting) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel < level) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel == level && level < maxLevel) {
                        enchantmentsToAdd.put(enchantment, level + 1);
                    } else {
                        enchantmentsToReturn.put(enchantment, level);
                    }
                }
            } else {
                for(Map.Entry<Enchantment, Integer> entry : input2ItemMeta.getEnchants().entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    int maxLevel = enchantment.getMaxLevel();

                    boolean hasEnchant = input1ItemMeta.hasEnchant(enchantment);
                    boolean isConflicting = input1ItemMeta.hasConflictingEnchant(enchantment);
                    int storedLevel = input1ItemMeta.getEnchantLevel(enchantment);

                    if(!hasEnchant && !isConflicting) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel < level) {
                        enchantmentsToAdd.put(enchantment, level);
                    } else if(hasEnchant && storedLevel == level && level < maxLevel) {
                        enchantmentsToAdd.put(enchantment, level + 1);
                    } else {
                        enchantmentsToReturn.put(enchantment, level);
                    }
                }
            }
        }

        return new EnchantmentData(enchantmentsToAdd, enchantmentsToReturn);
    }

    /**
     * Create the placeholder button for the output slot.
     * @param slot The slot to place the button.
     * @param buttonConfig The {@link ButtonConfig} for the output button.
     */
    private void createOutputButtonPlaceholder(int slot, @NotNull ButtonConfig buttonConfig) {
        if(inventoryView == null) return;

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, null, List.of());

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(itemStack);

            setButton(slot, builder.build());
        });
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(enchanterGUIConfig == null) return;

        enchanterGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the enchanter GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.item();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, List.of());
            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }

    /**
     * Calculate the costs required to apply the enchantments.
     * @return The {@link ApplicationCost} or null.
     */
    private @Nullable ApplicationCost calculateApplicationCosts() {
        @Nullable EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.error(AdventureUtil.deserialize("Unable to calculate application costs due to invalid enchantment options configuration."));
            return null;
        }

        if(enchantmentData == null) {
            logger.error(AdventureUtil.deserialize("Unable to calculate application costs due to invalid enchantment data."));
            return null;
        }

        double requiredMoney = 0.0;
        int requiredExpLevels = 0;
        int requiredPoints = 0;

        for(Map.Entry<Enchantment, Integer> entry : enchantmentData.enchantmentsToAdd().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            @Nullable ApplicationCost applicationCost = getApplicationCost(enchantmentOptionsConfig, enchantment, level);
            if(applicationCost == null) {
                logger.error(AdventureUtil.deserialize("Unable to calculate application costs due to missing enchantment configuration for " + enchantment.getKey() + " and level " + level));
                return null;
            }

            requiredMoney += applicationCost.money();
            requiredExpLevels += applicationCost.exp();
            requiredPoints += applicationCost.points();
        }

        return new ApplicationCost(requiredMoney, requiredExpLevels, requiredPoints);
    }

    /**
     * Get the {@link ApplicationCost} for the enchantment and enchantment level.
     * @param enchantmentOptionsConfig The plugin's {@link EnchantmentOptionsConfig}.
     * @param enchantment The {@link Enchantment}.
     * @param level The enchantment level.
     * @return The {@link ApplicationCost} or null.
     */
    private @Nullable ApplicationCost getApplicationCost(@NotNull EnchantmentOptionsConfig enchantmentOptionsConfig, @NotNull Enchantment enchantment, int level) {
        @Nullable EnchantmentOptions enchantmentOptions = enchantmentOptionsConfig.enchantmentOptions().get(enchantment.getKey().toString());
        if(enchantmentOptions == null) return null;

        return enchantmentOptions.costByLevel().get(level);
    }

    /**
     * Check if the player has the required money, exp levels, and points.
     * @apiNote Also cancels the {@link InventoryClickEvent} and sends any error messages to the player or console as necessary.
     * @param inventoryClickEvent The {@link InventoryClickEvent}.
     * @return true if the player has the required amounts, otherwise false.
     */
    private boolean hasRequiredCosts(@NotNull InventoryClickEvent inventoryClickEvent) {
        Locale locale = localeManager.getConfiguration();

        if(applicationCost == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.enchanterError()));
            inventoryClickEvent.setCancelled(true);
            return false;
        }

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Check if the player has the required money
        if(applicationCost.money() > 0) {
            if(economyHook.isHooked()) {
                if(economyHook.getBalance(player) < applicationCost.money()) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.insufficientFunds()));
                    inventoryClickEvent.setCancelled(true);
                    return false;
                }
            } else {
                logger.error(AdventureUtil.deserialize("Unable to remove the required money because the economy was not hooked into."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.enchanterError()));
                inventoryClickEvent.setCancelled(true);
                return false;
            }
        }

        // Check if the player has the required experience levels
        if(applicationCost.exp() > 0) {
            if(player.getLevel() < applicationCost.exp()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.insufficientExpLevels()));
                inventoryClickEvent.setCancelled(true);
                return false;
            }
        }

        // Check if the player has the required points
        if(applicationCost.points() > 0) {
            if(playerPointsHook.isHooked()) {
                if(playerPointsHook.getBalance(player) < applicationCost.points()) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.insufficientPoints()));
                    inventoryClickEvent.setCancelled(true);
                    return false;
                }
            } else {
                logger.error(AdventureUtil.deserialize("Unable to remove the required money because the PlayerPoints plugin was not hooked into."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.enchanterError()));
                inventoryClickEvent.setCancelled(true);
                return false;
            }
        }

        return true;
    }

    /**
     * Remove the required money, exp levels, and points from the player.
     */
    public void removeCosts() {
        if(applicationCost == null) return;
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        if(applicationCost.money() > 0 && economyHook.isHooked()) {
            economyHook.removeFromBalance(player, applicationCost.money());
        }

        if(applicationCost.exp() > 0) {
            player.setLevel(player.getLevel() - applicationCost.exp());
        }

        if(applicationCost.points() > 0 && playerPointsHook.isHooked()) {
            playerPointsHook.removeFromBalance(player, applicationCost.points());
        }
    }

    /**
     * Give the unadded enchantments back to the player if configured to do so and the map contains any enchantments.
     * @param settings The plugin's {@link Settings}.
     */
    private void returnUnappliedEnchantments(@NotNull Settings settings) {
        // If enchantment data is null, return
        if(enchantmentData == null) return;
        // If not configured to return unapplied enchantments, return
        if(!settings.giveEnchantedBookForUnappliedEnchantments()) return;
        // If the enchantments to return is empty, return
        if(enchantmentData.enchantmentsToReturn().isEmpty()) return;

        // Return the unapplied enchantments as either one book or multiple
        if(settings.giveUnappliedEnchantmentsAsOneBook()) {
            // Create the Enchanted Book ItemStack
            ItemStack returnStack = ItemType.ENCHANTED_BOOK.createItemStack();
            // Get the EnchantmentStorageMeta
            if(!(returnStack.getItemMeta() instanceof EnchantmentStorageMeta returnItemEnchantmentMeta)) return;

            // Add the enchantments to the enchanted book
            enchantmentData.enchantmentsToReturn().forEach((enchantment, level) ->
                    returnItemEnchantmentMeta.addStoredEnchant(enchantment, level, false));

            // Set the item meta of the ItemStack
            returnStack.setItemMeta(returnItemEnchantmentMeta);

            // Give the player the ItemStack
            PlayerUtil.giveItem(player.getInventory(), returnStack, returnStack.getAmount(), player.getLocation());
        } else {
            enchantmentData.enchantmentsToReturn().forEach((enchantment, level) -> {
                // Create the Enchanted Book ItemStack
                ItemStack returnStack = ItemType.ENCHANTED_BOOK.createItemStack();
                // Get the EnchantmentStorageMeta
                if(!(returnStack.getItemMeta() instanceof EnchantmentStorageMeta returnEnchantmentStorageMeta)) return;

                // Add the enchantment enchanted book
                returnEnchantmentStorageMeta.addStoredEnchant(enchantment, level, false);

                // Set the item meta of the ItemStack
                returnStack.setItemMeta(returnEnchantmentStorageMeta);

                // Give the player the ItemStack
                PlayerUtil.giveItem(player.getInventory(), returnStack, returnStack.getAmount(), player.getLocation());
            });
        }
    }

    /**
     * This record stores the enchantments to add and return.
     * @param enchantmentsToAdd The {@link Map} mapping {@link Enchantment}s to {@link Integer}s (levels) to add.
     * @param enchantmentsToReturn The {@link Map} mapping {@link Enchantment}s to {@link Integer}s (levels) to return.
     */
    private record EnchantmentData(
            @NotNull Map<Enchantment, Integer> enchantmentsToAdd,
            @NotNull Map<Enchantment, Integer> enchantmentsToReturn) {}
}