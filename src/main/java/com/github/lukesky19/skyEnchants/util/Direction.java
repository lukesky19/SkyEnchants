package com.github.lukesky19.skyEnchants.util;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

/**
 * This enum is used to identify the direction a player is looking.
 */
public enum Direction {
    /**
     * This direction is for facing north, which is in the negative z direction.
     */
    NORTH(0,0,-1),
    /**
     * This direction is for facing south, which is in the positive z direction.
     */
    SOUTH(0,0,1),
    /**
     * This direction is for facing east, which is in the positive x direction.
     */
    EAST(1,0,0),
    /**
     * This direction is for facing west, which is in the negative x direction.
     */
    WEST(-1,0,0),
    /**
     * This direction is for facing north-east, which is in the negative z and positive x direction.
     */
    NORTH_EAST(1, 0, -1),
    /**
     * This direction is for facing north-west, which is in the negative x and negative z direction.
     */
    NORTH_WEST(-1, 0, -1),
    /**
     * This direction is for facing south-east, which is in the positive x and positive z direction.
     */
    SOUTH_EAST(1, 0, 1),
    /**
     * This direction is for facing south-west, which is in the negative x and positive z direction.
     */
    SOUTH_WEST(-1, 0, 1),
    /**
     * This direction is for facing up, which is in the positive y direction.
     */
    UP(0,1,0),
    /**
     * This direction is for facing down, which is in the negative y direction.
     */
    DOWN(0,-1,0);

    /**
     * The {@link Vector} for the direction.
     */
    public final Vector vector;
    /**
     * If the direction is in the x direction
     */
    public final boolean isX;
    /**
     * If the direction is in the y direction
     */
    public final boolean isY;
    /**
     * If the direction is in the z direction
     */
    public final boolean isZ;

    /**
     * Constructor
     * @param x The x offset.
     * @param y The y offset.
     * @param z The z offset.
     */
    Direction(int x, int y, int z) {
        this.vector = new Vector(x, y, z);
        this.isX = x != 0;
        this.isY = y != 0;
        this.isZ = z != 0;
    }

    /**
     * Get the primary {@link Direction} the player is facing.
     * The direction is determined by the player's vector for the direction they are facing.<br>
     * If the Y or UP/DOWN direction is strong (> 0.5), UP or DOWN is returned.<br>
     * Then, if the X or EAST/WEST direction is strong (> 0.5), EAST or WEST is returned.<br>
     * Lastly, the Z or NORTH/SOUTH direction is strong, so NORTH or SOUTH is returned.<br>
     * Positive Y = UP, Negative Y = DOWN<br>
     * Positive X = EAST, Negative X = WEST<br>
     * Positive Z = SOUTH, Negative Z = NORTH
     * @param player The {@link Player} to get the direction they are facing for.
     * @return The {@link Direction} the player is facing.
     */
    public static @NonNull Direction getDirectionFacing(@NonNull Player player) {
        // Get the vector the player is facing.
        Vector playerDirectionVector = player.getLocation().getDirection().normalize();
        double vectorX = playerDirectionVector.getX();
        double vectorY = playerDirectionVector.getY();
        double vectorZ = playerDirectionVector.getZ();

        // Calculate based on the vector's X, Y, and Z which direction they are looking.
        if(Math.abs(vectorY) > 0.5) {
            return (vectorY > 0.0) ? Direction.UP : Direction.DOWN;
        } else if (Math.abs(vectorX) > 0.5) {
            return (vectorX > 0.0) ? Direction.EAST : Direction.WEST;
        } else {
            return (vectorZ > 0.0) ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
