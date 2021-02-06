package agh.cs.czolgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Tank implements IKillableEntity {
    // enable it to make everyone immortal and grant them unlimited powerups
    private final static boolean CHAOS_MODE = false;

    private final static int START_HEALTH = 1;
    private int currentHealth = START_HEALTH;
    private Vector2d position;
    private final ArrayList<IPositionChangeObserver> observers = new ArrayList<>();
    private final BattleField map;
    private final boolean isPlayer;
    private final int[] powerups = new int[6];
    private final int[] activePowerupsDurations = new int[6];
    private final static Random generator = new Random();

    public Tank(BattleField map, Vector2d initialPosition, boolean isPlayer, boolean drawPowerups) {
        this.position = initialPosition;
        this.map = map;
        this.map.fieldChanged(initialPosition);
        this.isPlayer = isPlayer;

        if (CHAOS_MODE) {
            Arrays.fill(this.activePowerupsDurations, Integer.MAX_VALUE);
        } else if (drawPowerups) {
            this.drawPowerups();
        }
    }

    private void drawPowerups() {
        for (int i = 0; i < this.activePowerupsDurations.length; i++) {
            Powerups powerup = Powerups.toEnum(i);
            if (generator.nextInt(2) == 1 && powerup != Powerups.IMMORTALITY) {
                this.activePowerupsDurations[i] = powerup.getDuration();
            }
        }
    }


    public void move(MapDirection direction) {
        Vector2d newPos = this.position.add(direction.toUnitVector());
        if (this.map.canMoveTo(newPos)) {
            Vector2d oldPos = this.position;
            this.position = newPos;
            this.positionChanged(oldPos, newPos);
        }
    }

    public Vector2d getPosition() {
        return this.position;
    }

    public void addObserver(IPositionChangeObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(IPositionChangeObserver observer) {
        this.observers.remove(observer);
    }

    private void positionChanged(Vector2d oldPosition, Vector2d newPosition) {
        for (IPositionChangeObserver observer : this.observers) {
            observer.tankPositionChanged(oldPosition, newPosition);
        }
    }

    public boolean isDead() {
        return this.currentHealth <= 0;
    }

    public int getHealth() {
        return this.currentHealth;
    }

    public void receiveDamage(int damage) {
        if (damage >= 0) {
            this.currentHealth -= damage;
        }
    }

    public int getMoveCount() {
        if (this.activePowerupsDurations[Powerups.PLAYER_MOVES.toInt()] > 0) {
            return Powerups.getPlayerMoves();
        } else {
            return 1;
        }
    }

    public int getBulletRange() {
        if (this.activePowerupsDurations[Powerups.BULLET_RANGE.toInt()] > 0) {
            return Powerups.getBulletRange();
        } else {
            return 1;
        }
    }

    public int getBulletDamage() {
        if (this.activePowerupsDurations[Powerups.PIERCING_BULLETS.toInt()] > 0) {
            return Powerups.getPiercingBulletsDamage();
        } else {
            return 1;
        }
    }

    public boolean shootsWithPiercingBullets() {
        return this.activePowerupsDurations[Powerups.PIERCING_BULLETS.toInt()] > 0;
    }

    public boolean shootsWithBouncingBullets() {
        return this.activePowerupsDurations[Powerups.BOUNCING_BULLETS.toInt()] > 0;
    }

    public void addPowerup(Powerups powerup) {
        if (powerup == Powerups.HEALTH_BOOST) {
            this.currentHealth += Powerups.getHealthBoost();
        }
        this.powerups[powerup.toInt()] += 1;
    }

    public void usePowerup(Powerups powerup) {
        if (this.powerups[powerup.toInt()] > 0) {
            this.powerups[powerup.toInt()] -= 1;
            this.activePowerupsDurations[powerup.toInt()] = powerup.getDuration();
        }
    }

    public void reduceActivePowerupsDurations() {
        for (int i = 0; i < this.activePowerupsDurations.length; i++) {
            this.activePowerupsDurations[i] -= 1;
        }
    }

    public int getPowerupAmount(Powerups powerups) {
        return this.powerups[powerups.toInt()];
    }

    public int getRemainingPowerupDuration(Powerups powerups) {
        return Math.max(this.activePowerupsDurations[powerups.toInt()], 0);
    }

    public boolean isImmortal() {
        return this.activePowerupsDurations[Powerups.IMMORTALITY.toInt()] > 0;
    }

    public boolean isPlayer() {
        return this.isPlayer;
    }

    @Override
    public int hashCode() {
        return this.position.hashCode();
    }
}
