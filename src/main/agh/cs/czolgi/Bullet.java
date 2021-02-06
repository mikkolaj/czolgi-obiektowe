package agh.cs.czolgi;

import java.util.ArrayList;

public class Bullet {
    private Vector2d position;
    private final int damage;
    private MapDirection direction;
    private final int rangePerRound;
    private final boolean vanishesAfterHit;
    private final boolean bouncesOff;
    private final ArrayList<IPositionChangeObserver> observers = new ArrayList<>();
    private final BattleField battleField;
    private final boolean shotByPlayer;

    public Bullet(Vector2d position, BattleField battleField, int damage, int rangePerRound,
                  MapDirection direction, boolean vanishesAfterHit, boolean bouncesOff, boolean shotByPlayer) {
        this.position = position;
        this.damage = damage;
        this.direction = direction;
        this.rangePerRound = rangePerRound;
        this.vanishesAfterHit = vanishesAfterHit;
        this.bouncesOff = bouncesOff;
        this.battleField = battleField;
        this.shotByPlayer = shotByPlayer;
    }

    public Vector2d getPosition() {
        return this.position;
    }

    public void move() {
        Vector2d oldPos = this.position;
        Vector2d newPos = this.position.add(this.direction.toUnitVector());

        // we can bounce off of an obstacle or a wall
        if ((!this.battleField.insideMap(newPos) && this.bouncesOff)
                || (this.battleField.insideMap(newPos)
                && this.bouncesOff && this.battleField.killableEntityAt(newPos) instanceof Obstacle)) {
            this.bounceOff();
        } else {
            this.position = newPos;
            this.positionChanged(oldPos, newPos);
        }
    }

    public int getDamage() {
        return this.damage;
    }

    public MapDirection getDirection() {
        return this.direction;
    }

    public int getRangePerRound() {
        return this.rangePerRound;
    }

    private void bounceOff() {
        if (this.bouncesOff) {
            this.direction = this.direction.opposite();
        }
    }

    public boolean vanishesAfterHit() {
        return this.vanishesAfterHit;
    }

    public boolean bouncesOff() {
        return this.bouncesOff;
    }

    public void addObserver(IPositionChangeObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(IPositionChangeObserver observer) {
        this.observers.remove(observer);
    }

    private void positionChanged(Vector2d oldPosition, Vector2d newPosition) {
        for (IPositionChangeObserver observer : this.observers) {
            observer.bulletPositionChanged(oldPosition, newPosition, this);
        }
    }

    public boolean wasShotByPlayer() {
        return this.shotByPlayer;
    }
}
