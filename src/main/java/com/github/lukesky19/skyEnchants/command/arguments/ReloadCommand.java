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
import com.github.lukesky19.skyEnchants.config.data.locale.Locale;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class creates the reload command to reload the plugin.
 */
public class ReloadCommand {
    private final @NotNull SkyEnchants skyEnchants;
    private final @NotNull LocaleManager localeManager;

    /**
     * Constructor
     * @param skyEnchants A {@link SkyEnchants} instance.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public ReloadCommand(@NotNull SkyEnchants skyEnchants, @NotNull LocaleManager localeManager) {
        this.skyEnchants = skyEnchants;
        this.localeManager = localeManager;
    }

    /**
     * Creates the reload command to reload the plugin.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skyenchants.commands.skyenchants.reload"))
            .executes(ctx -> {
                Locale locale = localeManager.getConfiguration();
                CommandSender sender = ctx.getSource().getSender();

                skyEnchants.reload();

                if(sender instanceof Player) {
                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.reload()));
                } else {
                    sender.sendMessage(AdventureUtil.deserialize(locale.reload()));
                }

                return 1;
            })
            .build();
    }
}
