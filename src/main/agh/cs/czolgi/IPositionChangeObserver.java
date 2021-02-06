package agh.cs.czolgi;

public interface IPositionChangeObserver {
   void tankPositionChanged(Vector2d oldPosition, Vector2d newPosition);
   void bulletPositionChanged(Vector2d oldPosistion, Vector2d newPosition, Bullet bullet);
}
