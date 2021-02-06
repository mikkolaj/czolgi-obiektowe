package agh.cs.czolgi;

public class Obstacle implements IKillableEntity {
    private final static int START_HEALTH = 2;
    private int currentHealth = START_HEALTH;
    private final Vector2d position;

    public Obstacle(Vector2d position) {
        this.position = position;
    }

    public Vector2d getPosition() {
        return this.position;
    }

    public void receiveDamage(int damage) {
        if (damage >= 0) {
            this.currentHealth -= damage;
        }
    }

    public boolean isDead() {
        return this.currentHealth <= 0;
    }

    public boolean isImmortal() {
        return false;
    }
}
