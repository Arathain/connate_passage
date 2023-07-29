package arathain.connatepassage.logic.worldshell;

public interface ScrollableWorldshell {
	float getSpeed();
	void setSpeed(float spd);
	default void addSpeed(double speed) {
		setSpeed(getSpeed()+(float)speed);
	}
}
