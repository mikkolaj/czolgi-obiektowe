package agh.cs.czolgi;

public enum Powerups {
    PLAYER_MOVES,
    BULLET_RANGE,
    PIERCING_BULLETS,
    BOUNCING_BULLETS,
    IMMORTALITY,
    HEALTH_BOOST;

    private final static int PLAYER_MOVES_COUNT = 2;
    private final static int BULLET_RANGE_PER_ROUND = 2;
    private final static int HEALTH_BOOST_AMOUNT = 1;
    private final static int PIERCING_BULLETS_DAMAGE = 2;

    public static int getPlayerMoves() {
        return PLAYER_MOVES_COUNT;
    }

    public static int getBulletRange() {
        return BULLET_RANGE_PER_ROUND;
    }

    public static int getHealthBoost() {
        return HEALTH_BOOST_AMOUNT;
    }

    public static int getPiercingBulletsDamage() {
        return PIERCING_BULLETS_DAMAGE;
    }

    public int toInt() {
        return switch (this) {
            case PLAYER_MOVES -> 0;
            case BULLET_RANGE -> 1;
            case PIERCING_BULLETS -> 2;
            case BOUNCING_BULLETS -> 3;
            case IMMORTALITY -> 4;
            case HEALTH_BOOST -> 5;
        };
    }


    public int getDuration() {
        return switch (this) {
            case PLAYER_MOVES, IMMORTALITY -> 2;
            case BULLET_RANGE -> 3;
            case PIERCING_BULLETS, BOUNCING_BULLETS -> 1;
            case HEALTH_BOOST -> Integer.MAX_VALUE;
        };
    }

    public static Powerups toEnum(int number) {
        return switch (number) {
            case 0 -> PLAYER_MOVES ;
            case 1 -> BULLET_RANGE;
            case 2 -> PIERCING_BULLETS ;
            case 3 -> BOUNCING_BULLETS;
            case 4 -> IMMORTALITY;
            case 5 -> HEALTH_BOOST;
            default ->
                    throw new IllegalArgumentException("No powerup corresponds to: " + number);
        };

    }
}
