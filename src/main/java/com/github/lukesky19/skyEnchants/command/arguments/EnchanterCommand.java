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
package com.github.lukesky19.skyEnchants.command.arguments;

import com.github.lukesky19.skyEnchants.SkyEnchants;
import com.github.lukesky19.skyEnchants.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyEnchants.gui.EnchanterGUI;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class creates the command to open the enchanter GUI.
 */
public class EnchanterCommand {
    private final @NonNull SkyEnchants skyEnchants;
    private final @NonNull UUIDGUIManager guiManager;
    private final @NonNull SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull EnchantmentOptionsConfigManager anvilConfigManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param anvilConfigManager An {@link EnchantmentOptionsConfigManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public EnchanterCommand(
            @NonNull SkyEnchants skyEnchants,
            @NonNull UUIDGUIManager guiManager,
            @NonNull SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull EnchantmentOptionsConfigManager anvilConfigManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull HookManager hookManager) {
        this.skyEnchants = skyEnchants;
        this.guiManager = guiManager;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.anvilConfigManager = anvilConfigManager;
        this.guiConfigManager = guiConfigManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the gui command to open the enchanter GUI.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("enchanter")
                .requires(ctx -> ctx.getSender().hasPermission("skyenchants.commands.skyenchants.gui.enchanter"))
                .executes(ctx -> {
                    EnchanterGUI enchanterGUI = new EnchanterGUI(skyEnchants, guiManager, (Player) ctx.getSource().getSender(), settingsManager, localeManager, anvilConfigManager, guiConfigManager, hookManager);

                    boolean creationResult = enchanterGUI.create();
                    if(!creationResult) return 0;

                    boolean updateResult = enchanterGUI.update();
                    if(!updateResult) return 0;

                    boolean openResult = enchanterGUI.open();
                    if(!openResult) return 0;

                    return 1;
                })
                .build();
    }
}