package agh.cs.czolgi;

public interface IKillableEntity {
    void receiveDamage(int damage);
    boolean isDead();
    boolean isImmortal();
    Vector2d getPosition();
}
