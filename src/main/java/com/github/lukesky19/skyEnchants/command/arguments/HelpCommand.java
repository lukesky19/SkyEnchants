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

import com.github.lukesky19.skyEnchants.config.data.locale.Locale;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.adventure.PaperAdventureUtility;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class creates the help command argument for the skyenchants command.
 */
public class HelpCommand {
    private final @NonNull LocaleManager localeManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     */
    public HelpCommand(@NonNull LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument for the /skyenchants command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument for the /skyenchants command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("help")
                .requires(ctx -> ctx.getSender().hasPermission("skyenchants.commands.skyenchants.help"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Locale locale = localeManager.getConfiguration();

                    if(sender instanceof Player player) {
                        for(String msg : locale.help()) {
                            player.sendMessage(PaperAdventureUtility.deserialize(player, msg));
                        }
                    } else {
                        for(String msg : locale.help()) {
                            sender.sendMessage(AdventureUtility.deserialize(msg));
                        }
                    }

                    return 1;
                }).build();
    }
}
