package agh.cs.czolgi;

public enum MapDirection {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST;

    public String toString() {
        return switch (this) {
            case NORTH -> "^";
            case NORTH_EAST -> "/^";
            case EAST -> ">";
            case SOUTH_EAST -> "\\v";
            case SOUTH -> "v";
            case SOUTH_WEST -> "v/";
            case WEST -> "<";
            case NORTH_WEST -> "^\\";
        };
    }

    public MapDirection opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case NORTH_EAST -> SOUTH_WEST;
            case EAST -> WEST;
            case SOUTH_EAST -> NORTH_WEST;
            case SOUTH -> NORTH;
            case SOUTH_WEST -> NORTH_EAST;
            case WEST -> EAST;
            case NORTH_WEST -> SOUTH_EAST;
        };
    }

    public static MapDirection parseDirection(int directionInt) {
        return switch (directionInt) {
            case 0 -> EAST;
            case 1 -> NORTH_EAST;
            case 2 -> NORTH;
            case 3 -> NORTH_WEST;
            case 4 -> WEST;
            case 5 -> SOUTH_WEST;
            case 6 -> SOUTH;
            case 7 -> SOUTH_EAST;
            default -> throw new IllegalStateException("No direction corrensponds to: " + directionInt);
        };
    }

    public Vector2d toUnitVector() {
        return switch (this) {
            case NORTH -> new Vector2d(0, 1);
            case NORTH_EAST -> new Vector2d(1, 1);
            case EAST -> new Vector2d(1, 0);
            case SOUTH_EAST -> new Vector2d(1, -1);
            case SOUTH -> new Vector2d(0, -1);
            case SOUTH_WEST -> new Vector2d(-1, -1);
            case WEST -> new Vector2d(-1, 0);
            case NORTH_WEST -> new Vector2d(-1, 1);
        };
    }
}
