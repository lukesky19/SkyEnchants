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
package com.github.lukesky19.skyEnchants.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * This class contains methods to compare {@link BlockType}s.
 */
public class BlockTypeUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public BlockTypeUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Blocks that may be the logs of the tree.
     */
    private static final Set<BlockType> LOG_BLOCKS = Set.of(
            BlockType.OAK_LOG, BlockType.BIRCH_LOG, BlockType.SPRUCE_LOG,
            BlockType.JUNGLE_LOG, BlockType.ACACIA_LOG, BlockType.DARK_OAK_LOG,
            BlockType.CHERRY_LOG, BlockType.MANGROVE_LOG, BlockType.PALE_OAK_LOG,
            BlockType.CRIMSON_STEM, BlockType.WARPED_STEM,
            BlockType.OAK_WOOD, BlockType.BIRCH_WOOD, BlockType.SPRUCE_WOOD,
            BlockType.JUNGLE_WOOD, BlockType.ACACIA_WOOD, BlockType.DARK_OAK_WOOD,
            BlockType.CHERRY_WOOD, BlockType.MANGROVE_WOOD, BlockType.PALE_OAK_WOOD,
            BlockType.CRIMSON_HYPHAE, BlockType.WARPED_HYPHAE
    );

    /**
     * Leaf blocks surrounding trees.
     */
    private static final Set<BlockType> LEAF_BLOCKS = Set.of(
            BlockType.OAK_LEAVES, BlockType.BIRCH_LEAVES, BlockType.SPRUCE_LEAVES,
            BlockType.JUNGLE_LEAVES, BlockType.ACACIA_LEAVES, BlockType.DARK_OAK_LEAVES,
            BlockType.CHERRY_LEAVES, BlockType.MANGROVE_LEAVES, BlockType.PALE_OAK_LEAVES,
            BlockType.NETHER_WART_BLOCK, BlockType.WARPED_WART_BLOCK
    );

    /**
     * Extra foliage blocks that may be attached to trees.
     */
    private static final Set<BlockType> FOLIAGE_BLOCKS = Set.of(
            BlockType.VINE, BlockType.TWISTING_VINES, BlockType.SHROOMLIGHT, BlockType.MANGROVE_PROPAGULE
    );

    /**
     * Is the {@link Block}'s {@link BlockType} that of a Log or Wood block?
     * @param block The {@link Block} to check.
     * @return true or false.
     */
    public static boolean isLogOrWoodBlock(@NotNull Block block) {
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return false;

        return LOG_BLOCKS.contains(blockType);
    }

    /**
     * Is the {@link Block}'s {@link BlockType} that of a Leaf or Wart bock?
     * @param block The {@link Block} to check.
     * @return true or false.
     */
    public static boolean isLeafOrWartBlock(@NotNull Block block) {
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return false;

        return LEAF_BLOCKS.contains(blockType);
    }

    /**
     * Is the {@link Block}'s {@link BlockType} that of a foliage attached to a tree?
     * @param block The {@link Block} to check.
     * @return true or false.
     */
    public static boolean isFoliageBlock(@NonNull Block block) {
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return false;

        return FOLIAGE_BLOCKS.contains(blockType);
    }
}
