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
package com.github.lukesky19.skyEnchants.integration.hooks;

import com.github.lukesky19.skyEnchants.integration.Hook;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages interfacing with RoseStacker.
 */
public class RoseStackerHook implements Hook {
    private final @NotNull SkyPlugin plugin;
    private @Nullable RoseStackerAPI roseStackerAPI;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public RoseStackerHook(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link RoseStackerAPI} from RoseStacker.
     */
    @Override
    public void initialize() {
        @Nullable Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("RoseStacker");
        if(plugin != null && plugin.isEnabled()) {
            roseStackerAPI = RoseStackerAPI.getInstance();
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return roseStackerAPI != null;
    }

    /**
     * Is the block a stacked block or spawner?
     * @apiNote Returns false if RoseStacker isn't hooked into.
     * @param block The {@link Block}.
     * @return true if stacked, false if not.
     */
    public boolean isStackedBlock(@NotNull Block block) {
        if(roseStackerAPI == null) return false;

        return roseStackerAPI.isBlockStacked(block) || roseStackerAPI.isSpawnerStacked(block);
    }
}