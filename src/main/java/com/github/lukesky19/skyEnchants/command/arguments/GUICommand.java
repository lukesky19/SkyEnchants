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
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.gui.EnchanterGUI;
import com.github.lukesky19.skyEnchants.gui.PreviewGUI;
import com.github.lukesky19.skyEnchants.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyEnchants.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyEnchants.manager.hook.HookManager;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class creates the gui command to open the enchanter and preview GUIs.
 */
public class GUICommand {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull UUIDGUIManager guiManager;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull EnchantmentOptionsConfigManager anvilConfigManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull HookManager hookManager;

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
    public GUICommand(
            @NotNull SkyEnchants skyEnchants,
            @NotNull UUIDGUIManager guiManager,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull EnchantmentOptionsConfigManager anvilConfigManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HookManager hookManager) {
        this.skyEnchants = skyEnchants;
        this.guiManager = guiManager;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.anvilConfigManager = anvilConfigManager;
        this.guiConfigManager = guiConfigManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the gui command to open the enchanter and preview GUIs.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("gui")
                .requires(ctx -> ctx.getSender().hasPermission("skyenchants.commands.skyenchants.gui") && ctx.getSender() instanceof Player)
                .then(Commands.literal("enchanter")
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
                    }))
                .then(Commands.literal("preview")
                        .executes(ctx -> {
                            PreviewGUI previewGUI = new PreviewGUI(skyEnchants, guiManager, (Player) ctx.getSource().getSender(), guiConfigManager);

                            boolean creationResult = previewGUI.create();
                            if(!creationResult) return 0;

                            boolean updateResult = previewGUI.update();
                            if(!updateResult) return 0;

                            boolean openResult = previewGUI.open();
                            if(!openResult) return 0;

                            return 1;
                        }))
                .build();
    }
}
