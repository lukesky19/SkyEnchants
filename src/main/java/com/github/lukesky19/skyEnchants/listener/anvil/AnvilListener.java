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
package com.github.lukesky19.skyEnchants.listener.anvil;

import com.github.lukesky19.skyEnchants.config.data.options.EnchantmentOptionsConfig;
import com.github.lukesky19.skyEnchants.config.data.locale.Locale;
import com.github.lukesky19.skyEnchants.config.data.misc.EnchantmentOptions;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Listens to when an enchantment is being applied through an anvil.
 * If the enchantment is disabled in anvils, the event is cancelled and tells the player to use the custom enchanter GUI instead.
 */
public class AnvilListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull EnchantmentOptionsConfigManager anvilConfigManager;
    private final @NotNull LocaleManager localeManager;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger}
     * @param anvilConfigManager A {@link EnchantmentOptionsConfigManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public AnvilListener(
            @NotNull ComponentLogger logger,
            @NotNull EnchantmentOptionsConfigManager anvilConfigManager,
            @NotNull LocaleManager localeManager) {
        this.logger = logger;
        this.anvilConfigManager = anvilConfigManager;
        this.localeManager = localeManager;
    }

    /**
     * Listens to when an enchantment is being applied through an anvil.
     * If it is a custom enchantment as an enchanted book, and the custom enchant isn't allowed in anvils, the event is cancelled.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        if(!(clickedInventory instanceof AnvilInventory  anvilInventory)) return;
        if(inventoryClickEvent.getSlot() != 2) return;

        @Nullable EnchantmentOptionsConfig enchantmentOptionsConfig = anvilConfigManager.getConfiguration();
        if(enchantmentOptionsConfig == null) {
            logger.error("<red>Unable to prevent custom enchantment use in anvils due to invalid anvil config.</red>");
            return;
        }
        Locale locale = localeManager.getConfiguration();

        @Nullable ItemStack inputItem = anvilInventory.getItem(1);
        if(inputItem == null || inputItem.isEmpty()) return;
        @Nullable ItemType inputItemType = inputItem.getType().asItemType();
        if(inputItemType == null) return;
        if(!(inputItem.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorageMeta)) return;
        @NotNull Map<Enchantment, Integer> inputItemEnchantments = enchantmentStorageMeta.getStoredEnchants();

        for(Enchantment enchantment : inputItemEnchantments.keySet()) {
            @Nullable EnchantmentOptions enchantmentOptions = enchantmentOptionsConfig.enchantmentOptions().get(enchantment.getKey().toString());
            if(enchantmentOptions == null) {
                logger.warn(AdventureUtil.deserialize("No anvil configuration found for NamespacedKey: " + enchantment.getKey()));
                continue;
            }

            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("enchantments", FormatUtil.formatKey(enchantment.getKey())));
            if(enchantmentOptions.disableAnvilUse()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.anvilUseNotAllowed(), placeholders));
                inventoryClickEvent.setCancelled(true);
                return;
            }
        }
    }
}
