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
import com.github.lukesky19.skyEnchants.gui.PreviewGUI;
import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class creates the command to open the preview/info GUI.
 */
public class InfoCommand {
    private final @NonNull SkyEnchants skyEnchants;
    private final @NonNull UUIDGUIManager guiManager;
    private final @NonNull GUIConfigManager guiConfigManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     */
    public InfoCommand(
            @NonNull SkyEnchants skyEnchants,
            @NonNull UUIDGUIManager guiManager,
            @NonNull GUIConfigManager guiConfigManager) {
        this.skyEnchants = skyEnchants;
        this.guiManager = guiManager;
        this.guiConfigManager = guiConfigManager;
    }

    /**
     * Creates the gui command to open the preview/info GUI.
     * @param commandName The name of the command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand(@NonNull String commandName) {
        return Commands.literal(commandName)
                .requires(ctx -> ctx.getSender().hasPermission("skyenchants.commands.skyenchants.gui." + commandName))
                .executes(ctx -> {
                    PreviewGUI previewGUI = new PreviewGUI(skyEnchants, guiManager, (Player) ctx.getSource().getSender(), guiConfigManager);

                    boolean creationResult = previewGUI.create();
                    if(!creationResult) return 0;

                    boolean updateResult = previewGUI.update();
                    if(!updateResult) return 0;

                    boolean openResult = previewGUI.open();
                    if(!openResult) return 0;

                    return 1;
                })
                .build();
    }
}