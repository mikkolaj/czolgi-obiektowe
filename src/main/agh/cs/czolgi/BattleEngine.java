package agh.cs.czolgi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BattleEngine{
    private final Map<Vector2d, Tank> enemyTankMap = new HashMap<>();
    private final Multimap<Vector2d, Bullet> bulletMap = ArrayListMultimap.create();
    private final BattleField battleField;
    private boolean isPaused = false;
    private AtomicInteger moveCount = new AtomicInteger(1);
    private AtomicInteger baseMoveCount = new AtomicInteger(1);
    private final BattleGUI battleGUI;
    private final Tank player;
    private int score = 0;

    private final static Random generator = new Random();
    private final static int ROUND_DELAY = 40;
    private final static int MAX_DAYS_WITHOUT_OBSTACLE = 5;
    private final static int MAX_DAYS_WITHOUT_ENEMY = 60;
    private final static int MAX_DAYS_WITHOUT_POWERUP = 20;
    private int daysWithoutObstacle = 0;
    private int daysWithoutEnemy = MAX_DAYS_WITHOUT_ENEMY;
    private int daysWithoutPowerup = MAX_DAYS_WITHOUT_POWERUP;
    private final boolean placeObstacles;
    private final boolean placePowerups;
    private final boolean enemiesSpawnWithPowerups;
    private final boolean enemiesCollectPowerups;
    private final static MapDirection[] movePossibilities = {MapDirection.NORTH, MapDirection.EAST, MapDirection.SOUTH, MapDirection.WEST};


    public BattleEngine(Stage primaryStage, BattleField battleField, boolean placeObstacles, boolean placePowerups,
                        boolean enemiesSpawnWithPowerups, boolean enemiesCollectPowerups) {
        this.placeObstacles = placeObstacles;
        this.placePowerups = placePowerups;
        this.enemiesSpawnWithPowerups = enemiesSpawnWithPowerups;
        this.enemiesCollectPowerups = enemiesCollectPowerups;

        Vector2d battleFieldSize = battleField.getMapSize();
        Vector2d initialPosition = new Vector2d(battleFieldSize.x / 2, battleFieldSize.y / 2);

        Tank player = new Tank(battleField, initialPosition, true, false);
        this.player = player;

        this.battleGUI = new BattleGUI(primaryStage, this, player,
                battleField, battleFieldSize.x, battleFieldSize.y);
        this.battleField = battleField;
        battleField.place(player);
    }


    public void run() {
        // new Thread not to block refreshing of the GUI
        new Thread(() -> {
            while (true) {
                if (this.player.isDead()) {
                    this.battleGUI.updateStats();
                    break;
                }

                this.battleField.findFreeSpots(this.player.getPosition());
                this.placeObjects();
                this.battleGUI.updatePowerups();
                this.processPlayersActions();
                this.moveBullets();
                this.moveEnemies();

                try {
                    Thread.sleep(ROUND_DELAY);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            Platform.runLater(this.battleGUI::showGameOverAlert);
        }).start();
    }

    private void processPlayersActions() {
        this.moveCount = new AtomicInteger(this.player.getMoveCount());
        this.baseMoveCount = this.moveCount;
        this.pauseBattle();

        while (this.isPaused) {
            while (this.moveCount.intValue() > 0) {
                Thread.onSpinWait();
            }
            this.player.reduceActivePowerupsDurations();
            this.resumeBattle();
        }
    }

    // place enemies, obstacles and powerups on the map
    private void placeObjects() {
        if ((this.enemyTankMap.values().size() == 0
                || daysWithoutEnemy >= MAX_DAYS_WITHOUT_ENEMY
                || generator.nextFloat() < 1.0 / MAX_DAYS_WITHOUT_ENEMY)
                && this.battleField.hasFreeEnemySpot()
        ) {
            Tank enemy = new Tank(battleField, battleField.drawTankPosition(player), false, this.enemiesSpawnWithPowerups);
            this.enemyTankMap.put(enemy.getPosition(), enemy);
            this.battleField.place(enemy);
            this.daysWithoutEnemy = 0;
        } else {
            this.daysWithoutEnemy += 1;
        }

        if(this.placeObstacles) {
            if ((this.daysWithoutObstacle >= MAX_DAYS_WITHOUT_OBSTACLE
                    || generator.nextFloat() < 1.0 / MAX_DAYS_WITHOUT_OBSTACLE)
                    && this.battleField.hasFreeObstacleSpot()
            ) {
                this.battleField.placeObstacle();
                this.daysWithoutObstacle = 0;
            } else {
                this.daysWithoutObstacle += 1;
            }
        }

        if(this.placePowerups){
            if ((this.daysWithoutPowerup >= MAX_DAYS_WITHOUT_POWERUP
                    || generator.nextFloat() < 1.0 / MAX_DAYS_WITHOUT_POWERUP)
                    && this.battleField.hasFreePowerupSpot()
            ) {
                this.battleField.placePowerup();
                this.daysWithoutPowerup = 0;
            } else {
                this.daysWithoutPowerup += 1;
            }
        }
    }

    private void moveBullets() {
        List<Bullet> bullets = new ArrayList<>(this.bulletMap.values());
        for (Bullet bullet : bullets) {
            int range = bullet.getRangePerRound();

            for (int i = 0; i < range && this.battleField.insideMap(bullet.getPosition()); i++) {
                Vector2d oldPos = bullet.getPosition();
                MapDirection direction = bullet.getDirection();
                bullet.move();
                Vector2d newPos = bullet.getPosition();

                // if a bullet bounced off it stays in the same position and deals damage
                // to the object it bounced off of
                if (oldPos.equals(newPos)) {
                    IKillableEntity killableEntity = this.battleField
                            .killableEntityAt(oldPos.add(direction.toUnitVector()));
                    if (killableEntity instanceof Obstacle) {
                        killableEntity.receiveDamage(bullet.getDamage());
                        this.battleField.removeKillableEntityAt(killableEntity.getPosition());
                    }
                } else {
                    // otherwise we move the bullet
                    this.bulletMap.remove(oldPos, bullet);
                    if (this.battleField.insideMap(newPos)) {
                        this.bulletMap.put(newPos, bullet);
                        this.dealDamageAndRemoveDeadEntity(newPos);
                    }
                }
            }
        }
    }

    private void moveEnemies() {
        List<Tank> tanks = new ArrayList<>(this.enemyTankMap.values());
        for (Tank tank : tanks) {
            this.performEnemyAction(tank);
            tank.reduceActivePowerupsDurations();
        }
    }

    // enemies have equal chance each round to shoot towards the player or decrease the distance between them
    private void performEnemyAction(Tank tank) {
        double decision = Math.random();
        MapDirection possibleMoveDirection = this.getPossibleMoveDirection(tank);
        if (decision < 0.5 && possibleMoveDirection != null) {
            Vector2d oldPos = tank.getPosition();
            tank.move(possibleMoveDirection);
            Vector2d newPos = tank.getPosition();

            if (this.enemiesCollectPowerups) {
                Powerups powerup = this.battleField.getPowerupAt(newPos);
                if (powerup != null) {
                    this.battleField.removePowerupAt(newPos);
                    tank.addPowerup(powerup);
                    tank.usePowerup(powerup);
                }
            }

            this.enemyTankMap.remove(oldPos, tank);
            this.enemyTankMap.put(newPos, tank);
        } else {
            // choose shot direction that is closest to hitting a player
            double angle = tank.getPosition().getAngleTo(this.player.getPosition());
            long directionInt = Math.round(angle / (2 * Math.PI) * 8);
            if (directionInt == 8) {
                directionInt = 0;
            }
            MapDirection shotDirection = MapDirection.parseDirection((int) directionInt);
            shootBullet(tank, shotDirection);
        }
    }

    private void shootBullet(Tank tank, MapDirection shotDirection) {
        Vector2d bulletPosition = tank.getPosition().add(shotDirection.toUnitVector());
        int range = tank.getBulletRange();
        int damage = tank.getBulletDamage();
        boolean vanishesAfterHit = !tank.shootsWithPiercingBullets();
        boolean bouncesOff = tank.shootsWithBouncingBullets();
        Bullet bullet = new Bullet(bulletPosition, this.battleField, damage,
                range, shotDirection, vanishesAfterHit, bouncesOff, tank.equals(this.player));
        this.bulletMap.put(bulletPosition, bullet);
        this.battleField.placeBullet(bullet);
        this.dealDamageAndRemoveDeadEntity(bulletPosition);
    }

    // check if it is possible to move in a direction that will get the enemy closer to the player
    private MapDirection getPossibleMoveDirection(Tank tank) {
        for (MapDirection direction : movePossibilities) {
            Vector2d newPos = tank.getPosition().add(direction.toUnitVector());
            if (this.battleField.canMoveTo(newPos) &&
                    tank.getPosition().calculateDistanceTo(this.player.getPosition()) >
                            newPos.calculateDistanceTo(this.player.getPosition())) {
                return direction;
            }
        }
        return null;
    }


    private boolean dealDamageAt(Vector2d position) {
        int sum = 0;
        for (Bullet bullet : this.bulletMap.get(position)) {
            sum += bullet.getDamage();
        }
        if (sum > 0) {
            IKillableEntity entity = this.battleField.killableEntityAt(position);
            if (entity != null && !entity.isImmortal()) {
                entity.receiveDamage(sum);
                return true;
            }
        }
        return false;
    }


    private void dealDamageAndRemoveDeadEntity(Vector2d position) {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        if (this.dealDamageAt(position)) {
            bulletsToRemove = this.bulletMap.get(position).stream().filter(Bullet::vanishesAfterHit).collect(Collectors.toList());

            this.battleField.removeBulletsAt(position);
            this.battleField.removeKillableEntityAt(position);
            Tank tankAt = this.enemyTankMap.get(position);
            if (this.battleField.killableEntityAt(position) == null && tankAt != null) {
                this.enemyTankMap.remove(position);
                if (bulletsToRemove.stream().anyMatch(Bullet::wasShotByPlayer)) {
                    this.score += 1;
                    this.battleGUI.updateStats();
                }
            }
        }

        for (Bullet bullet : bulletsToRemove) {
            this.bulletMap.remove(position, bullet);
        }
    }

    private void pauseBattle() {
        this.isPaused = true;
    }

    private void resumeBattle() {
        this.isPaused = false;
    }

    public void movePlayer(MapDirection direction) {
        Vector2d newPos = this.player.getPosition().add(direction.toUnitVector());
        if (isPaused && this.battleField.canMoveTo(newPos)) {
            // check if player used PLAYER_MOVES powerup and add additional moves if necessary
            if (this.player.getMoveCount() > this.baseMoveCount.intValue()) {
                this.moveCount.addAndGet(this.player.getMoveCount() - this.baseMoveCount.intValue());
                this.baseMoveCount = new AtomicInteger(this.player.getMoveCount());
            }

            this.player.move(direction);

            // automatically collect powerups
            Powerups powerup = this.battleField.getPowerupAt(newPos);
            if (powerup != null) {
                this.battleField.removePowerupAt(newPos);
                this.player.addPowerup(powerup);
                if (powerup == Powerups.HEALTH_BOOST) {
                    this.battleGUI.updateStats();
                }
            }
            this.moveCount.getAndDecrement();
        }
    }

    public void shoot(MapDirection shotDirection) {
        if (isPaused && this.battleField.insideMap(this.player.getPosition().add(shotDirection.toUnitVector()))) {
            shootBullet(this.player, shotDirection);
            this.moveCount.getAndDecrement();
        }
    }

    public int getScore() {
        return this.score;
    }
}
