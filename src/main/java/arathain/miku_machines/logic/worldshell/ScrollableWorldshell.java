package arathain.miku_machines.logic.worldshell;

/**
 * An abstract interface representing any {@link Worldshell} that's meant to have a scrollable 'speed' value the player can modify.
 * @see arathain.miku_machines.mixin.MouseMixin
 **/

public interface ScrollableWorldshell {
	float getSpeed();
	void setSpeed(float spd);
	default void addSpeed(double speed) {
		setSpeed(getSpeed()+(float)speed);
	}
}
