package agh.cs.czolgi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

public class BattleField implements IPositionChangeObserver {
    private final int MIN_ENEMY_DISTANCE;
    private final static Random generator = new Random();
    private final ArrayList<IFieldChangeObserver> observers = new ArrayList<>();
    private final Map<Vector2d, IKillableEntity> killableEntityMap = new HashMap<>();
    private final Map<Vector2d, Powerups> powerupMap = new HashMap<>();
    private final Multimap<Vector2d, Bullet> bulletMap = ArrayListMultimap.create();
    private final Vector2d mapLowerLeft;
    private final Vector2d mapUpperRight;
    private final Vector2d mapSize;
    private boolean freeObstacleSpot = true;
    private boolean freeEnemySpot = true;
    private boolean freePowerUpSpot = true;

    public BattleField(int width, int height, int minEnemyDistance) {
        this.mapLowerLeft = new Vector2d(0, 0);
        this.mapUpperRight = new Vector2d(width - 1, height - 1);
        this.mapSize = new Vector2d(width, height);
        this.MIN_ENEMY_DISTANCE = minEnemyDistance;
    }

    public void place(Tank tank) {
        Vector2d position = tank.getPosition();
        if (this.canMoveTo(position)) {
            this.killableEntityMap.put(position, tank);
            this.fieldChanged(position);
            tank.addObserver(this);
        } else {
            throw new IllegalArgumentException("Tank can't be placed on this field: " + position);
        }
    }

    public void placeBullet(Bullet bullet) {
        Vector2d position = bullet.getPosition();
        if (this.insideMap(position)) {
            this.bulletMap.put(position, bullet);
            bullet.addObserver(this);
            this.fieldChanged(position);
        } else {
            throw new IllegalArgumentException("Bullets can't be placed outside the map: " + position);
        }
    }

    public void placeObstacle() {
        if (freeObstacleSpot) {
            Vector2d position = this.drawFreePosition();
            Obstacle obstacle = new Obstacle(position);
            this.killableEntityMap.put(position, obstacle);
            this.fieldChanged(position);
        }
    }

    public void placePowerup() {
        if (freePowerUpSpot) {
            Vector2d position = this.drawFreePosition();
            Powerups powerup = Powerups.toEnum(generator.nextInt(6));
            this.powerupMap.put(position, powerup);
            this.fieldChanged(position);
        }
    }

    public Powerups getPowerupAt(Vector2d position) {
        return this.powerupMap.get(position);
    }

    public void removePowerupAt(Vector2d position) {
        this.powerupMap.remove(position);
    }

    private Vector2d drawFreePosition() {
        Vector2d freePosition;
        do {
            freePosition = new Vector2d(generator.nextInt(this.getMapSize().x), generator.nextInt(this.getMapSize().y));
        } while (this.objectAt(freePosition) != null);
        return freePosition;
    }

    public Vector2d drawTankPosition(Tank player) {
        Vector2d tankPosition;
        do {
            tankPosition = new Vector2d(generator.nextInt(this.getMapSize().x), generator.nextInt(this.getMapSize().y));
        } while (this.objectAt(tankPosition) != null || this.enemyToClose(player.getPosition(), tankPosition));
        return tankPosition;
    }


    public boolean insideMap(Vector2d position) {
        return (position.follows(this.mapLowerLeft) && position.precedes(this.mapUpperRight));
    }


    public void removeBulletsAt(Vector2d position) {
        List<Bullet> vanishedBullets;

        vanishedBullets = this.bulletMap.get(position).stream().filter(Bullet::vanishesAfterHit).collect(Collectors.toList());

        for (Bullet bulllet : vanishedBullets) {
            this.bulletMap.remove(position, bulllet);
        }
    }

    public Object objectAt(Vector2d position) {
        IKillableEntity killableEntity = killableEntityMap.get(position);
        if (killableEntity != null) {
            return killableEntity;
        } else {
            Collection<Bullet> bullets = this.bulletMap.get(position);
            return !bullets.isEmpty() ? bullets.iterator().next() : this.powerupMap.get(position);
        }
    }

    public IKillableEntity killableEntityAt(Vector2d position) {
        return killableEntityMap.get(position);
    }

    public void removeKillableEntityAt(Vector2d position) {
        if (this.killableEntityMap.get(position).isDead()) {
            this.killableEntityMap.remove(position);
            this.fieldChanged(position);
        }
    }

    public boolean canMoveTo(Vector2d position) {
        if(this.insideMap(position)) {
            Object object = objectAt(position);
            return object == null || object instanceof Powerups;
        } else {
            return false;
        }
    }

    // determine if it is possible to place an enemy, obstacle or powerup on the map
    public void findFreeSpots(Vector2d playerPos) {
        this.freeObstacleSpot = false;
        this.freeEnemySpot = false;
        this.freePowerUpSpot = false;
        for (int i = this.mapLowerLeft.x; i <= this.mapUpperRight.x; i++) {
            for (int j = this.mapLowerLeft.y; j <= this.mapUpperRight.y; j++) {
                Vector2d position = new Vector2d(i, j);
                if (this.objectAt(position) == null) {
                    if (!this.enemyToClose(playerPos, position) && !this.freeEnemySpot) {
                        this.freeEnemySpot = true;
                    } else if (!freeObstacleSpot) {
                        this.freeObstacleSpot = true;
                    } else {
                        this.freePowerUpSpot = true;
                    }
                }
            }
        }
    }

    // we need to inform our map about tanks and bullets changing their positions
    public void tankPositionChanged(Vector2d oldPosition, Vector2d newPosition) {
        if (!oldPosition.equals(newPosition)) {

            IKillableEntity tankToReposition = this.killableEntityMap.get(oldPosition);
            if (tankToReposition != null) {
                this.killableEntityMap.remove(oldPosition, tankToReposition);
                this.killableEntityMap.put(newPosition, tankToReposition);
                this.fieldChanged(oldPosition);
                this.fieldChanged(newPosition);
            }
        }
    }

    public void bulletPositionChanged(Vector2d oldPosition, Vector2d newPosition, Bullet bullet) {
        this.bulletMap.remove(oldPosition, bullet);
        this.fieldChanged(oldPosition);
        if (this.insideMap(newPosition)) {
            this.bulletMap.put(newPosition, bullet);
            this.fieldChanged(newPosition);
        }
    }

    private boolean enemyToClose(Vector2d playerPos, Vector2d enemyPos) {
        return (Math.abs(playerPos.x - enemyPos.x) <= MIN_ENEMY_DISTANCE && Math.abs(playerPos.y - enemyPos.y) <= MIN_ENEMY_DISTANCE);
    }

    public void addObserver(IFieldChangeObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(IFieldChangeObserver observer) {
        this.observers.remove(observer);
    }

    // inform observers of the map about a change on a field
    public void fieldChanged(Vector2d position) {
        for (IFieldChangeObserver observer : this.observers) {
            observer.fieldChanged(position);
        }
    }

    public Vector2d getMapSize() {
        return this.mapSize;
    }

    public boolean hasFreeEnemySpot() {
        return this.freeEnemySpot;
    }

    public boolean hasFreeObstacleSpot() {
        return this.freeObstacleSpot;
    }

    public boolean hasFreePowerupSpot() {
        return this.freePowerUpSpot;
    }
}
