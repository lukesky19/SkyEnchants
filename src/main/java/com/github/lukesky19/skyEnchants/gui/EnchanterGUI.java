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
import com.github.lukesky19.skyEnchants.config.data.settings.Settings;
import com.github.lukesky19.skyEnchants.config.data.gui.EnchanterGUIConfig;
import com.github.lukesky19.skyEnchants.config.data.gui.ButtonConfig;
import com.github.lukesky19.skyEnchants.config.data.misc.ApplicationCost;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.data.EnchantmentData;
import com.github.lukesky19.skyEnchants.integration.hooks.EconomyHook;
import com.github.lukesky19.skyEnchants.integration.hooks.PlayerPointsHook;
import com.github.lukesky19.skyEnchants.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyEnchants.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.paper.api.player.PlayerUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.github.lukesky19.skyEnchants.util.EnchantmentUtils.*;
import static com.github.lukesky19.skyEnchants.util.PriceUtils.*;

/**
 * This class creates the GUI to access different shop categories.
 */
public class EnchanterGUI extends ChestGUI<UUID> {
    private final @NonNull SkyEnchants skyEnchants;
    private final @NonNull SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager;
    private final @NonNull HookManager hookManager;
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
    private @Nullable ApplicationCost costs;
    // This contains the penalties to enchant the item.
    private @Nullable ApplicationCost penalties;
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
            @NonNull SkyEnchants skyEnchants,
            @NonNull UUIDGUIManager guiManager,
            @NonNull Player player,
            @NonNull SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull EnchantmentOptionsConfigManager enchantmentOptionsConfigManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull HookManager hookManager) {
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
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the enchanter GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = enchanterGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the enchanter GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54 -> {}

            default -> {
                logger.error(AdventureUtility.plain("Unsupported GUI Type in enchanter GUI config. Allowed Types: CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
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
            logger.warn(AdventureUtility.plain("Unable to add buttons to the enchanter GUI due to invalid gui configuration."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the enchanter GUI as the InventoryView was not created."));
            return false;
        }

        createFillerButtons(inventoryView.getTopInventory().getSize());
        createDummyButtons();

        this.enchantmentData = getEnchantmentData(logger, enchantmentOptionsConfigManager, input1ItemStack, input2ItemStack);
        this.costs = calculateCosts(logger, enchantmentOptionsConfigManager, enchantmentData);
        this.penalties = calculatePenalties(logger, enchantmentOptionsConfigManager, enchantmentData);
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
            logger.warn(AdventureUtility.plain("Unable to update the dynamic buttons for the enchanter GUI due to invalid gui configuration."));
            return;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to update the dynamic buttons for the enchanter GUI as the InventoryView was not created."));
            return;
        }

        this.enchantmentData = getEnchantmentData(logger, enchantmentOptionsConfigManager, input1ItemStack, input2ItemStack);
        this.costs = calculateCosts(logger, enchantmentOptionsConfigManager, enchantmentData);
        this.penalties = calculatePenalties(logger, enchantmentOptionsConfigManager, enchantmentData);
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
    public void handleClose(@NonNull InventoryCloseEvent inventoryCloseEvent) {
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
    public void handleBottomDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the entire inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NonNull InventoryClickEvent inventoryClickEvent) {
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
            ItemStack clickedItemStack = player.getInventory().getItem(clickedSlot);
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
    public void handleGlobalClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create the filler buttons for the GUI.
     * @param guiSize The size of the GUI.
     */
    private void createFillerButtons(int guiSize) {
        if(enchanterGUIConfig == null) return;

        ItemStackConfig fillerConfig = enchanterGUIConfig.filler();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyEnchants.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(fillerConfig, player, List.of());

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
        EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to add a the costs button to the enchanter GUI due to an invalid enchantment options config."));
            return;
        }

        ButtonConfig buttonConfig = enchanterGUIConfig.costButton();
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a the costs button to the enchanter GUI due to an invalid slot."));
            return;
        }

        // Get the required money, exp levels, and player points
        double requiredMoney = 0.0;
        int requiredExpLevels = 0;
        int requiredPoints = 0;
        if(costs != null) {
            requiredMoney += costs.money();
            requiredExpLevels += costs.exp();
            requiredPoints += costs.points();
        }

        if(penalties != null) {
            requiredMoney += penalties.money();
            requiredExpLevels += penalties.exp();
            requiredPoints += penalties.points();
        }

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("required_money", String.valueOf(requiredMoney)),
                Placeholder.parsed("required_levels", String.valueOf(requiredExpLevels)),
                Placeholder.parsed("required_points", String.valueOf(requiredPoints)));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, placeholders);

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
            logger.warn(AdventureUtility.plain("Unable to add a the player info button to the enchanter GUI due to an invalid slot."));
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
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, placeholders);

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
            logger.warn(AdventureUtility.plain("Unable to add the first input item button to the enchanter GUI due to an invalid slot."));
            return;
        }

        if(input1ItemStack != null && input1Slot != -1) {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(input1ItemStack);
            builder.setAction(_ -> {
                if(input1Slot == -1 || input1ItemStack == null) return;

                player.getInventory().setItem(input1Slot, input1ItemStack);

                input1ItemStack = null;
                input1Slot = -1;

                updateDynamicButtons();

                super.update();
            });

            setButton(buttonConfig.slot(), builder.build());
        } else {
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, List.of());
            Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isEmpty()) return;

            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(optionalItemStack.get());
            setButton(buttonConfig.slot(), builder.build());
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
            logger.warn(AdventureUtility.plain("Unable to add a the input enchantment button to the enchanter GUI due to an invalid slot."));
            return;
        }

        if(input2ItemStack != null && input2Slot != -1) {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(input2ItemStack);
            builder.setAction(_ -> {
                if(input2Slot == -1 || input2ItemStack == null) return;

                player.getInventory().setItem(input2Slot, input2ItemStack);

                input2ItemStack = null;
                input2Slot = -1;

                updateDynamicButtons();

                super.update();
            });

            setButton(buttonConfig.slot(), builder.build());
        } else {
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, List.of());
            Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isEmpty()) return;

            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(optionalItemStack.get());
            setButton(buttonConfig.slot(), builder.build());
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
            logger.warn(AdventureUtility.plain("Unable to add the output button to the enchanter GUI due to an invalid slot."));
            return;
        }

        if(input1ItemStack == null || input2ItemStack == null) {
            createOutputButtonPlaceholder(buttonConfig.slot(), buttonConfig);
            return;
        }

        EnchantmentOptionsConfig enchantmentOptionsConfig = enchantmentOptionsConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.error(AdventureUtility.plain("Unable to create the output ItemStack due to invalid enchantment options settings."));
            createOutputButtonPlaceholder(buttonConfig.slot(), buttonConfig);
            return;
        }

        this.outputItemStack = createOutputItemStack();
        if(outputItemStack == null) {
            createOutputButtonPlaceholder(buttonConfig.slot(), buttonConfig);
            return;
        }

        GUIButton.Builder builder = new GUIButton.Builder();
        builder.setItemStack(outputItemStack);
        builder.setAction(inventoryClickEvent -> {
            Settings settings = settingsManager.getConfiguration();
            if(settings == null) return;
            if(enchantmentData == null) return;

            // If the player does not have the required amounts, return
            if(!hasRequiredAmounts(logger, localeManager, hookManager, player, costs, penalties, inventoryClickEvent)) return;

            // Remove the calculated amounts from the player
            removeRequiredAmounts(hookManager, player, costs, penalties);
            
            // Clear costs and penalties
            costs = null;
            penalties = null;

            // Give the result item to the player
            PlayerUtil.giveItem(player.getInventory(), outputItemStack, outputItemStack.getAmount(), player.getLocation());

            // Clear any input data
            input1Slot = -1;
            input1ItemStack = null;
            input2Slot = -1;
            input2ItemStack = null;

            // Give any unadded enchantments if configured to do so
            returnUnappliedEnchantments(settings, player, enchantmentData);
            
            // Clear EnchantmentData
            enchantmentData = null;

            // Update the GUI's buttons
            updateDynamicButtons();

            super.update();
        });

        // Set the result button
        setButton(buttonConfig.slot(), builder.build());
    }

    /**
     * Create the {@link ItemStack} to display in the GUI for the output button and to give to the player.
     * @apiNote Returns null if the enchantments can't be added or combined.
     * @return An {@link ItemStack} or null.
     */
    private @Nullable ItemStack createOutputItemStack() {
        if(input1ItemStack == null || input2ItemStack == null) {
            logger.error(AdventureUtility.plain("Unable to create output ItemStack for the enchanter GUI due to one input item being null."));
            return null;
        }
        ItemStack resultItemStack = input1ItemStack.clone();
        // Get the enchantments to add and return
        EnchantmentData enchantmentData = getEnchantmentData(logger, enchantmentOptionsConfigManager, input1ItemStack, input2ItemStack);
        // Return null if no enchantments to add
        if(enchantmentData.enchantmentsToAdd().isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to create output ItemStack for the enchanter GUI due to no enchantments to add."));
            return null;
        }

        if(resultItemStack.getItemMeta() instanceof EnchantmentStorageMeta resultEnchantmentStorage) {
            enchantmentData.enchantmentConflicts().forEach((enchantment, _) ->
                    resultEnchantmentStorage.removeStoredEnchant(enchantment));

            enchantmentData.enchantmentsToAdd().forEach((enchantment, level) -> {
                resultEnchantmentStorage.removeStoredEnchant(enchantment);

                resultEnchantmentStorage.addStoredEnchant(enchantment, level, false);
            });

            resultItemStack.setItemMeta(resultEnchantmentStorage);
        } else {
            enchantmentData.enchantmentConflicts().forEach((enchantment, _) ->
                    resultItemStack.removeEnchantment(enchantment));

            enchantmentData.enchantmentsToAdd().forEach((enchantment, level) -> {
                resultItemStack.removeEnchantment(enchantment);

                resultItemStack.addEnchantment(enchantment, level);
            });
        }

        return resultItemStack;
    }

    /**
     * Create the placeholder button for the output slot.
     * @param slot The slot to place the button.
     * @param buttonConfig The {@link ButtonConfig} for the output button.
     */
    private void createOutputButtonPlaceholder(int slot, @NonNull ButtonConfig buttonConfig) {
        if(inventoryView == null) return;

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), player, List.of());

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the enchanter GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.item();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());
            Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }
}