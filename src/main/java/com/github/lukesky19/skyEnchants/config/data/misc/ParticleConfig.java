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
package com.github.lukesky19.skyEnchants.config.data.misc;

import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * This record contains the configuration to create and spawn a {@link org.bukkit.Particle}.
 * @param particleType The particle type name.
 * @param count The particle count.
 * @param offsetX The offset for the x direction.
 * @param offsetY The offset for the y direction.
 * @param offsetZ The offset for the z direction.
 * @param extra Extra data for the particle, normally speed. See {@link World#spawnParticle(org.bukkit.Particle, Location, int, double, double, double, double)}
 */
@ConfigSerializable
public record ParticleConfig(
        @Nullable String particleType,
        int count,
        double offsetX,
        double offsetY,
        double offsetZ,
        double extra) {}
